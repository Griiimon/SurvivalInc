package enginecrafter77.survivalinc.strugglecraft;

import enginecrafter77.survivalinc.stats.DrugRecord;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class DrugsCommand extends CommandBase{

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
//		sender.sendMessage(new TextComponentString("Deaths: "+DeathCounter.getDeaths((EntityPlayer)sender.getCommandSenderEntity())));

		EntityPlayer player= (EntityPlayer) sender;
		DrugRecord record= DrugModule.getRecord(player);
		
		for(int i= 0; i < Drug.drugList.size(); i++)
		{
			if(record.high[i]>0)
				sender.sendMessage(new TextComponentString("High from "+Drug.drugList.get(i).name));
		}

		for(int i= 0; i < Drug.drugList.size(); i++)
		{
			if(record.dependencyLevel[i]>0)
			{
				sender.sendMessage(new TextComponentString(Drug.drugList.get(i).name+" dependency Lvl "+record.dependencyLevel[i]));
				sender.sendMessage(new TextComponentString(Drug.drugList.get(i).name+" last used "+((player.world.getTotalWorldTime() - record.lastUse[i])/1200)+" minutes ago"));
				}
		}
	}

	@Override
	public String getName() {
		return "drugs";
	}

	@Override
	public String getUsage(ICommandSender arg0) {
		return "/drugs";
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
	    return true;
	}

}
