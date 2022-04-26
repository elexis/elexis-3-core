package ch.elexis.core.internal;

import ch.elexis.core.services.IQueryCursor;

public class EmptyCursor<T> implements IQueryCursor<T> {

	@Override
	public void close() {
	}

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
}
