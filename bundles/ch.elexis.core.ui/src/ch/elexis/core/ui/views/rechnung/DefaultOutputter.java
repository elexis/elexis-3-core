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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
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

		@SuppressWarnings("unchecked")
		Set<String> outputters = new LinkedHashSet<>(
				Optional.ofNullable((List<String>) compParent.getData(RnOutputDialog.RNOUTPUTTER_DESCRIPTION))
						.orElse(Collections.emptyList()));

		Label lbl = new Label(ret, SWT.NONE);
		lbl.setText(Messages.DefaultOutputter_InvoiceOutput + String.join(", ", outputters));
		lbl.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		return ret;
	}

	public Result<Rechnung> doOutput(TYPE type, Collection<Rechnung> rnn, final Properties props) {
		Result<Rechnung> res = new Result<>(null);
		props.setProperty(IRnOutputter.PROP_OUTPUT_METHOD, "asDefault"); //$NON-NLS-1$
		for (Rechnung rn : rnn) {
			Fall fall = rn.getFall();
			final IRnOutputter iro = fall.getOutputter();
			if (iro != null) {
				res.add(iro.doOutput(type, Arrays.asList(new Rechnung[] { rn }), props));
			}
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
