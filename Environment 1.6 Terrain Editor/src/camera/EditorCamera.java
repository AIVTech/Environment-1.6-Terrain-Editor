package camera;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

import display.DisplayManager;

public class EditorCamera {

	private Vector3f position = new Vector3f(0, 98.8f, 0);
	private float pitch, yaw, roll;

	// movement variables
	private float walkingSpeed = 60;
	private float runningSpeed = 100;
	private float movementSpeed = walkingSpeed;

	// mouse movement variables
	float mouseSensitivity = 0.08f;
	float xoffset = 0, yoffset = 0;

	public EditorCamera() {
		yaw += 45;
	}

	public void update() {
		processKeyboardInput();
		processMouseInput();
	}

	private void calculateMouseOffsets() {
		xoffset = Mouse.getDX();
		yoffset = -Mouse.getDY();
	}

	private void processMouseInput() {
		float velocity = movementSpeed * DisplayManager.getFrameTimeSeconds();
		
		int dWheel = Mouse.getDWheel();
		if (dWheel < 0) {
			position.y -= movementSpeed * velocity / 2;
		}
		if (dWheel > 0) {
			position.y += movementSpeed * velocity / 2;
		}
		
		
		if (Mouse.isButtonDown(0)) {
			calculateMouseOffsets();
			xoffset *= mouseSensitivity;
			yoffset *= mouseSensitivity;
			
			yaw -= xoffset;
			pitch -= yoffset;
		}

		// Limit checking
		if (pitch > 89.0f)
			pitch = 89.0f;
		if (pitch < -89.0f)
			pitch = -89.0f;
	}

	private void processKeyboardInput() {

		float velocity = movementSpeed * DisplayManager.getFrameTimeSeconds();
		
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			movementSpeed = runningSpeed;
		}
		else {
			movementSpeed = walkingSpeed;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			position.x += Math.sin(Math.toRadians(yaw)) * velocity;
			position.z -= Math.cos(Math.toRadians(yaw)) * velocity;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			position.x -= Math.sin(Math.toRadians(yaw)) * velocity;
			position.z += Math.cos(Math.toRadians(yaw)) * velocity;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			position.x += Math.sin(Math.toRadians(yaw - 90)) * velocity;
			position.z -= Math.cos(Math.toRadians(yaw - 90)) * velocity;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			position.x += Math.sin(Math.toRadians(yaw + 90)) * velocity;
			position.z -= Math.cos(Math.toRadians(yaw + 90)) * velocity;
		}
	}

	/******** GETTERS AND SETTERS *********/

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
	}

	public float getRoll() {
		return roll;
	}

	public void setRoll(float roll) {
		this.roll = roll;
	}

}
