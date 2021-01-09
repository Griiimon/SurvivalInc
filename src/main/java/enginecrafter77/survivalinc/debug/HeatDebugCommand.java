package enginecrafter77.survivalinc.debug;

import enginecrafter77.survivalinc.SurvivalInc;
import enginecrafter77.survivalinc.net.DebugToggleMessage;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class HeatDebugCommand extends CommandBase {
		public static boolean enabled= false;

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
			SurvivalInc.proxy.net.sendTo(new DebugToggleMessage(2), (EntityPlayerMP) sender);

			
			enabled= !enabled;

			sender.sendMessage(new TextComponentString(String.format("Heat debugging " +(enabled ? "enabled" : "disabled"))));
		}

		@Override
		public String getName() {
			return "debugheat";
		}

		@Override
		public String getUsage(ICommandSender arg0) {
			return "/debugheat";
		}

		@Override
		public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		    return true;
		}

	
	
	
}
