/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/
package ch.elexis.core.ui.views;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;

import ch.elexis.data.PersistentObject;

/**
 * Detailansicht eines PersistentObject
 * 
 * @author Gerry
 * 
 */
public interface IDetailDisplay {
	/**
	 * Detail {@link Composite} following the master detail pattern. Selection is provided via
	 * element class or context name.
	 * 
	 * @param parent
	 *            already has a {@link FillLayout}
	 * @param site
	 *            {@link IViewSite} the display is part of
	 */
	public Composite createDisplay(Composite parent, IViewSite site);
	
	public Class<? extends PersistentObject> getElementClass();
	
	public void display(Object obj);
	
	public String getTitle();
	
}
