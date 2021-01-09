package enginecrafter77.survivalinc.strugglecraft;

import java.util.ArrayList;
import java.util.List;

import enginecrafter77.survivalinc.SurvivalInc;
import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.stats.ListIntRecord;
import enginecrafter77.survivalinc.stats.SimpleStatRecord;
import enginecrafter77.survivalinc.stats.StatCapability;
import enginecrafter77.survivalinc.stats.StatProvider;
import enginecrafter77.survivalinc.stats.StatRecord;
import enginecrafter77.survivalinc.stats.StatRegisterEvent;
import enginecrafter77.survivalinc.stats.StatTracker;
import enginecrafter77.survivalinc.stats.impl.SanityModifier;
import enginecrafter77.survivalinc.stats.impl.SanityRecord;
import enginecrafter77.survivalinc.stats.impl.SanityTendencyModifier;
import enginecrafter77.survivalinc.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;


public class TraitModule implements StatProvider<ListIntRecord>{
	private static final long serialVersionUID = 6269992840199029918L;

	public static final int POS_TRAIT=0, NEG_TRAIT=1, NEUT_TRAIT=2;
	
	public static int traitIDctr=0;
	
	public static final int USAGE_FREQUENCY_MODERATELY= 10;
	public static final int USAGE_FREQUENCY_OFTEN= 100;
	public static final int USAGE_FREQUENCY_VERY_OFTEN= 1000;
	public static final int USAGE_FREQUENCY_CONTINUOUSLY= 10000;
	
	public enum TRAITS {
		PETLOVER("Petlover", POS_TRAIT,70, USAGE_FREQUENCY_OFTEN),				// likes to be near pets
		GREEN_THUMB("Green Thumb", POS_TRAIT,0),
		STAR_CHILD("Star Child", POS_TRAIT,0),
		COURAGEOUS("Courageous", POS_TRAIT,20,USAGE_FREQUENCY_OFTEN),			// less afraid of darkness
		WARM("Warm", POS_TRAIT,50, USAGE_FREQUENCY_CONTINUOUSLY),				// lower freezing threshold
		AQUAPHILE("Aquaphile", POS_TRAIT,70, USAGE_FREQUENCY_OFTEN),			// more comfortable in (cold) water
		HARD_WORKING("Hard Working", POS_TRAIT,0),
		DISCIPLINED("Disciplined", POS_TRAIT,50),								// less chance of drug dependency
		RUNNER("Runner", POS_TRAIT,100, USAGE_FREQUENCY_CONTINUOUSLY),
		MINER("Miner", POS_TRAIT,0),
		HELIOPHILE("Heliophile", POS_TRAIT,50, USAGE_FREQUENCY_CONTINUOUSLY),	// loves sunshine		
		WORKAHOLIC("Workaholic", POS_TRAIT,50, USAGE_FREQUENCY_OFTEN),			// mood boost from breaking blocks
		NONDISCRIMINATORY("Nondiscriminatory", POS_TRAIT,100),					// food variety not as important
		BLOODTHIRSTY("Bloodthirsty", POS_TRAIT,80),								// sanity boost from killing
		VERSATILE("Versatile", POS_TRAIT,0), 			
		AGGRESSIVE("Aggressive", POS_TRAIT,0),									// 40, but disabled cause aggressive potion effect may not have levels	
		AMUSING("Amusing", POS_TRAIT,0),										// tells jokes to increase other players mood
		CURIOUS("Curious", POS_TRAIT,0),
		ECSTATIC("Ecstatic", POS_TRAIT,20, USAGE_FREQUENCY_CONTINUOUSLY),		// less haste-push dampening
		DECADENT("Decadent", POS_TRAIT,0),
		NOBLE("Noble", POS_TRAIT,0),	
		DREAMER("Dreamer", POS_TRAIT,0),
		QUICK("Quick", POS_TRAIT,0),
		CHEERFUL("Cheerful", POS_TRAIT,0, USAGE_FREQUENCY_MODERATELY),			// TOO MUCH REWRITING/EXCEPTIONS chance to block sanity penalties (only one-timers)
		EDUCATED("Educated", POS_TRAIT,0),
		PATIENT("Patient", POS_TRAIT,0),	
		MASOCHIST("Masochist", POS_TRAIT,70),									// sanity from being hurt
		HARD_SHELL("Hard shell", POS_TRAIT,50),									// less sanity loss from damage
		HEALTHY("Healthy", POS_TRAIT, 0),
		
		
		
		LONER("Loner", NEUT_TRAIT,0),
		EMOTIONAL("Emotional", NEUT_TRAIT,0),
		NUDIST("Nudist", NEUT_TRAIT,0),	
		EXPLOSIVE("Explosive", NEUT_TRAIT,0),
		ACTIVE("Active", NEUT_TRAIT,50, USAGE_FREQUENCY_CONTINUOUSLY),			// more sanity from running, less from sleep
		DEFENSIVE("Defensive", NEUT_TRAIT,0),
		STUBBORN("Stubborn", NEUT_TRAIT,0),
		TASTELESS("Tasteless", NEG_TRAIT,50),									// no sanity effects from food
		ANIMAL_LOVER("Animal Lover", NEUT_TRAIT, 70, USAGE_FREQUENCY_VERY_OFTEN),// likes to be around but not to kill animals
	
		
		
		
		PACIFIST("Pacifist", NEG_TRAIT,50),										// sanity loss from killing mobs
		VEGETARIAN("Vegetarian", NEG_TRAIT,0),	
		SENSITIVE("Sensitive", NEG_TRAIT,0),									
		GOURMET("Gourmet", NEG_TRAIT,100),										// more annoyed by same food
		BLAND("Bland", NEG_TRAIT,0),
		CLUMSY("Clumsy", NEG_TRAIT,0),		
		DEMANDING("Demanding", NEG_TRAIT,0),
		NERVOUS("Nervous", NEG_TRAIT,0),
		IDIOTIC("Idiotic", NEG_TRAIT,0),
		IMPULSIVE("Impulsive", NEG_TRAIT,0),
		SLEEPY("Sleepy", NEG_TRAIT,100),										// wants to sleep during night
		PARANOID("Paranoid", NEG_TRAIT,0),
		UNINSPIRED("Uninspired", NEG_TRAIT,0),
		FRAGILE("Fragile", NEG_TRAIT,100),										// takes double damage
		CRYBABY("Crybaby", NEG_TRAIT,0),
		EASILY_ADDICTED("Addict", NEG_TRAIT,0),
		BITTER("Bitter", NEG_TRAIT,0),
		DEPRESSED("Depressed", NEG_TRAIT,80),
		AFRAID_DARKNESS("Afraid", NEG_TRAIT,70),
		AFRAID_MOBS("Coward", NEG_TRAIT,70),									// doesn't like to be around hostiles
		UNDEAD("Undead", NEG_TRAIT,20);											// doesn't like sunshine, may catch fire
				
		
		// new traits go here, to not corrupt save game trait ids
		
		
		public String traitName;
		int type,id,chance,usageFrequency;
		
		TRAITS(String s,int t, int c) { traitName=s; type=t; chance=c; id=traitIDctr;traitIDctr++;usageFrequency= 1;}
		TRAITS(String s,int t, int c, int freq) { traitName=s; type=t; chance=c; id=traitIDctr;traitIDctr++; usageFrequency= freq;}
		
	}
/*	
	public class TraitListEntry {
		public TRAITS trait;
		public int tier;
		
		TraitListEntry(TRAITS t)
		{
			this(t, 0);
		}

		TraitListEntry(TRAITS t, int i)
		{
			trait=t;
			tier=i;
		}


	}
	
	public ArrayList<TraitListEntry> listTraits=new ArrayList<TraitListEntry>();
*/
	
//	EntityPlayer playerEntity;

	
	public static TraitModule instance= new TraitModule();
	
	public TraitModule()
	{
	}
	
	public void init()
	{
		MinecraftForge.EVENT_BUS.register(TraitModule.class);
	}
/*	
	@SubscribeEvent
	public static void onSpawn(EntityJoinWorldEvent event)
	{
		if(!event.getWorld().isRemote)
			return;
		
		Entity ent= event.getEntity();
		if(ent instanceof EntityPlayer && !ent.isDead)
		{
			EntityPlayer player= (EntityPlayer) ent;
			
			if(!Util.thisClientOnly(player))
				return;

			StatTracker tracker = player.getCapability(StatCapability.target, null);
			ListIntRecord record= tracker.getRecord(TraitModule.instance);


			record.Clear();
			
			
			for(int i= 0; i < 3; i++)
				instance.AddRandomTraits(player);
		}
	}
*/	
	
	
	void ClearTraits(EntityPlayer player)
	{
		StatTracker tracker = player.getCapability(StatCapability.target, null);
		ListIntRecord record= tracker.getRecord(TraitModule.instance);
		
		record.Clear();
	}
	
	ArrayList<Integer> getTraitList(EntityPlayer player)
	{
		StatTracker tracker = player.getCapability(StatCapability.target, null);
		ListIntRecord record= tracker.getRecord(TraitModule.instance);
		
		return record.getList();
	}

	public boolean HasTrait(EntityPlayer player, TRAITS t)
	{
		StatTracker tracker = player.getCapability(StatCapability.target, null);
		ListIntRecord record= tracker.getRecord(TraitModule.instance);

		return HasTrait(record, t);
	}
	
	public boolean HasTrait(ListIntRecord record, TRAITS t)
	{
		if(!ModConfig.TRAITS.enabled)
			return false;
		
		
		for(int i=0; i < record.getListSize(); i+= 2)
			if(record.get(i) == t.id)
				return true;
		
		return false;
	}
	
	public int TraitTier(EntityPlayer player, TRAITS t)
	{
		StatTracker tracker = player.getCapability(StatCapability.target, null);
		ListIntRecord record= tracker.getRecord(TraitModule.instance);
		
		return TraitTier(record, t);
	}
	
	public int TraitTier(ListIntRecord record, TRAITS t)
	{

		for(int i=0; i < record.getListSize(); i+= 2)
			if(record.get(i) == t.id)
				return record.get(i+1);
				
		return 0;
		
	}
	
	public TRAITS getTrait(int id)
	{
		for(TRAITS t : TRAITS.values())
			if(t.id == id)
				return t;
		return null;
	}
	
/*	public TraitListEntry getEntry(TRAITS t)
	{
		for(TraitListEntry entry : listTraits)
			if(entry.trait == t)
				return entry;
		return null;
	}
*/

	public void AddRandomTrait(EntityPlayer player, boolean dontStackPositive)
	{
		StatTracker tracker = player.getCapability(StatCapability.target, null);
		ListIntRecord record= tracker.getRecord(TraitModule.instance);
//		AddRandomTrait(record);
//	}
	
	
//	public void AddRandomTrait(ListIntRecord record)
//	{
		TRAITS t;
		int i=0;
		
//		playerEntity.sendMessage(new TextComponentString("Trying to add random trait"));


		
		
		if( DeathCounter.getDeaths(player)>0)
		{
			if(Util.chance(DeathCounter.getDeaths(player)*3))
			{
//				playerEntity.sendMessage(new TextComponentString("Looking for negative trait"));
				
				do
				{
				while((t=TRAITS.values()[Util.rnd(traitIDctr)]).type!=NEG_TRAIT)
				{
				}
				i++;
				if(i>2000)
					return;
				}
				while((!HasTrait(record, t) && Util.chance(90f)) || Util.chance(100-t.chance));
			
				
				
				AddTrait(player, t);
				return;
			}
		}
		
		i=0;

//		playerEntity.sendMessage(new TextComponentString("Looking for pos/neut/remove trait"));

		do
		{
		while((t=TRAITS.values()[Util.rnd(traitIDctr)]).type==NEG_TRAIT)
		{
		}


		
		i++;
		if(i>2000)
			return;
		}	// chance + 5 per TRAITLEVEL | TODO lower probabilty to add neut/neg trait the longer player has lived ( sum of all trait tiers)
		while((!HasTrait(record, t) && Util.chance(50)) || t.chance==0 /*|| (t.type == POS_TRAIT && Util.rnd(100)>=(t.chance+playerEntity.experienceLevel-5))*/ || (t.type == NEUT_TRAIT && (Util.chance(100-t.chance) || Util.chance(DeathCounter.getDeaths(player)*3))) || (dontStackPositive && t.type == POS_TRAIT && HasTrait(record,t)));

		int numNegTraits=0;
		
	    for(int j=0;j<record.getListSize();j+= 2)
	    {
		     TRAITS trait= getTrait(record.get(j));
		     if(trait.type== NEG_TRAIT)
		    	 numNegTraits++;
	    }
		
	    boolean flag= false;
/*	    
	    if(myrand.nextInt(100)<numNegTraits*20)
	    {
			playerEntity.addChatMessage("Trying to remove negative trait ("+numNegTraits+" negative traits found");

	    	// loop until trait is removed
		    while(flag==false)
		    {
	    	for(int j=0;j<listTraits.size();j++)
		    {
			     // 10% chance to remove this trait
	    	     TRAITS trait= listTraits.get(j).trait;
	    		 if(myrand.nextInt(MathHelper.floor_float(trait.chance/10.0f)+1)==0)
			     {
			     if(trait.type== NEG_TRAIT)
			     {
			    	 playerEntity.addChatMessage("Removed negative Trait: "+trait.traitName);
       				 lastTraitAtLevel=this.lastXPlevel;
			    	 RemoveTrait(trait);
			    	 flag=true;
			    	 return;
			     }
			     }
				 
		    }
		    }
	    }
	    else
*/	    {
			AddTrait(player, t);
//			playerEntity.addChatMessage("New Trait: "+t.traitName);
	    }

	}

	public void AddTrait(EntityPlayer player, TRAITS t)
	{
		StatTracker tracker = player.getCapability(StatCapability.target, null);
		ListIntRecord record= tracker.getRecord(TraitModule.instance);
//		AddTrait(record, t);
//	}
	
//	public void AddTrait(ListIntRecord record, TRAITS t)
//	{
		
		boolean flag= false;
		
		for(int i=0; i < record.getListSize(); i+= 2)
			if(record.get(i) == t.id)
			{
				int newTier= record.get(i+1)+1;
				record.set(i+1, newTier);
				
				player.sendMessage(new TextComponentString("Trait "+t.traitName+" Level Up"));// => Level "+newTier));
				flag=true;
			}
		
		
		if(!flag)
		{
			record.Add(t.id);
			record.Add(0);
			player.sendMessage(new TextComponentString("New Trait "+t.traitName+" added !!"));
		}

	}
/*	
	public void RemoveTrait(TRAITS t)
	{
		boolean flag= false;
		
		for(int i=0; i < listTraits.size(); i++)
			if(listTraits.get(i).trait == t)
			{
				if(listTraits.get(i).tier == 0)
				{
					listTraits.remove(i);
					playerEntity.addChatMessage("Trait "+t.traitName+" removed");
					break;
				}
				else
				{
					listTraits.get(i).tier--;
					playerEntity.addChatMessage("Trait "+t.traitName+" Level - 1 => Level "+listTraits.get(i).tier);
				}
				
			}

	}
*/	
//	public void AddRandomSpawnTrait(ListIntRecord record)
/*	public void AddRandomSpawnTrait(EntityPlayer player)
	{
		StatTracker tracker = player.getCapability(StatCapability.target, null);
		ListIntRecord record= tracker.getRecord(TraitModule.instance);

		TRAITS t;
		int j=0;

		record.Clear();
		
		System.out.println("Adding random spawn trait");

		
		if( DeathCounter.getDeaths(player)>0)
		{
			// 2 NEG's possible when death>10, 3 when death>20..,
			for(int i=0; i<(Math.floor(DeathCounter.getDeaths(player)/10.0f)+1);i++)
			{
				if(Util.chance((DeathCounter.getDeaths(player)*3) > 90 ? 90 : (DeathCounter.getDeaths(player)*3)))
				{
					do
					{
						while((t=TRAITS.values()[Util.rnd(traitIDctr)]).type != NEG_TRAIT)
						{
						}
						j++;
						if((!HasTrait(record, t) || TraitTier(record, t) < 5) && Util.chance(t.chance))
						{
							AddTrait(player, t);
							return;
						}
					} while(j < 2000);
				}
			
			}
		
		}
	}
*/	
	public void UsingTrait(EntityPlayer player, TRAITS t)
	{
		UsingTrait(player, t, 1f);
	}

	
	public void UsingTrait(EntityPlayer player, TRAITS t, float factor)
	{
		if(player.world.isRemote)
			return;
		
//		if(t.type == NEUT_TRAIT)
//			return;
		
		if(ModConfig.DEBUG.traits && !HasTrait(player, t))
		{
			String str= "ERROR: Using "+t.toString();
			System.out.println(str);
			player.sendMessage(new TextComponentString(str));
			return;
		}
		
		int tier= TraitTier(player, t);
		
		if(Util.chanced((100f * factor) / (ModConfig.TRAITS.baseIncreaseDifficulty * Math.pow(tier+1,2) * t.usageFrequency)))
		{
			AddTrait(player, t);
//			playerEntity.sendMessage(new TextComponentString("Trait Level Up: "+t.traitName+" Lvl "+(tier)));
		}
		
	}
	
	
	@Override
	public void update(EntityPlayer target, StatRecord record) {
		if(target.world.isRemote || target.isDead)
			return;
		
		ListIntRecord trec= (ListIntRecord) record;
		
		if(trec.getListSize() == 0)
		{
			for(int i= 0; i < 3; i++)
				instance.AddRandomTrait(target, true);
		}
	}

	@Override
	public ResourceLocation getStatID() {
		return new ResourceLocation(SurvivalInc.MOD_ID, "traits");
	}

	@Override
	public ListIntRecord createNewRecord() {
		ListIntRecord record= new ListIntRecord();
		return record;
	}

	@Override
	public Class<ListIntRecord> getRecordClass() {
		return ListIntRecord.class;
	}

	@SubscribeEvent
	public static void registerStat(StatRegisterEvent event)
	{
		event.register(TraitModule.instance);
	}
	
}
