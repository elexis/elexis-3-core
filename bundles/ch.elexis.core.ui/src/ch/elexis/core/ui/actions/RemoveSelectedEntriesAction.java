package ch.elexis.core.ui.actions;

import java.util.Collection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;

import ch.elexis.core.ui.icons.Images;

/**
 * Remove selected entries from a structured viewer
 * @since 3.6
 */
public class RemoveSelectedEntriesAction extends Action {
	
	private StructuredViewer structuredViewer;
	private Collection<?> targetCollection;
	
	public RemoveSelectedEntriesAction(StructuredViewer structuredViewer,
		Collection<?> targetCollection){
		this.structuredViewer = structuredViewer;
		this.targetCollection = targetCollection;
		
		setImageDescriptor(Images.IMG_REMOVEITEM.getImageDescriptor());
		setText(ch.elexis.core.l10n.Messages.LabGroupPrefs_delete);
	}
	
	public RemoveSelectedEntriesAction(StructuredViewer structuredViewer){
		this(structuredViewer, null);
	}
	
	@Override
	public void run(){
		IStructuredSelection structuredSelection = structuredViewer.getStructuredSelection();
		if (targetCollection != null) {
			targetCollection.removeAll(structuredSelection.toList());
			structuredViewer.setInput(targetCollection);
		} else {
			Object input = structuredViewer.getInput();
			if (input instanceof Collection) {
				((Collection<?>) input).removeAll(structuredSelection.toList());
			}
			structuredViewer.refresh();
		}
		super.run();
	}
	
}
