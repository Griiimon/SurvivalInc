package enginecrafter77.survivalinc.season;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import enginecrafter77.survivalinc.SurvivalInc;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;

public class SeasonCommand extends CommandBase {

	public SeasonCommand()
	{
		System.out.println("Registering command season");
	}
	
	@Override
	public String getName()
	{
		return "season";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/season <set|info|advance> [<season> [day]]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		WorldServer world = server.getWorld(DimensionType.OVERWORLD.getId());		
		SeasonData data = SeasonData.load(world);
		
		if(args.length < 1) throw new WrongUsageException("Insufficient arguments\nUsgae: " + this.getUsage(sender));
		
		switch(args[0])
		{
		case "set":
			if(args.length < 3) throw new WrongUsageException("Insufficient arguments\nUsage: " + this.getUsage(sender));
			data.season = Season.valueOf(args[1]);
			data.day = Integer.parseInt(args[2]);
			sender.sendMessage(new TextComponentString("Set calendar time to " + data.toString()));
			data.markDirty();
			break;
		case "advance":
			int days = 1;
			if(args.length >= 2) days = CommandBase.parseInt(args[1]);
			data.advance(days);
			sender.sendMessage(new TextComponentString("Advancing season by " + days + " days --> " + data.toString()));
			data.markDirty();
			break;
		case "info":
			float currentoffset = data.season.getTemperatureOffset(data.day);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			PrintStream message = new PrintStream(buffer);
			message.format("$aCurrent season:$r %s\n", data.toString());
			message.format("$aSeason Length:$r %d\n", data.season.getLength());
			message.format("$aTemperature Offset on $eDay %d$a:$r %.03f\n", data.day, currentoffset);
			message.format("$aPeak Temperature Offset in $e%s$a:$r %f\n", data.season.name(), data.season.getPeakTemperatureOffset());
			message.format("$aCurrent Temperature Inclination:$r %.03f", data.season.getTemperatureOffset(data.day + 1) - currentoffset);
			if(sender instanceof Entity)
			{
				BlockPos position = new BlockPos(sender.getPositionVector());
				Biome biome = server.getWorld(0).getBiome(position);
				float biometempdiff = biome.getTemperature(position) - biome.getDefaultTemperature();
				message.format("\n$aNominal temperature in current biome:$r %.02f ($7%s$r)", SeasonController.instance.biomeTemp.originals.get(biome), SeasonCommand.formatOffset("%.03f", currentoffset));
				message.format("\n$aTemperature at $eX%d Y%d Z%d$a:$r %.02f ($7%s$r)", position.getX(), position.getY(), position.getZ(), biome.getTemperature(position), SeasonCommand.formatOffset("%.03f", biometempdiff));
			}
			message.close();
			sender.sendMessage(new TextComponentString(buffer.toString().replace('$', '\u00a7'))); // Replace the $ sign with the minecraft formatting sign (Code 00A7)
			break;
		default:
			break; // Like that's ever gonna happen! What a load of...
		}
		
		if(data.isDirty())
		{
			SurvivalInc.proxy.net.sendToDimension(new SeasonSyncMessage(data), DimensionType.OVERWORLD.getId());
			MinecraftForge.EVENT_BUS.post(new SeasonChangedEvent(world, data));
		}
	}
	
	private static <NUM extends Number> String formatOffset(String formatting, NUM number)
	{
		StringBuilder builder = new StringBuilder();
		if(number.floatValue() > 0) builder.append('+');
		builder.append(String.format(formatting, number));
		return builder.toString();
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
	    return true;
	}

}
