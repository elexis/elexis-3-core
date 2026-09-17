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

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating {@link FhirQueryAdapter} instances.
 * 
 * <p>
 * This factory provides a convenient way to create query adapters for Elexis
 * model classes that map to FHIR resources. It automatically selects the
 * appropriate attribute mapper based on the model class.
 * </p>
 * 
 * <h3>Usage Example:</h3>
 * 
 * <pre>{@code
 * // Create a query adapter for IPatient
 * ch.elexis.core.services.IQuery<IPatient> query = FhirQueryAdapterFactory.createPatientQuery();
 * 
 * // Use the query
 * List<IPatient> patients = query.and("lastName", ch.elexis.core.services.IQuery.COMPARATOR.EQUALS, "Smith")
 * 		.and("firstName", ch.elexis.core.services.IQuery.COMPARATOR.LIKE, "John")
 * 		.orderBy("lastName", ch.elexis.core.services.IQuery.ORDER.ASC).limit(10).execute();
 * }</pre>
 * 
 * @see FhirQueryAdapter
 * @see IQueryAttributeMapper
 */
public class FhirQueryAdapterFactory {

	private static final Map<Class<?>, IQueryAttributeMapper> ATTRIBUTE_MAPPERS = new HashMap<>();

	static {
		// Register default mappers
		registerMapper(new PatientQueryAttributeMapper());
	}

	/**
	 * Registers a custom attribute mapper for a specific model class.
	 * 
	 * @param mapper the attribute mapper to register
	 */
	public static void registerMapper(IQueryAttributeMapper mapper) {
		// For now, we use a simple approach: the mapper handles its supported classes
		// In a more sophisticated implementation, we might want to prioritize or chain
		// mappers
		ATTRIBUTE_MAPPERS.put(mapper.getClass(), mapper);
	}

	/**
	 * Gets the appropriate attribute mapper for the given model class.
	 * 
	 * @param modelClass the model class
	 * @return the attribute mapper, or null if none found
	 */
	public static IQueryAttributeMapper getMapper(Class<?> modelClass) {
		// Try to find a mapper that supports the model class
		for (IQueryAttributeMapper mapper : ATTRIBUTE_MAPPERS.values()) {
			if (mapper.supports(modelClass)) {
				return mapper;
			}
		}
		return null;
	}

}
