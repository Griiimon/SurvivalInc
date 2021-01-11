package enginecrafter77.survivalinc.stats.impl;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Range;

import enginecrafter77.survivalinc.SurvivalInc;
import enginecrafter77.survivalinc.client.Thermometer;
import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.debug.HeatDebugCommand;
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
import enginecrafter77.survivalinc.strugglecraft.TraitModule;
import enginecrafter77.survivalinc.strugglecraft.TraitModule.TRAITS;
import enginecrafter77.survivalinc.strugglecraft.WoolArmor;
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
import net.minecraft.util.text.TextComponentString;
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
	
	public static Map<String, ItemArmor.ArmorMaterial> customArmorTypes= new HashMap<String, ItemArmor.ArmorMaterial>();
	
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
		this.exchangerate.add(HeatModifier::applyBedInsulation);
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
		
		customArmorTypes.put("WOOL", WoolArmor.woolMaterial);
		
		// Armor heat isolation
		for(String entry : ModConfig.HEAT.armorMaterialConductivity)
		{
			int separator = entry.lastIndexOf(' ');
			String targetName= entry.substring(0, separator).toUpperCase();
			ItemArmor.ArmorMaterial target;
			if(customArmorTypes.containsKey(targetName))
				target= customArmorTypes.get(targetName);
			else
				target = ItemArmor.ArmorMaterial.valueOf(targetName);
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
		if(player.isCreative() || player.isSpectator() || player.isDead/* || player.world.isRemote*/) return;
		
		if(player.world.isRemote && !Util.thisClientOnly(player))
			return;

		
		float target;

		Biome biome = player.world.getBiome(player.getPosition());
		target = biome.getTemperature(player.getPosition());

		if(!Util.isDaytime(player))
		{
			if(!biome.isHighHumidity() && !biome.canRain())	// deserts/mesas
				target= 0f;
			else
				target-= ModConfig.HEAT.nightTemperatureDrop;
		}
			
		if(player.isSprinting())
			target+= ModConfig.HEAT.runningTemperatureIncrease;
		
		if(Util.isInSun(player))
			target+= ModConfig.HEAT.sunWarmth;
		
		if(player.posY < player.world.getSeaLevel()) // Cave
		{
			// temperature gradually changes 
			float depthThreshold= 30f;
			target = (float)Util.lerp(target, (float)ModConfig.HEAT.caveTemperature, (float)Math.min((player.world.getSeaLevel() - player.posY) / depthThreshold, 1f));   
		}
		else
		if(player.posY > player.world.getSeaLevel() + 25) // Mountain
		{
			target-= (player.posY - (player.world.getSeaLevel() + 25))*0.01f; 
		}
			
		if(player.isInWater())
			target-=0.2f;

		
		if(target < -0.2F) target = -0.2F;
		if(target > 1.5F) target = 1.5F;
		
		if(target < 0.4f && player.isPlayerSleeping())
			target+= Math.min(0.4f - target, 0.1f);
		
		target = targettemp.apply(player, target * (float)ModConfig.HEAT.tempCoefficient);
		

		SimpleStatRecord heat = (SimpleStatRecord)record;

		if(Util.thisClientOnly(player))
		{
			Thermometer.value= Math.max(0f, Math.min(2f*(float)ModConfig.HEAT.tempCoefficient, target+0.2f*(float)ModConfig.HEAT.tempCoefficient))/(2f*(float)ModConfig.HEAT.tempCoefficient);
		
			if(HeatDebugCommand.enabled && player.world.getTotalWorldTime() % 40 == 0)
			{
				player.sendMessage(new TextComponentString("Target: "+target+" , Current: "+heat.getValue()));
			}
			
		}
			
		
		float difference = Math.abs(target - heat.getValue());
		float rate = difference * (float)ModConfig.HEAT.heatExchangeFactor;
		rate = this.exchangerate.apply(player, rate);

		

		
		// Apply the "side effects"
//		this.consequences.apply(heat, player);

		boolean isColdResistant= TraitModule.instance.HasTrait(player, TRAITS.WARM);
		
		float coldResistance= isColdResistant ? TraitModule.instance.TraitTier(player, TRAITS.WARM) + 1 : 0f; 
		
		float sanityDrop= 0f;
		
		if(heat.getValue() < 10f - coldResistance)
		{
			if(Util.chance(1f))
				new DamageStatEffect(HYPOTHERMIA, (float)ModConfig.HEAT.damageAmount, 10).apply(heat, player);
			if(isColdResistant)
				TraitModule.instance.UsingTrait(player, TRAITS.WARM);
			sanityDrop+= 0.01f;
		}
		if(heat.getValue() < 20f - coldResistance)
		{
			new PotionStatEffect(MobEffects.MINING_FATIGUE, 0).apply(heat, player);
			if(isColdResistant)
				TraitModule.instance.UsingTrait(player, TRAITS.WARM);
			sanityDrop+= 0.005f;
		}
		if(heat.getValue() < 25f - coldResistance)
		{
			new PotionStatEffect(MobEffects.WEAKNESS, 0).apply(record, player);
			if(isColdResistant)
				TraitModule.instance.UsingTrait(player, TRAITS.WARM);
			if(Util.chance(0.5f))
				player.world.playSound(player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_BREATH, SoundCategory.AMBIENT, 1f, 1, false);
			sanityDrop+= 0.001f;
		}
		
		if(sanityDrop > 0f)
		{
			SanityTendencyModifier.instance.addToTendency(-sanityDrop, "Cold", player);
			sanityDrop= 0f;
		}
		
		
		if(heat.getValue() > 110f)
		{
			onHighTemperature(record, player);
			if(Util.chance(1f))
				player.world.playSound(player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_BREATH, SoundCategory.AMBIENT, 1f, 1, false);
//			SanityTendencyModifier.instance.addToTendency(-0.01f, "Very hot", player);
		}
//		else
		if(heat.getValue() > 80f)
		{
			SanityTendencyModifier.instance.addToTendency(-(heat.getValue() - 80f)*0.001f, "Hot", player);
		}
		
		if(heat.getValue() > 45f && heat.getValue()<55f)
			SanityTendencyModifier.instance.addToTendency(0.001f, "Perfect temperature", player);
			
		
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
			if(Util.chance(1f))
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

	public static float applyBedInsulation(EntityPlayer player, float current)
	{
		if(player.isPlayerSleeping())
			return current * 0.7f;
		return current * 1f;
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
				float proximity = (float)Math.sqrt(player.getPositionVector().squareDistanceTo(new Vec3d(position).add(new Vec3d(0.5, 0.5, 0.5))));
				currentheat *= (float)(ModConfig.HEAT.gaussScaling / (Math.pow(proximity, 2) + ModConfig.HEAT.gaussScaling));
				if(currentheat > heat) heat = currentheat; // Use only the maximum value
			}
		}
		
		return current + heat;
	}
	
	public float getPlayerTemperature(EntityPlayer player)
	{
		StatTracker stats = player.getCapability(StatCapability.target, null);
		SimpleStatRecord heat = stats.getRecord(HeatModifier.instance);

		return heat.getValue();
	}
}
