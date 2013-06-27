/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.data.AccountTransaction;
import ch.elexis.core.data.Patient;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.MoneyInput;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.Money;

/**
 * Eine Buchung zu einem Patientenkonto zufügen
 * 
 * @author gerry
 * 
 */
public class AddBuchungDialog extends TitleAreaDialog {
	
	Text betrag, text;
	// int result;
	Patient pat;
	
	public AddBuchungDialog(Shell parentShell, Patient p){
		super(parentShell);
		pat = p;
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		new Label(ret, SWT.NONE).setText(Messages.getString("AddBuchungDialog.amountAs000")); //$NON-NLS-1$
		betrag = new Text(ret, SWT.BORDER);
		betrag.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
		new Label(ret, SWT.NONE).setText(Messages.getString("AddBuchungDialog.textForBooking")); //$NON-NLS-1$
		text = SWTHelper.createText(ret, 4, SWT.NONE);
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		setTitle(Messages.getString("AddBuchungDialog.enterBooking")); //$NON-NLS-1$
		setMessage(Messages.getString("AddBuchungDialog.dontManual")); //$NON-NLS-1$
		setTitleImage(Images.IMG_LOGO.getImage(ImageSize._75x66_TitleDialogIconSize));
		getShell().setText(Messages.getString("AddBuchungDialog.manual")); //$NON-NLS-1$
	}
	
	@Override
	protected void okPressed(){
		try {
			Money mBetrag = MoneyInput.getFromTextField(betrag);
			new AccountTransaction(pat, null, mBetrag, null, text.getText());
		} catch (Exception ex) {
			SWTHelper
				.showError(
					Messages.getString("AddBuchungDialog.ErrorInAmount"), Messages.getString("AddBuchungDialog.CannotInterpretAmount")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		super.okPressed();
	}
	
}
