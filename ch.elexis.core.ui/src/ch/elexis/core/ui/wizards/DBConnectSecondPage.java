/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ui.util.SWTHelper;

public class DBConnectSecondPage extends WizardPage {
	Text name, pwd;
	
	public DBConnectSecondPage(String pageName){
		super(Messages.DBConnectSecondPage_0); //$NON-NLS-1$
		setTitle(Messages.DBConnectSecondPage_Credentials); //$NON-NLS-1$
		setMessage(Messages.DBConnectSecondPage_username1 + //$NON-NLS-1$
			Messages.DBConnectSecondPage_username2); //$NON-NLS-1$
	}
	
	public DBConnectSecondPage(String pageName, String title, ImageDescriptor titleImage){
		super(pageName, title, titleImage);
		// TODO Automatisch erstellter Konstruktoren-Stub
	}
	
	public void createControl(Composite parent){
		Composite form = new Composite(parent, SWT.NONE);
		form.setLayout(new GridLayout(1, false));
		new Label(form, SWT.NONE).setText(Messages.DBConnectSecondPage_databaseUsername); //$NON-NLS-1$
		name = new Text(form, SWT.BORDER);
		name.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(form, SWT.NONE).setText(Messages.DBConnectSecondPage_databasePassword); //$NON-NLS-1$
		pwd = new Text(form, SWT.BORDER);
		pwd.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		setControl(form);
	}
	
}
