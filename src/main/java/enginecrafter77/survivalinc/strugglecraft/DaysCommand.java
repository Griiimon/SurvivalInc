package enginecrafter77.survivalinc.strugglecraft;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class DaysCommand extends CommandBase{

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		sender.sendMessage(new TextComponentString("Days: "+(int)(server.worlds[0].getTotalWorldTime() / 24000)));
	}

	@Override
	public String getName() {
		return "days";
	}

	@Override
	public String getUsage(ICommandSender arg0) {
		return "/days";
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
	    return true;
	}

}
