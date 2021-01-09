package enginecrafter77.survivalinc.strugglecraft;

import enginecrafter77.survivalinc.SurvivalInc;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.EnumHelper;

public class RainArmor extends ItemArmor{

	public RainArmor(String internalName, EntityEquipmentSlot slot) {
		super(rainMaterial, slot == EntityEquipmentSlot.LEGS ? 2 : 1, slot);

		setRegistryName(new ResourceLocation(SurvivalInc.MOD_ID, internalName));
		this.setTranslationKey(internalName);
		this.setCreativeTab(SurvivalInc.mainTab);

	}
	
	
	public static final ArmorMaterial rainMaterial= EnumHelper.addArmorMaterial("armor_rain", SurvivalInc.MOD_ID + ":rain", 8, new int[] {0,1,2,0}, 10, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0f);

}