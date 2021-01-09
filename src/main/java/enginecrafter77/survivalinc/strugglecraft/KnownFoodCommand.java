package enginecrafter77.survivalinc.strugglecraft;


import java.util.Hashtable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class KnownFoodCommand extends CommandBase{

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		Hashtable<Integer, Integer> foodTable= FoodModule.instance.getFoodTable((EntityPlayer)sender);
		String s="";
		boolean flag= false;
		for(int key : foodTable.keySet())
		{
			if(flag)
				s+=", ";
			s+= new ItemStack(Item.getItemById(key)).getDisplayName();
			flag= true;
		}
		sender.sendMessage(new TextComponentString(s));
	}

	@Override
	public String getName() {
		return "knownfood";
	}

	@Override
	public String getUsage(ICommandSender arg0) {
		return "/knownfood";
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
	    return true;
	}

}
