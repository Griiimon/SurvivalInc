package enginecrafter77.survivalinc.strugglecraft;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import enginecrafter77.survivalinc.SurvivalInc;
import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.stats.FoodRecord;
import enginecrafter77.survivalinc.stats.ListIntRecord;
import enginecrafter77.survivalinc.stats.StatCapability;
import enginecrafter77.survivalinc.stats.StatProvider;
import enginecrafter77.survivalinc.stats.StatRecord;
import enginecrafter77.survivalinc.stats.StatRegisterEvent;
import enginecrafter77.survivalinc.stats.StatTracker;
import enginecrafter77.survivalinc.stats.impl.SanityTendencyModifier;
import enginecrafter77.survivalinc.strugglecraft.TraitModule.TRAITS;
import enginecrafter77.survivalinc.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import scala.collection.mutable.HashTable;


public class FoodModule implements StatProvider<FoodRecord> {
	private static final long serialVersionUID = 6277772840199029918L;


	public static final Map<Item, Float> foodSanityMap= new HashMap<Item, Float>();

	
	public static FoodModule instance= new FoodModule();
	

	public void init()
	{
		MinecraftForge.EVENT_BUS.register(FoodModule.class);
		
		// Compile food value list
		for(String entry : ModConfig.FOOD.foodSanityMap)
		{
			int separator = entry.lastIndexOf(' ');
			Item target = Item.getByNameOrId(entry.substring(0, separator));
			Float value = Float.parseFloat(entry.substring(separator + 1));
			foodSanityMap.put(target, value);
		}
	}
	
	
	
	@SubscribeEvent
	public static void consumeFood(LivingEntityUseItemEvent.Finish event)
	{
		EntityPlayer player= (EntityPlayer)event.getEntity();

		if(player.world.isRemote) return;
		
		if(event.getItem().getItem() instanceof ItemFood)
		{
			Hashtable<Integer, Integer> foodTable;
			
			StatTracker tracker = player.getCapability(StatCapability.target, null);
			FoodRecord record= tracker.getRecord(FoodModule.instance);

			foodTable= record.getFoodTable();
			
			
//			player.sendMessage(new TextComponentString("This is food"));

			ItemFood food= (ItemFood)event.getItem().getItem();
			int id= Item.getIdFromItem(food);
			
			
			// decrease annoyance level of random known food
			if(Util.chance((float)ModConfig.FOOD.decreaseLevelChance) && !foodTable.isEmpty())
			{
				Object[] arr= foodTable.keySet().toArray();
				int randomKey= (int)arr[Util.rnd(foodTable.size())];
				
				int value= foodTable.get(randomKey);
				
				if(value > 0 && value != id)
				{
					foodTable.put(randomKey, value-1);
				
					int offset= 0;
					if(TraitModule.instance.HasTrait(player,TRAITS.NONDISCRIMINATORY))
						offset+= 2;
					if(TraitModule.instance.HasTrait(player,TRAITS.GOURMET))
						offset-=2;
					
					if(value == ModConfig.FOOD.enoughThreshold - 1 + offset)
						player.sendMessage(new TextComponentString(new ItemStack(Item.getItemById(randomKey)).getDisplayName()+ " is more acceptable"));
				}
			}
			
			
			if(!foodTable.containsKey(id))
			{
				// tasted this food item for the first time
				int annoyedOffset= 0;
				
				// chance to become new favorite food
				if(Util.chance((float)ModConfig.FOOD.favFoodChance))
				{
					record.setFavoriteFoodId(id);
					player.sendMessage(new TextComponentString("I love "+event.getItem().getDisplayName()));
				}
				else
					annoyedOffset= Util.rnd(3);
				
				foodTable.put(id, annoyedOffset);
				
				// increase sanity boost with each new food tasted, but not linear
				SanityTendencyModifier.instance.addToTendencyServer((float)Math.sqrt(foodTable.values().size())*5f, "New food", player);
			}
			else
			if(id == record.getFavoriteFoodId())
			{
				if(!TraitModule.instance.HasTrait(player,TRAITS.TASTELESS))
					SanityTendencyModifier.instance.addToTendencyServer((float)ModConfig.FOOD.favFoodSanity, "Favourite food", player);
			}
			else
			if(foodSanityMap.containsKey(food))
			{
				if(!TraitModule.instance.HasTrait(player,TRAITS.TASTELESS))
					SanityTendencyModifier.instance.addToTendencyServer(foodSanityMap.get(food), "food", player);
			}
			else	// annoyance level only for foods not listed in sanity map / current favorite food
			if(Util.chance((float)ModConfig.FOOD.increaseLevelChance))
			{
				
				int level= foodTable.get(id)+1;
				foodTable.put(id, level);
				
				boolean isNondiscriminatory= TraitModule.instance.HasTrait(player,TRAITS.NONDISCRIMINATORY);
				int offset= 0;
				if(isNondiscriminatory)
					offset+= 2 + TraitModule.instance.TraitTier(player, TRAITS.NONDISCRIMINATORY);
				if(TraitModule.instance.HasTrait(player,TRAITS.GOURMET))
					offset-=2;
				
				if(level >= ModConfig.FOOD.enoughThreshold + offset)
				{
					SanityTendencyModifier.instance.addToTendencyOneTime(-1, "Same food", (EntityPlayer)event.getEntity());
					player.sendMessage(new TextComponentString("Please no more "+event.getItem().getDisplayName()+"!"));
					if(isNondiscriminatory)
						TraitModule.instance.UsingTrait(player,TRAITS.NONDISCRIMINATORY, 2f);
				}
				else
				if(level >= ModConfig.FOOD.annoyedThreshold + offset)
				{
					player.sendMessage(new TextComponentString("Again "+event.getItem().getDisplayName()+"?"));
					if(isNondiscriminatory)
						TraitModule.instance.UsingTrait(player,TRAITS.NONDISCRIMINATORY);
				}
			}
		}
//		else
//			player.sendMessage(new TextComponentString("This isn't food"));
	}
	
	
	public int getFavoriteFoodId(EntityPlayer player)
	{
		StatTracker tracker = player.getCapability(StatCapability.target, null);
		FoodRecord record= tracker.getRecord(FoodModule.instance);

		return record.getFavoriteFoodId();
	}
	
	public Hashtable<Integer, Integer> getFoodTable(EntityPlayer player)
	{
		StatTracker tracker = player.getCapability(StatCapability.target, null);
		FoodRecord record= tracker.getRecord(FoodModule.instance);

		return record.getFoodTable();
	}
	
/*	
	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		int arr[]= nbt.getIntArray("table");
		for(int i= 0; i < arr.length; i+= 2)
			foodTable.put(arr[i], arr[i+1]);
		favoriteFoodId= nbt.getInteger("favfood");
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		
		int arr[]= new int[foodTable.size()*2];
		
		int i= 0;
		for(int key : foodTable.keySet())
		{
			arr[i++]= key;
			arr[i++]= foodTable.get(key);
		}	
		
		tag.setIntArray("table", arr);
		tag.setInteger("favfood", favoriteFoodId);
		return tag;
	}
*/


	@Override
	public void update(EntityPlayer target, StatRecord record) {
		
	}



	@Override
	public ResourceLocation getStatID() {
		return new ResourceLocation(SurvivalInc.MOD_ID, "food");
	}

	@Override
	public FoodRecord createNewRecord() {
		FoodRecord record= new FoodRecord();
		return record;
	}

	@Override
	public Class<FoodRecord> getRecordClass() {
		return FoodRecord.class;
	}

	@SubscribeEvent
	public static void registerStat(StatRegisterEvent event)
	{
		event.register(FoodModule.instance);
	}

}
