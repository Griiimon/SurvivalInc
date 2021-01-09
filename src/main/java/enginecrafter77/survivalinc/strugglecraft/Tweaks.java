package enginecrafter77.survivalinc.strugglecraft;


import java.util.UUID;

import enginecrafter77.survivalinc.ModItems;
import enginecrafter77.survivalinc.SurvivalInc;
import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.season.Season;
import enginecrafter77.survivalinc.stats.impl.SanityTendencyModifier;
import enginecrafter77.survivalinc.util.Util;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
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

	public static final PropertyEnum<BlockTallGrass.EnumType> GRASS_TYPE = PropertyEnum.<BlockTallGrass.EnumType>create("type", BlockTallGrass.EnumType.class);
	
	public static Tweaks instance= new Tweaks();
	
	
	public void init()
	{
		MinecraftForge.EVENT_BUS.register(Tweaks.class);
	}

	
	// conflict with scaling health mod?
	@SubscribeEvent
	public static void onSpawn(EntityJoinWorldEvent event)
	{
//		if(event.getWorld().isRemote)
//			return;
		
		Entity ent= event.getEntity();

		
		if(!event.getWorld().isRemote && (ent instanceof EntityAnimal || ent instanceof EntityWaterMob) && !ent.isDead)
		{
//			System.out.println("Patching "+ent.getName()+" health");
//			EntityAnimal animal= (EntityAnimal) ent;
			EntityLiving animal= (EntityLiving)ent;
//			AttributeModifier attr= new AttributeModifier(UUID.fromString(maxHealthModifierUUID), maxHealthModifierName, ((EntityAnimal) ent).getHealth() * ModConfig.TWEAKS.animalHealthMultiplier, 0);
			AttributeModifier attr= new AttributeModifier(UUID.fromString(maxHealthModifierUUID), maxHealthModifierName, 50f, 0);
			if(!animal.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).hasModifier(attr))
			{
				animal.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(attr);
				animal.heal(100f);
				animal.setHealth(animal.getMaxHealth());
			}
		}

		// uncomment only after clients have set enhancedFishing to false in cfg
/*		if(ModConfig.TWEAKS.enhancedFishing && ent instanceof EntityFishHook && !(ent instanceof MyFishHook))//ent.getClass().equals(EntityFishHook.class)) 
		{
            EntityPlayer player = ((EntityFishHook) ent).getAngler();
            
            if(!event.getWorld().isRemote)
			{
	            ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
	            if (stack.getItem() != Items.FISHING_ROD) {
	                stack = player.getHeldItem(EnumHand.OFF_HAND);
	            }
	            ent.setDead();
	
	            MyFishHook hook = new MyFishHook(event.getWorld(), player);
	
	            int speed = EnchantmentHelper.getFishingSpeedBonus(stack);
	            if (speed > 0) {
	                hook.setLureSpeed(speed);
	            }
	            int luck = EnchantmentHelper.getFishingLuckBonus(stack);
	            if (luck > 0) {
	                hook.setLuck(luck);
	            }
	            event.getWorld().spawnEntity(hook);
			}
            else
            {
	            MyFishHook hook = new MyFishHook(event.getWorld(), player, ent.posX, ent.posY, ent.posZ);
	            EntityTracker.updateServerPosition(hook, ent.posX, ent.posY, ent.posZ);
	   	        hook.setEntityId(ent.getEntityId());
   	            hook.setUniqueId(ent.getUniqueID());
	     
	            ((WorldClient)event.getWorld()).addEntityToWorld(hook.getEntityId(), hook);
            }
            
	            event.setCanceled(true);
			
        }
*/
		
	}

	
	@SubscribeEvent
	public static void onBlockStartBreak(PlayerEvent.BreakSpeed event){
	    event.setNewSpeed((float)(event.getOriginalSpeed() * ModConfig.TWEAKS.digspeedFactor));
	
	}

	// TODO move to server, adding exhaustion has no effect client-side!?
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		
		if(event.side == Side.SERVER)
		{
			if(Util.chance(event.player.world, 0.01f))
			{
				ItemStack headGear= event.player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
				
				if(headGear != null && (headGear.getItem() == ModItems.HEADBAND.getItem() || headGear.getItem() == ModItems.SUPERIOR_HEADBAND.getItem()))
				{
					headGear.damageItem(1, event.player);				
				}
			}

			
		}
		
		
		if(event.side != Side.CLIENT) return;
		
//		event.player.world.getTotalWorldTime()
		
		EntityPlayer player= event.player;
		
		if(player.isPlayerSleeping())
			player.getFoodStats().addExhaustion((float)ModConfig.TWEAKS.sleepExhaustion);
		
		if(player.isSprinting())
			player.getFoodStats().addExhaustion((float)ModConfig.TWEAKS.runExhaustion);
		
		if(player.isHandActive())
			player.getFoodStats().addExhaustion((float)ModConfig.TWEAKS.handExhaustion);

		if(Util.isSwimming(player))
			player.getFoodStats().addExhaustion((float)ModConfig.TWEAKS.swimExhaustion);

	}

	@SubscribeEvent
	public static void onWorldTick(TickEvent.WorldTickEvent event) {
		
	
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
		float chance= 10f;
		
		if(event.getWorld().isRainingAt(event.getPos()))
			chance+= 40f;

		// sweet spot == 0
		//		Math.abs(event.getWorld().getBiome(event.getPos()).getTemperature(event.getPos()) - 0.5f))
		
		
		if(Util.chance(100-chance))
			event.setResult(Result.DENY);
	}
	

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


	    if (state.getBlock() instanceof BlockTallGrass) 
    	{
    		
    		event.getDrops().clear();

    		if (state.getValue(GRASS_TYPE) == BlockTallGrass.EnumType.FERN)
        		event.getDrops().add(new ItemStack(Items.WHEAT_SEEDS));
            else	 
            if (state.getValue(GRASS_TYPE) == BlockTallGrass.EnumType.GRASS && Util.chance(3f))
        		event.getDrops().add(new ItemStack(Items.WHEAT_SEEDS));
    	
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
