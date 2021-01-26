package enginecrafter77.survivalinc.strugglecraft;

import net.minecraft.util.math.BlockPos;

public class WorshipPlace {

	BlockPos center;
	public WORSHIP_PLACE_TYPE type;
	int value;
	int height;
	

	public WorshipPlace(BlockPos pos, int v)
	{
		center= pos;
		value= v;
		type= WORSHIP_PLACE_TYPE.STONE_CIRCLE;
	}

	public WorshipPlace(BlockPos pos, int v, int h)
	{
		center= pos;
		value= v;
		height= h;
		type= WORSHIP_PLACE_TYPE.MONUMENT;
	}

	
	public WorshipPlace(WORSHIP_PLACE_TYPE t, BlockPos pos, int v, int h)
	{
		center= pos;
		value= v;
		height= h;
		type= t;
	}

	
	public BlockPos getPosition()
	{
		return center;
	}
	
	public int getValue()
	{
		return value;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public enum WORSHIP_PLACE_TYPE
	{
		STONE_CIRCLE,
		MONUMENT
	}
}

