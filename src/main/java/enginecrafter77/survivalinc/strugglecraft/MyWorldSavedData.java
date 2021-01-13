package enginecrafter77.survivalinc.strugglecraft;

import java.util.ArrayList;

import enginecrafter77.survivalinc.SurvivalInc;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public class MyWorldSavedData extends WorldSavedData {
	private static final String DATA_NAME = SurvivalInc.MOD_ID + "_WorldData";

	ArrayList<WorshipPlace> worshipPlaces = new ArrayList<WorshipPlace>();

	// Required constructors
	public MyWorldSavedData() {
		super(DATA_NAME);
	}

	public MyWorldSavedData(String s) {
		super(s);
	}

	public static MyWorldSavedData get(World world) {
		// The IS_GLOBAL constant is there for clarity, and should be simplified into
		// the right branch.
//		  MapStorage storage = IS_GLOBAL ? world.getMapStorage() : world.getPerWorldStorage();
		MapStorage storage = world.getMapStorage();
		MyWorldSavedData instance = (MyWorldSavedData) storage.getOrLoadData(MyWorldSavedData.class, DATA_NAME);

		if (instance == null) {
			instance = new MyWorldSavedData();
			storage.setData(DATA_NAME, instance);
		}
		return instance;
	}

	public void addWorshipPlace(BlockPos pos, int value) {
		worshipPlaces.add(new WorshipPlace(pos, value));
		markDirty();
	}

	public void removeWorshipPlace(BlockPos pos) {
		System.out.println("DEBUG: Trying to remove Worship Place at "+pos);

		for (WorshipPlace place : worshipPlaces)
			if (place.getPosition().equals(pos)) {
				worshipPlaces.remove(place);
				markDirty();
				System.out.println("DEBUG: Found and removed");
				return;
			}

		System.out.println("DEBUG: ERROR can't remove worship place at " + pos);
	}

	public ArrayList<WorshipPlace> getWorshipPlaces() {
		return worshipPlaces;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		int arr[] = nbt.getIntArray("worshiparr");

		for (int i = 0; i < arr.length; i += 4) {
			worshipPlaces.add(new WorshipPlace(new BlockPos(arr[i], arr[i + 1], arr[i + 2]), arr[i + 3]));
		}

		System.out.println("DEBUG: Loaded " + worshipPlaces.size() + " worship places");

	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		int[] arr = new int[worshipPlaces.size() * 4];

		int i = 0;
		for (WorshipPlace place : worshipPlaces) {
			arr[i++] = place.getPosition().getX();
			arr[i++] = place.getPosition().getY();
			arr[i++] = place.getPosition().getZ();
			arr[i++] = place.getValue();
		}

		nbt.setIntArray("worshiparr", arr);

		System.out.println("DEBUG: Saved " + worshipPlaces.size() + " worship places");

		return nbt;
	}

}
