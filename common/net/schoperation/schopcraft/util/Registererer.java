package net.schoperation.schopcraft.util;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.schoperation.schopcraft.lib.ModBlocks;
import net.schoperation.schopcraft.lib.ModItems;
import net.schoperation.schopcraft.lib.ModSounds;

@Mod.EventBusSubscriber
public class Registererer {
	
	/*
	 * This is where all new crap added to the mod is registered (items, blocks, etc.)
	 * And wow, it's much easier. yeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee.
	 */
	
	// register all blocks
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		
		event.getRegistry().registerAll(ModBlocks.BLOCKS);
	}
	
	// register all items
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		
		// register normal items
		event.getRegistry().registerAll(ModItems.ITEMS);
		
		// register itemblocks (items to represent the blocks, dur)
		for (Block block : ModBlocks.BLOCKS) {
			
			event.getRegistry().register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
		}
	}
	
	// register all sounds
	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
		
		event.getRegistry().registerAll(ModSounds.SOUNDS);
	}
	
	// render models
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void registerModels(ModelRegistryEvent event) {
		
		// render item models
		for (Item item : ModItems.ITEMS) {
			
			ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
		}
		
		// render block models
		for (Block block : ModBlocks.BLOCKS) {
			
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
		}
	}
}