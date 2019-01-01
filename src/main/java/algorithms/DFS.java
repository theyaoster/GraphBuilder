package algorithms;

import algorithms.util.Search;
import graph.components.Node;
import graph.path.Path;

import java.util.*;
import java.util.function.BiFunction;

/**
 * Depth first search (BFS) implementation and usages.
 *
 * @author Brian Yao
 */
public class DFS extends Search {

	/**
	 * Performs a DFS starting from the provided node. Returns the set of all
	 * nodes reached during this traversal. If the followDirected parameter is
	 * set to false, then the search will ignore edge direction.
	 *
	 * @param start          The starting node of the traversal.
	 * @param followDirected false if we want to ignore edge direction.
	 * @return The set of all nodes reached during the DFS.
	 */
	public static Set<Node> explore(Node start, boolean followDirected) {
		Set<Node> visited = new HashSet<>();
		dfs(start, visited, followDirected, ($, $$) -> false);
		return visited;
	}

	/**
	 * Performs multiple {@link DFS#explore(Node, boolean) DFS explores}, using
	 * each of the nodes in startingNodes as the start of a separate explore.
	 * Returns all nodes explored during any of the explores.
	 *
	 * @param startingNodes  The collection of starting nodes.
	 * @param followDirected false if we want to ignore edge direction.
	 * @return The set of all nodes traversed.
	 */
	public static Set<Node> exploreAll(Collection<Node> startingNodes, boolean followDirected) {
		Set<Node> visited = new HashSet<>();
		startingNodes.forEach(start -> {
			if (!visited.contains(start)) {
				visited.addAll(explore(start, followDirected));
			}
		});
		return visited;
	}

	/**
	 * Use DFS to search from a starting node for the target node. Returns a
	 * path from the start node to the target node, if it exists. This path is
	 * defined when the target is explored for the first time. Set
	 * followDirected to false to ignore edge direction.
	 *
	 * Unlike in BFS, this does not guarantee the shortest path in unweighted
	 * graphs.
	 *
	 * @param start          The node from which the search begins.
	 * @param target         The target node to search for.
	 * @param followDirected false if we want to ignore edge direction.
	 * @return A path from start to target, or null if none exists.
	 */
	public static Path search(Node start, Node target, boolean followDirected) {
		return Search.search(start, target, followDirected, parentMap ->
			dfs(start, new HashSet<>(), followDirected,(visiting, neighbor) -> {
				if (!parentMap.containsKey(neighbor)) {
					parentMap.put(neighbor, visiting);
				}
				return neighbor == target;
			})
		);
	}

	/**
	 * Use DFS to check if there exists a path beginning at the start node
	 * and terminating at the target node.
	 *
	 * @param start          The start node.
	 * @param target         The target node.
	 * @param followDirected false if we want to ignore edge direction.
	 * @return true iff there exists a path from the start to target.
	 */
	public static boolean connected(Node start, Node target, boolean followDirected) {
		return start == target ||
			dfs(start, new HashSet<>(), followDirected, ($, neighbor) -> neighbor == target);
	}

	/**
	 * A method containing the implementation of DFS. Varying behavior is
	 * enabled through the exploreNeighbor parameter.
	 *
	 * @param start           The node to start the search from.
	 * @param visited         A set containing visited nodes.
	 * @param followDirected  false if we want to ignore edge direction.
	 * @param exploreNeighbor A BiFunction which takes as input the node being
	 *                        visited and the neighbor being discovered, and
	 *                        returns a boolean. If it returns true, DFS is
	 *                        halted immediately, otherwise it continues.
	 * @return true iff exploreNeighbor returned true at some point.
	 */
	private static boolean dfs(Node start, Set<Node> visited, boolean followDirected,
							   BiFunction<Node, Node, Boolean> exploreNeighbor) {
		Stack<Node> toVisit = new Stack<>();
		toVisit.push(start);

		while (!toVisit.isEmpty()) {
			Node visiting = toVisit.pop();
			for (Node neighbor : visiting.getNeighbors(followDirected)) {
				if (!visited.contains(neighbor)) {
					toVisit.push(neighbor);
					if (exploreNeighbor.apply(visiting, neighbor)) {
						return true;
					}
				}
			}

			visited.add(visiting);
		}

		return false;
	}

}
