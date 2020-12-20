package enginecrafter77.survivalinc.stats.impl;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Range;

import enginecrafter77.survivalinc.SurvivalInc;
import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.debug.SanityDebugCommand;
import enginecrafter77.survivalinc.stats.StatProvider;
import enginecrafter77.survivalinc.stats.StatRecord;
import enginecrafter77.survivalinc.stats.StatRegisterEvent;
import enginecrafter77.survivalinc.stats.SimpleStatRecord;
import enginecrafter77.survivalinc.stats.StatCapability;
import enginecrafter77.survivalinc.stats.StatTracker;
import enginecrafter77.survivalinc.stats.effect.DamageStatEffect;
import enginecrafter77.survivalinc.stats.effect.EffectApplicator;
import enginecrafter77.survivalinc.stats.effect.FunctionalCalculator;
import enginecrafter77.survivalinc.stats.effect.FunctionalEffectFilter;
import enginecrafter77.survivalinc.stats.effect.PotionStatEffect;
import enginecrafter77.survivalinc.stats.effect.SideEffectFilter;
import enginecrafter77.survivalinc.util.Util;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.init.SoundEvents;

/**
 * The class that handles heat radiation and
 * it's associated interactions with the
 * player entity.
 * @author Enginecrafter77
 */
public class SanityModifier implements StatProvider<SimpleStatRecord> {
	// TODO how to generate??
	private static final long serialVersionUID = 6269992840749029918L;
	
	public static final ResourceLocation distortshader = new ResourceLocation(SurvivalInc.MOD_ID, "shaders/distort.json");
	public static final SoundEvent staticbuzz = new SoundEvent(new ResourceLocation(SurvivalInc.MOD_ID, "staticbuzz"));

	
	public static final SanityModifier instance = new SanityModifier();
	
	public final EffectApplicator<SimpleStatRecord> effects;

	
	public SanityModifier()
	{
		this.effects = new EffectApplicator<SimpleStatRecord>();
	}
	
	public void init()
	{
		MinecraftForge.EVENT_BUS.register(SanityModifier.class);
	
		// this ain't working for some reason
//		this.effects.add(SanityModifier::playStaticNoise).addFilter(SideEffectFilter.CLIENT);

	}
	
	@SubscribeEvent
	public static void registerStat(StatRegisterEvent event)
	{
		event.register(SanityModifier.instance);
	}
	
	@Override
	public void update(EntityPlayer player, StatRecord record)
	{
		if(player.isCreative() || player.isSpectator()) return;
		
		StatTracker tracker = player.getCapability(StatCapability.target, null);
		
		if(tracker == null)
			return;
			
		SanityRecord tendency = tracker.getRecord(SanityTendencyModifier.instance);

		SimpleStatRecord sanity= (SimpleStatRecord) record;
		
		sanity.addToValue((float)(tendency.getValue() * ModConfig.SANITY.tendencyImpact));
		sanity.checkoutValueChange();
	}

	@Override
	public ResourceLocation getStatID()
	{
		return new ResourceLocation(SurvivalInc.MOD_ID, "sanity");
	}

	@Override
	public SimpleStatRecord createNewRecord()
	{
		SimpleStatRecord record = new SimpleStatRecord();
		record.setValueRange(Range.closed(0F, 100F));
		record.setValue((float)ModConfig.SANITY.startValue);
		return record;
	}
	
	@Override
	public Class<SimpleStatRecord> getRecordClass()
	{
		return SimpleStatRecord.class;
	}
	
	public static void playStaticNoise(SimpleStatRecord record, EntityPlayer player)
	{
		float threshold = (float)ModConfig.SANITY.hallucinationThreshold * 100f;

		if(player.world.getWorldTime() % 160 == 0)
		{
			/**
			 * This effect should only apply to the client player.
			 * Other player entities on client-side world should
			 * not be evaluated.
			 */
			Minecraft client = Minecraft.getMinecraft();
			if(player != client.player) return;
			
			if(Util.chance(25) && record.getValue() < threshold)
			{
				// collection of spooky sounds
				SoundEvent[] sounds= new SoundEvent[] {SoundEvents.BLOCK_FIRE_AMBIENT, SoundEvents.ENTITY_ZOMBIE_AMBIENT, SoundEvents.ENTITY_ZOMBIE_ATTACK_DOOR_WOOD, SoundEvents.ENTITY_CREEPER_PRIMED, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundEvents.ENTITY_GHAST_SCREAM, SoundEvents.ENTITY_HOSTILE_BIG_FALL, SoundEvents.BLOCK_LAVA_POP, SoundEvents.ENTITY_PLAYER_HURT_ON_FIRE, SoundEvents.ENTITY_PLAYER_HURT_DROWN,SoundEvents.ENTITY_PLAYER_HURT,SoundEvents.ENTITY_POLAR_BEAR_WARNING,SoundEvents.ENTITY_SPIDER_AMBIENT,SoundEvents.BLOCK_TRIPWIRE_CLICK_ON,SoundEvents.ENTITY_WITCH_AMBIENT,SoundEvents.ENTITY_WITCH_THROW,SoundEvents.ENTITY_WITHER_SKELETON_STEP,SoundEvents.ENTITY_WITHER_SKELETON_AMBIENT,SoundEvents.ENTITY_WITHER_SKELETON_HURT,SoundEvents.ENTITY_ITEM_BREAK,SoundEvents.ENTITY_ENDERMEN_STARE,SoundEvents.ENTITY_ENDERMEN_SCREAM,SoundEvents.ENTITY_ENDERMEN_TELEPORT};
				
				int r= (Util.rnd(sounds.length+1));
				
				if(r < sounds.length)
						player.world.playSound(player.posX, player.posY, player.posZ, sounds[r], SoundCategory.AMBIENT, Util.rndf(0.5f)+0.5f, 1, false);
				else
				{
					// 1F - current / threshold => this calculation is used to increase the volume for "more insane" players, up to 100% original volume (applied at sanity 0)
					float volume = (1F - record.getValue() / threshold) * (float)ModConfig.SANITY.staticBuzzIntensity;
					player.world.playSound(player.posX, player.posY, player.posZ, staticbuzz, SoundCategory.AMBIENT, volume, 1, false);
					client.entityRenderer.loadShader(distortshader);
				}
			}
			else
			{
				// Check if the current shader is our shader, and if so, stop using it.
				ShaderGroup shader = client.entityRenderer.getShaderGroup();
				if(shader != null && shader.getShaderGroupName().equals(distortshader.toString()))
				{
					client.entityRenderer.stopUseShader();
				}
			}
		}
	}
	
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void onClientWorldTick(TickEvent.PlayerTickEvent event)
	{
		if(event.side.isServer()) return;

		StatTracker tracker = event.player.getCapability(StatCapability.target, null);
		SimpleStatRecord sanity = tracker.getRecord(SanityModifier.instance);

		if(SanityDebugCommand.enabled)
		{
			// Sends debug information to the player each second about absolute sanity and sanity tendency 
			if(event.player.world.getWorldTime() % 20 == 0 && event.phase == Phase.START)
			{
				SimpleStatRecord sanityTendency = tracker.getRecord(SanityTendencyModifier.instance);
	
				float total= sanity.getValue();
				float tend= sanityTendency.getValue();
				
				event.player.sendMessage(new TextComponentString("Total: "+total+", Tend: "+tend));
			}
		}
		
		
		Minecraft client = Minecraft.getMinecraft();
		ShaderGroup shader = client.entityRenderer.getShaderGroup();
		if(shader != null && event.player.world.getWorldTime() % 160 == 0 && shader.getShaderGroupName().equals(distortshader.toString()) && !tracker.isActive(SanityModifier.instance, null))
		{
			client.entityRenderer.stopUseShader();
		}
		else
			playStaticNoise(sanity, event.player);
	}
	
	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event)
	{
		event.getRegistry().register(SanityModifier.staticbuzz);
	}
	
	
}
