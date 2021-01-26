package enginecrafter77.survivalinc.strugglecraft;

import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.stats.StatCapability;
import enginecrafter77.survivalinc.stats.impl.SanityTendencyModifier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

public class AutoShutdown {

	int shutdownCountdown= 0;
	
	
	public static AutoShutdown instance= new AutoShutdown(); 

	
	public static void init()
	{
		MinecraftForge.EVENT_BUS.register(AutoShutdown.class);
		
		
	}
	
	@SubscribeEvent
	public static void onLogout(PlayerLoggedOutEvent event)
	{
		if(!event.player.world.isRemote)
		{
			int numPlayers= event.player.world.getMinecraftServer().getCurrentPlayerCount();

			System.out.println("DEBUG: player logged out ("+(numPlayers-1)+" left)");
			
			if(numPlayers == 1)
			{
				System.out.println("Auto Shutdown countdown starting");
				instance.shutdownCountdown= ModConfig.AUTO_SHUTDOWN.ticks;
			}
		}
	}
	
	
	@SubscribeEvent
	public static void onSpawn(PlayerLoggedInEvent event)
	{
		System.out.println("DEBUG: (Auto Shutdown class) "+event.player.getDisplayNameString()+" logged in");
		
		if(event.player.world.isRemote)
			return;

		instance.shutdownCountdown= 0;
	}

	@SubscribeEvent
	public static void onTick(WorldTickEvent event)
	{
		if(instance.shutdownCountdown > 0 && event.phase == Phase.START)
		{
			instance.shutdownCountdown--;
			if(instance.shutdownCountdown == 0)
			{
				System.out.println("Auto Shutdown initiated");

				FMLCommonHandler.instance().getMinecraftServerInstance().initiateShutdown();
			}
		}
	}

	@SubscribeEvent
	public static void onLoad(WorldEvent.Load event)
	{
		int ticks= ModConfig.AUTO_SHUTDOWN.ticks * 5;
		
		System.out.println("Auto Shutdown will close server if noone connects within "+(ticks/20)+" seconds");
		
		instance.shutdownCountdown= ticks;
		
	}
	
}
