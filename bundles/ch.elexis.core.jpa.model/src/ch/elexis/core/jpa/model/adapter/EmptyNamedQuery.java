package ch.elexis.core.jpa.model.adapter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.IQueryCursor;

public class EmptyNamedQuery<T> implements INamedQuery<T> {

	@Override
	public List<T> executeWithParameters(Map<String, Object> parameters) {
		return Collections.emptyList();
	}

	@Override
	public IQueryCursor<T> executeAsCursorWithParameters(Map<String, Object> parameters) {
		return new EmptyCursor();
	}

	@Override
	public Optional<T> executeWithParametersSingleResult(Map<String, Object> parameters) {
		return Optional.empty();
	}

	@Override
	public INamedQuery<T> limit(int limit) {
		return this;
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
