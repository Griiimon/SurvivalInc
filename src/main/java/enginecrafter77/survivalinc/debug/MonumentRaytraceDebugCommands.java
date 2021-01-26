package enginecrafter77.survivalinc.debug;

import enginecrafter77.survivalinc.stats.impl.SanityTendencyModifier;
import enginecrafter77.survivalinc.strugglecraft.MyWorldSavedData;
import enginecrafter77.survivalinc.strugglecraft.WorshipPlace;
import enginecrafter77.survivalinc.strugglecraft.WorshipPlace.WORSHIP_PLACE_TYPE;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;

public class MonumentRaytraceDebugCommands extends CommandBase {

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

		EntityPlayer player = (EntityPlayer) sender;

		for (WorshipPlace place : MyWorldSavedData.get(player.world).getWorshipPlaces()) {
			if (place.type != WORSHIP_PLACE_TYPE.MONUMENT)
				continue;

			BlockPos pos = new BlockPos(place.getPosition());
			pos.add(new Vec3i(0, -place.getHeight(), 0));

			if (player.getDistanceSq(place.getPosition()) < place.getValue() * 2 || player.getDistanceSq(place.getPosition().down(place.getHeight())) < place.getValue() * 2) {
				RayTraceResult result = player.world.rayTraceBlocks(new Vec3d(player.getPosition().up()), new Vec3d(place.getPosition()), false, false, false);

				if(result != null)
				{
					sender.sendMessage(new TextComponentString(String.format("Raytrace: " + result.getBlockPos() + "  " + player.world.getBlockState(result.getBlockPos()).getBlock().getRegistryName().toString())));
	
					if (result.getBlockPos().equals(place.getPosition())) {
						sender.sendMessage(new TextComponentString("hit"));
						break;
					}
				}
				else
					sender.sendMessage(new TextComponentString("Raytrace: null"));
				
			}
			else
				sender.sendMessage(new TextComponentString("out of reach "+player.getDistanceSq(place.getPosition())+ " vs "+place.getValue() * 2));

		}

	}

	@Override
	public String getName() {
		return "debugmonument";
	}

	@Override
	public String getUsage(ICommandSender arg0) {
		return "/debugmonument";
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

}
