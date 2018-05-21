package terrain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

	private final float SIZE = 4096;
	private int VERTEX_COUNT = 1024;
	private float highestPoint = 0;
	private float lowestPoint = 0;
	private float[][] heights;

	private float x, z;
	private Mesh mesh;
	private TerrainTexturePack texturePack;
	private TerrainTexture blendMap;
	private int[] verticesVboID = new int[1];
	
	private String blendMapFilePath = "";
	private String backTexFilePath = "";
	private String rTexFilePath = "";
	private String gTexFilePath = "";
	private String bTexFilePath = "";

	public List<TerrainPoint> vertices = new ArrayList<TerrainPoint>();

	public Terrain(int gridX, int gridZ, StaticLoader loader, TerrainTexturePack texturePack, TerrainTexture blendMap) {
		this.texturePack = texturePack;
		this.blendMap = blendMap;
		this.x = gridX * SIZE;
		this.z = gridZ * SIZE;
		this.mesh = generateFlatTerrain(loader);
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
			vertex.setHeight(maxHeight);
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

	private Mesh loadPreBuiltTerrain(List<Float> vertexFloats, List<Float> normalArray, int vertexCount,
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

	public void loadFromFile(String filepath, StaticLoader loader) {
		try {
			FileReader isr = null;
			File terFile = new File(filepath);

			try {
				isr = new FileReader(terFile);
			} catch (FileNotFoundException e) {
				return;
			}

			BufferedReader reader = new BufferedReader(isr);
			String line;

			try {
				String vertexCountLine = reader.readLine();
				String[] parsedLine = vertexCountLine.split("\\s+");
				int vertex_count = Integer.valueOf(parsedLine[1]);

				List<Float> vertexFloats = new ArrayList<Float>();
				List<Float> normals = new ArrayList<Float>();
				while (true) {
					line = reader.readLine();
					try {
						if (line.startsWith("-blend_map ")) {
							String[] currentLine = line.split("\\s+");
							String filename = currentLine[1];
							loadBlendMap(filename, loader);
						}
						if (line.startsWith("-background_texture ")) {
							String[] currentLine = line.split("\\s+");
							String filename = currentLine[1];
							loadBackgroundTexture(filename, loader);
						}
						if (line.startsWith("-r_texture ")) {
							String[] currentLine = line.split("\\s+");
							String filename = currentLine[1];
							loadRTexture(filename, loader);
						}
						if (line.startsWith("-g_texture ")) {
							String[] currentLine = line.split("\\s+");
							String filename = currentLine[1];
							loadGTexture(filename, loader);
						}
						if (line.startsWith("-b_texture ")) {
							String[] currentLine = line.split("\\s+");
							String filename = currentLine[1];
							loadBTexture(filename, loader);
						}
						
						if (line.startsWith("-point ")) {
							String[] currentLine = line.split("\\s+");
							float xPos = Float.valueOf(currentLine[1]);
							float height = Float.valueOf(currentLine[2]);
							float zPos = Float.valueOf(currentLine[3]);
							vertexFloats.add(xPos);
							vertexFloats.add(height);
							vertexFloats.add(zPos);
							normals.add(0f);
							normals.add(1f);
							normals.add(0f);
						}
						if (line.startsWith("-end")) {
							break;
						}
					} catch (NullPointerException ne) {
						break;
					}
				}

				this.mesh = loadPreBuiltTerrain(vertexFloats, normals, vertex_count, loader);
			} catch (IOException e) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
				return;
			}
			try {
				reader.close();
			} catch (IOException e) {
			}
		} catch (Exception ex) {

		}
	}

	public void writeTerrainDataToFile(String outputPath) {
		File outputFile = new File(outputPath);
		try {
			outputFile.createNewFile();
			FileWriter fw = new FileWriter(outputFile, false);
			fw.write("vertex_count " + VERTEX_COUNT + "\n");
			fw.write("-blend_map " + blendMapFilePath + "\n");
			fw.write("-background_texture " + backTexFilePath + "\n");
			fw.write("-r_texture " + rTexFilePath + "\n");
			fw.write("-g_texture " + gTexFilePath + "\n");
			fw.write("-b_texture " + bTexFilePath + "\n");
			for (TerrainPoint vertex : vertices) {
				float xPos = vertex.x, zPos = vertex.z, height = vertex.getHeight();
				fw.write("-point " + xPos + " " + height + " " + zPos + "\n");
			}
			fw.write("\n-end");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadBlendMap(String blendMapFilePath, StaticLoader loader) {
		this.blendMap = loader.loadTerrainTexture("Assets/Textures/" + blendMapFilePath);
		this.blendMapFilePath = blendMapFilePath;
	}
	
	public void loadRTexture(String filepath, StaticLoader loader) {
		this.texturePack.setrTexture(loader.loadTerrainTexture("Assets/Textures/" + filepath));
		this.rTexFilePath = filepath;
	}
	
	public void loadGTexture(String filepath, StaticLoader loader) {
		this.texturePack.setgTexture(loader.loadTerrainTexture("Assets/Textures/" + filepath));
		this.gTexFilePath = filepath;
	}
	
	public void loadBTexture(String filepath, StaticLoader loader) {
		this.texturePack.setbTexture(loader.loadTerrainTexture("Assets/Textures/" + filepath));
		this.bTexFilePath = filepath;
	}
	
	public void loadBackgroundTexture(String filepath, StaticLoader loader) {
		this.texturePack.setBackgroundTexture(loader.loadTerrainTexture("Assets/Textures/" + filepath));
		this.backTexFilePath = filepath;
	}
}
