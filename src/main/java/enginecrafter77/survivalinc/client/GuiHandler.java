package enginecrafter77.survivalinc.client;

import enginecrafter77.survivalinc.block.ContainerCooler;
import enginecrafter77.survivalinc.block.ContainerHeater;
import enginecrafter77.survivalinc.block.TileEntityCooler;
import enginecrafter77.survivalinc.block.TileEntityHeater;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;


public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityHeater) {
            return new ContainerHeater(player.inventory, (TileEntityHeater) te);
        }
        else
        if (te instanceof TileEntityCooler) {
            return new ContainerCooler(player.inventory, (TileEntityCooler) te);
        }
        	
        	
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityHeater) {
            TileEntityHeater containerTileEntity = (TileEntityHeater) te;
            return new GuiHeater(containerTileEntity, new ContainerHeater(player.inventory, containerTileEntity));
        }
        if (te instanceof TileEntityCooler) {
            TileEntityCooler containerTileEntity = (TileEntityCooler) te;
            return new GuiCooler(containerTileEntity, new ContainerCooler(player.inventory, containerTileEntity));
        }

        return null;
    }
}