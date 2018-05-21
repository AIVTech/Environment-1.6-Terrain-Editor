package terrain;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.lwjgl.util.vector.Vector3f;

public class BlendMapCreator {

	private static BufferedImage image = null;
	private static final int IMAGE_SIZE = 512;

	public static void createBlackBlendMap(String filename) {
		image = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);

		for (int r = 0; r < IMAGE_SIZE; r++)
			for (int c = 0; c < IMAGE_SIZE; c++) {
				int red = 0;
				int green = 0;
				int blue = 0;
				int rgb = (red << 16) | (green << 8) | blue;
				image.setRGB(c, r, rgb);
			}
		try {
			ImageIO.write(image, "png", new File("Assets/Textures/" + filename + ".png"));
		} catch (Exception e) { }
	}
	
	public static void createCustomBlendMap(String filename, Vector3f[][] colors) {
		image = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);
		for (int r = 0; r < IMAGE_SIZE; r++)
			for (int c = 0; c < IMAGE_SIZE; c++) {
				Vector3f vertexColor = colors[r][c];
				int red = (int) vertexColor.x;
				int green = (int) vertexColor.y;
				int blue = (int) vertexColor.z;
				int rgb = (red << 16) | (green << 8) | blue;
				image.setRGB(c, r, rgb);
			}
		try {
			ImageIO.write(image, "png", new File("Assets/Textures/" + filename + ".png"));
		} catch (Exception e) { }
	}

}
