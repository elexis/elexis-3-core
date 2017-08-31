package ch.elexis.core.logging.osgi;

import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, service = {})
public class OsgiSl4fjLoggingAdapter implements LogListener {
	
	private Logger logger = LoggerFactory.getLogger("OSGI");
	
	@Reference
	public void bindLogReaderService(LogReaderService logReaderService){
		logReaderService.addLogListener(this);
	}
	
	public void unbindLogReaderService(LogReaderService logReaderService){
		logReaderService.removeLogListener(this);
	}
	
	@Override
	public void logged(LogEntry entry){
		
		Bundle bundle = entry.getBundle();
		String message =
			(bundle != null) ? "[" + bundle.getSymbolicName() + "] " + entry.getMessage()
					: entry.getMessage();
		Throwable exception = entry.getException();
		
		switch (entry.getLevel()) {
		case LogService.LOG_ERROR:
			logger.error(message, exception);
			break;
		case LogService.LOG_WARNING:
			logger.warn(message, exception);
			break;
		case LogService.LOG_INFO:
			logger.info(message, exception);
			break;
		default:
			logger.debug(message, exception);
			break;
		}
		
	}
	
}
