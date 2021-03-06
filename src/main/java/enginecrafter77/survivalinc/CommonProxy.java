package enginecrafter77.survivalinc;

import enginecrafter77.survivalinc.block.BlockMelting;
import enginecrafter77.survivalinc.client.GuiHandler;
import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.debug.HeatDebugCommand;
import enginecrafter77.survivalinc.debug.LightDebugCommand;
import enginecrafter77.survivalinc.debug.MonumentRaytraceDebugCommands;
import enginecrafter77.survivalinc.debug.SanityDebugCommand;
import enginecrafter77.survivalinc.ghost.GhostCommand;
import enginecrafter77.survivalinc.ghost.GhostProvider;
import enginecrafter77.survivalinc.net.DebugToggleMessage;
import enginecrafter77.survivalinc.net.DebugToggleUpdater;
import enginecrafter77.survivalinc.net.EntityItemUpdateMessage;
import enginecrafter77.survivalinc.net.EntityItemUpdater;
import enginecrafter77.survivalinc.net.SanityOverviewMessage;
import enginecrafter77.survivalinc.net.SanityOverviewReceiver;
import enginecrafter77.survivalinc.net.SanityReasonMessage;
import enginecrafter77.survivalinc.net.StatSyncMessage;
import enginecrafter77.survivalinc.net.StatSyncHandler;
import enginecrafter77.survivalinc.net.WaterDrinkMessage;
import enginecrafter77.survivalinc.season.SeasonCommand;
import enginecrafter77.survivalinc.season.SeasonController;
import enginecrafter77.survivalinc.season.SeasonSyncMessage;
import enginecrafter77.survivalinc.season.melting.MeltingController;
import enginecrafter77.survivalinc.season.melting.MeltingController.MelterEntry;
import enginecrafter77.survivalinc.stats.StatCommand;
import enginecrafter77.survivalinc.stats.StatRegisterDispatcher;
import enginecrafter77.survivalinc.stats.StatStorage;
import enginecrafter77.survivalinc.stats.StatTracker;
import enginecrafter77.survivalinc.stats.impl.HeatModifier;
import enginecrafter77.survivalinc.stats.impl.HydrationModifier;
import enginecrafter77.survivalinc.stats.impl.SanityModifier;
import enginecrafter77.survivalinc.stats.impl.SanityTendencyModifier;
import enginecrafter77.survivalinc.stats.impl.WetnessModifier;
import enginecrafter77.survivalinc.strugglecraft.*;
import net.minecraft.command.CommandHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy {
	
	public SimpleNetworkWrapper net;
	
	public void preInit(FMLPreInitializationEvent event)
	{
		// Register seasons if enabled
		if(ModConfig.SEASONS.enabled)
		{
			MinecraftForge.EVENT_BUS.register(SeasonController.instance);
			
			MeltingController.meltmap.add(new MelterEntry((BlockMelting)ModBlocks.MELTING_SNOW.get()).level(1, true)); // 1 = block above ground
			MeltingController.meltmap.add(new MelterEntry((BlockMelting)ModBlocks.MELTING_ICE.get()).level(0, true)); // 0 = ground
		}
		
		// Register capabilities.
		CapabilityManager.INSTANCE.register(StatTracker.class, StatStorage.instance, StatRegisterDispatcher.instance);
		
		MinecraftForge.EVENT_BUS.register(new DeathCounter());
		
		MinecraftForge.TERRAIN_GEN_BUS.register(new BiomeDecoratorDrugs());
	}

	public void init(FMLInitializationEvent event)
	{
		this.net = NetworkRegistry.INSTANCE.newSimpleChannel(SurvivalInc.MOD_ID);
		this.net.registerMessage(StatSyncHandler.class, StatSyncMessage.class, 0, Side.CLIENT);
		this.net.registerMessage(SeasonController.instance, SeasonSyncMessage.class, 1, Side.CLIENT);
		this.net.registerMessage(EntityItemUpdater.class, EntityItemUpdateMessage.class, 2, Side.CLIENT);
		//this.net.registerMessage(GhostUpdateMessageHandler.class, GhostUpdateMessage.class, 3, Side.CLIENT);
		this.net.registerMessage(HydrationModifier.class, WaterDrinkMessage.class, 3, Side.SERVER);

		
		this.net.registerMessage(DebugToggleUpdater.class, DebugToggleMessage.class, 4, Side.CLIENT);
		this.net.registerMessage(SanityTendencyModifier.class, SanityReasonMessage.class, 5, Side.CLIENT);
		this.net.registerMessage(SanityOverviewReceiver.class, SanityOverviewMessage.class, 6, Side.CLIENT);
		
		if(ModConfig.HEAT.enabled) HeatModifier.instance.init();
		if(ModConfig.HYDRATION.enabled) HydrationModifier.instance.init();
		if(ModConfig.SANITY.enabled)
		{
			SanityModifier.instance.init();
			SanityTendencyModifier.instance.init();
		}
		if(ModConfig.WETNESS.enabled) WetnessModifier.instance.init();
		if(ModConfig.GHOST.enabled) GhostProvider.instance.init();
		if(ModConfig.TRAITS.enabled) TraitModule.instance.init();
		
		if(ModConfig.FOOD.enabled) FoodModule.instance.init();
		if(ModConfig.TWEAKS.enabled) Tweaks.instance.init();
		if(ModConfig.DRUGS.enabled) DrugModule.instance.init();
		
		if(ModConfig.AUTO_SHUTDOWN.enabled) AutoShutdown.init();
		

	}
	
	public void postInit(FMLPostInitializationEvent event)
	{
		if(ModConfig.SEASONS.enabled && ModConfig.SEASONS.meltController.isValid())
		{
			MeltingController.compile(ModConfig.SEASONS.meltController);
			MinecraftForge.EVENT_BUS.register(MeltingController.class);
		}
		
		Tweaks.postInit();
	}

	public void serverStarting(FMLServerStartingEvent event)
	{
		MinecraftServer server = event.getServer();
		CommandHandler manager = (CommandHandler)server.getCommandManager();
		
		if(ModConfig.SEASONS.enabled) manager.registerCommand(new SeasonCommand());
		if(ModConfig.GHOST.enabled) manager.registerCommand(new GhostCommand());
		if(ModConfig.SANITY.enabled) manager.registerCommand(new SanityDebugCommand());
		if(ModConfig.SANITY.enabled) manager.registerCommand(new SanityOverviewCommand());
		if(ModConfig.SANITY.enabled) manager.registerCommand(new BoosterBlockCommand());
		if(ModConfig.SANITY.enabled) manager.registerCommand(new BoosterCalcCommand());
		if(ModConfig.TRAITS.enabled) manager.registerCommand(new TraitsCommand());
		if(ModConfig.TRAITS.enabled) manager.registerCommand(new DeathsCommand());
		if(ModConfig.FOOD.enabled) manager.registerCommand(new FavouriteFoodCommand());
		if(ModConfig.FOOD.enabled) manager.registerCommand(new HateFoodCommand());
		if(ModConfig.FOOD.enabled) manager.registerCommand(new KnownFoodCommand());
		if(ModConfig.DEBUG.sanity) manager.registerCommand(new AddTendencyCommand());
		if(ModConfig.DEBUG.sanity) manager.registerCommand(new SetSanityCommand());
		if(ModConfig.DEBUG.sanity) manager.registerCommand(new MonumentRaytraceDebugCommands());
		if(ModConfig.DEBUG.traits) manager.registerCommand(new DebugTraitsCommand());
		
		manager.registerCommand(new LightDebugCommand());
		manager.registerCommand(new HeatDebugCommand());

		if(ModConfig.DRUGS.enabled) manager.registerCommand(new DrugsCommand());
		manager.registerCommand(new WorshipCommand());

		manager.registerCommand(new DaysCommand());

		
		manager.registerCommand(new StatCommand());
		
		
	}
	
	public void AddReasonToClient(float value, String reason, boolean forceAdd)
	{
	}
	
	public void SanityOverviewOnClient(EntityPlayer player)
	{
	}
	
}