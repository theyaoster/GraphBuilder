package util;

import graph.Graph;
import graph.GraphConstraint;
import graph.components.Edge;
import graph.components.Node;
import structures.function.QuadFunction;
import structures.function.TriFunction;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Templates for unit tests of search algorithms such as BFS and DFS.
 *
 * Explore algorithm: These are expected to take as input a graph, a starting
 * node and a boolean parameter (followDirected), and output a set of nodes.
 *
 * Explore all algorithm: These are expected to take as input a graph,
 * a collection of starting nodes and a boolean parameter, and output a set
 * of nodes.
 *
 * Connected: These are expected to take as input a graph, a starting node, a
 * target node, and a boolean parameter, and output true iff the start node
 * is connected to the target node.
 *
 * @author Brian Yao
 */
public class SearchTemplates {

	/**
	 * Unit test case for exploring undirected graphs.
	 *
	 * @param explore The explore algorithm.
	 */
	public static void testExploreUndirected(TriFunction<Graph, Node, Boolean, Set<Node>> explore) {
		int constraints = GraphConstraint.SIMPLE | GraphConstraint.UNWEIGHTED | GraphConstraint.UNDIRECTED;
		Graph graph = new Graph(constraints);
		Node[] n = TestUtils.newNodes(10, 0);
		Edge[] e = TestUtils.newEdges(new int[][] {{0, 1}, {0, 2}, {2, 3}, {4, 5}, {6, 7}, {6, 8}},
									  TestUtils.booleans(6, false), n, n.length);
		graph.addNodes(n);
		assertTrue(graph.addEdges(e));

		Set<Node> set1 = Set.of(n[0], n[1], n[2], n[3]);
		Set<Node> set2 = Set.of(n[4], n[5]);
		Set<Node> set3 = Set.of(n[6], n[7], n[8]);
		Set<Node> set4 = Set.of(n[9]);

		assertEquals(set1, explore.apply(graph, n[0], true));
		assertEquals(set1, explore.apply(graph, n[1], true));
		assertEquals(set1, explore.apply(graph, n[2], true));
		assertEquals(set1, explore.apply(graph, n[3], true));
		assertEquals(set2, explore.apply(graph, n[4], true));
		assertEquals(set2, explore.apply(graph, n[5], true));
		assertEquals(set3, explore.apply(graph, n[6], true));
		assertEquals(set3, explore.apply(graph, n[7], true));
		assertEquals(set3, explore.apply(graph, n[8], true));
		assertEquals(set4, explore.apply(graph, n[9], true));
	}

	/**
	 * Unit test case for exploring directed graphs.
	 *
	 * @param explore The explore algorithm.
	 */
	public static void testExploreDirected(TriFunction<Graph, Node, Boolean, Set<Node>> explore) {
		int constraints = GraphConstraint.SIMPLE | GraphConstraint.UNWEIGHTED | GraphConstraint.DIRECTED;
		Graph graph = new Graph(constraints);
		Node[] n = TestUtils.newNodes(10, 0);
		Edge[] e = TestUtils.newEdges(new int[][] {{0, 1}, {0, 2}, {2, 3}, {3, 1}, {4, 5}, {6, 7}, {7, 8}},
									  TestUtils.booleans(7, true), n, n.length);
		graph.addNodes(n);
		assertTrue(graph.addEdges(e));

		assertEquals(Set.of(n[0], n[1], n[2], n[3]), explore.apply(graph, n[0], true));
		assertEquals(Set.of(n[1]), explore.apply(graph, n[1], true));
		assertEquals(Set.of(n[1], n[2], n[3]), explore.apply(graph, n[2], true));
		assertEquals(Set.of(n[1], n[3]), explore.apply(graph, n[3], true));
		assertEquals(Set.of(n[4], n[5]), explore.apply(graph, n[4], true));
		assertEquals(Set.of(n[5]), explore.apply(graph, n[5], true));
		assertEquals(Set.of(n[6], n[7], n[8]), explore.apply(graph, n[6], true));
		assertEquals(Set.of(n[7], n[8]), explore.apply(graph, n[7], true));
		assertEquals(Set.of(n[8]), explore.apply(graph, n[8], true));
		assertEquals(Set.of(n[9]), explore.apply(graph, n[9], true));

		Set<Node> set1 = Set.of(n[0], n[1], n[2], n[3]);
		Set<Node> set2 = Set.of(n[4], n[5]);
		Set<Node> set3 = Set.of(n[6], n[7], n[8]);
		Set<Node> set4 = Set.of(n[9]);

		assertEquals(set1, explore.apply(graph, n[0], false));
		assertEquals(set1, explore.apply(graph, n[1], false));
		assertEquals(set1, explore.apply(graph, n[2], false));
		assertEquals(set1, explore.apply(graph, n[3], false));
		assertEquals(set2, explore.apply(graph, n[4], false));
		assertEquals(set2, explore.apply(graph, n[5], false));
		assertEquals(set3, explore.apply(graph, n[6], false));
		assertEquals(set3, explore.apply(graph, n[7], false));
		assertEquals(set3, explore.apply(graph, n[8], false));
		assertEquals(set4, explore.apply(graph, n[9], false));
	}

	/**
	 * Unit test case for exploreAll in a mixed graph.
	 *
	 * @param exploreAll The exploreAll algorithm.
	 */
	public static void testExploreAll(TriFunction<Graph, Collection<Node>, Boolean, Set<Node>> exploreAll) {
		int constraints = GraphConstraint.SIMPLE | GraphConstraint.UNWEIGHTED | GraphConstraint.MIXED;
		Graph graph = new Graph(constraints);
		Node[] n = TestUtils.newNodes(10, 0);
		Edge[] e = TestUtils.newEdges(new int[][] {{0, 1}, {0, 2}, {2, 3}, {3, 1}, {4, 5}, {6, 7}, {7, 8}},
									  new boolean[] {true, true, false, true, true, false, true}, n, n.length);
		graph.addNodes(n);
		assertTrue(graph.addEdges(e));

		assertEquals(Set.of(n[1], n[2], n[3], n[5], n[8]), exploreAll.apply(graph, Set.of(n[2], n[5], n[8]), true));
		assertEquals(Set.of(n[1], n[4], n[5], n[6], n[7], n[8]),
					 exploreAll.apply(graph, Set.of(n[1], n[4], n[7]), true));
		assertEquals(Set.of(n[0], n[1], n[2], n[3], n[6], n[7], n[8], n[9]),
					 exploreAll.apply(graph, Set.of(n[0], n[6], n[9]), true));
		assertEquals(Set.of(n[4], n[5], n[6], n[7], n[8]), exploreAll.apply(graph, Set.of(n[5], n[8]), false));
		assertEquals(Set.of(n[0], n[1], n[2], n[3], n[9]), exploreAll.apply(graph, Set.of(n[1], n[9]), false));
	}

	/**
	 * Unit test case for connectivity testing.
	 *
	 * @param connected Connectivity test.
	 */
	public static void testConnected(QuadFunction<Graph, Node, Node, Boolean, Boolean> connected) {
		int constraints = GraphConstraint.SIMPLE | GraphConstraint.UNWEIGHTED | GraphConstraint.MIXED;
		Graph graph = new Graph(constraints);
		Node[] n = TestUtils.newNodes(10, 0);
		Edge[] e = TestUtils.newEdges(new int[][] {{0, 1}, {0, 2}, {2, 3}, {3, 1}, {4, 5}, {6, 7}, {7, 8}},
									  new boolean[] {true, true, false, true, true, false, true}, n, n.length);
		graph.addNodes(n);
		assertTrue(graph.addEdges(e));

		assertTrue(connected.apply(graph, n[0], n[1], true));
		assertTrue(connected.apply(graph, n[0], n[2], true));
		assertTrue(connected.apply(graph, n[0], n[3], true));
		assertFalse(connected.apply(graph, n[0], n[4], true));
		assertFalse(connected.apply(graph, n[0], n[6], true));
		assertFalse(connected.apply(graph, n[0], n[9], true));

		assertTrue(connected.apply(graph, n[4], n[5], true));
		assertFalse(connected.apply(graph, n[5], n[4], true));

		assertTrue(connected.apply(graph, n[6], n[8], true));
		assertTrue(connected.apply(graph, n[7], n[8], true));
		assertFalse(connected.apply(graph, n[8], n[6], true));

		assertTrue(connected.apply(graph, n[1], n[3], false));
		assertTrue(connected.apply(graph, n[5], n[4], false));
		assertTrue(connected.apply(graph, n[8], n[6], false));
	}

}
