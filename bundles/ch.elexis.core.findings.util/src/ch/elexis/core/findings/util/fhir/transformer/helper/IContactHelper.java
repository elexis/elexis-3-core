package ch.elexis.core.findings.util.fhir.transformer.helper;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Address.AddressUse;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.StringType;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IImage;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.MimeType;
import ch.elexis.core.services.IXidService;
import ch.elexis.core.types.Country;

public class IContactHelper extends AbstractHelper {

	public String getOrganizationName(IOrganization organization) {
		StringBuilder sb = new StringBuilder();
		if (organization.isOrganization()) {
			if (organization.getDescription1() != null) {
				sb.append(organization.getDescription1());
			}
			if (organization.getDescription2() != null) {
				if (sb.length() > 0) {
					sb.append(StringUtils.SPACE);
				}
				sb.append(organization.getDescription2());
			}
		}
		return sb.toString();
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

	public List<Identifier> getIdentifiers(IContact contact, IXidService xidService) {
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

	public void mapAddress(List<Address> sourceAdresses, IContact target) {
		target.setCity(null);
		target.setZip(null);
		target.setStreet(null);
		target.setCountry(null);

		for (Address address : sourceAdresses) {
			if (sourceAdresses.size() == 1 || AddressUse.HOME.equals(address.getUse())) {
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

	}

	public void mapTelecom(List<ContactPoint> sourceTelecoms, IContact target) {
		target.setMobile(null);
		target.setPhone1(null);
		target.setPhone2(null);
		target.setEmail(null);
		target.setFax(null);
		target.setWebsite(null);

		for (ContactPoint contactPoint : sourceTelecoms) {
			if (ContactPointSystem.PHONE.equals(contactPoint.getSystem())) {
				if (ContactPointUse.MOBILE.equals(contactPoint.getUse())) {
					target.setMobile(contactPoint.getValue());
				} else if (0 == contactPoint.getRank() || 1 == contactPoint.getRank()) {
					target.setPhone1(contactPoint.getValue());
				} else if (2 == contactPoint.getRank()) {
					target.setPhone2(contactPoint.getValue());
				}
			} else if (ContactPointSystem.EMAIL.equals(contactPoint.getSystem())) {
				target.setEmail(contactPoint.getValue());
			} else if (ContactPointSystem.FAX.equals(contactPoint.getSystem())) {
				target.setFax(contactPoint.getValue());
			} else if (ContactPointSystem.URL.equals(contactPoint.getSystem())) {
				target.setWebsite(contactPoint.getValue());
			}
		}
	}

	public Attachment mapContactImage(IContact source) {
		IImage image = source.getImage();
		Attachment contactImage = null;
		if (image != null) {
			Attachment _image = new Attachment();
			MimeType mimeType = image.getMimeType();
			_image.setContentType((mimeType != null) ? mimeType.getContentType() : null);
			_image.setData(image.getImage());
			contactImage = _image;
		}
		return contactImage;
	}
	
}
