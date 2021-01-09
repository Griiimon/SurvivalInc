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
		public double favFoodSanity= 2;

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
		public double sameFoodSanity= -2;

		@Config.LangKey("config.survivalinc:food.foodSanityMap")
		@Config.RequiresMcRestart
		public String[] foodSanityMap = {
		        "minecraft:rotten_flesh -10",
		        "minecraft:spider_eye -10",
		        "minecraft:poisonous_potato -10",
		        "minecraft:pumpkin_pie 5",
		        "minecraft:cake 10",
		        "minecraft:cookie 5",
		        "minecraft:melon 3",
		        "minecraft:apple 2",
		        "minecraft:golden_apple 10",
		        "foodexpansion:itemchocolatebar 10",
		        "foodexpansion:itemspidersoup -5",
		        "foodexpansion:itemnetherwartsoup -5",
		        "foodexpansion:itemcarrotpie 5",
		        "foodexpansion:itemmelonsalad 3",
		        "foodexpansion:itemlollipop 8"
		};

}
