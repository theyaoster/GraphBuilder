package ui;

import context.GBContext;
import structures.OrderedPair;
import tool.Tool;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

/**
 * The main tool bar at the top of the GBFrame.
 *
 * @author Brian
 */
public class ToolBar extends JToolBar {

	private static final long serialVersionUID = -4276539066793201017L;

	private GBContext context;

	private HashMap<Tool, OrderedPair<ImageIcon>> toolIcons;
	private HashMap<Tool, JButton> toolButtons;

	//Tool bar (right below menu bar)
	private JButton selectButton;
	private JButton edgeSelectButton;
	private JButton nodeButton;
	private JButton arrowButton;
	private JButton lineButton;
	private JButton panButton;

	//Icons for tool bar
	private final ImageIcon selectIcon;
	private final ImageIcon edgeSelectIcon;
	private final ImageIcon nodeIcon;
	private final ImageIcon arrowIcon;
	private final ImageIcon lineIcon;
	private final ImageIcon panIcon;
	private final ImageIcon selectSelectedIcon;
	private final ImageIcon edgeSelectSelectedIcon;
	private final ImageIcon nodeSelectedIcon;
	private final ImageIcon arrowSelectedIcon;
	private final ImageIcon lineSelectedIcon;
	private final ImageIcon panSelectedIcon;
	// Selected icons have 50% less brightness

	public ToolBar(GBContext ctxt) {

		context = ctxt;

		//Initialize icons for tools
		selectIcon = new ImageIcon("img/iconSelect.png");
		edgeSelectIcon = new ImageIcon("img/iconEdgeSelect.png");
		nodeIcon = new ImageIcon("img/iconCircle.png");
		arrowIcon = new ImageIcon("img/iconArrow.png");
		lineIcon = new ImageIcon("img/iconLine.png");
		panIcon = new ImageIcon("img/iconPan.png");
		selectSelectedIcon = new ImageIcon("img/iconSelectSelected.png");
		edgeSelectSelectedIcon = new ImageIcon("img/iconEdgeSelectSelected.png");
		nodeSelectedIcon = new ImageIcon("img/iconCircleSelected.png");
		arrowSelectedIcon = new ImageIcon("img/iconArrowSelected.png");
		lineSelectedIcon = new ImageIcon("img/iconLineSelected.png");
		panSelectedIcon = new ImageIcon("img/iconPanSelected.png");

		//Hash tools to both their icons
		toolIcons = new HashMap<>();
		toolIcons.put(Tool.SELECT, new OrderedPair<>(selectIcon, selectSelectedIcon));
		toolIcons.put(Tool.EDGE_SELECT, new OrderedPair<>(edgeSelectIcon, edgeSelectSelectedIcon));
		toolIcons.put(Tool.NODE, new OrderedPair<>(nodeIcon, nodeSelectedIcon));
		toolIcons.put(Tool.DIRECTED_EDGE, new OrderedPair<>(arrowIcon, arrowSelectedIcon));
		toolIcons.put(Tool.EDGE, new OrderedPair<>(lineIcon, lineSelectedIcon));
		toolIcons.put(Tool.PAN, new OrderedPair<>(panIcon, panSelectedIcon));

		//Initialize tool buttons and add listeners to them.
		selectButton = new JButton(selectIcon);
		selectButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				context.getGUI().updateTool(Tool.SELECT);
				context.getGUI().changeToolOptionsBar();
			}

		});
		selectButton.setToolTipText("Select Tool: Use the left mouse button to select and drag nodes.");
		edgeSelectButton = new JButton(edgeSelectIcon);
		edgeSelectButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				context.getGUI().updateTool(Tool.EDGE_SELECT);
				context.getGUI().changeToolOptionsBar();
			}

		});
		edgeSelectButton.setToolTipText("Edge Select Tool: Use the left mouse button to select the closest edge.");
		nodeButton = new JButton(nodeIcon);
		nodeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				context.getGUI().updateTool(Tool.NODE);
				context.getGUI().changeToolOptionsBar();
			}

		});
		nodeButton.setToolTipText("Node Tool: Places a new node.\nKeyboard shortcut: C");
		arrowButton = new JButton(arrowIcon);
		arrowButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				context.getGUI().updateTool(Tool.DIRECTED_EDGE);
				context.getGUI().changeToolOptionsBar();
			}

		});
		arrowButton.setToolTipText("Directed Edge Tool: Draws a new directed edge between two nodes. Select two nodes in succession to draw.\nKeyboard shortcut: A");
		lineButton = new JButton(lineIcon);
		lineButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				context.getGUI().updateTool(Tool.EDGE);
				context.getGUI().changeToolOptionsBar();
			}

		});
		lineButton.setToolTipText("Edge Tool: Draws a new edge between two nodes. Select two nodes in succession to draw.\nKeyboard shortcut: L");
		panButton = new JButton(panIcon);
		panButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				context.getGUI().updateTool(Tool.PAN);
				context.getGUI().changeToolOptionsBar();
			}

		});
		panButton.setToolTipText("Pan Tool: Allows you to pan the workspace by dragging the left mouse click.\nKeyboard shortcut: P");

		//Add buttons to the toolbar
		add(selectButton);
		add(edgeSelectButton);
		add(nodeButton);
		add(lineButton);
		add(arrowButton);
		add(panButton);

		//Initialize and fill mapping from tools to the corresponding button
		toolButtons = new HashMap<>();
		toolButtons.put(Tool.SELECT, selectButton);
		toolButtons.put(Tool.EDGE_SELECT, edgeSelectButton);
		toolButtons.put(Tool.NODE, nodeButton);
		toolButtons.put(Tool.DIRECTED_EDGE, arrowButton);
		toolButtons.put(Tool.EDGE, lineButton);
		toolButtons.put(Tool.PAN, panButton);
	}

	/**
	 * Get the mapping from tool to icons.
	 *
	 * @return The mapping from tool to icons.
	 */
	public HashMap<Tool, OrderedPair<ImageIcon>> getToolIcons() {
		return toolIcons;
	}

	/**
	 * Get the mapping from tool to tool buttons.
	 *
	 * @return The mapping from tool to tool buttons.
	 */
	public HashMap<Tool, JButton> getToolButtons() {
		return toolButtons;
	}

}