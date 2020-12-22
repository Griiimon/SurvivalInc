package enginecrafter77.survivalinc.strugglecraft;

import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.stats.impl.SanityTendencyModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class Tweaks {

	public static Tweaks instance= new Tweaks();
	
	Vec3d oldpos= null;
	
	
	public void init()
	{
		MinecraftForge.EVENT_BUS.register(Tweaks.class);
	}

	@SubscribeEvent
	public static void onBlockStartBreak(PlayerEvent.BreakSpeed event){
	    event.setNewSpeed((float)(event.getOriginalSpeed() * ModConfig.TWEAKS.digspeedFactor));
	
	}

	
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if(event.side != Side.CLIENT) return;
		
		
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
						if(player.isPushedByWater())
							SanityTendencyModifier.instance.addToTendency(0.02f, "Sliding", player);
						else
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

}
