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

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.Rechnung;
import ch.elexis.core.data.RnStatus;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IRnOutputter;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;

public class RnOutputDialog extends TitleAreaDialog {
	private final Collection<Rechnung> rnn;
	private List<IRnOutputter> lo;
	private Combo cbLo;
	private Button bCopy;
	private final List<Control> ctls = new ArrayList<Control>();
	private final StackLayout stack = new StackLayout();
	
	public RnOutputDialog(Shell shell, Collection<Rechnung> rnn){
		super(shell);
		this.rnn = rnn;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent){
		lo = Extensions.getClasses("ch.elexis.RechnungsManager", "outputter"); //$NON-NLS-1$ //$NON-NLS-2$
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		cbLo = new Combo(ret, SWT.SINGLE | SWT.READ_ONLY);
		cbLo.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		bCopy = new Button(ret, SWT.CHECK);
		bCopy.setText(Messages.getString("RnOutputDialog.markAsCopy")); //$NON-NLS-1$
		bCopy.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		final Composite bottom = new Composite(ret, SWT.NONE);
		bottom.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		bottom.setLayout(stack);
		for (IRnOutputter ro : lo) {
			cbLo.add(ro.getDescription());
			ctls.add((Control) ro.createSettingsControl(bottom));
		}
		cbLo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				int idx = cbLo.getSelectionIndex();
				if (idx != -1) {
					stack.topControl = ctls.get(idx);
					bottom.layout();
					CoreHub.localCfg.set(Preferences.RNN_DEFAULTEXPORTMODE, idx);
				}
			}
			
		});
		int lastSelected = CoreHub.localCfg.get(Preferences.RNN_DEFAULTEXPORTMODE, 0);
		if ((lastSelected < 0) || (lastSelected >= cbLo.getItemCount())) {
			lastSelected = 0;
			CoreHub.localCfg.set(Preferences.RNN_DEFAULTEXPORTMODE, 0);
		}
		cbLo.select(lastSelected);
		stack.topControl = ctls.get(cbLo.getSelectionIndex());
		bottom.layout();
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		int num = rnn.size();
		if (num > 1) {
			getShell().setText(Messages.getString("RnOutputDialog.outputCaption")); //$NON-NLS-1$
			setTitle(num + Messages.getString("RnOutputDialog.outputTitle")); //$NON-NLS-1$
			setMessage(MessageFormat
				.format(Messages.getString("RnOutputDialog.outputMessage"), num)); //$NON-NLS-1$
			
		} else {
			getShell().setText(Messages.getString("RnOutputDialog.outputBillCaption")); //$NON-NLS-1$
			setTitle(Messages.getString("RnOutputDialog.outputBillTitle")); //$NON-NLS-1$
			setMessage(Messages.getString("RnOutputDialog.outputBillMessage")); //$NON-NLS-1$
		}
		setTitleImage(Images.IMG_LOGO.getImage(ImageSize._75x66_TitleDialogIconSize));
	}
	
	@Override
	protected void okPressed(){
		int idx = cbLo.getSelectionIndex();
		if (idx != -1) {
			IRnOutputter rop = lo.get(idx);
			rop.saveComposite();
			Iterator<Rechnung> it = rnn.iterator();
			boolean bFlag = false;
			while (it.hasNext()) {
				Rechnung r = it.next();
				if (r.getStatus() == RnStatus.STORNIERT) {
					it.remove();
					bFlag = true;
				}
			}
			if (bFlag) {
				SWTHelper.alert("Stornierte Rechnungen in Liste",
					"Stornierte Rechnungen werden nicht ausgegeben.");
			}
			/* Result<Rechnung> result= */rop.doOutput(
				bCopy.getSelection() ? IRnOutputter.TYPE.COPY : IRnOutputter.TYPE.ORIG, rnn,
				new Properties());
		}
		super.okPressed();
	}
	
}
