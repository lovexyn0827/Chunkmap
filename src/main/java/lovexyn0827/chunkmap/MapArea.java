package lovexyn0827.chunkmap;

import net.minecraft.util.math.ChunkPos;

public class MapArea {
	final int sizeX;
	final int sizeZ;
	final int minX;
	final int minZ;
	
	public MapArea(int sizeX, int sizeZ, int minX, int minZ) {
		this.sizeX = sizeX;
		this.sizeZ = sizeZ;
		this.minX = minX;
		this.minZ = minZ;
	}
	
	public MapArea move(int dx, int dz) {
		return new MapArea(this.sizeX, this.sizeZ, this.minX + dx, this.minZ + dz);
	}
	
	public MapArea fromCenter(int x, int z) {
		return new MapArea(this.sizeX, this.sizeZ, x - (this.sizeX / 2), z - (this.sizeZ / 2));
	}
	
	public MapArea resize(int x, int z) {
		return new MapArea(x, z, this.minX, this.minZ);
	}
	
	public void forEach(ChunkConsumer action) {
		for(int dx = 0; dx < this.sizeX; dx++) {
			for(int dz = 0; dz < this.sizeZ; dz++) {
				action.accept(this.minX + dx, this.minZ + dz, dx, dz);
			}
		}
	}

	public ChunkPos getOriginPos() {
		return new ChunkPos(this.minX, this.minZ);
	}
	
	public interface ChunkConsumer {
		void accept(int x, int z, int dx, int dz);
	}
}
