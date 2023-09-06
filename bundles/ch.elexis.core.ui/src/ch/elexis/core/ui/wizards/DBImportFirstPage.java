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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;

public class DBImportFirstPage extends WizardPage {

	List dbTypes;
	Text server;
	Text dbName;
	String defaultUser;
	String defaultPassword;
	JdbcLink j = null;

	static final String[] supportedDB = new String[] { "mySQl", "PostgreSQL", "H2", "ODBC" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	};
	static final int MYSQL = 0;
	static final int POSTGRESQL = 1;
	static final int ODBC = 3;
	static final int H2 = 2;

	public DBImportFirstPage(String pageName) {
		super(Messages.Core_Connection, Messages.Core_Database_Type,
				Images.IMG_LOGO.getImageDescriptor(ImageSize._75x66_TitleDialogIconSize));
		setMessage(Messages.DBImportFirstPage_selectType + Messages.DBImportFirstPage_enterNameODBC);
		setDescription(Messages.Core_the_description);
	}

	public DBImportFirstPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		// TODO Automatisch erstellter Konstruktoren-Stub
	}

	@Override
	public void createControl(Composite parent) {
		DBImportWizard wiz = (DBImportWizard) getWizard();

		FormToolkit tk = UiDesk.getToolkit();
		Form form = tk.createForm(parent);
		form.setText(Messages.Core_Database_Connectiondetails); // $NON-NLS-1$
		Composite body = form.getBody();
		body.setLayout(new TableWrapLayout());
		tk.createLabel(body, Messages.Core_Enter_Typename); // $NON-NLS-1$
		dbTypes = new List(body, SWT.BORDER);
		dbTypes.setItems(supportedDB);
		dbTypes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int it = dbTypes.getSelectionIndex();
				switch (it) {
				case MYSQL:
				case POSTGRESQL:
					server.setEnabled(true);
					dbName.setEnabled(true);
					defaultUser = StringUtils.EMPTY;
					defaultPassword = StringUtils.EMPTY;
					break;
				case H2:
					server.setEnabled(false);
					dbName.setEnabled(true);
					defaultUser = "sa"; //$NON-NLS-1$
					defaultPassword = StringUtils.EMPTY;
					break;
				case ODBC:
					server.setEnabled(false);
					dbName.setEnabled(true);
					defaultUser = "sa"; //$NON-NLS-1$
					defaultPassword = StringUtils.EMPTY;
					break;
				default:
					break;
				}
				DBImportSecondPage sec = (DBImportSecondPage) getNextPage();
				sec.name.setText(defaultUser);
				sec.pwd.setText(defaultPassword);

			}

		});

		tk.adapt(dbTypes, true, true);
		tk.createLabel(body, Messages.DBImportFirstPage_serverAddress); // $NON-NLS-1$
		server = tk.createText(body, StringUtils.EMPTY, SWT.BORDER);

		TableWrapData twr = new TableWrapData(TableWrapData.FILL_GRAB);
		server.setLayoutData(twr);
		tk.createLabel(body, Messages.Core_Database_Name); // $NON-NLS-1$
		dbName = tk.createText(body, StringUtils.EMPTY, SWT.BORDER);
		TableWrapData twr2 = new TableWrapData(TableWrapData.FILL_GRAB);
		dbName.setLayoutData(twr2);
		if (wiz.preset != null && wiz.preset.length > 1) {
			int idx = StringTool.getIndex(supportedDB, wiz.preset[0]);
			if (idx < dbTypes.getItemCount()) {
				dbTypes.select(idx);
			}
			server.setText(wiz.preset[1]);
			dbName.setText(wiz.preset[2]);
		}
		setControl(form);
	}
}
