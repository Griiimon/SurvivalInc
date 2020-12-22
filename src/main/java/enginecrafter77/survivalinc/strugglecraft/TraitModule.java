package enginecrafter77.survivalinc.strugglecraft;

import java.util.ArrayList;
import java.util.List;

import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.stats.impl.SanityTendencyModifier;
import enginecrafter77.survivalinc.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class TraitModule implements INBTSerializable<NBTTagCompound>{
	public static final int POS_TRAIT=0, NEG_TRAIT=1, NEUT_TRAIT=2;
	
	public static int traitIDctr=0;
	
	public static final int USAGE_FREQUENCY_MODERATELY= 10;
	public static final int USAGE_FREQUENCY_OFTEN= 100;
	public static final int USAGE_FREQUENCY_CONTINUOUSLY= 1000;
	
	public enum TRAITS {
		PETLOVER("Petlover", POS_TRAIT,70),
		ANIMAL_LOVER("Animal Lover", POS_TRAIT, 70, USAGE_FREQUENCY_CONTINUOUSLY),
		GREEN_THUMB("Green Thumb", POS_TRAIT,0),
		STAR_CHILD("Star Child", POS_TRAIT,0),
		COURAGEOUS("Courageous", POS_TRAIT,20,USAGE_FREQUENCY_OFTEN),			// less afraid of darkness
		WARM("Warm", POS_TRAIT,50, USAGE_FREQUENCY_CONTINUOUSLY),				// lower freezing threshold
		AQUAPHILE("Aquaphile", POS_TRAIT,70, USAGE_FREQUENCY_OFTEN),			// more comfortable in (cold) water
		HARD_WORKING("Hard Working", POS_TRAIT,0),
		DISCIPLINED("Disciplined", POS_TRAIT,0),								//aka professional junkie
		RUNNER("Runner", POS_TRAIT,100, USAGE_FREQUENCY_CONTINUOUSLY),
		MINER("Miner", POS_TRAIT,0),
		HELIOPHILE("Heliophile", POS_TRAIT,100, USAGE_FREQUENCY_CONTINUOUSLY),	// loves sunshine		
		WORKAHOLIC("Workaholic", POS_TRAIT,100, USAGE_FREQUENCY_CONTINUOUSLY),	// less sleep deprivation impact
		NONDISCRIMINATORY("Nondiscriminatory", POS_TRAIT,0),					// food variety not as important
		BLOODTHIRSTY("Bloodthirsty", POS_TRAIT,80),								// sanity boost from killing
		VERSATILE("Versatile", POS_TRAIT,0), 			
		AGGRESSIVE("Aggressive", POS_TRAIT,0),									// 40, but disabled cause aggressive potion effect may not have levels	
		AMUSING("Amusing", POS_TRAIT,0),										// tells jokes to increase other players mood
		CURIOUS("Curious", POS_TRAIT,0),
		ECSTATIC("Ecstatic", POS_TRAIT,20),										// less haste-push dampening
		DECADENT("Decadent", POS_TRAIT,0),
		NOBLE("Noble", POS_TRAIT,0),	
		DREAMER("Dreamer", POS_TRAIT,60),
		QUICK("Quick", POS_TRAIT,50),
		CHEERFUL("Cheerful", POS_TRAIT,30),
		EDUCATED("Educated", POS_TRAIT,50),
		PATIENT("Patient", POS_TRAIT,40),	
		MASOCHIST("Masochist", POS_TRAIT,70),
		HARD_SHELL("Hard shell", POS_TRAIT,80),
		HEALTHY("Healthy", POS_TRAIT, 50),
		
		
		
		LONER("Loner", NEUT_TRAIT,30),
		EMOTIONAL("Emotional", NEUT_TRAIT,50),
		NUDIST("Nudist", NEUT_TRAIT,30),	
		EXPLOSIVE("Explosive", NEUT_TRAIT,0),
		ACTIVE("Active", NEUT_TRAIT,100),
		DEFENSIVE("Defensive", NEUT_TRAIT,70),
		STUBBORN("Stubborn", NEUT_TRAIT,100),
	
		
		
		
		PACIFIST("Pacifist", NEG_TRAIT,50),	
		VEGETARIAN("Vegetarian", NEG_TRAIT,70),	
		SENSITIVE("Sensitive", NEG_TRAIT,100),
		TASTELESS("Tasteless", NEG_TRAIT,50),
		GOURMET("Gourmet", NEG_TRAIT,100),
		BLAND("Bland", NEG_TRAIT,0),
		CLUMSY("Clumsy", NEG_TRAIT,0),		
		DEMANDING("Demanding", NEG_TRAIT,0),
		NERVOUS("Nervous", NEG_TRAIT,0),
		IDIOTIC("Idiotic", NEG_TRAIT,0),
		IMPULSIVE("Impulsive", NEG_TRAIT,0),
		SLEEPY("Sleepy", NEG_TRAIT,100),		
		PARANOID("Paranoid", NEG_TRAIT,0),
		UNINSPIRED("Uninspired", NEG_TRAIT,100),
		FRAGILE("Fragile", NEG_TRAIT,100),
		CRYBABY("Crybaby", NEG_TRAIT,100),
		EASILY_ADDICTED("Addict", NEG_TRAIT,100),
		BITTER("Bitter", NEG_TRAIT,70),
		AFRAID_DARKNESS("Afraid", NEG_TRAIT,70),
		AFRAID_MOBS("Coward", NEG_TRAIT,70),
		UNDEAD("Undead", NEG_TRAIT,20);
				
		
		// new traits go here, to not corrupt save game trait ids
		
		
		public String traitName;
		int type,id,chance,usageFrequency;
		
		TRAITS(String s,int t, int c) { traitName=s; type=t; chance=c; id=traitIDctr;traitIDctr++;usageFrequency= 1;}
		TRAITS(String s,int t, int c, int freq) { traitName=s; type=t; chance=c; id=traitIDctr;traitIDctr++; usageFrequency= freq;}
		
	}
	
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
	
	EntityPlayer playerEntity;
	int deathCtr= 0;
	
	public static TraitModule instance= new TraitModule();
	
	public TraitModule()
	{
	}
	
	public void init()
	{
		MinecraftForge.EVENT_BUS.register(TraitModule.class);
	}
	
	@SubscribeEvent
	public static void onSpawn(EntityJoinWorldEvent event)
	{
		if(event.getWorld().isRemote)
			return;
		
		Entity ent= event.getEntity();
		if(ent instanceof EntityPlayer)
		{
			// TODO only for local player
			instance.playerEntity= (EntityPlayer) ent;
			
			instance.listTraits.clear();
//			if(instance.listTraits.isEmpty())
				for(int i= 0; i < 3; i++)
					instance.AddRandomTrait();
		}
	}
	
	
	@SubscribeEvent
	public static void onEntityKilled(LivingDeathEvent event)
	{
		Entity ent= event.getEntity();
		if(ent instanceof EntityPlayer && (EntityPlayer)ent == instance.playerEntity)
		{
			instance.deathCtr++;
		}

	
	}

	
	public boolean HasTrait(TRAITS t)
	{
		if(!ModConfig.TRAITS.enabled)
			return false;
		
		for(int i=0; i < listTraits.size(); i++)
			if(listTraits.get(i).trait == t)
				return true;
		
		return false;
	}
	
	public int TraitTier(TRAITS t)
	{
		for(int i=0; i < listTraits.size(); i++)
			if(listTraits.get(i).trait == t)
				return listTraits.get(i).tier + 1;
		
		return 0;
		
	}
	
	public TraitListEntry getEntry(TRAITS t)
	{
		for(TraitListEntry entry : listTraits)
			if(entry.trait == t)
				return entry;
		return null;
	}
	
	public void AddRandomTrait()
	{
		TRAITS t;
		int i=0;
		
//		playerEntity.sendMessage(new TextComponentString("Trying to add random trait"));

		if( deathCtr>0)
		{
			if(Util.chance(deathCtr*3))
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
				while((!HasTrait(t) && Util.chance(90f)) || Util.chance(100-t.chance));
			
				
				
				AddTrait(t);
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
		}	// chance + 5 per TRAITLEVEL
		while((!HasTrait(t) && Util.chance(90)) || t.chance==0 || (t.type == POS_TRAIT && Util.rnd(100)>=(t.chance+playerEntity.experienceLevel-5)) || (t.type == NEUT_TRAIT && (Util.chance(100-t.chance) || Util.chance(deathCtr*3))));

		int numNegTraits=0;
		
	    for(int j=0;j<listTraits.size();j++)
	    {
		     TRAITS trait= listTraits.get(j).trait;
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
			AddTrait(t);
//			playerEntity.addChatMessage("New Trait: "+t.traitName);
	    }
		

	}
	
	public void AddTrait(TRAITS t)
	{
		
		boolean flag= false;
		
		for(int i=0; i < listTraits.size(); i++)
			if(listTraits.get(i).trait == t)
			{
				listTraits.get(i).tier++;
				playerEntity.sendMessage(new TextComponentString("Trait "+t.traitName+" Level + 1 => Level "+listTraits.get(i).tier + 1));
				flag=true;
			}
		
		if(!flag)
		{
			listTraits.add(new TraitListEntry(t));
			playerEntity.sendMessage(new TextComponentString("New Trait "+t.traitName+" added !!"));
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
	public void AddRandomSpawnTrait()
	{
		TRAITS t;
		int j=0;

		listTraits.clear();
		
		System.out.println("Adding random spawn trait");

		
		if( deathCtr>0)
		{
			// 2 NEG's possible when death>10, 3 when death>20..,
			for(int i=0; i<(Math.floor(deathCtr/10.0f)+1);i++)
			{
				if(Util.chance((deathCtr*3) > 90 ? 90 : (deathCtr*3)))
				{
					do
					{
						while((t=TRAITS.values()[Util.rnd(traitIDctr)]).type != NEG_TRAIT)
						{
						}
						j++;
//						if(j>2000)
//							return;
						// !!!  !HasTrait && xchance missing
		//				while(myrand.nextInt(100)>=t.chance);
						if((!HasTrait(t) || TraitTier(t) < 5) && Util.chance(t.chance))
						{
							AddTrait(t);
							return;
						}
		//				playerEntity.addChatMessage("New Trait: "+t.traitName);
					} while(j < 2000);
				}
			
			}
		
		}
	}
	
	public void UsingTrait(TRAITS t)
	{
		UsingTrait(t, 1f);
	}

	
	public void UsingTrait(TRAITS t, float factor)
	{
		TraitListEntry entry= getEntry(t);
		
		if(entry== null)
		{
			String str= "ERROR: Using "+t.toString();
			System.out.println(str);
			playerEntity.sendMessage(new TextComponentString(str));
			return;
		}
		
		if(Util.chanced((100f * factor) / (ModConfig.TRAITS.baseIncreaseDifficulty * Math.pow(entry.tier+1,2) * entry.trait.usageFrequency)))
		{
			entry.tier++;
			playerEntity.sendMessage(new TextComponentString("Trait Level Up: "+entry.trait.traitName+" Lvl "+(entry.tier+1)));
		}
		
	}
	

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		int[]arr=  nbt.getIntArray("traits");
		System.out.print(arr);
		listTraits.clear();
		int i= 0;
		for(i= 0; i < arr.length / 2; i++)
			listTraits.add(new TraitListEntry(TRAITS.values()[arr[i]], arr[i+arr.length/2]));
		deathCtr= nbt.getInteger("deaths");
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		int[] arr= new int[listTraits.size() * 2];
		int j= 0;
		for(int i= 0; i < listTraits.size(); i++)
			arr[j++]= listTraits.get(i).trait.id;
		for(int i= 0; i < listTraits.size(); i++)
			arr[j++]= listTraits.get(i).tier;
		tag.setIntArray("traits", arr);
		tag.setInteger("deaths", deathCtr);
		return tag;

	}
	
}
