package ch.elexis.core.importer.div.importers;

import java.util.List;
import org.eclipse.emf.ecore.EAttribute;
import org.slf4j.Logger;

import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.KontaktMatcher;
import ch.elexis.hl7.HL7PatientResolver;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public abstract class AbstractHL7PatientResolver extends HL7PatientResolver {
	
	IModelService coreModelService;
	Logger logger;
	
	@Override
	public IPatient resolvePatient(String firstname, String lastname, String birthDate){
		return resolvePatient(firstname, lastname, birthDate, null);
	}
	
	@Override
	public boolean matchPatient(IPatient patient, String firstname, String lastname,
		String birthDate){
		return KontaktMatcher.isSame(patient, lastname, firstname, birthDate);
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
			namedQuery.getParameterMap("code", StringTool.normalizeCase(patid)));
	}
	
	@Override
	public List<? extends IPatient> findPatientByNameAndBirthdate(String lastName, String firstName,
		String birthDate){
		IQuery<IPatient> patientQuery = CoreModelServiceHolder.get().getQuery(IPatient.class);
		patientQuery.and(ModelPackage.Literals.IPERSON__LAST_NAME, COMPARATOR.EQUALS, lastName,
			true);
		patientQuery.and(ModelPackage.Literals.IPERSON__FIRST_NAME, COMPARATOR.EQUALS, firstName,
			true);
		patientQuery.and(ModelPackage.Literals.IPERSON__DATE_OF_BIRTH, COMPARATOR.EQUALS,
			new TimeTool(birthDate).toLocalDate());
		return patientQuery.execute();
	}
	
}
