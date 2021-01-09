package enginecrafter77.survivalinc.net;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SanityReasonMessage implements IMessage {
	public float value;
	public String reason;
	public boolean forceAdd;
	
	public SanityReasonMessage(float v, String s, boolean b)
	{
		this.value= v;
		this.reason= s;
		this.forceAdd= b;
	}
	
	public SanityReasonMessage() {}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.value= buf.readFloat();
		this.reason = ByteBufUtils.readUTF8String(buf); 
		this.forceAdd= buf.readBoolean();
		
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeFloat(this.value);
		ByteBufUtils.writeUTF8String(buf, reason); 
		buf.writeBoolean(forceAdd);
	}
	
	@Override
	public String toString()
	{
		return "SanityReason("+value+", "+reason+","+forceAdd+")";
	}
}