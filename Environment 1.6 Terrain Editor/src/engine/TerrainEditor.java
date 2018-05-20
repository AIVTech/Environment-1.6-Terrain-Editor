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
import utility.MousePicker;

public class TerrainEditor {

	private static Thread glThread;
	
	private static List<Terrain> terrains;
	private static StaticLoader loader;
	private static CoreRenderer renderer;
	private static Terrain terrain;
	private static MousePicker mousePicker;
	private static EditorCamera camera;
	private static boolean editingMode = false;

	private static void stopGL() {
		try {
			glThread.join();
		} catch (InterruptedException e) {
			// handle exception
			e.printStackTrace();
		}
	}
	
	private static void setTerrainTexture(String textureFilepath) {
		terrains.get(0).setTexture(textureFilepath, loader);
	}
	
	private static void saveTerrainFile(String outputPath) {
		terrains.get(0).writeTerrainDataToFile(outputPath);
	}
	
	private static void openTerrainFile(String filepath) {
		terrains.get(0).loadFromFile(filepath, loader);
	}
	
	private static void loadNewTerrain() {
		terrains.get(0).loadFlatTerrain(loader);
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
				
				terrain = new Terrain(0, -1, loader, loader.loadMeshTexture("Assets/Textures/darkGrass.jpg"));
				terrains.add(terrain);

				camera = new EditorCamera();
				
				mousePicker = new MousePicker(camera, CoreRenderer.getProjectionMatrix(), terrain);

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
		if (editingMode) {												// Edit the terrain height in a highlighted spot
			if (Mouse.isButtonDown(1)) {
				terrain.changeVerticesHeight(mousePicker.getCurrentTerrainPoint(), DisplayManager.brushRadius, DisplayManager.brushForce, DisplayManager.editingTransformationMode);
			}
		}
	}
	
	private static void updateWorld() {
		if (DisplayManager.loadNewTexture) {							// Load Terrain Texture
			DisplayManager.loadNewTexture = false;
			setTerrainTexture(DisplayManager.textureFilePath);
		}
		
		if (DisplayManager.wireframeMode) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);	// Enable wireframe mode
		} else {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);	// Disable wireframe mode
		}
		
		if (DisplayManager.changeBrushState) {							// Toggle brush

			if (DisplayManager.brushEnabled) {
				renderer.setBrushState(false);
				DisplayManager.brushEnabled = false;
				editingMode = false;
			}
			else {
				renderer.setBrushState(true);
				DisplayManager.brushEnabled = true;
				editingMode = true;
			}
			
			DisplayManager.changeBrushState = false;
		}
		
		if (DisplayManager.changeBrushColor) {							// Load new brush color
			renderer.setBrushColor(DisplayManager.brushColor);
			DisplayManager.changeBrushColor = false;
		}
		
		if (DisplayManager.changeBrushRadius) {							// Update brush radius
			renderer.setBrushRadius(DisplayManager.brushRadius);
			DisplayManager.changeBrushRadius = false;
		}
		
		if (DisplayManager.saveTerrainFile) {							// Save terrain file
			DisplayManager.saveTerrainFile = false;
			saveTerrainFile(DisplayManager.outputPath);
		}
		
		if (DisplayManager.openTerrainFile) {							// Open terrain file
			DisplayManager.openTerrainFile = false;
			openTerrainFile(DisplayManager.openTerrainFilePath);
		}
		
		if (DisplayManager.loadNewTerrain) {
			DisplayManager.loadNewTerrain = false;
			loadNewTerrain();
		}
	}

}
