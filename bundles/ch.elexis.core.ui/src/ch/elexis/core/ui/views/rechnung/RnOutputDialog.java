/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.constants.ExtensionPointConstantsData;
import ch.elexis.core.data.interfaces.IRnOutputter;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.services.LocalConfigService;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Rechnung;

public class RnOutputDialog extends TitleAreaDialog {
	private final Collection<Rechnung> rnn;
	private List<IRnOutputter> lo;
	private Combo cbLo;
	private Button bCopy;
	private final List<Control> ctls = new ArrayList<>();
	private final StackLayout stack = new StackLayout();
	private Composite ret;

	private static final String STANDARD_OUTPUTTER_CLASS = "ch.elexis.core.ui.views.rechnung.DefaultOutputter";
	private static final String RECHNUNG_AUSDRUCKEN_OUTPUTTER_CLASS = "ch.elexis.pdfBills.QrRnOutputter";

	public RnOutputDialog(Shell shell, Collection<Rechnung> rnn) {
		super(shell);
		this.rnn = rnn;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent) {
		lo = Extensions.getClasses(ExtensionPointConstantsData.RECHNUNGS_MANAGER, "outputter"); //$NON-NLS-1$ //$NON-NLS-2$
		if (lo.isEmpty()) {
			String msg = "Elexis has no textplugin configured for outputting bills!"; //$NON-NLS-1$
			SWTHelper.alert(msg, msg);
			return null;
		}
		sortOutputters(lo);
		ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.horizontalIndent = 5;
		cbLo = new Combo(ret, SWT.SINGLE | SWT.READ_ONLY);
		cbLo.setLayoutData(gridData);
		bCopy = new Button(ret, SWT.CHECK);
		bCopy.setText(Messages.RnOutputDialog_markAsCopy);

		bCopy.setLayoutData(gridData);
		final Composite bottom = new Composite(ret, SWT.NONE);
		bottom.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		bottom.setLayout(stack);
		for (IRnOutputter ro : lo) {
			cbLo.add(ro.getDescription());
			Control control = (Control) ro.createSettingsControl(bottom);
			GridData controlGridData = new GridData();
			controlGridData.horizontalIndent = 50;
			control.setLayoutData(controlGridData);
			ctls.add(control);
		}
		cbLo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int idx = cbLo.getSelectionIndex();
				if (idx != -1) {
					setOkButtonEnabled(true);
					customizeDialog(lo.get(idx));
					stack.topControl = ctls.get(idx);
					bottom.layout();
					LocalConfigService.set(Preferences.RNN_DEFAULTEXPORTMODE, idx);
				}
			}
		});
		int defaultIndex = 0;
		cbLo.select(defaultIndex);
		LocalConfigService.set(Preferences.RNN_DEFAULTEXPORTMODE, defaultIndex);
		stack.topControl = ctls.get(cbLo.getSelectionIndex());
		bottom.layout();
		return ret;
	}

	@Override
	protected Control createContents(Composite parent) {
		Control ret = super.createContents(parent);
		customizeDialog(lo.get(cbLo.getSelectionIndex()));
		return ret;
	}

	private void customizeDialog(IRnOutputter rnOutputter) {
		resetCustomButtons();
		resetDialog();
		rnOutputter.customizeDialog(this);
	}

	private void resetDialog() {
		resetOkButtonText();
		resetCancelButtonText();
	}

	public void setButtonEnabled(String buttonText, boolean enabled) {
		for (Control control : additionalButtonParent.getChildren()) {
			if (control instanceof Button) {
				Button btn = (Button) control;
				if (btn.getText().equals(buttonText)) {
					btn.setEnabled(enabled);
					return;
				}
			}
		}
	}

	public void setOkButtonEnabled(boolean enabled) {
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			okButton.setEnabled(enabled);
		}
	}

	public void setOkButtonText(String text) {
		Button button = getButton(IDialogConstants.OK_ID);
		button.setText(text);
		button.getParent().layout();
	}

	public void resetOkButtonText() {
		setOkButtonText(IDialogConstants.OK_LABEL);
	}

	public void setCancelButtonText(String text) {
		Button button = getButton(IDialogConstants.CANCEL_ID);
		button.setText(text);
		button.getParent().layout();
	}

	public void resetCancelButtonText() {
		setCancelButtonText(IDialogConstants.CANCEL_LABEL);
	}

	private Composite additionalButtonParent;

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (parent.getLayout() instanceof GridLayout) {
			((GridLayout) parent.getLayout()).numColumns++;
			additionalButtonParent = new Composite(parent, SWT.NONE);
			additionalButtonParent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			additionalButtonParent.setLayout(new RowLayout());
		}
		super.createButtonsForButtonBar(parent);
	}

	public Button addCustomButton(String text) {
	    Button ret = new Button(additionalButtonParent, SWT.PUSH);
	    ret.setText(text);
	    additionalButtonParent.getParent().requestLayout();
	    return ret;
	}

	public void redrawLayout() {
	    if (ret != null && !ret.isDisposed()) {
	        ret.getParent().layout(true, true);
	    }
	}

	public void resetCustomButtons() {
		for (Control control : additionalButtonParent.getChildren()) {
			if (control instanceof Button) {
				control.setVisible(false);
				control.dispose();
			}
		}
		additionalButtonParent.getParent().requestLayout();
	}

	public void customButtonPressed(int buttonId) {
		buttonPressed(buttonId);
	}

	@Override
	public void create() {
		super.create();
		int num = rnn.size();
		if (num > 1) {
			getShell().setText(Messages.RnOutputDialog_outputCaption); // $NON-NLS-1$
			setTitle(num + Messages.RnOutputDialog_outputTitle); // $NON-NLS-1$
			setMessage(MessageFormat.format(Messages.RnOutputDialog_outputMessage, num)); // $NON-NLS-1$

		} else {
			getShell().setText(Messages.Core_Output_Invoice); // $NON-NLS-1$
			setTitle(Messages.Core_Output_Invoice); // $NON-NLS-1$
			setMessage(Messages.RnOutputDialog_outputBillMessage); // $NON-NLS-1$
		}
	}

	public void updateSize() {
		getShell().setSize(getShell().getSize().x, 360);
	}

	@Override
	protected void okPressed() {
		boolean activated = ConfigServiceHolder.getUser(Preferences.USR_SHOWPATCHGREMINDER, false);
		if (activated) {
			ConfigServiceHolder.setUser(Preferences.USR_SHOWPATCHGREMINDER, false);
		}
		int idx = cbLo.getSelectionIndex();
		if (idx != -1) {
			IRnOutputter rnOutputter = lo.get(idx);
			rnOutputter.saveComposite();
			Iterator<Rechnung> it = rnn.iterator();
			boolean bFlag = false;
			while (it.hasNext()) {
				Rechnung r = it.next();
				if (r.getInvoiceState() == InvoiceState.CANCELLED) {
					it.remove();
					bFlag = true;
				}
			}
			if (bFlag) {
				SWTHelper.alert("Stornierte Rechnungen in Liste", "Stornierte Rechnungen werden nicht ausgegeben.");
			}
			rnOutputter.doOutput(bCopy.getSelection() ? IRnOutputter.TYPE.COPY : IRnOutputter.TYPE.ORIG, rnn,
					new Properties());
		}
		if (activated) {
			ConfigServiceHolder.setUser(Preferences.USR_SHOWPATCHGREMINDER, true);
		}
		super.okPressed();
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	private String normalize(String input) {
		return input.replace("Ä", "A").replace("Ö", "O").replace("Ü", "U");
	}

	private void sortOutputters(List<IRnOutputter> outputters) {
		outputters.sort((o1, o2) -> {
			String desc1 = normalize(o1.getDescription());
			String desc2 = normalize(o2.getDescription());
			if (isStandardOutputter(o1)) {
				return -1;
			} else if (isStandardOutputter(o2)) {
				return 1;
			} else if (isRechnungAusdruckenOutputter(o1)) {

				return -1;
			} else if (isRechnungAusdruckenOutputter(o2)) {
				return 1;
			}
			return desc1.compareTo(desc2);
		});
	}

	private boolean isStandardOutputter(IRnOutputter outputter) {
		return outputter.getClass().getName().equals(STANDARD_OUTPUTTER_CLASS);
	}

	private boolean isRechnungAusdruckenOutputter(IRnOutputter outputter) {
		return outputter.getClass().getName().equals(RECHNUNG_AUSDRUCKEN_OUTPUTTER_CLASS);
	}

}
