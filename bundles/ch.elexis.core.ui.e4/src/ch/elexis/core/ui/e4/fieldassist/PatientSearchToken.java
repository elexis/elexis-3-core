package ch.elexis.core.ui.e4.fieldassist;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;

public class PatientSearchToken {

	private boolean ignore;

	private boolean lastname;
	private boolean firstname;
	private boolean name;

	private String token;

	private DateTimeFormatter timestampFormat = DateTimeFormatter.ofPattern("yyyyMMdd");

	private PatientSearchToken(String token) {
		this.token = token;
	}

	public void setIsIgnore() {
		ignore = true;
	}

	public void setIsFirstname() {
		firstname = true;
	}

	public void setIsLastname() {
		lastname = true;
	}

	public void setIsName() {
		name = true;
	}

	public static PatientSearchToken of(String string) {
		PatientSearchToken ret = new PatientSearchToken(string);
		return ret;
	}

	public void apply(IQuery<?> query) {
		if (isPatientCode()) {
			query.and("code", COMPARATOR.EQUALS, token.substring(1));
		} else if (isDate()) {
			query.and("dob", COMPARATOR.LIKE, AsyncContentProposalProvider.getElexisDateSearchString(token));
		} else if (lastname) {
			query.and("description1", COMPARATOR.LIKE, token + "%", true);
		} else if (firstname) {
			query.and("description2", COMPARATOR.LIKE, token + "%", true);
		} else if (name) {
			query.startGroup();
			query.or("description1", COMPARATOR.LIKE, token + "%", true);
			query.or("description2", COMPARATOR.LIKE, token + "%", true);
			query.andJoinGroups();
		}
	}

	public boolean test(IPatient patient) {
		if (isPatientCode()) {
			return patient.getCode().equals(token.substring(1));
		} else if (isDate() && patient.getDateOfBirth() != null) {
			String searchToken = AsyncContentProposalProvider.getElexisDateSearchString(token);
			String compareToken = searchToken.replaceAll("%", "");
			if (searchToken.startsWith("%")) {
				return timestampFormat.format(patient.getDateOfBirth()).endsWith(compareToken);
			} else {
				return timestampFormat.format(patient.getDateOfBirth()).startsWith(compareToken);
			}
		} else if (lastname) {
			return patient.getDescription1().toLowerCase().startsWith(token.toLowerCase());
		} else if (firstname) {
			return patient.getDescription2().toLowerCase().startsWith(token.toLowerCase());
		} else if (name) {
			return patient.getDescription1().toLowerCase().startsWith(token.toLowerCase())
					|| patient.getDescription2().toLowerCase().startsWith(token.toLowerCase());
		}
		return false;
	}

	boolean isNameToken() {
		return !isDate() && !isPatientCode() && StringUtils.isNotBlank(token);
	}

	private boolean isDate() {
		if (token.length() == 4 && StringUtils.isNumeric(token)) {
			return true;
		} else if (token.length() > 2 && token.matches("[0-9\\.]+")) {
			return true;
		}
		return false;
	}

	private boolean isPatientCode() {
		return token.length() > 1 && token.startsWith("#");
	}

	public boolean ignore() {
		return ignore;
	}

	public static List<PatientSearchToken> getPatientSearchTokens(String[] split) {
		if (split != null && split.length > 0) {
			List<PatientSearchToken> tokens = Arrays.asList(split).stream().map(s -> PatientSearchToken.of(s))
					.collect(Collectors.toList());
			// update name tokens
			List<PatientSearchToken> nameTokens = tokens.stream().filter(st -> st.isNameToken())
					.collect(Collectors.toList());
			for (int i = 0; i < nameTokens.size(); i++) {
				if (i == 0 && nameTokens.size() == 1) {
					nameTokens.get(i).setIsName();
				} else if (i == 0) {
					nameTokens.get(i).setIsLastname();
				} else if (i == 1) {
					nameTokens.get(i).setIsFirstname();
				} else {
					nameTokens.get(i).setIsIgnore();
				}
			}
			return tokens.stream().filter(st -> !st.ignore()).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
}