package enginecrafter77.survivalinc.net;

import enginecrafter77.survivalinc.SurvivalInc;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class SanityOverviewReceiver implements IMessageHandler<SanityOverviewMessage, IMessage>{

	
	public IMessage onMessage(SanityOverviewMessage message, MessageContext ctx) {
		if(ctx.side == Side.SERVER)
		{
			System.out.println("DEBUG: Received Message ERROR: server-side");
			throw new RuntimeException("SanityOverviewMessage is designed to be processed on client!");
		}
		
//		SurvivalInc.proxy.AddReasonToClient(message.value, message.reason, message.forceAdd);

		SurvivalInc.proxy.SanityOverviewOnClient(null);

		System.out.println("DEBUG: Received msg: "+message.toString());
		
		return null;
	
	}

}
