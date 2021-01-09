package enginecrafter77.survivalinc.client;

import java.util.ArrayList;
import java.util.List;

import enginecrafter77.survivalinc.stats.StatRecord;
import enginecrafter77.survivalinc.stats.StatTracker;
import net.minecraft.client.gui.ScaledResolution;

// extends statrecord only to be accepted by other functions
public class Thermometer<RECORD extends StatRecord> implements OverlayElement<StatTracker>{
	
	public final SymbolFillBar background;
	
	public static float value=0f;
	
	protected final List<SymbolFillBar> bars;
	
	public Thermometer(Direction2D direction, TexturedElement base)
	{
//		this.bars = new LinkedHashMap<SymbolFillBar, Function<RECORD, Float>>();
		this.bars= new ArrayList<SymbolFillBar>();
		this.background = new SymbolFillBar(base, direction);
	}
	
	public void setSpacing(int spacing)
	{
		this.background.setSpacing(spacing);
		for(SymbolFillBar bar : this.bars) bar.setSpacing(spacing);
	}
	
	public void setCapacity(int capacity)
	{
		this.background.setCapacity(capacity);
		for(SymbolFillBar bar : this.bars) bar.setCapacity(capacity);
	}
	
	public void addOverlay(TexturedElement texture)
	{
		SymbolFillBar bar = new SymbolFillBar(texture, this.background.direction);
		bar.setCapacity(this.background.getCapacity());
		bar.setSpacing(this.background.getSpacing());
		this.bars.add(bar);
	}
	
	@Override
	public int getSize(Axis2D axis)
	{
		return this.background.getSize(axis);
	}
	
	@Override
	public void draw(ScaledResolution resolution, ElementPositioner position, float partialTicks, StatTracker arg)
	{
		this.background.draw(resolution, position, partialTicks, 1F);
		
		for(SymbolFillBar entry : this.bars)
		{
			entry.draw(resolution, position, partialTicks, value);
		}
	}

	
}
