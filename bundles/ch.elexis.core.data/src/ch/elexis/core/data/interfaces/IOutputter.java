/*******************************************************************************
 * Copyright (c) 2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/
package ch.elexis.core.data.interfaces;

import java.util.Optional;

/**
 * Ach.elexis.exchange to output something
 *
 * @author gerry
 *
 */
public interface IOutputter {
	/** unique ID */
	public String getOutputterID();

	/** human readable description */
	public String getOutputterDescription();

	/**
	 * Get a {@link IOutputter} specific description of the outputted
	 * {@link Object}.
	 * 
	 * @param outputted
	 * @return
	 */
	public default Optional<String> getInfo(Object outputted) {
		return Optional.empty();
	}

	/**
	 * Image to symbolize this outputter (should be 16x16 or 24x24 Pixel)
	 *
	 * @return object castable to org.eclipse.swt.graphics.Image
	 * @since 3.0.0
	 */
	public Object getSymbol();
}
