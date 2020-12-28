package enginecrafter77.survivalinc.stats;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

public class ListIntRecord implements StatRecord{

	ArrayList<Integer> list= new ArrayList<Integer>();
	
	
	
	
	public void Add(int i)
	{
		list.add(i);
	}
	
	public void Clear()
	{
		list.clear();
	}
	
	public int get(int index)
	{
		return list.get(index);
	}
	
	public void set(int index, int value)
	{
		list.set(index, value);
	}
	
	public ArrayList<Integer> getList()
	{
		return list;
	}
	
	public int getListSize()
	{
		return list.size();
	}
	
	
	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		list.clear();
		int[] arr= nbt.getIntArray("list");
		for(int i= 0; i < arr.length; i++)
			list.add(arr[i]);
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();
 		int[] arr= new int[list.size()];
		for(int i= 0; i < list.size(); i++)
			arr[i]= (int)list.get(i);		
		tag.setIntArray("list", arr);
		return tag;	
	}

	
	
}
