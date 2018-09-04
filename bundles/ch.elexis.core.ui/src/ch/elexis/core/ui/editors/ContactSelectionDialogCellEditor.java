package ch.elexis.core.ui.editors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ch.elexis.core.model.IContact;
import ch.elexis.core.ui.dialogs.ContactSelectionDialog;
import ch.elexis.data.Kontakt;

public class ContactSelectionDialogCellEditor extends DialogCellEditor {
	
	private String title;
	private String message;
	
	public ContactSelectionDialogCellEditor(Composite composite, String title, String message){
		super(composite);
		this.title = title;
		this.message = message;
	}
	
	@Override
	protected void updateContents(Object value){
		if (value instanceof Kontakt) {
			Kontakt contact = (Kontakt) value;
			if (contact != null) {
				super.updateContents(contact.getLabel());
				return;
			}
		} else if (value instanceof IContact) {
			IContact contact = (IContact) value;
			if (contact != null) {
				super.updateContents(contact.getLabel());
				return;
			}
		}
		super.updateContents(null);
	}
	
	@Override
	protected Object openDialogBox(Control cellEditorWindow){
		ContactSelectionDialog dialog = new ContactSelectionDialog(cellEditorWindow.getShell(),
			IContact.class, title, message);
		if (getValue() instanceof IContact) {
			// TODO pre select
		}
		if (dialog.open() == Dialog.OK) {
			return dialog.getSelection();
		}
		return null;
	}
}
