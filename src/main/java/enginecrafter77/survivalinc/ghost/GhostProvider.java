package enginecrafter77.survivalinc.ghost;

import enginecrafter77.survivalinc.stats.StatCapability;
import enginecrafter77.survivalinc.stats.StatProvider;
import enginecrafter77.survivalinc.stats.StatRecord;
import enginecrafter77.survivalinc.stats.StatRegisterEvent;
import enginecrafter77.survivalinc.stats.StatTracker;
import enginecrafter77.survivalinc.stats.effect.EffectApplicator;
import enginecrafter77.survivalinc.stats.effect.EffectFilter;
import enginecrafter77.survivalinc.stats.effect.FunctionalEffectFilter;
import enginecrafter77.survivalinc.stats.effect.SideEffectFilter;
import enginecrafter77.survivalinc.stats.effect.ValueStatEffect;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import enginecrafter77.survivalinc.SurvivalInc;
import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.net.StatSyncMessage;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockRedstoneComparator;
import net.minecraft.block.BlockRedstoneRepeater;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.FoodStats;
import net.minecraft.util.MovementInput;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;

public class GhostProvider implements StatProvider<GhostEnergyRecord> {
	private static final long serialVersionUID = -2088047893866334112L;
	
	public static final EffectFilter<GhostEnergyRecord> active = (GhostEnergyRecord record, EntityPlayer player) -> record.isActive();
	public static final GhostProvider instance = new GhostProvider();
	public static final HelicalParticleSpawner resurrect_particles = new HelicalParticleSpawner(EnumParticleTypes.PORTAL).setHelixCount(8);
	public static final Vec3d rp_box = new Vec3d(0.6D, 1.5D, 0.6D), rp_offset = new Vec3d(0D, -0.3D, 0D);
	
	public final EffectApplicator<GhostEnergyRecord> applicator;
	public final InteractionProcessor interactor;
	
	public GhostProvider()
	{
		this.applicator = new EffectApplicator<GhostEnergyRecord>();
		this.interactor = new InteractionProcessor(PlayerInteractEvent.RightClickBlock.class, (float)ModConfig.GHOST.interactionCost);
	}
	
	public void init()
	{
		MinecraftForge.EVENT_BUS.register(GhostProvider.class);
		
		EffectFilter<Object> playerSprinting = FunctionalEffectFilter.byPlayer(EntityPlayer::isSprinting);
		this.applicator.add(new ValueStatEffect(ValueStatEffect.Operation.OFFSET, (float)ModConfig.GHOST.passiveNightRegen)).addFilter(GhostProvider::duringNight);
		this.applicator.add(GhostProvider::onGhostUpdate);
		
		this.applicator.add(GhostProvider::resurrectTick);
		if(ModConfig.GHOST.allowFlying) this.applicator.add(GhostProvider::provideFlying);
		if(ModConfig.GHOST.sprintingEnergyDrain > 0)
		{
			this.applicator.add(new ValueStatEffect(ValueStatEffect.Operation.OFFSET, -(float)ModConfig.GHOST.sprintingEnergyDrain)).addFilter(playerSprinting);
			this.applicator.add(GhostProvider::spawnSprintingParticles).addFilter(SideEffectFilter.CLIENT).addFilter(GhostProvider.active).addFilter(playerSprinting);
			this.applicator.add(GhostProvider::synchronizeFood).addFilter(GhostProvider.active);
		}
		
		this.interactor.addBlockClass(BlockDoor.class, 1F);
		this.interactor.addBlockClass(BlockFenceGate.class, 0.9F);
		this.interactor.addBlockClass(BlockRedstoneComparator.class, 2F);
		this.interactor.addBlockClass(BlockRedstoneRepeater.class, 2F);
		this.interactor.addBlockClass(BlockTrapDoor.class, 0.9F);
		this.interactor.setBlockCost(Blocks.STONE_BUTTON, 0.6F);
		this.interactor.setBlockCost(Blocks.WOODEN_BUTTON, 0.5F);
		this.interactor.setBlockCost(Blocks.LEVER, 0.75F);
	}
	
	@Override
	public void update(EntityPlayer target, StatRecord record)
	{
		GhostEnergyRecord ghost = (GhostEnergyRecord)record;
		
		if(ghost.shouldReceiveTicks())
		{
			this.applicator.apply(ghost, target);
			ghost.checkoutValueChange();
		}
	}
	
	@Override
	public ResourceLocation getStatID()
	{
		return new ResourceLocation(SurvivalInc.MOD_ID, "ghostenergy");
	}

	@Override
	public GhostEnergyRecord createNewRecord()
	{
		return new GhostEnergyRecord();
	}
	
	@Override
	public Class<GhostEnergyRecord> getRecordClass()
	{
		return GhostEnergyRecord.class;
	}
	
	public int energyToFood(GhostEnergyRecord record)
	{
		return Math.round(4F + 16F * record.getNormalizedValue());
	}
	
	//==================================
	//=========[Event Handling]=========
	//==================================
	
	@SubscribeEvent
	public static void registerStat(StatRegisterEvent event)
	{
		event.register(GhostProvider.instance);
	}
	
	@SubscribeEvent
	public static void onPlayerRespawn(PlayerRespawnEvent event)
	{
		EntityPlayer player = event.player;
		if(!event.isEndConquered())
		{
			StatTracker tracker = player.getCapability(StatCapability.target, null);
			GhostEnergyRecord record = tracker.getRecord(GhostProvider.instance);
			record.setActive(true);
			
			SurvivalInc.proxy.net.sendToAll(new StatSyncMessage(player));
		}
	}
	
	/**
	 * Takes care of placing interaction toll on ghosts.
	 * @param event The interaction event
	 */
	@SubscribeEvent
	public static void onPlayerInteract(PlayerInteractEvent event)
	{
		StatTracker tracker = event.getEntityPlayer().getCapability(StatCapability.target, null);
		GhostEnergyRecord record = tracker.getRecord(GhostProvider.instance);
		if(record.isActive())
		{
			float energy = record.getValue();
			if(ModConfig.GHOST.enableInteraction && energy >= ModConfig.GHOST.interactionThreshold)
			{
				Float cost = GhostProvider.instance.interactor.apply(event);
				if(cost != null && energy >= cost)
				{
					record.addToValue(-cost);
					GhostProvider.spawnInteractionParticles(event.getEntityPlayer(), event.getPos(), cost);
					return;
				}
			}
			
			if(event.isCancelable()) event.setCanceled(true);
			if(event.hasResult()) event.setResult(Result.DENY);
		}
	}
	
	/**
	 * Makes sure ghosts won't be able to hit mobs. If they attempt to do so,
	 * a tiny bit of their ghost energy will jump to the victim, thus draining
	 * the ghost's energy. 
	 * @param event The attack event
	 */
	@SubscribeEvent
	public static void onPlayerHitEntity(LivingAttackEvent event)
	{
		DamageSource source = event.getSource();
		if(source instanceof EntityDamageSource)
		{
			EntityDamageSource attack = (EntityDamageSource)source;
			Entity attacker = attack.getTrueSource();
			if(attacker instanceof EntityPlayer)
			{
				StatTracker tracker = attacker.getCapability(StatCapability.target, null);
				GhostEnergyRecord record = tracker.getRecord(GhostProvider.instance);
				
				if(record.isActive())
				{
					Entity victim = event.getEntity();
					Random rng = victim.world.rand;
					for(int pass = 32; pass > 0; pass--)
					{
						victim.world.spawnParticle(EnumParticleTypes.CLOUD, victim.posX + victim.width * (rng.nextDouble() - 0.5), victim.posY + victim.height * rng.nextGaussian(), victim.posZ + victim.width * (rng.nextDouble() - 0.5), 0, 0, 0);
					}
					record.addToValue(-1F); // We need to punish the player a lil' bit
					event.setCanceled(true);
				}
			}
		}
	}
	
	/**
	 * Called when an entity dies, so nearby ghosts
	 * can drain it's life energy and use it to form
	 * their former bodies.
	 * @param event The entity death event
	 */
	@SubscribeEvent
	public static void onEntityDeath(LivingDeathEvent event)
	{
		Vec3d offset = new Vec3d(3D, 1D, 3D);
		EntityLivingBase target = event.getEntityLiving();
		Vec3d origin = target.getPositionVector().add(0D, target.height / 2D, 0D);
		AxisAlignedBB box = new AxisAlignedBB(origin.subtract(offset), origin.add(offset));
		
		List<EntityPlayer> players = target.world.getEntitiesWithinAABB(EntityPlayer.class, box);
		for(EntityPlayer player : players)
		{
			StatTracker tracker = player.getCapability(StatCapability.target, null);
			GhostEnergyRecord record = tracker.getRecord(GhostProvider.instance);
			
			if(record.isActive() && record.getNormalizedValue() == 1F) record.tickResurrection();
		}
	}
	
	@SubscribeEvent
	public static void disableItemPickup(EntityItemPickupEvent event)
	{
		StatTracker tracker = event.getEntityPlayer().getCapability(StatCapability.target, null);
		GhostEnergyRecord record = tracker.getRecord(GhostProvider.instance);
		if(record.isActive())
		{
			event.setCanceled(true);
		}
	}
	
	/**
	 * Makes sure that ghosts are not attacked by any mobs whatsoever.
	 * @param event The visibility modifier event
	 */ 
	@SubscribeEvent
	public static void modifyVisibility(PlayerEvent.Visibility event)
	{
		StatTracker tracker = event.getEntityPlayer().getCapability(StatCapability.target, null);
		GhostEnergyRecord record = tracker.getRecord(GhostProvider.instance);
		if(record.isActive()) event.modifyVisibility(0D);
	}
	
	/**
	 * Makes sure that ghosts won't be able to move while being resurrected
	 * @param event The movement input update event
	 */
	@SubscribeEvent
	public static void blockMovementWhileResurrecting(InputUpdateEvent event)
	{
		if(ModConfig.GHOST.resurrectionBlocksMovement)
		{
			StatTracker tracker = event.getEntityPlayer().getCapability(StatCapability.target, null);
			GhostEnergyRecord record = tracker.getRecord(GhostProvider.instance);
			
			if(record.isResurrectionActive())
			{
				MovementInput input = event.getMovementInput();
				input.forwardKeyDown = false;
				input.rightKeyDown = false;
				input.backKeyDown = false;
				input.leftKeyDown = false;
				input.moveForward = 0F;
				input.moveStrafe = 0F;
				input.sneak = false;
				input.jump = false;
			}
		}
	}
	
	//==================================
	//=======[Functional Effects]=======
	//==================================
	
	/**
	 * Takes care of setting ghosts invulnerable,
	 * disabling bobbing and suspending all the
	 * other stats.
	 * @param record The ghost energy record
	 * @param player The player to apply the changes to
	 */
	public static void onGhostUpdate(GhostEnergyRecord record, EntityPlayer player)
	{		
		if(record.hasPendingChange())
		{
			boolean isGhost = record.isActive();
			player.capabilities.disableDamage = isGhost;
			player.capabilities.allowEdit = !isGhost;
			
			// A dirty hack to disable bobbing. Probably not the best way to do it, but whatever.
			if(player.world.isRemote && player == Minecraft.getMinecraft().player)
				Minecraft.getMinecraft().gameSettings.viewBobbing = !isGhost;
			
			// Suspend all other stats
			StatTracker tracker = player.getCapability(StatCapability.target, null);
			Collection<StatProvider<?>> providers = tracker.getRegisteredProviders();
			providers.remove(GhostProvider.instance);
			for(StatProvider<?> provider : providers)
				tracker.setSuspended(provider, isGhost);
			
			record.acceptChange();
		}
	}
	
	/**
	 * Synchronizes player's food level with the ghost energy.
	 * This makes sure that players with little ghost energy
	 * would not be able to sprint.
	 * @param record The ghost energy record
	 * @param player The player to apply the effect to
	 */
	public static void synchronizeFood(GhostEnergyRecord record, EntityPlayer player)
	{
		FoodStats food = player.getFoodStats();
		food.setFoodLevel(GhostProvider.instance.energyToFood(record));
	}
	
	/**
	 * Used to process ghost resurrection ticks.
	 * @param record The ghost energy record
	 * @param player The player to apply the effect to
	 */
	public static void resurrectTick(GhostEnergyRecord record, EntityPlayer player)
	{
		if(record.isResurrectionActive())
		{
			record.tickResurrection();
			
			Vec3d origin = player.getPositionVector().add(0D, player.height / 2D, 0D);
			if(player.world.isRemote)
			{
				WorldClient world = (WorldClient)player.world;
				if(record.timeUntilResurrection() == 60)
				{
					world.playSound(player, origin.x, origin.y, origin.z, SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 0.8F, 1F);
				}
				GhostProvider.resurrect_particles.spawn(world, origin.add(rp_offset), rp_box, Vec3d.ZERO, player.ticksExisted);
			}
			
			if(record.isResurrectionReady())
			{
				record.finishResurrection();
				
				if(!player.world.isRemote)
				{
					// Server code
					WorldServer world = (WorldServer)player.world;
					world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, origin.x, origin.y, origin.z, 10, 0D, 0D, 0D, 0D);
					world.playSound(null, origin.x, origin.y, origin.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.6F, 1F);
				}
			}
		}
	}
	
	/**
	 * Spawns cloud particles representing emitted ghost
	 * energy while the ghost is "sprinting".
	 * @param record The ghost energy record
	 * @param player The player to apply the effect to
	 */
	public static void spawnSprintingParticles(GhostEnergyRecord record, EntityPlayer player)
	{
		WorldClient world = (WorldClient)player.world;
		world.spawnParticle(EnumParticleTypes.CLOUD, player.lastTickPosX, player.lastTickPosY + (player.height / 2), player.lastTickPosZ, -player.motionX, 0.1D, -player.motionZ);
	}
	
	/**
	 * Makes sure that ghosts with enough energy are able to fly
	 * as long as their energy is above the flying threshold.
	 * @param record The ghost energy record
	 * @param player The player to apply the effect to
	 */
	public static void provideFlying(GhostEnergyRecord record, EntityPlayer player)
	{
		boolean shouldFly = record.isActive() && record.getValue() > ModConfig.GHOST.flyingThreshold;
		if(player.capabilities.allowFlying != shouldFly) player.capabilities.allowFlying = shouldFly;
		
		if(player.capabilities.isFlying)
		{
			record.addToValue(-(float)ModConfig.GHOST.flyingDrain);
			if(record.getValue() < ModConfig.GHOST.flyingThreshold) player.capabilities.isFlying = false;
		}
	}
	
	/**
	 * Passively gives free energy to ghosts during night.
	 * @param record The ghost energy record
	 * @param player The player to apply the effect to
	 */
	public static boolean duringNight(GhostEnergyRecord record, EntityPlayer player)
	{
		boolean night;
		if(player.world.isRemote)
		{
			float angle = player.world.getCelestialAngle(1F);
			night = angle < 0.75F && angle > 0.25F;
		}
		else night = !player.world.isDaytime();
		return night;
	}
	
	//=====================================
	//=======[Miscellaneous Methods]=======
	//=====================================
	
	/**
	 * Spawns a cloud around the block the player has right-clicked.
	 * @param record The ghost energy record
	 * @param player The player to apply the effect to
	 */
	public static void spawnInteractionParticles(EntityPlayer player, BlockPos position, float cost)
	{
		if(player.world.isRemote)
		{
			AxisAlignedBB box = player.world.getBlockState(position).getBoundingBox(player.world, position).grow(0.1D);
			Vec3d offbound = new Vec3d(box.maxX - box.minX, box.maxY - box.minY, box.maxZ - box.minY);
			Vec3d center = new Vec3d(position).add(box.getCenter());
			
			Random rng = player.world.rand;
			for(int pass = Math.round(cost * 2F); pass > 0; pass--)
			{
				Vec3d randoff = new Vec3d(rng.nextDouble() - 0.5D, rng.nextDouble() - 0.5D, rng.nextDouble() - 0.5D);
				Vec3d pos = center.add(offbound.x * randoff.x, offbound.y * randoff.y, offbound.z * randoff.z);
				player.world.spawnParticle(EnumParticleTypes.CLOUD, pos.x, pos.y, pos.z, 0, 0.2D, 0);
			}
		}
	}
}
