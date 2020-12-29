package enginecrafter77.survivalinc.strugglecraft;

import java.util.Random;
import java.util.UUID;

import enginecrafter77.survivalinc.SurvivalInc;
import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.stats.impl.SanityTendencyModifier;
import enginecrafter77.survivalinc.util.Util;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent.CropGrowEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;

public class Tweaks {

	
	public static String maxHealthModifierUUID = "2F28C409-EA90-4E54-AD57-13F3D92F68B2"; 
	public static String maxHealthModifierName = SurvivalInc.MOD_ID+".MaxHealthModifier"; 


	
	public static Tweaks instance= new Tweaks();
	
	Vec3d oldpos= null;
	
	
	public void init()
	{
		MinecraftForge.EVENT_BUS.register(Tweaks.class);
	}

	
	
	@SubscribeEvent
	public static void onSpawn(EntityJoinWorldEvent event)
	{
		if(event.getWorld().isRemote)
			return;
		
		Entity ent= event.getEntity();

		
		if(ent instanceof EntityAnimal && !ent.isDead)
		{
//			System.out.println("Patching "+ent.getName()+" health");
			EntityAnimal animal= (EntityAnimal) ent;
			AttributeModifier attr= new AttributeModifier(UUID.fromString(maxHealthModifierUUID), maxHealthModifierName, ((EntityAnimal) ent).getHealth() * ModConfig.TWEAKS.animalHealthMultiplier, 0);
			if(!animal.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).hasModifier(attr))
				animal.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(attr);
		}

	}
	
	@SubscribeEvent
	public static void onBlockStartBreak(PlayerEvent.BreakSpeed event){
	    event.setNewSpeed((float)(event.getOriginalSpeed() * ModConfig.TWEAKS.digspeedFactor));
	
	}

	
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if(event.side != Side.CLIENT) return;
		
//		event.player.world.getTotalWorldTime()
		
		EntityPlayer player= event.player;
		if(player.isSprinting())
			player.getFoodStats().addExhaustion((float)ModConfig.TWEAKS.runExhaustion);
		
		if(player.isHandActive())
			player.getFoodStats().addExhaustion((float)ModConfig.TWEAKS.handExhaustion);
		
		if(player.isInWater())
		{
			Vec3d pos= player.getPositionVector();
		
			if(instance.oldpos != null)
			{
				Vec3d deltapos= pos.subtract(instance.oldpos);
				// check for possible teleport/respawn position change etc..
				if(deltapos.length() < 1)
				{
					// min threshold for position change to be considered moving/swimming
					if(deltapos.length()> 0.01f)
					{
//						if(player.isPushedByWater())
//							SanityTendencyModifier.instance.addToTendency(0.02f, "Sliding", player);
//						else
							//swimming
							player.getFoodStats().addExhaustion((float)ModConfig.TWEAKS.swimExhaustion);
					}
				}
			}
			instance.oldpos= pos;
		}
	}
	
	@SubscribeEvent
	public static void onJump(LivingJumpEvent event)
	{
		if(event.getEntity() instanceof EntityPlayer)
 			((EntityPlayer)event.getEntity()).getFoodStats().addExhaustion((float)ModConfig.TWEAKS.jumpExhaustion);
		
	}
	
	@SubscribeEvent
	public static void onCropGrowth(CropGrowEvent.Pre event)
	{
		if(Util.chance(90f))
			event.setResult(Result.DENY);
	}
	
	// does this even has any effect?
	@SubscribeEvent
	public static void onDrop(HarvestDropsEvent event) {
	    if (event.isSilkTouching()) {
	        return;
	    }

	    final IBlockState state = event.getState();

	    // Skip Air or Error
	    if (state == null || state.getBlock() == null) {
	        return;
	    }


	    if (state.getBlock() instanceof BlockTallGrass) {
	    	if(Util.chance(90f))
	    		event.getDrops().clear();
	    }
	}
	 
	
	
	public static void postInit()
	{
		//reduce Tool durability
		
		ForgeRegistries.ITEMS.getValuesCollection()
        .stream()
        .filter(x -> {
            try {
                return x.getMaxDamage(null) != 0;
            } catch (Exception e) {
                return false;
            }
        })
        .forEach(x -> {
            int maxDamage = x.getMaxDamage(null);
            int newDamage = Math.max(1, (int)(maxDamage * 0.2f));
            if(x instanceof ItemSword)
            	newDamage*= 2;
            
            x.setMaxDamage(newDamage);
        });
		
	}

}
