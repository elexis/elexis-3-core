package ch.elexis.core.jpa.datasource.internal.jfr;

import java.util.List;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;

public class JFRQueryExecutionListener implements QueryExecutionListener {

	private ThreadLocal<QueryEvent> event;

	@Override
	public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
		event = QueryEvent.EVENT;
		if (event.get().isEnabled()) {
			event.get().sql = queryInfoList.get(0).getQuery() + " " + queryInfoList.get(0).getQueryArgsList();
			event.get().begin();
		}

	}

	@Override
	public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
		if (event.get().isEnabled()) {
			event.get().end();
			event.get().commit();
		}
	}

}
