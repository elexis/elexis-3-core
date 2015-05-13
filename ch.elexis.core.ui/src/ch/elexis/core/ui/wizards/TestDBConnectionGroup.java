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

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.data.util.DBConnection;
import ch.elexis.core.ui.UiDesk;
import ch.rgw.tools.JdbcLink;

public class TestDBConnectionGroup extends Group {
	
	private DBConnectWizard connectionWizard;
	private Label lblTestResult;
	
	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public TestDBConnectionGroup(Composite parent, int style, final DBConnectWizard connectionWizard){
		super(parent, style);
		setText("Verbindung testen");
		setLayout(new GridLayout(2, false));
		
		Button btnNewButton = new Button(this, SWT.FLAT);
		btnNewButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		btnNewButton.setText("Test");
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				testDatabaseConnection();
			}
		});
		
		final Button btnRestartAfterSwitch = new Button(this, SWT.FLAT | SWT.CHECK);
		btnRestartAfterSwitch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				connectionWizard.setRestartAfterConnectionChange(btnRestartAfterSwitch.getSelection());
			}
		});
		btnRestartAfterSwitch.setSelection(connectionWizard.getRestartAfterConnectionChange());
		btnRestartAfterSwitch.setText("Elexis nach Verbindungswechsel neu starten (Empfohlen)");
		new Label(this, SWT.NONE);
		
		lblTestResult = new Label(this, SWT.BORDER | SWT.WRAP);
		lblTestResult.setText("Testen Sie die Verbindung um diese übernehmen zu können, das Testergebnis wird hier angezeigt.");
		GridData gd_lblTestResult = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		gd_lblTestResult.minimumHeight = 60;
		lblTestResult.setLayoutData(gd_lblTestResult);
		
		this.connectionWizard = connectionWizard;
	}
	
	@Override
	protected void checkSubclass(){
		// Disable the check that prevents subclassing of SWT components
	}
	
	private void testDatabaseConnection(){
		boolean error = true;
		
		DBConnection tdbc = connectionWizard.getTargetedConnection();
		JdbcLink j = null;
		String text = null;
		
		try {			
			String hostname = (tdbc.port != null) ? tdbc.hostName + ":" + tdbc.port : tdbc.hostName;
			
			if(tdbc.databaseName==null || tdbc.databaseName.isEmpty()) {
				throw new IllegalArgumentException("No database name provided.");
			}
			
			switch (tdbc.rdbmsType) {
			case H2:
				j = JdbcLink.createH2Link(tdbc.databaseName);
				break;
			case MySQL:
				j = JdbcLink.createMySqlLink(hostname, tdbc.databaseName);
				break;
			case PostgreSQL:
				j = JdbcLink.createPostgreSQLLink(hostname, tdbc.databaseName);
				break;
			default:
				j = null;
				break;
			}
			
			Assert.isNotNull(j);
			
			j.connect(tdbc.username, tdbc.password);
			
			text = "Verbindung hergestellt";
			error = false;
		} catch (Exception e) {
			e.printStackTrace();
			text = "Exception " + e.getMessage();
		}
		
		lblTestResult.setText(text);
		
		if(!error) {
			lblTestResult.setForeground(UiDesk.getColor(UiDesk.COL_DARKGREEN));
			connectionWizard.storeJDBCConnections();
			connectionWizard.setCanFinish(true);
			
			tdbc.connectionString = j.getConnectString();
		} else {
			lblTestResult.setForeground(UiDesk.getColor(UiDesk.COL_RED));
		}
	}
}
