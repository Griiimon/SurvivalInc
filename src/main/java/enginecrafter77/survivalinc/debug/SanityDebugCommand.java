package enginecrafter77.survivalinc.debug;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class SanityDebugCommand extends CommandBase {
	public static boolean enabled= false;

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		enabled= !enabled;

		sender.sendMessage(new TextComponentString(String.format("Sanity debugging " +(enabled ? "enabled" : "disabled"))));
	}

	@Override
	public String getName() {
		return "debugsanity";
	}

	@Override
	public String getUsage(ICommandSender arg0) {
		return "/debugsanity";
	}

}
