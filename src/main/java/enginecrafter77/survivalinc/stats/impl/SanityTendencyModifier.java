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
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
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
import enginecrafter77.survivalinc.net.SanityOverviewMessage;
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
import enginecrafter77.survivalinc.strugglecraft.WorshipPlace.WORSHIP_PLACE_TYPE;
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

	public Map<String, Float> overview;
	public long overviewTimestamp= 0;
	
	public SanityTendencyModifier()
	{
		this.effects = new EffectApplicator<SanityRecord>();
//		reasons= new ArrayList<ReasonEntry>();
		reasons= Collections.synchronizedList(new ArrayList<ReasonEntry>());
		overview= Collections.synchronizedMap(new HashMap<String, Float>());
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
		this.effects.add(SanityTendencyModifier::whenDepressed);
		this.effects.add(SanityTendencyModifier::whenEducated);
	
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

			instance.ClearOverviewData(event.getWorld());
			
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
		if(!overview.containsKey(reason))
			overview.put(reason, value);
		else
			overview.put(reason, overview.get(reason)+value);
		
		if(reason == "Drain" || reason == "Depressed")
			return;
		
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
		if(target.isDead)
			overviewTimestamp= 0;
		
		if(target.isCreative() || target.isSpectator() || target.isDead/* || target.world.isRemote*/) return;
		
		if(overviewTimestamp == 0)
			overviewTimestamp= target.world.getTotalWorldTime();
		
		if(target.world.isRemote && !Util.thisClientOnly(target))
			return;
		
		SanityRecord sanity = (SanityRecord)record;
		++sanity.ticksAwake;
		this.effects.apply(sanity, target);
	
		// base sanity drain
		if(!target.isPlayerSleeping() || HeatModifier.instance.getPlayerTemperature(target) < HeatModifier.COLD_THRESHOLD - HeatModifier.getColdResistance(target))
			addToTendency((float)(-Math.pow(target.world.getTotalWorldTime() / 24000, 0.55) * 0.0001), "Drain", target);
		
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
	public static void renderOverlay(RenderGameOverlayEvent.Post event)
	{
		if(instance.currentReasonStr == "")
			return;
		
		
		String str= instance.currentReasonStr;
		
	
		FontRenderer renderer= Minecraft.getMinecraft().fontRenderer;
		
		if(event.getType() == ElementType.TEXT)
		{
			// reasons
			renderer.drawString(str, event.getResolution().getScaledWidth()-renderer.getStringWidth(str), event.getResolution().getScaledHeight()-renderer.FONT_HEIGHT, str.contains("+") ? 0x00ff00 : 0xff0000,false);
			

			
			
//			EntityPlayer player= Minecraft.getMinecraft().player;
	
				
//			if(player.isPlayerSleeping() || player.isRiding())
				if(Minecraft.getMinecraft().player.isPlayerSleeping() || Minecraft.getMinecraft().player.isRiding())
			{
				// food pct
				int foodPct= (int)Math.floor(Minecraft.getMinecraft().player.getFoodStats().getFoodLevel() / 20f * 100f);
				String text= "Food: "+foodPct+"%";
				int color= foodPct > 50f ? 0xFFFFFF : 0xFF0000;
				renderer.drawString(text, event.getResolution().getScaledWidth()-renderer.getStringWidth(text), event.getResolution().getScaledHeight()-renderer.FONT_HEIGHT*3, color, false);//true);
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
		if(player.isPlayerSleeping())
			return;
		
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
		if(!player.world.isRemote)
			return;
		
		if(player.isSprinting() && player.getActivePotionEffect(MobEffects.SLOWNESS) == null && !player.isInWater())
		{
			float factor= (float)ModConfig.SANITY.runningRelieve;
			if(TraitModule.instance.HasTrait(player,TRAITS.RUNNER))
			{
				factor*= 1f + (TraitModule.instance.TraitTier(player,TRAITS.RUNNER)+1)/2f;
				TraitModule.instance.UsingTrait(player,TRAITS.RUNNER);
			}
			if(TraitModule.instance.HasTrait(player,TRAITS.ACTIVE))
			{
				factor*= 1f+(TraitModule.instance.TraitTier(player,TRAITS.ACTIVE)+1)/4f;
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
		if(player.isPlayerSleeping() && HeatModifier.instance.getPlayerTemperature(player) >= HeatModifier.COLD_THRESHOLD - HeatModifier.getColdResistance(player))
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
		ItemStack body= player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		ItemStack legs= player.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
		ItemStack feet= player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
		
		if(head != null)
		{
			if(head.getItem() == ModItems.HEADBAND.getItem())
				instance.addToTendency(0.0002f, "Headband", player);
			else
				if(head.getItem() == ModItems.SUPERIOR_HEADBAND.getItem())
					instance.addToTendency(0.002f, "Headband", player);
				else
					if(head.getItem() == Items.GOLDEN_HELMET)
						instance.addToTendency(0.005f, "Wearing Gold", player);
		}
		
		if(body != null)
		{
			if(body.getItem() == Items.GOLDEN_CHESTPLATE)
				instance.addToTendency(0.01f, "Wearing Gold", player);
		}

		if(legs != null)
		{
			if(legs.getItem() == Items.GOLDEN_LEGGINGS)
				instance.addToTendency(0.008f, "Wearing Gold", player);
		}

		if(feet != null)
		{
			if(feet.getItem() == Items.GOLDEN_BOOTS)
				instance.addToTendency(0.003f, "Wearing Gold", player);
		}

	}

	public static void whenDepressed(SanityRecord record, EntityPlayer player)
	{
		if(!player.isPlayerSleeping())
			if(TraitModule.instance.HasTrait(player, TRAITS.DEPRESSED))
				instance.addToTendency(-0.0001f * (TraitModule.instance.TraitTier(player, TRAITS.DEPRESSED) + 1), "Depressed", player);

	}
	
	public static void whenEducated(SanityRecord record, EntityPlayer player)
	{
		
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
				SanityTendencyModifier.instance.addToTendencyServer(0.05f*(TraitModule.instance.TraitTier(event.getPlayer(), TRAITS.WORKAHOLIC) + 1), "Workaholic", event.getPlayer(), true);
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
				instance.addToTendencyServer((TraitModule.instance.TraitTier(player,TRAITS.BLOODTHIRSTY) + 1 ) * 1.5f, "killed something", player);				
				TraitModule.instance.UsingTrait(player,TRAITS.BLOODTHIRSTY);
			}
			
			if(target instanceof EntityAnimal)
			{
				if(TraitModule.instance.HasTrait(player,TRAITS.ANIMAL_LOVER))
					instance.addToTendencyServer(-1.5f * (TraitModule.instance.TraitTier(player,TRAITS.ANIMAL_LOVER) + 1), "Poor animal", player);
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
		
		
		// Check worship place proximity
		if(Util.chance(event.world, 0.05f))  
		{
			 PlayerList players= event.world.getMinecraftServer().getPlayerList();

			 // STONE CIRCLES
			 
			 List<EntityPlayerMP> playerList= new ArrayList<EntityPlayerMP>();
			 List<EntityPlayerMP> removeList= new ArrayList<EntityPlayerMP>();
			 
			playerList.addAll(players.getPlayers());
			 
			System.out.println("DEBUG: Searching "+MyWorldSavedData.get(event.world).getWorshipPlaces().size()+" worship places");
			 
			for(WorshipPlace place : MyWorldSavedData.get(event.world).getWorshipPlaces())
			{
				if(place.type != WORSHIP_PLACE_TYPE.STONE_CIRCLE)
					continue;
				
				for(EntityPlayerMP player : playerList)
				{
					if(player.isPlayerSleeping() || player.isDead)
						continue;
					
					if(Util.distance(player, place.getPosition()) < place.getValue())
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

		
			// MONUMENTS

			 playerList= new ArrayList<EntityPlayerMP>();
			 removeList= new ArrayList<EntityPlayerMP>();
			 
			playerList.addAll(players.getPlayers());
			 
			System.out.println("DEBUG: Searching "+MyWorldSavedData.get(event.world).getWorshipPlaces().size()+" worship places");
			 
			for(WorshipPlace place : MyWorldSavedData.get(event.world).getWorshipPlaces())
			{
				for(EntityPlayerMP player : playerList)
				{
					if(place.type != WORSHIP_PLACE_TYPE.MONUMENT)
						continue;

					if(player.isPlayerSleeping()  || player.isDead)
						continue;
					
					BlockPos pos= new BlockPos(place.getPosition());
					pos.add(new Vec3i(0,-place.getHeight(),0));
	
					// add bonus for being near base
/*					if(Util.distance(player,  pos) < 10)
					{
						SanityTendencyModifier.instance.addToTendencyServer((float)Math.min(30f, Math.pow(place.getValue(), 0.3f)), "Monument", player);
						// remove player from any further worship checks
						removeList.add(player);
					}
					else
*/					{

						if(player.getDistanceSq(place.getPosition()) < place.getValue() * 2 || player.getDistanceSq(place.getPosition().down(place.getHeight())) < place.getValue() * 2)
						{
							RayTraceResult result= event.world.rayTraceBlocks(new Vec3d(player.getPosition().up()), new Vec3d(place.getPosition()), false, false, false);
							
							if(result != null && result.getBlockPos() == place.getPosition())
							{
								SanityTendencyModifier.instance.addToTendencyServer(30f, "Monument", player);
								removeList.add(player);
							}
						}
					}
				}
				
				while(removeList.size()>0)
				{
					playerList.remove(removeList.get(0));
					removeList.remove(0);
				}
				
			}

		
		
		}


		// Check mob spawner proximity
		if(Util.chance(event.world, 0.5f))
		{
			for(EntityPlayer player : event.world.getMinecraftServer().getPlayerList().getPlayers())
			{
				int cx= player.chunkCoordX;
				int cz= player.chunkCoordZ;
				
				cx+= Util.rnd(event.world, -4, 4);
				cz+= Util.rnd(event.world, -4, 4);
				
				Chunk chunk= event.world.getChunk(cx, cz);
				
				if(chunk == null || !chunk.isLoaded() || !chunk.isPopulated())
					continue;
				
				for(Map.Entry<BlockPos,TileEntity> entry : chunk.getTileEntityMap().entrySet())
				{
					if(entry.getValue() instanceof TileEntityMobSpawner)
					{
						double dist= Util.distance(player, entry.getKey());
						if(dist < 35)
						{
							float factor= 1f;
							if(dist < 15)
								factor= 2f;
							
							SanityTendencyModifier.instance.addToTendencyServer(-10f*factor, "Evil place", player);
							break;
						}
					}
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

	
	public void ClearOverviewData(World world)
	{
		instance.overview.clear();
		instance.overviewTimestamp= world.getTotalWorldTime();
	}
	
}