package enginecrafter77.survivalinc.stats;

import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayer;

public interface StatTracker extends Iterable<Entry<StatProvider, Float>> {
	public void registerProvider(StatProvider provider);
	public void removeProvider(StatProvider identifier);
	public StatProvider getProvider(String identifier);
	
	public void modifyStat(StatProvider stat, float amount);
	public void setStat(StatProvider stat, float amount);
	public float getStat(StatProvider stat);
	
	public void update(EntityPlayer player);
}