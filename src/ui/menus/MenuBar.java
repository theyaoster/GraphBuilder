package ui.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.AbstractButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import keybindings.KeyActions;
import components.GraphComponent;
import components.Node;
import context.GraphBuilderContext;
import actions.edit.Copy;
import actions.edit.PushCut;
import actions.edit.PushDelete;
import actions.edit.PushDuplicate;
import actions.edit.PushPaste;
import actions.edit.Redo;
import actions.edit.Undo;
import actions.file.New;
import actions.file.Open;
import actions.file.Save;
import actions.file.SaveAs;
import ui.GUI;
import util.FileUtils;

/**
 * The main menu bar which appears at the top of the GUI.
 * 
 * @author Brian
 */
public class MenuBar extends JMenuBar {
	
	private static final long serialVersionUID = -7109662156036502356L;
	
	private GUI gui;
	
	private JMenu file;
	private JMenu edit;
	private JMenu view;
	private JMenu tools;
	private JMenu help;
	
	private JMenuItem newFile;
	private JMenuItem openFile;
	private JMenuItem saveFile;
	private JMenuItem saveAsFile;
	private JMenuItem exit;
	
	private JMenuItem undo;
	private JMenuItem redo;
	private JMenuItem copy;
	private JMenuItem copyFull;
	private JMenuItem duplicate;
	private JMenuItem duplicateFull;
	private JMenuItem paste;
	private JMenuItem cut;
	private JMenuItem cutFull;
	private JMenuItem delete;
	
	private JMenuItem grid;
	
	public MenuBar(final GUI g) {
		super();
		
		gui = g;
		
		//Initialize and fill menu bar
		file = new JMenu("File");
		edit = new JMenu("Edit");
		view = new JMenu("View");
		tools = new JMenu("Tools");
		help = new JMenu("Help");
		
		// Fill "File" menu
		newFile = new JMenuItem("New");
		newFile.setAccelerator(KeyActions.NEW);
		openFile = new JMenuItem("Open");
		openFile.setAccelerator(KeyActions.OPEN);
		saveFile = new JMenuItem("Save");
		saveFile.setAccelerator(KeyActions.SAVE);
		saveAsFile = new JMenuItem("Save As");
		saveAsFile.setAccelerator(KeyActions.SAVE_AS);
		exit = new JMenuItem("Exit");
		exit.setAccelerator(KeyActions.EXIT);
			
		file.add(newFile);
		file.add(openFile);
		file.add(saveFile);
		file.add(saveAsFile);
		file.add(exit);
		
		undo = new JMenuItem("Undo");
		undo.setAccelerator(KeyActions.UNDO);
		undo.setEnabled(false);
		redo = new JMenuItem("Redo");
		redo.setAccelerator(KeyActions.REDO);
		redo.setEnabled(false);
		copy = new JMenuItem("Copy");
		copy.setAccelerator(KeyActions.COPY);
		copy.setEnabled(false);
		copyFull = new JMenuItem("Copy full subgraph");
		copyFull.setAccelerator(KeyActions.COPY_FULL);
		copyFull.setEnabled(false);
		duplicate = new JMenuItem("Duplicate");
		duplicate.setAccelerator(KeyActions.DUPLICATE);
		duplicate.setEnabled(false);
		duplicateFull = new JMenuItem("Duplicate full subgraph");
		duplicateFull.setAccelerator(KeyActions.DUPLICATE_FULL);
		duplicateFull.setEnabled(false);
		paste = new JMenuItem("Paste");
		paste.setAccelerator(KeyActions.PASTE);
		paste.setEnabled(false);
		cut = new JMenuItem("Cut");
		cut.setAccelerator(KeyActions.CUT);
		cut.setEnabled(false);
		cutFull = new JMenuItem("Cut full subgraph");
		cutFull.setAccelerator(KeyActions.CUT_FULL);
		cutFull.setEnabled(false);
		delete = new JMenuItem("Delete");
		delete.setAccelerator(KeyActions.BACKSPACE);
		delete.setEnabled(false);
		
		edit.add(undo);
		edit.add(redo);
		edit.addSeparator();
		edit.add(copy);
		edit.add(copyFull);
		edit.add(duplicate);
		edit.add(duplicateFull);
		edit.add(paste);
		edit.add(cut);
		edit.add(cutFull);
		edit.add(delete);
		
		// Fill the view menu
		grid = new JMenuItem("Grid");
		grid.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				gui.getGridSettingsDialog().showDialog();
			}
			
		});
		
		view.add(grid);
		
		add(file);
		add(edit);
		add(view);
		add(tools);
		add(help);
		
		// Add action listeners
		updateContext(gui.getContext());
	}
	
	/**
	 * Sets the enabled state of the undo menu item.
	 * 
	 * @param enabled Whether undo is possible.
	 */
	public void setUndoEnabled(boolean enabled) {
		undo.setEnabled(enabled);
	}
	
	/**
	 * Sets the enabled state of the redo menu item.
	 * 
	 * @param enabled Whether redo is possible.
	 */
	public void setRedoEnabled(boolean enabled) {
		redo.setEnabled(enabled);
	}
	
	/**
	 * Update the actions associated with menu items to occur in the specified context.
	 * 
	 * @param newContext The new context to "switch" to.
	 */
	public void updateContext(GraphBuilderContext newContext) {
		removeAllActionListeners(new AbstractButton[] {
			newFile, openFile, saveFile, saveAsFile, exit, undo, redo, copy, copyFull, duplicate,
			duplicateFull, paste, cut, cutFull, delete, duplicate, duplicateFull
		});
		
		newFile.addActionListener(new New(gui.getContext()));
		openFile.addActionListener(new Open(gui.getContext()));
		saveFile.addActionListener(new Save(gui.getContext()));
		saveAsFile.addActionListener(new SaveAs(gui.getContext()));
		exit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				FileUtils.exitProcedure(gui.getContext());
			}
			
		});
		undo.addActionListener(new Undo(gui.getContext()));
		redo.addActionListener(new Redo(gui.getContext()));
		copy.addActionListener(new Copy(gui.getContext(), false));
		copyFull.addActionListener(new Copy(gui.getContext(), true));
		duplicate.addActionListener(new PushDuplicate(gui.getContext(), false));
		duplicateFull.addActionListener(new PushDuplicate(gui.getContext(), true));
		paste.addActionListener(new PushPaste(gui.getContext()));
		cut.addActionListener(new PushCut(gui.getContext(), false));
		cutFull.addActionListener(new PushCut(gui.getContext(), true));
		delete.addActionListener(new PushDelete(gui.getContext()));
		duplicate.addActionListener(new PushDuplicate(gui.getContext(), false));
		duplicateFull.addActionListener(new PushDuplicate(gui.getContext(), true));
	}
	
	/**
	 * Update the enabled/disabled state of menu items depending on empty/non-empty selection.
	 */
	public void updateWithSelection() {
		HashSet<GraphComponent> selections = gui.getEditor().getSelections();
		boolean somethingSelected = !selections.isEmpty();
		copy.setEnabled(somethingSelected);
		cut.setEnabled(somethingSelected);
		delete.setEnabled(somethingSelected);
		duplicate.setEnabled(somethingSelected);
		
		boolean nodeSelected = false;
		for (GraphComponent gc : gui.getEditor().getSelections()) {
			if (gc instanceof Node) {
				nodeSelected = true;
				break;
			}
		}
		copyFull.setEnabled(nodeSelected);
		duplicateFull.setEnabled(nodeSelected);
		cutFull.setEnabled(nodeSelected);
	}
	
	/**
	 * Update the enabled/disabled state of menu items depending on empty/non-empty clipboard.
	 */
	public void updateWithCopy() {
		paste.setEnabled(!gui.getContext().getClipboard().isEmpty());
	}
	
	/**
	 * Remove all action listeners from the list of buttons.
	 * 
	 * @param buttons The list of buttons to remove action listeners from.
	 */
	private static void removeAllActionListeners(AbstractButton[] buttons) {
		for (AbstractButton button : buttons) {
			for (ActionListener listener : button.getActionListeners()) {
				button.removeActionListener(listener);
			}
		}
	}
	
}
