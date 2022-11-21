/*******************************************************************************
 * Copyright (c) 2005-2022, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    M. Descher - Elexis 3.0 take information from real JDBC connection
 *    			   Elexis 3.10 switch to DataSource based persistence
 *******************************************************************************/

package ch.elexis.core.ui.preferences;

import java.sql.Connection;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.data.DBConnection;
import ch.elexis.data.PersistentObject;
import ch.rgw.io.Settings;

/**
 * Datenbankspezifische Einstellungen. Datenbanktyp, Connect-String, Jdbc-Klasse
 * usw.
 */
public class Datenbank extends PreferencePage implements IWorkbenchPreferencePage {

	Button bKons, bRn, bRepair;
	Label lOutputFile;
	Button bOutputFile, bCheck;
	Settings cfg;

	private static Logger log = LoggerFactory.getLogger(Datenbank.class);

	public Datenbank() {

		noDefaultAndApplyButton();
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.localCfg));
		cfg = CoreHub.localCfg;
		setDescription(Messages.Datenbank_databaseConnectionHeading);
	}

	@Override
	protected Control createContents(Composite parent) {
		final Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout(2, false));

		DBConnection defaultConnection = PersistentObject.getDefaultConnection();

		String dbDriver = defaultConnection.getDBDriver();
		String driver = dbDriver != null ? dbDriver : "unknown";//$NON-NLS-1$

		String user;
		Connection conn = null;
		try {
			conn = defaultConnection.getConnection();
			user = conn.getMetaData().getUserName();
		} catch (SQLException e) {
			user = "ERR: " + e.getMessage(); //$NON-NLS-1$
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				log.error("Error closing connection" + e); //$NON-NLS-1$
			}
		}
		String dbConnectstring = defaultConnection.getRawDBConnectString();
		String connectString = dbConnectstring != null ? dbConnectstring : "unknown";//$NON-NLS-1$

		String flavor = defaultConnection.getDBFlavor();

		new Label(ret, SWT.NONE).setText(Messages.Datenbank_databaseConnection);
		new Text(ret, SWT.READ_ONLY).setText(driver); // $NON-NLS-1$
		new Label(ret, SWT.NONE).setText(Messages.Datenbank_connectString);
		new Text(ret, SWT.READ_ONLY).setText(connectString); // $NON-NLS-1$
		new Label(ret, SWT.NONE).setText(Messages.Datenbank_usernameForDatabase);
		new Text(ret, SWT.READ_ONLY).setText(user); // $NON-NLS-1$
		new Label(ret, SWT.NONE).setText(Messages.Core_Database_Type);
		new Text(ret, SWT.READ_ONLY).setText(flavor); // $NON-NLS-1$

		return ret;
	}

	@Override
	public void init(IWorkbench workbench) {
	}

}