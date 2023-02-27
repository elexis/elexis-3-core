package ch.elexis.core.findings.util.fhir.transformer.mapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.fhir.FhirChConstants;
import ch.elexis.core.findings.IdentifierSystem;
import ch.elexis.core.findings.util.fhir.transformer.helper.IPersonHelper;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IImage;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.MaritalStatus;
import ch.elexis.core.model.MimeType;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IXidService;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class IPatientPatientAttributeMapper implements IdentifiableDomainResourceAttributeMapper<IPatient, Patient> {

	private IPersonHelper personHelper;
	private IModelService coreModelService;
	private IXidService xidService;

	public IPatientPatientAttributeMapper(IModelService coreModelService, IXidService xidService) {
		this.personHelper = new IPersonHelper();
		this.coreModelService = coreModelService;
		this.xidService = xidService;
	}

	@Override
	public void elexisToFhir(IPatient source, Patient target, SummaryEnum summaryEnum, Set<Include> includes) {

		target.setId(new IdDt(Patient.class.getSimpleName(), source.getId()));
		mapMetaData(source, target);
		if (SummaryEnum.DATA != summaryEnum) {
			mapNarrative(source, target);
		}
		if (SummaryEnum.TEXT == summaryEnum || SummaryEnum.COUNT == summaryEnum) {
			return;
		}

		mapIdentifiersAndPatientNumber(source, target);
		target.setName(personHelper.getHumanNames(source));
		target.setGender(personHelper.getGender(source.getGender()));
		target.setBirthDate(personHelper.getBirthDate(source));
		mapAddressTelecom(source, target);
		mapComments(source, target);
		mapMaritalStatus(source, target);
		mapRelatedContacts(source, target);
		mapContactImage(source, target);
	}

	@Override
	public void fhirToElexis(Patient source, IPatient target) {
		mapIdentifiers(source, target);
		personHelper.mapHumanName(source.getName(), target);
		personHelper.mapGender(source.getGender(), target);
		personHelper.mapBirthDate(source.getBirthDate(), target);
		personHelper.mapAddress(source.getAddress(), target);
		personHelper.mapTelecom(source.getTelecom(), target);
		mapComments(source, target);
		mapMaritalStatus(source, target);
		mapContactImage(source, target);
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
				IPerson legalGuardianPerson = CoreModelServiceHolder.get().load(legalGuardian.getId(), IPerson.class)
						.get();
				List<HumanName> humanNames = personHelper.getHumanNames(legalGuardianPerson);
				_legalGuardian.setName((!humanNames.isEmpty()) ? humanNames.get(0) : null);
				AdministrativeGender gender = personHelper.getGender(legalGuardianPerson.getGender());
				_legalGuardian.setGender(gender);
			} else if (legalGuardian.isOrganization()) {
				IOrganization legalGuardianOrganization = CoreModelServiceHolder.get()
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

	private void mapContactImage(IPatient source, Patient target) {
		IImage image = source.getImage();
		if (image != null) {
			Attachment _image = new Attachment();
			MimeType mimeType = image.getMimeType();
			_image.setContentType((mimeType != null) ? mimeType.getContentType() : null);
			_image.setData(image.getImage());
			target.setPhoto(Collections.singletonList(_image));
		}
	}

	private void mapContactImage(Patient source, IPatient target) {
		if (!source.getPhoto().isEmpty()) {
			Attachment fhirImage = source.getPhoto().get(0);
			IImage image = coreModelService.create(IImage.class);
			image.setDate(LocalDate.now());
			String contentType = fhirImage.getContentTypeElement().asStringValue();
			MimeType mimeType = MimeType.getByContentType(contentType);
			image.setMimeType(mimeType);
			image.setImage(fhirImage.getData());
			target.setImage(image);
		} else {
			target.setImage(null);
		}
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

	private void mapAddressTelecom(IPatient source, Patient target) {
		List<Address> addresses = personHelper.getAddresses(source);
		target.setAddress(addresses);
		List<ContactPoint> contactPoints = personHelper.getContactPoints(source);
		target.setTelecom(contactPoints);
	}

	private void mapIdentifiersAndPatientNumber(IPatient source, Patient target) {
		List<Identifier> identifiers = personHelper.getIdentifiers(source, xidService);
		identifiers.add(getElexisObjectIdentifier(source));

		String patNr = source.getPatientNr();
		Identifier identifier = new Identifier();
		identifier.setSystem(IdentifierSystem.ELEXIS_PATNR.getSystem());
		identifier.setValue(patNr);
		identifiers.add(identifier);
		target.setIdentifier(identifiers);
	}

	/**
	 * Selective support for mapping incoming identifiers. Currently only accepts
	 * AHV Number
	 *
	 * @param source
	 * @param target
	 */
	private void mapIdentifiers(Patient source, IPatient target) {
		// id must not be mapped (not updateable)
		// patientNumber must not be mapped (not updateable)
		List<Identifier> identifiers = source.getIdentifier();
		for (Identifier identifier : identifiers) {
			if (XidConstants.CH_AHV.equals(identifier.getSystem())) {
				target.addXid(XidConstants.CH_AHV, identifier.getValue(), true);
			}
			if (FhirChConstants.OID_AHV13_SYSTEM.equals(identifier.getSystem())) {
				target.addXid(XidConstants.CH_AHV, identifier.getValue(), true);
			}
		}
	}

}
