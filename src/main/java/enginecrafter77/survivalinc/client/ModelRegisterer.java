package enginecrafter77.survivalinc.client;

import enginecrafter77.survivalinc.ModBlocks;
import enginecrafter77.survivalinc.ModItems;
import enginecrafter77.survivalinc.SurvivalInc;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ModelRegisterer {

	/*
	 * Where models are registered. Client-only, as the server has no eyes...
	 * yet...
	 * 
	 */

	// Register models.
	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event)
	{

		// Register item models.
		for (ModItems mi : ModItems.values())
		{
			Item item = mi.get();
			// If there are subitems (items with metadata), create a list of
			// them and register those models separately.
			if (item.getHasSubtypes())
			{

				NonNullList<ItemStack> subItems = NonNullList.create();
				item.getSubItems(SurvivalInc.mainTab, subItems);

				for (ItemStack stack : subItems)
				{

					// The main item will have the normal model.
					if (item.getMetadata(stack) == 0)
					{

						ModelLoader.setCustomModelResourceLocation(item, stack.getMetadata(),
								new ModelResourceLocation(item.getRegistryName(), "inventory"));
					}

					// Otherwise give it its own model, where the model json is
					// called registryname_metadata.json
					else
					{

						ModelLoader.setCustomModelResourceLocation(item, stack.getMetadata(), new ModelResourceLocation(
								item.getRegistryName() + "_" + Integer.toString(stack.getMetadata()), "inventory"));
					}
				}
			}

			else
			{

				ModelLoader.setCustomModelResourceLocation(item, 0,
						new ModelResourceLocation(item.getRegistryName(), "inventory"));
			}
		}

		// Register block models.
		for(ModBlocks mb : ModBlocks.values())
		{
			Block block = mb.get();
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
		}
	}
}