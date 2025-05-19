package ch.elexis.core.ui.reminder.supplier;

import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.gclient.IQuery;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.IUserGroup;

public class FhirGroupSupplier implements Supplier<List<IReminder>> {

	private IUserGroup group;
	private String search;
	private int limit;

	public FhirGroupSupplier(IUserGroup group, String search, int limit) {
		this.group = group;
		this.search = search;
		this.limit = limit;
	}

	@Override
	public List<IReminder> get() {
		long start = System.currentTimeMillis();
		IQuery<IBaseBundle> query = FhirModelServiceHolder.get()
				.getQuery("Task?owner=" + group.getId() + "&status:not=COMPLETED");
		
		query.count(limit);
		List<IReminder> ret = FhirModelServiceHolder.get().getQueryResults(query, IReminder.class);

		if (StringUtils.isNoneBlank(search)) {
			ret = ret.stream().filter(r -> r.getSubject().contains(search)).toList();
		}
		LoggerFactory.getLogger(getClass())
				.info("Supply [" + group + "] took " + (System.currentTimeMillis() - start) + "[ms]");
		return ret;
	}
}
