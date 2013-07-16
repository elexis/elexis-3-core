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

import static ch.elexis.core.logging.Activator.bundleMarker;

import org.eclipse.equinox.log.ExtendedLogEntry;
import ch.elexis.core.logging.LoggingConstants;
import org.osgi.service.log.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class LogServiceUtil {
	
	// logger name is the Classname
	private static final Logger logger = LoggerFactory.getLogger(LogServiceUtil.class); 
	
	
	
	/*
	 * If there's a ServiceReference in the LogEntry, then the Servicereference
	 * will be printed as first part of the Message
	 * 
	 */
	protected static String getMessageWithServiceReference(LogEntry le) {
		
		if (le.getServiceReference() != null) {
			return LoggingConstants.SERVICE_PREFIX
				+le.getServiceReference().toString()
				+LoggingConstants.SERVICE_POSTFIX+le.getMessage();
		}
		return le.getMessage();
	}

	/*
	 * has the LogEntry a Bundle with Symbolic Name ?
	 * 
	 */
	protected static boolean hasBundleName(LogEntry entry) {
		return entry.getBundle() != null && entry.getBundle().getSymbolicName() != null;
	}
	
	/*
	 * has the ExtendedLogEntry a Logger with Name ?
	 * 
	 */
	protected static boolean hasLoggerName(ExtendedLogEntry extendedEntry) {
		return extendedEntry.getLoggerName() != null && extendedEntry.getLoggerName().length() > 0;
	}
	
	/*
	 * gets (or creates) a BundleMarker for a given LogEntry
	 * 
	 */
	protected static Marker getBundleMarker(LogEntry le) {
		Marker marker;
		if (le.getBundle() != null && le.getBundle().getSymbolicName() != null) {
	    	// create a Marker with the bundle symbolic name
			marker =  MarkerFactory.getMarker(le.getBundle().getSymbolicName());
		} else {
			marker = MarkerFactory.getMarker(LoggingConstants.NO_BUNDLE);
		}
		if (!marker.contains(MarkerFactory.getMarker(LoggingConstants.IS_BUNDLE_MARKER))) {
			// mark this SLF4J Marker as a BundleMarker
			marker.add(MarkerFactory.getMarker(LoggingConstants.IS_BUNDLE_MARKER));
		}
		// logging the Marker itself
    	logger.debug(bundleMarker,
    			"created marker: {} | LogEntry: {}" //$NON-NLS-1$
    			,marker.toString()
    			,le.toString()); 
		return marker;
	}	
	
	/*
	 * gets (or creates) a BundleMarker for a given LogEntry
	 * adds a special Marker to annotate this Logentry as a catched Extended LogEntry
	 * 
	 */
	protected static Marker getExtendedBundleMarker(ExtendedLogEntry le) {
		// gets the standard Marker
		Marker xMarker = getBundleMarker(le);
		// add a special Marker to mark it as an Extended Logentry
		xMarker.add(MarkerFactory.getMarker(LoggingConstants.IS_EXTENDED_OSGI_LOG_MARKER));

		// logging the Marker itself
    	logger.debug(bundleMarker,
    			"created extended marker: {} | LogEntry: {}" //$NON-NLS-1$
    			,xMarker.toString()
    			,le.toString()); 
		
		return xMarker;
	}	

}
