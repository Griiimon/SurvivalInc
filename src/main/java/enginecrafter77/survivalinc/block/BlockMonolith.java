package enginecrafter77.survivalinc.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.google.common.base.Optional;

import enginecrafter77.survivalinc.ModBlocks;
import enginecrafter77.survivalinc.SurvivalInc;
import enginecrafter77.survivalinc.strugglecraft.MyWorldSavedData;
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
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
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

public class BlockMonolith extends Block implements ITileEntityProvider {

	static final ArrayList<Block> boosterList= new ArrayList<Block>();
	
	public BlockMonolith() {
		super(Material.ROCK);
//	    setUnlocalizedName(SurvivalInc.MOD_ID + ".heater");
		setRegistryName(new ResourceLocation(SurvivalInc.MOD_ID, "monolith"));
//	    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
		this.setHardness(10f);
		
		// TODO move to static init
		if(boosterList.size() == 0)
		{
			boosterList.add(Blocks.STONE);
//			boosterList.add(Blocks.SANDSTONE); Smooth sandstone is subtype ??
			boosterList.add(Blocks.BONE_BLOCK);
		}
	}

	/*
	 * @SideOnly(Side.CLIENT) public void initModel() {
	 * ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0,
	 * new ModelResourceLocation(getRegistryName(), "inventory")); }
	 */
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity,
			ItemStack stack) {
		if (world.isRemote)
			return;

		tryToCloseCircle(world, pos);
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
	{
//	 final boolean CASCADE_UPDATE = false;  // I'm not sure what this flag does, but vanilla always sets it to false
	 // except for calls by World.setBlockState()
//	 worldIn.notifyNeighborsOfStateChange(pos, this, CASCADE_UPDATE);
		
		//TODO remove place and set all tile entities to false
		
		super.breakBlock(worldIn, pos, state);
	}


	static void tryToCloseCircle(World world, BlockPos startPos) {
		System.out.println("DEBUG: run circle check from " + startPos);

		List<BlockPos> checkList = new ArrayList<BlockPos>();
		List<BlockPos> finalList = new ArrayList<BlockPos>();

		boolean successful = false;
		BlockPos center = new BlockPos(0, 0, 0);
		BlockPos currentPos;

		int firstCheck = 0;

		checkList.add(startPos);

		int extraValue= 0;
		
		do {
			currentPos = checkList.get(0);

			checkList.remove(0);

			boolean runSuccessful = false;

			for (int x = -1; x < 2; x++)
				for (int z = -1; z < 2; z++)
					if (!successful && !runSuccessful && (x != 0 || z != 0)) {
						BlockPos checkPos = new BlockPos(currentPos.getX() + x, currentPos.getY(),
								currentPos.getZ() + z);
						if (world.getBlockState(checkPos).getBlock() == ModBlocks.MONOLITH.get()) {
							if (checkPos.equals(startPos)) {
								if (firstCheck < 2)
									continue;
								else {
									successful = true;
									System.out.println("DEBUG: found start pos again");
									break;
								}
							}

							boolean knownPos = false;

							for (BlockPos pos : finalList)
								if (checkPos.equals(pos)) {
									knownPos = true;
									break;
								}

							if (!knownPos) {
								finalList.add(checkPos);
								checkList.add(checkPos);
								runSuccessful = true;
								System.out.println("DEBUG: found next block " + checkPos);

								boolean extra= true;
								
								for (int x2 = -1; x2 < 2; x2++)
									for (int z2 = -1; z2 < 2; z2++)
										if (extra) {
								
											BlockPos belowPos = new BlockPos(checkPos.getX() + x2, checkPos.getY()-1,checkPos.getZ() + z2);
											
											// TODO get block subtype
											// world.getBlockState(belowPos).getBlock().
											if(!boosterList.contains(world.getBlockState(belowPos).getBlock()))
											{
												extra= false;
												break;
											}
										}
								
								if(extra)
									extraValue++;
								
								break;
							}
						}

					}

			firstCheck++;

		} while (checkList.size() > 0);

		if (successful) {
			System.out.println("DEBUG: successful");

			finalList.add(startPos);

			for (BlockPos pos : finalList)
				center = new BlockPos(center.getX() + pos.getX(), pos.getY(), center.getZ() + pos.getZ());

			center = new BlockPos(center.getX() / finalList.size(), center.getY(), center.getZ() / finalList.size());

			System.out.println("DEBUG: center " + center);

			for (BlockPos pos : finalList)
				BlockMonolith.setState(true, world, pos, center);

			int reach= finalList.size() + extraValue;
			
			MyWorldSavedData.get(world).addWorshipPlace(center, reach);
			world.getMinecraftServer().getPlayerList().sendMessage(
					new TextComponentString("New place of worship at " + center + " (reach " + reach + ")"));
		}

	}

	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		System.out.println("TILE ENTITY MONOLITH CREATED");
		return new TileEntityMonolith();
	}

	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	public static void setState(boolean activated, World worldIn, BlockPos pos, BlockPos center) {
		TileEntity tileentity = worldIn.getTileEntity(pos);

//    	worldIn.setBlockState(pos, ModBlocks.MONOLITH.get().getDefaultState());

		if (tileentity != null) {
			tileentity.validate();
			worldIn.setTileEntity(pos, tileentity);
			((TileEntityMonolith) tileentity).circleCenter = center;
		}
	}

	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	/**
	 * Gets the render layer this block will render on. SOLID for solid blocks,
	 * CUTOUT or CUTOUT_MIPPED for on-off transparency (glass, reeds), TRANSLUCENT
	 * for fully blended transparency (stained glass)
	 */
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

}