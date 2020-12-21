package enginecrafter77.survivalinc.stats.impl;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Range;

import enginecrafter77.survivalinc.SurvivalInc;
import enginecrafter77.survivalinc.config.ModConfig;
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
import enginecrafter77.survivalinc.survivecraft.TraitModule;
import enginecrafter77.survivalinc.survivecraft.TraitModule.TRAITS;
import enginecrafter77.survivalinc.util.Util;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * The class that handles heat radiation and
 * it's associated interactions with the
 * player entity.
 * @author Enginecrafter77
 */
public class HeatModifier implements StatProvider<SimpleStatRecord> {
	private static final long serialVersionUID = 6260092840749029918L;
	
	public static final DamageSource HYPERTHERMIA = new DamageSource("survivalinc_hyperthermia").setDamageIsAbsolute().setDamageBypassesArmor();
	public static final DamageSource HYPOTHERMIA = new DamageSource("survivalinc_hypothermia").setDamageIsAbsolute().setDamageBypassesArmor();
	
	public static final HeatModifier instance = new HeatModifier();
	
	public final Map<Block, Float> blockHeatMap;
	public final ArmorModifier armorInsulation;
	
	public final FunctionalCalculator targettemp;
	public final FunctionalCalculator exchangerate;
	public final EffectApplicator<SimpleStatRecord> consequences;
	
	public HeatModifier()
	{
		this.targettemp = new FunctionalCalculator();
		this.exchangerate = new FunctionalCalculator();
		this.consequences = new EffectApplicator<SimpleStatRecord>();
		this.blockHeatMap = new HashMap<Block, Float>();
		this.armorInsulation = new ArmorModifier();
	}
	
	public void init()
	{
		MinecraftForge.EVENT_BUS.register(HeatModifier.class);
		
		this.targettemp.add(HeatModifier::whenNearHotBlock);
		
		// Even with Wetness turn off this still applies to swimming
		this.exchangerate.add(HeatModifier::applyWetnessCooldown);
		this.exchangerate.add(this.armorInsulation);
		

		// Shit, these repeated parsers will surely get me a bad codefactor.io mark.
		// Block temperature map
		for(String entry : ModConfig.HEAT.blockHeatMap)
		{
			int separator = entry.lastIndexOf(' ');
			Block target = Block.getBlockFromName(entry.substring(0, separator));
			Float value = Float.parseFloat(entry.substring(separator + 1));
			this.blockHeatMap.put(target, value);
		}
		
		// Armor heat isolation
		for(String entry : ModConfig.HEAT.armorMaterialConductivity)
		{
			int separator = entry.lastIndexOf(' ');
			ItemArmor.ArmorMaterial target = ItemArmor.ArmorMaterial.valueOf(entry.substring(0, separator).toUpperCase());
			Float value = Float.parseFloat(entry.substring(separator + 1));
			this.armorInsulation.addArmorType(target, value);
		}
	}
	
	@SubscribeEvent
	public static void registerStat(StatRegisterEvent event)
	{
		event.register(HeatModifier.instance);
	}
	
	@Override
	public void update(EntityPlayer player, StatRecord record)
	{
		if(player.isCreative() || player.isSpectator()) return;
		
		float target;

		Biome biome = player.world.getBiome(player.getPosition());
		target = biome.getTemperature(player.getPosition());

		if(!Util.isDaytime(player))
			target-= ModConfig.HEAT.nightTemperatureDrop;
		
		if(player.isSprinting())
			target+= ModConfig.HEAT.runningTemperatureIncrease;
		
		if(Util.isDaytime(player))
			target+= ModConfig.HEAT.sunWarmth;
		
		if(player.posY < player.world.getSeaLevel()) // Cave
		{
			// temperature gradually changes 
			float depthThreshold= 30f;
			target = (float)Util.lerp(target, (float)ModConfig.HEAT.caveTemperature, (float)Math.min((player.world.getSeaLevel() - player.posY) / depthThreshold, 1f));   
		}

		
		if(target < -0.2F) target = -0.2F;
		if(target > 1.5F) target = 1.5F;
		
		target = targettemp.apply(player, target * (float)ModConfig.HEAT.tempCoefficient);
		
		SimpleStatRecord heat = (SimpleStatRecord)record;
		float difference = Math.abs(target - heat.getValue());
		float rate = difference * (float)ModConfig.HEAT.heatExchangeFactor;
		rate = this.exchangerate.apply(player, rate);

		

		
		// Apply the "side effects"
//		this.consequences.apply(heat, player);

		boolean isColdResistant= TraitModule.instance.HasTrait(TRAITS.WARM);
		
		float coldResistance= isColdResistant ? TraitModule.instance.TraitTier(TRAITS.WARM) + 1 : 0f; 
		
		if(heat.getValue() < 10f - coldResistance)
		{
			new DamageStatEffect(HYPOTHERMIA, (float)ModConfig.HEAT.damageAmount, 10).apply(heat, player);
			TraitModule.instance.UsingTrait(TRAITS.WARM, 5f);
		}
		if(heat.getValue() < 20f - coldResistance)
		{
			new PotionStatEffect(MobEffects.MINING_FATIGUE, 0).apply(heat, player);
			TraitModule.instance.UsingTrait(TRAITS.WARM, 1.5f);
		}
		if(heat.getValue() < 25f - coldResistance)
		{
			new PotionStatEffect(MobEffects.WEAKNESS, 0).apply(record, player);
			TraitModule.instance.UsingTrait(TRAITS.WARM);
			if(Util.chance(1f))
				player.world.playSound(player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_BREATH, SoundCategory.AMBIENT, Util.rndf(0.5f)+0.5f, 1, false);
		}
		if(heat.getValue() > 110f)
		{
			onHighTemperature(record, player);
			if(Util.chance(1f))
				player.world.playSound(player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_BREATH, SoundCategory.AMBIENT, Util.rndf(0.5f)+0.5f, 1, false);
		}
		
		// If the current value is higher than the target, go down instead of up
		if(heat.getValue() > target) rate *= -1;
		// Checkout the rate to the value
		heat.addToValue(rate);
		heat.checkoutValueChange();
		
	}

	@Override
	public ResourceLocation getStatID()
	{
		return new ResourceLocation(SurvivalInc.MOD_ID, "heat");
	}

	@Override
	public SimpleStatRecord createNewRecord()
	{
		SimpleStatRecord record = new SimpleStatRecord();
		record.setValueRange(Range.closed(-20F, 120F));
		record.setValue(80F);
		return record;
	}
	
	@Override
	public Class<SimpleStatRecord> getRecordClass()
	{
		return SimpleStatRecord.class;
	}
	
	public static void onHighTemperature(StatRecord record, EntityPlayer player)
	{
		if(ModConfig.HEAT.fireDuration > 0)
		{
			player.setFire(1);
		}
		else
		{
			player.attackEntityFrom(HYPERTHERMIA, (float)ModConfig.HEAT.damageAmount);
		}
	}
	
	public static float applyWetnessCooldown(EntityPlayer player, float current)
	{
		float wetnessFactor= 0f;
		
		if(player.isInWater())
			wetnessFactor= 1f;
		else
		if(ModConfig.WETNESS.enabled)
		{
			StatTracker stats = player.getCapability(StatCapability.target, null);
			SimpleStatRecord wetness = stats.getRecord(WetnessModifier.instance);
			wetnessFactor= wetness.getNormalizedValue();
		}
		return current * (1F + (float)ModConfig.HEAT.wetnessExchangeMultiplier * wetnessFactor);
	}
	
	/**
	 * Applies the highest heat emmited by the neighboring blocks.
	 * Note that this method does NOT account blocks inbetween, as
	 * that would need to involve costly raytracing. Also, only the
	 * heat Anyway, the
	 * way the heat delivered to the player is calculated by the
	 * following formula:
	 * <pre>
	 *                   s
	 * f(x): y = t * ---------
	 *                x^2 + s
	 * </pre>
	 * Where t is the base heat of the block (the heat delivered when
	 * the distance to the source is 0), s is a special so-called
	 * "gaussian constant" and x is the distance to the player. The
	 * "gaussian constant" has got it's name because the graph of
	 * that function roughly resembles gauss's curve. The constant
	 * in itself is a special value that indicates the scaling of
	 * the heat given. The higher the value is the slower the heat
	 * decline with distance is. A fairly reasonable value is 1.5,
	 * but this value can be specified in the config. It is recommended
	 * that players that use low block scan range to also use lower
	 * gaussian constant.
	 * @author Enginecrafter77
	 * @param player The player to apply this function to
	 * @return The addition to the heat stat value
	 */
	public static float whenNearHotBlock(EntityPlayer player, float current)
	{
		Vec3i offset = new Vec3i(ModConfig.HEAT.blockScanRange, 1, ModConfig.HEAT.blockScanRange);
		BlockPos originblock = player.getPosition();
		
		Iterable<BlockPos> blocks = BlockPos.getAllInBox(originblock.subtract(offset), originblock.add(offset));
		
		float heat = 0;
		for(BlockPos position : blocks)
		{
			Block block = player.world.getBlockState(position).getBlock();
			if(HeatModifier.instance.blockHeatMap.containsKey(block))
			{
				float currentheat = HeatModifier.instance.blockHeatMap.get(block);
				float proximity = (float)Math.sqrt(player.getPositionVector().squareDistanceTo(new Vec3d(position)));
				currentheat *= (float)(ModConfig.HEAT.gaussScaling / (Math.pow(proximity, 2) + ModConfig.HEAT.gaussScaling));
				if(currentheat > heat) heat = currentheat; // Use only the maximum value
			}
		}
		
		return current + heat;
	}
}
