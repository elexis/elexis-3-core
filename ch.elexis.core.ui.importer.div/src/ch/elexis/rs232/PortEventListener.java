/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/
package ch.elexis.rs232;

import gnu.io.SerialPortEventListener;

public interface PortEventListener extends SerialPortEventListener {
	
	public static final String XON = "\013"; //$NON-NLS-1$
	public final static String XOFF = "\015"; //$NON-NLS-1$
	public final static String STX = "\002"; //$NON-NLS-1$
	public final static String ETX = "\003"; //$NON-NLS-1$
	public static final String NAK = "\025"; //$NON-NLS-1$
	public static final String CR = "\015"; //$NON-NLS-1$
	public static final String LF = "\012"; //$NON-NLS-1$
}
