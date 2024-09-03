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
	public IQuery<T> startGroup() {
		return this;
	}

	@Override
	public IQuery<T> andJoinGroups() {
		return this;
	}

	@Override
	public IQuery<T> orJoinGroups() {
		return this;
	}

	@Override
	public IQuery<T> and(EStructuralFeature feature, COMPARATOR comparator, Object value, boolean ignoreCase) {
		return this;
	}

	@Override
	public IQuery<T> andFeatureCompare(EStructuralFeature feature, COMPARATOR comparator,
			EStructuralFeature otherFeature) {
		return this;
	}

	@Override
	public IQuery<T> and(String entityAttributeName, COMPARATOR comparator, Object value, boolean ignoreCase) {
		return this;
	}

	@Override
	public IQuery<T> or(EStructuralFeature feature, COMPARATOR comparator, Object value, boolean ignoreCase) {
		return this;
	}

	@Override
	public IQuery<T> or(String entityAttributeName, COMPARATOR comparator, Object value, boolean ignoreCase) {
		return this;
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
	public IQuery<T> orderBy(String fieldOrderBy, ORDER order) {
		return this;
	}

	@Override
	public IQuery<T> orderByLeftPadded(String fieldOrderBy, ORDER order, String field) {
		return this;
	}

	@Override
	public IQuery<T> orderBy(EStructuralFeature feature, ORDER order) {
		return this;
	}

	@Override
	public IQuery<T> orderBy(Map<String, Object> caseContext, ORDER order) {
		return this;
	}

	@Override
	public <S> ISubQuery<S> createSubQuery(Class<S> modelClazz, IModelService modelService) {
		return new EmptySubQuery<>();
	}

	@Override
	public IQuery<T> exists(ISubQuery<?> subQuery) {
		return this;
	}

	@Override
	public IQuery<T> notExists(ISubQuery<?> subQuery) {
		return this;
	}

	@Override
	public IQuery<T> limit(int limit) {
		return this;
	}

	@Override
	public IQuery<T> offset(int offset) {
		return this;
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
