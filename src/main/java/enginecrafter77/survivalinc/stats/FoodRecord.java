package enginecrafter77.survivalinc.stats;

import java.util.ArrayList;
import java.util.Hashtable;

import net.minecraft.nbt.NBTTagCompound;

public class FoodRecord implements StatRecord{
	
	Hashtable<Integer, Integer> foodTable= new Hashtable<Integer, Integer>();
	int favoriteFoodId= -1;
	
	
	public Hashtable<Integer, Integer> getFoodTable()
	{
		return foodTable;
	}
	
	public int getFavoriteFoodId()
	{
		return favoriteFoodId;
	}

	public void setFavoriteFoodId(int id)
	{
		favoriteFoodId= id;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		foodTable.clear();
		int[] arr= nbt.getIntArray("table");
		for(int i= 0; i < arr.length; i+=2)
			foodTable.put(arr[i], arr[i+1]);
		favoriteFoodId= nbt.getInteger("favfood");
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();

		ArrayList<Integer> list= new ArrayList<Integer>();
		
		foodTable.forEach((k,v) -> {
			list.add(k);
			list.add(v);
		});
		
		int[] arr= new int[list.size()];
		
		for(int i= 0; i < arr.length; i++)
			arr[i]= list.get(i);
		
 		tag.setIntArray("table", arr);
 		tag.setInteger("favfood", favoriteFoodId);
 		
 		return tag;	
	}

	
	
}
