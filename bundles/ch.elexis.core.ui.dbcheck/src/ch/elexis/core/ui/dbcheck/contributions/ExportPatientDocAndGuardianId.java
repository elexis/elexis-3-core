package ch.elexis.core.ui.dbcheck.contributions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQueryCursor;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.core.utils.CoreUtil;
import ch.elexis.scripting.CSVWriter;

public class ExportPatientDocAndGuardianId extends ExternalMaintenance {

	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion) {
		File file = new File(CoreUtil.getWritableUserDir(), "ExportPatientDocAndGuardianId.csv");
		IQuery<IPatient> patientsQuery = CoreModelServiceHolder.get().getQuery(IPatient.class);

		int exportCounter = 0;
		try (FileWriter fw = new FileWriter(file)) {
			CSVWriter csv = new CSVWriter(fw);
			String[] header = new String[] { "PatientenID", "StammarztID", "GesetzlicherVertreterID" };
			csv.writeNext(header);

			try (IQueryCursor<IPatient> cursor = patientsQuery.executeAsCursor()) {
				pm.beginTask("Bitte warten, alle Patienten, Stammarzt und Vertreter werden exportieren", cursor.size());
				while (cursor.hasNext()) {
					IPatient patient = cursor.next();
					String doctorId = patient.getFamilyDoctor() != null ? patient.getFamilyDoctor().getId()
							: StringUtils.EMPTY;
					String custodianId = patient.getLegalGuardian() != null ? patient.getLegalGuardian().getId()
							: StringUtils.EMPTY;
					csv.writeNext(new String[] { patient.getId(), doctorId, custodianId });
					exportCounter++;
					pm.worked(1);
				}
			}
			csv.close();
		} catch (IOException e) {
			LoggerFactory.getLogger(getClass()).error("Error writing csv");
			return "Die CSV Datei konnte nicht erstellt werden.";
		}

		return "Es wurden " + exportCounter + " Patienten, Stammarzt und Vertreter in die Datei ["
				+ file.getAbsolutePath() + "] geschrieben.";
	}

	@Override
	public String getMaintenanceDescription() {
		return "Export Patienten, Stammarzt und gesetzlicher Vertreter ID.";
	}
}
