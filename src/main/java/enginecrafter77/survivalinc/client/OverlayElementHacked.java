package enginecrafter77.survivalinc.client;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface OverlayElementHacked {
	public void draw(ScaledResolution resolution, ElementPositioner position, float partialTicks);
	
	/**
	 * @return The width of the element
	 */
	public int getSize(Axis2D axis);
}
