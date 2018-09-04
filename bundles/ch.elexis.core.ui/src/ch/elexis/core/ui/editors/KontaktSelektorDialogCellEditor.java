package ch.elexis.core.ui.editors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ch.elexis.core.model.IContact;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.data.Kontakt;

/**
 * 
 * 
 * @deprecated use {@link IContact} based {@link ContactSelektorDialogCellEditor} instead.
 *
 */
public class KontaktSelektorDialogCellEditor extends DialogCellEditor {
	
	private String title;
	private String message;
	
	public KontaktSelektorDialogCellEditor(Composite composite, String title, String message){
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
		KontaktSelektor ksl = new KontaktSelektor(cellEditorWindow.getShell(), Kontakt.class, title,
			message, Kontakt.DEFAULT_SORT);
		if (getValue() instanceof Kontakt) {
			Kontakt contact = (Kontakt) getValue();
			if (contact != null) {
				// TODO pre select
			}
		}
		if (ksl.open() == Dialog.OK) {
			return ksl.getSelection();
		}
		return null;
	}
	
}
