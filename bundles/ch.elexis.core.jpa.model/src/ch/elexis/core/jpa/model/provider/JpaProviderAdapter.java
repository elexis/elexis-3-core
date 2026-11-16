package ch.elexis.core.jpa.model.provider;

import java.util.Map;

import ch.elexis.core.jpa.model.adapter.AbstractModelAdapterFactory;
import ch.elexis.core.services.IQueryCursor;
import jakarta.persistence.TypedQuery;

public interface JpaProviderAdapter {

	public IQueryCursor<?> executeAsCursor(AbstractModelAdapterFactory adapterFactory, Class<?> clazz,
			TypedQuery<?> typedQuery, Map<String, Object> queryHints);

}
