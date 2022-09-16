package ch.elexis.core.ui.jfr;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

@Name("Application_E4_Events")
@Label("E4")
@Category("Application")
@StackTrace(false)
public final class E4Event extends Event {

	@Label("topic")
	String topic;

	public static final ThreadLocal<E4Event> EVENT = new ThreadLocal<>() {
		@Override
		protected E4Event initialValue() {
			return new E4Event();
		}
	};

}
