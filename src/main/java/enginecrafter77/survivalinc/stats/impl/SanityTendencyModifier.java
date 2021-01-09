package enginecrafter77.survivalinc.stats.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
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
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.google.common.collect.Range;

import enginecrafter77.survivalinc.ModItems;
import enginecrafter77.survivalinc.SurvivalInc;
import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.debug.LightDebugCommand;
import enginecrafter77.survivalinc.debug.SanityDebugCommand;
import enginecrafter77.survivalinc.net.EntityItemUpdateMessage;
import enginecrafter77.survivalinc.net.SanityReasonMessage;
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
import enginecrafter77.survivalinc.strugglecraft.DeathCounter;
import enginecrafter77.survivalinc.strugglecraft.MyWorldSavedData;
import enginecrafter77.survivalinc.strugglecraft.TraitModule;
import enginecrafter77.survivalinc.strugglecraft.TraitModule.TRAITS;
import enginecrafter77.survivalinc.strugglecraft.WorshipPlace;
import enginecrafter77.survivalinc.util.Util;

public class SanityTendencyModifier implements StatProvider<SanityRecord>, IMessageHandler<SanityReasonMessage, IMessage> {
	private static final long serialVersionUID = 6707924203617912749L;
	
	public static final SanityTendencyModifier instance = new SanityTendencyModifier();
	
	public final EffectApplicator<SanityRecord> effects;
	
	class ReasonEntry
	{
		public String name;
		public float value;
		public boolean oneTime;
		
		public ReasonEntry(String n, float v, boolean b) {	name= n; value= v; oneTime= b;}
	}

	List<ReasonEntry> reasons;
	
	
	String currentReasonStr= "";
	boolean isCurrentReasonOneTimer= false;
	int reasonTicks= 0;
	int reasonFlipTicks= 60;

	public SanityTendencyModifier()
	{
		this.effects = new EffectApplicator<SanityRecord>();
//		reasons= new ArrayList<ReasonEntry>();
		reasons= Collections.synchronizedList(new ArrayList<ReasonEntry>());
	}
	
	public void init()
	{
		MinecraftForge.EVENT_BUS.register(SanityTendencyModifier.class);

		if(ModConfig.WETNESS.enabled) this.effects.add(SanityTendencyModifier::whenWet).addFilter(FunctionalEffectFilter.byPlayer(EntityPlayer::isInWater).invert());
		this.effects.add(SanityTendencyModifier::whenSleeping);
		this.effects.add(SanityTendencyModifier::whenInDark).addFilter(HydrationModifier.isOutsideOverworld.invert());
		this.effects.add(SanityTendencyModifier::whenNearEntities);
		this.effects.add(SanityTendencyModifier::whenRunning);
		this.effects.add(SanityTendencyModifier::whenInSun);
		this.effects.add(SanityTendencyModifier::whenInWater);
		this.effects.add(SanityTendencyModifier::whenNight);
		this.effects.add(SanityTendencyModifier::whenWearing);
	
	}
	

	
	@SubscribeEvent
	public static void onSpawn(EntityJoinWorldEvent event)
	{
		Entity ent= event.getEntity();

		
		// if isDead check is omitted this will trigger repeatedly while dead (before respawn)
		if(ent instanceof EntityPlayer && !ent.isDead)
		{
			EntityPlayer player= (EntityPlayer) ent;

			System.out.println(player.getName()+" spawned");

			
//			if(!Util.thisClientOnly(player))
			if(event.getWorld().isRemote)
				return;

			System.out.println("... on server");

			
			if(instance.getTendency(player) != 0f)
			{
				System.out.println(player.getName()+" data loaded ("+instance.getTendency(player)+")");
				player.sendMessage(new TextComponentString("Player data loaded ("+instance.getTendency(player)+")"));
				return;
			}
			
			if(DeathCounter.getDeaths(player) == 0 && event.getWorld().getScoreboard().getPlayersTeam(player.getName()) == null)
			{
				event.getWorld().getScoreboard().addPlayerToTeam(player.getName(), "Team");
				// start with ultra haste-push
				System.out.println(player.getName()+" first login");
				player.sendMessage(new TextComponentString("First login"));
				instance.setTendency(100f, player);
			}
			else
			{
				System.out.println(player.getName()+" respawn");
				player.sendMessage(new TextComponentString("Respawn"));
				instance.setTendency(0f, player);
			}
			
			// no effect
			StatCapability.updateNextTick= true;
			
		}
	}
	
	public void addToTendencyOneTime(float value, String reason, EntityPlayer player)
	{
		addToTendency(value, reason, player, true, true);
	}
	
	public void addToTendency(float value, String reason, EntityPlayer player)
	{
		addToTendency(value, reason, player, false);
	}

	
	public void addToTendency(float value, String reason, EntityPlayer player, boolean forceAddReason)
	{
		addToTendency(value, reason, player, forceAddReason, false);
	}

	
	public void addToTendency(float value, String reason, EntityPlayer player, boolean forceAddReason, boolean oneTime)
	{
		StatTracker stats = player.getCapability(StatCapability.target, null);
		addToTendency(value, reason, stats.getRecord(SanityTendencyModifier.instance), forceAddReason, oneTime, false);

	}
	

	public void addToTendency(float value, String reason, SanityRecord record)
	{
		addToTendency(value, reason, record, false);
	}

	
	public void addToTendency(float value, String reason, SanityRecord record, boolean forceAddReason)
	{
		addToTendency(value, reason, record, forceAddReason, false, false);
	}


	public void addToTendencyServer(float value, String reason, EntityPlayer player)
	{
		addToTendencyServer(value, reason, player, true);
	}
	
	public void addToTendencyServer(float value, String reason, EntityPlayer player, boolean forceAddReason)
	{
		StatTracker stats = player.getCapability(StatCapability.target, null);
		addToTendency(value, reason, stats.getRecord(SanityTendencyModifier.instance), forceAddReason, true, true);
		
		if(player.world.isRemote)
			System.out.println("DEBUG: Error tried to send ReasonMessage from Client");
		else
		{
			SanityReasonMessage msg= new SanityReasonMessage(value, reason, forceAddReason);
			System.out.println("DEBUG: Send msg: "+msg);
			SurvivalInc.proxy.net.sendTo(msg, (EntityPlayerMP) player);
		}

	}
	
	public void addToTendency(float value, String reason, SanityRecord record, boolean forceAddReason, boolean oneTime, boolean serverSide)
	{
		record.addToValue(value);
		
		if(value == 0f || reason == "" || serverSide)
			return;

		addReason(value, reason, forceAddReason, oneTime);
		
	}
	

	public void addReason(float value, String reason, boolean forceAddReason, boolean oneTime)
	{
		if(reasons.size() < 3 || forceAddReason)
		{
			boolean alreadyUsed= false;
			if(!oneTime)
				for(ReasonEntry entry : reasons)
					if(entry.name == reason)
					{
						alreadyUsed= true;
						break;
					}
			
			if(!alreadyUsed)
			{
//				if(oneTime)
//					System.out.println("DEBUG: Adding reason "+reason);
				if(oneTime)
				{
					int i= 0;
					while(i < reasons.size() && reasons.get(i).oneTime)
						i++;
					
					reasons.add(i, new ReasonEntry(reason, value, oneTime));
					
					if(!isCurrentReasonOneTimer)
						reasonTicks= reasonFlipTicks;

				}
				else
					reasons.add(new ReasonEntry(reason, value, oneTime));
				
/*				if(oneTime)
				{
					System.out.println("DEBUG: new reason list");
					for(ReasonEntry e : reasons)
						System.out.println(e.toString());
				}
*/				
			}
		}
	}
	
	public void setTendency(float value, EntityPlayer player)
	{
		StatTracker stats = player.getCapability(StatCapability.target, null);
		stats.getRecord(SanityTendencyModifier.instance).setValue(value);
	}

	public float getTendency(EntityPlayer player)
	{
		StatTracker stats = player.getCapability(StatCapability.target, null);
		return stats.getRecord(SanityTendencyModifier.instance).getValue();
	}

	
	@Override
	public void update(EntityPlayer target, StatRecord record)
	{
		if(target.isCreative() || target.isSpectator() || target.isDead/* || target.world.isRemote*/) return;
		
		if(target.world.isRemote && !Util.thisClientOnly(target))
			return;
		
		SanityRecord sanity = (SanityRecord)record;
		++sanity.ticksAwake;
		this.effects.apply(sanity, target);
	
		// base sanity drain
		if(!target.isPlayerSleeping())
			addToTendency((float)(-Math.pow(target.world.getTotalWorldTime() / 24000, 0.85) * 0.0001), "", target);
		
		sanity.checkoutValueChange();
		

		if(Util.thisClientOnly(target))
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
				
				float factor= reasons.get(0).oneTime ? 50f : 1f;
				
				if(Math.abs(value) > 0.01f * factor)
					postStr+= ""+c;
				
				if(Math.abs(value) > 0.03f * factor)
					postStr+= ""+c;

				if(Math.abs(value) > 0.1f * factor)
					postStr+= ""+c;

				currentReasonStr+= " "+postStr;
				
				isCurrentReasonOneTimer= reasons.get(0).oneTime;
				
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
		{
			
			renderer.drawString(str, event.getResolution().getScaledWidth()-renderer.getStringWidth(str), event.getResolution().getScaledHeight()-renderer.FONT_HEIGHT, str.contains("+") ? 0x00ff00 : 0xff0000,false);
			if(Minecraft.getMinecraft().player.isPlayerSleeping())
			{
				int foodPct= (int)Math.floor(Minecraft.getMinecraft().player.getFoodStats().getFoodLevel() / 20f * 100f);
				String text= "Food: "+foodPct+"%";

				renderer.drawString(text, event.getResolution().getScaledWidth()-renderer.getStringWidth(text), event.getResolution().getScaledHeight()-renderer.FONT_HEIGHT*3, 0xFFFFFF, false);//true);
		
			}
		}
		
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
	

	
	public static void whenInDark(SanityRecord record, EntityPlayer player)
	{
		BlockPos position = new BlockPos(player.getPositionVector().add(0D, player.getEyeHeight(), 0D));
		int lightlevel = player.world.getLight(position);
		
		// If there is not enough light, steve/alex feels anxious
		int comfortLightLevel= ModConfig.SANITY.comfortLightLevel;

		boolean isCourageous= TraitModule.instance.HasTrait(player,TRAITS.COURAGEOUS);
		
		if(isCourageous)
			comfortLightLevel-= 2 + TraitModule.instance.TraitTier(player,TRAITS.COURAGEOUS);
		
		if(lightlevel < comfortLightLevel)
		{
			float darknesslevel = (float)(comfortLightLevel - lightlevel) / (float)comfortLightLevel;
			instance.addToTendency((float)ModConfig.SANITY.darkSpookFactorBase * -darknesslevel, "Darkness", record);
			if(isCourageous)
				TraitModule.instance.UsingTrait(player,TRAITS.COURAGEOUS);
		}
//		else
//		if(isCourageous && lightlevel < ModConfig.SANITY.comfortLightLevel)
//			TraitModule.instance.UsingTrait(player,TRAITS.COURAGEOUS);
			
	}
	
	public static void whenWet(SanityRecord record, EntityPlayer player)
	{
		if(!player.world.isRemote)
			return;
		
		if(WetnessModifier.instance.outOfWaterDelay > 0)
			return;
		
		float boundary = (float)ModConfig.SANITY.wetnessAnnoyanceThreshold;
		StatTracker stats = player.getCapability(StatCapability.target, null);
		SimpleStatRecord wetness = stats.getRecord(WetnessModifier.instance);		
		if(wetness.getNormalizedValue() > boundary)
		{
			float playerTemp= HeatModifier.instance.getPlayerTemperature(player);
			if(playerTemp < 60f)
			{
				float temperatureFactor= Math.max(1f, 40f/Math.max(1f, playerTemp));
				instance.addToTendency(temperatureFactor*((wetness.getNormalizedValue() - boundary) / (1F - boundary)) * -(float)ModConfig.SANITY.maxWetnessAnnoyance, "Wet", record);
			}
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
					float factor= (float)ModConfig.SANITY.tamedMobProximity;
					if(TraitModule.instance.HasTrait(player,TRAITS.PETLOVER))
						factor*= (TraitModule.instance.TraitTier(player,TRAITS.PETLOVER)+2) / 2f;
					instance.addToTendency(factor, "Near pet", record);
				}
			}
			else if(creature instanceof EntityAnimal && TraitModule.instance.HasTrait(player,TRAITS.ANIMAL_LOVER))
			{
				instance.addToTendency((float)(ModConfig.SANITY.friendlyMobBonus * (TraitModule.instance.TraitTier(player,TRAITS.ANIMAL_LOVER) +2) /2f), "Near animals", record);
				TraitModule.instance.UsingTrait(player,TRAITS.ANIMAL_LOVER);
			}
			else if(creature instanceof EntityMob  && TraitModule.instance.HasTrait(player,TRAITS.AFRAID_MOBS))
				instance.addToTendency(-(float)(ModConfig.SANITY.hostileMobModifier * (TraitModule.instance.TraitTier(player,TRAITS.AFRAID_MOBS) + 2) / 2f), "Afraid of mobs", record);
		}
	}
	
	public static void whenRunning(SanityRecord record, EntityPlayer player)
	{
		if(player.isSprinting() && player.getActivePotionEffect(MobEffects.SLOWNESS) == null)
		{
			float factor= (float)ModConfig.SANITY.runningRelieve;
			if(TraitModule.instance.HasTrait(player,TRAITS.RUNNER))
			{
				factor*= (TraitModule.instance.TraitTier(player,TRAITS.RUNNER)+2)/2f;
				TraitModule.instance.UsingTrait(player,TRAITS.RUNNER);
			}
			if(TraitModule.instance.HasTrait(player,TRAITS.ACTIVE))
			{
				factor*= (TraitModule.instance.TraitTier(player,TRAITS.ACTIVE)+2)/4f;
				TraitModule.instance.UsingTrait(player,TRAITS.ACTIVE);
			}
			
			instance.addToTendency(factor, "Running", record);
		}
	}
	
	public static void whenInWater(SanityRecord record, EntityPlayer player)
	{
		
		if(Util.isSwimming(player)/*player.isInWater()*/)
		{			
			StatTracker tracker = player.getCapability(StatCapability.target, null);
			SimpleStatRecord heatRecord = tracker.getRecord(HeatModifier.instance);
			
			float heat= heatRecord.getValue();
			
			float coldThreshold= 50;
			float comfortableThreshold= 60;
			boolean isAquaphile= TraitModule.instance.HasTrait(player,TRAITS.AQUAPHILE);
			
			if(isAquaphile)
			{
				float offset= (TraitModule.instance.TraitTier(player,TRAITS.AQUAPHILE)+1)*2f;
				coldThreshold-= offset;
				comfortableThreshold-= offset;
			}
			
			
			if(heat < coldThreshold)
			{
				instance.addToTendency(-1f/Math.max(1, heat), "Cold water", player);
				if(isAquaphile)
					TraitModule.instance.UsingTrait(player,TRAITS.AQUAPHILE, coldThreshold / Math.max(heat, 1));
			}
			else
			if(heat > comfortableThreshold)
				instance.addToTendency((float)((heat-comfortableThreshold) * ModConfig.SANITY.swimFun) , "Nice swim", player);
		}
	}
	
	public static void whenInSun(SanityRecord record, EntityPlayer player)
	{
		if(Util.isInSun(player))
		{
			float factor= (float)ModConfig.SANITY.sunMoodBoost;
			
			if(TraitModule.instance.HasTrait(player,TRAITS.UNDEAD))
			{
				int tier= TraitModule.instance.TraitTier(player,TRAITS.UNDEAD);
				factor*= -(tier + 2)/2f;
				if(Util.chance(0.1f))
					player.setFire(tier+2);
			}
			else
			if(TraitModule.instance.HasTrait(player,TRAITS.HELIOPHILE))
			{
				factor*= (TraitModule.instance.TraitTier(player,TRAITS.HELIOPHILE)+2) / 2f;
				TraitModule.instance.UsingTrait(player,TRAITS.HELIOPHILE);
			}
			
			instance.addToTendency(factor, "Sun", record);
		}
	}

	
	public static void whenSleeping(SanityRecord record, EntityPlayer player)
	{
		if(player.isPlayerSleeping())
		{
			float factor= 1f;
			
			if(TraitModule.instance.HasTrait(player,TRAITS.ACTIVE))
			{
				factor-= (TraitModule.instance.TraitTier(player,TRAITS.ACTIVE) + 1) / 5f;
			}
				
			instance.addToTendency((float)ModConfig.SANITY.sleepRestoration*factor, "Sleep", record);
		}
	}
	
	public static void whenNight(SanityRecord record, EntityPlayer player)
	{
		if(!Util.isDaytime(player) && !player.isPlayerSleeping())
		{
			if(TraitModule.instance.HasTrait(player,TRAITS.SLEEPY))
			{
				instance.addToTendency(-0.001f, "Sleepy", player);
			}
			
		}
	}

	public static void whenWearing(SanityRecord record, EntityPlayer player)
	{
		if(player.isPlayerSleeping())
			return;
		
		ItemStack head= player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		
		if(head != null)
		{
			if(head.getItem() == ModItems.HEADBAND.getItem())
				instance.addToTendency(0.0002f, "Headband", player);
			else
				if(head.getItem() == ModItems.SUPERIOR_HEADBAND.getItem())
					instance.addToTendency(0.002f, "Headband", player);
		}
	}

	
	@SubscribeEvent
	public static void onPlayerWakeUp(PlayerWakeUpEvent event)
	{
/*		EntityPlayer player = event.getEntityPlayer();
		if(event.shouldSetSpawn()) // If the "lying in bed" was successful (the player actually fell asleep)
		{
			StatTracker stats = player.getCapability(StatCapability.target, null);
			SanityRecord sanity = stats.getRecord(SanityTendencyModifier.instance);
			sanity.resetSleep();
			instance.addToTendency(sanity.getValueRange().upperEndpoint() * (float)ModConfig.SANITY.sleepResoration, "sleep", player);
			SurvivalInc.proxy.net.sendToAll(new StatSyncMessage(player));
			player.getFoodStats().setFoodLevel(player.getFoodStats().getFoodLevel() - 8);
		}
*/	}

	@SubscribeEvent
	public static void onBlockBreak(BreakEvent event)
	{
		if(TraitModule.instance.HasTrait(event.getPlayer(),TRAITS.WORKAHOLIC))
		{
			float hardness= event.getWorld().getBlockState(event.getPos()).getBlockHardness(event.getWorld(), event.getPos());
			if(hardness > 0f)
			{
				TraitModule.instance.UsingTrait(event.getPlayer(), TRAITS.WORKAHOLIC, hardness);
				// TODO is server correct?
				SanityTendencyModifier.instance.addToTendencyServer(0.01f*(TraitModule.instance.TraitTier(event.getPlayer(), TRAITS.WORKAHOLIC) + 1), "Workaholic", event.getPlayer(), true);
			}
		}
	}


	
	@SubscribeEvent
	public static void onTame(AnimalTameEvent event)
	{
		Entity ent = event.getEntity();

		if(ent instanceof EntityPlayer)
		{
//			instance.addToTendencyOneTime((float)ModConfig.SANITY.animalTameBoost, "taming", (EntityPlayer)ent);
			instance.addToTendencyServer((float)ModConfig.SANITY.animalTameBoost, "taming", (EntityPlayer)ent);
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
			EntityPlayer player= (EntityPlayer) sourceEntity;
			
//			if(!Util.thisClientOnly(player))
//				return;
			
			if(target instanceof EntityMob)
			{
				System.out.println(player.getName() + " killed mob, remote: "+player.world.isRemote);
				
				if(TraitModule.instance.HasTrait(player,TRAITS.PACIFIST))
					instance.addToTendencyServer(-(float)ModConfig.SANITY.mobKill, "pacifist", player);				
				else
					instance.addToTendencyServer((float)ModConfig.SANITY.mobKill, "mob killed", player);				
			}

			if(TraitModule.instance.HasTrait(player,TRAITS.BLOODTHIRSTY))
			{
				instance.addToTendencyServer((TraitModule.instance.TraitTier(player,TRAITS.BLOODTHIRSTY) + 1 ) / 2f, "killed something", player);				
				TraitModule.instance.UsingTrait(player,TRAITS.BLOODTHIRSTY);
			}
			
			if(target instanceof EntityAnimal)
			{
				if(TraitModule.instance.HasTrait(player,TRAITS.ANIMAL_LOVER))
					instance.addToTendencyServer(-1.5f, "Poor animal", player);
			}
		}
	}
	
	@SubscribeEvent
	public static void onDamaged(LivingDamageEvent event)
	{
		Entity entity= event.getEntity();
		
		if(entity != null && entity instanceof EntityPlayer)
		{
			EntityPlayer player= (EntityPlayer) entity;
	
			
			boolean isMasochist= TraitModule.instance.HasTrait(player,TRAITS.MASOCHIST);
			
			float factor= 1;
			if(isMasochist)
				factor*= -(TraitModule.instance.TraitTier(player, TRAITS.MASOCHIST) + 1);

			boolean skipSanity= false;
			if(TraitModule.instance.HasTrait(player,TRAITS.HARD_SHELL))
			{
				if(Util.chance((TraitModule.instance.TraitTier(player,TRAITS.HARD_SHELL) + 1) * 20f))
				{
					skipSanity= true;
					TraitModule.instance.UsingTrait(player,TRAITS.HARD_SHELL);
				}
			}
			
			if(!skipSanity)
//				instance.addToTendencyOneTime((float)(ModConfig.SANITY.hurt * factor), "Ouch", player);
				instance.addToTendencyServer((float)(ModConfig.SANITY.hurt * factor), "Ouch", player);
			
			if(isMasochist)
				TraitModule.instance.UsingTrait(player,TRAITS.MASOCHIST);
			
			if(TraitModule.instance.HasTrait(player,TRAITS.FRAGILE))
				event.setAmount(event.getAmount()* (1f + (TraitModule.instance.TraitTier(player,TRAITS.FRAGILE) + 1)/2f));
		}
	}
	
	@SubscribeEvent
	public static void onWorldTick(TickEvent.WorldTickEvent event)
	{
		if(event.side == Side.CLIENT)
			return;
		
		if(event.phase == Phase.END)
			return;
		
		if(Util.chance(event.world, 0.05f))  
		{
			 PlayerList players= event.world.getMinecraftServer().getPlayerList();

			 List<EntityPlayerMP> playerList= new ArrayList<EntityPlayerMP>();
			 List<EntityPlayerMP> removeList= new ArrayList<EntityPlayerMP>();
			 
			playerList.addAll(players.getPlayers());
			 
			System.out.println("DEBUG: Searching "+MyWorldSavedData.get(event.world).getWorshipPlaces().size()+" worship places");
			 
			for(WorshipPlace place : MyWorldSavedData.get(event.world).getWorshipPlaces())
			{
				for(EntityPlayerMP player : playerList)
				{
					if(player.isPlayerSleeping())
						continue;
					if(Util.distance(player.getPosition(), place.getPosition()) < place.getValue())
					{
						SanityTendencyModifier.instance.addToTendencyServer(5f, "Worship", player);
						// remove player from any further worship checks
						removeList.add(player);
					}
				}
				
				while(removeList.size()>0)
				{
					playerList.remove(removeList.get(0));
					removeList.remove(0);
				}
				
			}
		}
		
	}

	@Override
	public IMessage onMessage(SanityReasonMessage message, MessageContext ctx) {
		if(ctx.side == Side.SERVER)
		{
			System.out.println("DEBUG: Received Message ERROR: server-side");
			throw new RuntimeException("SanityReasonMessage is designed to be processed on client!");
		}
		
		SurvivalInc.proxy.AddReasonToClient(message.value, message.reason, message.forceAdd);
		
		System.out.println("DEBUG: Received msg: "+message.toString());
		
		return null;
	
	}
	
	
}