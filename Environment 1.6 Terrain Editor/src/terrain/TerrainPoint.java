package terrain;

import org.lwjgl.util.vector.Vector3f;

public class TerrainPoint {

	public float x, z;
	private float height = 0;
	public Vector3f worldPosition;

	public TerrainPoint(float x, float z) {
		this.x = x;
		this.z = z;
		this.worldPosition = new Vector3f(x, height, z);
	}

	public boolean arePointsClose(TerrainPoint point) {
		if (point.x < this.x + 2 && point.x > this.x - 2 && 
			point.z < this.z + 2 && point.z > this.z - 2) {
			return true;
		}
		return false;
	}
	
	public void setHeight(float value) {
		this.height = value;
		this.worldPosition.y = value;
	}
	
	public float getHeight() {
		return this.height;
	}
}
