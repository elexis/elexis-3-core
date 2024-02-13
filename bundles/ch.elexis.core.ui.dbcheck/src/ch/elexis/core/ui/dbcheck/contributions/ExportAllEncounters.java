package ch.elexis.core.ui.dbcheck.contributions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQueryCursor;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.core.utils.CoreUtil;
import ch.elexis.scripting.CSVWriter;

public class ExportAllEncounters extends ExternalMaintenance {

	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion) {
		File file = new File(CoreUtil.getWritableUserDir(), "ExportAllEncounters.csv");
		IQuery<IPatient> patientsQuery = CoreModelServiceHolder.get().getQuery(IPatient.class);

		int exportCounter = 0;
		try (FileWriter fw = new FileWriter(file)) {
			CSVWriter csv = new CSVWriter(fw);
			String[] header = new String[] { "PatNr", "PatId", "KonsId", "KonsText" };
			csv.writeNext(header);

			try (IQueryCursor<IPatient> cursor = patientsQuery.executeAsCursor()) {
				pm.beginTask("Bitte warten, alle Konsultationen werden exportieren", cursor.size());
				while (cursor.hasNext()) {
					IPatient patient = cursor.next();
					for (ICoverage coverage : patient.getCoverages()) {
						for (IEncounter encounter : coverage.getEncounters()) {
							csv.writeNext(new String[] { patient.getPatientNr(), patient.getId(), encounter.getId(),
									encounter.getHeadVersionInPlaintext() });
							exportCounter++;
						}
					}
					pm.worked(1);
				}
			}
			csv.close();
		} catch (IOException e) {
			LoggerFactory.getLogger(getClass()).error("Error writing csv");
			return "Die CSV Datei konnte nicht erstellt werden.";
		}

		return "Es wurden " + exportCounter + " Konsultationen in die Datei [" + file.getAbsolutePath()
				+ "] geschrieben.";
	}

	@Override
	public String getMaintenanceDescription() {
		return "Alle Konsultationen als CSV exportieren.";
	}

}
