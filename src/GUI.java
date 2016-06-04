import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class GUI extends JFrame{
	private final int PANE_PADDING = 10;
//	private PriorityQueue<Node> order;
	private int currentID;
	
	//Menu bar
	private JMenuBar menuBar;
	private JMenu file;
	private JMenu edit;
	private JMenu tools;
	private JMenu help;
	
	//Tool bar (right below menu bar)
	private JToolBar toolbar;
	private JButton selectButton;
	private JButton circleButton;
	private JButton arrowButton;
	private JButton lineButton;
	private JButton panButton;
	
	//Icons for tool bar
	private final ImageIcon selectIcon;
	private final ImageIcon circleIcon;
	private final ImageIcon arrowIcon;
	private final ImageIcon lineIcon;
	private final ImageIcon panIcon;
	private final ImageIcon selectSelectedIcon;
	private final ImageIcon circleSelectedIcon;
	private final ImageIcon arrowSelectedIcon;
	private final ImageIcon lineSelectedIcon;
	private final ImageIcon panSelectedIcon;
	// Selected icons have 50% less brightness
	
	private Tool currentTool;
	private HashMap<Tool, JButton> toolButtons;
	private HashMap<Tool, ImageIcon[]> toolIcons;
	private HashMap<Character, Tool> toolShortcuts;
	
	private Circle linePoint;
	private Circle arrowPoint;
	
	//Tool options bar 
	private final ToolOptionsPanel nodeOptions;
	private ToolOptionsPanel lineOptions;
	private JPanel toolOptions;
//	private JLabel circleRadiusLabel;
//	private JTextField circleRadiusTextField;
//	private JSlider circleRadiusSlider;
//	private JLabel circleFillColorLabel;
//	private BufferedImage circleFillColorImage;
//	private ImageIcon circleFillColorIcon;
//	private JButton circleFillColorButton;
//	private Color circleFillColor;
//	private JLabel circleBorderColorLabel;
//	private BufferedImage circleBorderColorImage;
//	private ImageIcon circleBorderColorIcon;
//	private JButton circleBorderColorButton;
//	private Color circleBorderColor;
//	private JLabel circleTextColorLabel;
//	private BufferedImage circleTextColorImage;
//	private ImageIcon circleTextColorIcon;
//	private JButton circleTextColorButton;
//	private Color circleTextColor;
	
	//The currently selected item
	private GraphComponent selection;
	
	private JScrollPane panelEditorScroll;
	
	//The main panel workspace!
	private Editor panelEditor;
	
	private JPanel panelProperties;
	
	private JPanel panelStatus;
	
	private JTabbedPane propertiesCirclePane;
	private JPanel generalCircleTab;	
	private JPanel appearanceCircleTab;
	private JPanel descriptionCircleTab;
	
	private JLabel generalSelection;
	private JLabel generalSelectionID;
	private JLabel generalNodeLocation;
	private JLabel generalNodeRadius;
	private JLabel generalNodeEdges;
	private JButton generalNodeEdgesAll;
	private JLabel generalNodeIndegree;
	private JLabel generalNodeOutdegree;
	private JLabel generalArrowFrom;
	private JLabel generalArrowTo;
	
	
	
	private HashSet<Circle> circles;
	private HashSet<Line> lines;
	private HashSet<Arrow> arrows;

	private boolean redrawLine;
	
	public GUI(){
		super("Graph Builder 1.0");
		currentID = 0;
		redrawLine = false;
		
		//Initialize and fill menu bar
		menuBar = new JMenuBar();
		file = new JMenu("File");
		edit = new JMenu("Edit");
		tools = new JMenu("Tools");
		help = new JMenu("Help");
		menuBar.add(file);
		menuBar.add(edit);
		menuBar.add(tools);
		menuBar.add(help);
		setJMenuBar(menuBar);
		
		//Initialize toolbar
		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		
		//Initialize icons for tools
		selectIcon = new ImageIcon("iconSelect.png");
		circleIcon = new ImageIcon("iconCircle.png");
		arrowIcon = new ImageIcon("iconArrow.png");
		lineIcon = new ImageIcon("iconLine.png");
		panIcon = new ImageIcon("iconPan.png");
		selectSelectedIcon = new ImageIcon("iconSelectSelected.png");
		circleSelectedIcon = new ImageIcon("iconCircleSelected.png");
		arrowSelectedIcon = new ImageIcon("iconArrowSelected.png");
		lineSelectedIcon = new ImageIcon("iconLineSelected.png");
		panSelectedIcon = new ImageIcon("iconPanSelected.png");
		
		//Hash tools to both their icons
		toolIcons = new HashMap<>();
		toolIcons.put(Tool.SELECT, new ImageIcon[] {selectIcon, selectSelectedIcon});
		toolIcons.put(Tool.NODE, new ImageIcon[] {circleIcon, circleSelectedIcon});
		toolIcons.put(Tool.ARROW, new ImageIcon[] {arrowIcon, arrowSelectedIcon});
		toolIcons.put(Tool.LINE, new ImageIcon[] {lineIcon, lineSelectedIcon});
		toolIcons.put(Tool.PAN, new ImageIcon[] {panIcon, panSelectedIcon});
		
		//Initialize tool buttons and add listeners to them.
		selectButton = new JButton(selectIcon);
		selectButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				actionForToolButtons(Tool.SELECT);
				changeToolOptions(Tool.SELECT);
			}
		});
		selectButton.setToolTipText("Select Tool: Use the left mouse button to select and drag circles.");
		circleButton = new JButton(circleIcon);
		circleButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				actionForToolButtons(Tool.NODE);
				changeToolOptions(Tool.NODE);
			}
		});
		circleButton.setToolTipText("Circle Tool: Places a new circle.\nKeyboard shortcut: C");
		arrowButton = new JButton(arrowIcon);
		arrowButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				actionForToolButtons(Tool.ARROW);
				changeToolOptions(Tool.ARROW);
			}
		});
		arrowButton.setToolTipText("Arrow Tool: Draws a new arrow.\nKeyboard shortcut: A");
		lineButton = new JButton(lineIcon);
		lineButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				actionForToolButtons(Tool.LINE);
				changeToolOptions(Tool.LINE);
			}
		});		
		lineButton.setToolTipText("Line Tool: Draws a new line.\nKeyboard shortcut: L");
		panButton = new JButton(panIcon);
		panButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				actionForToolButtons(Tool.PAN);
				changeToolOptions(Tool.LINE);
			}
		});
		panButton.setToolTipText("Pan Tool: Allows you to pan the workspace by dragging the left mouse click.\nKeyboard shortcut: P");
		
		//Fill mapping from shortcut key to the appropriate tool, and add key listener for shortcuts
		toolShortcuts = new HashMap<>();
		toolShortcuts.put('s', Tool.SELECT);
		toolShortcuts.put('c', Tool.NODE);
		toolShortcuts.put('a', Tool.ARROW);
		toolShortcuts.put('l', Tool.LINE);
		toolShortcuts.put('p', Tool.PAN);
		this.addKeyListener(new KeyListener(){
			@Override
			public void keyPressed(KeyEvent arg0) {}
			@Override
			public void keyReleased(KeyEvent arg0) {}
			@Override
			public void keyTyped(KeyEvent arg0) {
				char typed = arg0.getKeyChar();
				Tool lookup = toolShortcuts.get(typed);
				if(lookup != null && currentTool != lookup){
					actionForToolButtons(lookup);
					changeToolOptions(lookup);
				}
			}
		});
		
		//Add buttons to the toolbar
		toolbar.add(selectButton);
		toolbar.add(circleButton);
		toolbar.add(arrowButton);
		toolbar.add(lineButton);
		toolbar.add(panButton);
		
		//Initialize and fill mapping from tools to the corresponding button
		toolButtons = new HashMap<>();
		toolButtons.put(Tool.SELECT, selectButton);
		toolButtons.put(Tool.NODE, circleButton);
		toolButtons.put(Tool.ARROW, arrowButton);
		toolButtons.put(Tool.LINE, lineButton);
		toolButtons.put(Tool.PAN, panButton);
		
		//Initialize and fill out the options panel
		toolOptions = new JPanel();
		toolOptions.setLayout(new FlowLayout(FlowLayout.LEADING));
		nodeOptions = new ToolOptionsPanel(this, Tool.NODE);
		lineOptions = new ToolOptionsPanel(this, Tool.LINE);
		
		//Set JFrame properties
		circles = new HashSet<>(); //Set of all circles in the editor
		lines = new HashSet<>();
		arrows = new HashSet<>();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1024, 768);
		setVisible(true);
		setLayout(new GridBagLayout());
		
		//Manage the layout constraints
		GridBagConstraints tbar = new GridBagConstraints();
		GridBagConstraints topt = new GridBagConstraints();
		GridBagConstraints editor = new GridBagConstraints();
		GridBagConstraints prop = new GridBagConstraints();
		GridBagConstraints status = new GridBagConstraints();
		
		tbar.gridx = 0;
		tbar.gridy = 0;
		tbar.gridwidth = 1;
		tbar.weightx = 1;
		tbar.weighty = 0.01;
		tbar.fill = GridBagConstraints.HORIZONTAL;
		add(toolbar, tbar);
		
		topt.gridx = 0;
		topt.gridy = 1;
		topt.gridwidth = 1;
		topt.weightx = 1;
		topt.weighty = 0.01;
		topt.fill = GridBagConstraints.HORIZONTAL;
		add(toolOptions, topt);
		
		editor.gridx = 0;
		editor.gridy = 2;
		editor.weightx = 1;
		editor.weighty = 1;
		editor.gridheight = 2;
		editor.insets = new Insets(9, 9, 9, 9);
		editor.fill = GridBagConstraints.BOTH;
		
		prop.gridx = 1;
		prop.gridy = 2;
		prop.weightx = 0.1;
		prop.weighty = 0.8;
		prop.insets = new Insets(2, 2, 2, 2);
		prop.fill = GridBagConstraints.BOTH;
		
		status.gridx = 1;
		status.gridy = 3;
		status.weightx = 0.1;
		status.weighty = 0.4;
		status.insets = new Insets (2, 2, 2, 2);
		status.fill = GridBagConstraints.BOTH;
		
		//Initialize and set up the main editor panel
		panelEditor = new Editor(this);
		panelEditorScroll = new JScrollPane(panelEditor, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panelEditorScroll.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		JScrollBar horiz = panelEditorScroll.getHorizontalScrollBar();
		JScrollBar vert = panelEditorScroll.getVerticalScrollBar();
		horiz.setValue((horiz.getMaximum() + horiz.getVisibleAmount() - horiz.getMinimum())/2);
		vert.setValue((vert.getMaximum()  + vert.getVisibleAmount() - vert.getMinimum())/2);
		add(panelEditorScroll, editor);
		
		//Initialize and set up the properties panel, used when someone right clicks an object and clicks properties
		panelProperties = new JPanel();
		Border lowerEtched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		panelProperties.setBorder(BorderFactory.createTitledBorder(lowerEtched, "Properties"));
		
		generalCircleTab = new JPanel();
		GroupLayout gl = new GroupLayout(generalCircleTab);
		gl.setAutoCreateGaps(true);
		generalSelection = new JLabel("Selection Type:");
		generalSelectionID = new JLabel("Selection ID:");
		generalNodeLocation = new JLabel("Location:");
		generalNodeRadius = new JLabel("Radius:");
		generalNodeEdges = new JLabel("Edges:");
		generalNodeEdgesAll = new JButton("See Edges");
		generalNodeIndegree = new JLabel("Indegree:");
		generalNodeOutdegree = new JLabel("Outdegree:");
		gl.setHorizontalGroup(gl.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(generalSelection)
				.addComponent(generalSelectionID)
				.addComponent(generalNodeLocation)
				.addComponent(generalNodeRadius)
				.addGroup(gl.createSequentialGroup()
						.addComponent(generalNodeEdges)
						.addComponent(generalNodeEdgesAll))
				.addComponent(generalNodeIndegree)
				.addComponent(generalNodeOutdegree));
		gl.setVerticalGroup(gl.createSequentialGroup()
				.addComponent(generalSelection)
				.addComponent(generalSelectionID)
				.addComponent(generalNodeLocation)
				.addComponent(generalNodeRadius)
				.addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(generalNodeEdges)
						.addComponent(generalNodeEdgesAll))
				.addComponent(generalNodeIndegree)
				.addComponent(generalNodeOutdegree));
		
		generalCircleTab.setLayout(gl);
		
		appearanceCircleTab = new JPanel();
		descriptionCircleTab = new JPanel();
		
		//Initialize and fill out the tabbed pane
		propertiesCirclePane = new JTabbedPane();
		propertiesCirclePane.addTab("General", generalCircleTab);
		propertiesCirclePane.addTab("Appearance", appearanceCircleTab);
		propertiesCirclePane.addTab("Description", descriptionCircleTab);
		panelProperties.add(propertiesCirclePane, BorderLayout.CENTER);
//		panelProperties.addComponentListener(new ComponentListener(){
//			@Override
//			public void componentHidden(ComponentEvent arg0) {}
//			@Override
//			public void componentMoved(ComponentEvent arg0) {}
//			@Override
//			public void componentResized(ComponentEvent arg0) {
//				Rectangle newBounds = ((JPanel) arg0.getSource()).getBounds();
//				propertiesPane.setBounds(newBounds.x + PANE_PADDING, newBounds.y + PANE_PADDING,
//										newBounds.width - PANE_PADDING, newBounds.height - PANE_PADDING);
//				generalTab.setBounds(newBounds.x + PANE_PADDING, newBounds.y + PANE_PADDING,
//						newBounds.width - PANE_PADDING, newBounds.height - PANE_PADDING);
//				appearanceTab.setBounds(newBounds.x + PANE_PADDING, newBounds.y + PANE_PADDING,
//						newBounds.width - PANE_PADDING, newBounds.height - PANE_PADDING);
//				descriptionTab.setBounds(newBounds.x + PANE_PADDING, newBounds.y + PANE_PADDING,
//						newBounds.width - PANE_PADDING, newBounds.height - PANE_PADDING);
//			}
//			@Override
//			public void componentShown(ComponentEvent arg0) {}
//		});
		
		add(panelProperties, prop);
		
		panelStatus = new JPanel();
		panelStatus.setBorder(lowerEtched);
		add(panelStatus, status);
		revalidate();
	}
	
	/** The procedure when a tool button is pressed. Updates the current tool and the button appearance. */
	private void actionForToolButtons(Tool t){
		if(currentTool != null){
			if(currentTool != t){
				toolButtons.get(currentTool).setIcon(toolIcons.get(currentTool)[0]);
				toolButtons.get(t).setIcon(toolIcons.get(t)[1]);
				currentTool = t;
			}else{
				toolButtons.get(t).setIcon(toolIcons.get(t)[0]);
				currentTool = null;
			}
		}else{
			toolButtons.get(t).setIcon(toolIcons.get(t)[1]);
			currentTool = t;
		}
	}
	
	private void changeToolOptions(Tool t){
		toolOptions.removeAll();
		if(currentTool != null && currentTool != Tool.SELECT && currentTool != Tool.PAN){
			if(currentTool == Tool.NODE)
				toolOptions.add(nodeOptions);
			else if(currentTool == Tool.LINE){
				lineOptions.arrowToLine();
				toolOptions.add(lineOptions);
			}else if(currentTool == Tool.ARROW){
				lineOptions.lineToArrow();
				toolOptions.add(lineOptions);
			}
		}
		toolOptions.repaint();
		toolOptions.revalidate();
	}
	
	public void addCircle(Circle c){
		c.setID(currentID++);
		circles.add(c);
		panelEditor.add(c);
		panelEditor.repaint();
		panelEditor.revalidate();
	}
	
	public void removeCircle(Circle c){
		circles.remove(c);
		Iterator<Line> lineit = lines.iterator();
		while(lineit.hasNext())
			if(lineit.next().hasEndpoint(c))
				lineit.remove();
		Iterator<Arrow> arrowit = arrows.iterator();
		while(arrowit.hasNext())
			if(arrowit.next().hasEndpoint(c))
				arrowit.remove();
		panelEditor.remove(c);
		panelEditor.repaint();
		panelEditor.revalidate();
	}
	
	public void addLine(Line l){
		lines.add(l);
	}
	
	public void addArrow(Arrow a){
		arrows.add(a);
	}
	
	public boolean getRedrawLine(){
		return redrawLine;
	}
	
	public void setRedrawLine(boolean b){
		redrawLine = b;
	}
	
	public void displayProperties(GraphComponent g){
		String type = "";
		JPanel panel = new JPanel();
		if(g instanceof Circle){
			type = "Circle";
			generalNodeLocation.setText("Location: %d, %d");
			generalNodeRadius.setText("Radius: %d");
			generalNodeEdges.setText("Number of Edges: %d");
			generalNodeEdgesAll.setText("See Edges");
			generalNodeIndegree.setText("Indegree: %d");
			generalNodeOutdegree.setText("Outdegree: %d");
			panel.add(propertiesCirclePane, BorderLayout.CENTER);;
		}
		generalSelection.setText(type);
		generalSelectionID.setText(String.valueOf(g.getID()));
		JFrame props = new JFrame(String.format("%s Properties: \"%d\"", type, g.getID()));
		int result = JOptionPane.showConfirmDialog(props, panel);
		if(result == JOptionPane.OK_OPTION){
			//TODO
		}
	}
	
	public JScrollPane getScrollPane(){
		return panelEditorScroll;
	}
	
	public HashSet<Circle> getCircles(){
		return circles;
	}
	
	public HashSet<Line> getLines(){
		return lines;
	}
	
	public HashSet<Arrow> getArrows(){
		return arrows;
	}
	
	public int currentID(){
		return currentID;
	}
	
	public GraphComponent getSelection(){
		return selection;
	}
	
	public Tool getCurrentTool(){
		return currentTool;
	}
	
	public Circle getLinePoint(){
		return linePoint;
	}
	
	public void setLinePoint(Circle c){
		linePoint = c;
	}
	
	public Circle getArrowPoint(){
		return arrowPoint;
	}
	
	public void setArrowPoint(Circle c){
		arrowPoint = c;
	}
	
	public int getCurrentRadius(){
		if(currentTool == Tool.NODE)
			return nodeOptions.getCurrentRadius();
		return -1;
	}
	
	public Color[] getCurrentCircleColors(){
		if(currentTool == Tool.NODE)
			return nodeOptions.getCurrentCircleColors();
		return null;
	}
	
	public int getCurrentLineWeight(){
		if(currentTool == Tool.LINE || currentTool == Tool.ARROW)
			return lineOptions.getCurrentLineWeight();
		return -1;
	}
	
	public Color getCurrentLineColor(){
		if(currentTool == Tool.LINE || currentTool == Tool.ARROW)
			return lineOptions.getCurrentLineColor();
		return null;
	}
	
	public void setSelection(GraphComponent gc){
		selection = gc;
	}
}
