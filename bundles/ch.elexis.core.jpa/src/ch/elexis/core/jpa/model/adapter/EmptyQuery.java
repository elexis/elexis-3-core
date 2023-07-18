package ch.elexis.core.jpa.model.adapter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.ecore.EStructuralFeature;

import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQueryCursor;
import ch.elexis.core.services.ISubQuery;

public class EmptyQuery<T> implements IQuery<T> {

	@Override
	public void startGroup() {
	}

	@Override
	public void andJoinGroups() {
	}

	@Override
	public void orJoinGroups() {
	}

	@Override
	public void and(EStructuralFeature feature, COMPARATOR comparator, Object value, boolean ignoreCase) {
	}

	@Override
	public void andFeatureCompare(EStructuralFeature feature, COMPARATOR comparator, EStructuralFeature otherFeature) {
	}

	@Override
	public void and(String entityAttributeName, COMPARATOR comparator, Object value, boolean ignoreCase) {
	}

	@Override
	public void or(EStructuralFeature feature, COMPARATOR comparator, Object value, boolean ignoreCase) {
	}

	@Override
	public void or(String entityAttributeName, COMPARATOR comparator, Object value, boolean ignoreCase) {
	}

	@Override
	public List<T> execute() {
		return Collections.emptyList();
	}

	@Override
	public IQueryCursor<T> executeAsCursor() {
		return new EmptyCursor();
	}

	@Override
	public IQueryCursor<T> executeAsCursor(Map<String, Object> queryHints) {
		return new EmptyCursor();
	}

	@Override
	public Optional<T> executeSingleResult() {
		return Optional.empty();
	}

	@Override
	public void orderBy(String fieldOrderBy, ORDER order) {
	}

	@Override
	public void orderBy(EStructuralFeature feature, ORDER order) {
	}

	@Override
	public void orderBy(Map<String, Object> caseContext, ORDER order) {
	}

	@Override
	public <S> ISubQuery<S> createSubQuery(Class<S> modelClazz, IModelService modelService) {
		return new EmptySubQuery<S>();
	}

	@Override
	public void exists(ISubQuery<?> subQuery) {
	}

	@Override
	public void notExists(ISubQuery<?> subQuery) {
	}

	@Override
	public void limit(int limit) {
	}

	@Override
	public void offset(int offset) {
	}

	private class EmptySubQuery<S> implements ISubQuery<S> {

		@Override
		public Object getQuery() {
			return null;
		}

		@Override
		public void startGroup() {
		}

		@Override
		public void andJoinGroups() {
		}

		@Override
		public void orJoinGroups() {
		}

		@Override
		public void and(EStructuralFeature feature, COMPARATOR comparator, Object value, boolean ignoreCase) {
		}

		@Override
		public void andFeatureCompare(EStructuralFeature feature, COMPARATOR comparator,
				EStructuralFeature otherFeature) {
		}

		@Override
		public void and(String entityAttributeName, COMPARATOR comparator, Object value, boolean ignoreCase) {
		}

		@Override
		public void or(EStructuralFeature feature, COMPARATOR comparator, Object value, boolean ignoreCase) {
		}

		@Override
		public void or(String entityAttributeName, COMPARATOR comparator, Object value, boolean ignoreCase) {
		}

		@Override
		public void andParentCompare(String parentEntityAttributeName, COMPARATOR equals, String entityAttributeName) {
		}
	}
	
	private class EmptyCursor implements IQueryCursor<T> {

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public T next() {
			return null;
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public void clear() {
		}

		@Override
		public void close() {
		}
	}
}
