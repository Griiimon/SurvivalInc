package enginecrafter77.survivalinc.util;

import javax.vecmath.Point3d;

import enginecrafter77.survivalinc.ModItems;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
		if(!isDaytime(player))
			return false;
		
//		if(player.world.isRainingAt(player.getPosition()))
		if(player.world.isRaining())
			return false;
		
		
		boolean mainHand= player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() == ModItems.UMBRELLA.getItem();
		boolean offHand= player.getHeldItemOffhand() != null && player.getHeldItemOffhand().getItem() == ModItems.UMBRELLA.getItem();

		if(mainHand || offHand)
			return false;
		
		ItemStack hat= player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		
		if(hat != null && hat.getItem() == ModItems.STRAW_HAT.getItem())
			return false;
		
//		if(player.world.getLight(new BlockPos(player.getPositionVector().add(0D, player.getEyeHeight(), 0D))) < 13)
//			return false;
		
		return player.world.canBlockSeeSky(player.getPosition());
	}
	
	public static boolean isSwimming(EntityPlayer player)
	{
		if(player.motionX == 0 && player.motionZ == 0)
			return false;
		
		// boat
		if(player.isRiding())
			return false;
		
		if(player.world.getBlockState(player.getPosition().down()).getMaterial() != Material.WATER)
			return false;
		
		return player.isInWater();
	}
	
	public static int rnd(int r)
	{
		return (int)Math.floor(Math.random()*r);
	}

	public static int rnd(World world, int r)
	{
		return (int)Math.floor(world.rand.nextFloat()*r);
	}

	
	public static int rnd(int min, int max)
	{
		return (int)Math.floor(Math.random()*(max-min)+min);
	}

	public static int rnd(World world, int min, int max)
	{
		return (int)Math.floor(world.rand.nextFloat()*(max-min)+min);
	}

	
	public static float rnd(float min, float max) {
		return (float)(Math.random()*(max-min)+min);
	}
	
	public static float rndf(float r)
	{
		return (float)Math.random()*r;
	}
	
	public static float randf()
	{
		return (float)Math.random();
	}
	
	public static boolean chance(float f)
	{
		return Math.random() * 100 < f;
	}

	public static boolean chance(World world, float f)
	{
		return world.rand.nextFloat() * 100 < f;
	}

	
	public static boolean chanced(double d)
	{
		return Math.random() * 100 < d;
	}
	
	public static double distance(BlockPos pos1, BlockPos pos2)
	{
		Point3d p1= new Point3d(pos1.getX(), pos1.getY(), pos1.getZ());
		Point3d p2= new Point3d(pos2.getX(), pos2.getY(), pos2.getZ());
		
		return p1.distance(p2);
	}

	public static double distance(EntityPlayer player, BlockPos pos2)
	{
		Point3d p1= new Point3d(player.posX, player.posY, player.posZ);
		Point3d p2= new Point3d(pos2.getX()+0.5, pos2.getY()+0.5, pos2.getZ()+0.5);
		
		return p1.distance(p2);
	}

	
	public static boolean thisClientOnly(EntityPlayer player)
	{
		if(!player.world.isRemote)
			return false;
		Minecraft client = Minecraft.getMinecraft();
		if(player == client.player) 
			return true;
		return false;
	}
	
}
