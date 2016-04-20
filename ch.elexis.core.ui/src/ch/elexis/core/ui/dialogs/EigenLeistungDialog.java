/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.codesystems.Messages;
import ch.elexis.data.Eigenleistung;
import ch.rgw.tools.TimeTool;

public class EigenLeistungDialog extends TitleAreaDialog {
	Text tName, tKurz, tEK, tVK, tTime;
	// Eigenleistung result;
	private IVerrechenbar result;
	
	public EigenLeistungDialog(final Shell shell, final IVerrechenbar lstg){
		super(shell);
		result = lstg;
	}
	
	@Override
	public void create(){
		super.create();
		if (result instanceof Eigenleistung) {
			setTitle(Messages.BlockDetailDisplay_editServiceCaption); //$NON-NLS-1$
			setMessage(Messages.BlockDetailDisplay_editServiceBody); //$NON-NLS-1$
		} else if (result == null) {
			setTitle(Messages.BlockDetailDisplay_defineServiceCaption); //$NON-NLS-1$
			setMessage(Messages.BlockDetailDisplay_defineServiceBody); //$NON-NLS-1$
		}
		getShell().setText(Messages.BlockDetailDisplay_SerlfDefinedService); //$NON-NLS-1$
	}
	
	@Override
	protected Control createDialogArea(final Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(2, false));
		new Label(ret, SWT.NONE).setText(Messages.BlockDetailDisplay_shortname); //$NON-NLS-1$
		tKurz = new Text(ret, SWT.BORDER);
		tKurz.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.BlockDetailDisplay_name); //$NON-NLS-1$
		tName = new Text(ret, SWT.BORDER);
		tName.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.BlockDetailDisplay_costInCents); //$NON-NLS-1$
		tEK = new Text(ret, SWT.BORDER);
		tEK.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.BlockDetailDisplay_priceInCents); //$NON-NLS-1$
		tVK = new Text(ret, SWT.BORDER);
		tVK.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.BlockDetailDisplay_timeInMinutes); //$NON-NLS-1$
		tTime = new Text(ret, SWT.BORDER);
		tTime.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		if (result instanceof Eigenleistung) {
			Eigenleistung el = (Eigenleistung) result;
			tName.setText(el.get(Messages.BlockDetailDisplay_title)); //$NON-NLS-1$
			tKurz.setText(el.get(Messages.BlockDetailDisplay_code)); //$NON-NLS-1$
			tEK.setText(el.getKosten(new TimeTool()).getCentsAsString());
			tVK.setText(el.getPreis(new TimeTool(), null).getCentsAsString());
		}
		return ret;
	}
	
	public IVerrechenbar getResult(){
		return result;
	}
	
	@Override
	protected void okPressed(){
		if (result == null) {
			result =
				new Eigenleistung(tKurz.getText(), tName.getText(), tEK.getText(), tVK.getText());
		} else if (result instanceof Eigenleistung) {
			((Eigenleistung) result).set(new String[] {
				Eigenleistung.CODE, Eigenleistung.BEZEICHNUNG, Eigenleistung.EK_PREIS,
				Eigenleistung.VK_PREIS
			}, new String[] {
				tKurz.getText(), tName.getText(), tEK.getText(), tVK.getText()
			});
		}
		super.okPressed();
	}
	
}
