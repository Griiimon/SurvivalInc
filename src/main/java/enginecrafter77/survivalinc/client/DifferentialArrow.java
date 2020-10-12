package enginecrafter77.survivalinc.client;

import enginecrafter77.survivalinc.SurvivalInc;
import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.stats.SimpleStatRecord;
import enginecrafter77.survivalinc.stats.StatCapability;
import enginecrafter77.survivalinc.stats.StatProvider;
import enginecrafter77.survivalinc.stats.StatTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DifferentialArrow extends ScalableOverlayElement {
	public static final ResourceLocation arrowtexture = new ResourceLocation(SurvivalInc.MOD_ID, "textures/gui/arrow.png");
	
	public final TextureManager texturer;
	public final StatProvider provider;
	
	protected float amplitude, min_scale, max_scale;
	
	public DifferentialArrow(StatProvider provider, int width, int height)
	{
		super(width, height);
		this.texturer = Minecraft.getMinecraft().renderEngine;
		this.provider = provider;
		
		this.amplitude = 10F;
		this.min_scale = 0.3F;
		this.max_scale = 1F;
		
		// Create a dummy record to see if it's a subclass of SimpleStatRecord
		if(!(provider.createNewRecord() instanceof SimpleStatRecord))
		{
			throw new IllegalArgumentException("Differential Arrow can be used only with providers using SimpleStatRecord records!");
		}
	}
	
	@Override
	public void draw(RenderGameOverlayEvent event)
	{
		if(event.getType() == ElementType.HOTBAR) super.draw(event);
	}
	
	@Override
	public void draw()
	{
		// Bind the arrow texture
		this.texturer.bindTexture(arrowtexture);
		
		float value = this.getArrowScale(Minecraft.getMinecraft().player.getCapability(StatCapability.target, null));
		boolean inverse = value < 0F;
		value = Math.abs(value);
		
		GlStateManager.enableAlpha();
		GlStateManager.pushMatrix(); // Create new object by pushing matrix
		// Offset this object into the desired position + centering offset
		GlStateManager.translate(this.getX() + (this.width / 2), this.getY() + (this.height / 2), 0F);
		GlStateManager.pushMatrix(); // Create new object by pushing matrix
		GlStateManager.scale(value, value, 1F); // Scale the arrow
		if(inverse) GlStateManager.rotate(180F, 0F, 0F, 1F); // Rotate the arrow
		Gui.drawModalRectWithCustomSizedTexture(-this.width / 2, -this.height / 2, 0, 0, this.width, this.height, 8, 12); // Draw the arrow (center at origin)
		GlStateManager.popMatrix(); // Render the scaled and rotated arrow
		GlStateManager.popMatrix(); // Render the offset arrow in place
		GlStateManager.disableAlpha();
	}

	/**
	 * Returns the relative arrow scale form 0 (invisible)
	 * to 1 (visible). Negative returned values indicate
	 * that the arrow should be rotated upside down.
	 * @param tracker The stat tracker which tracks the provided stat
	 * @return A value from -1 to 1, ranging from 0 = invisible to |1| = fully visible
	 */
	public float getArrowScale(StatTracker tracker)
	{
		SimpleStatRecord record = (SimpleStatRecord)tracker.getRecord(provider);
		
		float scale = Math.abs(record.getLastChange() * this.amplitude);
		if(!ModConfig.CLIENT.linearArrow)
		{
			/*
			 * The scale is calculated using this relatively simple exponential formula:
			 *      1 - n^(r|x * a|)
			 * y = ------------
			 *      1 - n^(r)
			 * 
			 * where n and r are constants, which define the shape of the curve, y is the
			 * resultant scale of the arrow and X is the last change to the target stat.
			 * 'a' is a constant which defines the linear scaling up of the last change,
			 * so it's real effects can be visible.
			 */
			scale = (1F - (float)Math.pow(6F, -2F * scale)) / (1F - (float)Math.pow(6F, -2F));
		}
		
		// Cap the scale
		if(scale > this.max_scale) scale = this.max_scale; // Always results in 1 or -1
		if(scale < this.min_scale && scale != 0) scale = this.min_scale;
		
		// Reintroduce the direction sign into the scale
		if(record.getLastChange() < 0) scale *= -1F;
		return scale;
	}
	
}