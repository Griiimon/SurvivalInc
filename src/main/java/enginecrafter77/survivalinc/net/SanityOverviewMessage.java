package enginecrafter77.survivalinc.net;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SanityOverviewMessage implements IMessage {
	
	public SanityOverviewMessage()
	{
	}
	
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
	
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		// dummy
		buf.writeBoolean(true);

	}

	
	@Override
	public String toString()
	{
		return "SanityOverview()";
	}


}