/*******************************************************************************
 * Copyright (c) 2006 - 2010 ekke (ekkehard gentz) rosenheim germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * License: EPL (Eclipse Public License)
 * http://ekkes-corner.org, http://gentz-software.de
 * mailto: ekke[at]ekkes-corner.org
 * twitter: [at]ekkescorner
 * 
 * Contributors:
 *    ekke (ekkehard gentz) - initial API and implementation 
 * 
 ***********************************************copyright 2006 - 2010**********/
package ch.elexis.core.logging;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.equinox.log.ExtendedLogEntry;
import org.eclipse.equinox.log.ExtendedLogReaderService;
import ch.elexis.core.logging.LoggingConstants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public class LogReaderListenerComponent {
	
	private static final Logger logger = LoggerFactory.getLogger(LogReaderListenerComponent.class);
	
	private Map<LogReaderService, NLogListener> logReaderMap =
		Collections.synchronizedMap(new HashMap<LogReaderService, NLogListener>());

	private Map<ExtendedLogReaderService, XLogListener> extendedLogReaderMap =
		Collections.synchronizedMap(new HashMap<ExtendedLogReaderService, XLogListener>());
	
	public void addLogReaderService(LogReaderService aLogReaderService) {
		logger.debug(Activator.bundleMarker,
    			"addLogReaderServicee [{}]" //$NON-NLS-1$
    			,aLogReaderService.toString());	
		NLogListener listener = new NLogListener();
		logReaderMap.put(aLogReaderService, listener);
		aLogReaderService.addLogListener(listener);
	}

	public void removeLogReaderService(LogReaderService aLogReaderService) {
		logger.debug(Activator.bundleMarker,
    			"removeLogReaderService [{}]" //$NON-NLS-1$
    			,aLogReaderService.toString());
		NLogListener listener = logReaderMap.get(aLogReaderService);
		if (listener != null) {
			aLogReaderService.removeLogListener(listener);
		}
		logReaderMap.remove(aLogReaderService);
	}
	
	public void addExtendedLogReaderService(ExtendedLogReaderService aLogReaderService) {
		logger.debug(Activator.bundleMarker,
    			"addExtendedLogReaderService [{}]" //$NON-NLS-1$
    			,aLogReaderService.toString());	
		XLogListener listener = new XLogListener();
		extendedLogReaderMap.put(aLogReaderService, listener);
		aLogReaderService.addLogListener(listener);
	}

	public void removeExtendedLogReaderService(ExtendedLogReaderService aLogReaderService) {
		logger.debug(Activator.bundleMarker,
    			"removeExtendedLogReaderService [{}]" //$NON-NLS-1$
    			,aLogReaderService.toString());
		XLogListener listener = extendedLogReaderMap.get(aLogReaderService);
		if (listener != null) {
			aLogReaderService.removeLogListener(listener);
		}
		extendedLogReaderMap.remove(aLogReaderService);
	}

	/**
	 * Activate.
	 * 
	 * @param context
	 *            the context
	 */
	public void activate(ComponentContext context) {
		logger.debug(Activator.bundleMarker,
    			"activate [{}]" //$NON-NLS-1$
    			,context.toString());
	}

	/**
	 * Deactivate.
	 * 
	 * @param context
	 *            the context
	 */
	public void deactivate(ComponentContext context) {
		logger.debug(Activator.bundleMarker,
    			"deactivate [{}]" //$NON-NLS-1$
    			,context.toString());
		if (logReaderMap.size() > 0) {
			logger.info(Activator.bundleMarker,
	    			"logReaderMap not empty: contains [{}] entries - they should be removed before" //$NON-NLS-1$
	    			,logReaderMap.size());
			Set<LogReaderService> services = logReaderMap.keySet();  // Needn't be in synchronized block
		    synchronized(logReaderMap) {  // Synchronizing on m, not s!
		      Iterator<LogReaderService> i = services.iterator(); // Must be in synchronized block
		      while (i.hasNext())
		    	  removeLogReaderService(i.next());
		    }
		}
		if (extendedLogReaderMap.size() > 0) {
			logger.info(Activator.bundleMarker,
	    			"extendedLogReaderMap not empty: contains [{}] entries - they should be removed before" //$NON-NLS-1$
	    			,extendedLogReaderMap.size());
			Set<ExtendedLogReaderService> services = extendedLogReaderMap.keySet();  // Needn't be in synchronized block
		    synchronized(extendedLogReaderMap) {  // Synchronizing on m, not s!
		      Iterator<ExtendedLogReaderService> i = services.iterator(); // Must be in synchronized block
		      while (i.hasNext())
		    	  removeExtendedLogReaderService(i.next());
		    }
		}
	}
	

    /**
     * gets the extended LogEntry and logs with prefix "X." to slf4j (LOGBack)
     * also creates a marker with bundle name and (optional) service name
     * 
     * @author ekke
     *
     */
    private class XLogListener implements LogListener {

		public void logged(LogEntry entry) {
			
			// the LogEntry is an ExtendedLogEntry
			ExtendedLogEntry extendedEntry = (ExtendedLogEntry) entry;
			
			// logging the logging-activity itself
			if (logger.isDebugEnabled()) {
				if (extendedEntry.getServiceReference() != null) {
					Object[] logParamArray = {extendedEntry.getLoggerName(), 
							extendedEntry.getBundle().getSymbolicName(),
							extendedEntry.getMessage(), 
							extendedEntry.getTime(),
							extendedEntry.getServiceReference().toString()
							};
					logger.debug(Activator.bundleMarker,
			    			"logged eXtended Listener: got Loggername[{}] from Bundle [{}] with message [{}] time [{}] - ServiceReference [{}]" //$NON-NLS-1$
			    			,logParamArray);
				} else {
					Object[] logParamArray = {extendedEntry.getLoggerName(), 
							extendedEntry.getBundle().getSymbolicName(),
							extendedEntry.getMessage(), 
							extendedEntry.getTime()
							};
					logger.debug(Activator.bundleMarker,
			    			"logged eXtended Listener: got Loggername[{}] from Bundle [{}] with message [{}] time [{}]" //$NON-NLS-1$
			    			,logParamArray);
				}
			}
			
	    	
			// the ExtendedLogEntry can include an own Logger - we log it again
	    	// with the same Logger 
			// if no Loggername exists we test if a bundle exists - if yes we
			// use the Bundle Symbolic Name as Logger name
			// if nothing exists we use the _X_ logger
			Logger xLogger;
			if (LogServiceUtil.hasLoggerName(extendedEntry)) {
				xLogger = LoggerFactory.getLogger(extendedEntry.getLoggerName());
			} else if (LogServiceUtil.hasBundleName(extendedEntry)) {
				xLogger = LoggerFactory.getLogger(entry.getBundle().getSymbolicName());
			} else {
				xLogger = LoggerFactory.getLogger(LoggingConstants.IS_EXTENDED_OSGI_LOG_MARKER);
			}
			
			
			// logging the catched ExtendedLogEntry to our log impl (LOGBack)
			if (extendedEntry.getServiceReference() != null) {
				logLogEntry(xLogger,
						extendedEntry.getLevel(), 
						LogServiceUtil.getExtendedBundleMarker(extendedEntry), 
						LogServiceUtil.getMessageWithServiceReference(extendedEntry), 
						extendedEntry.getException());
			} else {
				logLogEntry(xLogger,
						extendedEntry.getLevel(), 
						LogServiceUtil.getExtendedBundleMarker(extendedEntry), 
						extendedEntry.getMessage(), 
						extendedEntry.getException());
			}
			
		}
    	
    }
    
    // catch the LogEntry and log it to our LOGBack Log Impl
    // also creates a marker with bundle name and (optional) service name
    private class NLogListener implements LogListener {
    	
    	public void logged(LogEntry entry) {
	    	
	    	// logging the logging-activity itself
			if (logger.isDebugEnabled()) {
				if (entry.getServiceReference() != null) {
					Object[] logParamArray = { 
							entry.getBundle().getSymbolicName(),
							entry.getMessage(), 
							entry.getTime(),
							entry.getServiceReference().toString()
							};
					logger.debug(Activator.bundleMarker,
			    			"logged from Bundle [{}] with message [{}] time [{}] - ServiceReference [{}]" //$NON-NLS-1$
			    			,logParamArray);
				} else {
					Object[] logParamArray = { 
							entry.getBundle().getSymbolicName(),
							entry.getMessage(), 
							entry.getTime()
							};
					logger.debug(Activator.bundleMarker,
			    			"logged from Bundle [{}] with message [{}] time [{}]" //$NON-NLS-1$
			    			,logParamArray);
				}
			}
	    	
	    	
	    	// logging the catched LogEntry to our log impl (LOGBack)
			// using the Bundle Symbolic Name as logger if exists
			// otherwise using IS_OSGI_LOG_MARKER as loggername
			Logger nLogger;
			if (LogServiceUtil.hasBundleName(entry)) {
				nLogger = LoggerFactory.getLogger(entry.getBundle().getSymbolicName());
			} else {
				nLogger = LoggerFactory.getLogger(LoggingConstants.IS_OSGI_LOG_MARKER);
			}
			
			
			if (entry.getServiceReference() != null) {
				logLogEntry(nLogger,
						entry.getLevel(), 
						LogServiceUtil.getBundleMarker(entry), 
						LogServiceUtil.getMessageWithServiceReference(entry), 
						entry.getException());
			} else {
				logLogEntry(nLogger,
						entry.getLevel(), 
						LogServiceUtil.getBundleMarker(entry), 
						entry.getMessage(), 
						entry.getException());
			}
			

    	}
    	
    }
    /**
     * 
     * @param logger A <code>Logger</code> object.
     * @param level	The Level from <code>LogService</code>
     * @param marker	A <code>Marker</code> object to mark the entry with Bundle- or Service names
     * @param message	A <code>String</code> object containing the message
     * @param e	A <code>Throwable</code> with the exception (can be null)
     */
    private void logLogEntry (Logger logger, int level, Marker marker, String message, Throwable e) {
    	// log it dependent from Log Level
    	if (e == null) {
    		switch (level) {
    		case LogService.LOG_DEBUG:
    			logger.debug(marker, message);
    			break;
    		case LogService.LOG_ERROR:
    			logger.error(marker, message);
    			break;
    		case LogService.LOG_WARNING:
    			logger.warn(marker, message);
    			break;				
    		default:
    			logger.info(marker, message);
    			break;
    		}
		} else {
			switch (level) {
			case LogService.LOG_DEBUG:
				logger.debug(marker, message, e);
				break;
			case LogService.LOG_ERROR:
				logger.error(marker, message, e);
				break;
			case LogService.LOG_WARNING:
				logger.warn(marker, message, e);
				break;				
			default:
				logger.info(marker, message, e);
				break;
			}
		}
    }
 

}
