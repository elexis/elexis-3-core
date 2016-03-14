package ch.elexis.core.ui.importer.div.importers;

import java.util.List;
import java.util.stream.Collectors;

import ch.elexis.core.data.beans.ContactBean;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.exchange.KontaktMatcher;
import ch.elexis.core.ui.exchange.KontaktMatcher.CreateMode;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.elexis.hl7.HL7PatientResolver;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class ImporterPatientResolver extends HL7PatientResolver {
	
	private TimeTool convertTool = new TimeTool();
	
	@Override
	public IPatient resolvePatient(String firstname, String lastname, String birthDate){
		// resolve with full data
		Patient pat = KontaktMatcher.findPatient(lastname, firstname, birthDate, "", "", "", "", "",
			CreateMode.FAIL);
		// try to resolve with only the beginning of the name
		if (pat == null) {
			String shortLastname = lastname;
			String shortFirstname = firstname;
			if (lastname.length() > 3) {
				shortLastname = lastname.substring(0, 3);
			}
			if (firstname.length() > 3) {
				shortFirstname = firstname.substring(0, 3);
			}
			pat = KontaktMatcher.findPatient(shortLastname, shortFirstname, birthDate, "", "", "",
				"", "", CreateMode.FAIL);
		}
		// user decides
		if (pat == null) {
			convertTool.set(birthDate);
			String birthStr = convertTool.toString(TimeTool.DATE_GER);
			pat = (Patient) KontaktSelektor.showInSync(Patient.class, Messages.HL7_SelectPatient,
				Messages.HL7_WhoIs + lastname + " " + firstname + " ," + birthStr + "?");
		}
		
		return new ContactBean(pat);
	}
	
	@Override
	public boolean matchPatient(IPatient patient, String firstname, String lastname,
		String birthDate){
		return KontaktMatcher.isSame(patient, lastname, firstname, birthDate);
	}
	
	@Override
	public IPatient createPatient(String lastName, String firstName, String birthDate, String sex){
		return new ContactBean(new Patient(lastName, firstName, birthDate, sex));
	}
	
	@Override
	public List<IPatient> getPatientById(String patid){
		Query<Patient> qbe = new Query<Patient>(Patient.class);
		qbe.add(Patient.FLD_PATID, Query.EQUALS, StringTool.normalizeCase(patid));
		return qbe.execute().stream().map(p -> new ContactBean(p)).collect(Collectors.toList());
	}
	
	@Override
	public List<IPatient> findPatientByNameAndBirthdate(String lastName, String firstName,
		String birthDate){
		Query<Patient> qbe = new Query<Patient>(Patient.class);
		qbe.add(Person.NAME, Query.EQUALS, StringTool.normalizeCase(lastName));
		qbe.add(Person.FIRSTNAME, Query.EQUALS, StringTool.normalizeCase(firstName));
		qbe.add(Person.BIRTHDATE, Query.EQUALS,
			new TimeTool(birthDate).toString(TimeTool.DATE_COMPACT));
		return qbe.execute().stream().map(p -> new ContactBean(p)).collect(Collectors.toList());
	}
}
