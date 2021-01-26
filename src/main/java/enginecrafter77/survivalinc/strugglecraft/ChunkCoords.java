package enginecrafter77.survivalinc.strugglecraft;

public class ChunkCoords {
	public int posX;
	public int posZ;
	private int fHashCode;
	
	public ChunkCoords(int x, int z) {
		posX = x;
		posZ = z;
	}
	

	@Override
	public int hashCode() {
		if (fHashCode == 0) {
			  int result = 0;
			  result = posX*10000;
			  result += posZ;
			  fHashCode = result;
		}
		return fHashCode;
	}
	
	@Override
	public boolean equals(Object other) {
		ChunkCoords that = (ChunkCoords) other;
		return that.posX == posX && that.posZ == posZ;	
	}
	
	@Override
	public String toString() {
		return "("+posX+","+posZ+")";
	}
}