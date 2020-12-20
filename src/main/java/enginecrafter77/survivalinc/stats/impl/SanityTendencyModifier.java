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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class SanityTendencyModifier implements StatProvider<SanityRecord> {
	private static final long serialVersionUID = 6707924203617912749L;
	
	public static final SanityTendencyModifier instance = new SanityTendencyModifier();
	
	public final EffectApplicator<SanityRecord> effects;
	public final Map<Item, Float> foodSanityMap;
	
	String currentReasonStr= "";
	float currentReasonVal= 0f;
	int reasonTicks= 0;
	const int reasonFlipTicks= 60;
	
	
	public SanityTendencyModifier()
	{
		this.effects = new EffectApplicator<SanityRecord>();
		this.foodSanityMap = new HashMap<Item, Float>();
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
		
		// Compile food value list
		for(String entry : ModConfig.SANITY.foodSanityMap)
		{
			int separator = entry.lastIndexOf(' ');
			Item target = Item.getByNameOrId(entry.substring(0, separator));
			Float value = Float.parseFloat(entry.substring(separator + 1));
			this.foodSanityMap.put(target, value);
		}
	}
	
	
	public void addToTendency(float value, String reason, EntityPlayer player)
	{
		StatTracker stats = player.getCapability(StatCapability.target, null);
		addToTendency(value, reason, stats.getRecord(SanityTendencyModifier.instance));

	}
	
	
	public void addToTendency(float value, String reason, SanityRecord record)
	{
		record.addToValue(value);
		
		if(value == 0f)
			return;
		
		if(reasonTicks > reasonFlipTicks && !reason.equals(currentReasonStr))
		{
			currentReasonStr= reason;
			currentReasonVal= value;
			reasonTicks= 0;
		}
	}
	
	@Override
	public void update(EntityPlayer target, StatRecord record)
	{
		if(target.isCreative() || target.isSpectator()) return;
		
		SanityRecord sanity = (SanityRecord)record;
		++sanity.ticksAwake;
		this.effects.apply(sanity, target);
		sanity.checkoutValueChange();
		
		reasonTicks++;
		if(reasonTicks > reasonFlipTicks)
		{
			currentReasonStr= "";
		}
	}
	
/*	
	public static void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		if(event.side == Side.SERVER)
		{
			daytime= event.
		}
	}
*/	

	@SubscribeEvent
	public static void RenderReason(RenderGameOverlayEvent.Post event)
	{
		if(instance.currentReasonStr == "")
			return;
		
		String str= instance.currentReasonStr;
		
		char c= instance.currentReasonVal > 0f ? '+' : '-';
		
		String postStr= ""+c;
		
		if(Math.abs(instance.currentReasonVal) > 0.1f)
			postStr+= ""+c;
		
		if(Math.abs(instance.currentReasonVal) > 0.3f)
			postStr+= ""+c;

		if(Math.abs(instance.currentReasonVal) > 1f)
			postStr+= ""+c;

		
		str+= " "+postStr;
		
		FontRenderer renderer= Minecraft.getMinecraft().fontRenderer;
		
		if(event.getType() == ElementType.TEXT)
			renderer.drawString(str, event.getResolution().getScaledWidth()-renderer.getStringWidth(str), event.getResolution().getScaledHeight()-renderer.FONT_HEIGHT, instance.currentReasonVal > 0 ? 0x00ff00 : 0xff0000,false);
	}
	
	@Override
	public ResourceLocation getStatID()
	{
		return new ResourceLocation(SurvivalInc.MOD_ID, "sanity_tendency");
	}

	@Override
	public SanityRecord createNewRecord()
	{
		return new SanityRecord();
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
		if(record.getTicksAwake() > ModConfig.SANITY.sleepDeprivationMin)
		{
//			record.addToValue(-(float)ModConfig.SANITY.sleepDeprivationDebuff * (record.getTicksAwake() - ModConfig.SANITY.sleepDeprivationMin) / (ModConfig.SANITY.sleepDeprivationMax - ModConfig.SANITY.sleepDeprivationMin));
			instance.addToTendency(-(float)ModConfig.SANITY.sleepDeprivationDebuff * (record.getTicksAwake() - ModConfig.SANITY.sleepDeprivationMin) / (ModConfig.SANITY.sleepDeprivationMax - ModConfig.SANITY.sleepDeprivationMin), "No sleep", record);
		}
	}
	

	
	public static void whenInDark(SanityRecord record, EntityPlayer player)
	{
		BlockPos position = new BlockPos(player.getPositionVector().add(0D, player.getEyeHeight(), 0D));
		int lightlevel = player.world.getLight(position);
		
		// If there is not enough light, steve/alex feels anxious
		if(lightlevel < ModConfig.SANITY.comfortLightLevel)
		{
			float darknesslevel = (float)(ModConfig.SANITY.comfortLightLevel - lightlevel) / (float)ModConfig.SANITY.comfortLightLevel;
			instance.addToTendency((float)ModConfig.SANITY.darkSpookFactorBase * -darknesslevel, "Darkness", record);
		}
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
		
//		float value = record.getValue();
		for(EntityCreature creature : entities)
		{
			if(creature instanceof EntityTameable)
			{
				EntityTameable pet = (EntityTameable)creature;
				// 4x bonus for tamed creatures. Having pets has it's perks :D
//				float bonus = pet.isTamed() ? (float)ModConfig.SANITY.tamedMobMultiplier : 1;
//				value += ModConfig.SANITY.friendlyMobBonus * bonus;
				if(pet.isTamed())
					instance.addToTendency((float)ModConfig.SANITY.tamedMobVincinity, "Near pet", record);
			}
/* 
- reserve for ANIMAL_LOVER 
			else if(creature instanceof EntityAnimal)
				value += ModConfig.SANITY.friendlyMobBonus;
- reserve for AFRAID				
			else if(creature instanceof EntityMob)
				value -= ModConfig.SANITY.hostileMobModifier;
*/		}
//		record.setValue(value);
	}
	
	public static void whenRunning(SanityRecord record, EntityPlayer player)
	{
		if(player.isSprinting())
			instance.addToTendency((float)ModConfig.SANITY.runningRelieve, "running", record);
	}
	
	public static void whenInSun(SanityRecord record, EntityPlayer player)
	{
		boolean day= player.world.getWorldTime() % 24000 < 12000;
		
		if(day && player.world.canBlockSeeSky(player.getPosition()))
			instance.addToTendency((float)ModConfig.SANITY.sunMoodBoost, "sun exposure", record);
	}
	

	@SubscribeEvent
	public static void onPlayerWakeUp(PlayerWakeUpEvent event)
	{
		EntityPlayer player = event.getEntityPlayer();
		if(event.shouldSetSpawn()) // If the "lying in bed" was successful (the player actually fell asleep)
		{
			StatTracker stats = player.getCapability(StatCapability.target, null);
			SanityRecord sanity = stats.getRecord(SanityTendencyModifier.instance);
//			sanity.addToValue(sanity.getValueRange().upperEndpoint() * (float)ModConfig.SANITY.sleepResoration);
			sanity.resetSleep();
			instance.addToTendency(sanity.getValueRange().upperEndpoint() * (float)ModConfig.SANITY.sleepResoration, "sleep", player);
			SurvivalInc.proxy.net.sendToAll(new StatSyncMessage(player));
			player.getFoodStats().setFoodLevel(player.getFoodStats().getFoodLevel() - 8);
		}
	}
	
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
		
		if(target instanceof EntityMob)
		{
			DamageSource source= event.getSource();
			Entity sourceEntity= source.getImmediateSource();
			if(sourceEntity != null && sourceEntity instanceof EntityPlayer)
			{
				instance.addToTendency((float)ModConfig.SANITY.mobKill, "mob killed", (EntityPlayer)sourceEntity);				
			}
		}
	}
}