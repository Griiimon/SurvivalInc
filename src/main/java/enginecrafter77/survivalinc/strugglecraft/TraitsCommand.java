package enginecrafter77.survivalinc.strugglecraft;

import java.util.ArrayList;
import java.util.List;

import enginecrafter77.survivalinc.config.ModConfig;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class TraitsCommand extends CommandBase{


	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(ModConfig.TRAITS.enabled)
		{
			ArrayList<Integer> list= TraitModule.instance.getTraitList((EntityPlayer)sender.getCommandSenderEntity());
			
			for(int i= 0; i < list.size(); i+= 2)
			{
				sender.sendMessage(new TextComponentString(TraitModule.instance.getTrait(list.get(i)).traitName+" Lvl "+(list.get(i+1)+1)));
				
			}
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
