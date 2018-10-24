package util;

import graph.components.Edge;
import graph.components.Node;

import java.util.Arrays;

/**
 * A class containing methods used in unit testing.
 *
 * @author Brian Yao
 */
public class TestUtils {

	/**
	 * Generate an array of nodes with the specified length.
	 *
	 * @param numNodes The number of nodes to create.
	 * @param startId  The starting ID; consecutive IDs will be assigned to
	 *                 subsequently generated nodes.
	 * @return the array of new nodes.
	 */
	public static Node[] newNodes(int numNodes, int startId) {
		Node[] nodes = new Node[numNodes];
		for (int i = 0; i < numNodes; i++) {
			nodes[i] = new Node();
			nodes[i].setId(startId + i);
		}
		return nodes;
	}

	/**
	 * Generate an array of edges. The indexPairs parameter is an array of
	 * pairs (arrays of length 2) of indices. These indices are for identifying
	 * nodes in the provided array of nodes. Thus a pair of indices defines the
	 * endpoints of the edge being created. The directed array specifies
	 * whether the i-th generated edge should be directed or not.
	 *
	 * @param indexPairs An array of index pairs specifying node endpoints.
	 * @param directed   An array of booleans specifying whether an edge should
	 *                   be directed.
	 * @param nodes      The list of nodes from which endpoints are taken.
	 * @param startId    The starting ID; consecutive IDs will be assigned to
	 *                   subsequently generated edges.
	 * @return the array of new edges.
	 */
	public static Edge[] newEdges(int[][] indexPairs, boolean[] directed, Node[] nodes, int startId) {
		Edge[] edges = new Edge[indexPairs.length];
		for (int i = 0 ; i < edges.length ; i++) {
			Node firstEnd = nodes[indexPairs[i][0]];
			Node secondEnd = nodes[indexPairs[i][1]];
			edges[i] = new Edge(firstEnd, secondEnd, directed[i]);
			edges[i].setId(startId + i);
		}
		return edges;
	}

	/**
	 * Create a boolean array of the specified length with all entries set
	 * to the specified value. Useful with the newEdges method.
	 *
	 * @param length The length of the array.
	 * @param value  The value to fill the array with.
	 * @return the filled array.
	 */
	public static boolean[] booleans(int length, boolean value) {
		boolean[] directed = new boolean[length];
		Arrays.fill(directed, value);
		return directed;
	}

}