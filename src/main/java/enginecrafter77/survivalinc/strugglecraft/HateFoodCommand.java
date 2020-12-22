package enginecrafter77.survivalinc.strugglecraft;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class HateFoodCommand extends CommandBase{

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		for(int key : FoodModule.instance.foodTable.keySet())
			if(FoodModule.instance.foodTable.get(key) > 4)
				sender.sendMessage(new TextComponentString(new ItemStack(Item.getItemById(key)).getDisplayName()));
	}

	@Override
	public String getName() {
		return "hatefood";
	}

	@Override
	public String getUsage(ICommandSender arg0) {
		return "/hatefood";
	}
}
