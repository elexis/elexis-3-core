package ch.elexis.core.ui.reminder.supplier;

import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.gclient.IQuery;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IReminder;

public class FhirContactSupplier implements Supplier<List<IReminder>> {

	private IContact contact;
	private String search;
	private boolean showCompleted;
	private int limit;

	public FhirContactSupplier(IContact contact, String search, boolean showCompleted, int limit) {
		this.contact = contact;
		this.search = search;
		this.showCompleted = showCompleted;
		this.limit = limit;
	}

	@Override
	public List<IReminder> get() {
		long start = System.currentTimeMillis();
		IQuery<IBaseBundle> query = FhirModelServiceHolder.get()
				.getQuery("Task?owner=" + contact.getId() + "&status" + (showCompleted ? "" : ":not") + "=COMPLETED");
		
		query.count(limit);
		List<IReminder> ret = FhirModelServiceHolder.get().getQueryResults(query, IReminder.class);

		if (StringUtils.isNoneBlank(search)) {
			ret = ret.stream().filter(r -> r.getSubject() != null && r.getSubject().contains(search)).toList();
		}
		LoggerFactory.getLogger(getClass())
				.info("Supply [" + contact + "] took " + (System.currentTimeMillis() - start) + "[ms]");
		return ret;
	}
}
