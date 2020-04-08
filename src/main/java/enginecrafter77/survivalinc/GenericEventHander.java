package enginecrafter77.survivalinc;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeBeach;
import net.minecraft.world.biome.BiomeOcean;
import net.minecraft.world.biome.BiomeSwamp;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import java.util.Iterator;
import java.util.List;

import enginecrafter77.survivalinc.cap.ghost.GhostMain;
import enginecrafter77.survivalinc.cap.ghost.GhostProvider;
import enginecrafter77.survivalinc.cap.ghost.IGhost;
import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.net.StatUpdateMessage;
import enginecrafter77.survivalinc.stats.StatRegister;
import enginecrafter77.survivalinc.stats.StatTracker;
import enginecrafter77.survivalinc.stats.impl.DefaultStats;
import enginecrafter77.survivalinc.stats.impl.SanityModifier;
import enginecrafter77.survivalinc.stats.impl.WetnessModifier;
import enginecrafter77.survivalinc.util.SchopServerEffects;

/*
 * This is the event handler regarding capabilities and changes to individual stats.
 * Most of the actual code is stored in the modifier classes of each stat, and fired here.
 */
@Mod.EventBusSubscriber
public class GenericEventHander {

	// Modifiers
	private static final GhostMain ghostMain = new GhostMain();
	
	public static void sendUpdate(EntityPlayer player, StatTracker stats, IGhost ghost)
	{
		SurvivalInc.proxy.net.sendTo(new StatUpdateMessage(stats), (EntityPlayerMP) player);
	}
	
	@SubscribeEvent
	public static void attachCapability(AttachCapabilitiesEvent<Entity> event)
	{
		if(!(event.getObject() instanceof EntityPlayer)) return;
		
		event.addCapability(new ResourceLocation(SurvivalInc.MOD_ID, "ghost"), new GhostProvider());
	}

	// When an entity is updated. So, all the time.
	// This also deals with packets to the client.
	@SubscribeEvent
	public static void onPlayerUpdate(LivingUpdateEvent event)
	{
		// Only continue if it's a player.
		if (event.getEntity() instanceof EntityPlayer)
		{
			// Instance of player.
			EntityPlayer player = (EntityPlayer)event.getEntity();
			StatTracker stat = player.getCapability(StatRegister.CAPABILITY, null);
			IGhost ghost = player.getCapability(GhostProvider.GHOST_CAP, null);
			
			// Server-side
			if(!player.world.isRemote)
			{				
				if(!player.isCreative() && !player.isSpectator())
				{
					stat.update(player);
					
					if(ModConfig.MECHANICS.enableSanity)
					{
						SanityModifier.applyAdverseEffects(player);
						
						// Fire this if the player is sleeping
						if(player.isPlayerSleeping())
						{
							stat.modifyStat(DefaultStats.SANITY, 0.004f);
							SchopServerEffects.affectPlayer(player.getCachedUniqueIdString(), "hunger", 20, 4, false, false);
						}
					}

					if(ModConfig.MECHANICS.enableWetness)
					{
						WetnessModifier.onPlayerUpdate(player);
					}

					if(ModConfig.MECHANICS.enableGhost)
					{
						ghostMain.onPlayerUpdate(player);
					}
				}
				GenericEventHander.sendUpdate(player, stat, ghost);
			}
		}
	}

	// When a player interacts with a block (usually right clicking something).
	@SubscribeEvent
	public static void onPlayerInteract(PlayerInteractEvent event)
	{
		// Instance of player.
		EntityPlayer player = event.getEntityPlayer();

		// Server-side
		if(!player.world.isRemote)
		{
			// Cancel interacting with blocks if the player is a ghost. This must be done here.
			IGhost ghost = player.getCapability(GhostProvider.GHOST_CAP, null);

			if(ghost.status() && event.isCancelable())
			{
				event.setCanceled(true);
			}

			// Fire methods.
			if(ModConfig.MECHANICS.enableThirst && !player.isCreative() && !player.isSpectator())
			{
				GenericEventHander.raytraceWater(player);
			}
		}
	}

	// When a player (kind of) finishes using an item. Technically one tick
	// before it's actually consumed.
	@SubscribeEvent
	public static void onPlayerUseItem(LivingEntityUseItemEvent.Tick event)
	{
		if(event.getEntity() instanceof EntityPlayer)
		{
			// Instance of player.
			EntityPlayer player = (EntityPlayer) event.getEntity();

			// Server-side
			if (!player.world.isRemote && event.getDuration() == 1)
			{
				// Instance of item.
				ItemStack itemUsed = event.getItem();

				// Fire methods.
				if(!player.isCreative())
				{

					if(ModConfig.MECHANICS.enableSanity)
					{
						SanityModifier.onPlayerConsumeItem(player, itemUsed);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerWakeUp(PlayerWakeUpEvent event)
	{
		EntityPlayer player = event.getEntityPlayer();

		if(!player.world.isRemote)
		{
			SanityModifier.onPlayerWakeUp(player);
		}
	}
	
	@SubscribeEvent
	public static void onDropsDropped(LivingDropsEvent event)
	{
		// The entity that was killed.
		Entity entityKilled = event.getEntity();

		// Server-side
		if (!entityKilled.world.isRemote)
		{

			// A list of their drops.
			List<EntityItem> drops = event.getDrops();

			// The looting level of the weapon.
			int lootingLevel = event.getLootingLevel();

			// Damage source.
			DamageSource damageSource = event.getSource();

			// Fire methods.
			if(ModConfig.MECHANICS.enableSanity)
			{
				SanityModifier.onDropsDropped(entityKilled, drops, lootingLevel, damageSource);
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerRespawn(PlayerRespawnEvent event)
	{
		// Instance of player.
		EntityPlayer player = event.player;

		/* 
		 * Check if we are operating on server side.
		 * Also, going through the end portal back to
		 * the overworld counts as respawning. This
		 * shouldn't make you a ghost.
		 */
		if(!(player.world.isRemote || event.isEndConquered()))
		{
			ghostMain.onPlayerRespawn(player);
			
			// This should be handled by vanilla codes. No need to get our hands dirty.
			/*else
			{
				// Set no gravity.
				player.setNoGravity(true);

				// Move player away from portal.
				player.setLocationAndAngles(player.posX + 3, player.posY + 1, player.posZ + 3, 0.0f, 0.0f);

				// Set gravity back.
				player.setNoGravity(false);
			}*/
		}
	}
	
	@SubscribeEvent
	public static void onItemPickup(EntityItemPickupEvent event)
	{
		EntityPlayer player = event.getEntityPlayer();
		IGhost ghost = player.getCapability(GhostProvider.GHOST_CAP, null);

		if(ghost.status())
		{
			event.setCanceled(true);
		}
	}
	
	public static void raytraceWater(EntityPlayer player)
	{
		// Capability
		StatTracker stat = player.getCapability(StatRegister.CAPABILITY, null);
		
		// Ray trace result for drinking with bare hands. pretty ineffective.
		double vecX = player.getLookVec().x < 0 ? -0.5 : 0.5;
		double vecZ = player.getLookVec().z < 0 ? -0.5 : 0.5;
		
		// Now the actual raytrace.
		Vec3d look = player.getPositionEyes(1.0f).add(player.getLookVec().addVector(vecX, -1, vecZ));
		RayTraceResult raytrace = player.world.rayTraceBlocks(player.getPositionEyes(1.0f), look, true);
		
		// Is there something?
		if(raytrace != null)
		{
			// Is it a block?
			if(raytrace.typeOfHit == RayTraceResult.Type.BLOCK)
			{
				BlockPos pos = raytrace.getBlockPos();
				Iterator<ItemStack> handItems = player.getHeldEquipment().iterator();

				// If it is water and the player isn't holding jack squat (main
				// hand).
				if(player.world.getBlockState(pos).getMaterial() == Material.WATER && handItems.next().isEmpty())
				{
					// Still more if statements. now see what biome the player
					// is in, and quench thirst accordingly.
					Biome biome = player.world.getBiome(pos);
					
					if(biome instanceof BiomeOcean || biome instanceof BiomeBeach)
					{
						stat.modifyStat(DefaultStats.HYDRATION, 0.5f);
					}
					else if(biome instanceof BiomeSwamp)
					{
						stat.modifyStat(DefaultStats.HYDRATION, 0.25f);
						player.addPotionEffect(new PotionEffect(MobEffects.POISON, 12, 3, false, false));
					}
					else
					{
						stat.modifyStat(DefaultStats.HYDRATION, 0.4f); 
						if(Math.random() <= 0.50)
							player.addPotionEffect(new PotionEffect(MobEffects.POISON, 12, 1, false, false));
					}
					
					if(!player.world.isRemote)
					{
						player.world.spawnParticle(EnumParticleTypes.DRIP_WATER, pos.getX(), pos.getY(), pos.getZ(), 0.3d, 0.5d, 0.3d);
						player.world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_SWIM, SoundCategory.NEUTRAL, 0.5f, 1.5f);
					}
				}
			}
		}
	}
}