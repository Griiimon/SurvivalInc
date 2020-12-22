package enginecrafter77.survivalinc.stats.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.google.common.collect.Range;

import enginecrafter77.survivalinc.SurvivalInc;
import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.net.StatSyncMessage;
import enginecrafter77.survivalinc.stats.SimpleStatRecord;
import enginecrafter77.survivalinc.stats.StatCapability;
import enginecrafter77.survivalinc.stats.StatProvider;
import enginecrafter77.survivalinc.stats.StatRecord;
import enginecrafter77.survivalinc.stats.StatRegisterEvent;
import enginecrafter77.survivalinc.stats.StatTracker;
import enginecrafter77.survivalinc.stats.effect.EffectApplicator;
import enginecrafter77.survivalinc.stats.effect.FunctionalEffectFilter;
import enginecrafter77.survivalinc.stats.effect.SideEffectFilter;
import enginecrafter77.survivalinc.stats.effect.ValueStatEffect;
import enginecrafter77.survivalinc.strugglecraft.TraitModule;
import enginecrafter77.survivalinc.strugglecraft.TraitModule.TRAITS;
import enginecrafter77.survivalinc.util.Util;

public class SanityTendencyModifier implements StatProvider<SanityRecord> {
	private static final long serialVersionUID = 6707924203617912749L;
	
	public static final SanityTendencyModifier instance = new SanityTendencyModifier();
	
	public final EffectApplicator<SanityRecord> effects;
	
	class ReasonEntry
	{
		public String name;
		public float value;
		
		public ReasonEntry(String n, float v) {	name= n; value= v; }
	}

	ArrayList<ReasonEntry> reasons;
	

	
	String currentReasonStr= "";
	int reasonTicks= 0;
	int reasonFlipTicks= 60;

	public SanityTendencyModifier()
	{
		this.effects = new EffectApplicator<SanityRecord>();
		reasons= new ArrayList<ReasonEntry>();
	}
	
	public void init()
	{
		MinecraftForge.EVENT_BUS.register(SanityTendencyModifier.class);

		if(ModConfig.WETNESS.enabled) this.effects.add(SanityTendencyModifier::whenWet).addFilter(FunctionalEffectFilter.byPlayer(EntityPlayer::isInWater).invert());
		this.effects.add(new ValueStatEffect(ValueStatEffect.Operation.OFFSET, 0.004F)).addFilter(FunctionalEffectFilter.byPlayer(EntityPlayer::isPlayerSleeping));
		this.effects.add(SanityTendencyModifier::whenInDark).addFilter(HydrationModifier.isOutsideOverworld.invert());
		this.effects.add(SanityTendencyModifier::whenNearEntities);
		this.effects.add(SanityTendencyModifier::whenRunning);
		this.effects.add(SanityTendencyModifier::whenInSun);
		this.effects.add(SanityTendencyModifier::sleepDeprivation);
		this.effects.add(SanityTendencyModifier::whenInWater);
		
		
	
	}
	
/*	
	@SubscribeEvent
	public static void onSpawn(EntityJoinWorldEvent event)
	{
		if(event.getWorld().isRemote)
			return;
		
		Entity ent= event.getEntity();
		if(ent instanceof EntityPlayer)
		{
			// TODO only for local player
			setTendency(0f, (EntityPlayer)ent);
		}
	}
*/
	
	public void addToTendency(float value, String reason, EntityPlayer player)
	{
		addToTendency(value, reason, player, false);
	}

	
	public void addToTendency(float value, String reason, EntityPlayer player, boolean forceAddReason)
	{
		StatTracker stats = player.getCapability(StatCapability.target, null);
		addToTendency(value, reason, stats.getRecord(SanityTendencyModifier.instance), forceAddReason);

	}
	

	public void addToTendency(float value, String reason, SanityRecord record)
	{
		addToTendency(value, reason, record, false);
	}

	
	public void addToTendency(float value, String reason, SanityRecord record, boolean forceAddReason)
	{
		record.addToValue(value);
		
		if(value == 0f || reason == "")
			return;
		
		if(reasons.size() < 3 || forceAddReason)
		{
			boolean alreadyUsed= false;
			for(ReasonEntry entry : reasons)
				if(entry.name == reason)
				{
					alreadyUsed= true;
					break;
				}
			
			if(!alreadyUsed)
			{
//				if(reasons.size() == 0)
//					reasonTicks= 0;
				reasons.add(new ReasonEntry(reason, value));
			}
		}
		
	}
	
	@Override
	public void update(EntityPlayer target, StatRecord record)
	{
		if(target.isCreative() || target.isSpectator()/* || target.world.isRemote*/) return;
		
		SanityRecord sanity = (SanityRecord)record;
		++sanity.ticksAwake;
		this.effects.apply(sanity, target);
		sanity.checkoutValueChange();
		
		if(!target.world.isRemote)
			tickReasons();
	}
	
	void tickReasons()
	{
		reasonTicks++;
		if(reasonTicks > reasonFlipTicks)
		{
			if(!reasons.isEmpty())
			{
				currentReasonStr= reasons.get(0).name;
				float value= reasons.get(0).value;
			
				char c= value > 0f ? '+' : '-';
				
				String postStr= ""+c;
				
				if(Math.abs(value) > 0.01f)
					postStr+= ""+c;
				
				if(Math.abs(value) > 0.03f)
					postStr+= ""+c;

				if(Math.abs(value) > 0.1f)
					postStr+= ""+c;

				currentReasonStr+= " "+postStr;
				
				reasons.remove(0);
			}
			else
				currentReasonStr= "";
			
			reasonTicks= 0;
		}
	}
	

	@SubscribeEvent
	public static void renderReason(RenderGameOverlayEvent.Post event)
	{
		if(instance.currentReasonStr == "")
			return;
		
		String str= instance.currentReasonStr;
		
	
		FontRenderer renderer= Minecraft.getMinecraft().fontRenderer;
		
		if(event.getType() == ElementType.TEXT)
			renderer.drawString(str, event.getResolution().getScaledWidth()-renderer.getStringWidth(str), event.getResolution().getScaledHeight()-renderer.FONT_HEIGHT, str.contains("+") ? 0x00ff00 : 0xff0000,false);
	}
	
	@Override
	public ResourceLocation getStatID()
	{
		return new ResourceLocation(SurvivalInc.MOD_ID, "sanity_tendency");
	}

	@Override
	public SanityRecord createNewRecord()
	{
		SanityRecord record= new SanityRecord();
		record.setValue((float)ModConfig.SANITY.startTendencyValue);
		return record;
	}
	
	@Override
	public Class<SanityRecord> getRecordClass()
	{
		return SanityRecord.class;
	}
	
	
	@SubscribeEvent
	public static void registerStat(StatRegisterEvent event)
	{
		event.register(SanityTendencyModifier.instance);
	}
	
	public static void sleepDeprivation(SanityRecord record, EntityPlayer player)
	{	
		boolean isWorkaholic= TraitModule.instance.HasTrait(TRAITS.WORKAHOLIC);
		
		int sleepMin= ModConfig.SANITY.sleepDeprivationMin;
		int sleepMax= ModConfig.SANITY.sleepDeprivationMax;
		
		if(isWorkaholic)
		{
			int offset= (TraitModule.instance.TraitTier(TRAITS.WORKAHOLIC)+1)*2000;
			sleepMin+= offset;
			sleepMax+= offset*4;
		}
		if(TraitModule.instance.HasTrait(TRAITS.SLEEPY))
		{
			int offset= (TraitModule.instance.TraitTier(TRAITS.SLEEPY)+1)*2000;
			sleepMin-= offset;
			sleepMax-= offset*4;
		}
		
		if(record.getTicksAwake() > sleepMin)
		{
//			record.addToValue(-(float)ModConfig.SANITY.sleepDeprivationDebuff * (record.getTicksAwake() - ModConfig.SANITY.sleepDeprivationMin) / (ModConfig.SANITY.sleepDeprivationMax - ModConfig.SANITY.sleepDeprivationMin));
			instance.addToTendency(-(float)ModConfig.SANITY.sleepDeprivationDebuff * (record.getTicksAwake() - sleepMin) / (sleepMax - sleepMin), "No sleep", record);
			if(isWorkaholic)
				TraitModule.instance.UsingTrait(TRAITS.WORKAHOLIC);
		}
	}
	

	
	public static void whenInDark(SanityRecord record, EntityPlayer player)
	{
		BlockPos position = new BlockPos(player.getPositionVector().add(0D, player.getEyeHeight(), 0D));
		int lightlevel = player.world.getLight(position);
		
		// If there is not enough light, steve/alex feels anxious
		int comfortLightLevel= ModConfig.SANITY.comfortLightLevel;

		boolean isCourageous= TraitModule.instance.HasTrait(TRAITS.COURAGEOUS);
		
		if(isCourageous)
			comfortLightLevel-= 2 + TraitModule.instance.TraitTier(TRAITS.COURAGEOUS);
		
		if(lightlevel < comfortLightLevel)
		{
			float darknesslevel = (float)(comfortLightLevel - lightlevel) / (float)comfortLightLevel;
			instance.addToTendency((float)ModConfig.SANITY.darkSpookFactorBase * -darknesslevel, "Darkness", record);
			if(isCourageous)
				TraitModule.instance.UsingTrait(TRAITS.COURAGEOUS);
		}
//		else
//		if(isCourageous && lightlevel < ModConfig.SANITY.comfortLightLevel)
//			TraitModule.instance.UsingTrait(TRAITS.COURAGEOUS);
			
	}
	
	public static void whenWet(SanityRecord record, EntityPlayer player)
	{
		float boundary = (float)ModConfig.SANITY.wetnessAnnoyanceThreshold;
		StatTracker stats = player.getCapability(StatCapability.target, null);
		SimpleStatRecord wetness = stats.getRecord(WetnessModifier.instance);		
		if(wetness.getNormalizedValue() > boundary)
		{
			instance.addToTendency(((wetness.getNormalizedValue() - boundary) / (1F - boundary)) * -(float)ModConfig.SANITY.maxWetnessAnnoyance, "Wet", record);
		}
	}
	
	public static void whenNearEntities(SanityRecord record, EntityPlayer player)
	{
		BlockPos origin = player.getPosition();
		Vec3i offset = new Vec3i(3, 3, 3);
		
		AxisAlignedBB box = new AxisAlignedBB(origin.subtract(offset), origin.add(offset));
		List<EntityCreature> entities = player.world.getEntitiesWithinAABB(EntityCreature.class, box);
		
		for(EntityCreature creature : entities)
		{
			if(creature instanceof EntityTameable)
			{
				EntityTameable pet = (EntityTameable)creature;
				// 4x bonus for tamed creatures. Having pets has it's perks :D
//				float bonus = pet.isTamed() ? (float)ModConfig.SANITY.tamedMobMultiplier : 1;
//				value += ModConfig.SANITY.friendlyMobBonus * bonus;
				if(pet.isTamed())
				{	
					float factor= (float)ModConfig.SANITY.tamedMobVincinity;
					if(TraitModule.instance.HasTrait(TRAITS.PETLOVER))
						factor*= (TraitModule.instance.TraitTier(TRAITS.PETLOVER)+2) / 2f;
					instance.addToTendency(factor, "Near pet", record);
				}
			}
			else if(creature instanceof EntityAnimal && TraitModule.instance.HasTrait(TRAITS.ANIMAL_LOVER))
			{
				instance.addToTendency((float)(ModConfig.SANITY.friendlyMobBonus * (TraitModule.instance.TraitTier(TRAITS.ANIMAL_LOVER) +2) /2f), "Near animals", record);
				TraitModule.instance.UsingTrait(TRAITS.ANIMAL_LOVER);
			}
			else if(creature instanceof EntityMob  && TraitModule.instance.HasTrait(TRAITS.AFRAID_MOBS))
				instance.addToTendency(-(float)(ModConfig.SANITY.hostileMobModifier * (TraitModule.instance.TraitTier(TRAITS.AFRAID_MOBS) + 2) / 2f), "Afraid of mobs", record);
		}
	}
	
	public static void whenRunning(SanityRecord record, EntityPlayer player)
	{
		if(player.isSprinting())
		{
			float factor= (float)ModConfig.SANITY.runningRelieve;
			if(TraitModule.instance.HasTrait(TRAITS.RUNNER))
			{
				factor*= (TraitModule.instance.TraitTier(TRAITS.RUNNER)+2)/2f;
				TraitModule.instance.UsingTrait(TRAITS.RUNNER);
			}
			instance.addToTendency(factor, "Running", record);
		}
	}
	
	public static void whenInWater(SanityRecord record, EntityPlayer player)
	{
		if(player.isInWater())
		{
			StatTracker tracker = player.getCapability(StatCapability.target, null);
			SimpleStatRecord heatRecord = tracker.getRecord(HeatModifier.instance);
			
			float heat= heatRecord.getValue();
			
			float coldThreshold= 50;
			float comfortableThreshold= 60;
			boolean isAquaphile= TraitModule.instance.HasTrait(TRAITS.AQUAPHILE);
			
			if(isAquaphile)
			{
				float offset= (TraitModule.instance.TraitTier(TRAITS.AQUAPHILE)+1)*2f;
				coldThreshold-= offset;
				comfortableThreshold-= offset;
			}
			
			
			if(heat < coldThreshold)
			{
				instance.addToTendency(-1f/heat, "Cold water", player);
				if(isAquaphile)
					TraitModule.instance.UsingTrait(TRAITS.AQUAPHILE, coldThreshold / Math.max(heat, 1));
			}
			else
			if(heat > comfortableThreshold)
				instance.addToTendency((heat-60f) / 100f, "Nice swim", player);
		}
	}
	
	public static void whenInSun(SanityRecord record, EntityPlayer player)
	{
		if(Util.isDaytime(player) && Util.isInSun(player) /* TODO && !Umbrella */)
		{
			float factor= (float)ModConfig.SANITY.sunMoodBoost;
			
			if(TraitModule.instance.HasTrait(TRAITS.UNDEAD))
			{
				int tier= TraitModule.instance.TraitTier(TRAITS.UNDEAD);
				factor*= -(tier + 2)/2f;
				if(Util.chance(0.1f))
					player.setFire(tier+2);
			}
			else
			if(TraitModule.instance.HasTrait(TRAITS.HELIOPHILE))
			{
				factor*= (TraitModule.instance.TraitTier(TRAITS.HELIOPHILE)+2) / 2f;
				TraitModule.instance.UsingTrait(TRAITS.HELIOPHILE);
			}
			
			instance.addToTendency(factor, "sun exposure", record);
		}
	}
	

	@SubscribeEvent
	public static void onPlayerWakeUp(PlayerWakeUpEvent event)
	{
		EntityPlayer player = event.getEntityPlayer();
		if(event.shouldSetSpawn()) // If the "lying in bed" was successful (the player actually fell asleep)
		{
			StatTracker stats = player.getCapability(StatCapability.target, null);
			SanityRecord sanity = stats.getRecord(SanityTendencyModifier.instance);
			sanity.resetSleep();
			instance.addToTendency(sanity.getValueRange().upperEndpoint() * (float)ModConfig.SANITY.sleepResoration, "sleep", player);
			SurvivalInc.proxy.net.sendToAll(new StatSyncMessage(player));
			player.getFoodStats().setFoodLevel(player.getFoodStats().getFoodLevel() - 8);
		}
	}

	/*
	@SubscribeEvent
	public static void onConsumeItem(LivingEntityUseItemEvent.Finish event)
	{		
		Entity ent = event.getEntity();	
		if(ent instanceof EntityPlayer)
		{
			try
			{
				// Try to get the modifier from the map (throws NPE when no such mapping exists)
				
				// TODO use default mod if there's no key in the map
				float mod = SanityTendencyModifier.instance.foodSanityMap.get(event.getItem().getItem());
				
				// Modify the sanity value
				EntityPlayer player = (EntityPlayer)ent;
				instance.addToTendency(mod, "food", player);

			}
			catch(NullPointerException exc)
			{
				// Food simply doesn't have any sanity mapping associated
			}
		}
	}
*/
	
	@SubscribeEvent
	public static void onTame(AnimalTameEvent event)
	{
		Entity ent = event.getEntity();

		if(ent instanceof EntityPlayer)
		{
			instance.addToTendency((float)ModConfig.SANITY.animalTameBoost, "taming", (EntityPlayer)ent);
		}
	}
	
	@SubscribeEvent
	public static void onKilled(LivingDeathEvent event)
	{
		EntityLivingBase target= event.getEntityLiving();

		DamageSource source= event.getSource();
		Entity sourceEntity= source.getImmediateSource();

		if(sourceEntity != null && sourceEntity instanceof EntityPlayer)
		{
			if(target instanceof EntityMob)
			{
					instance.addToTendency((float)ModConfig.SANITY.mobKill, "mob killed", (EntityPlayer)sourceEntity);				
			}

			if(TraitModule.instance.HasTrait(TRAITS.BLOODTHIRSTY))
			{
				instance.addToTendency((TraitModule.instance.TraitTier(TRAITS.BLOODTHIRSTY) + 1 ) /10f, "killed something", (EntityPlayer)sourceEntity);				
				TraitModule.instance.UsingTrait(TRAITS.BLOODTHIRSTY);
			}
		}
	}
}