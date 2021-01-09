package enginecrafter77.survivalinc.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class DebugToggleMessage implements IMessage {
	/** The item stack of the entity */
	protected int debugid;
	
	/**
	 * Constructs the packet to update the target entity.
	 * @param entity The {@link EntityItem} to be updated
	 */
	public DebugToggleMessage(int id)
	{
		this.debugid= id;
	}
	
	public DebugToggleMessage() {}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.debugid = buf.readInt(); 
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(this.debugid);
	}
}