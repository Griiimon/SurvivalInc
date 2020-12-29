package enginecrafter77.survivalinc.strugglecraft;

import java.util.ArrayList;

import enginecrafter77.survivalinc.stats.impl.SanityTendencyModifier;
import enginecrafter77.survivalinc.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class Drug {

	
	public static Drug JOINT= new Drug("Joint", new DrugEffect[] {new DrugEffect(MobEffects.SLOWNESS, 50, 60,120,0,3), new DrugEffect(MobEffects.WEAKNESS, 50, 60,120,0,3)}, 1f, 2, 60);
	
	
	public static ArrayList<Drug> drugList= new ArrayList<Drug>();
	
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
		SanityTendencyModifier.instance.addToTendencyOneTime(satisfaction, name, player);

		for(DrugEffect effect : sideEffects)
		{
			
			if(Util.chance(effect.chance))
			{
				player.addPotionEffect(new PotionEffect(effect.potion, Util.rnd(effect.minDuration, effect.maxDuration), Util.rnd(effect.minAmplifier, effect.maxAmplifier)));
			}
		}
		
		DrugModule.instance.take(this, player);
	}
}
