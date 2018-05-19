package display;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Vector4f;

public class DisplayManager {

	//private static int DISPLAY_WIDTH = 1200;
	//private static int DISPLAY_HEIGHT = 700;
	private static int DISPLAY_WIDTH = 1700;
	private static int DISPLAY_HEIGHT = 1000;
	private static int preferedFPS_CAP = 250;

	private static long lastFrameTime;
	private static float delta;

	private static Canvas canvas;

	//private int frameWidth = 1700;
	//private int frameHeight = 1000;
	private int frameWidth = 1700;
	private int frameHeight = 1000;
	//private int displayWidth = 1200;
	//private int displayHeight = 700;
	private int displayWidth = 1700;
	private int displayHeight = 1000;
	
	/* static controlling variables */
	public static String textureFilePath = "";
	public static boolean loadNewTexture = false;
	public static boolean wireframeMode = false;
	
	// editing brush options
	public static boolean brushEnabled = false;
	public static boolean changeBrushState = false;
	public static Vector4f brushColor = new Vector4f(1.0f, 1.0f, 0.0f, 1.0f);
	public static boolean changeBrushColor = false;
	public static float brushRadius = 40.0f;
	public static boolean changeBrushRadius = false;
	
	// brush colors
	private ArrayList<Vector4f> colorValues = new ArrayList<Vector4f>();
	private ArrayList<String> colorNames = new ArrayList<String>();

	/**
	 * @wbp.parser.entryPoint
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void embedDisplay() {
		final JFrame frame = new JFrame("Envrironment World Editor");
		frame.setSize(frameWidth, frameHeight);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				int result = JOptionPane.showConfirmDialog(frame, "Do you want to quit the Application?");
				if (result == JOptionPane.OK_OPTION) {
					frame.setVisible(false);
					frame.dispose(); // canvas's removeNotify() will be called
					System.exit(0);
				}
			}
		});

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(null);
		frame.setBackground(Color.DARK_GRAY);
		frame.getContentPane().setBackground(Color.DARK_GRAY);
		mainPanel.setBackground(Color.DARK_GRAY);

		/************** ALL THE GUI STUFF HERE **********************/
		colorValues.add(new Vector4f(1.0f, 1.0f, 0.0f, 1.0f));	// yellow
		colorValues.add(new Vector4f(0.0f, 1.0f, 0.0f, 1.0f));	// green
		colorValues.add(new Vector4f(1.0f, 0.0f, 0.0f, 1.0f));	// red
		colorValues.add(new Vector4f(0.0f, 0.0f, 1.0f, 1.0f));	// blue
		
		colorNames.add("Yellow");
		colorNames.add("Green");
		colorNames.add("Red");
		colorNames.add("Blue");
		
		brushColor = colorValues.get(0);
		
		JButton loadTextureBtn = new JButton("Load Texture");
		loadTextureBtn.setBounds(1280, 60, 140, 30);
		loadTextureBtn.setVisible(true);
		mainPanel.add(loadTextureBtn);
		loadTextureBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				File workingDirectory = new File(System.getProperty("user.dir"));
				fileChooser.setCurrentDirectory(workingDirectory);
				int result = fileChooser.showOpenDialog(frame);
				String texturePath = "";
				if(result == JFileChooser.OPEN_DIALOG) {
					texturePath = fileChooser.getSelectedFile().getAbsolutePath().toString();
					textureFilePath = texturePath;
					loadNewTexture = true;
                }
			}
		});
		
		JButton toggleWireframeBtn = new JButton("Enable Wireframe Mode");
		toggleWireframeBtn.setBounds(1440, 60, 180, 30);
		toggleWireframeBtn.setVisible(true);
		mainPanel.add(toggleWireframeBtn);
		toggleWireframeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (wireframeMode) {
					wireframeMode = false;
					toggleWireframeBtn.setText("Enable Wireframe Mode");
				}
				else {
					wireframeMode = true;
					toggleWireframeBtn.setText("Disable Wireframe Mode");
				}
			}
		});
		
		JButton toggleBrushBtn = new JButton("Enable Editing Brush");
		toggleBrushBtn.setBounds(1280, 140, 160, 30);
		toggleBrushBtn.setVisible(true);
		mainPanel.add(toggleBrushBtn);
		toggleBrushBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (toggleBrushBtn.getText().equals("Disable Editing Brush")) {
					toggleBrushBtn.setText("Enable Editing Brush");
					changeBrushState = true;
				}
				else {
					toggleBrushBtn.setText("Disable Editing Brush");
					changeBrushState = true;
				}
			}
		});
		
		JComboBox colorCombobox = new JComboBox();
		colorCombobox.setBounds(1480, 140, 120, 30);
		colorCombobox.setModel(new DefaultComboBoxModel(colorNames.toArray()));
		colorCombobox.setSelectedIndex(0);
		colorCombobox.setEditable(false);
		colorCombobox.setVisible(true);
		mainPanel.add(colorCombobox);
		colorCombobox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = colorCombobox.getSelectedIndex();
				brushColor = colorValues.get(index);
				changeBrushColor = true;
			}
		});
		
		JLabel brushRadiusLbl = new JLabel("Brush Radius");
		brushRadiusLbl.setBounds(1380, 220, 120, 30);
		brushRadiusLbl.setHorizontalAlignment(SwingConstants.CENTER);
		brushRadiusLbl.setForeground(Color.DARK_GRAY);
		brushRadiusLbl.setVisible(true);
		mainPanel.add(brushRadiusLbl);
		
		JSlider brushRadiusSlider = new JSlider();
		brushRadiusSlider.setBounds(1340, 250, 200, 30);
		brushRadiusSlider.setBackground(Color.DARK_GRAY);
		brushRadiusSlider.setMinimum(1);
		brushRadiusSlider.setMaximum(240);
		brushRadiusSlider.setValue(20);
		brushRadiusSlider.setVisible(true);
		mainPanel.add(brushRadiusSlider);
		brushRadiusSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				brushRadius = (float)brushRadiusSlider.getValue();
				changeBrushRadius = true;
			}
		});
		
		// Menu Bars
		final JMenuBar topMenuBar = new JMenuBar();
		frame.setJMenuBar(topMenuBar);
		
		// Menus
		JMenu fileMenu = new JMenu("File");
		topMenuBar.add(fileMenu);
		
		JMenu editMenu = new JMenu("Edit");
		topMenuBar.add(editMenu);
		
		// File Menu Items
		JMenuItem newFile_MenuItem = new JMenuItem("New Flat Terrain");
		fileMenu.add(newFile_MenuItem);
		
		JMenuItem openFile_MenuItem = new JMenuItem("Open File");
		fileMenu.add(openFile_MenuItem);
		fileMenu.addSeparator();
		
		JMenuItem saveFile_MenuItem = new JMenuItem("Save");
		fileMenu.add(saveFile_MenuItem);
		
		JMenuItem saveFileAs_MenuItem = new JMenuItem("Save As...");
		fileMenu.add(saveFileAs_MenuItem);
		
		// Edit Menu Items
		JMenuItem undo_MenuItem = new JMenuItem("Undo");
		editMenu.add(undo_MenuItem);
		
		JMenuItem redo_MenuItem = new JMenuItem("Redo");
		editMenu.add(redo_MenuItem);
		
		/******** ###################################### *************///

		canvas = new Canvas() {
			private static final long serialVersionUID = 1L;

		};
		canvas.setPreferredSize(new Dimension(displayWidth, displayHeight));
		canvas.setIgnoreRepaint(true);

		try {
			Display.setParent(canvas);
		} catch (LWJGLException e) {
			// handle exception
			e.printStackTrace();
		}
		JPanel canvasPanel = new JPanel();
		canvasPanel.add(canvas);
		canvasPanel.setBounds(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT);
		mainPanel.add(canvasPanel);

		frame.getContentPane().add(mainPanel);
		frame.setResizable(false);

		// frame.pack();
		frame.setVisible(true);
	}

	public static void createDisplay() {

		ContextAttribs attribs = new ContextAttribs(4, 4).withForwardCompatible(true).withProfileCore(true);

		try {
			Display.setParent(canvas);
			Display.setDisplayMode(new DisplayMode(DISPLAY_WIDTH, DISPLAY_HEIGHT));
			Display.create(new PixelFormat().withDepthBits(24), attribs); // setting the format
			Display.setTitle("Environment 1.6 Terrain Editor"); // setting the title
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

		// telling OpenGL where to setup the display
		GL11.glViewport(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT);
		lastFrameTime = getCurrentTime();
	}

	public static void updateDisplay() {
		Display.sync(preferedFPS_CAP); // lets it sync with a constant fps to avoid lag
		Display.update();
		long currentFrameTime = getCurrentTime();
		delta = (currentFrameTime - lastFrameTime) / 1000f; // this will give us the time it took the frame to render in
															// seconds.
		lastFrameTime = currentFrameTime;
	}

	public static float getFrameTimeSeconds() {
		return delta;
	}

	public static void closeDisplay() {
		Display.destroy();
		// any additional closing statements

	}

	private static long getCurrentTime() {
		return Sys.getTime() * 1000 / Sys.getTimerResolution();
	}

}
