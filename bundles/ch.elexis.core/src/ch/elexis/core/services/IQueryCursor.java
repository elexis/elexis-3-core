package ch.elexis.core.services;

import java.io.Closeable;
import java.util.Iterator;

import ch.elexis.core.internal.EmptyCursor;

public interface IQueryCursor<T> extends Iterator<T>, Closeable {

	@Override
	public boolean hasNext();

	@Override
	public T next();
	
	public int size();
	
	public void clear();
	
	public static <T> IQueryCursor<T> empty(){
		return new EmptyCursor<>();
	}
	
	public void close();
}
