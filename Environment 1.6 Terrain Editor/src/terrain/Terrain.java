package terrain;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

public class Terrain {

	public static final float SIZE = 1000;
	private int VERTEX_COUNT = 512;
	private float highestPoint = 0;
	private float lowestPoint = 0;
	private float[][] heights;

	private float x, z;
	public int gridX, gridZ;
	private Mesh mesh;
	private TerrainTexturePack texturePack;
	private TerrainTexture blendMap;
	private int[] verticesVboID = new int[1];
	
	public String blendMapFilePath = "none";
	public String backTexFilePath = "none";
	public String rTexFilePath = "none";
	public String gTexFilePath = "none";
	public String bTexFilePath = "none";

	public List<TerrainPoint> vertices = new ArrayList<TerrainPoint>();

	public Terrain(int gridX, int gridZ, StaticLoader loader, TerrainTexturePack texturePack, TerrainTexture blendMap) {
		this.texturePack = texturePack;
		this.blendMap = blendMap;
		this.gridX = gridX;
		this.gridZ = gridZ;
		this.x = gridX * SIZE;
		this.z = gridZ * SIZE;
		this.mesh = generateFlatTerrain(loader);
	}
	
	public Terrain() {
		
	}
	
	public void setPosX(int gridX) {
		this.x = gridX * SIZE;
		this.gridX = gridX;
	}
	
	public void setPosZ(int gridZ) {
		this.z = gridZ * SIZE;
		this.gridZ = gridZ;
	}
	
	public void setMesh(Mesh mesh) {
		this.mesh = mesh;
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

	public void loadFlatTerrain(StaticLoader loader) {
		this.mesh = generateFlatTerrain(loader);
	}

	private Mesh generateFlatTerrain(StaticLoader loader) {
		vertices.clear();
		int count = VERTEX_COUNT * VERTEX_COUNT;
		heights = new float[VERTEX_COUNT][VERTEX_COUNT];
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
				heights[j][i] = pointHeight;
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

	private void processVertexHeight(TerrainPoint vertex) {
		if (vertex.getHeight() > highestPoint) {
			highestPoint = vertex.getHeight();
		} else if (vertex.getHeight() < lowestPoint) {
			lowestPoint = vertex.getHeight();
		}
	}

	private void transformVertexHeightSinusoid(TerrainPoint vertex, float rayX, float rayHeight, float rayZ,
			float radius, float maxHeight) {
		float distanceToVertex = distanceBetweenTwoVectors(new Vector3f(vertex.x, vertex.getHeight(), vertex.z),
				new Vector3f(rayX, rayHeight, rayZ));
		if (distanceToVertex < radius) {
			float targetHeight = maxHeight * (float) (Math.cos((Math.PI / (2 * radius)) * distanceToVertex))
					+ vertex.getHeight();
			vertex.setHeight(targetHeight);
			heights[(int) (vertex.x / SIZE)][(int) (vertex.z / SIZE)] = (int) vertex.getHeight();
		}
	}

	private void transformVertexHeightSharp(TerrainPoint vertex, float rayX, float rayHeight, float rayZ, float radius,
			float maxHeight) {
		if (inRange(new Vector3f(vertex.x, vertex.getHeight(), vertex.z), new Vector3f(rayX, rayHeight, rayZ),
				radius)) {
			vertex.setHeight(vertex.getHeight() + maxHeight);
			heights[(int) (vertex.x / SIZE)][(int) (vertex.z / SIZE)] = (int) vertex.getHeight();
		}
	}

	private void transformVertexHeightEraser(TerrainPoint vertex, float rayX, float rayHeight, float rayZ, float radius,
			float maxHeight) {
		if (inRange(new Vector3f(vertex.x, vertex.getHeight(), vertex.z), new Vector3f(rayX, rayHeight, rayZ),
				radius)) {
			vertex.setHeight(0);
			heights[(int) (vertex.x / SIZE)][(int) (vertex.z / SIZE)] = (int) vertex.getHeight();
		}
	}

	public void changeVerticesHeight(Vector3f rayPosition, float radius, float maxHeight, String transformationMode) { // MAIN
																														// METHOD
																														// FOR
																														// EDITING
																														// THE
																														// TERRAIN
		if (rayPosition == null || radius < 0.001) {
			return;
		}
		float rayX = rayPosition.x;
		float rayZ = rayPosition.z;
		if (rayZ < 0) {
			rayZ *= -1;
		}
		rayZ = SIZE - rayZ;
		rayX = rayPosition.x - this.x;
		rayZ = rayPosition.z - this.z;
		for (TerrainPoint vertex : vertices) {
			switch (transformationMode) {
			case "sharp":
				transformVertexHeightSharp(vertex, rayX, rayPosition.y, rayZ, radius, maxHeight); // Sharp
				break;
			case "sinusoidal":
				transformVertexHeightSinusoid(vertex, rayX, rayPosition.y, rayZ, radius, maxHeight); // Sinusoidal
				break;
			case "eraser":
				transformVertexHeightEraser(vertex, rayX, rayPosition.y, rayZ, radius, maxHeight); // Eraser for Terrain
				break;
			default:
				transformVertexHeightSharp(vertex, rayX, rayPosition.y, rayZ, radius, maxHeight); // Default (Sharp)
				break;
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

	public TerrainTexturePack getTexturePack() {
		return texturePack;
	}
	
	public TerrainTexture getBlendMap() {
		return blendMap;
	}

	public float getHeightOfTerrain(float worldX, float worldZ) {
		float terrainX = worldX - this.x;
		float terrainZ = worldZ - this.z;
		float gridSquareSize = SIZE / ((float) heights.length - 1);
		int gridX = (int) Math.floor(terrainX / gridSquareSize);
		int gridZ = (int) Math.floor(terrainZ / gridSquareSize);

		if (gridX >= heights.length - 1 || gridZ >= heights.length - 1 || gridX < 0 || gridZ < 0) {
			return 0;
		}

		float xCoord = (terrainX % gridSquareSize) / gridSquareSize;
		float zCoord = (terrainZ % gridSquareSize) / gridSquareSize;
		return heights[(int) xCoord][(int) zCoord];
	}

	public Mesh loadPreBuiltTerrain(List<Float> vertexFloats, List<Float> normalArray, int vertexCount,
			StaticLoader loader) {
		vertices.clear();
		this.VERTEX_COUNT = vertexCount;
		int count = VERTEX_COUNT * VERTEX_COUNT;
		heights = new float[VERTEX_COUNT][VERTEX_COUNT];
		float[] vertexArray = new float[count * 3];
		float[] normals = new float[count * 3];
		float[] textureCoords = new float[count * 2];
		int[] indices = new int[6 * (VERTEX_COUNT - 1) * (VERTEX_COUNT - 1)];
		int vertexPointer = 0;
		for (int i = 0; i < VERTEX_COUNT; i++) {
			for (int j = 0; j < VERTEX_COUNT; j++) {
				float pointHeight = vertexFloats.get(vertexPointer * 3 + 1);
				TerrainPoint vertex = new TerrainPoint(vertexFloats.get(vertexPointer * 3),
						vertexFloats.get(vertexPointer * 3 + 2));
				heights[j][i] = pointHeight;
				vertex.setHeight(pointHeight);
				vertices.add(vertex);
				normals[vertexPointer * 3] = normalArray.get(vertexPointer * 3);
				normals[vertexPointer * 3 + 1] = normalArray.get(vertexPointer * 3 + 1);
				normals[vertexPointer * 3 + 2] = normalArray.get(vertexPointer * 3 + 2);
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

	public void parseTerrainFile(String filepath, StaticLoader loader) {
		
	}

	public void writeTerrainDataToFile(String outputPath, List<Terrain> terrains) {
		File outputFile = new File(outputPath);
		try {
			outputFile.createNewFile();
			FileWriter fw = new FileWriter(outputFile, false);
			for (Terrain ter : terrains) {
				int gridPosX = (int) (ter.x / Terrain.SIZE);
				int gridPosZ = (int) (ter.z / Terrain.SIZE);
				fw.write("-NEW_TERRAIN\n");
				fw.write("-vertex_count " + ter.VERTEX_COUNT + "\n");
				fw.write("-grid_position " + gridPosX + " " + gridPosZ + "\n");
				fw.write("-blend_map " + ter.blendMapFilePath + "\n");
				fw.write("-background_texture " + ter.backTexFilePath + "\n");
				fw.write("-r_texture " + ter.rTexFilePath + "\n");
				fw.write("-g_texture " + ter.gTexFilePath + "\n");
				fw.write("-b_texture " + ter.bTexFilePath + "\n");
				for (TerrainPoint vertex : ter.vertices) {
					float xPos = vertex.x, zPos = vertex.z, height = vertex.getHeight();
					fw.write("-point " + xPos + " " + height + " " + zPos + "\n");
				}
				fw.write("-TERRAIN_END\n");
			}
			fw.write("\n-end");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadNewTexturePack(String backTex, String rTex, String gTex, String bTex, StaticLoader loader) {
		loadBackgroundTexture(backTex, loader);
		loadRTexture(rTex, loader);
		loadGTexture(gTex, loader);
		loadBTexture(bTex, loader);
	}

	public void loadBlendMap(String blendMapFilePath, StaticLoader loader) {
		this.blendMap = loader.loadTerrainTexture("Assets/Textures/" + blendMapFilePath);
		this.blendMapFilePath = blendMapFilePath;
	}
	
	public void loadRTexture(String filepath, StaticLoader loader) {
		TerrainTexture rTexture = loader.loadTerrainTexture("Assets/Textures/" + filepath);
		TerrainTexture backTex = this.texturePack.getBackgroundTexture();
		TerrainTexture gTex = this.texturePack.getgTexture();
		TerrainTexture bTex = this.texturePack.getbTexture();
		this.texturePack = new TerrainTexturePack(backTex, rTexture, gTex, bTex);
		this.rTexFilePath = filepath;
	}
	
	public void loadGTexture(String filepath, StaticLoader loader) {
		TerrainTexture gTexture = loader.loadTerrainTexture("Assets/Textures/" + filepath);
		TerrainTexture backTex = this.texturePack.getBackgroundTexture();
		TerrainTexture rTex = this.texturePack.getrTexture();
		TerrainTexture bTex = this.texturePack.getbTexture();
		this.texturePack = new TerrainTexturePack(backTex, rTex, gTexture, bTex);
		this.gTexFilePath = filepath;
	}
	
	public void loadBTexture(String filepath, StaticLoader loader) {
		TerrainTexture bTexture = loader.loadTerrainTexture("Assets/Textures/" + filepath);
		TerrainTexture backTex = this.texturePack.getBackgroundTexture();
		TerrainTexture rTex = this.texturePack.getrTexture();
		TerrainTexture gTex = this.texturePack.getgTexture();
		this.texturePack = new TerrainTexturePack(backTex, rTex, gTex, bTexture);
		this.bTexFilePath = filepath;
	}
	
	public void loadBackgroundTexture(String filepath, StaticLoader loader) {
		TerrainTexture backTexture = loader.loadTerrainTexture("Assets/Textures/" + filepath);
		TerrainTexture rTex = this.texturePack.getrTexture();
		TerrainTexture gTex = this.texturePack.getgTexture();
		TerrainTexture bTex = this.texturePack.getbTexture();
		this.texturePack = new TerrainTexturePack(backTexture, rTex, gTex, bTex);
		this.backTexFilePath = filepath;
	}
}