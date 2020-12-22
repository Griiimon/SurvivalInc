package enginecrafter77.survivalinc.config;

import enginecrafter77.survivalinc.SurvivalInc;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = SurvivalInc.MOD_ID, name = SurvivalInc.MOD_ID, category = "")
public class ModConfig {

	// The values in the config file. Add on more as needed.
	// Sub-categories
	@Config.Name("general")
	public static final GeneralConfig GENERAL = new GeneralConfig();
	
	@Config.Name("client")
	public static final ClientConfig CLIENT = new ClientConfig();
	
	@Config.Name("heat")
	public static final HeatConfig HEAT = new HeatConfig();
	
	@Config.Name("hydration")
	public static final HydrationConfig HYDRATION = new HydrationConfig();
	
	@Config.Name("sanity")
	public static final SanityConfig SANITY = new SanityConfig();
	
	@Config.Name("wetness")
	public static final WetnessConfig WETNESS = new WetnessConfig();

	@Config.Name("ghost")
	public static final GhostConfig GHOST = new GhostConfig();
	
	@Config.Name("seasons")
	public static final SeasonConfig SEASONS = new SeasonConfig();

	@Config.Name("traits")
	public static final TraitsConfig TRAITS = new TraitsConfig();

	@Config.Name("food")
	public static final FoodConfig FOOD = new FoodConfig();

	@Config.Name("tweaks")
	public static final TweaksConfig TWEAKS = new TweaksConfig();

	// This deals with changed the config values in Forge's GUI in-game.
	// It also deals with syncing some config values from the server to the
	// client, so everything doesn't get messed up.
	@Mod.EventBusSubscriber
	private static class ConfigEvents {
		@SubscribeEvent
		public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event)
		{
			// Make sure it's the right mod.
			if (event.getModID().equals(SurvivalInc.MOD_ID))
			{
				ConfigManager.sync(SurvivalInc.MOD_ID, Config.Type.INSTANCE);
			}
		}
	}
}