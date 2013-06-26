/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.perspectives;

import org.eclipse.swt.SWT;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import ch.elexis.core.ui.views.BBSView;

/**
 * Schwarzes Brett. Jeder angemeldete Anwender kann Themen eröffnen und/oder Anwprten zu schon
 * erschienenen Texten schreiben
 * 
 * @author gerry
 * 
 */
public class BBSPerspective implements IPerspectiveFactory {
	public static final String ID = "ch.elexis.SchwarzesBrett"; //$NON-NLS-1$
	
	public void createInitialLayout(IPageLayout layout){
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);
		layout.addView(BBSView.ID, SWT.RIGHT, 0.9f, editorArea);
		
	}
	
}
