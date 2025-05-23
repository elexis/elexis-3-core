package ch.elexis.core.ui.reminder.supplier;

import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.IReminderResponsibleLink;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.ISubQuery;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class ContactSupplier implements Supplier<List<IReminder>> {

	private IContact contact;
	private String search;
	private boolean showCompleted;
	private int limit;

	public ContactSupplier(IContact contact, String search, boolean showCompleted, int limit) {
		this.contact = contact;
		this.search = search;
		this.showCompleted = showCompleted;
		this.limit = limit;
	}

	@Override
	public List<IReminder> get() {
		long start = System.currentTimeMillis();
		IQuery<IReminder> query = CoreModelServiceHolder.get().getQuery(IReminder.class);
		query.and(ModelPackage.Literals.IREMINDER__STATUS, showCompleted ? COMPARATOR.EQUALS : COMPARATOR.NOT_EQUALS,
				ProcessStatus.CLOSED);

		ISubQuery<IReminderResponsibleLink> subQuery = query.createSubQuery(IReminderResponsibleLink.class,
				CoreModelServiceHolder.get());
		subQuery.andParentCompare("id", COMPARATOR.EQUALS, "reminderid");
		subQuery.and("responsible", COMPARATOR.EQUALS, contact);
		query.exists(subQuery);

		if (StringUtils.isNotBlank(search)) {
			addSearchToQuery(query);
		}

		query.orderBy(ModelPackage.Literals.IREMINDER__DUE, ORDER.DESC);
		query.limit(limit);
		List<IReminder> ret = query.execute();
		LoggerFactory.getLogger(getClass())
				.info("Supply [" + contact + "] took " + (System.currentTimeMillis() - start) + "[ms]");
		return ret;
	}

	private void addSearchToQuery(IQuery<IReminder> query) {
		String likeSearch = "%" + search + "%";
		query.and(ModelPackage.Literals.IREMINDER__SUBJECT, COMPARATOR.LIKE, likeSearch, true);
	}
}
