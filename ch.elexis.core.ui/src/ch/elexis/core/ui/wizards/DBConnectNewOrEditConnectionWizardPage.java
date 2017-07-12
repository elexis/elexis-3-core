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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.common.DBConnection.DBType;
import ch.elexis.data.PersistentObject;

public class DBConnectNewOrEditConnectionWizardPage extends DBConnectWizardPage {
	private Text txtDBName;
	private Text txtDBHost;
	private Text txtDBPort;
	private Text txtDBUsername;
	private Text txtDBPassword;
	private ComboViewer comboViewerDBType;
	
	/**
	 * @wbp.parser.constructor
	 */
	public DBConnectNewOrEditConnectionWizardPage(){
		super(Messages.DBConnectFirstPage_Connection);
		
		setMessage(Messages.DBConnectNewOrEditConnectionWizardPage_this_message);
		setTitle(Messages.DBConnectNewOrEditConnectionWizardPage_this_title);
	}
	
	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent){
		Composite container = new Composite(parent, SWT.NULL);
		
		setControl(container);
		container.setLayout(new GridLayout(1, false));
		
		Group group = new Group(container, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		group.setText(Messages.DBConnectFirstPage_connectioNDetails);
		group.setLayout(new GridLayout(4, false));
		
		Label lblTyp = new Label(group, SWT.NONE);
		lblTyp.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTyp.setText("Typ");
		
		comboViewerDBType = new ComboViewer(group, SWT.READ_ONLY);
		comboViewerDBType.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event){
				DBType selection =
					(DBType) ((StructuredSelection) comboViewerDBType.getSelection())
						.getFirstElement();
				txtDBPort.setText(selection.defaultPort);
				if(selection.equals(DBType.H2)) {
					String h2Username = "sa"; //$NON-NLS-1$
					String h2DBName = "elexis"; //$NON-NLS-1$
					getDBConnectWizard().getTargetedConnection().username = h2Username; 
					getDBConnectWizard().getTargetedConnection().databaseName = h2DBName;
					txtDBUsername.setText(h2Username);
					txtDBName.setText(h2DBName);
				}
				
				getDBConnectWizard().getTargetedConnection().rdbmsType = selection;
				getDBConnectWizard().getTargetedConnection().port = selection.defaultPort;
			}
		});
		Combo comboDBType = comboViewerDBType.getCombo();
		comboDBType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboViewerDBType.setContentProvider(ArrayContentProvider.getInstance());
		comboViewerDBType.setInput(DBConnection.DBType.values());
		
		Label lblDBName = new Label(group, SWT.NONE);
		lblDBName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDBName.setText(Messages.DBImportFirstPage_databaseName);
		
		txtDBName = new Text(group, SWT.BORDER);
		txtDBName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtDBName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e){
				getDBConnectWizard().getTargetedConnection().databaseName = txtDBName.getText();
			}
		});
		
		Label lblHost = new Label(group, SWT.NONE);
		lblHost.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblHost.setText(Messages.DBImportFirstPage_serverAddress);
		
		txtDBHost = new Text(group, SWT.BORDER);
		txtDBHost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtDBHost.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e){
				getDBConnectWizard().getTargetedConnection().hostName = txtDBHost.getText();
			}
		});
		
		Label lblPort = new Label(group, SWT.NONE);
		lblPort.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPort.setText("Port");
		
		txtDBPort = new Text(group, SWT.BORDER);
		txtDBPort.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtDBPort.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e){
				getDBConnectWizard().getTargetedConnection().port = txtDBPort.getText();
			}
		});
		
		Label lblDBUsername = new Label(group, SWT.NONE);
		lblDBUsername.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDBUsername.setText(Messages.DBConnectSecondPage_databaseUsername);
		
		txtDBUsername = new Text(group, SWT.BORDER);
		txtDBUsername.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtDBUsername.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e){
				getDBConnectWizard().getTargetedConnection().username = txtDBUsername.getText();
			}
		});
		
		Label lblDBPassword = new Label(group, SWT.NONE);
		lblDBPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDBPassword.setText(Messages.DBConnectSecondPage_databasePassword);
		
		txtDBPassword = new Text(group, SWT.PASSWORD);
		txtDBPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtDBPassword.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e){
				getDBConnectWizard().getTargetedConnection().password = txtDBPassword.getText();
			}
		});
		new Label(group, SWT.NONE);
		new Label(group, SWT.NONE);
		new Label(group, SWT.NONE);
		new Label(group, SWT.NONE);
		//		
		//		new Label(group, SWT.NONE);
		//		new Label(group, SWT.NONE);
		//		new Label(group, SWT.NONE);
		//		new Label(group, SWT.NONE);
		
		//		Button btnCreateDB = new Button(group, SWT.CHECK);
		//		btnCreateDB.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		//		btnCreateDB.setText("Datenbank erstellen");
		//		new Label(group, SWT.NONE);
		
		//		Group grpRootCredentials = new Group(group, SWT.NONE);
		//		grpRootCredentials.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		//		grpRootCredentials.setText("Administrator / Root Credentials");
		//		grpRootCredentials.setLayout(new GridLayout(2, false));
		//		
		//		Label lblRootUsername = new Label(grpRootCredentials, SWT.NONE);
		//		lblRootUsername.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		//		lblRootUsername.setText("Username");
		//		
		//		txtRootUsername = new Text(grpRootCredentials, SWT.BORDER);
		//		txtRootUsername.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		//		
		//		Label lblRootPassword = new Label(grpRootCredentials, SWT.NONE);
		//		lblRootPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		//		lblRootPassword.setText("Passwort");
		//		
		//		txtRootPassword = new Text(grpRootCredentials, SWT.BORDER);
		//		txtRootPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		tdbg = new TestDBConnectionGroup(container, SWT.NONE, getDBConnectWizard());
		tdbg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	}
	
	private DBConnectWizard getDBConnectWizard(){
		return (DBConnectWizard) getWizard();
	}
	
	@Override
	public void setVisible(boolean visible){
		super.setVisible(visible);
		if (visible) {
			fillConnectionDetailFields(getDBConnectWizard().getTargetedConnection());
		}
	}
	
	private void fillConnectionDetailFields(DBConnection dbConnection){
		if (dbConnection.rdbmsType != null) {
			comboViewerDBType.setSelection(new StructuredSelection(dbConnection.rdbmsType));
		} else {
			comboViewerDBType.setSelection(new StructuredSelection(DBType.MySQL));
		}
		txtDBHost.setText(PersistentObject.checkNull(dbConnection.hostName));
		txtDBName.setText(PersistentObject.checkNull(dbConnection.databaseName));
		txtDBUsername.setText(PersistentObject.checkNull(dbConnection.username));
		if (dbConnection.port != null)
			txtDBPort.setText(dbConnection.port);
		txtDBPassword.setText(PersistentObject.checkNull(dbConnection.password));
	}
}
