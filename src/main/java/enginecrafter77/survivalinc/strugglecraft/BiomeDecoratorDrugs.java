package enginecrafter77.survivalinc.strugglecraft;

import java.util.Random;

import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BiomeDecoratorDrugs/* extends BiomeDecorator{

	WorldGeneratorDrugs genDrugs;
	
	
	
	  @Override
	    public void decorate(World worldIn, Random random, Biome biome, BlockPos pos)
	    {
	        if (decorating)
	        {
	            throw new RuntimeException("Already decorating");
	        }
	        else
	        {
	            chunkPos = pos;
	            genDecorations(biome, worldIn, random);
	            decorating = false;
	        }
	    }
	    

	    @Override
	    protected void genDecorations(Biome biomeIn, World worldIn, Random random)
	    {
//	        MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Pre(worldIn, random, chunkPos));

	        generateOres(worldIn, random);
	        generateFlowers(worldIn, biomeIn, random, chunkPos);

//	        MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Post(worldIn, random, chunkPos));
	    }
	    
	    
	    private void generateFlowers(World worldIn, Biome biomeIn, Random random, BlockPos chunkPos)
	    {
//	        if(TerrainGen.decorate(worldIn, random, chunkPos, DecorateBiomeEvent.Decorate.EventType.FLOWERS))
	        for (int numFlowersGenerated = 0; numFlowersGenerated < flowersPerChunk; ++numFlowersGenerated)
	        {
	            int flowerX = random.nextInt(16) + 8;
	            int flowerZ = random.nextInt(16) + 8;
	            int yRange = worldIn.getHeight(chunkPos.add(flowerX, 0, flowerZ)).getY() + 32;

	            genDrugs = new WorldGeneratorDrugs();
	            
	            if (yRange > 0)
	            {
	                int flowerY = random.nextInt(yRange);
	                BlockPos flowerBlockPos = chunkPos.add(flowerX, flowerY, flowerZ);
	                flowerGen.generate(worldIn, random, flowerBlockPos);
	            }
	        }
	    }
*/
{
	@SubscribeEvent
	public void decorate(DecorateBiomeEvent.Decorate event) {
		World world = event.getWorld();
		Biome biome = world.getBiomeForCoordsBody(event.getPos());
		Random rand = event.getRand();
		if ((biome == Biomes.PLAINS) && event.getType() == DecorateBiomeEvent.Decorate.EventType.GRASS) {
			if (rand.nextDouble() > 0.1) return;
			int x = rand.nextInt(16) + 8;
			int y = rand.nextInt(16) + 8;
			WorldGeneratorDrugs gen = new WorldGeneratorDrugs();
			gen.generate(world, rand, world.getHeight(event.getPos().add(x, 0, y)));
		}
}

	
}
