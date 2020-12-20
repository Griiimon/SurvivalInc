package enginecrafter77.survivalinc.survivecraft;

import enginecrafter77.survivalinc.config.ModConfig;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class TraitsCommand extends CommandBase{


	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(ModConfig.TRAITS.enabled)
		{
			for(TraitModule.TraitListEntry entry : TraitModule.instance.listTraits)
				sender.sendMessage(new TextComponentString(entry.trait.traitName+" Lvl "+entry.tier));
			
		}
	}

	@Override
	public String getName() {
		return "traits";
	}

	@Override
	public String getUsage(ICommandSender arg0) {
		return "/traits";
	}
}
