package enginecrafter77.survivalinc.strugglecraft;

import enginecrafter77.survivalinc.SurvivalInc;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.EnumHelper;

public class WinterArmor extends ItemArmor{

	public WinterArmor(String internalName, EntityEquipmentSlot slot) {
		super(winterMaterial, slot == EntityEquipmentSlot.LEGS ? 2 : 1, slot);

		setRegistryName(new ResourceLocation(SurvivalInc.MOD_ID, internalName));
		this.setTranslationKey(internalName);
		this.setCreativeTab(SurvivalInc.mainTab);

	}
	
	
	public static final ArmorMaterial winterMaterial= EnumHelper.addArmorMaterial("armor_winter", SurvivalInc.MOD_ID + ":winter", 8, new int[] {0,1,2,0}, 10, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0f);

}
