package ch.elexis.core.ui.reminder.supplier;

import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.gclient.IQuery;
import ch.elexis.core.model.IReminder;

public class FhirPopupSupplier implements Supplier<List<IReminder>> {

	private String search;
	private int limit;

	public FhirPopupSupplier(String search, int limit) {
		this.search = search;
		this.limit = limit;
	}

	@Override
	public List<IReminder> get() {
		long start = System.currentTimeMillis();
		IQuery<IBaseBundle> query = FhirModelServiceHolder.get()
				.getQuery("Task?code=http://www.elexis.info/task/visibility|popup&status:not=COMPLETED");
		
		query.count(limit);
		List<IReminder> ret = FhirModelServiceHolder.get().getQueryResults(query, IReminder.class);

		if (StringUtils.isNoneBlank(search)) {
			ret = ret.stream().filter(r -> r.getSubject().contains(search)).toList();
		}
		LoggerFactory.getLogger(getClass())
				.info("Supply [ALL] took " + (System.currentTimeMillis() - start) + "[ms]");
		return ret;
	}
}
