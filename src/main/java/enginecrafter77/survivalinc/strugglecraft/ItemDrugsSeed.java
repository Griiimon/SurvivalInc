package enginecrafter77.survivalinc.strugglecraft;

import enginecrafter77.survivalinc.ModBlocks;
import enginecrafter77.survivalinc.ModItems;
import enginecrafter77.survivalinc.SurvivalInc;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSeeds;
import net.minecraft.util.ResourceLocation;

public class ItemDrugsSeed extends ItemSeeds {

	public ItemDrugsSeed(String name, ModBlocks crops, ModItems result) {
		super(crops.get(), Blocks.FARMLAND);
		
		this.setRegistryName(new ResourceLocation(SurvivalInc.MOD_ID, name));
		this.setTranslationKey(name);

		this.setCreativeTab(SurvivalInc.mainTab);

		
		((BlockDrugsCrop)crops.get()).postInit(this, result.getItem());
	}

	
	
}
