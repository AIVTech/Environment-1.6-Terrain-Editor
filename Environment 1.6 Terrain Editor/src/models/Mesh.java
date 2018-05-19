package models;

public class Mesh {
	
	private int vaoID;
	private int vertexCount;
	
	public Mesh(int id, int vertexCount) {
		this.vaoID = id;
		this.vertexCount = vertexCount;
	}

	public int getVaoID() {
		return vaoID;
	}

	public int getVertexCount() {
		return vertexCount;
	}

}
