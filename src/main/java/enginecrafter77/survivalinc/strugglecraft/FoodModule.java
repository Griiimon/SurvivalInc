package enginecrafter77.survivalinc.strugglecraft;

import java.util.HashMap;

import java.util.Hashtable;
import java.util.Map;

import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.stats.impl.SanityTendencyModifier;
import enginecrafter77.survivalinc.util.Util;
import ibxm.Player;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentKeybind;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class FoodModule implements INBTSerializable<NBTTagCompound>{

	Hashtable<Integer, Integer> foodTable= new Hashtable<Integer, Integer>();
	
	public final Map<Item, Float> foodSanityMap= new HashMap<Item, Float>();

	
	int favoriteFoodId= -1;
	
	public static FoodModule instance= new FoodModule();
	

	public void init()
	{
		MinecraftForge.EVENT_BUS.register(FoodModule.class);
		
		// Compile food value list
		for(String entry : ModConfig.SANITY.foodSanityMap)
		{
			int separator = entry.lastIndexOf(' ');
			Item target = Item.getByNameOrId(entry.substring(0, separator));
			Float value = Float.parseFloat(entry.substring(separator + 1));
			this.foodSanityMap.put(target, value);
		}
	}
	
	
	
	@SubscribeEvent
	public static void consumeFood(LivingEntityUseItemEvent.Finish event)
	{
		EntityPlayer player= (EntityPlayer)event.getEntity();

		if(player.world.isRemote) return;
		
		if(event.getItem().getItem() instanceof ItemFood)
		{
			player.sendMessage(new TextComponentString("This is food"));

			ItemFood food= (ItemFood)event.getItem().getItem();
			int id= Item.getIdFromItem(food);
			
			
			// decrease annoyance level of random known food
			if(Util.chance((float)ModConfig.FOOD.decreaseLevelChance) && !instance.foodTable.isEmpty())
			{
				Object[] arr= instance.foodTable.keySet().toArray();
				int randomKey= (int)arr[Util.rnd(instance.foodTable.size())];
				
				int value= instance.foodTable.get(randomKey);
				
				if(value > 0)
				{
					instance.foodTable.put(randomKey, value-1);
					if(value == ModConfig.FOOD.enoughThreshold - 1)
						player.sendMessage(new TextComponentString(new ItemStack(Item.getItemById(instance.favoriteFoodId)).getDisplayName()+ " is more acceptable"));
				}
			}
			
			
			if(!instance.foodTable.containsKey(id))
			{
				// tasted this food item for the first time
				int annoyedOffset= 0;
				
				// chance to become new favorite food
				if(Util.chance((float)ModConfig.FOOD.favFoodChance))
				{
					instance.favoriteFoodId= id;
					player.sendMessage(new TextComponentString("I love "+event.getItem().getDisplayName()));
				}
				else
					annoyedOffset= Util.rnd(3);
				
				instance.foodTable.put(id, annoyedOffset);
				
				// increase sanity boost with each new food tasted, but not linear
				SanityTendencyModifier.instance.addToTendency((float)Math.sqrt(instance.foodTable.values().size()), "New food", player, true);
			}
			else
			if(id == instance.favoriteFoodId)
			{
				SanityTendencyModifier.instance.addToTendency((float)ModConfig.FOOD.favFoodSanity, "Favourite food", player, true);
			}
			else
			if(instance.foodSanityMap.containsKey(food))
			{
				SanityTendencyModifier.instance.addToTendency(instance.foodSanityMap.get(food), "food", player);
			}
			else	// annoyance level only for foods not listed in sanity map / current favorite food
			if(Util.chance((float)ModConfig.FOOD.increaseLevelChance))
			{
				
				int level= instance.foodTable.get(id)+1;
				instance.foodTable.put(id, level);
				
				if(level >= ModConfig.FOOD.enoughThreshold)
				{
					SanityTendencyModifier.instance.addToTendency(-1, "Same food", (EntityPlayer)event.getEntity(), true);
					player.sendMessage(new TextComponentString("Please no more "+event.getItem().getDisplayName()+"!"));
				}
				else
				if(level >= ModConfig.FOOD.annoyedThreshold)
				{
					player.sendMessage(new TextComponentString("Again "+event.getItem().getDisplayName()+"?"));
				}
			}
		}
		else
			player.sendMessage(new TextComponentString("This isn't food"));
	}
	
	
	
	
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
}
