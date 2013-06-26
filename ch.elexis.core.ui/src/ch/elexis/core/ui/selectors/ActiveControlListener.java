/*******************************************************************************
 * Copyright (c) 2009-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.selectors;

public interface ActiveControlListener {
	/**
	 * Contents of field has chsnged.
	 * 
	 * @param ac
	 *            the field that changed or NULL: Any of the observed field(s) have changed
	 */
	public void contentsChanged(ActiveControl ac);
	
	public void titleClicked(ActiveControl field);
	
	public void invalidContents(ActiveControl field);
}
