package enginecrafter77.survivalinc.util;

import net.minecraft.client.Minecraft;
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

	public static boolean isInSun(EntityPlayer player)
	{
		return player.world.canBlockSeeSky(player.getPosition());
	}
	
	public static int rnd(int r)
	{
		return (int)Math.floor(Math.random()*r);
	}
	
	public static float rndf(float r)
	{
		return (float)Math.random()*r;
	}
	
	public static boolean chance(float f)
	{
		return Math.random() * 100 < f;
	}

	public static boolean chanced(double d)
	{
		return Math.random() * 100 < d;
	}
	
	public static boolean thisClientOnly(EntityPlayer player)
	{
		Minecraft client = Minecraft.getMinecraft();
		if(player == client.player) 
			return true;
		return false;
	}

}
