package enginecrafter77.survivalinc.strugglecraft;

import java.util.ArrayList;

import enginecrafter77.survivalinc.stats.impl.SanityTendencyModifier;
import enginecrafter77.survivalinc.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class Drug {
	public static ArrayList<Drug> drugList= new ArrayList<Drug>();
	
	public static Drug JOINT= new Drug("Joint", new DrugEffect[] {new DrugEffect(MobEffects.SLOWNESS, 50, 60,120,0,3), new DrugEffect(MobEffects.WEAKNESS, 50, 60,120,0,3), new DrugEffect(MobEffects.MINING_FATIGUE, 50, 60,120,0,2), new DrugEffect(MobEffects.HUNGER, 30, 20,60,0,2)}, 5f, 2, 60);
	public static Drug CIGARETTE= new Drug("Cigarette", null, 1f, 20, 20);
	public static Drug COCA_LEAVES= new Drug("Coca Leaves", new DrugEffect[] {new DrugEffect(MobEffects.HEALTH_BOOST, 30, 60,120,0,1), new DrugEffect(MobEffects.SATURATION, 100, 90,180,0,2), new DrugEffect(MobEffects.HASTE, 30, 60,120,0,1)}, 1f, 5, 30);
	public static Drug OPIUM_PIPE= new Drug("Pipe", new DrugEffect[] {new DrugEffect(MobEffects.SLOWNESS, 90, 60,180,1,4), new DrugEffect(MobEffects.WEAKNESS, 90, 60,180,1,4), new DrugEffect(MobEffects.MINING_FATIGUE, 90, 60,180,1,4)}, 15f, 30, 180);
	
	
	static class DrugEffect
	{
		public Potion potion;
		public float chance;
		public int minDuration, maxDuration;
		public int minAmplifier, maxAmplifier;
		
		public DrugEffect(Potion p, float c, int mind, int maxd, int minamp, int maxamp) 
		{
			potion= p;
			chance= c;
			minDuration= mind*20;
			maxDuration=maxd*20;
			minAmplifier= minamp;
			maxAmplifier= maxamp;
		}
	}
	
	
	String name;
	DrugEffect[] sideEffects;
	float satisfaction;
	int dependencyChance;
	int highDuration;
	
	public Drug(String n, DrugEffect[] effects, float satis, int depend, int high)
	{
		name= n;
		sideEffects= effects;
		satisfaction= satis;
		dependencyChance= depend;
		highDuration= high*20;
		
		drugList.add(this);
	}
	
	public void take(EntityPlayer player)
	{
		if(sideEffects != null)
		{
			for(DrugEffect effect : sideEffects)
			{
				
				if(Util.chance(effect.chance))
				{
					player.addPotionEffect(new PotionEffect(effect.potion, Util.rnd(effect.minDuration, effect.maxDuration), Util.rnd(effect.minAmplifier, effect.maxAmplifier)));
				}
			}
		}
		
		DrugModule.instance.take(this, player);
	}
}
