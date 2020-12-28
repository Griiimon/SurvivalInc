package enginecrafter77.survivalinc.strugglecraft;

import enginecrafter77.survivalinc.config.ModConfig;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class FavouriteFoodCommand extends CommandBase{


	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		int id= FoodModule.instance.getFavoriteFoodId((EntityPlayer)sender);
		if(id == -1)
			sender.sendMessage(new TextComponentString("No favourite food yet"));
		else
			sender.sendMessage(new TextComponentString(new ItemStack(Item.getItemById(id)).getDisplayName()));
	}

	@Override
	public String getName() {
		return "favfood";
	}

	@Override
	public String getUsage(ICommandSender arg0) {
		return "/favfood";
	}
}
