/*******************************************************************************
 * Copyright (c) 2005-2013, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    M. Descher - Elexis 3.0 take information from real JDBC connection    
 *******************************************************************************/

package ch.elexis.core.ui.preferences;

import java.sql.SQLException;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.io.Settings;
import ch.rgw.tools.JdbcLink;

/**
 * Datenbankspezifische Einstellungen. Datenbanktyp, Connect-String, Jdbc-Klasse usw.
 */
public class Datenbank extends PreferencePage implements IWorkbenchPreferencePage {
	
	Button bKons, bRn, bRepair;
	Label lOutputFile;
	Button bOutputFile, bCheck;
	Settings cfg;
	
	public Datenbank(){
		
		noDefaultAndApplyButton();
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.localCfg));
		cfg = CoreHub.localCfg;
		setDescription(Messages.Datenbank_databaseConnectionHeading);
	}
	
	@Override
	protected Control createContents(Composite parent){
		final Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout(2, false));
		
		JdbcLink jdbcl = PersistentObject.getConnection();
		
		String driver = jdbcl.getDriverName();
		String user;
		try {
			user = jdbcl.getConnection().getMetaData().getUserName();
		} catch (SQLException e) {
			user = "ERR: " + e.getMessage();
		}
		String typ = jdbcl.dbDriver();
		String connectstring = jdbcl.getConnectString();
		
		new Label(ret, SWT.NONE).setText(Messages.Datenbank_databaseConnection);
		new Text(ret, SWT.READ_ONLY).setText(driver); //$NON-NLS-1$
		new Label(ret, SWT.NONE).setText(Messages.Datenbank_connectString);
		new Text(ret, SWT.READ_ONLY).setText(connectstring); //$NON-NLS-1$
		new Label(ret, SWT.NONE).setText(Messages.Datenbank_usernameForDatabase);
		new Text(ret, SWT.READ_ONLY).setText(user); //$NON-NLS-1$
		new Label(ret, SWT.NONE).setText(Messages.Datenbank_typeOfDatabase);
		new Text(ret, SWT.READ_ONLY).setText(typ); //$NON-NLS-1$
		
		return ret;
	}
	
	@Override
	public void init(IWorkbench workbench){
		// TODO Auto-generated method stub
		
	}
	
}