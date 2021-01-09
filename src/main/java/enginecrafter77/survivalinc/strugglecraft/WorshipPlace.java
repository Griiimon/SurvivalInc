package enginecrafter77.survivalinc.strugglecraft;

import net.minecraft.util.math.BlockPos;

public class WorshipPlace {

	BlockPos center;
	WORSHIP_PLACE_TYPE type;
	int value;
	

	public WorshipPlace(BlockPos pos, int v)
	{
		center= pos;
		value= v;
	}
	
	public BlockPos getPosition()
	{
		return center;
	}
	
	public int getValue()
	{
		return value;
	}
	
	public enum WORSHIP_PLACE_TYPE
	{
		STONE_CIRCLE
	}
}

