package terrain;

public class TerrainTexturePack {

	private TerrainTexture backgroundTexture;
	private TerrainTexture rTexture;
	private TerrainTexture gTexture;
	private TerrainTexture bTexture;

	public TerrainTexturePack(TerrainTexture backgroundTexture, TerrainTexture rTexture, TerrainTexture gTexture,
			TerrainTexture bTexture) {
		this.backgroundTexture = backgroundTexture;
		this.rTexture = rTexture;
		this.gTexture = gTexture;
		this.bTexture = bTexture;
	}

	public TerrainTexture getBackgroundTexture() {
		return backgroundTexture;
	}

	public TerrainTexture getrTexture() {
		return rTexture;
	}

	public TerrainTexture getgTexture() {
		return gTexture;
	}

	public TerrainTexture getbTexture() {
		return bTexture;
	}

	public void setBackgroundTexture(TerrainTexture backgroundTexture) {
		this.backgroundTexture = backgroundTexture;
	}

	public void setrTexture(TerrainTexture rTexture) {
		this.rTexture = rTexture;
	}

	public void setgTexture(TerrainTexture gTexture) {
		this.gTexture = gTexture;
	}

	public void setbTexture(TerrainTexture bTexture) {
		this.bTexture = bTexture;
	}
}