/*******************************************************************************
 * Copyright (c) 2012 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.contacts.controls;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

import ch.elexis.core.data.beans.ContactBean;

public class UserManagementComposite extends AbstractComposite {

	private TabbedPropertySheetWidgetFactory tpsf = null;

	private Group anwenderGroup;
	private GridData gdAnwenderGroup;
	private Group mandantGroup;
	private Label lblUsername, lblPassword;
	private Text txtUSERNAME, txtPASSWORD;
	private Label lblKuerzel;
	private Text txtKUERZEL;

	public UserManagementComposite(Composite parent, int style,
			TabbedPropertySheetPage tpsp) {
		this(parent, style);
		tpsf = tpsp.getWidgetFactory();
		if (tpsf != null) {
			tpsf.adapt(anwenderGroup);
			tpsf.paintBordersFor(anwenderGroup);
			tpsf.adapt(mandantGroup);
			tpsf.paintBordersFor(mandantGroup);
		}
	}

	public UserManagementComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));

		anwenderGroup = new Group(this, SWT.None);
		anwenderGroup.setText("Anwender");
		mandantGroup = new Group(this, SWT.None);
		mandantGroup.setText("Mandant");
		lblUsername = new Label(anwenderGroup, SWT.None);
		lblUsername.setText("Benutzername");
		txtUSERNAME = new Text(anwenderGroup, SWT.BORDER);
		lblPassword = new Label(anwenderGroup, SWT.None);
		lblPassword.setText("Passwort");
		txtPASSWORD = new Text(anwenderGroup, SWT.BORDER);

		anwenderGroup.setLayout(new GridLayout(2, false));
		gdAnwenderGroup = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		anwenderGroup.setLayoutData(gdAnwenderGroup);
		mandantGroup.setLayout(new GridLayout(2, false));
		mandantGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		lblKuerzel = new Label(mandantGroup, SWT.NONE);
		GridData gd_lblKuerzel = new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1);
		gd_lblKuerzel.widthHint = 80;
		lblKuerzel.setLayoutData(gd_lblKuerzel);
		lblKuerzel.setText("Kürzel");

		txtKUERZEL = new Text(mandantGroup, SWT.BORDER);
		txtKUERZEL.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		GridData gd_lblUsername = new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1);
		gd_lblUsername.widthHint = 80;
		lblUsername.setLayoutData(gd_lblUsername);
		txtUSERNAME.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		GridData gd_lblPassword = new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1);
		gd_lblPassword.widthHint = 80;
		lblPassword.setLayoutData(gd_lblPassword);
		txtPASSWORD.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		initDataBindings();
	}

	protected void initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();

		Text[] control = { txtUSERNAME, txtPASSWORD, txtKUERZEL };
		String[] property = { "username", "password", "description3" };

		for (int i = 0; i < control.length; i++) {
			bindValue(control[i], property[i], bindingContext);
		}
	}

	@Override
	public void setContact(ContactBean contact) {
		anwenderGroup.setVisible(contact.isUser());
		gdAnwenderGroup.exclude = !contact.isUser();
		mandantGroup.setVisible(contact.isMandator());
		contactObservable.setValue(contact);
	}

}
