package engine;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import camera.EditorCamera;
import display.DisplayManager;
import loaders.StaticLoader;
import rendering.CoreRenderer;
import terrain.Terrain;
import terrain.TerrainLoader;
import terrain.TerrainTexture;
import terrain.TerrainTexturePack;
import utility.MousePicker;

public class TerrainEditor {

	private static Thread glThread;

	private static List<Terrain> terrains;
	private static Terrain[][] terrainGrid;
	private static StaticLoader loader;
	private static CoreRenderer renderer;
	private static Terrain terrain;
	private static MousePicker mousePicker;
	private static EditorCamera camera;

	private static void stopGL() {
		try {
			glThread.join();
		} catch (InterruptedException e) {
			// handle exception
			e.printStackTrace();
		}
	}

	private static void saveTerrainFile(String outputPath) {
		terrains.get(0).writeTerrainDataToFile(outputPath, terrains);
	}

	private static void openTerrainFile(String filepath) {
		terrains = TerrainLoader.loadFromFile(filepath, loader);
		terrainGrid = TerrainLoader.getTerrainGrid(terrains);
		mousePicker.setTerrainGrid(terrainGrid);
	}

	private static void loadNewTerrain() {
		terrains.clear();
		TerrainTexture backTexture = loader.loadTerrainTexture("Assets/Textures/darkGrass.jpg");
		TerrainTexture rTexture = loader.loadTerrainTexture("Assets/Textures/darkGrass.png");
		TerrainTexture gTexture = loader.loadTerrainTexture("Assets/Textures/darkGrass.jpg");
		TerrainTexture bTexture = loader.loadTerrainTexture("Assets/Textures/darkGrass.jpg");
		TerrainTexture blendMap = loader.loadTerrainTexture("Assets/Textures/defaultBlendMap.png");
		TerrainTexturePack texPack = new TerrainTexturePack(backTexture, rTexture, gTexture, bTexture);
		terrain = new Terrain(0, 0, loader, texPack, blendMap);
		terrains.add(terrain);
		terrainGrid = TerrainLoader.getTerrainGrid(terrains);
		mousePicker.setTerrainGrid(terrainGrid);
	}

	private static void startMain() {
		glThread = new Thread(new Runnable() {
			public void run() {
				new DisplayManager().embedDisplay();
				DisplayManager.createDisplay();
				/*****************************************************/
				// ================= PREPARATIONS ====================//

				loader = new StaticLoader();
				renderer = new CoreRenderer();

				terrains = new ArrayList<Terrain>();
				
				TerrainTexture backTexture = loader.loadTerrainTexture("Assets/Textures/darkGrass.jpg");
				TerrainTexture rTexture = loader.loadTerrainTexture("Assets/Textures/darkGrass.png");
				TerrainTexture gTexture = loader.loadTerrainTexture("Assets/Textures/darkGrass.jpg");
				TerrainTexture bTexture = loader.loadTerrainTexture("Assets/Textures/darkGrass.jpg");
				TerrainTexture blendMap = loader.loadTerrainTexture("Assets/Textures/defaultBlendMap.png");
				
				TerrainTexturePack texPack = new TerrainTexturePack(backTexture, rTexture, gTexture, bTexture);

				terrain = new Terrain(0, 0, loader, texPack, blendMap);
				terrains.add(terrain);
				
				terrainGrid = TerrainLoader.getTerrainGrid(terrains);
				
				camera = new EditorCamera();

				mousePicker = new MousePicker(camera, CoreRenderer.getProjectionMatrix(), terrainGrid);

				// ***************************************************//

				while (!Display.isCloseRequested()) {

					// Logic
					camera.update();
					updateWorld();

					mousePicker.update();
					Vector3f rayPosition = mousePicker.getCurrentTerrainPoint();
					if (rayPosition != null) {
						renderer.setRayPosition(rayPosition);
					}

					// Rendering
					renderer.prepare();
					renderer.renderScene(terrains, camera);

					// Processing
					ProcessWindowInput();

					DisplayManager.updateDisplay();
				}

				// Clean Up
				renderer.cleanUp();
				loader.cleanUp();

				DisplayManager.closeDisplay();
				stopGL();
				System.exit(0);
			}
		}, "LWJGL Thread");

		glThread.start();
	}

	public static void main(String[] args) {
		startMain();
	}

	private static void ProcessWindowInput() {
		if (DisplayManager.brushEnabled) { 								// Edit the terrain height in a highlighted spot
			if (Mouse.isButtonDown(1)) {
				Terrain terr = mousePicker.getCurrentTerrain();
				if (terr != null) {
					terr.changeVerticesHeight(mousePicker.getCurrentTerrainPoint(), DisplayManager.brushRadius,
							DisplayManager.brushForce, DisplayManager.editingTransformationMode);
				}
			}
		}
	}

	private static void updateWorld() {
		if (DisplayManager.loadNewBlendMap) { 							// Load new blend map
			DisplayManager.loadNewBlendMap = false;
			Terrain terr = mousePicker.getCurrentTerrain();
			if (terr == null) {
				return;
			}
			terr.loadBlendMap(DisplayManager.blendMapFilePath, loader);
		}
		
		if (DisplayManager.updateTerrainTextures) {						// Update terrain textures
			Terrain terr = mousePicker.getCurrentTerrain();
			if (terr == null) {
				return;
			}
			terr.loadBackgroundTexture(DisplayManager.backTexFilePath, loader);
			terr.loadRTexture(DisplayManager.rTexFilePath, loader);
			terr.loadGTexture( DisplayManager.gTexFilePath, loader);
			terr.loadBTexture(DisplayManager.bTexFilePath, loader);
			DisplayManager.updateTerrainTextures = false;
		}

		if (DisplayManager.wireframeMode) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE); 	// Enable wireframe mode
		} else {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL); 	// Disable wireframe mode
		}

		if (DisplayManager.changeBrushColor) { 							// Load new brush color
			renderer.setBrushColor(DisplayManager.brushColor);
			DisplayManager.changeBrushColor = false;
		}

		if (DisplayManager.changeBrushRadius) { 						// Update brush radius
			renderer.setBrushRadius(DisplayManager.brushRadius);
			DisplayManager.changeBrushRadius = false;
		}

		if (DisplayManager.saveTerrainFile) { 							// Save terrain file
			DisplayManager.saveTerrainFile = false;
			saveTerrainFile(DisplayManager.outputPath);
		}
		
		if (DisplayManager.brushEnabled) {								// Enable brush
			renderer.setBrushState(true);
		}
		else {															// Disable brush
			renderer.setBrushState(false);
		}

		if (DisplayManager.openTerrainFile) { 							// Open terrain file
			DisplayManager.openTerrainFile = false;
			openTerrainFile(DisplayManager.openTerrainFilePath);
		}

		if (DisplayManager.loadNewTerrain) {							// Load new flat terrain
			DisplayManager.loadNewTerrain = false;
			loadNewTerrain();
		}
		
		if (DisplayManager.addAnotherTerrain) {							// Add another terrain
			DisplayManager.addAnotherTerrain = false;
			TerrainTexture grassTex = loader.loadTerrainTexture("Assets/Textures/darkGrass.jpg");
			TerrainTexturePack pack = new TerrainTexturePack(grassTex, grassTex, grassTex, grassTex);
			Terrain terrain = new Terrain(DisplayManager.terrainPosX, DisplayManager.terrainPosZ, loader, pack, loader.loadTerrainTexture(""));
			terrains.add(terrain);
			terrainGrid = TerrainLoader.getTerrainGrid(terrains);
			mousePicker.setTerrainGrid(terrainGrid);
		}
		
		if (DisplayManager.deleteTerrain) {								// Delete terrain
			DisplayManager.deleteTerrain = false;
			for (Terrain terr : terrains) {
				if (terr.gridX == DisplayManager.terrainPosX && terr.gridZ == DisplayManager.terrainPosZ) {
					terrains.remove(terr);
					terrainGrid = TerrainLoader.getTerrainGrid(terrains);
					mousePicker.setTerrainGrid(terrainGrid);
					break;
				}
			}
		}
		
		if (DisplayManager.moveTerrain) {								// Move terrain
			DisplayManager.moveTerrain = false;
			for (Terrain terr : terrains) {
				if (terr.gridX == mousePicker.getCurrentTerrain().gridX && terr.gridZ == mousePicker.getCurrentTerrain().gridZ) {
					terr.setPosX(DisplayManager.terrainPosX);
					terr.setPosZ(DisplayManager.terrainPosZ);
					terrainGrid = TerrainLoader.getTerrainGrid(terrains);
					mousePicker.setTerrainGrid(terrainGrid);
					break;
				}
			}
		}
	}

}
