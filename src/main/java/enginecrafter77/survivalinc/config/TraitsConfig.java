package enginecrafter77.survivalinc.config;

import net.minecraftforge.common.config.Config;

@Config.LangKey("config.survivalinc:traits")
public class TraitsConfig {
		@Config.LangKey("config.survivalinc:traits.enable")
		@Config.RequiresMcRestart
		public boolean enabled = true;
}
