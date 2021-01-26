package enginecrafter77.survivalinc.strugglecraft;

import java.util.HashMap;

import enginecrafter77.survivalinc.util.Util;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FishChunk {
	static final int START_POPULATION= 100;
	static final int REGENERATION_TICKS= 15*60*20;
	
	
	public int population;
	public long lastUpdate;
	
	
	// TODO  SAVE INFO IN CHUNKS NBT!!!
	
	public FishChunk(World world)
	{
		population= START_POPULATION;
		lastUpdate= world.getTotalWorldTime();
	}
	
	public void substract(World world, ChunkCoords coords, HashMap<ChunkCoords, FishChunk> fishPopulation)
	{
		System.out.println("DEBUG: FishChunk.substract("+coords+") current pop: "+population);

		update(world, coords, fishPopulation);
		
		if(population > 0)
			population-= 10;
		
		System.out.println("DEBUG: FishChunk.substract("+coords+") new pop: "+population);
	}
	
	public void update(World world, ChunkCoords coords, HashMap<ChunkCoords, FishChunk> fishPopulation)
	{
		long ticksPassed= world.getTotalWorldTime() - lastUpdate;
		
		int regenerationSteps= (int)(ticksPassed / REGENERATION_TICKS);

		System.out.println("DEBUG: FishChunk.update("+coords+") ticksPassed: "+ticksPassed+", steps: "+regenerationSteps);

		
		
		for(int i= 0; i < regenerationSteps; i++)
		{
			
			float environment= scanEnvironment(world, coords, fishPopulation);
			

			
			if(population <= 0)
				population= (int) (environment * 10f);
			else
				population*= 1f + environment;

			System.out.println("DEBUG: regStep #"+i+" env factor: "+environment+", new pop: "+population);

		}
		
		if(population > START_POPULATION)
			population= START_POPULATION;
		
		lastUpdate+= regenerationSteps * REGENERATION_TICKS;
		
		System.out.println("DEBUG: set last Update: "+lastUpdate+" (current tick "+world.getTotalWorldTime()+")");

	}
	
	float scanEnvironment(World world, ChunkCoords coords, HashMap<ChunkCoords, FishChunk> fishPopulation)
	{
		float factor= 0f;

		System.out.println("DEBUG: scanEnvironment()");
		
		for(int x= coords.posX - 1; x < coords.posX + 2; x++)
			for(int z= coords.posZ - 1; z < coords.posZ + 2; z++)
//				if(x != coords.posX || z != coords.posZ)
				{
					int bx = x<<4;
					int bz = z<<4;
					
					int water= 0;
					
					for(int i= 0; i < 5; i++)
					{
						BlockPos pos= new BlockPos(bx+Util.rnd(world, 16), world.getSeaLevel(), bz+Util.rnd(world, 16));
						
						while(world.getBlockState(pos).getBlock() == Blocks.WATER || world.getBlockState(pos).getBlock() == Blocks.FLOWING_WATER)
						{
							pos= pos.down();
							water++;
						}
						System.out.println("DEBUG: "+water+" deep water at "+pos);

					}
					
					
					factor+= Math.pow(water, 2) / 1000f;
				}
		
		return Math.min(1f, factor);
	}
}
