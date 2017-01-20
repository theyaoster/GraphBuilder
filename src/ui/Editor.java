package ui;

import graph.Graph;
import graph.GraphConstraint;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.javatuples.Pair;

import math.Complex;
import math.CubicFormula;
import preferences.Preferences;
import structures.OrderedPair;
import structures.UnorderedNodePair;
import tool.Tool;
import ui.menus.EditorRightClickMenu;
import util.CoordinateUtils;
import actions.PlaceNodeAction;
import components.Edge;
import components.GraphComponent;
import components.Node;
import components.SelfEdge;
import components.SimpleEdge;
import components.display.NodePanel;
import components.display.SelfEdgeData;
import context.GraphBuilderContext;

/**
 * The main panel on which the user will be drawing graphs.
 * 
 * @author Brian
 */
public class Editor extends JPanel {
	
	private static final long serialVersionUID = 7327691115632869820L;
	
	private GUI gui; // The GUI this editor is placed in
	
	private Point lastMousePoint; // Keep track of the last mouse position
	
	private Point closestEdgeSelectPoint; // Used for the edge select tool
	private Edge closestEdge;
	
	private Node edgeBasePoint; // The first node that's selected when drawing an edge
	
	private Set<Node> selectedNodes;
	private Map<UnorderedNodePair, List<Edge>> selectedEdges;
	
	private HashMap<NodePanel, Point> nodePanelPosition; // Map from panel to upper left corner position on editor
	
	private Edge previewEdge;
	private int previewEdgeIndex;
	private boolean drawPreviewUsingObject; // If true, the preview edge should be the object previewEdge
	
	/** 
	 * Constructor for an editor panel.
	 * 
	 * @param g The GUI object we want our editor placed in.
	 */
	public Editor(GUI g) {
		super();
		gui = g;
		lastMousePoint = new Point(0, 0);
		
		selectedNodes = new HashSet<Node>();
		selectedEdges = new HashMap<UnorderedNodePair, List<Edge>>();
		nodePanelPosition = new HashMap<NodePanel, Point>();
		
		// Initialize the panel with default settings...
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(2048, 2048));
		
		// Listen for mouse events
		addMouseListener(new MouseListener() {
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (SwingUtilities.isRightMouseButton(arg0)) {
					EditorRightClickMenu.show(Editor.this, arg0.getX(), arg0.getY());
				}
			}
			
			@Override
			public void mouseEntered(MouseEvent evt) {
				repaint();
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {}
			
			@Override
			public void mousePressed(MouseEvent evt) {
				Tool currentTool = gui.getCurrentTool();
				if (currentTool == Tool.SELECT) {
					// Clicking the canvas will deselect all selections
					if ((evt.getModifiers() & (InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK)) == 0
							&& SwingUtilities.isLeftMouseButton(evt)) {
						Editor.this.removeAllSelections();
						Editor.this.repaint();
						gui.getMainMenuBar().updateWithSelection();
					}
				} else if (currentTool == Tool.NODE && SwingUtilities.isLeftMouseButton(evt)) {
					// If user left clicks the canvas with the node tool, add a new node
					Color[] colors = gui.getNodeOptionsBar().getCurrentCircleColors();
					int currentRadius = gui.getNodeOptionsBar().getCurrentRadius();
					
					// If grid snap is enabled, the location of placement is different
					Point placed;
					if (gui.getGridSettingsDialog().getSnapToGrid()) {
						// Compute "snapped" placement position
						placed = CoordinateUtils.closestGridPoint(gui, lastMousePoint);
						placed.x -= currentRadius;
						placed.y -= currentRadius;
					} else {
						// Use regular placement position (at cursor)
						placed = new Point(Math.max(0, lastMousePoint.x - currentRadius), Math.max(0, lastMousePoint.y - currentRadius));
					}
					// By default, the new node has no text
					NodePanel newPanel = new NodePanel(placed.x, placed.y, currentRadius, Node.DEFAULT_TEXT, colors[0], colors[1], colors[2], gui.getContext());
					Node newNode = new Node(gui.getContext(), gui.getContext().getNextIDAndInc(), newPanel);
					
					// Perform the action for placing a node
					PlaceNodeAction placeAction = new PlaceNodeAction(gui.getContext(), newNode);
					placeAction.actionPerformed(null);
					gui.getContext().pushReversibleAction(placeAction, true, false);
				}
				
				if (currentTool == Tool.EDGE_SELECT && closestEdge != null && SwingUtilities.isLeftMouseButton(evt)) {
					// Select the edge
					if ((evt.getModifiers() & InputEvent.CTRL_MASK) != InputEvent.CTRL_MASK && (evt.getModifiers() & InputEvent.SHIFT_MASK) != InputEvent.SHIFT_MASK) {
						// De-select all else if shift and control are not held
						removeAllSelections();
					}
					addSelection(closestEdge);
					gui.getMainMenuBar().updateWithSelection();
					repaint(); // To draw the selected look
				}
				
				if ((currentTool == Tool.EDGE || currentTool == Tool.DIRECTED_EDGE) && edgeBasePoint != null && SwingUtilities.isRightMouseButton(evt)) {
					// Reset base point to "cancel" edge placement using right click
					edgeBasePoint = null;
					repaint(); // To "un"-draw the preview
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent arg0) {}
			
		});
		
		// Listen for dragging and hovering mouse events
		addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseDragged(MouseEvent evt) {
				Tool current = gui.getCurrentTool();
				if (current == Tool.PAN) {
					Point currentPoint = evt.getPoint();
					int changeX = currentPoint.x - lastMousePoint.x;
					int changeY = currentPoint.y - lastMousePoint.y;
					JScrollPane sp = gui.getScrollPane();
					JScrollBar horiz = sp.getHorizontalScrollBar();
					JScrollBar vert = sp.getVerticalScrollBar();
					double multiplier = (double) Preferences.PAN_SENSITIVITY.getData();
					int newHorizVal = (int) Math.min(horiz.getMaximum(), Math.max(horiz.getMinimum(), horiz.getValue() - multiplier*changeX));
					int newVertVal = (int) Math.min(vert.getMaximum(), Math.max(vert.getMinimum(), vert.getValue() - multiplier*changeY));
					horiz.setValue(newHorizVal);
					vert.setValue(newVertVal);
				}
				lastMousePoint = evt.getPoint();
				repaint();
			}
			
			@Override
			public void mouseMoved(MouseEvent evt) {
				lastMousePoint = evt.getPoint();
				Tool currentTool = gui.getCurrentTool();
				
				if (currentTool == Tool.EDGE_SELECT) {
					// Compute the line and distance to the closest edge 
					Point2D.Double clickd = new Point2D.Double(lastMousePoint.x, lastMousePoint.y); // Move point
					
					// Iteratively updated variables for determining the closest edge
					closestEdgeSelectPoint = null;
					closestEdge = null;
					double minDistance = Double.MAX_VALUE;
					
					// Get the edgemap
					Map<UnorderedNodePair, List<Edge>> em = gui.getContext().getEdgeMap();
					
					// Iterate through the edges
					for (UnorderedNodePair endpoints : em.keySet()) {
						// Get set of edges between first and second
						List<Edge> betweenTwo = em.get(endpoints);
						
						// Iterate through edges between the nodes "first" and "second"
						// For each edge, find the closest distance from the current mouse position to the edge
						// The Edge Select tool requires this to find the edge closest to the mouse
						for (Edge l : betweenTwo) {
							double dist = Double.MAX_VALUE;
							Point closest = null;
							Point2D.Double[] bcp;
							if (l instanceof SimpleEdge) {
								SimpleEdge simpe = (SimpleEdge) l;
								bcp = simpe.getData().getBezierPoints();
								if (bcp[1].x < 0 && bcp[1].y < 0) {
									// Only the endpoints of the line are stored...
									Point2D.Double c1 = bcp[0];
									Point2D.Double c2 = bcp[2];
									if (c2.x != c1.x && c2.y != c1.y) {
										// Find closest point to line using a parametric line
										Point2D.Double c1click = new Point2D.Double(clickd.x - c1.x, clickd.y - c1.y);
										Point2D.Double c1c2 = new Point2D.Double(c2.x - c1.x, c2.y - c1.y);
										double dot = c1click.x * c1c2.x + c1click.y * c1c2.y;
										double t = dot / (c1c2.x * c1c2.x + c1c2.y * c1c2.y);
										double intersectx = c1.x + c1c2.x * t;
										double intersecty = c1.y + c1c2.y * t;
										
									    // Only 3 candidates: both endpoints and the intersection of the edge and the line perpendicular to the edge which passes through the cursor
										double[] dists = new double[3];
										Point2D.Double[] points = {c1, new Point2D.Double(intersectx, intersecty), c2};
										for (int i = 0 ; i < dists.length ; i++) {
											dists[i] = points[i].distance(clickd);
										}
										
										// If the closest point on the line is NOT actually on the line segment
										if (t < 0 || t > 1) {
											dists[1] = Double.MAX_VALUE;
										}
										
										// Determine the closest point among the candidates
										int minind = 0;
										dist = dists[0];
										for (int i = 1 ; i < dists.length ; i++) {
											if (dists[i] < dist) {
												dist = dists[i];
												minind = i;
											}
										}
										closest = new Point((int) points[minind].x, (int) points[minind].y);
									} else if (c2.x == c1.x) {
										// If the line happens to be vertical...
										closest = new Point((int) c1.x, lastMousePoint.y);
										dist = Math.abs(c1.x - clickd.x);
									} else {
										// If the line happens to be horizontal...
										closest = new Point(lastMousePoint.x, (int) c1.y);
										dist = Math.abs(c1.y - clickd.y);
									}
								} else {
									// To get the closest point on a bezier curve, we're going to need to solve a cubic...
									double a = bcp[0].x;
									double b = bcp[0].y;
									double c = bcp[1].x;
									double d = bcp[1].y;
									double e = bcp[2].x;
									double f = bcp[2].y;
									double x = clickd.x;
									double y = clickd.y;
									
									// The coefficients of the cubic 
									double n1 = (a - 2*c + e)*(a - 2*c + e) + (b - 2*d + f)*(b - 2*d + f);
									double n2 = -3*((a - c)*(a - 2*c + e) + (b - d)*(b - 2*d + f));
									double n3 = a*(3*a - x + e) - 2*c*(3*a - c - x) - e*x + b*(3*b - y + f) - 2*d*(3*b - d - y) - f*y;
									double n4 = (c - a)*(a - x) + (d - b)*(b - y);
									
									// Compute the roots of the equation n1x^3 + n2x^2 + n3x + n4 = 0
									Complex[] roots = CubicFormula.getRoots(n1, n2, n3, n4);
										
									// Temporary holder of the points corresponding to the REAL roots of the cubic
									ArrayList<Point2D.Double> candidateClosestPoints = new ArrayList<Point2D.Double>();
									
									// Determine which roots are real; use these values of t to determine the candidate "closest" points
									for (Complex rt : roots) {
										if (rt.isReal() && rt.getReal() >= 0 && rt.getReal() <= 1) {
											candidateClosestPoints.add(getBezierPoint(bcp, rt.getReal()));
										}
									}
									
									// Make sure to include the endpoints of the bezier curve
									candidateClosestPoints.add(bcp[0]);
									candidateClosestPoints.add(bcp[2]);
									
									// Determine, out of the candidate points, which is the closest
									double tempdist;
									for (Point2D.Double pt : candidateClosestPoints) {
										if ((tempdist = pt.distance(clickd)) < dist) {
											dist = tempdist; // Set the distance from the cursor for this edge
											closest = new Point((int) pt.x, (int) pt.y);
										}
									}
								}
							} else if (l instanceof SelfEdge) {
								SelfEdge selfe = (SelfEdge) l;
								SelfEdgeData selfeData = selfe.getData();
								Point2D.Double cen = selfeData.getArcCenter();
								
								// Since a loop is a (part of a) circle, the math for determining the point closest to the mouse is simple
								dist = Math.abs(selfeData.getRadius() - Math.sqrt((cen.x - clickd.x) * (cen.x - clickd.x) + (cen.y - clickd.y) * (cen.y - clickd.y)));
								
								// Determine closest point on the loop to the cursor
								double distcen = Math.sqrt((cen.x - clickd.x) * (cen.x - clickd.x) + (cen.y - clickd.y) * (cen.y - clickd.y));
								double closex = (clickd.x - cen.x) * selfeData.getRadius() / distcen + cen.x;
								double closey = (clickd.y - cen.y) * selfeData.getRadius() / distcen + cen.y;
								closest = new Point((int) closex, (int) closey);
							}
							
							// Update the closest edge; once the distances to all edges is computed, closestEdge will contain the closest edge
							if (dist < minDistance) {
								minDistance = dist;
								closestEdge = l;
								closestEdgeSelectPoint = closest;
							}
						}
					}
				}
				repaint(); // Repaint whenever the mouse moves
			}
			
		});
	}
	
	/**
	 * Get the context of the GUI this editor is on.
	 * 
	 * @return The context of the GUI this editor is on.
	 */
	public GraphBuilderContext getContext() {
		return gui.getContext();
	}
	
	/**
	 * Clears all state held by the editor, like the set of selections, the nodes
	 * currently displayed, the preview edge, etc.
	 */
	public void clearState() {
		selectedNodes.clear();
		selectedEdges.clear();
		nodePanelPosition.clear();
		this.removeAll();
		clearPreviewEdge();
	}
	
	/**
	 * Get the GUI this editor is on.
	 * 
	 * @return The GUI instance this editor is on.
	 */
	public GUI getGUI() {
		return gui;
	}
	
	/**
	 * Get the "base point" of the edge being added.
	 * 
	 * @return The base (first) point of the edge being added.
	 */
	public Node getEdgeBasePoint() {
		return edgeBasePoint;
	}
	
	/**
	 * Set the base point of the edge being drawn (usually to null).
	 * 
	 * @param n The new base point.
	 */
	public void setEdgeBasePoint(Node n) {
		edgeBasePoint = n;
	}
	
	/**
	 * Sets the last mouse point; we need this if the mouse is not on the Editor panel.
	 * 
	 * @param x The x coordinate of the new position.
	 * @param y The y coordinate of the new position.
 	 */
	public void setLastMousePoint(int x, int y) {
		if (lastMousePoint != null) {
			lastMousePoint.setLocation(x, y);
		} else {
			lastMousePoint = new Point(x, y);
		}
	}
	
	/**
	 * Get the mapping from node panel to position.
	 * 
	 * @return The mapping of node panel to position.
	 */
	public HashMap<NodePanel, Point> getNodePanelPositionMap() {
		return nodePanelPosition;
	}
	
	/**
	 * Add a new entry to the mapping from node panel to position.
	 * 
	 * @param np The node panel we want to add.
	 */
	public void addNodePanelEntry(NodePanel np) {
		nodePanelPosition.put(np, np.getCoords());
	}
	
	/**
	 * Remove an entry from the mapping from node panel to position.
	 * 
	 * @param np The node panel whose entry to remove.
	 */
	public void removeNodePanelEntry(NodePanel np) {
		nodePanelPosition.remove(np);
	}
	
	/**
	 * Add the given component to the set of selections.
	 * 
	 * @param gc The selected component.
	 */
	public void addSelection(GraphComponent gc) {
		gc.setSelected(true);
		if (gc instanceof Node) {
			// Node case
			selectedNodes.add((Node) gc);
		} else {
			// Edge case
			Edge selectedEdge = (Edge) gc;
			UnorderedNodePair key = new UnorderedNodePair(selectedEdge);
			if (selectedEdges.containsKey(key)) {
				// Insert this edge into the right position
				List<Edge> totalList = this.getContext().getEdgeMap().get(key);
				List<Edge> selectedList = selectedEdges.get(key);
				int selectionIndex = 0;
				for (Edge existingEdge : totalList) {
					if (selectedEdge == existingEdge) {
						selectedList.add(selectionIndex, selectedEdge);
						break;
					} else if (selectedList.contains(existingEdge)) {
						selectionIndex++;
					}
				}
			} else {
				// Create new entry
				List<Edge> newList = new ArrayList<>();
				newList.add(selectedEdge);
				selectedEdges.put(key, newList);
			}
		}
	}
	
	/**
	 * Add the given components to the set of selections.
	 * 
	 * @param components The collection of graph components.
	 */
	public void addSelections(Collection<? extends GraphComponent> components) {
		for (GraphComponent component : components) {
			this.addSelection(component);
		}
	}
	
	/**
	 * Remove the given component from the set of selections.
	 * 
	 * @param gc The deselected component.
	 */
	public void removeSelection(GraphComponent gc) {
		gc.setSelected(false);
		if (gc instanceof Node) {
			// Node case
			selectedNodes.remove(gc);
		} else {
			// Edge case
			Edge selectedEdge = (Edge) gc;
			UnorderedNodePair key = new UnorderedNodePair(selectedEdge);
			List<Edge> selectedList = selectedEdges.get(key);
			if (selectedList != null) {
				selectedList.remove(selectedEdge);
				if (selectedList.isEmpty()) {
					selectedEdges.remove(key);
				}
			}
		}
	}
	
	/**
	 * Remove the given components from the set of selections.
	 * 
	 * @param components The deselected components.
	 */
	public void removeSelections(Collection<? extends GraphComponent> components) {
		for (GraphComponent gc : components) {
			this.removeSelection(gc);
		}
	}
	
	/**
	 * Clear all selections.
	 */
	public void removeAllSelections() {
		// De-select nodes
		Iterator<Node> nodeIterator = selectedNodes.iterator();
		Node tempNode;
		while (nodeIterator.hasNext()) {
			tempNode = nodeIterator.next();
			tempNode.setSelected(false);
			nodeIterator.remove();
		}
		
		// De-select edges
		Iterator<Map.Entry<UnorderedNodePair, List<Edge>>> edgeIterator = selectedEdges.entrySet().iterator();
		Map.Entry<UnorderedNodePair, List<Edge>> tempEntry;
		while (edgeIterator.hasNext()) {
			tempEntry = edgeIterator.next();
			for (Edge inList : tempEntry.getValue()) {
				inList.setSelected(false);
			}
			edgeIterator.remove();
		}
	}
	
	/**
	 * Check whether the selections are currently empty.
	 * 
	 * @return true iff no graph components are selected.
	 */
	public boolean selectionsEmpty() {
		return selectedNodes.isEmpty() && selectedEdges.isEmpty();
	}
	
	/**
	 * Get the set of all selected edges.
	 * 
	 * @return The set of all selected edges.
	 */
	public Set<Edge> getSelectedEdges() {
		Set<Edge> edgeSet = new HashSet<>();
		for (List<Edge> edgeList : selectedEdges.values()) {
			edgeSet.addAll(edgeList);
		}
		return edgeSet;
	}
	
	/**
	 * Get the set of all selected components. The node and
	 * edge selections are wrapped together as a Pair.
	 * 
	 * @return The Pair, containing a set of selected nodes and map of selected edges.
	 */
	public Pair<Set<Node>, Map<UnorderedNodePair, List<Edge>>> getSelections() {
		return new Pair<Set<Node>, Map<UnorderedNodePair, List<Edge>>>(selectedNodes, selectedEdges);
	}
	
	/**
	 * Set the editor's preview edge and the preview edge's position
	 * 
	 * @param edge        The edge object representing the preview edge.
	 * @param position    The position of the preview edge relative to the existing edges between the
	 *                    the same endpoints the preview edge has.
	 * @param useThisEdge Set to true if we want to draw the preview edge in the first place.
	 */
	public void setPreviewEdge(Edge edge, int position, boolean useThisEdge) {
		previewEdge = edge;
		previewEdgeIndex = position;
		drawPreviewUsingObject = useThisEdge;
	}
	
	/**
	 * Clear the preview edge object.
	 */
	public void clearPreviewEdge() {
		this.setPreviewEdge(null, -1, false);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		
		// Explicitly set the position of the nodes; this allows the pane to be scrollable while
		// retaining the position of the circles relative to the top left corner of the editor panel
		for (Node c : gui.getContext().getNodes()) {
			c.getNodePanel().setLocation(c.getNodePanel().getCoords());
		}
		
		// Set anti-aliasing on for smoother appearance
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Draw grid lines if they are enabled
		if (gui.getGridSettingsDialog().getShowGrid()) {
			int level = gui.getGridSettingsDialog().getGridLevel();
			g2d.setStroke(new BasicStroke(1));
			g2d.setColor(gui.getGridSettingsDialog().getGridColor());
			for (int xi = level ; xi < this.getWidth() ; xi += level) {
				g2d.drawLine(xi, 0, xi, this.getHeight());
			}
			for (int yi = level ; yi < this.getHeight() ; yi += level) {
				g2d.drawLine(0, yi, this.getWidth(), yi);
			}
		}
		
		// Draw all edges by iterating through pairs of nodes
		Map<UnorderedNodePair, List<Edge>> edgeMap = gui.getContext().getEdgeMap();
		
		// Check if the preview edge's endpoint pair exists in the edge map
		// If not, then we need to draw it separately
		Graph currentGraph = this.getContext().getGraph();
		boolean violatesLoops = !currentGraph.hasConstraint(GraphConstraint.LOOPS_ALLOWED) && previewEdge instanceof SelfEdge;
		if (previewEdge != null) {
			UnorderedNodePair previewPair = new UnorderedNodePair(previewEdge);
			if (!edgeMap.containsKey(previewPair)) {
				if (!violatesLoops) {
					List<Edge> previewList = new ArrayList<>();
					previewList.add(previewEdge);
					drawEdgesBetweenNodePair(g2d, previewPair, previewList, previewEdge == null);
				}
			}
		}
		
		// Iterate through the pairs of nodes in the edge map, and draw the edges between them
		// If there is a preview edge, we "add" it (not directly) to the list of existing edges
		// before we draw the list of edges
		List<Edge> pairEdges, toDrawEdges;
		for (UnorderedNodePair pair : edgeMap.keySet()) {
			pairEdges = edgeMap.get(pair);
			if (previewEdge != null && pair.equals(new UnorderedNodePair(previewEdge))) {
				// Check if drawing preview edge would violate a constraint
				boolean violatesSimple = currentGraph.hasConstraint(GraphConstraint.SIMPLE) && currentGraph.getEdges().containsKey(pair);
				if (!violatesLoops && !violatesSimple) {
					toDrawEdges = new ArrayList<>(pairEdges);
					int newIndex = previewEdgeIndex < 0 || previewEdgeIndex > pairEdges.size() ? pairEdges.size() : previewEdgeIndex;
					toDrawEdges.add(newIndex, previewEdge);
				} else {
					toDrawEdges = pairEdges;
				}
			} else {
				toDrawEdges = pairEdges;
			}
			drawEdgesBetweenNodePair(g2d, pair, toDrawEdges, previewEdge == null);
		}
		
		g2d.setStroke(new BasicStroke());
		
		// Draw tool-specific graphics
		Tool ctool = gui.getCurrentTool();
		if (ctool == Tool.NODE) {
			// Draw preview for the Node tool
			g2d.setColor((Color) Preferences.CIRCLE_PREVIEW_COLOR.getData());
			int currentRadius = gui.getNodeOptionsBar().getCurrentRadius();
			
			// If snap to grid is enabled, draw the preview differently
			Ellipse2D.Double preview;
			if (gui.getGridSettingsDialog().getSnapToGrid()) {
				// Draw the "snapped" preview circle
				Point closestGridPoint = CoordinateUtils.closestGridPoint(gui, lastMousePoint);
				preview = new Ellipse2D.Double(closestGridPoint.x - currentRadius,
						closestGridPoint.y - currentRadius, 2*currentRadius, 2*currentRadius);
			} else {
				// Draw the normal preview circle (at the cursor location)
				preview = new Ellipse2D.Double(lastMousePoint.x - currentRadius,
						lastMousePoint.y - currentRadius, 2*currentRadius, 2*currentRadius);
			}
			
			g2d.draw(preview);
		} else if (ctool == Tool.EDGE_SELECT) {
			if (closestEdgeSelectPoint != null) {
				// Draw the edge select line to closest edge
				g2d.setColor((Color) Preferences.EDGE_SELECT_PREVIEW_COLOR.getData());
				g2d.drawLine(closestEdgeSelectPoint.x, closestEdgeSelectPoint.y, lastMousePoint.x, lastMousePoint.y);
			}
		} else if (ctool == Tool.EDGE || ctool == Tool.DIRECTED_EDGE) {
			if (edgeBasePoint != null && !drawPreviewUsingObject) {
				int weight = gui.getEdgeOptionsBar().getCurrentLineWeight();
				g2d.setStroke(new BasicStroke(weight));
				g2d.setColor((Color) Preferences.EDGE_PREVIEW_COLOR.getData());
				Point center = edgeBasePoint.getNodePanel().getLocation();
				center.x += edgeBasePoint.getNodePanel().getRadius();
				center.y += edgeBasePoint.getNodePanel().getRadius();
				g2d.drawLine(center.x, center.y, lastMousePoint.x, lastMousePoint.y);
				if (ctool == Tool.DIRECTED_EDGE) {
					double dist = Point.distance(lastMousePoint.x, lastMousePoint.y, center.x, center.y);
					double unitX = - (lastMousePoint.x - center.x) / dist;
					double unitY = - (lastMousePoint.y - center.y) / dist;
					drawArrowTip(g2d, unitX, unitY, lastMousePoint, weight);
				}
			}
		}
		
		// Draw the "selected" look on the selected edge
		for (Map.Entry<UnorderedNodePair, List<Edge>> edgeEntry : selectedEdges.entrySet()) {
			for (Edge selectedEdge : edgeEntry.getValue()) {
				int side = (Integer) Preferences.EDGE_SELECT_SQUARE_SIZE.getData();
				g2d.setColor((Color) Preferences.EDGE_SELECT_SQUARE_COLOR.getData());
				if (selectedEdge instanceof SimpleEdge) {
					SimpleEdge simpe = (SimpleEdge) selectedEdge;
					Point2D.Double[] pts = simpe.getData().getBezierPoints();
					g2d.fillOval((int) (pts[0].x - side / 2.0), (int) (pts[0].y - side / 2.0), side, side);
					g2d.fillOval((int) (pts[2].x - side / 2.0), (int) (pts[2].y - side / 2.0), side, side);
					Point2D.Double mid;
					if (pts[1] != null) {
						mid = getBezierPoint(pts, 0.5);
					} else {
						mid = new Point2D.Double((pts[0].x + pts[2].x) / 2.0, (pts[0].y + pts[2].y) / 2.0);
					}
					g2d.fillOval((int) (mid.x - side / 2.0), (int) (mid.y - side / 2.0), side, side);
				}
			}
		}
	}
	
	/** A helper method for drawing the edges between a pair of nodes. 
	 * 
	 * @param g2d      The Graphics2D object we want to draw with.
	 * @param nodePair The pair of nodes we are drawing edges between.
	 * @param edges    The list of edges we need to draw between c1 and c2.
	 * @param preview  The preview edge object.
	 * */
	private static void drawEdgesBetweenNodePair(Graphics2D g2d, UnorderedNodePair nodePair, List<Edge> edges, boolean noPreview) {
		// Draw edges between nodes c1 and c2; if edges.size() > 1, draw them as quadratic bezier curves
		double angle = (Double) Preferences.EDGE_SPREAD_ANGLE.getData();
		double lowerAngle = (1 - edges.size()) * angle / 2.0;
		int count = 0; // Keep count of how many edges have been drawn so far
		
		Node n1 = nodePair.getFirst();
		
		// Draw the edges one by one
		Edge e;
		for (int i = 0 ; i < edges.size() ; i++) {
			e = edges.get(i);
			
			// Get the weight of the current edge
			int eweight = e.getData().getWeight();
			g2d.setStroke(new BasicStroke(eweight));
			g2d.setColor(e.getData().getColor());
			
			// Split cases by the type of edge
			if (e instanceof SelfEdge) {
				// Draw this self edge (looks like a loop)
				SelfEdge selfe = (SelfEdge) e;
				double offsetAngle = selfe.getData().getOffsetAngle();

				Point nodeCenter = n1.getNodePanel().getCenter();

				// The radius of n1's panel
				int n1r = n1.getNodePanel().getRadius();
				
				double centralAngle = (Double) Preferences.SELF_EDGE_SUBTENDED_ANGLE.getData();
				double edgeAngle = (Double) Preferences.SELF_EDGE_ARC_ANGLE.getData();
				double edgeRadius = Math.sin(centralAngle / 2) * n1r / Math.sin(edgeAngle / 2);
				double unitX = Math.cos(offsetAngle);
				double unitY = Math.sin(offsetAngle);
				double centralDist = edgeRadius * Math.cos(edgeAngle / 2) + n1r * Math.cos(centralAngle / 2);
				double edgeCenterX = centralDist * unitX + nodeCenter.x;
				double edgeCenterY = centralDist * unitY + nodeCenter.y;
				g2d.drawOval((int) (edgeCenterX - edgeRadius), (int) (edgeCenterY - edgeRadius), (int) (2 * edgeRadius), (int) (2 * edgeRadius));
				
				// If the edge is directed, draw the arrow tip
				if (e.isDirected()) {
					double intersectX = n1r * unitX * Math.cos(centralAngle / 2) - n1r * unitY * Math.sin(centralAngle / 2);
					double intersectY = n1r * unitX * Math.sin(centralAngle / 2) + n1r * unitY * Math.cos(centralAngle / 2);
					double tanUnitX = intersectX / n1r;
					double tanUnitY = intersectY / n1r;
					intersectX += nodeCenter.x;
					intersectY += nodeCenter.y;
					double leftCornerX = intersectX + 5 * eweight * tanUnitX - 2.5 * eweight * tanUnitY;
					double leftCornerY = intersectY + 5 * eweight * tanUnitY - 2.5 * eweight * (- tanUnitX);
					Point rightCorner = new Point((int) (leftCornerX + 5 * eweight * tanUnitY), (int) (leftCornerY + 5 * eweight * (- tanUnitX)));
					g2d.fillPolygon(new int[] {(int) intersectX, (int) leftCornerX, rightCorner.x}, new int[] {(int) intersectY, (int) leftCornerY, rightCorner.y}, 3);
				}
				
				selfe.getData().setArcCenter(new Point2D.Double(edgeCenterX, edgeCenterY));
				selfe.getData().setRadius(edgeRadius);
				count++;
			} else if (e instanceof SimpleEdge) {
				SimpleEdge simpe = (SimpleEdge) e;
				
				// The edge is either a line or bezier curve; either way, they get drawn the same way
				OrderedPair<Node> ends = e.getEndpoints();
				double initAngle = lowerAngle + count * angle;
				if (ends.getFirst() != n1) {
					initAngle = -initAngle;
				}
				Point p1 = ends.getSecond().getNodePanel().getCenter();
				Point p2 = ends.getFirst().getNodePanel().getCenter();
				
				// Reciprocal of distance between the centers of the nodes
				double dist = 1 / Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
				
				// Get the radii of both ends
				int end0r = ends.getFirst().getNodePanel().getRadius();
				int end1r = ends.getSecond().getNodePanel().getRadius();
				
				// Compute components of vectors pointing from one node to the other
				// The length of the vectors is the radius of the node they point from
				double radiusVectorX1 = end1r * dist * (p2.x - p1.x);
				double radiusVectorY1 = end1r * dist * (p2.y - p1.y);
				double radiusVectorX2 = end0r * dist * (p1.x - p2.x);
				double radiusVectorY2 = end0r * dist * (p1.y - p2.y);
				
				// If the index corresponds to the "center" edge, it is drawn as a straight line.
				if (edges.size() % 2 == 1 && i == edges.size() / 2) {
					// Draw the line
					double linep1X = radiusVectorX1 + p1.x;
					double linep1Y = radiusVectorY1 + p1.y;
					double linep2X = radiusVectorX2 + p2.x;
					double linep2Y = radiusVectorY2 + p2.y;
					g2d.drawLine((int) linep2X, (int) linep2Y, (int) linep1X, (int) linep1Y);
					
					// Set the control point to a negative value to indicate it does not exist
					if (noPreview) {
						simpe.getData().setPoints(linep1X, linep1Y, -1, -1, linep2X, linep2Y);
					}
					
					// If this edge is directed, draw the arrow
					if (e.isDirected()) {
						Point tip = new Point((int) radiusVectorX2 + p2.x, (int) radiusVectorY2 + p2.y);
						drawArrowTip(g2d, radiusVectorX2 / end1r, radiusVectorY2 / end1r, tip, eweight);
					}
				} else {
					// Rotate the radius vectors by an angle; this is how the curved edges are separated
					double circ1X = radiusVectorX1 * Math.cos(initAngle) + radiusVectorY1 * Math.sin(initAngle);
					double circ1Y = radiusVectorY1 * Math.cos(initAngle) - radiusVectorX1 * Math.sin(initAngle);
					double circ2X = radiusVectorX2 * Math.cos(initAngle) - radiusVectorY2 * Math.sin(initAngle);
					double circ2Y = radiusVectorX2 * Math.sin(initAngle) + radiusVectorY2 * Math.cos(initAngle);
					
					if (e.isDirected()) {
						// Draw the triangular tip of the arrow if the edge is directed
						double unitX = circ2X / end0r;
						double unitY = circ2Y / end0r;
						Point tip = new Point((int) circ2X + p2.x, (int) circ2Y + p2.y);
						drawArrowTip(g2d, unitX, unitY, tip, eweight);
					}
					
					// Compute slopes of the rotated "radius vectors"
					double slope1 = circ1Y / circ1X;
					double slope2 = circ2Y / circ2X;
					
					// Set the "base point" of these vectors
					circ1X += p1.x;
					circ1Y += p1.y;
					circ2X += p2.x;
					circ2Y += p2.y;
					
					// Compute the intersection of the two radius vectors (the control point for quadratic beziers)
					double controlX = (slope2 * circ2X - slope1 * circ1X + circ1Y - circ2Y) / (slope2 - slope1);
					double controlY = slope1 * (controlX - circ1X) + circ1Y;
					
					// Draw the curve
					QuadCurve2D curve = new QuadCurve2D.Double(circ1X, circ1Y, controlX, controlY, circ2X, circ2Y);
					g2d.draw(curve);
					
					// Only set the points if we are not also drawing a preview edge
					if (noPreview) {
						simpe.getData().setPoints(circ1X, circ1Y, controlX, controlY, circ2X, circ2Y);
					}
				}
				count++;
			}
		}		
	}
	
	/**
	 * A helper method for drawing the tip of a directed edge (a triangle) given the
	 * direction in which the edge is pointing (unit vector), the locatin of the arrow's tip,
	 * and the weight of the edge.
	 * 
	 * @param g2d         The Graphics2D object which will draw the tip.
	 * @param unitVectorX The x component of the (opposite) direction the edge is pointing.
	 * @param unitVectorY The y component of the (opposite) direction the edge is pointing.
	 * @param tip         The Point object representing the coordinates of the edge's tip.
	 * @param weight      The weight of the edge.
	 */
	private static void drawArrowTip(Graphics2D g2d, double unitVectorX, double unitVectorY, Point tip, int weight) {
		double leftCornerX = tip.x + 5 * weight * unitVectorX - 2.5 * weight * unitVectorY;
		double leftCornerY = tip.y + 5 * weight * unitVectorY - 2.5 * weight * (- unitVectorX);
		double rightCornerX = leftCornerX + 5 * weight * unitVectorY;
		double rightCornerY = leftCornerY + 5 * weight * (- unitVectorX); 
		g2d.fillPolygon(new int[] {tip.x, (int) leftCornerX, (int) rightCornerX}, new int[] {tip.y, (int) leftCornerY, (int) rightCornerY}, 3);
	}
	
	/** 
	 * A helper method for obtaining the point on the given quadratic bezier at the parameter 0 <= t <= 1.
	 * 
	 * @param coords The array of Points which define the quadratic bezier.
	 * @param t      The parameter t where 0 <= t <= 1.
	 * @return       The Point on the bezier corresponding to the given value of t.
	 */
	private static Point2D.Double getBezierPoint(Point2D.Double[] coords, double t) {
		return new Point2D.Double(
			(1 - t)*(1 - t)*coords[0].x + 2*(1 - t)*t*coords[1].x + t*t*coords[2].x,
			(1 - t)*(1 - t)*coords[0].y + 2*(1 - t)*t*coords[1].y + t*t*coords[2].y
		);
	}
	
}
