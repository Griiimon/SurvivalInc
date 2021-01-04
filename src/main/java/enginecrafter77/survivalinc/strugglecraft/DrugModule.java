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
import enginecrafter77.survivalinc.strugglecraft.TraitModule.TRAITS;
import enginecrafter77.survivalinc.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DrugModule implements StatProvider<DrugRecord> {
	private static final long serialVersionUID = 6277772840198798718L;

	
	
	public static int numDrugs;

	public static DrugModule instance= new DrugModule();
	
	public void init()
	{
		MinecraftForge.EVENT_BUS.register(DrugModule.class);
		
		numDrugs= Drug.drugList.size();
	}
	
	public void take(Drug drug, EntityPlayer player)
	{
		
		
		int index= Drug.drugList.indexOf(drug);
		
		if(index == -1)
		{
			String error= "ERROR: Can't find "+drug.name+" in list!";
			player.sendMessage(new TextComponentString(error));
			System.out.println(error);
			return;
		}

		StatTracker tracker = player.getCapability(StatCapability.target, null);
		DrugRecord record= tracker.getRecord(DrugModule.instance);

		
		if(record.high[index] > drug.highDuration * Util.rnd(2f,4f))
			player.sendMessage(new TextComponentString("Sorry, can't get any higher.."));
		else
			SanityTendencyModifier.instance.addToTendencyServer(drug.satisfaction, drug.name, player);

		
		record.lastUse[index]= player.world.getTotalWorldTime();
		record.high[index]+= drug.highDuration;
		
		float factor= 1f;
		
		boolean isDisciplined= TraitModule.instance.HasTrait(player, TRAITS.DISCIPLINED);
		
		if(isDisciplined)
			factor= TraitModule.instance.TraitTier(player, TRAITS.DISCIPLINED)+2;
		
		
		if(Util.chance(drug.dependencyChance / factor) && record.dependencyLevel[index] < 5)
		{
			record.dependencyLevel[index]++;
			player.sendMessage(new TextComponentString("You've become more addicted to "+drug.name));
		}
		
	}
	
	@Override
	public void update(EntityPlayer player, StatRecord origRecord) {
//		if(!player.world.isRemote)
		if(player.world.isRemote || player.isDead)
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
					
					if(TraitModule.instance.HasTrait(player, TRAITS.DISCIPLINED))
						TraitModule.instance.UsingTrait(player, TRAITS.DISCIPLINED, drug.dependencyChance);
					
					record.lastUse[i]= player.world.getTotalWorldTime();
					
				}
				else
				if((float)delta / (float)threshold > 0.75f && player.world.getTotalWorldTime() % (drug.highDuration / 2) == 0)
				{
					player.sendMessage(new TextComponentString(drug.name+" please!"));
				}
			}
		}

	}

	public static DrugRecord getRecord(EntityPlayer player)
	{
		StatTracker tracker = player.getCapability(StatCapability.target, null);
		return tracker.getRecord(DrugModule.instance);
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
