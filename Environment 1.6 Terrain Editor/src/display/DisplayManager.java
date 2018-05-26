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
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Vector4f;

public class DisplayManager {

	// private static int DISPLAY_WIDTH = 1200;
	// private static int DISPLAY_HEIGHT = 700;
	private static int DISPLAY_WIDTH = 1700;
	private static int DISPLAY_HEIGHT = 1000;
	private static int preferedFPS_CAP = 250;

	private static long lastFrameTime;
	private static float delta;

	private static Canvas canvas;

	private int frameWidth = 1700;
	private int frameHeight = 1000;

	private int displayWidth = 1700;
	private int displayHeight = 1000;

	/* static controlling variables */
	public static String blendMapFilePath = "";
	public static boolean loadNewBlendMap = false;
	public static boolean wireframeMode = false;

	// editing brush options
	public static boolean brushEnabled = false;
	public static Vector4f brushColor = new Vector4f(1.0f, 1.0f, 0.0f, 1.0f);
	public static boolean changeBrushColor = false;
	public static float brushRadius = 40.0f;
	public static boolean changeBrushRadius = false;
	public static float brushForce = 1.0f;
	public static String editingTransformationMode = "sharp";

	// brush colors
	private ArrayList<Vector4f> colorValues = new ArrayList<Vector4f>();
	private ArrayList<String> colorNames = new ArrayList<String>();

	public static String outputPath = null;
	public static boolean saveTerrainFile = false;
	public static String openTerrainFilePath = "";
	public static boolean openTerrainFile = false;
	public static boolean loadNewTerrain = false;
	private boolean firstTimeLoad = true;
	
	public static String backTexFilePath = "";
	public static String rTexFilePath = "";
	public static String gTexFilePath = "";
	public static String bTexFilePath = "";
	public static boolean updateTerrainTextures = false;
	
	public static int newTerrainPosX = -999;
	public static int newTerrainPosZ = -999;
	public static boolean addAnotherTerrain = false;

	private JFrame frame;

	/**
	 * @wbp.parser.entryPoint
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void embedDisplay() {
		frame = new JFrame("Envrironment Terrain Editor");
		frame.setSize(frameWidth, frameHeight);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				int result = JOptionPane.showConfirmDialog(frame, "Do you want to quit the Application?", null,
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null);
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
		colorValues.add(new Vector4f(1.0f, 1.0f, 0.0f, 1.0f)); // yellow
		colorValues.add(new Vector4f(1.0f, 0.0f, 0.0f, 1.0f)); // red
		colorValues.add(new Vector4f(0.0f, 1.0f, 0.0f, 1.0f)); // green
		colorValues.add(new Vector4f(0.0f, 0.0f, 1.0f, 1.0f)); // blue

		colorNames.add("Yellow");
		colorNames.add("Red");
		colorNames.add("Green");
		colorNames.add("Blue");

		brushColor = colorValues.get(0);

		JButton loadTextureBtn = new JButton("Load BlendMap");
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
				String blendMapPath = "";
				if (result == JFileChooser.OPEN_DIALOG) {
					blendMapPath = fileChooser.getSelectedFile().getName();
					blendMapFilePath = blendMapPath;
					loadNewBlendMap = true;
				}
			}
		});

		JButton updateTexturesBtn = new JButton("Load New Textures");
		updateTexturesBtn.setBounds(1350, 590, 180, 30);
		updateTexturesBtn.setVisible(true);
		mainPanel.add(updateTexturesBtn);
		updateTexturesBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String filepath = "";
				int result = promptUser("Update Background Texture?");
				if (result == 0) {
					filepath = chooseFile();
					if (!filepath.equals("")) {
						backTexFilePath = filepath;
					}
				}
				result = promptUser("Update R Texture?");
				if (result == 0) {
					filepath = chooseFile();
					if (!filepath.equals("")) {
						rTexFilePath = filepath;
					}
				}
				result = promptUser("Update G Texture?");
				if (result == 0) {
					filepath = chooseFile();
					if (!filepath.equals("")) {
						gTexFilePath = filepath;
					}
				}
				result = promptUser("Update B Texture?");
				if (result == 0) {
					filepath = chooseFile();
					if (!filepath.equals("")) {
						bTexFilePath = filepath;
					}
				}
				
				// Update Textures
				updateTerrainTextures = true;
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
				} else {
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
					brushEnabled = false;
				} else {
					toggleBrushBtn.setText("Disable Editing Brush");
					brushEnabled = true;
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
		brushRadiusSlider.setMaximum(100);
		brushRadiusSlider.setValue(10);
		brushRadiusSlider.setVisible(true);
		mainPanel.add(brushRadiusSlider);
		brushRadiusSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				brushRadius = (float) brushRadiusSlider.getValue();
				changeBrushRadius = true;
			}
		});

		JLabel brushForceLbl = new JLabel("Brush Force");
		brushForceLbl.setBounds(1380, 320, 120, 30);
		brushForceLbl.setHorizontalAlignment(SwingConstants.CENTER);
		brushForceLbl.setForeground(Color.DARK_GRAY);
		brushForceLbl.setVisible(true);
		mainPanel.add(brushForceLbl);

		JSlider brushForceSlider = new JSlider();
		brushForceSlider.setBounds(1340, 360, 200, 30);
		brushForceSlider.setBackground(Color.DARK_GRAY);
		brushForceSlider.setMinimum(-20);
		brushForceSlider.setMaximum(20);
		brushForceSlider.setValue(10);
		brushForceSlider.setMajorTickSpacing(1);
		brushForceSlider.setPaintTicks(true);
		brushForceSlider.setVisible(true);
		mainPanel.add(brushForceSlider);
		brushForceSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				float force = (float) brushForceSlider.getValue();
				force /= 10;
				if (force == 0) {
					force = 2.0f;
				}
				brushForce = force;
			}
		});

		JRadioButton sharpModeRB = new JRadioButton("Sharp");
		sharpModeRB.setBounds(1380, 430, 120, 30);
		sharpModeRB.setVisible(true);
		sharpModeRB.setSelected(true);
		mainPanel.add(sharpModeRB);

		JRadioButton sinusoidalModeRB = new JRadioButton("Sinusoidal");
		sinusoidalModeRB.setBounds(1380, 460, 120, 30);
		sinusoidalModeRB.setVisible(true);
		sinusoidalModeRB.setSelected(false);
		mainPanel.add(sinusoidalModeRB);

		JRadioButton eraserModeRB = new JRadioButton("Eraser");
		eraserModeRB.setBounds(1380, 490, 120, 30);
		eraserModeRB.setVisible(true);
		eraserModeRB.setSelected(false);
		mainPanel.add(eraserModeRB);

		JRadioButton smoothingModeRB = new JRadioButton("Smoothing Tool");
		smoothingModeRB.setBounds(1380, 520, 120, 30);
		smoothingModeRB.setVisible(true);
		smoothingModeRB.setSelected(false);
		mainPanel.add(smoothingModeRB);

		sharpModeRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sharpModeRB.setSelected(true);
				sinusoidalModeRB.setSelected(false);
				eraserModeRB.setSelected(false);
				smoothingModeRB.setSelected(false);
				editingTransformationMode = "sharp";
			}
		});

		sinusoidalModeRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sinusoidalModeRB.setSelected(true);
				sharpModeRB.setSelected(false);
				eraserModeRB.setSelected(false);
				smoothingModeRB.setSelected(false);
				editingTransformationMode = "sinusoidal";
			}
		});

		eraserModeRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				eraserModeRB.setSelected(true);
				sharpModeRB.setSelected(false);
				sinusoidalModeRB.setSelected(false);
				smoothingModeRB.setSelected(false);
				editingTransformationMode = "eraser";
			}
		});

		smoothingModeRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				smoothingModeRB.setSelected(true);
				eraserModeRB.setSelected(false);
				sharpModeRB.setSelected(false);
				sinusoidalModeRB.setSelected(false);
				editingTransformationMode = "smoothing";
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
		
		JMenu addTerrainMenu = new JMenu("Terrain");
		topMenuBar.add(addTerrainMenu);

		// File Menu Items
		JMenuItem newFile_MenuItem = new JMenuItem("New Project");
		newFile_MenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				loadNewTerrain = true;
			}
		});
		fileMenu.add(newFile_MenuItem);

		JMenuItem openFile_MenuItem = new JMenuItem("Load Project");
		openFile_MenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				File workingDirectory = new File(System.getProperty("user.dir"));
				fileChooser.setCurrentDirectory(workingDirectory);
				int result = fileChooser.showOpenDialog(frame);
				if (result == JFileChooser.OPEN_DIALOG) {
					openTerrainFilePath = fileChooser.getSelectedFile().getAbsolutePath().toString();
					openTerrainFile = true;
				}
			}
		});
		fileMenu.add(openFile_MenuItem);
		fileMenu.addSeparator();

		JMenuItem saveFile_MenuItem = new JMenuItem("Save");
		saveFile_MenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (outputPath == null) {
					// Choosing output directory
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fileChooser.setFileFilter(new FileNameExtensionFilter("Terrain File", ".ter"));
					File workingDirectory = new File(System.getProperty("user.dir"));
					fileChooser.setCurrentDirectory(workingDirectory);
					int result = fileChooser.showSaveDialog(frame);
					if (result == JFileChooser.OPEN_DIALOG) {
						outputPath = fileChooser.getSelectedFile().getAbsolutePath().toString();
						if (!outputPath.endsWith(".ter")) {
							outputPath += ".ter";
						}

						if (firstTimeLoad) {
							if (new File(outputPath).exists()) {
								int choice = JOptionPane.showConfirmDialog(frame,
										"File already exists. Are you sure you want to overwrite?", null,
										JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null);
								if (choice != 0) {
									// User DOES NOT want to overwrite
									return;
								}
							}
							firstTimeLoad = false;
						}

						saveTerrainFile = true;
					}
				} else {
					saveTerrainFile = true;
				}
			}
		});
		fileMenu.add(saveFile_MenuItem);

		JMenuItem saveFileAs_MenuItem = new JMenuItem("Save As...");
		saveFileAs_MenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Choosing output directory
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setFileFilter(new FileNameExtensionFilter("Terrain File", ".ter"));
				File workingDirectory = new File(System.getProperty("user.dir"));
				fileChooser.setCurrentDirectory(workingDirectory);
				int result = fileChooser.showSaveDialog(frame);
				if (result == JFileChooser.OPEN_DIALOG) {
					outputPath = fileChooser.getSelectedFile().getAbsolutePath().toString();
					if (!outputPath.endsWith(".ter")) {
						outputPath += ".ter";
					}

					if (new File(outputPath).exists()) {
						int choice = JOptionPane.showConfirmDialog(frame,
								"File already exists. Are you sure you want to overwrite?", null,
								JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null);
						if (choice != 0) {
							// User DOES NOT want to overwrite
							return;
						}
					}

					saveTerrainFile = true;
				}
			}
		});
		fileMenu.add(saveFileAs_MenuItem);

		// Edit Menu Items
		JMenuItem undo_MenuItem = new JMenuItem("Undo");
		editMenu.add(undo_MenuItem);

		JMenuItem redo_MenuItem = new JMenuItem("Redo");
		editMenu.add(redo_MenuItem);
		
		JMenuItem addTerrain_MenuItem = new JMenuItem("Add Terrain");
		addTerrain_MenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// prompt user for position of the terrain
				newTerrainPosX = Integer.parseInt(JOptionPane.showInputDialog("Enter Grid Position X: "));
				newTerrainPosZ = Integer.parseInt(JOptionPane.showInputDialog("Enter Grid Position Z: "));
				addAnotherTerrain = true;
			}
		});
		addTerrainMenu.add(addTerrain_MenuItem);

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

	private int promptUser(String prompt) {
		int choice = JOptionPane.showConfirmDialog(frame, prompt, null, JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null);
		return choice;
	}
	
	private String chooseFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		File workingDirectory = new File(System.getProperty("user.dir"));
		fileChooser.setCurrentDirectory(workingDirectory);
		int result = fileChooser.showOpenDialog(frame);
		String filePath = "";
		if (result == JFileChooser.OPEN_DIALOG) {
			filePath = fileChooser.getSelectedFile().getName();
		}
		return filePath;
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
