package enginecrafter77.survivalinc.stats;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * SimpleStatRegister is the default implementation of StatTracker.
 * It features all the basic facilities and principles recommended
 * by StatTracker definition.
 * @author Enginecrafter77
 */
public class SimpleStatRegister implements StatTracker {
	/** A map of the stat provider IDs with the {@link SimpleStatRegisterEntry} */
	public final Map<ResourceLocation, SimpleStatRegisterEntry> statmap;
	
	public SimpleStatRegister()
	{
		this.statmap = new HashMap<ResourceLocation, SimpleStatRegisterEntry>();
	}
	
	@Override
	public void registerProvider(StatProvider<?> provider) throws IllegalStateException
	{
		ResourceLocation identifier = provider.getStatID();
		if(this.statmap.containsKey(identifier)) throw new IllegalStateException("Provider " + provider.getClass().getCanonicalName() + " already registered!");
		this.statmap.put(identifier, this.createNewEntry(provider));
	}
	
	@Override
	public void removeProvider(StatProvider<?> provider) throws IllegalStateException
	{
		ResourceLocation identifier = provider.getStatID();
		if(!this.statmap.containsKey(identifier))
			throw new IllegalStateException("Provider " + identifier.toString() + " was never registered!");
		this.statmap.remove(identifier);
	}
	
	@Override
	public StatProvider<?> getProvider(ResourceLocation identifier)
	{
		return this.statmap.get(identifier).provider;
	}
	
	@Override
	public <RECORD extends StatRecord> void setRecord(StatProvider<RECORD> stat, RECORD value)
	{
		SimpleStatRegisterEntry entry = this.getEntry(stat);
		if(entry != null) entry.setRecord(value);
	}
	
	@Override
	public <RECORD extends StatRecord> RECORD getRecord(StatProvider<RECORD> stat)
	{
		SimpleStatRegisterEntry entry = this.getEntry(stat);
		return entry == null ? null : stat.getRecordClass().cast(entry.getRecord());
	}
	
	@Override
	public void update(EntityPlayer player)
	{
		this.statmap.values().forEach((SimpleStatRegisterEntry entry) -> entry.tick(player));
	}

	@Override
	public Collection<StatProvider<?>> getRegisteredProviders()
	{
		Set<StatProvider<?>> providers = new HashSet<StatProvider<?>>(this.statmap.size());
		for(SimpleStatRegisterEntry entry : this.statmap.values())
			providers.add(entry.provider);
		return providers;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getSimpleName());
		builder.append('[');
		for(Map.Entry<ResourceLocation, SimpleStatRegisterEntry> entry : this.statmap.entrySet())
		{
			builder.append(entry.getKey().toString());
			builder.append(": ");
			builder.append(entry.getValue().toString());
			builder.append(", ");
		}
		builder.setLength(builder.length() - 2);
		builder.append(']');
		return builder.toString();
	}

	@Override
	public boolean isActive(StatProvider<?> stat, @Nullable EntityPlayer player)
	{
		SimpleStatRegisterEntry entry = this.getEntry(stat);
		return player == null ? entry.isActive() : entry.isActiveFor(player);
	}

	@Override
	public void setSuspended(StatProvider<?> stat, boolean suspended)
	{
		this.getEntry(stat).setActive(!suspended);
	}
	
	/**
	 * Returns the internally associated {@link SimpleStatRegisterEntry}.
	 * @param stat The stat provider
	 * @return Internally associated {@link SimpleStatRegisterEntry}, or null if no such entry exists
	 */
	public SimpleStatRegisterEntry getEntry(StatProvider<?> stat)
	{
		return this.statmap.get(stat.getStatID());
	}
	
	/**
	 * Creates a new {@link SimpleStatRegisterEntry} to be used
	 * with this implementation of {@link SimpleStatRegister}.
	 * @param stat
	 * @return
	 */
	protected SimpleStatRegisterEntry createNewEntry(StatProvider<?> stat)
	{
		return new SimpleStatRegisterEntry(stat);
	}
	
	protected static class SimpleStatRegisterEntry
	{
		public final StatProvider<?> provider;
		public boolean runInCreative;
		protected StatRecord record;
		private boolean shouldTick;
		
		public SimpleStatRegisterEntry(StatProvider<?> provider)
		{
			this.record = provider.createNewRecord();
			this.runInCreative = false;
			this.provider = provider;
			this.shouldTick = true;
		}
		
		public void tick(EntityPlayer player)
		{
			if(this.isActiveFor(player))
			{
				this.provider.update(player, this.record);
			}
		}
		
		public boolean isActiveFor(EntityPlayer player)
		{
			return this.isActive() && (this.runInCreative || !player.isCreative()) && !player.isSpectator();
		}
		
		public void setRecord(StatRecord record)
		{
			this.record = record;
		}
		
		public StatRecord getRecord()
		{
			return this.record;
		}
		
		public void setActive(boolean active)
		{
			this.shouldTick = active;
		}
		
		public boolean isActive()
		{
			return this.shouldTick;
		}
	}
}
