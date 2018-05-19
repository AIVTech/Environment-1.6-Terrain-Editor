package terrain;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector3f;

import loaders.StaticLoader;
import models.Mesh;
import models.MeshTexture;

public class Terrain {

	private final float SIZE = 4096;
	private final int VERTEX_COUNT = 1024;
	private float highestPoint = 0;
	private float lowestPoint = 0;

	private float x, z;
	private Mesh mesh;
	private MeshTexture texture;
	private int[] verticesVboID = new int[1];

	public List<TerrainPoint> vertices = new ArrayList<TerrainPoint>();

	public Terrain(int gridX, int gridZ, StaticLoader loader, MeshTexture texture) {
		this.texture = texture;
		this.x = gridX * SIZE;
		this.z = gridZ * SIZE;
		this.mesh = generateTerrain(loader);
	}
	
	public void setTexture(String filename, StaticLoader loader) {
		this.texture = loader.loadMeshTexture(filename);
	}

	public void updateVertices() {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, verticesVboID[0]);
		FloatBuffer buffer = toFloatBuffer(getVerticesFromList(vertices));
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STREAM_DRAW);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	private FloatBuffer toFloatBuffer(float[] data) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}

	private Mesh generateTerrain(StaticLoader loader) {
		vertices.clear();
		int count = VERTEX_COUNT * VERTEX_COUNT;
		float[] vertexArray = new float[count * 3];
		float[] normals = new float[count * 3];
		float[] textureCoords = new float[count * 2];
		int[] indices = new int[6 * (VERTEX_COUNT - 1) * (VERTEX_COUNT - 1)];
		int vertexPointer = 0;
		for (int i = 0; i < VERTEX_COUNT; i++) {
			for (int j = 0; j < VERTEX_COUNT; j++) {
				float pointHeight = 0;
				TerrainPoint vertex = new TerrainPoint((float) j / ((float) VERTEX_COUNT - 1) * SIZE,
						(float) i / ((float) VERTEX_COUNT - 1) * SIZE);
				vertex.setHeight(pointHeight);
				vertices.add(vertex);
				normals[vertexPointer * 3] = 0;
				normals[vertexPointer * 3 + 1] = 1;
				normals[vertexPointer * 3 + 2] = 0;
				textureCoords[vertexPointer * 2] = (float) j / ((float) VERTEX_COUNT - 1);
				textureCoords[vertexPointer * 2 + 1] = (float) i / ((float) VERTEX_COUNT - 1);
				vertexPointer++;
			}
		}
		int pointer = 0;
		for (int gz = 0; gz < VERTEX_COUNT - 1; gz++) {
			for (int gx = 0; gx < VERTEX_COUNT - 1; gx++) {
				int topLeft = (gz * VERTEX_COUNT) + gx;
				int topRight = topLeft + 1;
				int bottomLeft = ((gz + 1) * VERTEX_COUNT) + gx;
				int bottomRight = bottomLeft + 1;
				indices[pointer++] = topLeft;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = topRight;
				indices[pointer++] = topRight;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = bottomRight;
			}
		}

		vertexArray = getVerticesFromList(vertices);

		return loader.loadTerrainMesh(vertexArray, textureCoords, normals, indices, verticesVboID);
	}

	private float[] getVerticesFromList(List<TerrainPoint> vertices) {
		float[] vertexArray = new float[vertices.size() * 3];
		int vertexPointer = 0;
		for (TerrainPoint vertex : vertices) {
			vertexArray[vertexPointer * 3] = vertex.x;
			vertexArray[vertexPointer * 3 + 1] = vertex.getHeight();
			vertexArray[vertexPointer * 3 + 2] = vertex.z;
			vertexPointer++;
		}
		return vertexArray;
	}

	private float distanceBetweenTwoVectors(Vector3f first, Vector3f second) {
		float xDiff = (first.x - second.x) * (first.x - second.x);
		float zDiff = (first.z - second.z) * (first.z - second.z);
		float distance = (float) Math.sqrt(xDiff + zDiff);
		return distance;
	}

	public boolean inRange(Vector3f first, Vector3f second, float radius) {
		return distanceBetweenTwoVectors(first, second) < radius;
	}

	public Vector3f worldToGridCoords(Vector3f vertex) {
		return new Vector3f(vertex.x / SIZE, vertex.y, vertex.z / SIZE);
	}

	public Vector3f gridToWorldCoords(Vector3f vertex) {
		return new Vector3f(vertex.x * SIZE, vertex.y, (1 - vertex.z) * SIZE);
	}

	public float worldToGridFloat(float value) {
		return value / SIZE;
	}

	public float gridToWorldFloat(float value) {
		return value * SIZE;
	}

	private void processVertexHeight(TerrainPoint vertex) {
		if (vertex.getHeight() > highestPoint) {
			highestPoint = vertex.getHeight();
		} else if (vertex.getHeight() < lowestPoint) {
			lowestPoint = vertex.getHeight();
		}
	}

	public void changeVerticesHeight(Vector3f rayPosition, float targetRadius, float maxHeight) { // MAIN METHOD FOR EDITING THE TERRAIN
		if (rayPosition == null || targetRadius < 0.001) {
			return;
		}
		float rayX = rayPosition.x;
		float rayZ = rayPosition.z;
		if (rayZ < 0) {
			rayZ *= -1;
		}
		rayZ = SIZE - rayZ;
		for (TerrainPoint vertex : vertices) {
			if (inRange(new Vector3f(vertex.x, vertex.getHeight(), vertex.z), new Vector3f(rayX, rayPosition.y, rayZ), targetRadius)) {
				vertex.setHeight(vertex.getHeight() + maxHeight);
			}
			processVertexHeight(vertex);
		}
		updateVertices();
	}

	public float getX() {
		return x;
	}

	public float getZ() {
		return z;
	}

	public Mesh getMesh() {
		return mesh;
	}

	public MeshTexture getTexture() {
		return texture;
	}

	public float getHeightOfTerrain(float x, float z) {
		// return (highestPoint);
		return 0;
	}
}
