package ch.elexis.core.model.format;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.prescription.EntryType;
import ch.rgw.tools.StringTool;

public class PatientFormatUtil {

	public static String getNarrativeText(IPatient patient) {
		return ((patient.getTitel() != null) ? patient.getTitel() + StringUtils.SPACE : StringUtils.EMPTY)
				+ patient.getFirstName() + StringUtils.SPACE + patient.getLastName() + " ("
				+ PersonFormatUtil.getGenderCharLocalized(patient) + "), " + PersonFormatUtil.getDateOfBirth(patient)
				+ " (" + patient.getAgeInYears() + ") - [" + patient.getPatientNr() + "]";
	}

	/**
	 * Get the medication of the {@link IPatient} as text in separate lines. Only
	 * medication with matching {@link EntryType} is considered.
	 * 
	 * @param patient
	 * @param filterType
	 * @return
	 */
	public static String getMedicationText(IPatient patient, @Nullable EntryType filterType) {
		List<IPrescription> prescriptions = patient.getMedication(Collections.singletonList(filterType));
		StringBuilder sb = new StringBuilder();

		prescriptions.stream().forEach(p -> {
			if (sb.length() > 0) {
				sb.append(StringTool.lf);
			}
			sb.append(p.getLabel());
		});
		return sb.toString();
	}
}
