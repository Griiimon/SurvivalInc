package enginecrafter77.survivalinc;

import java.text.DecimalFormat;

import enginecrafter77.survivalinc.client.DifferentialArrow;
import enginecrafter77.survivalinc.client.Direction2D;
import enginecrafter77.survivalinc.client.ElementPositioner;
import enginecrafter77.survivalinc.client.RenderHUD;
import enginecrafter77.survivalinc.client.StatFillBar;
import enginecrafter77.survivalinc.client.Thermometer;
import enginecrafter77.survivalinc.client.TextureResource;
import enginecrafter77.survivalinc.client.TexturedColorElement;
import enginecrafter77.survivalinc.client.TexturedElement;
import enginecrafter77.survivalinc.client.TranslateRenderFilter;
import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.ghost.GhostEnergyBar;
import enginecrafter77.survivalinc.ghost.GhostUIRenderFilter;
import enginecrafter77.survivalinc.ghost.RenderGhost;
import enginecrafter77.survivalinc.season.LeafColorer;
import enginecrafter77.survivalinc.stats.SimpleStatRecord;
import enginecrafter77.survivalinc.stats.impl.HeatModifier;
import enginecrafter77.survivalinc.stats.impl.HydrationModifier;
import enginecrafter77.survivalinc.stats.impl.SanityModifier;
import enginecrafter77.survivalinc.stats.impl.SanityTendencyModifier;
import enginecrafter77.survivalinc.stats.impl.SanityRecord;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class ClientProxy extends CommonProxy {
	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		super.preInit(event);
		
		if(ModConfig.SEASONS.enabled) MinecraftForge.EVENT_BUS.register(LeafColorer.instance);
	}
	
	@Override
	public void init(FMLInitializationEvent event)
	{
		super.init(event);
		
		TextureResource newicons = new TextureResource(new ResourceLocation(SurvivalInc.MOD_ID, "textures/gui/staticons.png"), 18, 18);
		TextureResource sanityicon = new TextureResource(new ResourceLocation(SurvivalInc.MOD_ID, "textures/gui/sanity.png"), 32, 16);
		TextureResource hastepushicon = new TextureResource(new ResourceLocation(SurvivalInc.MOD_ID, "textures/gui/hastepush.png"), 32, 16);
		TextureResource thermometericon = new TextureResource(new ResourceLocation(SurvivalInc.MOD_ID, "textures/gui/thermometer.png"), 16, 32);
		TranslateRenderFilter moveup = new TranslateRenderFilter(new ElementPositioner(0F, 0F, 0, -10));
		if(ModConfig.HEAT.enabled)
		{
			StatFillBar<SimpleStatRecord> bar = new StatFillBar<SimpleStatRecord>(HeatModifier.instance, SimpleStatRecord.class, Direction2D.RIGHT, new TexturedElement(newicons, 0, 0, 9, 9, true));
			bar.addOverlay(new TexturedElement(newicons, 9, 0, 9, 9, true), SimpleStatRecord::getNormalizedValue);
			bar.setCapacity(10);
			bar.setSpacing(-1);
			RenderHUD.instance.addIndependent(bar, new ElementPositioner(0.5F, 1F, 10, -49));
			RenderHUD.instance.addFilter(moveup, ElementType.ARMOR);
			
			// TODO ...
			Thermometer<SimpleStatRecord> thermo = new Thermometer<SimpleStatRecord>(Direction2D.UP, new TexturedElement(thermometericon, 0, 0, 8, 32, true));
			thermo.addOverlay(new TexturedElement(thermometericon, 8, 0, 8, 32, true));
			thermo.setCapacity(1);
			RenderHUD.instance.addIndependent(thermo, new ElementPositioner(0.5F, 1F, -100, -31));
			RenderHUD.instance.addFilter(moveup, ElementType.SUBTITLES);
		}
		if(ModConfig.HYDRATION.enabled)
		{
			StatFillBar<SimpleStatRecord> bar = new StatFillBar<SimpleStatRecord>(HydrationModifier.instance, SimpleStatRecord.class, Direction2D.LEFT, new TexturedElement(newicons, 0, 9, 9, 9, true));
			bar.addOverlay(new TexturedElement(newicons, 9, 9, 9, 9, true), SimpleStatRecord::getNormalizedValue);
			bar.setCapacity(10);
			bar.setSpacing(-1);
			RenderHUD.instance.addIndependent(bar, new ElementPositioner(0.5F, 1F, -91, -49));
			RenderHUD.instance.addFilter(moveup, ElementType.AIR);
		}
		if(ModConfig.SANITY.enabled)
		{
			StatFillBar<SimpleStatRecord> bar = new StatFillBar<SimpleStatRecord>(SanityModifier.instance, SimpleStatRecord.class, Direction2D.UP, new TexturedElement(sanityicon, 0, 0, 16, 16, true));
			bar.addOverlay(new TexturedElement(sanityicon, 16, 0, 16, 16, true), SimpleStatRecord::getNormalizedValue);
			bar.setCapacity(1);
			RenderHUD.instance.addIndependent(bar, new ElementPositioner(0.5F, 1F, -8, -51));
			RenderHUD.instance.addFilter(moveup, ElementType.SUBTITLES);
			
			DifferentialArrow arrow= new DifferentialArrow(SanityTendencyModifier.instance, 8,12,true);
			RenderHUD.instance.addIndependent(arrow, new ElementPositioner(0.5F, 1F, -2, -51));
			
			// TODO use via GUI_ID or something
			TexturedColorElement hastePushEffect= new TexturedColorElement(hastepushicon, 0, 0, 16, 16, true);
			RenderHUD.instance.addIndependent(hastePushEffect, new ElementPositioner(0.5F, 1F, -8, -59));
		}
		if(ModConfig.GHOST.enabled)
		{
			RenderHUD.instance.addIndependent(new GhostEnergyBar(), new ElementPositioner(0.5F, 1F, -91, -39));
			RenderHUD.instance.addFilterToAll(new GhostUIRenderFilter(), ElementType.HEALTH, ElementType.AIR, ElementType.ARMOR, ElementType.FOOD);
		}
		
	}
	
	@Override
	public void postInit(FMLPostInitializationEvent event)
	{
		super.postInit(event);
		
		if(RenderHUD.instance.isUseful()) MinecraftForge.EVENT_BUS.register(RenderHUD.instance);
		if(ModConfig.GHOST.enabled) MinecraftForge.EVENT_BUS.register(new RenderGhost());
	}

	@Override
	public void AddReasonToClient(float value, String reason, boolean forceAdd)
	{
		SanityTendencyModifier.instance.addReason(value, reason, forceAdd, true);
	}

	@Override
	public void SanityOverviewOnClient(EntityPlayer player)
	{
		int passedTicks= (int)(Minecraft.getMinecraft().world.getTotalWorldTime() - SanityTendencyModifier.instance.overviewTimestamp);

		DecimalFormat f = new DecimalFormat("##.00");
		
		Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Overview ("+(passedTicks / 1200)+" minutes):"));

		for(String reason : SanityTendencyModifier.instance.overview.keySet())
		{
			float value= SanityTendencyModifier.instance.overview.get(reason);
			float perMin= passedTicks == 0 ? 0 : (float)value / ((float)passedTicks / 1200f);
			String formVal= Math.abs(value) < 1 ? "0" : "" + f.format(value);
			String formValMin= Math.abs(perMin) < 1 ? "0" : "" + f.format(perMin);
			
			String str= (value > 0f ? "+" : "")+formVal+" ( "+formValMin+"/min )";
			
			Minecraft.getMinecraft().player.sendMessage(new TextComponentString(reason+" "+str));
		}
		
		
		SanityTendencyModifier.instance.ClearOverviewData(Minecraft.getMinecraft().world);
	}
	
	
}