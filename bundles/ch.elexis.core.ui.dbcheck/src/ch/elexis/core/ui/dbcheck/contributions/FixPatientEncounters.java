package ch.elexis.core.ui.dbcheck.contributions;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.EncounterServiceHolder;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.core.ui.dialogs.base.InputDialog;
import ch.rgw.tools.VersionedResource;

public class FixPatientEncounters extends ExternalMaintenance {

	private String patientNumber;

	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion) {
		Display.getDefault().syncExec(() -> {
			InputDialog inputDialog = new InputDialog(Display.getDefault().getActiveShell(), "Patient number",
					"Insert number of patient to fix encounters", StringUtils.EMPTY, null,
					SWT.SINGLE | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
			if (inputDialog.open() == MessageDialog.OK) {
				patientNumber = inputDialog.getValue();
			}
		});
		IPatient patient = CoreModelServiceHolder.get().getQuery(IPatient.class).and("code", COMPARATOR.EQUALS, patientNumber).executeSingleResult()
				.orElseThrow(() -> new IllegalStateException("No patient with number [" + patientNumber + "]"));

		int checked = 0;
		int fixed = 0;

		List<IEncounter> allEncounters = EncounterServiceHolder.get().getAllEncountersForPatient(patient);
		pm.beginTask("Fixing encounters", allEncounters.size());
		for (IEncounter iEncounter : allEncounters) {
			VersionedResource vr = iEncounter.getVersionedEntry();
			String s = vr.getHead();
			String noControlChar = filterNonPrintable(s);
			if (noControlChar.toCharArray().length != s.toCharArray().length) {
				vr.update(noControlChar, "Fix invalid char");
				iEncounter.setVersionedEntry(vr);
				CoreModelServiceHolder.get().save(iEncounter);
				fixed++;
			}
			checked++;
		}

		pm.done();
		return fixed + " invalid encounters fixed of " + checked + " overall";
	}

	private String filterNonPrintable(String input) {
		return input.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String getMaintenanceDescription() {
		return "Fix invalid encounters of a patient";
	}

}
