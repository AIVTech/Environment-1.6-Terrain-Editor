package models;

public class StaticModel {
	
	private Mesh mesh;
	private MeshTexture texture;
	
	public StaticModel(Mesh mesh, MeshTexture texture) {
		this.mesh = mesh;
		this.texture = texture;
	}

	public Mesh getMesh() {
		return mesh;
	}

	public MeshTexture getTexture() {
		return texture;
	}

}
