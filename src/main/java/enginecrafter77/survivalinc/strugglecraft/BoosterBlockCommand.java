package enginecrafter77.survivalinc.strugglecraft;




import enginecrafter77.survivalinc.block.BlockMonumentTop;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class BoosterBlockCommand extends CommandBase{


	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

		for(Block booster : BlockMonumentTop.boosterBlockMap.keySet())
			sender.sendMessage(new TextComponentString(booster.getLocalizedName()+": "+BlockMonumentTop.boosterBlockMap.get(booster)));
	}

	@Override
	public String getName() {
		return "booster";
	}

	@Override
	public String getUsage(ICommandSender arg0) {
		return "/booster";
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
	    return true;
	}

}
