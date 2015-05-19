/*******************************************************************************
 * Copyright (c) 2015 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;

public class DBConnectWizardPage extends WizardPage {
	
	protected TestDBConnectionGroup tdbg;
	
	protected DBConnectWizardPage(String pageName){
		super(pageName);
		
		setImageDescriptor(Images.lookupImageDescriptor("db_configure_banner.png",
			ImageSize._75x66_TitleDialogIconSize));
	}

	@Override
	public void createControl(Composite parent){
		// do nothing
	}
	
	public TestDBConnectionGroup getTdbg(){
		return tdbg;
	}
}
