package enginecrafter77.survivalinc.block;

import enginecrafter77.survivalinc.strugglecraft.WorshipPlace;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class TileEntityMonolith extends TileEntity{

	BlockPos circleCenter;
	boolean active=false;
	
	
	public TileEntityMonolith()
	{
		markDirty();
	}
	
	public void Set(BlockPos center)
	{
		active= true;
		circleCenter= center;
		markDirty();
	}
	
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        if(compound.hasKey("center"))
        {
	        int[] arr= compound.getIntArray("center");
	        
	        circleCenter= new BlockPos(arr[0], arr[1], arr[2]);
        }
        
        active= compound.getBoolean("active");
        
    }

    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
    
		int[] arr= new int[3];
		
		int i= 0;
		arr[i++]= circleCenter.getX();
		arr[i++]= circleCenter.getY();
		arr[i++]= circleCenter.getZ();
		
		compound.setIntArray("center", arr);

		compound.setBoolean("active", active);
		
        return compound;
    }
}
