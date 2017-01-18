package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class containing data structure-related utility methods.
 * 
 * @author Brian
 */
public class StructureUtils {

	/**
	 * Make a shallow copy of a map whose value type is a list.
	 * 
	 * @param original The original map to copy.
	 * @return The shallow copy.
	 */
	public static <K, V> Map<K, List<V>> shallowCopy(Map<K, List<V>> original) {
		Map<K, List<V>> newMap = new HashMap<K, List<V>>();
		for (Map.Entry<K, List<V>> entry : original.entrySet()) {
			newMap.put(entry.getKey(), new ArrayList<V>(entry.getValue()));
		}
		return newMap;
	}
	
}