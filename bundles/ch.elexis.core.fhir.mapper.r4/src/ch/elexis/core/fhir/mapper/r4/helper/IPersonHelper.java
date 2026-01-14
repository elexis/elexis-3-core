package ch.elexis.core.fhir.mapper.r4.helper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.HumanName.NameUse;

import ch.elexis.core.model.IPerson;
import ch.elexis.core.types.Gender;

public class IPersonHelper extends IContactHelper {

	private String createLabel(IPerson person) {
		StringBuilder sb = new StringBuilder();
		String titel = person.getTitel();
		String firstName = person.getFirstName();
		String lastName = person.getLastName();
		String titelSuffix = person.getTitelSuffix();

		if (StringUtils.isNotBlank(titel)) {
			sb.append(titel + StringUtils.SPACE);
		}
		sb.append(firstName);
		sb.append(StringUtils.SPACE + lastName);
		if (StringUtils.isNotBlank(titelSuffix)) {
			sb.append(", " + titelSuffix);
		}
		return sb.toString();
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

	public Date getBirthDate(IPerson kontakt) {
		LocalDateTime dateOfBirth = kontakt.getDateOfBirth();
		if (dateOfBirth != null) {
			return getDate(dateOfBirth);
		}
		return null;
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

	public void mapHumanName(List<HumanName> names, IPerson target) {
		target.setFirstName(null);
		target.setLastName(null);
		target.setTitel(null);
		target.setTitelSuffix(null);

		for (HumanName humanName : names) {
			if (names.size() == 1 || HumanName.NameUse.OFFICIAL.equals(humanName.getUse())) {
				target.setFirstName(humanName.getGivenAsSingleString());
				target.setLastName(humanName.getFamily());
				target.setTitel(humanName.getPrefixAsSingleString());
				target.setTitelSuffix(humanName.getSuffixAsSingleString());
			}
		}
	}

	public void mapGender(AdministrativeGender gender, IPerson target) {
		if (gender != null) {
			switch (gender) {
			case FEMALE:
				target.setGender(Gender.FEMALE);
				break;
			case MALE:
				target.setGender(Gender.MALE);
				break;
			case UNKNOWN:
				target.setGender(Gender.UNKNOWN);
				break;
			default:
				target.setGender(Gender.UNDEFINED);
			}
		}
	}

	public void mapBirthDate(Date birthDate, IPerson target) {
		if (birthDate != null) {
			LocalDateTime dob = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			target.setDateOfBirth(dob);
		} else {
			target.setDateOfBirth(null);
		}
	}

}
