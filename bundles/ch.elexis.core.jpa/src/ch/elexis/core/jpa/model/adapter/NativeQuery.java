package ch.elexis.core.jpa.model.adapter;

import java.util.Map;
import java.util.stream.Stream;


import ch.elexis.core.services.INativeQuery;
import jakarta.persistence.Query;

public class NativeQuery implements INativeQuery {

	private final Query query;

	public NativeQuery(Query query) {
		this.query = query;
	}

	@Override
	public Stream<?> executeWithParameters(Map<Integer, Object> parameters) {
		parameters.forEach((k, v) -> {
			query.setParameter(k, v);
		});
		return query.getResultStream();
	}

}
