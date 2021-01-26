package enginecrafter77.survivalinc;

import enginecrafter77.survivalinc.item.*;
import enginecrafter77.survivalinc.strugglecraft.*;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;


@Mod.EventBusSubscriber
public enum ModItems {
	
	CANTEEN(new ItemCanteen(), "canteen_drain", "canteen_refill"),
	FEATHER_FAN(new ItemFeatherFan(), "feather_fan"),
	TOWEL(new ItemTowel(), "towel_dry", "towel_wet"),
	WOOL_HAT(new WoolArmor("wool_helmet", EntityEquipmentSlot.HEAD), "wool_helmet"),
	WOOL_SWEATER(new WoolArmor("wool_chestplate", EntityEquipmentSlot.CHEST), "wool_chestplate"),
	WOOL_PANTS(new WoolArmor("wool_leggings", EntityEquipmentSlot.LEGS), "wool_leggings"),
	WOOL_SOCKS(new WoolArmor("wool_boots", EntityEquipmentSlot.FEET), "wool_boots"),
	WINTER_HAT(new WinterArmor("winter_helmet", EntityEquipmentSlot.HEAD), "winter_helmet"),
	WINTER_SWEATER(new WinterArmor("winter_chestplate", EntityEquipmentSlot.CHEST), "winter_chestplate"),
	WINTER_PANTS(new WinterArmor("winter_leggings", EntityEquipmentSlot.LEGS), "winter_leggings"),
	WINTER_SOCKS(new WinterArmor("winter_boots", EntityEquipmentSlot.FEET), "winter_boots"),
	WEED(new Item().setRegistryName(new ResourceLocation(SurvivalInc.MOD_ID, "weed")).setCreativeTab(SurvivalInc.mainTab), "weed"),
	WEED_SEEDS(new ItemDrugsSeed("weed_seeds", ModBlocks.WEED_CROP, WEED), "weed_seeds"),
	JOINT(new ItemDrug(Drug.JOINT), "joint"),
	TOBACCO(new Item().setRegistryName(new ResourceLocation(SurvivalInc.MOD_ID, "tobacco")).setCreativeTab(SurvivalInc.mainTab), "tobacco"),
	TOBACCO_SEEDS(new ItemDrugsSeed("tobacco_seeds", ModBlocks.TOBACCO_CROP, TOBACCO), "tobacco_seeds"),
	CIGARETTE(new ItemDrug(Drug.CIGARETTE), "cigarette"),
	COCA_LEAVES(new ItemDrug(Drug.COCA_LEAVES), "coca_leaves"),
	COCA_SEEDS(new ItemDrugsSeed("coca_seeds", ModBlocks.COCA_CROP, COCA_LEAVES), "coca_seeds"),
	OPIUM(new Item().setRegistryName(new ResourceLocation(SurvivalInc.MOD_ID, "opium")).setCreativeTab(SurvivalInc.mainTab), "opium"),
	POPPY_SEEDS(new ItemDrugsSeed("poppy_seeds", ModBlocks.POPPY_CROP, OPIUM), "poppy_seeds"),
	PIPE(new ItemDrug(Drug.OPIUM_PIPE), "pipe"),
	UMBRELLA(new Item().setRegistryName(new ResourceLocation(SurvivalInc.MOD_ID, "umbrella")).setCreativeTab(SurvivalInc.mainTab), "umbrella"),
	STRAW_HAT(new StrawArmor("straw_helmet", EntityEquipmentSlot.HEAD), "straw_helmet"),
	HEADBAND(new HeadbandArmor("headband", EntityEquipmentSlot.HEAD, false), "headband"),
	SUPERIOR_HEADBAND(new HeadbandArmor("superior_headband", EntityEquipmentSlot.HEAD, true), "superior_headband"),
	RAIN_CAPE(new RainArmor("raincape", EntityEquipmentSlot.CHEST), "raincape"),
	WINTER_ITEM(new Item().setRegistryName(new ResourceLocation(SurvivalInc.MOD_ID, "winter_item")).setCreativeTab(SurvivalInc.mainTab), "winter_item"),
	ARMORED_BOAT(new ItemArmoredBoat(), "armored_boat"),
	;

	
	public final Item target;
	public final String[] models;
	public final int[] mappings;
	
	private ModItems(Item instance, int[] mappings, String... models)
	{
		this.target = instance;
		this.models = models;
		this.mappings = mappings;
	}
	
	private ModItems(Item instance, String... models)
	{
		this(instance, new int[0], models);
	}
	
	public Item getItem()
	{
		return this.target;
	}
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		IForgeRegistry<Item> reg = event.getRegistry();
		for(ModItems mi : ModItems.values())
			reg.register(mi.getItem());
	}
	
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event)
	{
		for(ModItems entry : ModItems.values())
		{
			Item item = entry.getItem();
			int index = 0, meta;
			
			for(String model : entry.models)
			{
				meta = index;
				if(entry.mappings.length > index)
					meta = entry.mappings[index];
				ResourceLocation loc = new ResourceLocation(SurvivalInc.MOD_ID, model);
				ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(loc, "inventory"));
				SurvivalInc.logger.info("Registering model {} on meta {} for item {}", loc.toString(), meta, item.getRegistryName().toString());
				index++;
			}
		}
	}
}