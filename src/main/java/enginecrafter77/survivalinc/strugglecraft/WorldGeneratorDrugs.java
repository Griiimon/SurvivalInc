package enginecrafter77.survivalinc.strugglecraft;

import java.util.Random;

import enginecrafter77.survivalinc.ModBlocks;
import enginecrafter77.survivalinc.util.Util;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class WorldGeneratorDrugs extends WorldGenerator
{
    
    public WorldGeneratorDrugs()
    {
    }

    
    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        if(rand.nextInt() % 100 < 5)
        {
    	
        	BlockDrugsCrop[] arr= new BlockDrugsCrop[] {(BlockDrugsCrop)ModBlocks.WEED_CROP.get(), (BlockDrugsCrop)ModBlocks.TOBACCO_CROP.get(), (BlockDrugsCrop)ModBlocks.COCA_CROP.get(), (BlockDrugsCrop)ModBlocks.POPPY_CROP.get()}; 
        	
	    	BlockDrugsCrop crops = arr[Util.rnd(arr.length)]; 
	        
	        for (int i = 0; i < 2; ++i)
	        {
	            BlockPos blockpos = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));
	
	            if (worldIn.isAirBlock(blockpos) && (!worldIn.provider.isNether() || blockpos.getY() < 255) && Blocks.RED_FLOWER.canBlockStay(worldIn, blockpos, Blocks.RED_FLOWER.getDefaultState()))
	            {
//	                worldIn.setBlockState(blockpos, crops.getDefaultState(), 2);
	                worldIn.setBlockState(blockpos, crops.getGrownState(), 2);
	                System.out.println("DEBUG: spawned drugs at "+blockpos.toString());
	            }
	        }
        }
        return true;
    }
	
	
	
	
}

