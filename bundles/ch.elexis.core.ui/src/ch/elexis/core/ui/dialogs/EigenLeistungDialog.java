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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.ICustomService;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.Money;

public class EigenLeistungDialog extends TitleAreaDialog {
	Text tTarif, tName, tKurz, tEK, tVK, tTime;
	// Eigenleistung result;
	private ICustomService result;
	private boolean create;

	public EigenLeistungDialog(final Shell shell, final ICustomService lstg) {
		super(shell);
		if (lstg == null) {
			create = true;
			result = CoreModelServiceHolder.get().create(ICustomService.class);
		} else {
			create = false;
			result = lstg;
		}
	}

	@Override
	public void create() {
		super.create();
		if (create) {
			setTitle(Messages.BlockDetailDisplay_defineServiceCaption);
			setMessage(Messages.BlockDetailDisplay_defineServiceBody);
		} else {
			setTitle(Messages.BlockDetailDisplay_editServiceCaption);
			setMessage(Messages.BlockDetailDisplay_editServiceBody);
		}
		getShell().setText(Messages.BlockDetailDisplay_SerlfDefinedService);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setData("TEST_COMP_NAME", "EigenLeistungDialog_ret"); //$NON-NLS-1$ //$NON-NLS-2$
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(2, false));

		new Label(ret, SWT.NONE).setText(Messages.Service_Tarif);
		tTarif = new Text(ret, SWT.BORDER);
		tTarif.setData("TEST_COMP_NAME", "EigenLeistungDialog_tTarif"); //$NON-NLS-1$ //$NON-NLS-2$
		tTarif.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.Core_Short_Label); // $NON-NLS-1$
		tKurz = new Text(ret, SWT.BORDER);
		tKurz.setData("TEST_COMP_NAME", "EigenLeistungDialog_tKurz"); //$NON-NLS-1$ //$NON-NLS-2$
		tKurz.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.Core_Name); // $NON-NLS-1$
		tName = new Text(ret, SWT.BORDER);
		tName.setData("TEST_COMP_NAME", "EigenLeistungDialog_tName"); //$NON-NLS-1$ //$NON-NLS-2$
		tName.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.BlockDetailDisplay_costInCents); // $NON-NLS-1$
		tEK = new Text(ret, SWT.BORDER);
		tEK.setData("TEST_COMP_NAME", "EigenLeistungDialog_tEK"); //$NON-NLS-1$ //$NON-NLS-2$
		tEK.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.BlockDetailDisplay_priceInCents); // $NON-NLS-1$
		tVK = new Text(ret, SWT.BORDER);
		tVK.setData("TEST_COMP_NAME", "EigenLeistungDialog_tVK"); //$NON-NLS-1$ //$NON-NLS-2$
		tVK.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.BlockDetailDisplay_timeInMinutes); // $NON-NLS-1$
		tTime = new Text(ret, SWT.BORDER);
		tTime.setData("TEST_COMP_NAME", "EigenLeistungDialog_tTime"); //$NON-NLS-1$ //$NON-NLS-2$
		tTime.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));

		if (!create) {
			tTarif.setText(result.getCodeSystemCode());
			tName.setText(result.getText());
			tKurz.setText(result.getCode());
			tEK.setText(result.getNetPrice().getCentsAsString());
			tVK.setText(result.getPrice().getCentsAsString());
			tTime.setText(Integer.toString(result.getMinutes()));
		}
		return ret;
	}

	public ICustomService getResult() {
		return result;
	}

	@Override
	protected void okPressed() {
		if (StringUtils.isBlank(tName.getText()) && StringUtils.isBlank(tKurz.getText())) {
			setMessage(Messages.Inputfield_empty, IMessageProvider.ERROR);
			return;
		}
		Money moneyNet = new Money();
		if (StringUtils.isNotBlank(tEK.getText())) {
			try {
				int cents = Integer.parseInt(tEK.getText());
				moneyNet = new Money(cents);
			} catch (Exception e) {
				// ignore
			}
		}
		Money moneyPrice = new Money();
		if (StringUtils.isNotBlank(tVK.getText())) {
			try {
				int cents = Integer.parseInt(tVK.getText());
				moneyPrice = new Money(cents);
			} catch (Exception e) {
				// ignore
			}
		}
		int time = 0;
		if (StringUtils.isNotBlank(tTime.getText())) {
			try {
				time = Integer.parseInt(tTime.getText());
			} catch (Exception e) {
				// ignore
			}
		}
		result.setCodeSystemCode(tTarif.getText());
		result.setCode(tKurz.getText());
		result.setText(tName.getText());
		result.setNetPrice(moneyNet);
		result.setPrice(moneyPrice);
		result.setMinutes(time);
		CoreModelServiceHolder.get().save(result);
		super.okPressed();
	}
}
