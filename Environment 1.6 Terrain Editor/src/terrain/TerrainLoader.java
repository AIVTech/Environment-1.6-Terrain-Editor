package terrain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import loaders.StaticLoader;

public class TerrainLoader {
	
	public static List<Terrain> loadFromFile(String filepath, StaticLoader loader) {
		List<Terrain> terrains = new ArrayList<Terrain>();
		try {
			FileReader isr = null;
			File terFile = new File(filepath);

			try {
				isr = new FileReader(terFile);
			} catch (FileNotFoundException e) {
				return terrains;
			}

			BufferedReader reader = new BufferedReader(isr);
			String line;

			try {
				List<Float> vertexFloats = new ArrayList<Float>();
				List<Float> normals = new ArrayList<Float>();
				int vertex_count = 0;
				Terrain terrain = null;
				TerrainTexture grassTex = loader.loadTerrainTexture("");
				TerrainTexturePack pack = new TerrainTexturePack(grassTex, grassTex, grassTex, grassTex);
				while (true) {
					line = reader.readLine();
					try {
						if (line.startsWith("-NEW_TERRAIN")) {
							terrain = new Terrain(-999, -999, loader, pack, loader.loadTerrainTexture(""));
						}
						if (line.startsWith("-TERRAIN_END")) {
							if (terrain != null) {
								terrain.setMesh(terrain.loadPreBuiltTerrain(vertexFloats, normals, vertex_count, loader));
								terrains.add(terrain);
								vertexFloats.clear();
								normals.clear();
							}
						}
						if (line.startsWith("-vertex_count ")) {
							String[] parsedLine = line.split("\\s+");
							vertex_count = Integer.valueOf(parsedLine[1]);
						}
						if (line.startsWith("-grid_position ")) {
							String[] parsedPositionLine = line.split("\\s+");
							terrain.setPosX(Integer.valueOf(parsedPositionLine[1]));
							terrain.setPosZ(Integer.valueOf(parsedPositionLine[2]));
						}
						if (line.startsWith("-blend_map ")) {
							String[] currentLine = line.split("\\s+");
							String filename = currentLine[1];
							terrain.loadBlendMap(filename, loader);
						}
						if (line.startsWith("-background_texture ")) {
							String[] currentLine = line.split("\\s+");
							String filename = currentLine[1];
							terrain.loadBackgroundTexture(filename, loader);
						}
						if (line.startsWith("-r_texture ")) {
							String[] currentLine = line.split("\\s+");
							String filename = currentLine[1];
							terrain.loadRTexture(filename, loader);
						}
						if (line.startsWith("-g_texture ")) {
							String[] currentLine = line.split("\\s+");
							String filename = currentLine[1];
							terrain.loadGTexture(filename, loader);
						}
						if (line.startsWith("-b_texture ")) {
							String[] currentLine = line.split("\\s+");
							String filename = currentLine[1];
							terrain.loadBTexture(filename, loader);
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
					} catch (NullPointerException npe) {
						npe.printStackTrace();
						break;
					}
				}
			} catch (IOException e) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
				return terrains;
			}
			try {
				reader.close();
			} catch (IOException e) {
			}
		} catch (Exception ex) {

		}
		System.out.println("FINAL SIZE: " + terrains.size());
		return terrains;
	}
	
	public static Terrain[][] getTerrainGrid(List<Terrain> terrains) {
		Terrain[][] grid = new Terrain[100][100];
		for (Terrain terrain : terrains) {
			int x = (int) (terrain.getX() / Terrain.SIZE);
			int z = (int) (terrain.getZ() / Terrain.SIZE);
			grid[x][z] = terrain;
		}
		return grid;
	}
	
}
