/**
 * Copyright (c) 2026 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 */
package ch.elexis.core.fhir.model.adapter.query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.rest.gclient.ICriterion;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.types.Gender;

/**
 * Attribute mapper for {@link IPatient} and {@link IPerson} model classes.
 * 
 * <p>
 * Maps Elexis Patient/Person attributes to FHIR Patient search parameters.
 * Supports common attributes like firstName, lastName, dateOfBirth, gender,
 * etc.
 * </p>
 * 
 * <h3>Supported Attribute Mappings:</h3>
 * <table>
 * <tr>
 * <th>Elexis Attribute</th>
 * <th>FHIR Search Parameter</th>
 * <th>Value Transformation</th>
 * </tr>
 * <tr>
 * <td>firstName, description2</td>
 * <td>given</td>
 * <td>String (no transformation)</td>
 * </tr>
 * <tr>
 * <td>lastName, description1</td>
 * <td>family</td>
 * <td>String (no transformation)</td>
 * </tr>
 * <tr>
 * <td>name</td>
 * <td>name</td>
 * <td>String (combined name search)</td>
 * </tr>
 * <tr>
 * <td>dateOfBirth</td>
 * <td>birthdate</td>
 * <td>LocalDateTime to FHIR date format (yyyy-MM-dd)</td>
 * </tr>
 * <tr>
 * <td>gender</td>
 * <td>gender</td>
 * <td>Gender enum to FHIR AdministrativeGender code</td>
 * </tr>
 * <tr>
 * <td>patientNr</td>
 * <td>identifier</td>
 * <td>String (patient number as identifier value)</td>
 * </tr>
 * <tr>
 * <td>dateOfDeath</td>
 * <td>deceased</td>
 * <td>LocalDateTime to FHIR date format (yyyy-MM-dd)</td>
 * </tr>
 * <tr>
 * <td>maritalStatus</td>
 * <td>N/A</td>
 * <td>Not supported (not mappable to standard FHIR Patient search)</td>
 * </tr>
 * </table>
 * 
 * @see IQueryAttributeMapper
 */
public class PatientQueryAttributeMapper implements IQueryAttributeMapper {

	private static final Map<String, String> ATTRIBUTE_MAP = new HashMap<>();
	private static final Map<String, Function<Object, Object>> VALUE_TRANSFORMERS = new HashMap<>();

	// FHIR date format: yyyy-MM-dd
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

	static {
		// Map Elexis attribute names to FHIR Patient search parameters
		// Note: description1 and description2 are the actual field names in the
		// database
		// for lastName and firstName respectively (from IPerson annotations)
		ATTRIBUTE_MAP.put("firstName", "given");
		ATTRIBUTE_MAP.put("description2", "given");
		ATTRIBUTE_MAP.put("lastName", "family");
		ATTRIBUTE_MAP.put("description1", "family");
		ATTRIBUTE_MAP.put("name", "name"); // Combined name search
		ATTRIBUTE_MAP.put("dateOfBirth", "birthdate");
		ATTRIBUTE_MAP.put("dob", "birthdate");
		ATTRIBUTE_MAP.put("gender", "gender");
		ATTRIBUTE_MAP.put("patientNr", "identifier");
		ATTRIBUTE_MAP.put("dateOfDeath", "deceased");
		ATTRIBUTE_MAP.put("maritalStatus", null); // Not directly mappable

		// Value transformers
		VALUE_TRANSFORMERS.put("birthdate", PatientQueryAttributeMapper::transformLocalDateTimeToDateString);
		VALUE_TRANSFORMERS.put("dateOfDeath", PatientQueryAttributeMapper::transformLocalDateTimeToDateString);
		VALUE_TRANSFORMERS.put("gender", PatientQueryAttributeMapper::transformGender);
	}

	@Override
	public String mapAttribute(String attributeName) {
		return ATTRIBUTE_MAP.get(attributeName);
	}

	@Override
	public Function<Object, Object> getValueTransformer(String attributeName) {
		return VALUE_TRANSFORMERS.get(attributeName);
	}

	@Override
	public boolean supports(Class<?> modelClass) {
		return IPatient.class.equals(modelClass) || IPerson.class.equals(modelClass)
				|| (modelClass != null && IPerson.class.isAssignableFrom(modelClass));
	}

	/**
	 * Builds a HAPI FHIR search criterion for the given Elexis attribute.
	 * 
	 * @param attributeName the Elexis attribute name
	 * @param comparator    the Elexis comparator
	 * @param value         the value to search for
	 * @return an ICriterion that can be applied to a HAPI IQuery, or null if not
	 *         supported
	 */
	public ICriterion<?> buildCriterion(String attributeName, ch.elexis.core.services.IQuery.COMPARATOR comparator,
			Object value) {
		if (value == null) {
			return null;
		}

		// Transform value if transformer exists
		Function<Object, Object> transformer = getValueTransformer(attributeName);
		Object transformedValue = transformer != null ? transformer.apply(value) : value;

		if (transformedValue == null) {
			return null;
		}

		if (transformedValue instanceof String) {
			transformedValue = ((String) transformedValue).replaceAll("%", "");
		}

		return switch (attributeName) {
		case "firstName", "description2" -> buildGivenCriterion(comparator, (String) transformedValue);
		case "lastName", "description1" -> buildFamilyCriterion(comparator, (String) transformedValue);
		case "name" -> buildNameCriterion(comparator, (String) transformedValue);
		case "dateOfBirth" -> buildBirthdateCriterion(comparator, (String) transformedValue);
		case "gender" -> buildGenderCriterion(comparator, (String) transformedValue);
		case "patientNr" -> buildIdentifierCriterion(comparator, (String) transformedValue);
		case "dateOfDeath" -> buildDeceasedCriterion(comparator, (String) transformedValue);
		default -> null;
		};
	}

	/**
	 * Builds criterion for Patient.given (first name).
	 */
	private ICriterion<?> buildGivenCriterion(ch.elexis.core.services.IQuery.COMPARATOR comparator, String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return switch (comparator) {
		case EQUALS -> Patient.GIVEN.matches().value(value);
		case LIKE -> Patient.GIVEN.contains().value(value);
		case NOT_EQUALS -> throw new UnsupportedOperationException(
				"NOT_EQUALS comparator not supported for given name parameter - FHIR does not support negation for string parameters in this API version");
		case NOT_LIKE -> throw new UnsupportedOperationException(
				"NOT_LIKE comparator not supported for given name parameter - FHIR does not support negation for string parameters in this API version");
		default -> throw new UnsupportedOperationException(
				"Comparator " + comparator + " not supported for given name parameter");
		};
	}

	/**
	 * Builds criterion for Patient.family (last name).
	 */
	private ICriterion<?> buildFamilyCriterion(ch.elexis.core.services.IQuery.COMPARATOR comparator, String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return switch (comparator) {
		case EQUALS -> Patient.FAMILY.matches().value(value);
		case LIKE -> Patient.FAMILY.contains().value(value);
		case NOT_EQUALS -> throw new UnsupportedOperationException(
				"NOT_EQUALS comparator not supported for family name parameter - FHIR does not support negation for string parameters in this API version");
		case NOT_LIKE -> throw new UnsupportedOperationException(
				"NOT_LIKE comparator not supported for family name parameter - FHIR does not support negation for string parameters in this API version");
		default -> throw new UnsupportedOperationException(
				"Comparator " + comparator + " not supported for family name parameter");
		};
	}

	/**
	 * Builds criterion for Patient.name (combined name search).
	 */
	private ICriterion<?> buildNameCriterion(ch.elexis.core.services.IQuery.COMPARATOR comparator, String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return switch (comparator) {
		case EQUALS -> Patient.NAME.matches().value(value);
		case LIKE -> Patient.NAME.contains().value(value);
		case NOT_EQUALS -> throw new UnsupportedOperationException(
				"NOT_EQUALS comparator not supported for name parameter - FHIR does not support negation for string parameters in this API version");
		case NOT_LIKE -> throw new UnsupportedOperationException(
				"NOT_LIKE comparator not supported for name parameter - FHIR does not support negation for string parameters in this API version");
		default ->
			throw new UnsupportedOperationException("Comparator " + comparator + " not supported for name parameter");
		};
	}

	/**
	 * Builds criterion for Patient.birthdate.
	 */
	private ICriterion<?> buildBirthdateCriterion(ch.elexis.core.services.IQuery.COMPARATOR comparator, String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return switch (comparator) {
		case EQUALS -> Patient.BIRTHDATE.exactly().day(value);
		case GREATER -> Patient.BIRTHDATE.afterOrEquals().day(value);
		case GREATER_OR_EQUAL -> Patient.BIRTHDATE.afterOrEquals().day(value);
		case LESS -> Patient.BIRTHDATE.beforeOrEquals().day(value);
		case LESS_OR_EQUAL -> Patient.BIRTHDATE.beforeOrEquals().day(value);
		case NOT_EQUALS -> throw new UnsupportedOperationException(
				"NOT_EQUALS comparator not supported for birthdate parameter - FHIR date negation requires 'ne' prefix not available in this API version");
		default -> throw new UnsupportedOperationException(
				"Comparator " + comparator + " not supported for birthdate parameter");
		};
	}

	/**
	 * Builds criterion for Patient.gender.
	 */
	private ICriterion<?> buildGenderCriterion(ch.elexis.core.services.IQuery.COMPARATOR comparator, String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return switch (comparator) {
		case EQUALS -> Patient.GENDER.exactly().code(value);
		case NOT_EQUALS -> throw new UnsupportedOperationException(
				"NOT_EQUALS comparator not supported for gender parameter - FHIR token negation requires :not modifier not available in this API version");
		default ->
			throw new UnsupportedOperationException("Comparator " + comparator + " not supported for gender parameter");
		};
	}

	/**
	 * Builds criterion for Patient.identifier (patient number).
	 */
	private ICriterion<?> buildIdentifierCriterion(ch.elexis.core.services.IQuery.COMPARATOR comparator, String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return switch (comparator) {
		case EQUALS -> Patient.IDENTIFIER.exactly().identifier(value);
		case LIKE -> Patient.IDENTIFIER.exactly().identifier(value); // LIKE not directly supported for token, fall back
																		// to exact
		case NOT_EQUALS -> throw new UnsupportedOperationException(
				"NOT_EQUALS comparator not supported for identifier parameter - FHIR token negation requires :not modifier not available in this API version");
		case NOT_LIKE -> throw new UnsupportedOperationException(
				"NOT_LIKE comparator not supported for identifier parameter - FHIR token negation requires :not modifier not available in this API version");
		default -> throw new UnsupportedOperationException(
				"Comparator " + comparator + " not supported for identifier parameter");
		};
	}

	/**
	 * Builds criterion for Patient.deceased.
	 */
	/**
	 * Builds criterion for Patient.deceased. Note: Patient.DECEASED is a
	 * TokenClientParam in HAPI FHIR R4, not DateClientParam.
	 */
	private ICriterion<?> buildDeceasedCriterion(ch.elexis.core.services.IQuery.COMPARATOR comparator, String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return switch (comparator) {
		case EQUALS -> Patient.DECEASED.exactly().code(value);
		case GREATER, GREATER_OR_EQUAL -> throw new UnsupportedOperationException(
				"GREATER and GREATER_OR_EQUAL comparators not supported for deceased (token) parameter");
		case LESS, LESS_OR_EQUAL -> throw new UnsupportedOperationException(
				"LESS and LESS_OR_EQUAL comparators not supported for deceased (token) parameter");
		case NOT_EQUALS -> throw new UnsupportedOperationException(
				"NOT_EQUALS comparator not supported for deceased parameter - FHIR token negation requires :not modifier not available in this API version");
		default -> throw new UnsupportedOperationException(
				"Comparator " + comparator + " not supported for deceased parameter");
		};
	}

	/**
	 * Transforms LocalDateTime to FHIR date format string (yyyy-MM-dd).
	 */
	private static String transformLocalDateTimeToDateString(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof LocalDateTime) {
			return ((LocalDateTime) value).toLocalDate().format(DATE_FORMATTER);
		}
		if (value instanceof LocalDate) {
			return ((LocalDate) value).format(DATE_FORMATTER);
		}
		if (value instanceof String) {
			return (String) value;
		}
		return value.toString();
	}

	/**
	 * Transforms Elexis Gender enum to FHIR AdministrativeGender code.
	 */
	private static String transformGender(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Gender) {
			return switch (((Gender) value)) {
			case MALE -> "male";
			case FEMALE -> "female";
			case UNKNOWN, UNDEFINED -> "unknown";
			};
		}
		if (value instanceof String) {
			return (String) value;
		}
		return value.toString().toLowerCase();
	}
}
