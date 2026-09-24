package ch.elexis.core.ui.util.viewers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ICommonViewerContentProvider;

public abstract class CommonViewerContentProvider implements ICommonViewerContentProvider {

	protected CommonViewer commonViewer;

	protected Map<String, String> fieldFilterValues;

	protected String fieldOrderBy;
	protected ORDER fieldOrder = ORDER.DESC;
	protected String[] orderFields;

	protected List<QueryFilter> queryFilters = new ArrayList<>();

	protected boolean ignoreLimit;

	public CommonViewerContentProvider(CommonViewer commonViewer) {
		this.commonViewer = commonViewer;
	}

	@Override
	public void changed(HashMap<String, String> values) {
		if (commonViewer.getConfigurer().getControlFieldProvider().isEmpty()) {
			commonViewer.notify(CommonViewer.Message.empty);
		} else {
			commonViewer.notify(CommonViewer.Message.notempty);
		}
		fieldFilterValues = values;
		commonViewer.notify(CommonViewer.Message.update);
	}

	@Override
	public void reorder(String field) {
		if (fieldOrderBy != null && fieldOrderBy.equals(field)) {
			fieldOrder = fieldOrder == ORDER.DESC ? ORDER.ASC : ORDER.DESC;
		} else {
			fieldOrder = ORDER.DESC;
		}
		fieldOrderBy = field;
		commonViewer.notify(CommonViewer.Message.update);
	}

	public void setOrderFields(String... name) {
		orderFields = name;
	}

	@Override
	public void selected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void startListening() {
		commonViewer.getConfigurer().controlFieldProvider.addChangeListener(this);
	}

	@Override
	public void stopListening() {
		commonViewer.getConfigurer().controlFieldProvider.removeChangeListener(this);
	}

	/**
	 * Get a copy of the currently set {@link QueryFilter}s.
	 *
	 * @return
	 */
	public List<QueryFilter> getQueryFilters() {
		synchronized (queryFilters) {
			return new ArrayList<>(queryFilters);
		}
	}

	/**
	 * Add a {@link QueryFilter} to the currently set {@link QueryFilter}s. Does not
	 * add if the {@link QueryFilter} is already present (List#contains).
	 *
	 * @param queryFilter
	 */
	public void addQueryFilter(QueryFilter queryFilter) {
		synchronized (queryFilters) {
			if (!queryFilters.contains(queryFilter)) {
				queryFilters.add(queryFilter);
			}
		}
	}

	/**
	 * Remove a {@link QueryFilter} from the currently set {@link QueryFilter}s.
	 * Does not remove if the {@link QueryFilter} is already present
	 * (List#contains).
	 *
	 * @param queryFilter
	 */
	public void removeQueryFilter(QueryFilter queryFilter) {
		synchronized (queryFilters) {
			if (queryFilters.contains(queryFilter)) {
				queryFilters.remove(queryFilter);
			}
		}
	}

	/**
	 * Remove all instances of clazz from the currently set {@link QueryFilter}s.
	 *
	 * @param clazz
	 */
	public void removeAllQueryFilterByType(Class<?> clazz) {
		synchronized (queryFilters) {
			for (QueryFilter queryFilter : new ArrayList<>(queryFilters)) {
				if (clazz.isInstance(queryFilter)) {
					queryFilters.remove(queryFilter);
				}
			}
		}
	}

	/**
	 * Test if a {@link QueryFilter} of type clazz is available in the currently set
	 * {@link QueryFilter}s.
	 *
	 * @param clazz
	 * @return
	 */
	public boolean isQueryFilterByType(Class<?> clazz) {
		synchronized (queryFilters) {
			for (QueryFilter queryFilter : new ArrayList<>(queryFilters)) {
				if (clazz.isInstance(queryFilter)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get a {@link QueryFilter} instance of type clazz from the currently set
	 * {@link QueryFilter}s.
	 *
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> Optional<T> getQueryFilterByType(Class<T> clazz) {
		synchronized (queryFilters) {
			for (QueryFilter queryFilter : new ArrayList<>(queryFilters)) {
				if (clazz.isInstance(queryFilter)) {
					return Optional.of((T) queryFilter);
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Get the base {@link IQuery} for the content of the provider. On each call a
	 * new {@link IQuery} should be returned, as the {@link QueryFilter}s will be
	 * applied to the returned query.
	 *
	 * @return
	 */
	protected abstract IQuery<?> getBaseQuery();

	/**
	 * Apply all available {@link QueryFilter}s to the query.
	 */
	protected void applyQueryFilters(IQuery<?> query) {
		if (query != null) {
			synchronized (queryFilters) {
				for (QueryFilter fp : queryFilters) {
					fp.apply(query);
				}
			}
		}
	}

	public interface QueryFilter {
		public void apply(IQuery<?> query);
	}

	protected void setIgnoreLimit(boolean value) {
		this.ignoreLimit = value;
	}

	public void resetLimit() {
		setIgnoreLimit(false);
	}
}
