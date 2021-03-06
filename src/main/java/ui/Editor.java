package ui;

import actions.PlaceNodeAction;
import context.GBContext;
import graph.Graph;
import graph.GraphConstraint;
import graph.components.Node;
import graph.components.display.NodePanel;
import graph.components.gb.GBEdge;
import graph.components.gb.GBNode;
import lombok.Getter;
import math.Complex;
import math.CubicFormula;
import org.javatuples.Triplet;
import config.Preferences;
import structures.EditorData;
import structures.UOPair;
import tool.Tool;
import ui.menus.EditorRightClickMenu;
import util.CoordinateUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.*;
import java.util.List;

/**
 * The main panel on which the user draws graphs.
 *
 * @author Brian Yao
 */
public class Editor extends JPanel {

	private static final long serialVersionUID = 7327691115632869820L;

	private static final Dimension DEFAULT_DIMENSION = new Dimension(4096, 4096);

	private GBFrame gui; // The GBFrame this editor is placed in

	@Getter
	private EditorData data;

	/**
	 * Constructor for an editor panel.
	 *
	 * @param g The GBFrame object we want our editor placed in.
	 */
	public Editor(GBFrame g) {
		super();

		gui = g;
		data = new EditorData(this);

		// Initialize the panel with default settings...
		setBackground(Color.WHITE);
		setPreferredSize(DEFAULT_DIMENSION);

		// Listen for mouse events
		addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent evt) {}

			@Override
			public void mouseEntered(MouseEvent evt) {
				data.setLastMousePoint(evt.getX(), evt.getY());
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent evt) {
				data.setLastMousePoint(evt.getX(), evt.getY());
				repaint();
			}

			@Override
			public void mousePressed(MouseEvent evt) {
				// Clicking the editor will remove all highlights and ONLY do
				// so, such that any other press actions require another press
				if (!data.highlightsEmpty()) {
					data.removeAllHighlights();
					repaint();
					return;
				}

				// Handle right click events
				if (SwingUtilities.isRightMouseButton(evt)) {
					if (data.getEdgeBasePoint() == null) {
						EditorRightClickMenu.show(Editor.this, evt.getX(), evt.getY());
					} else {
						data.clearEdgeBasePoint();
					}

					if (data.getPathBasePoint() != null) {
						data.clearPathBasePoint();
					}
				}

				// Handle left click events
				if (SwingUtilities.isLeftMouseButton(evt)) {
					Tool currentTool = gui.getCurrentTool();
					boolean shouldDeselect = currentTool == Tool.SELECT ||
						((currentTool == Tool.EDGE || currentTool == Tool.DIRECTED_EDGE) &&
							data.getEdgeBasePoint() == null);
					if (shouldDeselect && !evt.isControlDown() && !evt.isShiftDown()) {
						// Clicking the editor will deselect all selections
						// unless shift or control is held
						data.removeAllSelections();
						gui.getMainMenuBar().updateWithSelection();
					}

					if (currentTool == Tool.NODE) {
						// If user left clicks the canvas with the node tool, add a new node
						Color[] colors = gui.getNodeOptionsBar().getCurrentCircleColors();
						int currentRadius = gui.getNodeOptionsBar().getCurrentRadius();

						// If grid snap is enabled, the location of placement is different
						Point mousePoint = data.getLastMousePoint();
						Point placed;
						if (gui.getGridSettingsDialog().getSnapToGrid()) {
							// Compute "snapped" placement position
							placed = CoordinateUtils.closestGridPoint(gui, mousePoint);
							placed.x -= currentRadius;
							placed.y -= currentRadius;
						} else {
							// Use regular placement position (at cursor)
							placed = new Point(Math.max(0, mousePoint.x - currentRadius),
											   Math.max(0, mousePoint.y - currentRadius));
						}

						// Create the node panel with appropriate colors
						NodePanel newPanel = new NodePanel(placed.x, placed.y, currentRadius);
						newPanel.setFillColor(colors[0]);
						newPanel.setBorderColor(colors[1]);
						newPanel.setTextColor(colors[2]);

						// The new node being added to the graph
						GBNode newNode = new GBNode(new Node(getContext().getNextIdAndInc()), getContext(), newPanel);

						// Perform the action for placing a node
						PlaceNodeAction placeAction = new PlaceNodeAction(getContext(), newNode);
						placeAction.perform();
						gui.getContext().pushReversibleAction(placeAction, true, false);
					}

					GBEdge closestEdge = data.getClosestEdge();
					if (currentTool == Tool.EDGE_SELECT && closestEdge != null) {
						// Select the edge
						if (!evt.isControlDown() && !evt.isShiftDown()) {
							// De-select all else if shift and control are not held
							data.removeAllSelections();
						}
						data.addSelection(closestEdge);
						gui.getMainMenuBar().updateWithSelection();
					}
				}

				// Repaint on each press
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent evt) {}

		});

		// Listen for dragging and hovering mouse events
		addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent evt) {
				Tool current = gui.getCurrentTool();
				Point mousePoint = data.getLastMousePoint();
				if (current == Tool.PAN) {
					Point currentPoint = evt.getPoint();
					int changeX = currentPoint.x - mousePoint.x;
					int changeY = currentPoint.y - mousePoint.y;
					JScrollPane sp = gui.getScrollPane();
					JScrollBar horiz = sp.getHorizontalScrollBar();
					JScrollBar vert = sp.getVerticalScrollBar();
					double multiplier = Preferences.PAN_SENSITIVITY;
					int newHorizVal = (int) Math.min(
						horiz.getMaximum(),
						Math.max(horiz.getMinimum(), horiz.getValue() - multiplier * changeX)
					);
					int newVertVal = (int) Math.min(
						vert.getMaximum(),
						Math.max(vert.getMinimum(), vert.getValue() - multiplier * changeY)
					);
					horiz.setValue(newHorizVal);
					vert.setValue(newVertVal);
				}

				data.setLastMousePoint(evt.getX(), evt.getY());
				repaint();
			}

			@Override
			public void mouseMoved(MouseEvent evt) {
				data.setLastMousePoint(evt.getX(), evt.getY());
				Tool currentTool = gui.getCurrentTool();

				if (currentTool == Tool.EDGE_SELECT) {
					findClosestEdge();
				}

				// Repaint whenever the mouse moves
				repaint();
			}

		});
	}

	/**
	 * Get the GBFrame this editor is on.
	 *
	 * @return The GBFrame instance this editor is on.
	 */
	public GBFrame getGUI() {
		return gui;
	}

	/**
	 * Get the context of the GBFrame this editor is on.
	 *
	 * @return The context of the GBFrame this editor is on.
	 */
	public GBContext getContext() {
		return gui.getContext();
	}

	/**
	 * Clears all data maintained by this editor, and remove all node panels.
	 */
	public void clearState() {
		data.clear();

		// Remove all node panels
		this.removeAll();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;

		// Explicitly set the position of the nodes; this allows the pane to be scrollable while
		// retaining the position of the circles relative to the top left corner of the editor panel
		this.getContext().getGbNodes().forEach(node -> node.getPanel().enforceLocation());

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
		Map<UOPair<GBNode>, List<GBEdge>> edgeMap = gui.getContext().getGbEdges();

		// Check if the preview edge's endpoint pair exists in the edge map
		// If not, then we need to draw it separately
		Graph currentGraph = this.getContext().getGraph();
		GBEdge previewEdge = data.getPreviewEdge();
		boolean violatesLoops = !currentGraph.hasConstraint(GraphConstraint.MULTIGRAPH) &&
			previewEdge != null && previewEdge.isSelfEdge();
		if (previewEdge != null) {
			UOPair<GBNode> previewPair = previewEdge.getUoEndpoints();
			if (!edgeMap.containsKey(previewPair) && !violatesLoops) {
				List<GBEdge> previewList = Collections.singletonList(previewEdge);
				drawEdgesBetweenNodePair(g2d, previewPair, previewList, false);
			}
		}

		// Iterate through the pairs of nodes in the edge map, and draw the edges between them
		// If there is a preview edge, we "add" it (not directly) to the list of existing edges
		// before we draw the list of edges
		int previewEdgeIndex = data.getPreviewEdgeIndex();
		edgeMap.keySet().forEach(pair -> {
			List<GBEdge> toDrawEdges;
			List<GBEdge> pairEdges = edgeMap.get(pair);
			if (previewEdge != null && pair.equals(previewEdge.getUoEndpoints())) {
				// Check if drawing preview edge would violate a constraint
				UOPair<Node> nodePair = pair.map(GBNode::getNode);
				boolean violatesSimple = currentGraph.hasConstraint(GraphConstraint.SIMPLE) &&
					currentGraph.getEdges().containsKey(nodePair);
				if (!violatesLoops && !violatesSimple) {
					toDrawEdges = new ArrayList<>(pairEdges);
					int newIndex = previewEdgeIndex < 0 || previewEdgeIndex > pairEdges.size() ?
						pairEdges.size() : previewEdgeIndex;
					toDrawEdges.add(newIndex, previewEdge);
				} else {
					toDrawEdges = pairEdges;
				}
			} else {
				toDrawEdges = pairEdges;
			}

			// Draw edges for this pair of nodes
			drawEdgesBetweenNodePair(g2d, pair, toDrawEdges, previewEdge == null);
		});

		g2d.setStroke(new BasicStroke());

		// Draw tool-specific graphics
		Tool ctool = gui.getCurrentTool();
		Point mousePoint = data.getLastMousePoint();
		if (ctool == Tool.NODE) {
			// Draw preview for the Node tool
			g2d.setColor(Preferences.PREVIEW_COLOR);
			int currentRadius = gui.getNodeOptionsBar().getCurrentRadius();

			// If snap to grid is enabled, draw the preview differently
			Ellipse2D.Double preview;
			if (gui.getGridSettingsDialog().getSnapToGrid()) {
				// Draw the "snapped" preview circle
				Point closestGridPoint = CoordinateUtils.closestGridPoint(gui, mousePoint);
				preview = new Ellipse2D.Double(closestGridPoint.x - currentRadius,
											   closestGridPoint.y - currentRadius,
											   2 * currentRadius, 2 * currentRadius);
			} else {
				// Draw the normal preview circle (at the cursor location)
				preview = new Ellipse2D.Double(mousePoint.x - currentRadius,
											   mousePoint.y - currentRadius,
											   2 * currentRadius, 2 * currentRadius);
			}

			g2d.draw(preview);
		} else if (ctool == Tool.EDGE_SELECT) {
			Point closestEdgePoint = data.getClosestEdgePoint();
			if (closestEdgePoint != null) {
				// Draw the edge select line to closest edge
				g2d.setColor(Preferences.EDGE_SELECT_PREVIEW_COLOR);
				g2d.drawLine(closestEdgePoint.x, closestEdgePoint.y, mousePoint.x, mousePoint.y);
			}
		} else if (ctool == Tool.EDGE || ctool == Tool.DIRECTED_EDGE) {
			GBNode edgeBasePoint = data.getEdgeBasePoint();
			if (edgeBasePoint != null && !data.isPreviewUsingObject()) {
				// Draw preview edge when not hovering over a node
				int weight = gui.getEdgeOptionsBar().getCurrentLineWeight();
				g2d.setStroke(new BasicStroke(weight));
				g2d.setColor(Preferences.PREVIEW_COLOR);
				Point center = edgeBasePoint.getPanel().getLocation();
				center.x += edgeBasePoint.getPanel().getRadius();
				center.y += edgeBasePoint.getPanel().getRadius();
				g2d.drawLine(center.x, center.y, mousePoint.x, mousePoint.y);
				if (ctool == Tool.DIRECTED_EDGE) {
					double dist = Point.distance(mousePoint.x, mousePoint.y, center.x, center.y);
					double unitX = (mousePoint.x - center.x) / dist;
					double unitY = (mousePoint.y - center.y) / dist;
					drawArrowTip(g2d, unitX, unitY, mousePoint, weight);
				}
			}
		}
	}

	// Private instance methods

	/**
	 * Finds the edge closest to the cursor position, and sets the closestEdge
	 * and closestEdgePoint fields.
	 */
	private void findClosestEdge() {
		Point2D.Double mouse = new Point2D.Double(data.getLastMousePoint().x, data.getLastMousePoint().y);

		// Get the closest edge and
		gui.getContext().getGbEdgeSet().stream()
			.map(e -> distanceToEdge(e, mouse))
			.min(Comparator.comparingDouble(Triplet::getValue2))
			.ifPresent(triplet -> {
				data.setClosestEdge(triplet.getValue0());
				data.setClosestEdgePoint(triplet.getValue1());
			});
	}

	// Private static methods

	/**
	 * A helper method for drawing the edges between a pair of nodes.
	 *
	 * @param g2d       The Graphics2D object we want to draw with.
	 * @param nodePair  The pair of nodes we are drawing edges between.
	 * @param edges     The list of edges we need to draw between c1 and c2.
	 * @param noPreview True if there is no preview object.
	 */
	private static void drawEdgesBetweenNodePair(Graphics2D g2d, UOPair<GBNode> nodePair,
												 List<GBEdge> edges, boolean noPreview) {
		// Draw edges between nodes c1 and c2; if edges.size() > 1, draw them as quadratic bezier curves
		double lowerAngle = (1 - edges.size()) * Preferences.EDGE_SPREAD_ANGLE / 2.0;
		int count = 0; // Keep count of how many edges have been drawn so far

		GBNode n1 = nodePair.getFirst();

		// Draw the edges
		for (int i = 0 ; i < edges.size() ; i++) {
			GBEdge e = edges.get(i);

			// Get the visual properties of this edge
			int weight = e.getWeight();
			g2d.setStroke(new BasicStroke(weight));
			g2d.setColor(trueEdgeColor(e));

			// Split cases by the type of edge
			if (e.isSelfEdge()) {
				drawSelfEdge(e, n1, g2d);
				count++;
			} else {
				// The edge is either a line or bezier curve; either way, they get drawn the same way
				double initAngle = lowerAngle + count * Preferences.EDGE_SPREAD_ANGLE;
				if (e.getFirstEnd() != n1) {
					initAngle *= -1;
				}
				Point p1 = e.getFirstEnd().getPanel().getCenter();
				Point p2 = e.getSecondEnd().getPanel().getCenter();

				// Distance between the centers of the nodes
				double dist = p1.distance(p2);

				// Get the radii of both ends
				int p1r = e.getFirstEnd().getPanel().getRadius();
				int p2r = e.getSecondEnd().getPanel().getRadius();

				// Compute components of vectors pointing from one node to the other
				// The length of the vectors is the radius of the node they point from
				double radiusVectorX1 = p1r * (p2.x - p1.x) / dist;
				double radiusVectorY1 = p1r * (p2.y - p1.y) / dist;
				double radiusVectorX2 = p2r * (p1.x - p2.x) / dist;
				double radiusVectorY2 = p2r * (p1.y - p2.y) / dist;

				if (edges.size() % 2 == 1 && i == edges.size() / 2) {
					// If the index corresponds to the "center" edge, it is drawn as a straight line.

					// Draw the line
					double linep1X = radiusVectorX1 + p1.x;
					double linep1Y = radiusVectorY1 + p1.y;
					double linep2X = radiusVectorX2 + p2.x;
					double linep2Y = radiusVectorY2 + p2.y;
					g2d.drawLine((int) linep1X, (int) linep1Y, (int) linep2X, (int) linep2Y);

					// Set the endpoints and the control point to null
					if (noPreview) {
						e.setLineEnds(linep1X, linep1Y, linep2X, linep2Y);
					}

					// If this edge is directed, draw the arrow
					if (e.isDirected()) {
						Point tip = new Point((int) radiusVectorX2 + p2.x, (int) radiusVectorY2 + p2.y);
						drawArrowTip(g2d, -radiusVectorX2, -radiusVectorY2, tip, weight);
					}
				} else {
					// Otherwise, the edges are drawn as quadratic bezier curves

					// Rotate the radius vectors by an angle; this is how the curved edges are separated
					double circ1X = radiusVectorX1 * Math.cos(initAngle) + radiusVectorY1 * Math.sin(initAngle);
					double circ1Y = radiusVectorY1 * Math.cos(initAngle) - radiusVectorX1 * Math.sin(initAngle);
					double circ2X = radiusVectorX2 * Math.cos(initAngle) - radiusVectorY2 * Math.sin(initAngle);
					double circ2Y = radiusVectorX2 * Math.sin(initAngle) + radiusVectorY2 * Math.cos(initAngle);

					if (e.isDirected()) {
						// Draw the triangular tip of the arrow if the edge is directed
						Point tip = new Point((int) circ2X + p2.x, (int) circ2Y + p2.y);
						drawArrowTip(g2d, -circ2X, -circ2Y, tip, weight);
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
						e.setBezierPoints(circ1X, circ1Y, controlX, controlY, circ2X, circ2Y);
					}
				}
				count++;
			}
		}
	}

	/**
	 * Computes the distance from the given mouse point to the given edge.
	 *
	 * @param edge  The edge whose distance from the mouse is measured.
	 * @param mouse The position of the mouse.
	 * @return A triple containing: the given edge, the closest point on the
	 *         edge to the mouse point, and the distance to the closest point.
	 */
	private static Triplet<GBEdge, Point, Double> distanceToEdge(GBEdge edge, Point2D.Double mouse) {
		Point closestPoint;
		double closestDist;
		if (edge.isSelfEdge()) {
			// Case where edge is a self edge
			Point2D.Double cen = edge.getArcCenter();

			// Determine closest point on the loop to the cursor
			double distcen = Point.distance(cen.x, cen.y, mouse.x, mouse.y);
			double closex = (mouse.x - cen.x) * edge.getRadius() / distcen + cen.x;
			double closey = (mouse.y - cen.y) * edge.getRadius() / distcen + cen.y;
			closestPoint = new Point((int) closex, (int) closey);
			closestDist = Math.abs(distcen - edge.getRadius());
		} else {
			Point2D.Double[] bcp = edge.getBezierPoints();
			if (bcp[0] == bcp[2]) {
				// Ends coincide
				closestPoint = new Point((int) bcp[0].x, (int) bcp[0].y);
				closestDist = bcp[0].distance(mouse);
			} else if (bcp[1] == null) {
				// Only 3 candidates: both endpoints and the intersection of the edge and the
				// line perpendicular to the edge which passes through the cursor
				List<Point2D.Double> candidatePoints = new ArrayList<>();
				candidatePoints.add(bcp[0]);
				candidatePoints.add(bcp[2]);

				// Find closest point to line using a parametric line
				Point2D.Double toMouse = new Point2D.Double(mouse.x - bcp[0].x, mouse.y - bcp[0].y);
				Point2D.Double b0b2 = new Point2D.Double(bcp[2].x - bcp[0].x, bcp[2].y - bcp[0].y);
				double t = (toMouse.x * b0b2.x + toMouse.y * b0b2.y) / (b0b2.x * b0b2.x + b0b2.y * b0b2.y);

				// Only a candidate if the intersection lies on the line segment
				if (t > 0 && t < 1) {
					candidatePoints.add(new Point2D.Double(bcp[0].x + b0b2.x * t, bcp[0].y + b0b2.y * t));
				}

				// Determine the closest point among the candidates
				Point2D.Double closestCandidate = Collections.min(
					candidatePoints,
					Comparator.comparingDouble(p -> p.distanceSq(mouse))
				);

				closestDist = closestCandidate.distance(mouse);
				closestPoint = new Point((int) closestCandidate.x, (int) closestCandidate.y);
			} else {
				// To get the closest point on a bezier curve... solve a cubic
				double a = bcp[0].x;
				double b = bcp[0].y;
				double c = bcp[1].x;
				double d = bcp[1].y;
				double e = bcp[2].x;
				double f = bcp[2].y;
				double x = mouse.x;
				double y = mouse.y;

				// The coefficients of the cubic
				double n1 = (a - 2 * c + e) * (a - 2 * c + e) + (b - 2 * d + f) * (b - 2 * d + f);
				double n2 = -3 * ((a - c) * (a - 2 * c + e) + (b - d) * (b - 2 * d + f));
				double n3 = a * (3 * a - x + e) - 2 * c * (3 * a - c - x) - e * x +
					b * (3 * b - y + f) - 2 * d * (3 * b - d - y) - f * y;
				double n4 = (c - a) * (a - x) + (d - b) * (b - y);

				// Compute the roots of the equation n1x^3 + n2x^2 + n3x + n4 = 0
				Complex[] roots = CubicFormula.getRoots(n1, n2, n3, n4);

				// Temporary holder of the points corresponding to the REAL roots of the cubic
				List<Point2D.Double> candidatePoints = new ArrayList<>();

				// Determine which roots are real; use these values of t to determine
				// the candidate "closest" points
				for (Complex rt : roots) {
					if (rt.isReal() && rt.getReal() > 0 && rt.getReal() < 1) {
						candidatePoints.add(getBezierPoint(bcp, rt.getReal()));
					}
				}

				// Make sure to include the endpoints of the bezier curve
				candidatePoints.add(bcp[0]);
				candidatePoints.add(bcp[2]);

				// Determine, out of the candidate points, which is the closest
				Point2D.Double closestCandidate = Collections.min(
					candidatePoints,
					Comparator.comparingDouble(p -> p.distanceSq(mouse))
				);

				closestDist = closestCandidate.distance(mouse);
				closestPoint = new Point((int) closestCandidate.x, (int) closestCandidate.y);
			}
		}

		return new Triplet<>(edge, closestPoint, closestDist);
	}

	/**
	 * Private helper method for drawing a self edge.
	 *
	 * @param e   The self edge.
	 * @param n   The node of the self edge.
	 * @param g2d The graphics object to draw with.
	 */
	private static void drawSelfEdge(GBEdge e, GBNode n, Graphics2D g2d) {
		double offsetAngle = e.getAngle();

		Point nodeCenter = n.getPanel().getCenter();

		// The radius of n1's circle
		int n1r = n.getPanel().getRadius();

		double centralAngle = Preferences.SELF_EDGE_SUBTENDED_ANGLE;
		double edgeAngle = Preferences.SELF_EDGE_ARC_ANGLE;

		double edgeRadius = Math.sin(centralAngle / 2) * n1r / Math.sin(edgeAngle / 2);
		double unitX = Math.cos(offsetAngle);
		double unitY = Math.sin(offsetAngle);
		double centralDist = edgeRadius * Math.cos(edgeAngle / 2) + n1r * Math.cos(centralAngle / 2);
		double edgeCenterX = centralDist * unitX + nodeCenter.x;
		double edgeCenterY = centralDist * unitY + nodeCenter.y;
		g2d.drawOval((int) (edgeCenterX - edgeRadius), (int) (edgeCenterY - edgeRadius),
					 (int) (2 * edgeRadius), (int) (2 * edgeRadius));

		// If the edge is directed, draw the arrow tip
		if (e.isDirected()) {
			double toEndX = n1r * unitX * Math.cos(centralAngle / 2) -
				n1r * unitY * Math.sin(centralAngle / 2);
			double toEndY = n1r * unitX * Math.sin(centralAngle / 2) +
				n1r * unitY * Math.cos(centralAngle / 2);

			drawArrowTip(g2d, -toEndX, -toEndY,
						 new Point((int) toEndX + nodeCenter.x, (int) toEndY + nodeCenter.y), e.getWeight());
		}

		e.setArcCenter(new Point2D.Double(edgeCenterX, edgeCenterY));
		e.setRadius(edgeRadius);
	}

	/**
	 * Get the color that will be used to draw the edge.
	 *
	 * @param edge The edge to get the color of.
	 * @return the color of this edge.
	 */
	private static Color trueEdgeColor(GBEdge edge) {
		if (edge.isHighlighted()) {
			return Preferences.HIGHLIGHT_COLOR;
		} else if (edge.isSelected()) {
			return Preferences.SELECTION_COLOR;
		}  else {
			return edge.getColor();
		}
	}

	/**
	 * A helper method for drawing the tip of a directed edge (a triangle) given the
	 * direction in which the edge is pointing (unit vector), the location of the arrow's tip,
	 * and the weight (thickness) of the edge.
	 *
	 * @param g2d     The Graphics2D object which will draw the tip.
	 * @param vectorX The x component of a vector in the direction the edge is pointing.
	 * @param vectorY The y component of a vector in the direction the edge is pointing.
	 * @param tip     The Point object representing the coordinates of the edge's tip.
	 * @param weight  The weight of the edge.
	 */
	private static void drawArrowTip(Graphics2D g2d, double vectorX, double vectorY, Point tip, int weight) {
		double scale = Preferences.ARROW_TIP_SCALE_FACTOR;
		double halfScale = scale / 2;

		double mag = Math.sqrt(vectorX * vectorX + vectorY * vectorY);
		double unitVectorX = vectorX / mag;
		double unitVectorY = vectorY / mag;
		double leftCornerX = tip.x - scale * weight * unitVectorX + halfScale * weight * unitVectorY;
		double leftCornerY = tip.y - scale * weight * unitVectorY - halfScale * weight * unitVectorX;
		double rightCornerX = leftCornerX - scale * weight * unitVectorY;
		double rightCornerY = leftCornerY + scale * weight * unitVectorX;
		g2d.fillPolygon(new int[] {tip.x, (int) leftCornerX, (int) rightCornerX},
						new int[] {tip.y, (int) leftCornerY, (int) rightCornerY}, 3);
	}

	/**
	 * A helper method for obtaining the point on the given quadratic bezier at the parameter 0 <= t <= 1.
	 *
	 * @param coords The array of Points which define the quadratic bezier.
	 * @param t      The parameter t where 0 <= t <= 1.
	 * @return The Point on the bezier corresponding to the given value of t.
	 */
	private static Point2D.Double getBezierPoint(Point2D.Double[] coords, double t) {
		return new Point2D.Double(
			(1 - t) * (1 - t) * coords[0].x + 2 * (1 - t) * t * coords[1].x + t * t * coords[2].x,
			(1 - t) * (1 - t) * coords[0].y + 2 * (1 - t) * t * coords[1].y + t * t * coords[2].y
		);
	}

}
