package ch.elexis.core.ui.actions;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.preferences.Messages;

/**
 * Move an entry within a structured viewer
 * 
 * @since 3.7
 */
public class MoveEntryWithinListAction extends Action {
	
	private StructuredViewer structuredViewer;
	private List<String> targetCollection;
	private final boolean moveUpwards;
	
	/**
	 * 
	 * @param listViewerArea
	 * @param areas
	 * @param moveUpwards
	 *            move entry upwards (true) or downwards (false) on execution
	 */
	public MoveEntryWithinListAction(StructuredViewer structuredViewer,
		List<String> targetCollection, boolean moveUpwards){
		this.structuredViewer = structuredViewer;
		this.targetCollection = targetCollection;
		this.moveUpwards = moveUpwards;
		
		if (moveUpwards) {
			setImageDescriptor(Images.IMG_ARROWUP.getImageDescriptor());
			setText(Messages.Leistungscodes_moveItemUp);
			setAccelerator(SWT.COMMAND + SWT.ARROW_UP);
		} else {
			setImageDescriptor(Images.IMG_ARROWDOWN.getImageDescriptor());
			setText(Messages.Leistungscodes_moveItemDown);
			setAccelerator(SWT.COMMAND + SWT.ARROW_DOWN);
		}
		
		structuredViewer.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e){
				if (e.stateMask == SWT.COMMAND && e.keyCode == SWT.ARROW_UP && moveUpwards) {
					run();
					e.doit = false;
				} else if (e.stateMask == SWT.COMMAND && e.keyCode == SWT.ARROW_DOWN
					&& !moveUpwards) {
					run();
					e.doit = false;
				}
			}
		});
	}
	
	@Override
	public void run(){
		boolean empty = structuredViewer.getSelection().isEmpty();
		if (!empty) {
			Object selectedElement = structuredViewer.getStructuredSelection().getFirstElement();
			int j = targetCollection.indexOf(selectedElement);
			int destination = (moveUpwards) ? j - 1 : j + 1;
			if (destination >= 0 && destination < targetCollection.size()) {
				Collections.swap(targetCollection, j, destination);
			}
		}
		
		structuredViewer.refresh();
		
		super.run();
	}
	
}
