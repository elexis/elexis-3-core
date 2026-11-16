package ch.elexis.core.jpa.provider;

import java.util.Map;

import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.queries.ScrollableCursor;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.jpa.model.adapter.AbstractModelAdapterFactory;
import ch.elexis.core.jpa.model.adapter.internal.QueryCursor;
import ch.elexis.core.jpa.model.provider.JpaProviderAdapter;
import ch.elexis.core.services.IQueryCursor;
import jakarta.persistence.TypedQuery;

@Component(immediate = true)
public class EclipselinkJpaProviderAdapter implements JpaProviderAdapter {

	@Override
	public IQueryCursor<?> executeAsCursor(AbstractModelAdapterFactory adapterFactory, Class<?> clazz,
			TypedQuery<?> typedQuery, Map<String, Object> queryHints) {
		if (queryHints == null) {
			typedQuery.setHint(QueryHints.MAINTAIN_CACHE, HintValues.FALSE);
			typedQuery.setHint(QueryHints.SCROLLABLE_CURSOR, HintValues.TRUE);
		} else {
			queryHints.forEach(typedQuery::setHint);
		}
		typedQuery.setHint(QueryHints.SCROLLABLE_CURSOR, HintValues.TRUE);
		ScrollableCursor cursor = (ScrollableCursor) typedQuery.getSingleResult();
		return new QueryCursor<>(cursor, adapterFactory, clazz);
	}

}
