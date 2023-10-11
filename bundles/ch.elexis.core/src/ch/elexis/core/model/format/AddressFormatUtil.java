package ch.elexis.core.model.format;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.types.Country;
import ch.elexis.core.types.Gender;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class AddressFormatUtil {

	/**
	 * Get a single line formatted representation of the {@link IPerson}. Including
	 * the following properties, if set.
	 * <li>Gender / Salutation</li>
	 * <li>Title</li>
	 * <li>Firstname</li>
	 * <li>Lastname</li>
	 * <li>Date of Birth</li>
	 * <li>Street</li>
	 * <li>Country</li>
	 * <li>Zip</li>
	 * <li>City</li>
	 * <li>Phone1</li>
	 * <li>Phone2</li>
	 * <li>Mobile</li>
	 * <li>Fax</li>
	 * <li>Email</li>
	 *
	 * @param person
	 * @return
	 */
	public static String getSingleLine(IPerson person) {
		StringBuilder ret = new StringBuilder();

		ret.append(PersonFormatUtil.getSalutation(person));
		ret.append(StringTool.space);

		// name with title
		String titel = person.getTitel();
		if (!StringTool.isNothing(titel)) {
			ret.append(titel).append(StringTool.space);
		}
		if (!StringTool.isNothing(person.getLastName())) {
			ret.append(person.getLastName() + StringTool.space);
		}
		if (!StringTool.isNothing(person.getFirstName())) {
			ret.append(person.getFirstName());
		}
		// date of birth
		LocalDateTime dateOfBirth = person.getDateOfBirth();
		if (dateOfBirth != null) {
			ret.append("," + StringTool.space + new TimeTool(dateOfBirth).toString(TimeTool.DATE_GER));
		}
		// address
		String thisAddressStreet = person.getStreet();
		if (!StringTool.isNothing(thisAddressStreet)) {
			ret.append("," + StringTool.space + thisAddressStreet);
		}
		Country thisAddressCountry = person.getCountry();
		if (thisAddressCountry != null && thisAddressCountry != Country.NDF) {
			ret.append("," + StringTool.space + thisAddressCountry.name() + "-");
		}
		String thisAddressZip = person.getZip();
		if (!StringTool.isNothing(thisAddressZip)) {
			if (StringTool.isNothing(thisAddressCountry)) {
				ret.append("," + StringTool.space);
			}
			ret.append(thisAddressZip);
		}
		String thisAddressCity = person.getCity();
		if (!StringTool.isNothing(thisAddressCity)) {
			if (StringTool.isNothing(thisAddressCountry) && StringTool.isNothing(thisAddressZip)) {
				ret.append(",");
			}
			ret.append(StringTool.space + thisAddressCity);
		}
		// phone numbers and fax
		String thisAddressPhone1 = person.getPhone1();
		if (!StringTool.isNothing(thisAddressPhone1)) {
			ret.append("," + StringTool.space + StringTool.space + thisAddressPhone1);
		}
		String thisAddressPhone2 = person.getPhone2();
		if (!StringTool.isNothing(thisAddressPhone2)) {
			ret.append("," + StringTool.space + StringTool.space + thisAddressPhone2);
		}
		String thisAddressMobile = person.getMobile();
		if (!StringTool.isNothing(thisAddressMobile)) {
			ret.append(
					"," + StringTool.space + Messages.Core_Mobilephone + StringTool.space + thisAddressMobile);
		}
		String thisAddressFax = person.getFax();
		if (!StringTool.isNothing(thisAddressFax)) {
			ret.append("," + StringTool.space + Messages.Core_Fax + StringTool.space + thisAddressFax);
		}
		// email
		String thisAddressEmail = person.getEmail();
		if (!StringTool.isNothing(thisAddressEmail)) {
			ret.append("," + StringTool.space + thisAddressEmail);
		}

		return ret.toString();
	}

	/**
	 * Get the full name of the contact depending on type {@link IPerson} or
	 * {@link IOrganization}. The following properties are used to determine name
	 * and salutation. </br>
	 * </br>
	 * IPerson:</br>
	 * <li>Gender / Salutation</li>
	 * <li>Title</li>
	 * <li>Firstname</li>
	 * <li>Lastname</li> </br>
	 * IOrganization:</br>
	 * <li>Description1</li>
	 * <li>Description2</li>
	 *
	 * @param contact
	 * @return
	 */
	public static String getFullnameWithSalutation(IContact contact) {
		StringBuilder sb = new StringBuilder();
		if (contact instanceof IPerson) {
			IPerson person = (IPerson) contact;

			String salutation;
			if (person.getGender() == Gender.MALE) {
				salutation = Messages.Contact_SalutationM;
			} else {
				salutation = Messages.Contact_SalutationF;
			}
			sb.append(salutation);
			sb.append(StringTool.lf);

			if (StringUtils.isNotBlank(person.getTitel())) {
				sb.append(person.getTitel()).append(StringTool.space);
			}
			sb.append(person.getFirstName()).append(StringTool.space).append(person.getLastName())
					.append(StringTool.lf);
		} else if (contact instanceof IOrganization) {
			IOrganization organization = (IOrganization) contact;
			sb.append(organization.getDescription1()).append(StringTool.space);
			if (StringUtils.isNotBlank(organization.getDescription2())) {
				sb.append(organization.getDescription2());
			}
			sb.append(StringTool.lf);
		}
		return sb.toString();
	}

	/**
	 * Create the postal address String of the contact using a {@link PostalAddress}
	 * object. The postal address property of the {@link IContact} is updated if it
	 * is empty.
	 *
	 * @param contact
	 * @return
	 */
	public static String createPostalAddress(IContact contact) {
		PostalAddress postalAddress = PostalAddress.of(contact);
		String ret = getFullnameWithSalutation(contact) + postalAddress.getWrittenAddress(false, true);
		// create the postal if it does not exist yet
		if (StringUtils.isEmpty(contact.getPostalAddress())) {
			contact.setPostalAddress(ret);
		}
		return ret;
	}

	/**
	 * Get the postal address of the {@link IContact}. If the postal address is
	 * currently empty, a new address String is generated using
	 * {@link AddressFormatUtil#createPostalAddress(IContact)}.
	 *
	 * @param contact
	 * @param multiline
	 * @return
	 */
	public static String getPostalAddress(IContact contact, boolean multiline) {
		String postalAddress = contact.getPostalAddress();
		if (StringTool.isNothing(postalAddress)) {
			postalAddress = createPostalAddress(contact);
		}
		postalAddress = postalAddress.replaceAll("[\\r\\n]\\n", StringTool.lf); //$NON-NLS-1$
		return multiline == true ? postalAddress : postalAddress.replaceAll("\\n", StringTool.space); //$NON-NLS-1$
	}

	/**
	 * Get address, phone, fax and email of the {@link IContact}.
	 *
	 * @param contact
	 * @param multiline
	 * @param includePhone
	 * @return
	 */
	public static String getAddressPhoneFaxEmail(IContact contact, boolean multiline, boolean includePhone) {

		StringBuffer thisAddress = new StringBuffer();

		thisAddress.append(getPostalAddress(contact, true).trim());
		thisAddress.append(System.lineSeparator());

		if (includePhone) {
			if (StringUtils.isNotBlank(contact.getPhone1())) {
				thisAddress.append(contact.getPhone1() + System.lineSeparator());
			}

			if (StringUtils.isNotBlank(contact.getPhone2())) {
				thisAddress.append(contact.getPhone2() + System.lineSeparator());
			}

			if (StringUtils.isNotBlank(contact.getMobile())) {
				thisAddress.append(Messages.Core_Mobilephone + ":" + StringTool.space + contact.getMobile()
						+ System.lineSeparator());
			}
		}

		if (StringUtils.isNotBlank(contact.getFax())) {
			thisAddress.append(Messages.Core_Fax + ":" + StringTool.space + contact.getFax()
					+ System.lineSeparator());
		}
		if (StringUtils.isNotBlank(contact.getEmail())) {
			thisAddress.append(contact.getEmail() + System.lineSeparator());
		}

		String an = thisAddress.toString();
		an = an.replaceAll("[\\r\\n]\\n", StringTool.lf); //$NON-NLS-1$
		return multiline == true ? an : an.replaceAll("\\n", StringTool.space); //$NON-NLS-1$
	}
}
