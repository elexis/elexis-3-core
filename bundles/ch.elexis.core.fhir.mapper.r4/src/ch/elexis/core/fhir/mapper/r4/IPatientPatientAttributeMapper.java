package ch.elexis.core.fhir.mapper.r4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Patient.ContactComponent;
import org.hl7.fhir.r4.model.StringType;

import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.mapper.r4.helper.IPersonHelper;
import ch.elexis.core.findings.IdentifierSystem;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.MaritalStatus;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IXidService;

/**
 * @see https://hl7.org/fhir/R4/patient.html
 */
public class IPatientPatientAttributeMapper extends IdentifiableDomainResourceAttributeMapper<IPatient, Patient> {

	private IPersonHelper personHelper;
	private IModelService coreModelService;
	private IXidService xidService;

	public IPatientPatientAttributeMapper(IModelService coreModelService, IXidService xidService) {
		super(Patient.class);

		this.personHelper = new IPersonHelper();
		this.coreModelService = coreModelService;
		this.xidService = xidService;
	}

	@Override
	public void fullElexisToFhir(IPatient source, Patient target, SummaryEnum summaryEnum) {

		List<Identifier> identifiers = personHelper.getIdentifiers(source, xidService);
		identifiers.add(getPatientNumberIdentifier(source));
		target.setIdentifier(identifiers);

		target.setName(personHelper.getHumanNames(source));
		target.setGender(personHelper.getGender(source.getGender()));
		target.setBirthDate(personHelper.getBirthDate(source));
		target.setAddress(personHelper.getAddresses(source));
		target.setTelecom(personHelper.getContactPoints(source));

		mapComments(source, target);
		mapMaritalStatus(source, target);
		mapRelatedContacts(source, target);

		Attachment contactImage = personHelper.mapContactImage(source);
		target.setPhoto(contactImage != null ? Collections.singletonList(contactImage) : null);
	}

	@Override
	public void fullFhirToElexis(Patient source, IPatient target) {
		personHelper.mapIdentifiers(coreModelService, source.getIdentifier(), target);
		personHelper.mapHumanName(source.getName(), target);
		personHelper.mapGender(source.getGender(), target);
		personHelper.mapBirthDate(source.getBirthDate(), target);
		personHelper.mapAddress(source.getAddress(), target);
		personHelper.mapTelecom(source.getTelecom(), target);
		personHelper.mapContactImage(coreModelService,
				(source.getPhoto() != null && !source.getPhoto().isEmpty()) ? source.getPhoto().get(0) : null, target);
		mapComments(source, target);
		mapMaritalStatus(source, target);

	}

	private void mapRelatedContacts(IPatient source, Patient target) {
		List<ContactComponent> contacts = new ArrayList<>();

		IContact legalGuardian = source.getLegalGuardian();
		if (legalGuardian != null) {
			ContactComponent _legalGuardian = new ContactComponent();

			CodeableConcept addCoding = new CodeableConcept().addCoding(new Coding().setCode("N"));
			_legalGuardian.setRelationship(Collections.singletonList(addCoding));
			_legalGuardian.setId(legalGuardian.getId());
			if (legalGuardian.isPerson()) {
				IPerson legalGuardianPerson = coreModelService.load(legalGuardian.getId(), IPerson.class).get();
				List<HumanName> humanNames = personHelper.getHumanNames(legalGuardianPerson);
				_legalGuardian.setName((!humanNames.isEmpty()) ? humanNames.get(0) : null);
				AdministrativeGender gender = personHelper.getGender(legalGuardianPerson.getGender());
				_legalGuardian.setGender(gender);
			} else if (legalGuardian.isOrganization()) {
				IOrganization legalGuardianOrganization = coreModelService
						.load(legalGuardian.getId(), IOrganization.class).get();
				personHelper.getOrganizationName(legalGuardianOrganization);
			}
			List<Address> addresses = personHelper.getAddresses(legalGuardian);
			_legalGuardian.setAddress((!addresses.isEmpty()) ? addresses.get(0) : null);
			List<ContactPoint> contactPoints = personHelper.getContactPoints(legalGuardian);
			_legalGuardian.setTelecom(contactPoints);

			contacts.add(_legalGuardian);
		}

		target.setContact(contacts);
	}

	private void mapMaritalStatus(IPatient source, Patient target) {
		MaritalStatus maritalStatus = source.getMaritalStatus();
		if (maritalStatus != null) {
			target.setMaritalStatus(new CodeableConcept().addCoding(new Coding().setCode(maritalStatus.getFhirCode())));
		}
	}

	private void mapMaritalStatus(Patient source, IPatient target) {
		CodeableConcept maritalStatus = source.getMaritalStatus();
		if (maritalStatus != null && !maritalStatus.getCoding().isEmpty()) {
			String code = maritalStatus.getCoding().get(0).getCode();
			target.setMaritalStatus(MaritalStatus.byFhirCodeSafe(code));
		}
	}

	private void mapComments(Patient source, IPatient target) {
		List<Extension> extensionsByUrl = source.getExtensionsByUrl("www.elexis.info/extensions/patient/notes");
		if (!extensionsByUrl.isEmpty()) {
			target.setComment(extensionsByUrl.get(0).getValue().toString());
		}
	}

	private void mapComments(IPatient source, Patient target) {
		Extension elexisPatientNote = new Extension();
		elexisPatientNote.setUrl("www.elexis.info/extensions/patient/notes");
		elexisPatientNote.setValue(new StringType(source.getComment()));
		target.addExtension(elexisPatientNote);
	}

	private Identifier getPatientNumberIdentifier(IPatient source) {
		String patNr = source.getPatientNr();
		Identifier identifier = new Identifier();
		identifier.setSystem(IdentifierSystem.ELEXIS_PATNR.getSystem());
		identifier.setValue(patNr);
		return identifier;
	}

}
