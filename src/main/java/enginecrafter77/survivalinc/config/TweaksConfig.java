package enginecrafter77.survivalinc.config;

import net.minecraftforge.common.config.Config;

@Config.LangKey("config.survivalinc:tweaks")
public class TweaksConfig {
		@Config.LangKey("config.survivalinc:tweaks.enable")
		@Config.RequiresMcRestart
		public boolean enabled = true;
		
		@Config.LangKey("config.survivalinc:tweaks.digspeedFactor")
		@Config.RangeDouble(min = 0)
		public double digspeedFactor= 0.1;
		
		@Config.LangKey("config.survivalinc:tweaks.addJumpExhaustion")
		@Config.RangeDouble(min = 0)
		public double jumpExhaustion= 0.1;

		@Config.LangKey("config.survivalinc:tweaks.addRunExhaustion")
		@Config.RangeDouble(min = 0)
		public double runExhaustion= 0.02;

		@Config.LangKey("config.survivalinc:tweaks.addHandExhaustion")
		@Config.RangeDouble(min = 0)
		public double handExhaustion= 0.1;
		
		@Config.LangKey("config.survivalinc:tweaks.swimExhaustion")
		@Config.RangeDouble(min = 0)
		public double swimExhaustion= 0.05;

		@Config.LangKey("config.survivalinc:tweaks.sleepExhaustion")
		@Config.RangeDouble(min = 0)
		public double sleepExhaustion= 0.001;
		
		@Config.LangKey("config.survivalinc:tweaks.rowingExhaustion")
		@Config.RangeDouble(min = 0)
		public double rowingExhaustion= 0.01;
		
		@Config.LangKey("config.survivalinc:tweaks.animalHealthMultiplier")
		@Config.RangeDouble(min = 0)
		public double animalHealthMultiplier= 6;
		
		@Config.LangKey("config.survivalinc:tweaks.enable")
		@Config.RequiresMcRestart
		public boolean enhancedFishing= true;

}
