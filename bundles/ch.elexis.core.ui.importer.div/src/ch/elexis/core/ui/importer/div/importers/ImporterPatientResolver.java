package ch.elexis.core.ui.importer.div.importers;

import java.util.List;

import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.types.Gender;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.exchange.KontaktMatcher;
import ch.elexis.core.ui.exchange.KontaktMatcher.CreateMode;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
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
		if (pat != null) {
			return CoreModelServiceHolder.get().load(pat.getId(), IPatient.class).orElse(null);
		}
		return null;
	}
	
	@Override
	public boolean matchPatient(IPatient patient, String firstname, String lastname,
		String birthDate){
		return KontaktMatcher.isSame(Person.load(patient.getId()), lastname, firstname, birthDate);
	}
	
	@Override
	public IPatient createPatient(String lastName, String firstName, String birthDate, String sex){
		TimeTool birthDateTimeTool = new TimeTool(birthDate);
		Gender gender = Gender.fromValue(sex);
		return new IContactBuilder.PatientBuilder(CoreModelServiceHolder.get(), firstName, lastName,
			birthDateTimeTool.toLocalDate(), gender).buildAndSave();
	}
	
	@Override
	public List<? extends IPatient> getPatientById(String patid){
		INamedQuery<IPatient> namedQuery =
			CoreModelServiceHolder.get().getNamedQuery(IPatient.class, "code");
		return namedQuery.executeWithParameters(
			CoreModelServiceHolder.get().getParameterMap("code", StringTool.normalizeCase(patid)));
	}
	
	@Override
	public List<? extends IPatient> findPatientByNameAndBirthdate(String lastName, String firstName,
		String birthDate){
		IQuery<IPatient> patientQuery = CoreModelServiceHolder.get().getQuery(IPatient.class);
		patientQuery.and(ModelPackage.Literals.IPERSON__LAST_NAME, COMPARATOR.EQUALS,
			lastName, true);
		patientQuery.and(ModelPackage.Literals.IPERSON__FIRST_NAME, COMPARATOR.EQUALS,
			firstName, true);
		patientQuery.and(ModelPackage.Literals.IPERSON__DATE_OF_BIRTH, COMPARATOR.EQUALS,
			new TimeTool(birthDate).toLocalDate());
		return patientQuery.execute();
	}
}
