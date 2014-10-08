package ch.elexis.core.ui.laboratory.controls.util;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.dialogs.DisplayLabDokumenteDialog;
import ch.elexis.core.ui.dialogs.DisplayTextDialog;
import ch.elexis.core.ui.laboratory.controls.LaborResultsComposite;
import ch.elexis.core.ui.laboratory.controls.Messages;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;

public class DisplayDoubleClickListener implements IDoubleClickListener {
	
	private static Font dialogFont = null;
	private LaborResultsComposite composite;
	
	public DisplayDoubleClickListener(LaborResultsComposite composite){
		this.composite = composite;
	}

	@Override
	public void doubleClick(DoubleClickEvent event){
		List<LabResult> results = composite.getSelectedResults();
		if (results != null) {
			for (LabResult labResult : results) {
				openDisplayDialog(labResult);
			}
		}
	}
	
	private void openDisplayDialog(LabResult labResult){
		LabItem labItem = labResult.getItem();
		if (labItem.getTyp().equals(LabItem.typ.TEXT) || (labResult.getComment().length() > 0)) {
			DisplayTextDialog dlg =
				new DisplayTextDialog(composite.getShell(),
					Messages.LaborResultsComposite_textResultTitle, labItem.getName(),
					labResult.getComment());
			// HL7 Befunde enthalten oft mit Leerzeichen formatierte Bemerkungen,
			// die nur mit nicht-proportionalen Fonts dargestellt werden k��nnen
			// Wir versuchen also, die Anzeige mit Courier New, ohne zu wissen ob die
			// auf Mac und Linux auch drauf sind.
			// Falls der Font nicht geladen werden kann, wird der System-Default Font
			// verwendet
			// Hier die Fonts, welche getestet worden sind:
			// Windows: Courier New (getestet=
			// Mac: nicht getestet
			// Linux: nicht getestet
			try {
				if (dialogFont == null) {
					dialogFont = new Font(null, "Courier New", 9, SWT.NORMAL); //$NON-NLS-1$
				}
			} catch (Exception ex) {
				// Do nothing -> Use System Default font
			} finally {
				dlg.setFont(dialogFont);
			}
			dlg.setWhitespaceNormalized(false);
			dlg.open();
		} else if (labItem.getTyp().equals(LabItem.typ.DOCUMENT)) {
			Patient patient = ElexisEventDispatcher.getSelectedPatient();
			if (patient != null) {
				new DisplayLabDokumenteDialog(composite.getShell(),
					Messages.LaborResultsComposite_Documents, Collections.singletonList(labResult))
					.open();//$NON-NLS-1$
			}
		}
	}
}
