package enginecrafter77.survivalinc.strugglecraft;

import java.util.ArrayList;

import enginecrafter77.survivalinc.SurvivalInc;
import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.net.SanityOverviewMessage;
import enginecrafter77.survivalinc.net.SanityReasonMessage;
import enginecrafter77.survivalinc.stats.impl.SanityTendencyModifier;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class SanityOverviewCommand extends CommandBase{


	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(ModConfig.SANITY.enabled)
		{
			SanityOverviewMessage msg= new SanityOverviewMessage();
			System.out.println("DEBUG: Send msg: "+msg);
			SurvivalInc.proxy.net.sendTo(msg, (EntityPlayerMP) sender);
		}
	}

	@Override
	public String getName() {
		return "sanityoverview";
	}

	@Override
	public String getUsage(ICommandSender arg0) {
		return "/sanityoverview";
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
	    return true;
	}

}
