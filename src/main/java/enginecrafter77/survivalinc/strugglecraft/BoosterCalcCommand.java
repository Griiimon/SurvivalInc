package enginecrafter77.survivalinc.strugglecraft;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class BoosterCalcCommand extends CommandBase{

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		sender.sendMessage(new TextComponentString("Reach: "+(int)Math.sqrt(Integer.parseInt(args[0]) * 2)));
	}

	@Override
	public String getName() {
		return "boostercalc";
	}

	@Override
	public String getUsage(ICommandSender arg0) {
		return "/boostercalc";
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
	    return true;
	}

}