package ch.elexis.core.model.format;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.IPatient;

public class PatientFormatUtil {

	public static String getNarrativeText(IPatient patient) {
		return ((patient.getTitel() != null) ? patient.getTitel() + StringUtils.SPACE : StringUtils.EMPTY)
				+ patient.getFirstName() + StringUtils.SPACE + patient.getLastName() + " ("
				+ PersonFormatUtil.getGenderCharLocalized(patient) + "), " + PersonFormatUtil.getDateOfBirth(patient)
				+ " (" + patient.getAgeInYears() + ") - [" + patient.getPatientNr() + "]";
	}
}
