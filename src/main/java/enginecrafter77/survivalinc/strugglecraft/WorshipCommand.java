package enginecrafter77.survivalinc.strugglecraft;


import java.util.List;

import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.util.Util;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class WorshipCommand extends CommandBase{


	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

		EntityPlayer player= (EntityPlayer) sender;
		
		List<WorshipPlace> list= MyWorldSavedData.get(player.world).getWorshipPlaces();
		
		if(list.size() == 0)
		{
			sender.sendMessage(new TextComponentString("You haven't build any.."));
			return;
		}
		
		for(WorshipPlace place : list)
		{
			int dist= (int)Util.distance(player.getPosition(), place.getPosition());
				if(dist < place.getValue())
				{
					sender.sendMessage(new TextComponentString("In reach ("+dist+"/"+place.getValue()+")"));
					return;
				}
		}
		
		sender.sendMessage(new TextComponentString("Out of reach"));

	}

	@Override
	public String getName() {
		return "worship";
	}

	@Override
	public String getUsage(ICommandSender arg0) {
		return "/worship";
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
	    return true;
	}

}
