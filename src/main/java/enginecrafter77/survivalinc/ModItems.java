package enginecrafter77.survivalinc;

import java.util.function.Supplier;

import enginecrafter77.survivalinc.item.*;
import net.minecraft.item.Item;

public enum ModItems implements Supplier<Item> {
	
	CANTEEN(new ItemCanteen()),
	CHARCOAL_FILTER(new ItemCharcoalFilter()),
	FEATHER_FAN(new ItemFeatherFan()),
	HYDROPOUCH(new ItemHydroPouch()),
	ICE_CREAM(new ItemIceCream(4, 0.4f, false)),
	LUCID_DREAM_ESSENCE(new ItemLucidDreamEssence()),
	RESETTER(new ItemResetter()),
	TOWEL(new ItemTowel());
	
	public final Item target;

	private ModItems(Item instance)
	{
		this.target = instance;
	}
	
	@Override
	public Item get()
	{
		return this.target;
	}
}