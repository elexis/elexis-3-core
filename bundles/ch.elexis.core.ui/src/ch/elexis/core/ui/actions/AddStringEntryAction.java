package ch.elexis.core.ui.actions;

import java.util.Collection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StructuredViewer;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.Images;

/**
 * Add a string entry to a structured viewer
 * 
 * @since 3.6
 */
public class AddStringEntryAction extends Action {
	
	private StructuredViewer structuredViewer;
	private Collection<String> targetCollection;
	
	public AddStringEntryAction(StructuredViewer structuredViewer,
		Collection<String> targetCollection){
		this.structuredViewer = structuredViewer;
		this.targetCollection = targetCollection;
		
		setImageDescriptor(Images.IMG_ADDITEM.getImageDescriptor());
		setText(ch.elexis.core.l10n.Messages.LabGroupPrefs_add);
	}
	
	public AddStringEntryAction(StructuredViewer structuredViewer){
		this(structuredViewer, null);
	}
	
	@Override
	public void run(){
		InputDialog inputDialog = new InputDialog(UiDesk.getTopShell(), "Hinzufügen",
			"Bitte geben Sie die Bezeichnung an", null, null);
		int retVal = inputDialog.open();
		if (retVal != Dialog.OK) {
			return;
		}
		String value = inputDialog.getValue();
		
		if (targetCollection != null) {
			targetCollection.add(value);
			structuredViewer.setInput(targetCollection);
		} else {
			Object input = structuredViewer.getInput();
			if (input instanceof Collection) {
				((Collection<String>) input).add(value);
				structuredViewer.refresh();
			}
			super.run();
		}
	}
}
