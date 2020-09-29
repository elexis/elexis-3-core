package ch.elexis.core.jpa.model.adapter.internal;

import java.util.Optional;

import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.queries.Cursor;
import org.eclipse.persistence.queries.ScrollableCursor;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.jpa.model.adapter.AbstractModelAdapterFactory;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IQueryCursor;

public class QueryCursor<T> implements IQueryCursor<T> {
	
	private Cursor cursor;
	private AbstractModelAdapterFactory adapterFactory;
	private Class<?> interfaceClazz;
	
	private boolean adapt;
	
	public QueryCursor(ScrollableCursor cursor, AbstractModelAdapterFactory adapterFactory,
		Class<?> interfaceClazz){
		this.cursor = cursor;
		this.adapterFactory = adapterFactory;
		this.interfaceClazz = interfaceClazz;
		this.adapt = adapterFactory != null && interfaceClazz != null;
	}
	
	@Override
	public void close(){
		try {
			cursor.close();
		} catch (DatabaseException e) {
			LoggerFactory.getLogger(getClass()).error("Error closing cursor", e);
		}
	}
	
	@Override
	public boolean hasNext(){
		return cursor.hasNext();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T next(){
		if (adapt) {
			Optional<Identifiable> adapter =
				adapterFactory.getModelAdapter((EntityWithId) cursor.next(), interfaceClazz, true);
			return (T) adapter.get();
		} else {
			return (T) cursor.next();
		}
	}
	
	@Override
	public int size(){
		return cursor.size();
	}
	
	@Override
	public void clear(){
		cursor.clear();
	}
	
}
