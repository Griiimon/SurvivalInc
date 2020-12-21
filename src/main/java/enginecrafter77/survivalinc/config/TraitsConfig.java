package enginecrafter77.survivalinc.config;

import net.minecraftforge.common.config.Config;

@Config.LangKey("config.survivalinc:traits")
public class TraitsConfig {
		@Config.LangKey("config.survivalinc:traits.enable")
		@Config.RequiresMcRestart
		public boolean enabled = true;
		
		@Config.LangKey("config.survivalinc:traits.baseIncreaseDifficulty")
		@Config.RangeDouble(min = 1)
		public double baseIncreaseDifficulty= 20;
}
