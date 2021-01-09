package enginecrafter77.survivalinc.block;

import java.util.Collection;
import java.util.Random;

import com.google.common.base.Optional;

import enginecrafter77.survivalinc.ModBlocks;
import enginecrafter77.survivalinc.SurvivalInc;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;

import net.minecraft.stats.StatList;
//import net.minecraft.tileentity.TileEntityHeater;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;

public class BlockCooler extends Block implements ITileEntityProvider {
	public static final int GUI_ID = 2;

	private boolean isActive = false;

	public BlockCooler(boolean active) {
		super(Material.ROCK);
		setRegistryName(new ResourceLocation(SurvivalInc.MOD_ID, (active ? "active_" : "") + "cooler"));
		this.setHardness(3.5f);
		isActive= active;
	}

	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(ModBlocks.COOLER.get());
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		System.out.println("TILE ENTITY COOLER CREATED");
		return new TileEntityCooler();
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		// Only execute on the server
		if (worldIn.isRemote) {
			return true;
		}
		TileEntity te = worldIn.getTileEntity(pos);
		if (!(te instanceof TileEntityCooler)) {
			return false;
		}

		playerIn.openGui(SurvivalInc.instance, GUI_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (this.isActive) {
			double d0 = (double) pos.getX() + 0.5D;
			double d1 = (double) pos.getY() + rand.nextDouble() * 6.0D / 16.0D;
			double d2 = (double) pos.getZ() + 0.5D;
			double d3 = 0.52D;
			double d4 = rand.nextDouble() * 0.6D - 0.3D;

			if (rand.nextDouble() < 0.1D) {
				worldIn.playSound((double) pos.getX() + 0.5D, (double) pos.getY(), (double) pos.getZ() + 0.5D,
						SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
			}

			worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 - 0.52D, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
			worldIn.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, d0 - 0.52D, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
			worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + 0.52D, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
			worldIn.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, d0 + 0.52D, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
			worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d4, d1, d2 - 0.52D, 0.0D, 0.0D, 0.0D);
			worldIn.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, d0 + d4, d1, d2 - 0.52D, 0.0D, 0.0D, 0.0D);
			worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d4, d1, d2 + 0.52D, 0.0D, 0.0D, 0.0D);
			worldIn.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, d0 + d4, d1, d2 + 0.52D, 0.0D, 0.0D, 0.0D);
		}
	}

	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;

	}

	public static void setState(boolean active, World worldIn, BlockPos pos) {
		TileEntity tileentity = worldIn.getTileEntity(pos);

		if (active)
			worldIn.setBlockState(pos, ModBlocks.ACTIVE_COOLER.get().getDefaultState());
		else
			worldIn.setBlockState(pos, ModBlocks.COOLER.get().getDefaultState());

		if (tileentity != null) {
			tileentity.validate();
			worldIn.setTileEntity(pos, tileentity);
		}

	}

}