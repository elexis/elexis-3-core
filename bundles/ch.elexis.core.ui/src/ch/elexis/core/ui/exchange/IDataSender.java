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

import ch.elexis.core.model.Identifiable;
import ch.elexis.core.ui.exchange.elements.XChangeElement;
import ch.elexis.data.PersistentObject;

/**
 * A generic mediator between Elexis Objects and XML-Files. Any number of
 * Objects can be sent to the IDataSender, finishing with a call to
 * finalizeExport. The ultimate destination depends on the implementation
 *
 * @author Gerry
 *
 */
public interface IDataSender {
	/**
	 * Prepare an object for export
	 *
	 * @param output an object this IDataSender can handle
	 * @return the XML element created
	 * @throws XChangeException if an error occurred
	 */
	public XChangeElement store(Object output) throws XChangeException;

	/**
	 * Send the stored objects to this IDataSender's ultimate destinaion (e.g. file,
	 * URL). The IDataTransfer is invalid after finalizing.
	 *
	 * @throws XChangeException
	 */
	public void finalizeExport() throws XChangeException;

	/**
	 * Ask if this IDataSender can handle a certain type
	 *
	 * @param clazz the class in question
	 * @return true if it can handle objects of that class.
	 */
	public boolean canHandle(Class<? extends PersistentObject> clazz);

	/**
	 * Checks whether this {@link IDataSender} is able to handle the given
	 * {@link Identifiable} object.
	 * <p>
	 * This method is typically used to determine at runtime whether a specific
	 * object instance can be processed by this {@code IDataSender}, regardless of
	 * its concrete class.
	 *
	 * @param identifiable the object to check
	 * @return {@code true} if this {@code IDataSender} can handle the given object;
	 *         {@code false} otherwise
	 */
	public boolean canHandle(Identifiable identifiable);

}
