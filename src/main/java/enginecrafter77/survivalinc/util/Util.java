package enginecrafter77.survivalinc.util;

import net.minecraft.entity.player.EntityPlayer;

public class Util {

	public static double lerp(float a, float b, float f) 
	{
	    return ((a * (1.0 - f)) + (b * f));
	}
	
	//because player.world.isDaytime won't work on clients
	public static boolean isDaytime(EntityPlayer player)
	{
		return player.world.getWorldTime() % 24000 < 12000;
	}
	
}
