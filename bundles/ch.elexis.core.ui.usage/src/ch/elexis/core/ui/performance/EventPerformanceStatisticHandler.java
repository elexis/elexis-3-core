package ch.elexis.core.ui.performance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher.IPerformanceStatisticHandler;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.ui.usage.model.EventStatistic;
import ch.elexis.core.ui.usage.model.IStatistic;
import ch.elexis.core.ui.usage.model.ModelFactory;

public class EventPerformanceStatisticHandler implements IPerformanceStatisticHandler {
	
	private Map<String, EventStatistic> statistics;
	
	public EventPerformanceStatisticHandler(){
		statistics = new HashMap<>();
	}
	
	@Override
	public void startCatchEvent(ElexisEvent ee, ElexisEventListener listener){
		EventStatistic stat = getOrCreateStatistics(ee, listener);
		stat.setLastStart(System.currentTimeMillis());
		int count = stat.getValue();
		stat.setValue(++count);
	}
	
	
	@Override
	public void endCatchEvent(ElexisEvent ee, ElexisEventListener listener){
		EventStatistic stat = getStatistics(ee, listener);
		if (stat != null) {
			long durationMs = System.currentTimeMillis() - stat.getLastStart();
			if (durationMs > stat.getMaxDuration()) {
				stat.setMaxDuration((int) durationMs);
			}
			if (durationMs < stat.getMinDuration()) {
				stat.setMinDuration((int) durationMs);
			}
			long lastAverage = stat.getAvgDuration();
			stat.setAvgDuration(lastAverage + ((durationMs - lastAverage) / stat.getValue()));
		} else {
			LoggerFactory.getLogger(getClass())
				.warn("No start stat found for " + getEventKey(ee, listener));
		}
	}
	
	private EventStatistic getStatistics(ElexisEvent ee, ElexisEventListener listener){
		String key = getEventKey(ee, listener);
		return statistics.get(key);
	}
	
	private EventStatistic getOrCreateStatistics(ElexisEvent ee, ElexisEventListener listener){
		String key = getEventKey(ee, listener);
		EventStatistic ret = statistics.get(key);
		if (ret == null) {
			ret = ModelFactory.eINSTANCE.createEventStatistic();
			ret.setAction(key);
			ret.setMinDuration(Integer.MAX_VALUE);
			statistics.put(key, ret);
		}
		return ret;
	}
	
	private String getEventKey(ElexisEvent ee, ElexisEventListener listener){
		return ee.getObjectClass().getName() + "[" + getEventType(ee.getType()) + ", "
			+ getPriority(ee.getPriority()) + "] -> " + listener.getClass();
	}
	
	public Collection<? extends IStatistic> getStatistics(){
		return statistics.values();
	}
	
	private String getEventType(int eventType){
		switch (eventType) {
		case 0x0001:
			return "EVENT_CREATE";
		case 0x0002:
			return "EVENT_DELETE";
		case 0x0004:
			return "EVENT_UPDATE";
		case 0x0008:
			return "EVENT_RELOAD";
		case 0x0010:
			return "EVENT_SELECTED";
		case 0x0020:
			return "EVENT_DESELECTED";
		case 0x0040:
			return "EVENT_USER_CHANGED";
		case 0x0080:
			return "EVENT_MANDATOR_CHANGED";
		case 0x0100:
			return "EVENT_ELEXIS_STATUS";
		case 0x0200:
			return "EVENT_OPERATION_PROGRESS";
		case 0x0400:
			return "EVENT_NOTIFICATION";
		case 0x1000:
			return "EVENT_LOCK_AQUIRED";
		case 0x2000:
			return "EVENT_LOCK_PRERELEASE";
		case 0x4000:
			return "EVENT_LOCK_RELEASED";
		default:
			return "EVENT_UNKNOWN";
		}
	}
	
	private String getPriority(int priority){
		switch (priority) {
		case 1:
			return "PRIORITY_SYNC";
		case 1000:
			return "PRIORITY_HIGH";
		case 10000:
			return "PRIORITY_NORMAL";
		default:
			return "PRIORITY_UNKNOWN";
		}
	}
}
