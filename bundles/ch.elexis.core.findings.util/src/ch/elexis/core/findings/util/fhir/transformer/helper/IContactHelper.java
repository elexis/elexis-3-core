package ch.elexis.core.findings.util.fhir.transformer.helper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Address.AddressUse;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.HumanName.NameUse;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.StringType;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IXid;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IUserService;
import ch.elexis.core.services.IXidService;
import ch.elexis.core.types.Gender;

public class IContactHelper extends AbstractHelper {

	private IModelService modelService;
	private IXidService xidService;
	private IUserService userService;

	public IContactHelper(IModelService modelService, IXidService xidService, IUserService userService) {
		this.modelService = modelService;
		this.xidService = xidService;
		this.userService = userService;
	}

	public List<HumanName> getHumanNames(IPerson person) {
		List<HumanName> ret = new ArrayList<>();
		if (person.isPerson()) {
			HumanName humanName = new HumanName();
			humanName.setFamily(person.getLastName());
			humanName.addGiven(person.getFirstName());
			humanName.addPrefix(person.getTitel());
			humanName.addSuffix(person.getTitelSuffix());
			humanName.setText(createLabel(person));
			humanName.setUse(NameUse.OFFICIAL);
			ret.add(humanName);
		}
//		if (person.isUser()) {
//			List<IUser> userLocalObject = userService.getUsersByAssociatedContact(person);
//			if (!userLocalObject.isEmpty()) {
//				HumanName sysName = new HumanName();
//				sysName.setText(userLocalObject.get(0).getId());
//				sysName.setUse(NameUse..ANONYMOUS);
//				ret.add(sysName);
//			}
//		}
		return ret;
	}

	private String createLabel(IPerson person) {
		StringBuilder sb = new StringBuilder();
		String titel = person.getTitel();
		String firstName = person.getFirstName();
		String lastName = person.getLastName();
		String titelSuffix = person.getTitelSuffix();

		if (StringUtils.isNotBlank(titel)) {
			sb.append(titel + " ");
		}
		sb.append(firstName);
		sb.append(" " + lastName);
		if (StringUtils.isNotBlank(titelSuffix)) {
			sb.append(", " + titelSuffix);
		}
		return sb.toString();
	}

	public String getOrganizationName(IOrganization organization) {
		StringBuilder sb = new StringBuilder();
		if (organization.isOrganization()) {
			if (organization.getDescription1() != null) {
				sb.append(organization.getDescription1());
			}
			if (organization.getDescription2() != null) {
				if (sb.length() > 0) {
					sb.append(" ");
				}
				sb.append(organization.getDescription2());
			}
		}
		return sb.toString();
	}

	public AdministrativeGender getGender(Gender gender) {
		if (gender == Gender.FEMALE) {
			return AdministrativeGender.FEMALE;
		} else if (gender == Gender.MALE) {
			return AdministrativeGender.MALE;
		} else if (gender == Gender.UNKNOWN) {
			return AdministrativeGender.UNKNOWN;
		} else {
			return AdministrativeGender.OTHER;
		}
	}

	public Date getBirthDate(IPerson kontakt) {
		LocalDateTime dateOfBirth = kontakt.getDateOfBirth();
		if (dateOfBirth != null) {
			return getDate(dateOfBirth);
		}
		return null;
	}

	public List<Address> getAddresses(IContact contact) {
		List<Address> ret = new ArrayList<>();

		// main address data
		Address address = new Address();
		address.setUse(AddressUse.HOME);
		address.setCity(contact.getCity());
		address.setPostalCode(contact.getZip());
		address.setCountry((contact.getCountry() != null) ? contact.getCountry().name() : null);
		List<StringType> lines = new ArrayList<>();
		lines.add(new StringType(contact.getStreet()));
		address.setLine(lines);
		ret.add(address);

		return ret;
	}

	public List<ContactPoint> getContactPoints(IContact contact) {
		List<ContactPoint> ret = new ArrayList<>();
		if (contact.getPhone1() != null && !contact.getPhone1().isEmpty()) {
			ContactPoint contactPoint = new ContactPoint();
			contactPoint.setSystem(ContactPointSystem.PHONE);
			contactPoint.setRank(1);
			contactPoint.setValue(contact.getPhone1());
			ret.add(contactPoint);
		}
		if (contact.getPhone2() != null && !contact.getPhone2().isEmpty()) {
			ContactPoint contactPoint = new ContactPoint();
			contactPoint.setSystem(ContactPointSystem.PHONE);
			contactPoint.setValue(contact.getPhone2());
			contactPoint.setRank(2);
			ret.add(contactPoint);
		}
		if (contact.getMobile() != null && !contact.getMobile().isEmpty()) {
			ContactPoint contactPoint = new ContactPoint();
			contactPoint.setSystem(ContactPointSystem.PHONE);
			contactPoint.setUse(ContactPointUse.MOBILE);
			contactPoint.setValue(contact.getMobile());
			ret.add(contactPoint);
		}
		if (contact.getEmail() != null && !contact.getEmail().isEmpty()) {
			ContactPoint contactPoint = new ContactPoint();
			contactPoint.setSystem(ContactPointSystem.EMAIL);
			contactPoint.setValue(contact.getEmail());
			ret.add(contactPoint);
		}
		if (contact.getWebsite() != null && !contact.getWebsite().isEmpty()) {
			ContactPoint contactPoint = new ContactPoint();
			contactPoint.setSystem(ContactPointSystem.URL);
			contactPoint.setValue(contact.getWebsite());
			ret.add(contactPoint);
		}
		if (contact.getFax() != null && !contact.getFax().isEmpty()) {
			ContactPoint contactPoint = new ContactPoint();
			contactPoint.setSystem(ContactPointSystem.FAX);
			contactPoint.setValue(contact.getFax());
			ret.add(contactPoint);
		}
		return ret;
	}

	public List<Identifier> getIdentifiers(IContact contact) {
		List<Identifier> ret = new ArrayList<>();
		List<IXid> xids = xidService.getXids(contact);
		for (IXid xid : xids) {
			Identifier identifier = new Identifier();
			identifier.setSystem(xid.getDomain());
			identifier.setValue(xid.getDomainId());
			ret.add(identifier);
		}
		return ret;
	}
}
