package enginecrafter77.survivalinc;

import java.util.function.Function;
import java.util.function.Supplier;

import enginecrafter77.survivalinc.block.*;
import enginecrafter77.survivalinc.strugglecraft.*;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
public enum ModBlocks implements Supplier<Block> {
	MELTING_SNOW(new BlockMeltingSnow()),
	MELTING_ICE(new BlockMeltingIce()),
	HEATER(new BlockHeater(false)),
	LIT_HEATER(new BlockHeater(true)),
	WEED_CROP(new BlockDrugsCrop("weed_crop")/*, postInit(ModItems.WEED.getItem(), ModItems.WEED_SEEDS.getItem())*/);
	
	private final Block instance;
	private final Function postInitFunction;
	
	private ModBlocks(Block instance) 
	{
		 this(instance, null);
	}
	
	
	private ModBlocks(Block instance, Function func)
	{
		this.instance = instance;
		postInitFunction= func;
	}
	
	@Override
	public Block get()
	{
		return this.instance;
	}
	
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		IForgeRegistry<Block> reg = event.getRegistry();
		for(ModBlocks block : ModBlocks.values())
			reg.register(block.instance);
		
		GameRegistry.registerTileEntity(TileEntityHeater.class, new ResourceLocation(SurvivalInc.MOD_ID, "heater"));

	}
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		IForgeRegistry<Item> reg = event.getRegistry();
		for(ModBlocks block : ModBlocks.values())
		{
			Item blockitem = new ItemBlock(block.instance);
			blockitem.setRegistryName(block.instance.getRegistryName());
			reg.register(blockitem);
		}
	}
	
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event)
	{
		for(ModBlocks block : ModBlocks.values())
		{
			Item blockitem = Item.getItemFromBlock(block.instance);
			ModelLoader.setCustomModelResourceLocation(blockitem, 0, new ModelResourceLocation(blockitem.getRegistryName(), "inventory"));
		}
	}

}
