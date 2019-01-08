/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.l10n.Messages;
public class ImporterHost extends PreferencePage implements IWorkbenchPreferencePage {
	
	public ImporterHost(){
		noDefaultAndApplyButton();
	}
	
	@Override
	protected Control createContents(Composite parent){
		Composite ret = new Composite(parent, SWT.READ_ONLY);
		ret.setLayout(new FillLayout());
		Label text = new Label(ret, SWT.WRAP);
		text.setText(Messages.ImporterHost_Explanation);
		return ret;
	}
	
	public void init(IWorkbench workbench){
		// TODO Automatisch erstellter Methoden-Stub
		
	}
	
}
