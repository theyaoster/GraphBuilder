package algorithms;

import graph.Graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import components.Edge;
import components.Node;

/**
 * An instance represents a path within a graph. A non-empty path contains at
 * least one node and at least zero edges. The number of edges is always one
 * less than the number of nodes.
 * 
 * @author Brian
 */
public final class Path {
	
	public static final Path DISCONNECTED = new Path();
	public static final Path WRONG_GRAPH = new Path();

	// For n nodes, there are n-1 corresponding edges
	private List<Node> nodes;
	private List<Edge> edges;
	
	/**
	 * An instance is a item containing the necessary information for Dijkstra's algorithm.
	 * 
	 * @author Brian
	 */
	private static final class DijkstraItem {
		
		private Double priority;
		private Node currentNode;
		private Node previousNode;
		private Edge fromPreviousNode;
		
		/**
		 * @param priority         The initial priority of this item.
		 * @param currentNode      The node this item encapsulates.
		 */
		private DijkstraItem(Double priority, Node currentNode) {
			this.priority = priority;
			this.currentNode = currentNode;
			this.previousNode = null;
			this.fromPreviousNode = null;
		}
		
		public void setPriority(Double newPriority) {
			priority = newPriority;
		}
		
		public void setPrevious(Node newNode, Edge toNewNode) {
			previousNode = newNode;
			fromPreviousNode = toNewNode;
		}
		
		public boolean equals(Object obj) {
			DijkstraItem other = (DijkstraItem) obj;
			return currentNode.equals(other.currentNode);
		}
		
	}
	
	/**
	 * Default constructor. Constructs an empty path.
	 */
	public Path() {
		nodes = new ArrayList<Node>();
		edges = new ArrayList<Edge>();
	}
	
	/**
	 * Constructs a path with only a single a node and no edges.
	 * 
	 * @param start The initial node to put in the path.
	 */
	public Path(Node start) {
		this();
		nodes.add(start);
	}
	
	/**
	 * Append a node (and edge) to the "end" of this path.
	 * 
	 * @param nextNode   The node to append.
	 * @param toNextNode The edge to the appended node.
	 */
	public void appendNode(Node nextNode, Edge toNextNode) {
		nodes.add(nextNode);
		edges.add(toNextNode);
	}
	
	/**
	 * Prepend a node (and edge) to the "start" of this path.
	 * 
	 * @param prevNode     The node to prepend.
	 * @param fromPrevNode The edge from the prepended node.
	 */
	public void prependNode(Node prevNode, Edge fromPrevNode) {
		nodes.add(0, prevNode);
		edges.add(0, fromPrevNode);
	}
	
	/**
	 * An implementation of Dijkstra's algorithm, which computes the shortest
	 * path from the starting node to the destination node.
	 * 
	 * @param graph       The graph to search for the path in.
	 * @param start       The start of our path.
	 * @param destination The destination of our path.
	 * @return The shortest path from start to destination in graph.
	 */
	public static Path shortestPath(Graph graph, Node start, Node destination) {
		if (!graph.containsNode(start) || !graph.containsNode(destination)) {
			// Graph must contain start and end
			return WRONG_GRAPH;
		} else if (start == destination) {
			// If the start and end are the same node
			return new Path(start);
		}
		Comparator<DijkstraItem> comparator = new Comparator<DijkstraItem>() {

			@Override
			public int compare(DijkstraItem o1, DijkstraItem o2) {
				return o1.priority.compareTo(o2.priority);
			}

		};
		PriorityQueue<DijkstraItem> heap = new PriorityQueue<>(comparator);
		Set<Node> visited = new HashSet<>();
		Map<Node, DijkstraItem> nodeToItem = new HashMap<>();
		
		Set<Node> subGraph = Traversals.breadthFirstSearch(start, true);
		if (!subGraph.contains(destination)) {
			// If there is no connected path from start to end
			return DISCONNECTED;
		}
		
		// Fill the heap
		DijkstraItem startItem = new DijkstraItem(0., start);
		heap.add(startItem);
		subGraph.remove(start);
		for (Node node : subGraph) {
			DijkstraItem nodeItem = new DijkstraItem(Double.POSITIVE_INFINITY, node);
			heap.add(nodeItem);
			nodeToItem.put(node, nodeItem);
		}
		
		// Visit nodes until we visit the destination node
		while (!visited.contains(destination)) {
			DijkstraItem currentItem = heap.poll();
			Node currentNode = currentItem.currentNode;
			
			// Calculate tentative distance for each neighbor
			for (Node neighbor : currentNode.getNeighbors(true)) {
				if (!visited.contains(neighbor)) {
					Double minEdgeWeight = Double.MAX_VALUE;
					Edge minEdge = null;
					
					// Only consider directed and undirected edges to neighbor
					Set<Edge> undirectedNeighbors = currentNode.getUndirectedEdges().get(neighbor);
					Set<Edge> directedNeighbors = currentNode.getOutgoingDirectedEdges().get(neighbor);
					if (undirectedNeighbors != null) {
						for (Edge undirEdge : undirectedNeighbors) {
							Double undirWeight = graph.getWeight(undirEdge);
							if (undirWeight < minEdgeWeight) {
								minEdgeWeight = undirWeight;
								minEdge = undirEdge;
							}
						}
					}
					if (directedNeighbors != null) {
						for (Edge dirEdge : directedNeighbors) {
							Double dirWeight = graph.getWeight(dirEdge);
							if (dirWeight < minEdgeWeight) {
								minEdgeWeight = dirWeight;
								minEdge = dirEdge;
							}
						}
					}
					
					// Check if tentative priority is lower than existing priority
					// for this neighbor. If so, set the priority to be the new lower one
					DijkstraItem neighborItem = nodeToItem.get(neighbor);
					Double newPriority = currentItem.priority + minEdgeWeight;
					if (newPriority < neighborItem.priority) {
						neighborItem.setPriority(newPriority);
						neighborItem.setPrevious(currentNode, minEdge);
					}
				}
			}
			
			visited.add(currentNode);
		}
		
		// Construct the minimum path
		Path shortestPath = new Path(destination);
		Node currentlyLinking = destination;
		while (currentlyLinking != null) {
			DijkstraItem linkingItem = nodeToItem.get(currentlyLinking);
			shortestPath.prependNode(linkingItem.previousNode, linkingItem.fromPreviousNode);
			currentlyLinking = linkingItem.previousNode;
		}
		return shortestPath;
	}
	
}