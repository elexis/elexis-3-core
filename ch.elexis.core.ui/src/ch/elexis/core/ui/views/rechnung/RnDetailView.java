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
package ch.elexis.core.ui.views.rechnung;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.ui.util.SWTHelper;

public class RnDetailView extends ViewPart {
	public final static String ID = "ch.elexis.RechnungsDetailView"; //$NON-NLS-1$
	RechnungsBlatt blatt;
	
	@Override
	public void createPartControl(Composite parent){
		parent.setLayout(new GridLayout());
		blatt = new RechnungsBlatt(parent, getViewSite());
		blatt.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		
	}
	
	@Override
	public void setFocus(){
		blatt.setFocus();
	}
	
}
