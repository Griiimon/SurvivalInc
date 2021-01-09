package enginecrafter77.survivalinc.stats.impl;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Range;

import enginecrafter77.survivalinc.SurvivalInc;
import enginecrafter77.survivalinc.client.DifferentialArrow;
import enginecrafter77.survivalinc.client.TexturedColorElement;
import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.debug.LightDebugCommand;
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
import enginecrafter77.survivalinc.strugglecraft.TraitModule;
import enginecrafter77.survivalinc.strugglecraft.TraitModule.TRAITS;
import enginecrafter77.survivalinc.util.Util;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemArmor;
import net.minecraft.potion.PotionEffect;
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

	public static final DamageSource LOW_SANITY_DAMAGE= new DamageSource("survivalinc_lowsanity").setDamageIsAbsolute().setDamageBypassesArmor();

	
	public static final SanityModifier instance = new SanityModifier();
	
	public final EffectApplicator<SimpleStatRecord> effects;

	
	public SanityModifier()
	{
		this.effects = new EffectApplicator<SimpleStatRecord>();
	}
	
	public void init()
	{
		MinecraftForge.EVENT_BUS.register(SanityModifier.class);
	
		this.effects.add(SanityModifier::lowSanityConsequences);//.addFilter(SideEffectFilter.CLIENT);

	}
	
	@SubscribeEvent
	public static void registerStat(StatRegisterEvent event)
	{
		event.register(SanityModifier.instance);
	}
	
	@Override
	public void update(EntityPlayer player, StatRecord record)
	{
		if(player.isCreative() || player.isSpectator() || player.isDead/*|| player.world.isRemote*/) return;
		
		
		if(player.world.isRemote && !Util.thisClientOnly(player))
			return;

		StatTracker tracker = player.getCapability(StatCapability.target, null);
		
		if(tracker == null)
			return;
			
		SanityRecord tendency = tracker.getRecord(SanityTendencyModifier.instance);

		SimpleStatRecord sanity= (SimpleStatRecord) record;
		// TODO correct?
		this.effects.apply(sanity, player);
		
		sanity.addToValue((float)(tendency.getValue() * ModConfig.SANITY.tendencyImpact));
		sanity.checkoutValueChange();

		
		
		// excited state / haste-push
		if(sanity.getValue() == 100f)
		{
			if(Util.thisClientOnly(player))
			{
				TexturedColorElement.scale= tendency.getValue() / 100f;
			}
			
			if(tendency.getValue()>0f)
			{
				boolean isEcstatic= TraitModule.instance.HasTrait(player, TRAITS.ECSTATIC);
				
				// dampener
				float factor= tendency.getValue()/10000f;
				
				if(isEcstatic)
				{
					factor/= (TraitModule.instance.TraitTier(player, TRAITS.ECSTATIC)+1f) * 2f;
				}
				
				// tendency dampener
				SanityTendencyModifier.instance.addToTendency(-factor, "", player);

				if(tendency.getValue() > 50f)
				{
					new PotionStatEffect(MobEffects.HASTE, 2).apply(tendency, player);
					new PotionStatEffect(MobEffects.SPEED, 1).apply(tendency, player);
					if(isEcstatic)
						TraitModule.instance.UsingTrait(player, TRAITS.ECSTATIC, 2f);
				}
				else
				if(tendency.getValue() > 20f)
				{
					new PotionStatEffect(MobEffects.HASTE, 1).apply(tendency, player);
					new PotionStatEffect(MobEffects.SPEED, 0).apply(tendency, player);
					if(isEcstatic)
						TraitModule.instance.UsingTrait(player, TRAITS.ECSTATIC, 2f);
				}
				else
				if(tendency.getValue() > 5f)
				{
					new PotionStatEffect(MobEffects.HASTE, 0).apply(tendency, player);
					if(isEcstatic)
						TraitModule.instance.UsingTrait(player, TRAITS.ECSTATIC);
				}
			}
		}
		else
		{
			if(Util.thisClientOnly(player))
			{
				TexturedColorElement.scale= 0f;
			}
		}
			
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
	
	public static void lowSanityConsequences(SimpleStatRecord record, EntityPlayer player)
	{
		float threshold = (float)ModConfig.SANITY.hallucinationThreshold * 100f;

		if(player.world.getWorldTime() % 160 == 0 && !player.isPlayerSleeping())
		{


			if(Util.chance((threshold - record.getValue()) * (100f / (float)threshold)))
			{

				
				if(Util.chance(80f))
				{
					if(Util.chance(50))
						return;
					
					if(!Util.thisClientOnly(player))
						return;

					// collection of spooky sounds
					SoundEvent[] sounds= new SoundEvent[] {SoundEvents.BLOCK_FIRE_AMBIENT, SoundEvents.ENTITY_ZOMBIE_AMBIENT, SoundEvents.ENTITY_ZOMBIE_ATTACK_DOOR_WOOD, SoundEvents.ENTITY_CREEPER_PRIMED, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundEvents.ENTITY_GHAST_SCREAM, SoundEvents.ENTITY_HOSTILE_BIG_FALL, SoundEvents.BLOCK_LAVA_POP, SoundEvents.ENTITY_PLAYER_HURT_ON_FIRE, SoundEvents.ENTITY_PLAYER_HURT_DROWN,SoundEvents.ENTITY_PLAYER_HURT,SoundEvents.ENTITY_POLAR_BEAR_WARNING,SoundEvents.ENTITY_SPIDER_AMBIENT,SoundEvents.BLOCK_TRIPWIRE_CLICK_ON,SoundEvents.ENTITY_WITCH_AMBIENT,SoundEvents.ENTITY_WITCH_THROW,SoundEvents.ENTITY_WITHER_SKELETON_STEP,SoundEvents.ENTITY_WITHER_SKELETON_AMBIENT,SoundEvents.ENTITY_WITHER_SKELETON_HURT,SoundEvents.ENTITY_ITEM_BREAK,SoundEvents.ENTITY_ENDERMEN_STARE,SoundEvents.ENTITY_ENDERMEN_SCREAM,SoundEvents.ENTITY_ENDERMEN_TELEPORT};
					
					int r= (Util.rnd(sounds.length));
					player.world.playSound(player.posX, player.posY, player.posZ, sounds[r], SoundCategory.AMBIENT, Util.rndf(0.8f)+0.2f, 1, false);
				}
				else
				if(Util.chance(50f) && record.getValue() < threshold / 2f)
				{
					if(!player.world.isRemote)
					{
						if(Util.chance(50))
							player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, Util.rnd(15*20,45*20), Util.rnd(155)+100));
						else
							player.attackEntityFrom(LOW_SANITY_DAMAGE, Util.rnd(1,8));
					}
				}
				else
				{
					if(!Util.thisClientOnly(player))
						return;

					// 1F - current / threshold => this calculation is used to increase the volume for "more insane" players, up to 100% original volume (applied at sanity 0)
					float volume = (1F - record.getValue() / threshold) * (float)ModConfig.SANITY.staticBuzzIntensity;
					player.world.playSound(player.posX, player.posY, player.posZ, staticbuzz, SoundCategory.AMBIENT, volume, 1, false);
//					if(Util.chance(10f))
//						Minecraft.getMinecraft().entityRenderer.loadShader(distortshader);
				}
			}
			else
			{
/*				// Check if the current shader is our shader, and if so, stop using it.
				ShaderGroup shader = client.entityRenderer.getShaderGroup();
				if(player.world.getTotalWorldTime() % 100 == 0 && shader != null && shader.getShaderGroupName().equals(distortshader.toString()))
				{
					client.entityRenderer.stopUseShader();
				}
*/			}
		}
/*		else
		{
			Minecraft client = Minecraft.getMinecraft();
			ShaderGroup shader = client.entityRenderer.getShaderGroup();
			if(player.world.getTotalWorldTime() % 100 == 0 && shader != null && shader.getShaderGroupName().equals(distortshader.toString()))
			{
				client.entityRenderer.stopUseShader();
			}
		}
*/			
	}
	
	
	// TODO Move
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
			if(event.player.world.getWorldTime() % 40 == 0 && event.phase == Phase.START)
			{
				SimpleStatRecord sanityTendency = tracker.getRecord(SanityTendencyModifier.instance);
	
				float total= sanity.getValue();
				float tend= sanityTendency.getValue();
				
				event.player.sendMessage(new TextComponentString("Total: "+total+", Tend: "+tend));
			}
		}
		
		
		if(LightDebugCommand.enabled)
		{
			// Sends debug information to the player each second about absolute sanity and sanity tendency 
			if(event.player.world.getWorldTime() % 40 == 0 && event.phase == Phase.START)
			{
				BlockPos position = new BlockPos(event.player.getPositionVector().add(0D, event.player.getEyeHeight(), 0D));
				int lightlevel = event.player.world.getLight(position);

				event.player.sendMessage(new TextComponentString("Light level: "+lightlevel));

			}
		}
		
/*		
		Minecraft client = Minecraft.getMinecraft();
		ShaderGroup shader = client.entityRenderer.getShaderGroup();
		if(shader != null && event.player.world.getWorldTime() % 160 == 0 && shader.getShaderGroupName().equals(distortshader.toString())  && !tracker.isActive(SanityModifier.instance, null))
		{
			client.entityRenderer.stopUseShader();
		}
*/
	}
	
	
	public void set(float value, EntityPlayer player)
	{
		StatTracker tracker = player.getCapability(StatCapability.target, null);
		SimpleStatRecord sanity = tracker.getRecord(SanityModifier.instance);
		sanity.setValue(value);
	}
	
	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event)
	{
		event.getRegistry().register(SanityModifier.staticbuzz);
	}
	
	
}
