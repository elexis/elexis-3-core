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
package ch.elexis.core.data.interfaces;

import ch.elexis.data.Verrechnet;
import ch.rgw.tools.Money;

/**
 * Implementations of {@link IVerrechnetAdjuster} can adjust a {@link Verrechnet} as it is created.
 * 
 * @author thomas
 * 
 */
public interface IVerrechnetAdjuster {
	/**
	 * Adjust the created {@link Verrechnet}.
	 * 
	 * @param verrechnet
	 *            the Verrechnet object to adjust
	 */
	public void adjust(Verrechnet verrechnet);
	
	/**
	 * Adjust netto price of {@link Verrechnet}.
	 * 
	 * @param verrechnet
	 *            the Verrechnet object this price belongs to
	 * @param price
	 *            the price to adjust
	 */
	public void adjustGetNettoPreis(Verrechnet verrechnet, Money price);
}
