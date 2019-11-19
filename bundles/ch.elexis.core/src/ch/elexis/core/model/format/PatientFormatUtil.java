package ch.elexis.core.model.format;

import ch.elexis.core.model.IPatient;

import ch.rgw.tools.StringTool;
public class PatientFormatUtil {
	
	public static String getNarrativeText(IPatient patient){
		return ((patient.getTitel() != null) ? patient.getTitel() + StringTool.space : StringTool.leer)
			+ patient.getFirstName() + StringTool.space + patient.getLastName() + " ("
			+ PersonFormatUtil.getGenderCharLocalized(patient) + "), "
			+ PersonFormatUtil.getDateOfBirth(patient) + " (" + patient.getAgeInYears()
			+ ") - [" + patient.getPatientNr() + "]";
	}
}
