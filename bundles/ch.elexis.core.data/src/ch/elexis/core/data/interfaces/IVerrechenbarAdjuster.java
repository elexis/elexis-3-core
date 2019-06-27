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

import ch.elexis.data.Konsultation;

/**
 * Implementations of {@link IVerrechenbarAdjuster} can adjust a {@link IVerrechenbar} before it is
 * attempted to be billed.
 * 
 * @author thomas
 * 
 */
public interface IVerrechenbarAdjuster {
	
	/**
	 * Adjust the {@link IVerrechenbar} before it is attempted to be billed. The adjusted
	 * {@link IVerrechenbar}, possibly a different object than verrechenbar, is returned. If no
	 * adjustment is performed, the same object is returned.
	 * 
	 * @param verrechenbar
	 * @return the adjusted {@link IVerrechenbar}
	 */
	public IVerrechenbar adjust(IVerrechenbar verrechenbar, Konsultation kons);
}
