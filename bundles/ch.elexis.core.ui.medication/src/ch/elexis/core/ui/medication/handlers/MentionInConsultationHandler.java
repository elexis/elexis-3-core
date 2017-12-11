package ch.elexis.core.ui.medication.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.text.model.Samdas;
import ch.elexis.core.text.model.Samdas.Record;
import ch.elexis.core.ui.medication.views.MedicationTableViewerItem;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Prescription;

public class MentionInConsultationHandler extends AbstractHandler {
	@SuppressWarnings("unchecked")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		ISelection selection =
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (selection != null && !selection.isEmpty()) {
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			List<Prescription> prescriptions = new ArrayList<Prescription>();
			List<MedicationTableViewerItem> mtvItems = strucSelection.toList();
			for (MedicationTableViewerItem mtvItem : mtvItems) {
				Prescription p = mtvItem.getPrescription();
				if (p != null) {
					prescriptions.add(p);
				}
			}
			
			Konsultation cons = Konsultation.getAktuelleKons();
			if (cons != null) {
				StringBuilder sb = new StringBuilder();
				for (Prescription presc : prescriptions) {
					String articleLabel = "";
					if (presc.getArtikel() != null) {
						articleLabel = presc.getArtikel().getLabel();
					}
					sb.append("\n");
					sb.append("Medikation: " + articleLabel + ", " + presc.getDosis() + " "
						+ getType(presc.getEntryType()));
				}
				
				Samdas samdas = new Samdas(cons.getEintrag().getHead());
				Record rec = samdas.getRecord();
				String recText = rec.getText();
				recText += sb.toString();
				rec.setText(recText);
				cons.updateEintrag(samdas.toString(), true);
			}
		}
		
		return null;
	}
	
	private String getType(EntryType entryType){
		switch (entryType) {
		case FIXED_MEDICATION:
			return "(Fixmedikation)";
		case RESERVE_MEDICATION:
			return "(Reservemedikation)";
		case SELF_DISPENSED:
			return "(Dispensiert)";
		case RECIPE:
			return "(Rezeptiert)";
		default:
			return "";
		}
	}
}
