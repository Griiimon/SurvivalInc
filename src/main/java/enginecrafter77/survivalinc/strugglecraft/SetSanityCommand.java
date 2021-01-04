package enginecrafter77.survivalinc.strugglecraft;

import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.stats.impl.SanityModifier;
import enginecrafter77.survivalinc.stats.impl.SanityTendencyModifier;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class SetSanityCommand extends CommandBase{

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		float value;
		try {
			value= Float.parseFloat(args[0]);
		}
		catch(Exception e)
		{
			sender.sendMessage(new TextComponentString("usage: /setsanity float"));
			return;
		}
		SanityModifier.instance.set(value, (EntityPlayer)sender);
	}

	@Override
	public String getName() {
		return "setsanity";
	}

	@Override
	public String getUsage(ICommandSender arg0) {
		return "/setsanity";
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
	    return true;
	}

}
