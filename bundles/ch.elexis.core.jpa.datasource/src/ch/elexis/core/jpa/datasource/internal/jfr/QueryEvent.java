package ch.elexis.core.jpa.datasource.internal.jfr;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

@Name("QueryEvent")
@Label("Query")
@Category("DataSource")
@StackTrace(false)
public final class QueryEvent extends Event {

	@Label("sql")
	String sql;

	public static final ThreadLocal<QueryEvent> EVENT = new ThreadLocal<>() {
		@Override
		protected QueryEvent initialValue() {
			return new QueryEvent();
		}
	};

}
