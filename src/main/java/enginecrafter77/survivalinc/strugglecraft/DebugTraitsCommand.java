package enginecrafter77.survivalinc.strugglecraft;

import java.util.ArrayList;

import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.strugglecraft.TraitModule.TRAITS;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class DebugTraitsCommand extends CommandBase{


	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(ModConfig.TRAITS.enabled)
		{
			for(TRAITS t : TRAITS.values())
				if(t.chance > 0)
				{
					if(args == null || args[0] == "pos")
					TraitModule.instance.AddTrait((EntityPlayer)sender, t);
				}
		}
	}

	@Override
	public String getName() {
		return "debugtraits";
	}

	@Override
	public String getUsage(ICommandSender arg0) {
		return "/debugtraits";
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
	    return true;
	}

}

