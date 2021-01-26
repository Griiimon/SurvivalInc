package enginecrafter77.survivalinc.block;


import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import enginecrafter77.survivalinc.ModBlocks;
import enginecrafter77.survivalinc.SurvivalInc;
import enginecrafter77.survivalinc.strugglecraft.MyWorldSavedData;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class BlockMonumentTop extends Block{

	public static Map<Block, Integer>boosterBlockMap;
	
	public BlockMonumentTop() {
		super(Material.IRON);

		setRegistryName(new ResourceLocation(SurvivalInc.MOD_ID, "monument_top"));
		this.setHardness(10f);
		
		boosterBlockMap= new HashMap<Block, Integer>();	
		
		boosterBlockMap.put(Blocks.STAINED_GLASS, 1);
		boosterBlockMap.put(Blocks.IRON_BARS, 1);
		boosterBlockMap.put(Blocks.CONCRETE, 5);
		boosterBlockMap.put(Blocks.WOOL, 10);
		boosterBlockMap.put(Blocks.BRICK_BLOCK, 10);
		boosterBlockMap.put(Blocks.COAL_BLOCK, 15);
		boosterBlockMap.put(Blocks.BONE_BLOCK, 20);
		boosterBlockMap.put(Blocks.OBSIDIAN, 50);
		boosterBlockMap.put(Blocks.SLIME_BLOCK, 50);
		boosterBlockMap.put(Blocks.IRON_BLOCK, 50);
		boosterBlockMap.put(Blocks.LAPIS_BLOCK, 100);
		boosterBlockMap.put(Blocks.REDSTONE_BLOCK, 150);
		boosterBlockMap.put(Blocks.EMERALD_BLOCK, 150);
		boosterBlockMap.put(Blocks.QUARTZ_BLOCK, 200);
		boosterBlockMap.put(Blocks.GOLD_BLOCK, 200);
		boosterBlockMap.put(Blocks.DIAMOND_BLOCK, 1000);
		
		boosterBlockMap= sortByComparator(boosterBlockMap, true);
	}
	
	


	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState blockState, EntityLivingBase player, ItemStack itemStack) {
		super.onBlockPlacedBy(world, pos, blockState, player, itemStack);

		if(world.isRemote)
			return;
		
		BlockPos p= pos.down();
		
		int total= 0;
		
		int height= 0;
		
		while(world.getBlockState(p).getBlock() == ModBlocks.MONUMENT_STRUCTURE.get())
		{
			boolean boosterValid= true;
			boolean boosterEmpty= false;
			boolean first= true;
			
			Block boosterBlock= null;
			
			for(int x= pos.getX() - 1; x < pos.getX() + 2; x++)
				for(int z= pos.getZ() - 1; z < pos.getZ() + 2; z++)
					if(boosterValid && (x != pos.getX() || z != pos.getZ()))
					{
						BlockPos neighbourPos= new BlockPos(x, p.getY(), z);
						
						Block block= world.getBlockState(neighbourPos).getBlock();
						
						System.out.println("DEBUG: Booster at "+neighbourPos+" : "+block.getLocalizedName());
						
						if(block == Blocks.AIR)
						{
							if(first)
								boosterEmpty= true;
							else
							if(!boosterEmpty)
							{
								boosterValid= false;
								break;
							}
						}
						else
						{
							if(first)
							{
								boosterEmpty= false;
								boosterBlock= block;
							}
							else
							{
								if(boosterEmpty || block != boosterBlock)
								{
									boosterValid= false;
									break;
								}
							}							
							
						}
						
						first= false;
					}
			
			if(boosterValid && !boosterEmpty)
				if(boosterBlockMap.containsKey(boosterBlock))
					total+= boosterBlockMap.get(boosterBlock);

			System.out.println("DEBUG: Booster new total: "+total);

			
			height++;
			
			p= p.down();
		}
		
		MyWorldSavedData.get(world).addWorshipPlace(pos, height, total);
		
		world.getMinecraftServer().getPlayerList().sendMessage(new TextComponentString("Monument built (reach " + (int)Math.sqrt(total * 2) + ")"));

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
	
    private static Map<Block, Integer> sortByComparator(Map<Block, Integer> unsortMap, final boolean order)
    {

        List<Entry<Block, Integer>> list = new LinkedList<Entry<Block, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<Block, Integer>>()
        {
            public int compare(Entry<Block, Integer> o1,
                    Entry<Block, Integer> o2)
            {
                if (order)
                {
                    return o1.getValue().compareTo(o2.getValue());
                }
                else
                {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<Block, Integer> sortedMap = new LinkedHashMap<Block, Integer>();
        for (Entry<Block, Integer> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
	
	
}
