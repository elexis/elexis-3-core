/*******************************************************************************
 * Copyright (c) 2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.Kontakt;
import ch.elexis.core.data.Mandant;
import ch.elexis.core.data.Person;
import ch.elexis.core.data.admin.AccessControl;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.StringTool;

public class ErsterMandantDialog extends TitleAreaDialog {
	Text tUsername, tPwd1, tPwd2, tTitle, tFirstname, tLastname, tEmail, tStreet, tZip, tPlace,
			tPhone, tFax;
	String[] anreden =
		{
			Messages.ErsterMandantDialog_Herr, Messages.ErsterMandantDialog_Frau, Messages.ErsterMandantDialog_Firma}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	Combo cbAnrede;
	
	public ErsterMandantDialog(Shell parent){
		super(parent);
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite rx = (Composite) super.createDialogArea(parent);
		Composite ret = new Composite(rx, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(2, false));
		new Label(ret, SWT.NONE).setText(Messages.ErsterMandantDialog_Username); //$NON-NLS-1$
		tUsername = new Text(ret, SWT.BORDER);
		tUsername.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.ErsterMandantDialog_Password); //$NON-NLS-1$
		tPwd1 = new Text(ret, SWT.BORDER | SWT.PASSWORD);
		tPwd1.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.ErsterMandantDialog_PasswordRepeat); //$NON-NLS-1$
		tPwd2 = new Text(ret, SWT.BORDER | SWT.PASSWORD);
		tPwd2.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.ErsterMandantDialog_Anrede); //$NON-NLS-1$
		cbAnrede = new Combo(ret, SWT.SIMPLE | SWT.SINGLE);
		cbAnrede.setItems(anreden);
		
		new Label(ret, SWT.NONE).setText(Messages.ErsterMandantDialog_Title); //$NON-NLS-1$
		tTitle = new Text(ret, SWT.BORDER);
		tTitle.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.ErsterMandantDialog_Firstname); //$NON-NLS-1$
		tFirstname = new Text(ret, SWT.BORDER);
		tFirstname.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.ErsterMandantDialog_Lastname); //$NON-NLS-1$
		tLastname = new Text(ret, SWT.BORDER);
		tLastname.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.ErsterMandantDialog_EMail); //$NON-NLS-1$
		tEmail = new Text(ret, SWT.BORDER);
		tEmail.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.ErsterMandantDialog_Street); //$NON-NLS-1$
		tStreet = new Text(ret, SWT.BORDER);
		tStreet.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.ErsterMandantDialog_zip); //$NON-NLS-1$
		tZip = new Text(ret, SWT.BORDER);
		tZip.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.ErsterMandantDialog_place); //$NON-NLS-1$
		tPlace = new Text(ret, SWT.BORDER);
		tPlace.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.ErsterMandantDialog_phone); //$NON-NLS-1$
		tPhone = new Text(ret, SWT.BORDER);
		tPhone.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.ErsterMandantDialog_fax); //$NON-NLS-1$
		tFax = new Text(ret, SWT.BORDER);
		tFax.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		return rx;
	}
	
	@Override
	public void create(){
		super.create();
		getShell().setText(Messages.ErsterMandantDialog_createFirstMandatorCaption); //$NON-NLS-1$
		setMessage(Messages.ErsterMandantDialog_createFirstMandatorMessage); //$NON-NLS-1$
		setTitleImage(Images.IMG_LOGO.getImage(ImageSize._75x66_TitleDialogIconSize));
	}
	
	@Override
	protected void okPressed(){
		String pwd = tPwd1.getText();
		if (!pwd.equals(tPwd2.getText())) {
			SWTHelper
				.showError(
					Messages.ErsterMandantDialog_passwordErrorCaption, Messages.ErsterMandantDialog_passwordErrorBody); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		String email = tEmail.getText();
		if (!StringTool.isMailAddress(email)) {
			SWTHelper
				.showError(
					Messages.ErsterMandantDialog_mailnvalidCaption, Messages.ErsterMandantDialog_mailInvaildBody); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		String username = tUsername.getText();
		if (username.equals("")) { //$NON-NLS-1$
			SWTHelper
				.showError(
					Messages.ErsterMandantDialog_noUsernameCaption, Messages.ErsterMandantDialog_noUsernameBody); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		Mandant m = new Mandant(username, pwd);
		String g = Person.MALE;
		if (cbAnrede.getText().startsWith("F")) { //$NON-NLS-1$
			g = Person.FEMALE;
		}
		m.set(new String[] {
			Person.NAME, Person.FIRSTNAME, Person.TITLE, Person.SEX, Person.FLD_E_MAIL,
			Person.FLD_PHONE1, Person.FLD_FAX, Kontakt.FLD_STREET, Kontakt.FLD_ZIP,
			Kontakt.FLD_PLACE
		}, tLastname.getText(), tFirstname.getText(), tTitle.getText(), g, email, tPhone.getText(),
			tFax.getText(), tStreet.getText(), tZip.getText(), tStreet.getText());
		String gprs = m.getInfoString(AccessControl.KEY_GROUPS); //$NON-NLS-1$
		gprs = StringConstants.ROLE_ADMIN + "," + StringConstants.ROLE_USERS;
		m.setInfoElement(AccessControl.KEY_GROUPS, gprs);
		super.okPressed();
	}
	
}
