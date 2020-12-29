package enginecrafter77.survivalinc.config;

import net.minecraftforge.common.config.Config;

@Config.LangKey("config.survivalinc:drugs")
public class DrugConfig {
	
	@Config.LangKey("config.survivalinc:drugs.enable")
	@Config.RequiresMcRestart
	public boolean enabled = true;
}
