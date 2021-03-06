package actions.edit;

import actions.SimpleAction;
import context.GBContext;

import java.awt.event.ActionEvent;

/**
 * Action for pushing a new cut action (used by key bindings).
 *
 * @author Brian
 */
public class PushCut extends SimpleAction {

	private static final long serialVersionUID = -3718976211533487482L;

	private boolean full;

	/**
	 * @param ctxt The context in which this action occurs.
	 * @param full Whether this cut performs a full subgraph copy or not.
	 */
	public PushCut(GBContext ctxt, boolean full) {
		super(ctxt);
		this.full = full;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!this.getContext().getGUI().getEditor().getData().selectionsEmpty()) {
			Cut cutAction = new Cut(this.getContext(), full);
			cutAction.perform();
			this.getContext().pushReversibleAction(cutAction, true, false);
		}
	}

}