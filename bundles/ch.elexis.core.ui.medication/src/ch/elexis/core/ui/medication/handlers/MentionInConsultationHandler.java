package ch.elexis.core.ui.medication.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.EncounterServiceHolder;
import ch.elexis.core.text.model.Samdas;
import ch.elexis.core.text.model.Samdas.Record;
import ch.elexis.core.ui.medication.views.MedicationTableViewerItem;

public class MentionInConsultationHandler extends AbstractHandler {
	@SuppressWarnings("unchecked")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (selection != null && !selection.isEmpty()) {
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			List<IPrescription> prescriptions = new ArrayList<>();
			List<MedicationTableViewerItem> mtvItems = strucSelection.toList();
			for (MedicationTableViewerItem mtvItem : mtvItems) {
				IPrescription p = mtvItem.getPrescription();
				if (p != null) {
					prescriptions.add(p);
				}
			}
			if (!prescriptions.isEmpty()) {
				IPatient patient = prescriptions.get(0).getPatient();
				Optional<IEncounter> encounter = EncounterServiceHolder.get().getLatestEncounter(patient);
				encounter.ifPresent(enc -> {
					StringBuilder sb = new StringBuilder();
					for (IPrescription presc : prescriptions) {
						String articleLabel = StringUtils.EMPTY;
						if (presc.getArticle() != null) {
							articleLabel = presc.getArticle().getLabel();
						}
						sb.append(StringUtils.LF);
						sb.append("Medikation: " + articleLabel + ", " + presc.getDosageInstruction() //$NON-NLS-2$
								+ StringUtils.SPACE + getType(presc.getEntryType()));
					}

					Samdas samdas = new Samdas(enc.getVersionedEntry().getHead());
					Record rec = samdas.getRecord();
					String recText = rec.getText();
					recText += sb.toString();
					rec.setText(recText);
					EncounterServiceHolder.get().updateVersionedEntry(enc, samdas);
					ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, enc);
				});
			}
		}

		return null;
	}

	private String getType(EntryType entryType) {
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
			return StringUtils.EMPTY;
		}
	}
}
