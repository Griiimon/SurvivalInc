package enginecrafter77.survivalinc.client;

import java.awt.Color;
import enginecrafter77.survivalinc.stats.SimpleStatRecord;
import enginecrafter77.survivalinc.stats.StatProvider;
import enginecrafter77.survivalinc.stats.StatTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SimpleStatBar extends OverlayElementGroup<StatTracker> {	
	public final StatProvider provider;
	
	public SimpleStatBar(StatProvider provider, TexturedElement icon, Color color)
	{
		super(Axis.VERTICAL);
		this.provider = provider;
		
		this.elements.add(new DifferentialArrow(provider, 8, 12));
		this.elements.add(new ElementTypeAdapter<StatTracker, Float>(new GaugeBar(color), this::getRecordValue));
		this.elements.add(icon);
		
		// Create a dummy record to see if it's a subclass of SimpleStatRecord
		if(!(provider.createNewRecord() instanceof SimpleStatRecord))
		{
			throw new IllegalArgumentException("Differential Arrow can be used only with providers using SimpleStatRecord records!");
		}
	}

	@Override
	public void draw(ScaledResolution resolution, ElementPositioner position, float partialTicks, StatTracker tracker)
	{
		if(tracker.isActive(this.provider, Minecraft.getMinecraft().player))
		{
			super.draw(resolution, position, partialTicks, tracker);
		}
	}

	private Float getRecordValue(StatTracker tracker)
	{
		return ((SimpleStatRecord)tracker.getRecord(provider)).getNormalizedValue();
	}
}