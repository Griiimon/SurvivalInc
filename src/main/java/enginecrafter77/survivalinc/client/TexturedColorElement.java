package enginecrafter77.survivalinc.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TexturedColorElement extends TexturedElement{

	public static float scale= 1f;
	
	public TexturedColorElement(TextureResource resource, int offset_x, int offset_y, int width, int height,
			boolean alpha) {
		super(resource, offset_x, offset_y, width, height, alpha);
	}

		
	@Override
	public void draw(ScaledResolution resolution, ElementPositioner position, float partialTicks, Object arg)
	{
		TextureDrawingContext context = this.createContext(this.texturer);
		GlStateManager.pushMatrix(); // Create new object by pushing matrix
		// Offset this object into the desired position + centering offset
		GlStateManager.translate(position.getX(resolution) + (this.width / 2), position.getY(resolution) + (this.height / 2), 0F);
		GlStateManager.pushMatrix(); // Create new object by pushing matrix
		GlStateManager.scale(1f, scale, 1F); // Scale the arrow
		Gui.drawModalRectWithCustomSizedTexture(-this.width / 2, -this.height / 2, 0, 0, this.width, this.height, 16, 16); // Draw the element (center at origin)
		GlStateManager.popMatrix(); // Render the scaled element
		GlStateManager.popMatrix(); // Render the offset arrow in place
		context.close();
	}
	
	
	
}
