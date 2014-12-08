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

// 17.5.2009: added H2

import java.util.Hashtable;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;

public class DBConnectFirstPage extends WizardPage {
	
	Combo dbTypes;
	Text server, dbName;
	String defaultUser, defaultPassword;
	JdbcLink j = null;
	
	static final String[] supportedDB = new String[] {
		"mySQl", "PostgreSQL", "H2" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
	};
	
	public DBConnectFirstPage(String pageName){
		super(Messages.DBConnectFirstPage_Connection, Messages.DBConnectFirstPage_typeOfDB,
			Images.IMG_LOGO.getImageDescriptor(ImageSize._75x66_TitleDialogIconSize)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		setMessage(Messages.DBConnectFirstPage_selectType); //$NON-NLS-1$
		setDescription(Messages.DBConnectFirstPage_theDescripotion); //$NON-NLS-1$
		
	}
	
	public DBConnectFirstPage(String pageName, String title, ImageDescriptor titleImage){
		super(pageName, title, titleImage);
		// TODO Automatisch erstellter Konstruktoren-Stub
	}
	
	public void createControl(Composite parent){
		FormToolkit tk = UiDesk.getToolkit();
		Form form = tk.createForm(parent);
		form.setText(Messages.DBConnectFirstPage_connectioNDetails); //$NON-NLS-1$
		Composite body = form.getBody();
		body.setLayout(new TableWrapLayout());
		FormText alt = tk.createFormText(body, false);
		StringBuilder old = new StringBuilder();
		old.append("<form>"); //$NON-NLS-1$
		String driver = "";
		String user = "";
		String typ = "";
		String connectString = "";
		Hashtable<Object, Object> hConn = null;
		String cnt = CoreHub.localCfg.get(Preferences.CFG_FOLDED_CONNECTION, null);
		if (cnt != null) {
			hConn = PersistentObject.fold(StringTool.dePrintable(cnt));
			if (hConn != null) {
				driver =
					PersistentObject.checkNull(hConn.get(Preferences.CFG_FOLDED_CONNECTION_DRIVER));
				connectString =
					PersistentObject.checkNull(hConn
						.get(Preferences.CFG_FOLDED_CONNECTION_CONNECTSTRING));
				user =
					PersistentObject.checkNull(hConn.get(Preferences.CFG_FOLDED_CONNECTION_USER));
				typ = PersistentObject.checkNull(hConn.get(Preferences.CFG_FOLDED_CONNECTION_TYPE));
			}
		}
		// Check whether we were overridden
		String dbUser = System.getProperty(ElexisSystemPropertyConstants.CONN_DB_USERNAME);
		String dbPw = System.getProperty(ElexisSystemPropertyConstants.CONN_DB_PASSWORD);
		String dbFlavor = System.getProperty(ElexisSystemPropertyConstants.CONN_DB_FLAVOR);
		String dbSpec = System.getProperty(ElexisSystemPropertyConstants.CONN_DB_SPEC);
		if (dbUser != null && dbPw != null && dbFlavor != null && dbSpec != null) {
			old.append("<br/><li><b>Aktuelle Verbindung wurde via Übergabeparameter ans Programm gesetzt!</b></li><br/>"); //$NON-NLS-1$
		}
		if (ch.rgw.tools.StringTool.isNothing(connectString)) {
			old.append("<form>:<br/>"); //$NON-NLS-1$
			old.append("Keine konfigurierte Verbindung."); //$NON-NLS-1$
		} else {
			old.append("Konfigurierte Verbindung ist:<br/>"); //$NON-NLS-1$
			old.append("<li><b>Typ:</b>       ").append(typ).append("</li>"); //$NON-NLS-1$ //$NON-NLS-2$
			old.append("<li><b>Treiber</b>    ").append(driver).append("</li>"); //$NON-NLS-1$ //$NON-NLS-2$
			old.append("<li><b>Verbinde</b>   ").append(connectString).append("</li>"); //$NON-NLS-1$ //$NON-NLS-2$
			old.append("<li><b>Username</b>   ").append(user).append("</li>"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (PersistentObject.getConnection() != null
			&& PersistentObject.getConnection().getConnectString() != null) {
			old.append("<li><b>Effektiv</b> verwendet wird:").append( //$NON-NLS-1$
				PersistentObject.getConnection().getConnectString()).append("</li>"); //$NON-NLS-1$
		}
		old.append("</form>"); //$NON-NLS-1$
		alt.setText(old.toString(), true, false);
		// Composite form=new Composite(parent, SWT.BORDER);
		Label sep = tk.createSeparator(body, SWT.NONE);
		TableWrapData twd = new TableWrapData();
		twd.heightHint = 5;
		sep.setLayoutData(twd);
		tk.createLabel(body, Messages.DBConnectFirstPage_enterType); //$NON-NLS-1$
		dbTypes = new Combo(body, SWT.BORDER | SWT.SIMPLE);
		dbTypes.setItems(supportedDB);
		dbTypes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				int it = dbTypes.getSelectionIndex();
				switch (it) {
				case 0:
				case 1:
					server.setEnabled(true);
					dbName.setEnabled(true);
					defaultUser = "elexis"; //$NON-NLS-1$
					defaultPassword = "elexisTest"; //$NON-NLS-1$
					break;
				case 2:
					server.setEnabled(false);
					dbName.setEnabled(true);
					defaultUser = "sa"; //$NON-NLS-1$
					defaultPassword = ""; //$NON-NLS-1$
					break;
				default:
					break;
				}
				DBConnectSecondPage sec = (DBConnectSecondPage) getNextPage();
				sec.name.setText(defaultUser);
				sec.pwd.setText(defaultPassword);
				
			}
			
		});
		tk.adapt(dbTypes, true, true);
		tk.createLabel(body, Messages.DBConnectFirstPage_serevrAddress); //$NON-NLS-1$
		server = tk.createText(body, "", SWT.BORDER); //$NON-NLS-1$
		TableWrapData twr = new TableWrapData(TableWrapData.FILL_GRAB);
		server.setLayoutData(twr);
		tk.createLabel(body, Messages.DBConnectFirstPage_databaseName); //$NON-NLS-1$
		dbName = tk.createText(body, "", SWT.BORDER); //$NON-NLS-1$
		TableWrapData twr2 = new TableWrapData(TableWrapData.FILL_GRAB);
		dbName.setLayoutData(twr2);
		setControl(form);
	}
	
}
