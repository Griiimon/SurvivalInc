package enginecrafter77.survivalinc.strugglecraft;

import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.stats.impl.SanityTendencyModifier;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class AddTendencyCommand extends CommandBase{

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		float value;
		try {
			value= Float.parseFloat(args[0]);
		}
		catch(Exception e)
		{
			sender.sendMessage(new TextComponentString("usage: /addtendency float"));
			return;
		}
		SanityTendencyModifier.instance.addToTendency(value,"Debug cmd", (EntityPlayer)sender);
	}

	@Override
	public String getName() {
		return "addtendency";
	}

	@Override
	public String getUsage(ICommandSender arg0) {
		return "/addtendency";
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
	    return true;
	}

}
