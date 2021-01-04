package enginecrafter77.survivalinc.stats;

import enginecrafter77.survivalinc.SurvivalInc;
import enginecrafter77.survivalinc.strugglecraft.Drug;
import enginecrafter77.survivalinc.strugglecraft.TraitModule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DrugRecord implements StatRecord{

	public int[] high, dependencyLevel;
	public long[] lastUse;

/*	
	long c = (long)a << 32 | b & 0xFFFFFFFFL;
	int aBack = (int)(c >> 32);
	int bBack = (int)c;
*/
	
	public DrugRecord()
	{
		int numDrugs= Drug.drugList.size();

		System.out.println("DEBUG: creating DrugRecord with size "+numDrugs);
		
		high= new int[numDrugs];
		dependencyLevel= new int[numDrugs];
		lastUse= new long[numDrugs];
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound tag) {
		high= tag.getIntArray("high");
		dependencyLevel= tag.getIntArray("dependency");
		
		int[] parseLong= tag.getIntArray("lastuse");
		lastUse= new long[parseLong.length/2];
		for(int i= 0; i < lastUse.length; i++)
		{
			lastUse[i]= (long)parseLong[i*2] << 32 | parseLong[i*2+1] & 0xFFFFFFFFL;
		}
		
		
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag= new NBTTagCompound();
		tag.setIntArray("high", high);
		tag.setIntArray("dependency", dependencyLevel);
		
		int[] parseLong= new int[lastUse.length * 2];
		for(int i= 0; i < parseLong.length; i+= 2)
		{
			parseLong[i]= (int)lastUse[i / 2] >> 32;
			parseLong[i]= (int)lastUse[i/2];
		}
		
		tag.setIntArray("lastuse", parseLong);
		return tag;
	}

	
	
}
