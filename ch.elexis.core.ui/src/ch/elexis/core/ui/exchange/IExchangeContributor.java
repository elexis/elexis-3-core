/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
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

import org.eclipse.core.runtime.IExecutableExtension;

import ch.elexis.core.ui.exchange.elements.MedicalElement;
import ch.elexis.data.PersistentObject;

/**
 * A Class that wants to contribute data to eXChange or that can load data from eXChange must
 * implement this interface
 * 
 * @author gerry
 * 
 */
public interface IExchangeContributor extends IExecutableExtension {
	public static final String ExtensionPointName = "ch.elexis.xCHangeContribution";
	
	/**
	 * An Element is to be exported. The method can contribute its own data
	 */
	public void exportHook(MedicalElement me);
	
	/**
	 * An Element ist to be imported. The method can fetch data it can handle
	 * 
	 * @param container
	 *            the source container
	 */
	public void importHook(XChangeContainer container, PersistentObject context);
	
	/**
	 * Perform any needed initialization before the first call
	 * 
	 * @param me
	 *            : The Medical into/from wich the transfer will happen
	 * @param bExport
	 *            : true: export is about to begin. False: import is about to begin
	 * @return false: skip me. True: ok, go on
	 */
	public boolean init(MedicalElement me, boolean bExport);
	
}
