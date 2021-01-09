package enginecrafter77.survivalinc.block;




import enginecrafter77.survivalinc.config.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBoat;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TileEntityCooler extends TileEntity implements ITickable
{
	
	public static final int SIZE = 1;

    private int furnaceBurnTime= 0;
    private int currentItemBurnTime=0;

    
	
    // This item handler will hold our nine inventory slots
    private ItemStackHandler itemStackHandler = new ItemStackHandler(SIZE) {
    	@Override
        protected void onContentsChanged(int slot) {
            TileEntityCooler.this.markDirty();
        }
    };
    



	@Override
    public void update()
    {
        boolean flag = this.isCooling();
        boolean flag1 = false;

        if (this.isCooling())
        {
            --this.furnaceBurnTime;
        }

        if (!this.world.isRemote)
        {
            ItemStack itemstack = itemStackHandler.getStackInSlot(0);

            if (this.isCooling() || !itemstack.isEmpty())
            {
                if (!this.isCooling())
                {
                    this.furnaceBurnTime = (int)(getItemBurnTime(itemstack) * 1f / ModConfig.HEAT.coolerFuelUsage);
                    this.currentItemBurnTime = this.furnaceBurnTime;

                    if (this.isCooling())
                    {
                        flag1 = true;

                        if (!itemstack.isEmpty())
                        {
                            Item item = itemstack.getItem();
                            itemstack.shrink(1);

                            if (itemstack.isEmpty())
                            {
                                Item item1 = item.getContainerItem();
                                itemStackHandler.setStackInSlot(0, item1 == null ? ItemStack.EMPTY : new ItemStack(item1));
                            }
                        }
                    }
                }
            }
            if (flag != this.isCooling())
            {
                flag1 = true;
                BlockCooler.setState(this.isCooling(), this.world, this.pos);
            }
            
            
        }

        if (flag1)
            this.markDirty();
        
    }
    
 
    

    public static int getItemBurnTime(ItemStack stack)
    {
        if (stack.isEmpty())
            return 0;
        return 5000;
    }
    
    public static boolean isItemFuel(ItemStack stack)
    {
        return stack.getItem() == Items.SNOWBALL;
    }
    
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
            return isItemFuel(stack); // || SlotFurnaceFuel.isBucket(stack) && itemstack.getItem() != Items.BUCKET;
    }

    
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction)
    {
        return this.isItemValidForSlot(index, itemStackIn);
    }

    public boolean isCooling()
    {
        return this.furnaceBurnTime > 0;
    }
    
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("items")) {
            itemStackHandler.deserializeNBT((NBTTagCompound) compound.getTag("items"));
        }
        currentItemBurnTime= compound.getInteger("itemburntime");
        furnaceBurnTime= compound.getInteger("burntime");
    }

    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("items", itemStackHandler.serializeNBT());
        compound.setInteger("itemburntime", currentItemBurnTime);
        compound.setInteger("burntime", furnaceBurnTime);
        return compound;
    }

    public boolean canInteractWith(EntityPlayer playerIn) {
        // If we are too far away from this tile entity you cannot use it
        return !isInvalid() && playerIn.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64D;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemStackHandler);
        }
        return super.getCapability(capability, facing);
    }





    
}