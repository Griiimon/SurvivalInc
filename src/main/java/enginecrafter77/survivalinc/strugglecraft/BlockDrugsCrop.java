package enginecrafter77.survivalinc.strugglecraft;

import java.util.Random;

import enginecrafter77.survivalinc.ModItems;
import enginecrafter77.survivalinc.SurvivalInc;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockDrugsCrop extends BlockCrops{

	Item seedItem, cropItem;
	
	public BlockDrugsCrop(String name/*ModItems crop, Item seed*/)
	{
		super();
		//		super(Blocks.ICE, Blocks.WATER);

		this.setRegistryName(new ResourceLocation(SurvivalInc.MOD_ID, name));
		this.setTranslationKey(name);

		
	}
	
	public void postInit(Item seed, Item crop)
	{
		seedItem= seed;
		cropItem= crop;
		
	}
	
	@Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        super.updateTick(worldIn, pos, state, rand);

		if(worldIn.getBiome(pos).getDefaultTemperature() < 0.2f)
			return;

        
        if (worldIn.getLightFromNeighbors(pos.up()) >= 9)
        {
            int i = this.getAge(state);

            if (i < this.getMaxAge())
            {
                float f = getGrowthChance(this, worldIn, pos);
                
                if(worldIn.isRainingAt(pos))
                	f*= 3;
                
//                if (rand.nextInt((int)(25.0F / f) + 1) == 0)
               	if (rand.nextInt((int)(100.0F / f) + 1) == 0)
                {
                    worldIn.setBlockState(pos, this.withAge(i + 1), 2);
                }
            }
        }
    }
	

	@Override
    protected int getBonemealAgeIncrease(World worldIn)
    {
        return 1;
    }

	@Override
    protected Item getSeed()
    {
        return seedItem;
    }

	@Override
    protected Item getCrop()
    {
        return cropItem;
    }
	
	public IBlockState getGrownState()
	{
		return blockState.getBaseState().withProperty(this.getAgeProperty(), Integer.valueOf(7));
	}
}
