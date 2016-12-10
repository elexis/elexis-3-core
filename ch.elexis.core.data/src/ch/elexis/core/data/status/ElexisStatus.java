/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.data.status;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This class represents a Status of the Elexis Application. It can be logged or shown to the user
 * or even ignored, depending on the logLevel etc. and the StatusHandler implementation.
 * <b> Following defined status levels are taken from <link>ch.elexis.util.Log</link> class. Based
 * on their values the StatusHandler implementation can control the logging. Default value is
 * ERRORS. </b>
 */
public class ElexisStatus extends Status {
	
	public static final int LOG_FATALS = 1;
	public static final int LOG_ERRORS = 2;
	public static final int LOG_WARNINGS = 3;
	public static final int LOG_INFOS = 4;
	public static final int LOG_DEBUGMSG = 5;
	public static final int LOG_TRACE = 6;
	
	// defined values can be combined with or operator
	public static final int CODE_NONE = 0x00;
	public static final int CODE_NOFEEDBACK = 0x01;
	public static final int CODE_RESTART = 0x02;
	
	private int logLevel;
	
	public ElexisStatus(int severity, String pluginId, int code, String message,
		Exception exception){
		super(severity, pluginId, code, message, exception);
		this.logLevel = LOG_ERRORS;
	}
	
	public ElexisStatus(int severity, String pluginId, int code, String message,
		Exception exception, int logLevel){
		super(severity, pluginId, code, message, exception);
		this.logLevel = logLevel;
	}
	
	public ElexisStatus(int severity, String pluginId, int code, String message, int logLevel){
		super(severity, pluginId, code, message, null);
		this.logLevel = logLevel;
	}
	
	public ElexisStatus(IStatus status){
		super(status.getSeverity(), status.getPlugin(), status.getCode(), status.getMessage(),
			status.getException());
	}
	
	public int getLogLevel(){
		return logLevel;
	}
	
	public void setLogLevel(int logLevel){
		this.logLevel = logLevel;
	}
	
	@Override
	public void setCode(int code){
		super.setCode(code);
	}
	
	@Override
	public void setMessage(String message){
		super.setMessage(message);
	}
}
