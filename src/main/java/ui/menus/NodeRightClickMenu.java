package ui.menus;

import actions.edit.Copy;
import actions.edit.PushCut;
import actions.edit.PushDelete;
import actions.edit.PushDuplicate;
import context.GBContext;
import graph.components.display.NodePanel;
import structures.EditorData;

import javax.swing.*;

/**
 * The right click menu which appears when the user right clicks on a node.
 *
 * @author Brian Yao
 */
public class NodeRightClickMenu {

	/**
	 * Display the menu on the provided node at the specified location.
	 *
	 * @param n The node panel to display the menu on.
	 * @param x The x-coordinate at which to display the menu.
	 * @param y The y-coordinate at which to display the menu.
	 */
	public static void show(final NodePanel n, final int x, final int y) {
		final GBContext ctxt = n.getGbNode().getContext();
		JPopupMenu menu = new JPopupMenu();

		EditorData editorData = ctxt.getGUI().getEditor().getData();

		boolean selectionsNotEmpty = !editorData.selectionsEmpty();
		JMenuItem properties = new JMenuItem("View/Edit Properties");

		JMenuItem copy = new JMenuItem("Copy");
		copy.setEnabled(selectionsNotEmpty);
		copy.addActionListener(new Copy(ctxt, false));

		JMenuItem copyFull = new JMenuItem("Copy full subgraph");
		boolean nodeIsSelected = !editorData.getSelectedNodes().isEmpty();
		copyFull.setEnabled(nodeIsSelected);
		copyFull.addActionListener(new Copy(ctxt, true));

		JMenuItem duplicate = new JMenuItem("Duplicate");
		duplicate.setEnabled(selectionsNotEmpty);
		duplicate.addActionListener(new PushDuplicate(ctxt, false));

		JMenuItem duplicateFull = new JMenuItem("Duplicate full subgraph");
		duplicateFull.setEnabled(nodeIsSelected);
		duplicateFull.addActionListener(new PushDuplicate(ctxt, true));

		JMenuItem cut = new JMenuItem("Cut");
		cut.setEnabled(selectionsNotEmpty);
		cut.addActionListener(new PushCut(ctxt, false));

		JMenuItem cutFull = new JMenuItem("Cut full subgraph");
		cutFull.setEnabled(nodeIsSelected);
		cutFull.addActionListener(new PushCut(ctxt, true));

		JMenuItem delete = new JMenuItem("Delete");
		delete.setEnabled(selectionsNotEmpty);
		delete.addActionListener(new PushDelete(ctxt));

		menu.add(properties);
		menu.addSeparator();
		menu.add(copy);
		menu.add(copyFull);
		menu.add(duplicate);
		menu.add(duplicateFull);
		menu.add(cut);
		menu.add(cutFull);
		menu.add(delete);
		menu.show(n, x, y);
	}

}
