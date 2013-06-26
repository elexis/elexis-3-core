/*******************************************************************************
 * Copyright (c) 2010, St. Schenk and Medshare GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    St. Schenk - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class Services extends PreferencePage implements IWorkbenchPreferencePage {
	
	public Services(){
		noDefaultAndApplyButton();
	}
	
	@Override
	protected Control createContents(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new FillLayout());
		StyledText text = new StyledText(ret, SWT.NONE);
		text.setWordWrap(true);
		text.setText(Messages.Services_ExplanationLine1 + Messages.Services_ExplanationLine2
			+ Messages.Services_ExplanationLine3);
		return ret;
	}
	
	public void init(IWorkbench workbench){
		// TODO Automatisch erstellter Methoden-Stub
		
	}
	
}
