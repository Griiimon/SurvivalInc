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
	COOLER(new BlockCooler(false)),
	ACTIVE_COOLER(new BlockCooler(true)),
	WEED_CROP(new BlockDrugsCrop("weed_crop")),
	TOBACCO_CROP(new BlockDrugsCrop("tobacco_crop")),
	COCA_CROP(new BlockDrugsCrop("coca_crop")),
	POPPY_CROP(new BlockDrugsCrop("poppy_crop")),
	MONOLITH(new BlockMonolith()),
	MONUMENT_TOP(new BlockMonumentTop()),
	MONUMENT_STRUCTURE(new BlockMonumentStructure())
	
	;
	
	private final Block instance;
	
	private ModBlocks(Block instance)
	{
		this.instance = instance;
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
		
		// TODO move to block.init
		GameRegistry.registerTileEntity(TileEntityHeater.class, new ResourceLocation(SurvivalInc.MOD_ID, "heater"));
		GameRegistry.registerTileEntity(TileEntityCooler.class, new ResourceLocation(SurvivalInc.MOD_ID, "cooler"));
		GameRegistry.registerTileEntity(TileEntityMonolith.class, new ResourceLocation(SurvivalInc.MOD_ID, "monolith"));

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
