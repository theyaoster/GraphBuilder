package actions.edit;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.javatuples.Pair;

import actions.SimpleAction;
import structures.UnorderedNodePair;
import util.ClipboardUtils;
import components.Edge;
import components.Node;
import context.GraphBuilderContext;

/**
 * A copy action on graph components (stores a copy of all selected components in the Clipboard).
 * 
 * @author Brian
 */
public class Copy extends SimpleAction {
	
	private static final long serialVersionUID = 3116865860340575301L;
	
	private boolean full;
	
	/**
	 * @param ctxt The context this action occurs in.
	 * @param full Whether this is a full subgraph copy or not.
	 */
	public Copy(GraphBuilderContext ctxt, boolean full) {
		super(ctxt);
		this.full = full;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Pair<HashSet<Node>, HashMap<UnorderedNodePair, ArrayList<Edge>>> pair = ClipboardUtils.separateSelections(this.getContext());
		HashMap<UnorderedNodePair, ArrayList<Edge>> edges;
		if (full) {
			edges = ClipboardUtils.getSubEdgeMap(this.getContext(), pair.getValue0());
		} else {
			edges = pair.getValue1();
		}
		this.getContext().getClipboard().setContents(pair.getValue0(), edges);
		this.getContext().getGUI().getMainMenuBar().updateWithCopy();
	}
	
}
