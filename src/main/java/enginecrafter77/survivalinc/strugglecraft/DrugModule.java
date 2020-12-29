package enginecrafter77.survivalinc.strugglecraft;


import enginecrafter77.survivalinc.SurvivalInc;
import enginecrafter77.survivalinc.stats.DrugRecord;
import enginecrafter77.survivalinc.stats.FoodRecord;
import enginecrafter77.survivalinc.stats.ListIntRecord;
import enginecrafter77.survivalinc.stats.StatCapability;
import enginecrafter77.survivalinc.stats.StatProvider;
import enginecrafter77.survivalinc.stats.StatRecord;
import enginecrafter77.survivalinc.stats.StatRegisterEvent;
import enginecrafter77.survivalinc.stats.StatTracker;
import enginecrafter77.survivalinc.stats.impl.SanityTendencyModifier;
import enginecrafter77.survivalinc.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DrugModule implements StatProvider<DrugRecord> {
	private static final long serialVersionUID = 6277772840198745918L;

	
	
	public static int numDrugs;

	public static DrugModule instance= new DrugModule();
	
	public void init()
	{
		numDrugs= Drug.drugList.size();
	}
	
	public void take(Drug drug, EntityPlayer player)
	{
		int index= Drug.drugList.indexOf(drug);

		StatTracker tracker = player.getCapability(StatCapability.target, null);
		DrugRecord record= tracker.getRecord(DrugModule.instance);

		
		record.lastUse[index]= player.world.getTotalWorldTime();
		record.high[index]+= drug.highDuration;
		
		if(Util.chance(drug.dependencyChance))
		{
			record.dependencyLevel[index]++;
			
			player.sendMessage(new TextComponentString("You've become more addicted to "+drug.name));
		}
		
	}
	
	@Override
	public void update(EntityPlayer player, StatRecord origRecord) {
		if(!player.world.isRemote)
			return;
		
		DrugRecord record= (DrugRecord) origRecord;
		
		for(int i= 0; i < numDrugs; i++)
		{
			if(record.high[i] > 0)
				record.high[i]--;
		
			if(record.dependencyLevel[i] > 0 && record.high[i] == 0)
			{
				Drug drug= Drug.drugList.get(i);
				long delta= player.world.getTotalWorldTime() - record.lastUse[i];
				int threshold= drug.highDuration * 10 / record.dependencyLevel[i];
				if(delta >  threshold)
				{
					player.sendMessage(new TextComponentString("ARRRGHH!!"));
					
					SanityTendencyModifier.instance.addToTendencyOneTime(-drug.satisfaction * 5f, "No "+drug.name, player);
					
					if(Util.chance(50))
					{
						record.dependencyLevel[i]--;

						if(record.dependencyLevel[i]>0)
							player.sendMessage(new TextComponentString("You've become less addicted to "+drug.name));
						else
							player.sendMessage(new TextComponentString("You aren't addicted to "+drug.name+" anymore"));
					}
					
				}
				else
				if((float)delta / (float)threshold > 0.8f && player.world.getTotalWorldTime() % 600 == 0)
				{
					player.sendMessage(new TextComponentString(drug.name+" please!"));
				}
			}
		}

	}

	@Override
	public ResourceLocation getStatID() {
		return new ResourceLocation(SurvivalInc.MOD_ID, "drugs");
	}

	@Override
	public DrugRecord createNewRecord() {
		DrugRecord record= new DrugRecord();
		return record;
	}

	@Override
	public Class<DrugRecord> getRecordClass() {
		return DrugRecord.class;
	}

	@SubscribeEvent
	public static void registerStat(StatRegisterEvent event)
	{
		event.register(DrugModule.instance);
	}
		
	
	
}
