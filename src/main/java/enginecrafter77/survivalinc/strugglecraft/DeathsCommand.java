package enginecrafter77.survivalinc.strugglecraft;

import enginecrafter77.survivalinc.config.ModConfig;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class DeathsCommand extends CommandBase{

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
//		if(ModConfig.TRAITS.enabled)
//		{
//			sender.sendMessage(new TextComponentString("Deaths: "+TraitModule.instance.getDeaths((EntityPlayer)sender.getCommandSenderEntity())));
			sender.sendMessage(new TextComponentString("Deaths: "+DeathCounter.getDeaths((EntityPlayer)sender.getCommandSenderEntity())));
//		}
	}

	@Override
	public String getName() {
		return "deaths";
	}

	@Override
	public String getUsage(ICommandSender arg0) {
		return "/deaths";
	}
}
