package enginecrafter77.survivalinc.block;

import enginecrafter77.survivalinc.SurvivalInc;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;

public class BlockMonumentStructure extends Block{

	public BlockMonumentStructure() {
		super(Material.IRON);

		setRegistryName(new ResourceLocation(SurvivalInc.MOD_ID, "monument_structure"));
		this.setHardness(10f);

	}
	
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
}
