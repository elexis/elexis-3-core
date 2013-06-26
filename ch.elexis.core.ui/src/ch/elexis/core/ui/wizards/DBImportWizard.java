/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    D. Lutz    - adapted for importing data from other databases
 *    
 *******************************************************************************/

package ch.elexis.core.ui.wizards;

import org.eclipse.jface.wizard.Wizard;

public class DBImportWizard extends Wizard {
	private String type = null;
	private String server = null;
	private String db = null;
	private String user = null;
	private String pwd = null;
	String[] preset = null;
	
	DBImportFirstPage first = new DBImportFirstPage(Messages.getString("DBImportWizard.typeOfDB")); //$NON-NLS-1$
	DBImportSecondPage sec = new DBImportSecondPage("Credentials"); //$NON-NLS-1$
	
	public DBImportWizard(){
		super();
		setWindowTitle(Messages.getString("DBImportWizard.connectDB")); //$NON-NLS-1$
	}
	
	public DBImportWizard(String[] preset){
		this();
		this.preset = preset;
	}
	
	@Override
	public void addPages(){
		addPage(first);
		addPage(sec);
	}
	
	@Override
	public boolean performFinish(){
		int ti = first.dbTypes.getSelectionIndex();
		server = first.server.getText();
		db = first.dbName.getText();
		user = sec.name.getText();
		pwd = sec.pwd.getText();
		switch (ti) {
		case DBImportFirstPage.MYSQL:
			type = "MySQL"; //$NON-NLS-1$
			break;
		case DBImportFirstPage.POSTGRESQL:
			type = "PostgreSQL"; //$NON-NLS-1$
			break;
		case DBImportFirstPage.H2:
			type = "H2";
			break;
		case DBImportFirstPage.ODBC:
			type = "ODBC"; //$NON-NLS-1$
			break;
		default:
			type = null;
			return false;
		}
		return true;
	}
	
	public String getType(){
		return type;
	}
	
	public String getServer(){
		return server;
	}
	
	public String getDb(){
		return db;
	}
	
	public String getUser(){
		return user;
	}
	
	public String getPassword(){
		return pwd;
	}
}
