package enginecrafter77.survivalinc.config;

import net.minecraftforge.common.config.Config;

@Config.LangKey("config.survivalinc:debug")
public class DebugConfig {
	
	@Config.LangKey("config.survivalinc:debug.traits")
	@Config.RequiresMcRestart
	public boolean traits= true;
	
}
