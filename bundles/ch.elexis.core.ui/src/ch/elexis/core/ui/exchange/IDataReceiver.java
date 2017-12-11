/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.exchange;

import org.jdom.Element;

import ch.rgw.tools.Result;

/**
 * A generic mediator between XML-Sources and Elexis objects
 * 
 * @author Gerry
 * 
 */
public interface IDataReceiver {
	/**
	 * load an Object from an XML stream
	 * 
	 * @param input
	 *            the Element to load
	 * @param context
	 *            the context to put the element
	 * @return the created object on success or an error message
	 */
	public Result<Object> load(Element input, Object context);
	
	/**
	 * end of the input operation. can be used for cleanup. The IDataReceiver becomes invalid after
	 * finalize
	 * 
	 * @return ok on success.
	 */
	public Result finalizeImport();
	
}
