package net.schoperation.schopcraft.cap.wetness;

public interface IWetness {
	
	// basic crap we can do with wetness
	
	public void increase(float amount);
	public void decrease(float amount);
	public void set(float amount);
	public float getWetness();
	public float getMaxWetness();

}