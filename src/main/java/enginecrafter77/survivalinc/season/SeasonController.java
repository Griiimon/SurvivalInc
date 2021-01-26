package enginecrafter77.survivalinc.season;

import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeEnd;
import net.minecraft.world.biome.BiomeHell;
import net.minecraft.world.biome.BiomeOcean;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import enginecrafter77.survivalinc.SurvivalInc;

public class SeasonController implements IMessageHandler<SeasonSyncMessage, IMessage> {
	
	/** The length of one full minecraft day/night cycle */
	public static final int minecraftDayLength = 24000;
	
	/** The season controller singleton */
	public static final SeasonController instance = new SeasonController();
	
	/** The biome temperature controller instance */
	public BiomeTempController biomeTemp;
	
	private SeasonController()
	{
		try
		{
			this.biomeTemp = new BiomeTempController();
			this.biomeTemp.excluded.add(BiomeOcean.class);
			this.biomeTemp.excluded.add(BiomeHell.class);
			this.biomeTemp.excluded.add(BiomeEnd.class);
		}
		catch(NoSuchFieldException exc)
		{
			RuntimeException nexc = new RuntimeException();
			nexc.initCause(exc);
			throw nexc;
		}
	}
	
	public void notifyClient(EntityPlayerMP client)
	{
		SeasonData data = SeasonData.load(client.world);
		SurvivalInc.logger.info("Sending season data ({}) to player \"{}\", who just entered overworld", data.toString(), client.getDisplayNameString());
		SurvivalInc.proxy.net.sendTo(new SeasonSyncMessage(data), client);
	}
	
	// Process the event and broadcast it on client
	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(SeasonSyncMessage message, MessageContext ctx)
	{
		WorldClient world = Minecraft.getMinecraft().world;
		if(world == null) SurvivalInc.logger.error("Minecraft remote world tracking instance is null. This is NOT a good thing. Season sync packet dropped...");
		else if(world.provider.getDimensionType() != DimensionType.OVERWORLD) throw new RuntimeException("Received a stat update message from outside overworld.");
		else
		{
			SurvivalInc.logger.info("Updateding client's world season data to {}...", message.data.toString());
			world.setData(SeasonData.datakey, message.data);
			MinecraftForge.EVENT_BUS.post(new SeasonChangedEvent(world, message.data));
			SurvivalInc.logger.info("Updated client's world season data to {}", message.data.toString());
		}
		return null;
	}
	
	/**
	 * Sends the season data to new players in overworld
	 * @param event The player logged in event
	 */
	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
	{
		if(!event.player.world.isRemote && event.player.world.provider.getDimensionType() == DimensionType.OVERWORLD)
			this.notifyClient((EntityPlayerMP)event.player);
	}
	
	/**
	 * Sends the season data to players entering overworld
	 * @param event The player logged in event
	 */
	@SubscribeEvent
	public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event)
	{
		if(!event.player.world.isRemote && event.toDim == DimensionType.OVERWORLD.getId())
			this.notifyClient((EntityPlayerMP)event.player);
	}
	
	/**
	 * Load the season data from disk on server side.
	 * @param event The world load event
	 */
	@SubscribeEvent
	public void loadSeasonData(WorldEvent.Load event)
	{
		World world = event.getWorld();
		if(!world.isRemote && world.provider.getDimensionType() == DimensionType.OVERWORLD) MinecraftForge.EVENT_BUS.post(new SeasonChangedEvent(world));
	}
	
	/**
	 * Updates the season data on server. Unfortunately,
	 * this method won't work for clients.
	 * @param event The world tick event
	 */
	@SubscribeEvent
	public void onUpdate(TickEvent.WorldTickEvent event)
	{
		DimensionType dimension = event.world.provider.getDimensionType();
		if(event.side == Side.SERVER && event.phase == TickEvent.Phase.START && dimension == DimensionType.OVERWORLD)
		{
			// Is it early morning? Also, before the player really joins, some time passes. Give the player some time to actually receive the update.
			if(event.world.getWorldTime() % SeasonController.minecraftDayLength == 1200)
			{
				SurvivalInc.logger.info("Season update triggered in {} on {}", dimension.name(), event.side.name());
				SeasonData data = SeasonData.load(event.world);
				data.advance(1);
				data.markDirty();
				
				SurvivalInc.proxy.net.sendToDimension(new SeasonSyncMessage(data), DimensionType.OVERWORLD.getId()); // Synchronize the data with clients, so they being processing on their side
				MinecraftForge.EVENT_BUS.post(new SeasonChangedEvent(event.world, data)); // Broadcast and process the event on server side
			}
		}
	}
	
	/**
	 * Capture the season update event and apply biome temperatures
	 * @param event The season changed event
	 */
	@SubscribeEvent
	public void applySeasonData(SeasonChangedEvent event)
	{
		WorldInfo info = event.getWorld().getWorldInfo();
		this.biomeTemp.applySeason(event.data);
		
		// Determine the weather. The season is the main factor.
		float randWeather = (float) Math.random();
		if(randWeather < event.getSeason().rainfallchance && info.isRaining())
			info.setRaining(true);
		
		SurvivalInc.logger.info("Applied biome temperatures for season {}", event.data.toString());
	}
	
	/**
	 * Determines the color of the grass in the said biome
	 * @param event The grass color event
	 */
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void biomeGrass(BiomeEvent.GetGrassColor event)
	{
		float deftemp = event.getBiome().getDefaultTemperature();
		
		// If the current biome is permafrost
		if((deftemp + Season.SUMMER.getPeakTemperatureOffset()) < 0F)
			event.setNewColor(Season.WINTER.grasscolor);
		else if((deftemp + Season.WINTER.getPeakTemperatureOffset()) > 1F)
			event.setNewColor(Season.SUMMER.grasscolor);
		else
		{
			WorldClient world = Minecraft.getMinecraft().world;
			SeasonData data = SeasonData.load(world);
			
			if(data.season.grasscolor != 0)
				event.setNewColor(data.season.grasscolor);
		}
		
	}

}