package rendering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import camera.EditorCamera;
import entities.Entity;
import models.StaticModel;
import shaders.TerrainShader;
import terrain.Terrain;

public class CoreRenderer {

	private TerrainShader terrainShader = new TerrainShader();
	private TerrainRenderer terrainRenderer;

	private static Matrix4f projectionMatrix;
	private float fov = 60;
	private float near_plane = 0.1f;
	private float far_plane = 3000.0f;

	private Map<StaticModel, List<Entity>> entities = new HashMap<StaticModel, List<Entity>>();
	private List<Terrain> terrains = new ArrayList<Terrain>();

	public CoreRenderer() {
		createProjectionMatrix();
		terrainRenderer = new TerrainRenderer(terrainShader, projectionMatrix);
		
		terrainShader.start();
		terrainShader.loadBrushEnabledState(false);
		terrainShader.loadBrushColor(new Vector4f(1.0f, 1.0f, 0.0f, 1.0f));
		terrainShader.loadBrushRadius(40.0f);
		terrainShader.stop();
	}

	public void prepare() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(0.4f, 0.4f, 0.4f, 1.0f);
	}

	public void processEntity(Entity entity) {
		StaticModel entityModel = entity.getModel();
		List<Entity> batch = entities.get(entityModel);
		if (batch != null) {
			batch.add(entity);
		} else {
			List<Entity> newBatch = new ArrayList<Entity>();
			newBatch.add(entity);
			entities.put(entityModel, newBatch);
		}
	}

	public void renderScene(List<Terrain> terrains, EditorCamera camera) {
		for (Terrain terrain : terrains) {
			processTerrain(terrain);
		}
		render(camera);
	}
	
	public void render(EditorCamera camera) {
		prepare();
		terrainShader.start();
		terrainShader.loadViewMatrix(camera);
		terrainRenderer.render(terrains);
		terrainShader.stop();
		terrains.clear();
	}
	
	public void processTerrain(Terrain terrain) {
        terrains.add(terrain);
    }

	public void cleanUp() {
		terrainShader.cleanUp();
	}
	
	public static Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}

	private void createProjectionMatrix() {
		float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
		float y_scale = (float) ((1f / Math.tan(Math.toRadians(fov / 2f))) * aspectRatio);
		float x_scale = y_scale / aspectRatio;
		float frustum_length = far_plane - near_plane;

		projectionMatrix = new Matrix4f();
		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((far_plane + near_plane) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * near_plane * far_plane) / frustum_length);
		projectionMatrix.m33 = 0;
	}
	
	public void setBrushState(boolean state) {
		terrainShader.start();
		terrainShader.loadBrushEnabledState(state);
		terrainShader.stop();
	}
	
	public void setBrushColor(Vector4f color) {
		terrainShader.start();
		terrainShader.loadBrushColor(color);
		terrainShader.stop();
	}
	
	public void setRayPosition(Vector3f pos) {
		terrainShader.start();
		terrainShader.loadRayPosition(pos);
		terrainShader.stop();
	}
	
	public void setBrushRadius(float radius) {
		terrainShader.start();
		terrainShader.loadBrushRadius(radius);
		terrainShader.stop();
	}
}
