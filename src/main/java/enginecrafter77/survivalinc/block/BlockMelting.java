package enginecrafter77.survivalinc.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

public class BlockMelting extends Block {
	public static final PropertyInteger MELTPHASE = PropertyInteger.create("meltphase", 0, 3);
	
	public static final float FREEZING_TEMPERATURE = 0.15F;
	
	public final Block from, to;
	
	public BlockMelting(Block from, Block to, boolean autotick)
	{
		super(from.getDefaultState().getMaterial());
		this.setTickRandomly(autotick);
		this.from = from;
		this.to = to;
	}
	
	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] {MELTPHASE});
	}
	
	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(MELTPHASE);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return this.getDefaultState().withProperty(MELTPHASE, meta);
	}

	@Override
	public void updateTick(World world, BlockPos position, IBlockState state, Random rng)
	{
		world.profiler.startSection("melting");
		
		boolean melts = !BlockMelting.isFreezingAt(world, position);
		int phase = state.getValue(MELTPHASE) + (melts ? 1 : -1);
		if(MELTPHASE.getAllowedValues().contains(phase))
		{
			state = state.withProperty(MELTPHASE, phase);
			world.setBlockState(position, state, 2);
		}
		else
		{
			Block target = melts ? this.to : this.from;
			world.setBlockState(position, target.getDefaultState(), 2);
		}
		
		world.profiler.endSection();
	}
	
	public static boolean isFreezingAt(World world, BlockPos position)
	{
		return world.getBiome(position).getTemperature(position) <= BlockMelting.FREEZING_TEMPERATURE;
	}
	
	public static boolean isFreezingAt(Chunk chunk, BlockPos position)
	{
		World world = chunk.getWorld();
		Biome biome = chunk.getBiome(position, world.getBiomeProvider());
		return biome.getTemperature(chunk.getPos().getBlock(position.getX(), position.getY(), position.getZ())) <= BlockMelting.FREEZING_TEMPERATURE;
	}
}
