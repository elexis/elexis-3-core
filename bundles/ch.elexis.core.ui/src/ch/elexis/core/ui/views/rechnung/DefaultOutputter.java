/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/

package ch.elexis.core.ui.views.rechnung;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.data.interfaces.IRnOutputter;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Fall;
import ch.elexis.data.Rechnung;
import ch.rgw.tools.Result;

/**
 * This outputter takes the output target from the case's billing syste,
 *
 * @author Gerry
 *
 */
public class DefaultOutputter implements IRnOutputter {
	private ArrayList<IRnOutputter> configured = new ArrayList<>();

	public boolean canBill(Fall fall) {
		if (fall.getOutputter().getDescription().equals(getDescription())) {
			return false;
		}
		return fall.getOutputter().canBill(fall);
	}

	public boolean canStorno(Rechnung rn) {
		if (rn == null) {
			return false;
		}
		return rn.getFall().getOutputter().canStorno(rn);
	}

	public Object createSettingsControl(Object parent) {
		final Composite compParent = (Composite) parent;
		Composite ret = new Composite(compParent, SWT.NONE);
		ret.setLayout(new GridLayout(2, false));
		Label lbl = new Label(ret, SWT.NONE);
		lbl.setText(Messages.DefaultOutputter_useIdividualPlugins);
		lbl.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		return ret;
	}

	public Result<Rechnung> doOutput(TYPE type, Collection<Rechnung> rnn, final Properties props) {
		Result<Rechnung> res = new Result<>(null);
		props.setProperty(IRnOutputter.PROP_OUTPUT_METHOD, "asDefault"); //$NON-NLS-1$
		for (Rechnung rn : rnn) {
			Fall fall = rn.getFall();
			final IRnOutputter iro = fall.getOutputter();
			if (!configured.contains(iro)) {
				SWTHelper.SimpleDialog dlg = new SWTHelper.SimpleDialog(new SWTHelper.IControlProvider() {
					public Control getControl(Composite parent) {
						parent.getShell().setText(iro.getDescription());
						return (Control) iro.createSettingsControl(parent);

					}

					public void beforeClosing() {
						iro.saveComposite();
					}
				});
				if (dlg.open() == Dialog.OK) {
					configured.add(iro);
				} else {
					continue;
				}
			}

			res.add(iro.doOutput(type, Arrays.asList(new Rechnung[] { rn }), props));
		}
		return null;
	}

	public String getDescription() {
		return Messages.DefaultOutputter_defaultOutputForCase; // $NON-NLS-1$
	}

	public void saveComposite() {
		// Nothing
	}

	@Override
	public void customizeDialog(Object rnOutputDialog) {
		if (rnOutputDialog instanceof RnOutputDialog) {
			((RnOutputDialog) rnOutputDialog).setOkButtonText(Messages.RechnungsListeView_printAction);

		}
	}
}
