package shaders;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import camera.EditorCamera;
import utility.Maths;

public class TerrainShader extends ShaderBase {
	
	private static final String VERTEX_FILE = "src/shaders/terrainVertexShader.txt";
	private static final String FRAGMENT_FILE = "src/shaders/terrainFragmentShader.txt";
	
	private int transformationMatrixLocation;
	private int projectionMatrixLocation;
	private int viewMatrixLocation;
	
	private int brushEnabledLocation;
	private int brushColorLocation;
	private int brushRadiusLocation;
	private int rayPositionLocation;
	
	private int backgroundTextureLocation;
	private int rTextureLocation;
	private int	gTextureLocation;
	private int bTextureLocation;
	private int blendMapLocation;
	
	public TerrainShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void getAllUniformLocations() {
		transformationMatrixLocation = super.getUniformLocation("transformationMatrix");
		projectionMatrixLocation = super.getUniformLocation("projectionMatrix");
		viewMatrixLocation = super.getUniformLocation("viewMatrix");
		brushEnabledLocation = super.getUniformLocation("brushEnabled");
		brushColorLocation = super.getUniformLocation("brushColor");
		rayPositionLocation = super.getUniformLocation("rayPosition");
		brushRadiusLocation = super.getUniformLocation("brushRadius");
		backgroundTextureLocation = super.getUniformLocation("backgroundTexture");
		rTextureLocation = super.getUniformLocation("rTexture");
		gTextureLocation = super.getUniformLocation("gTexture");
		bTextureLocation = super.getUniformLocation("bTexture");
		blendMapLocation = super.getUniformLocation("blendMap");
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "vertexPosition");
		super.bindAttribute(1, "textureCoordinate");
		super.bindAttribute(2, "normalVector");
	}
	
	public void connectTextureUnits() {
		super.loadInt(backgroundTextureLocation, 0);
		super.loadInt(rTextureLocation, 1);
		super.loadInt(gTextureLocation, 2);
		super.loadInt(bTextureLocation, 3);
		super.loadInt(blendMapLocation, 4);
	}
	
	public void loadTransformationMatrix(Matrix4f matrix) {
		super.loadMatrix4(transformationMatrixLocation, matrix);
	}
	
	public void loadProjectionMatrix(Matrix4f matrix) {
		super.loadMatrix4(projectionMatrixLocation, matrix);
	}
	
	public void loadViewMatrix(EditorCamera camera) {
		Matrix4f matrix = Maths.createViewMatrix(camera);
		super.loadMatrix4(viewMatrixLocation, matrix);
	}
	
	public void loadBrushEnabledState(boolean state) {
		super.loadBoolean(brushEnabledLocation, state);
	}
	
	public void loadBrushColor(Vector4f color) {
		super.loadVector(brushColorLocation, color);
	}
	
	public void loadRayPosition(Vector3f pos) {
		super.loadVector(rayPositionLocation, pos);
	}
	
	public void loadBrushRadius(float radius) {
		super.loadFloat(brushRadiusLocation, radius);
	}
}
