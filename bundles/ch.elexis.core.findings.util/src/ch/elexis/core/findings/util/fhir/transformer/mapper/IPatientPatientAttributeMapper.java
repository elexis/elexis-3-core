package ch.elexis.core.findings.util.fhir.transformer.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Address.AddressUse;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointUse;
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
import ch.elexis.core.findings.IdentifierSystem;
import ch.elexis.core.findings.util.fhir.transformer.helper.IContactHelper;
import ch.elexis.core.model.IImage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.MaritalStatus;
import ch.elexis.core.model.MimeType;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IUserService;
import ch.elexis.core.services.IXidService;
import ch.elexis.core.types.Country;
import ch.elexis.core.types.Gender;

public class IPatientPatientAttributeMapper implements IdentifiableDomainResourceAttributeMapper<IPatient, Patient> {

	private IContactHelper contactHelper;
	private IModelService coreModelService;
	private IUserService userService;

	public IPatientPatientAttributeMapper(IModelService coreModelService, IXidService xidService,
			IUserService userService) {
		this.contactHelper = new IContactHelper(coreModelService, xidService, userService);
		this.coreModelService = coreModelService;
	}

	@Override
	public void elexisToFhir(IPatient source, Patient target, SummaryEnum summaryEnum, Set<Include> includes) {

		target.setId(new IdDt("Patient", source.getId()));
		mapMetaData(source, target);
		if (SummaryEnum.DATA != summaryEnum) {
			mapNarrative(source, target);
		}
		if (SummaryEnum.TEXT == summaryEnum || SummaryEnum.COUNT == summaryEnum) {
			return;
		}

		mapIdentifiersAndPatientNumber(source, target);
		target.setName(contactHelper.getHumanNames(source));
		target.setGender(contactHelper.getGender(source.getGender()));
		target.setBirthDate(contactHelper.getBirthDate(source));
		mapAddressTelecom(source, target);
		mapComments(source, target);
		mapMaritalStatus(source, target);
		mapRelatedContacts(source, target);
		mapContactImage(source, target);
	}

	@Override
	public void fhirToElexis(Patient source, IPatient target) {
		mapIdentifiers(source, target);
		mapName(source, target);
		mapGender(source, target);
		mapBirthDate(source, target);
		mapAddressTelecom(source, target);
		mapComments(source, target);
		mapMaritalStatus(source, target);
		mapContacts(source, target);
		mapContactImage(source, target);
	}

	private void mapContacts(Patient source, IPatient target) {

	}

	private void mapRelatedContacts(IPatient source, Patient target) {
		List<ContactComponent> contacts = new ArrayList<>();

		IPerson legalGuardian = source.getLegalGuardian();
		if (legalGuardian != null) {
			ContactComponent _legalGuardian = new ContactComponent();

			CodeableConcept addCoding = new CodeableConcept().addCoding(new Coding().setCode("N"));
			_legalGuardian.setRelationship(Collections.singletonList(addCoding));
			_legalGuardian.setId(legalGuardian.getId());
			List<HumanName> humanNames = contactHelper.getHumanNames(legalGuardian);
			_legalGuardian.setName((!humanNames.isEmpty()) ? humanNames.get(0) : null);
			List<Address> addresses = contactHelper.getAddresses(legalGuardian);
			_legalGuardian.setAddress((!addresses.isEmpty()) ? addresses.get(0) : null);
			AdministrativeGender gender = contactHelper.getGender(legalGuardian.getGender());
			_legalGuardian.setGender(gender);
			List<ContactPoint> contactPoints = contactHelper.getContactPoints(legalGuardian);
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
		List<Address> addresses = contactHelper.getAddresses(source);
		target.setAddress(addresses);
		List<ContactPoint> contactPoints = contactHelper.getContactPoints(source);
		target.setTelecom(contactPoints);
	}

	private void mapAddressTelecom(Patient source, IPatient target) {
		List<Address> addresses = source.getAddress();
		for (Address address : addresses) {
			if (AddressUse.HOME.equals(address.getUse())) {
				target.setCity(address.getCity());
				target.setZip(address.getPostalCode());
				if (!address.getLine().isEmpty()) {
					target.setStreet(address.getLine().get(0).asStringValue());
				}
				Country country = null;
				try {
					country = Country.valueOf(address.getCountry());
				} catch (IllegalArgumentException | NullPointerException e) {
					// ignore
				}
				target.setCountry(country);
			}
		}

		List<ContactPoint> telecoms = source.getTelecom();
		for (ContactPoint contactPoint : telecoms) {
			if (ContactPointSystem.PHONE.equals(contactPoint.getSystem())) {
				if (ContactPointUse.MOBILE.equals(contactPoint.getUse())) {
					target.setMobile(contactPoint.getValue());
				} else if (1 == contactPoint.getRank()) {
					target.setPhone1(contactPoint.getValue());
				} else if (2 == contactPoint.getRank()) {
					target.setPhone2(contactPoint.getValue());
				}
			} else if (ContactPointSystem.EMAIL.equals(contactPoint.getSystem())) {
				target.setEmail(contactPoint.getValue());
			} else if (ContactPointSystem.FAX.equals(contactPoint.getSystem())) {
				target.setFax(contactPoint.getValue());
			}
		}
	}

	private void mapBirthDate(Patient source, IPatient target) {
		if (source.getBirthDate() != null) {
			LocalDateTime dob = source.getBirthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			target.setDateOfBirth(dob);
		}
	}

	private void mapGender(Patient source, IPatient target) {
		AdministrativeGender gender = source.getGender();
		if (gender != null) {
			switch (gender) {
				case FEMALE :
					target.setGender(Gender.FEMALE);
					break;
				case MALE :
					target.setGender(Gender.MALE);
					break;
				case UNKNOWN :
					target.setGender(Gender.UNKNOWN);
					break;
				default :
					target.setGender(Gender.UNDEFINED);
			}
		}
	}

	private void mapName(Patient source, IPatient target) {
		List<HumanName> names = source.getName();
		for (HumanName humanName : names) {
			if (HumanName.NameUse.OFFICIAL.equals(humanName.getUse())) {
				target.setFirstName(humanName.getGivenAsSingleString());
				target.setLastName(humanName.getFamily());
				target.setTitel(humanName.getPrefixAsSingleString());
				target.setTitelSuffix(humanName.getSuffixAsSingleString());
			}
		}
	}

	private void mapIdentifiersAndPatientNumber(IPatient source, Patient target) {
		List<Identifier> identifiers = contactHelper.getIdentifiers(source);
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
		}
	}

	private Identifier getElexisObjectIdentifier(Identifiable dbObject) {
		Identifier identifier = new Identifier();
		identifier.setSystem(IdentifierSystem.ELEXIS_OBJID.getSystem());
		identifier.setValue(dbObject.getId());
		return identifier;
	}

}
