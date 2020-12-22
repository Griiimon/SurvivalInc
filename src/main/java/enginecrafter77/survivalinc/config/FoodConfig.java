package enginecrafter77.survivalinc.config;

import net.minecraftforge.common.config.Config;

@Config.LangKey("config.survivalinc:food")
public class FoodConfig {
		@Config.LangKey("config.survivalinc:food.enable")
		@Config.RequiresMcRestart
		public boolean enabled = true;
		
		@Config.LangKey("config.survivalinc:food.favFoodResetChance")
		@Config.RangeDouble(min = 0)
		public double favFoodChance= 10;

		@Config.LangKey("config.survivalinc:food.favFoodSanity")
		@Config.RangeDouble(min = 0)
		public double favFoodSanity= 0.5;

		@Config.LangKey("config.survivalinc:food.annoyedThreshold")
		@Config.RangeInt(min = 0)
		public int annoyedThreshold= 4;
		
		@Config.LangKey("config.survivalinc:food.enoughThreshold")
		@Config.RangeInt(min = 0)
		public int enoughThreshold= 5;

		@Config.LangKey("config.survivalinc:food.increaseLevelChance")
		@Config.RangeDouble(min = 0)
		public double increaseLevelChance= 20;

		@Config.LangKey("config.survivalinc:food.decreaseLevelChance")
		@Config.RangeDouble(min = 0)
		public double decreaseLevelChance= 20;

		@Config.LangKey("config.survivalinc:food.sameFoodSanity")
		@Config.RangeDouble(max = 0)
		public double sameFoodSanity= -1;

		@Config.LangKey("config.survivalinc:food.foodSanityMap")
		@Config.RequiresMcRestart
		public String[] foodSanityMap = {
		        "minecraft:rotten_flesh -5",
		        "minecraft:spider_eye -5",
		        "minecraft:poisonous_potato -5",
		        "minecraft:pumpkin_pie 4",
		        "minecraft:cake 5",
		        "minecraft:cookie 2.5",
		        "minecraft:melon 1.5",
		        "minecraft:apple 1",
		        "minecraft:golden_apple 5",
		};

}
