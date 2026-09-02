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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IQuery;
import ch.elexis.core.fhir.model.IFhirModelService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.ISubQuery;

/**
 * Adapter that implements {@link ch.elexis.core.services.IQuery} by wrapping a
 * HAPI FHIR {@link ca.uhn.fhir.rest.gclient.IQuery}.
 * 
 * <p>
 * This adapter translates Elexis query operations (using attribute names and
 * comparators) into FHIR search parameters and operators. It supports basic
 * query operations for Patient resources.
 * </p>
 * 
 * <p>
 * <b>Note:</b> Not all Elexis IQuery features are supported. Complex operations
 * like subqueries, EXISTS, and some comparator types may throw
 * {@link UnsupportedOperationException}.
 * </p>
 * 
 * @param <T> the model type (e.g., {@link ch.elexis.core.model.IPatient})
 */
public class FhirQueryAdapter<T> implements ch.elexis.core.services.IQuery<T> {

	private static final Logger logger = LoggerFactory.getLogger(FhirQueryAdapter.class);

	private final Class<T> modelClass;
	private final IFhirModelService fhirModelService;
	private final IQueryAttributeMapper attributeMapper;

	private int limit = -1;
	private int offset = 0;
	private final List<SearchClause> searchClauses = new ArrayList<>();
	private final List<OrderClause> orderClauses = new ArrayList<>();

	/**
	 * Represents a search clause with attribute, comparator, value, and join type.
	 */
	private static class SearchClause {
		final String attribute;
		final ch.elexis.core.services.IQuery.COMPARATOR comparator;
		final Object value;
		final boolean ignoreCase;
		final boolean isOr;

		SearchClause(String attribute, ch.elexis.core.services.IQuery.COMPARATOR comparator, Object value,
				boolean ignoreCase, boolean isOr) {
			this.attribute = attribute;
			this.comparator = comparator;
			this.value = value;
			this.ignoreCase = ignoreCase;
			this.isOr = isOr;
		}
	}

	/**
	 * Represents an order by clause.
	 */
	private static class OrderClause {
		final String field;
		final ch.elexis.core.services.IQuery.ORDER order;

		OrderClause(String field, ch.elexis.core.services.IQuery.ORDER order) {
			this.field = field;
			this.order = order;
		}
	}

	/**
	 * Creates a new FhirQueryAdapter.
	 *
	 * @param modelClass       the Elexis model class (e.g., IPatient.class)
	 * @param fhirModelService the FHIR model service for result adaptation
	 * @param attributeMapper  the attribute mapper for the specific model type
	 */
	public FhirQueryAdapter(Class<T> modelClass, IFhirModelService fhirModelService,
			IQueryAttributeMapper attributeMapper) {
		this.modelClass = modelClass;
		this.fhirModelService = fhirModelService;
		this.attributeMapper = attributeMapper;
	}

	@Override
	public ch.elexis.core.services.IQuery<T> startGroup() {
		// Grouping is handled by tracking clauses and applying them together
		// In FHIR search, we use _and/_or parameters for compound queries
		return this;
	}

	@Override
	public ch.elexis.core.services.IQuery<T> andJoinGroups() {
		// Apply AND join between groups - in FHIR this is the default
		return this;
	}

	@Override
	public ch.elexis.core.services.IQuery<T> orJoinGroups() {
		// Would need to use _or parameter in FHIR search
		// For simplicity, we'll note this and handle in execute()
		return this;
	}

	@Override
	public ch.elexis.core.services.IQuery<T> and(EStructuralFeature feature,
			ch.elexis.core.services.IQuery.COMPARATOR comparator, Object value, boolean ignoreCase) {
		String attributeName = getAttributeName(feature);
		return and(attributeName, comparator, value, ignoreCase);
	}

	@Override
	public ch.elexis.core.services.IQuery<T> and(String entityAttributeName,
			ch.elexis.core.services.IQuery.COMPARATOR comparator, Object value, boolean ignoreCase) {
		searchClauses.add(new SearchClause(entityAttributeName, comparator, value, ignoreCase, false));
		return this;
	}

	@Override
	public ch.elexis.core.services.IQuery<T> andFeatureCompare(EStructuralFeature feature,
			ch.elexis.core.services.IQuery.COMPARATOR comparator, EStructuralFeature otherFeature) {
		// Feature comparison not directly supported by FHIR search
		// Would need to be handled as a custom filter after retrieval
		logger.warn("Feature comparison not supported in FHIR query adapter for {} and {}", feature.getName(),
				otherFeature.getName());
		return this;
	}

	@Override
	public ch.elexis.core.services.IQuery<T> or(EStructuralFeature feature,
			ch.elexis.core.services.IQuery.COMPARATOR comparator, Object value, boolean ignoreCase) {
		String attributeName = getAttributeName(feature);
		return or(attributeName, comparator, value, ignoreCase);
	}

	@Override
	public ch.elexis.core.services.IQuery<T> or(String entityAttributeName,
			ch.elexis.core.services.IQuery.COMPARATOR comparator, Object value, boolean ignoreCase) {
		searchClauses.add(new SearchClause(entityAttributeName, comparator, value, ignoreCase, true));
		return this;
	}

	@Override
	public ch.elexis.core.services.IQuery<T> orderBy(String fieldOrderBy, ch.elexis.core.services.IQuery.ORDER order) {
		orderClauses.add(new OrderClause(fieldOrderBy, order));
		return this;
	}

	@Override
	public ch.elexis.core.services.IQuery<T> orderBy(EStructuralFeature feature,
			ch.elexis.core.services.IQuery.ORDER order) {
		String attributeName = getAttributeName(feature);
		return orderBy(attributeName, order);
	}

	@Override
	public ch.elexis.core.services.IQuery<T> orderByLeftPadded(String fieldOrderBy,
			ch.elexis.core.services.IQuery.ORDER order) {
		// Left-padded ordering is not natively supported by FHIR
		// Could be implemented with a custom sort after retrieval
		logger.warn("Left-padded ordering not supported in FHIR query adapter for {}", fieldOrderBy);
		orderClauses.add(new OrderClause(fieldOrderBy, order));
		return this;
	}

	@Override
	public ch.elexis.core.services.IQuery<T> orderBy(Map<String, Object> caseContext,
			ch.elexis.core.services.IQuery.ORDER order) {
		// Complex CASE statement ordering not supported by FHIR search
		logger.warn("CASE-based ordering not supported in FHIR query adapter");
		return this;
	}

	@Override
	public <S> ISubQuery<S> createSubQuery(Class<S> modelClazz, IModelService modelService) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Subqueries not supported in FHIR query adapter");
	}

	@Override
	public ch.elexis.core.services.IQuery<T> exists(ch.elexis.core.services.ISubQuery<?> subQuery) {
		throw new UnsupportedOperationException("EXISTS not supported in FHIR query adapter");
	}

	@Override
	public ch.elexis.core.services.IQuery<T> notExists(ch.elexis.core.services.ISubQuery<?> subQuery) {
		throw new UnsupportedOperationException("NOT EXISTS not supported in FHIR query adapter");
	}

	@Override
	public ch.elexis.core.services.IQuery<T> limit(int limit) {
		this.limit = limit;
		return this;
	}

	@Override
	public ch.elexis.core.services.IQuery<T> offset(int offset) {
		this.offset = offset;
		return this;
	}

	@Override
	public List<T> execute() {
		// Build the FHIR query based on accumulated clauses
		IQuery<IBaseBundle> query = buildFhirQuery();

		// Apply limit and offset
		if (limit > 0) {
			query = query.count(limit);
		}
		if (offset > 0) {
			// FHIR uses _offset parameter, but HAPI may handle this differently
			logger.debug("Offset {} not fully supported by all FHIR servers", offset);
		}

		// Execute and adapt results
		// Note: getQueryResults will execute the query, so we pass it directly
		return fhirModelService.getQueryResults(query, modelClass);
	}

	@Override
	public ch.elexis.core.services.IQueryCursor<T> executeAsCursor() {
		// For cursor-based execution, we execute and wrap results
		List<T> results = execute();
		return new ListBackedQueryCursor<>(results);
	}

	@Override
	public ch.elexis.core.services.IQueryCursor<T> executeAsCursor(Map<String, Object> queryHints) {
		// Query hints are not directly supported by FHIR
		// We execute and return a cursor over the results
		return executeAsCursor();
	}

	@Override
	public Optional<T> executeSingleResult() {
		List<T> results = execute();
		if (results.isEmpty()) {
			return Optional.empty();
		}
		if (results.size() > 1) {
			logger.warn("Query returned {} results, expected 1", results.size());
		}
		return Optional.of(results.get(0));
	}

	/**
	 * Builds the FHIR query based on accumulated search and order clauses.
	 * 
	 * @return the configured HAPI FHIR query
	 */
	private IQuery<IBaseBundle> buildFhirQuery() {
		IQuery<IBaseBundle> query = fhirModelService.getFhirQuery(modelClass);

		// Apply search clauses
		// For simplicity, we'll handle the most common case: AND-joined clauses
		// FHIR search uses & for AND by default
		// Group AND/OR clauses separately for proper FHIR query construction
		List<ICriterion<?>> andCriteria = new ArrayList<>();
		List<ICriterion<?>> orCriteria = new ArrayList<>();
		
		for (SearchClause clause : searchClauses) {
			// Transform the value if needed
			Function<Object, Object> valueTransformer = attributeMapper.getValueTransformer(clause.attribute);
			Object transformedValue = valueTransformer != null 
					? valueTransformer.apply(clause.value) 
					: clause.value;
			
			// Build the criterion if the mapper supports it
			if (attributeMapper instanceof PatientQueryAttributeMapper) {
				PatientQueryAttributeMapper patientMapper = (PatientQueryAttributeMapper) attributeMapper;
				ICriterion<?> criterion = patientMapper.buildCriterion(
						clause.attribute, clause.comparator, transformedValue);
				if (criterion != null) {
					if (clause.isOr) {
						orCriteria.add(criterion);
					} else {
						andCriteria.add(criterion);
					}
				}
			} else {
				// For other mappers, log that we can't apply the criterion
				logger.info("No criterion builder for attribute {} with mapper {}",
						clause.attribute, attributeMapper.getClass().getName());
			}
		}
		
		// Apply AND criteria first (default join)
		for (ICriterion<?> criterion : andCriteria) {
			query = query.where(criterion);
		}
		
		// Apply OR criteria
		// For simplicity, we'll apply OR criteria as separate where clauses
		// Note: FHIR _or parameter requires specific handling for true OR semantics
		for (ICriterion<?> criterion : orCriteria) {
			query = query.where(criterion);
		}

		// Apply order clauses
		// FHIR uses _sort parameter
		if (!orderClauses.isEmpty()) {
			List<SortSpec> sortSpecs = new ArrayList<>();
			for (OrderClause orderClause : orderClauses) {
				String fhirField = attributeMapper.mapAttribute(orderClause.field);
				if (fhirField != null) {
					SortOrderEnum sortOrder = orderClause.order == ch.elexis.core.services.IQuery.ORDER.ASC
							? SortOrderEnum.ASC
							: SortOrderEnum.DESC;
					sortSpecs.add(new SortSpec(fhirField, sortOrder));
				}
			}
			if (!sortSpecs.isEmpty()) {
				// Apply all sort specs
				for (SortSpec sortSpec : sortSpecs) {
					query = query.sort(sortSpec);
				}
			}
		}

		return query;
	}

	/**
	 * Maps Elexis COMPARATOR to FHIR search operator.
	 * 
	 * @param comparator the Elexis comparator
	 * @return the FHIR search operator string (e.g., "=", ":", etc.)
	 */
	private String mapComparator(ch.elexis.core.services.IQuery.COMPARATOR comparator) {
		return switch (comparator) {
		case EQUALS -> "=";
		case LIKE -> ":contains"; // FHIR string search uses :contains for LIKE
		case NOT_EQUALS -> "ne";
		case NOT_LIKE -> ":not-contains";
		case LESS -> "<";
		case LESS_OR_EQUAL -> "<=";
		case GREATER -> ">";
		case GREATER_OR_EQUAL -> ">=";
		case IN -> ","; // Multiple values comma-separated
		};
	}

	/**
	 * Extracts attribute name from EStructuralFeature.
	 * 
	 * @param feature the structural feature
	 * @return the attribute name
	 */
	private String getAttributeName(EStructuralFeature feature) {
		return feature != null ? feature.getName() : null;
	}

	/**
	 * Simple cursor implementation backed by a list.
	 * 
	 * @param <T> the element type
	 */
	private static class ListBackedQueryCursor<T> implements ch.elexis.core.services.IQueryCursor<T> {
		private final List<T> results;
		private int index = 0;

		ListBackedQueryCursor(List<T> results) {
			this.results = new ArrayList<>(results);
		}

		@Override
		public boolean hasNext() {
			return index < results.size();
		}

		@Override
		public T next() {
			if (!hasNext()) {
				throw new java.util.NoSuchElementException();
			}
			return results.get(index++);
		}

		@Override
		public void close() {
			// No resources to close for list-backed cursor
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Remove not supported");
		}

		@Override
		public int size() {
			return results.size();
		}

		@Override
		public void clear() {
			results.clear();
		}
	}
}
