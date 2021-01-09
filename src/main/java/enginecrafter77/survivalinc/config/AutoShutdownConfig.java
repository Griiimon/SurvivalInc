package enginecrafter77.survivalinc.config;

import net.minecraftforge.common.config.Config;

@Config.LangKey("config.survivalinc:auto_shutdown")
public class AutoShutdownConfig {
	
	@Config.LangKey("config.survivalinc:auto_shutdown.enable")
	@Config.RequiresMcRestart
	public boolean enabled= true;

	@Config.LangKey("config.survivalinc:auto_shutdown.ticks")
	@Config.RangeInt(min = 1)
	public int ticks = 300;
}
