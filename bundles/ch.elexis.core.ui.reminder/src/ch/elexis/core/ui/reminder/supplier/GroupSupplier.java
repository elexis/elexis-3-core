package ch.elexis.core.ui.reminder.supplier;

import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class GroupSupplier implements Supplier<List<IReminder>> {

	private IUserGroup group;
	private String search;
	private boolean showCompleted;
	private int limit;

	public GroupSupplier(IUserGroup group, String search, boolean showCompleted, int limit) {
		this.group = group;
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

		query.and("userGroup", COMPARATOR.EQUALS, group);

		if (StringUtils.isNotBlank(search)) {
			addSearchToQuery(query);
		}

		query.orderBy(ModelPackage.Literals.IDENTIFIABLE__LASTUPDATE, ORDER.DESC);
		query.orderBy(ModelPackage.Literals.IREMINDER__DUE, ORDER.DESC);
		query.limit(limit);
		List<IReminder> ret = query.execute();
		LoggerFactory.getLogger(getClass())
				.info("Supply [" + group + "] took " + (System.currentTimeMillis() - start) + "[ms]");
		return ret;
	}

	private void addSearchToQuery(IQuery<IReminder> query) {
		String likeSearch = "%" + search + "%";
		query.and(ModelPackage.Literals.IREMINDER__SUBJECT, COMPARATOR.LIKE, likeSearch, true);
	}
}
