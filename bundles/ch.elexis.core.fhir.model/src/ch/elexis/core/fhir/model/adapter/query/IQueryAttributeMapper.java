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

import java.util.function.Function;

/**
 * Interface for mapping Elexis model attribute names to FHIR search parameters.
 * 
 * <p>Implementations of this interface provide the mapping between Elexis model
 * attributes (e.g., "firstName", "lastName", "dateOfBirth") and their corresponding
 * FHIR search parameters (e.g., "Patient.GIVEN", "Patient.FAMILY", "Patient.BIRTHDATE").</p>
 * 
 * <p>Additionally, implementations can provide value transformers for converting
 * Elexis attribute values to their FHIR representation.</p>
 */
public interface IQueryAttributeMapper {

	/**
	 * Maps an Elexis model attribute name to a FHIR search parameter.
	 * 
	 * @param attributeName the Elexis attribute name (e.g., "firstName", "lastName")
	 * @return the FHIR search parameter name (e.g., "Patient.GIVEN", "Patient.FAMILY"),
	 *         or null if the attribute is not mappable
	 */
	String mapAttribute(String attributeName);

	/**
	 * Returns a transformer function for converting attribute values from Elexis format
	 * to FHIR search format.
	 * 
	 * <p>For example, gender values may need to be converted from Elexis Gender enum
	 * to FHIR AdministrativeGender codes.</p>
	 * 
	 * @param attributeName the Elexis attribute name
	 * @return a function that transforms the value, or null if no transformation is needed
	 */
	Function<Object, Object> getValueTransformer(String attributeName);

	/**
	 * Checks if this mapper supports the given model class.
	 * 
	 * @param modelClass the model class
	 * @return true if this mapper can handle attributes for the given class
	 */
	boolean supports(Class<?> modelClass);
}
