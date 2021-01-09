package enginecrafter77.survivalinc.net;

import enginecrafter77.survivalinc.debug.HeatDebugCommand;
import enginecrafter77.survivalinc.debug.LightDebugCommand;
import enginecrafter77.survivalinc.debug.SanityDebugCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class DebugToggleUpdater implements IMessageHandler<DebugToggleMessage, IMessage> {

	@Override
	public IMessage onMessage(DebugToggleMessage message, MessageContext ctx)
	{
		if(ctx.side == Side.SERVER)
			throw new RuntimeException("DebugToggleMessage is designed to be processed on client!");
		
		// Damn, I know what I am doing!
//		Minecraft instance = Minecraft.getMinecraft();
//		WorldClient world = instance.world;

		switch(message.debugid)
		{
			case 1:
				SanityDebugCommand.enabled= !SanityDebugCommand.enabled;
				break;
			case 2:
				LightDebugCommand.enabled= !LightDebugCommand.enabled;
				break;
			case 3:
				HeatDebugCommand.enabled= !LightDebugCommand.enabled;
				break;
				

			default:
				throw new RuntimeException("Debug Message couldnt be handled! id: "+message.debugid);
		}
		
		return null;

		
//		 ctx.setPacketHandled(true);
		 

		
		
	}

}

