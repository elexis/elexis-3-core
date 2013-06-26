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
package ch.elexis.core.ui.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.util.DayDateCombo;
import ch.elexis.core.ui.util.MoneyInput;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

public class KonsZumVerrechnenWizardDialog extends TitleAreaDialog {
	private static final String CONFIG = "dialogs/konszumverrechnen/"; //$NON-NLS-1$
	private static final String ALLMARKED = Messages
		.getString("KonsZumVerrechnenWizardDialog.selectCasesToCharge"); //$NON-NLS-1$
	private static final String TAGEN_BZW_DEM = Messages
		.getString("KonsZumVerrechnenWizardDialog.daysOrDate"); //$NON-NLS-1$
	private static final String RECHNUNGEN_ERSTELLEN = Messages
		.getString("KonsZumVerrechnenWizardDialog.createBills"); //$NON-NLS-1$
	private static final String BEHANDLUNGEN_ZUM_VERRECHNEN_AUTOMATISCH_AUSWAEHLEN = Messages
		.getString("KonsZumVerrechnenWizardDialog.createProposal"); //$NON-NLS-1$
	private static final String RECHNUNGS_AUTOMATIK = Messages
		.getString("KonsZumVerrechnenWizardDialog.billingAUtomation"); //$NON-NLS-1$
	private static final String TREATMENT_TRIMESTER = Messages
		.getString("KonsZumVerrechnenWizardDialog.choseAllQuartal"); //$NON-NLS-1$
	private static final String TREATMENT_AMOUNTHIGHER = Messages
		.getString("KonsZumVerrechnenWizardDialog.chooseFromAmount"); //$NON-NLS-1$
	private static final String TREATMENTENDBEFORE = Messages
		.getString("KonsZumVerrechnenWizardDialog.choseEndeDate"); //$NON-NLS-1$
	private final static String TREATMENTBEGINBEFORE = Messages
		.getString("KonsZumVerrechnenWizardDialog.chooseBeginningDate"); //$NON-NLS-1$
	
	private static final String SKIPSELECTION = Messages
		.getString("KonsZumVerrechnenWizardDialog.skipProposal"); //$NON-NLS-1$
	private static final String CFG_SKIP = CONFIG + "skipselection"; //$NON-NLS-1$
	
	Button cbMarked, cbBefore, cbAmount, cbTime, cbQuartal, cbSkip;
	// DatePickerCombo dp1, dp2;
	// Spinner sp1, sp2;
	MoneyInput mi1;
	DayDateCombo ddc1, ddc2;
	
	public TimeTool ttFirstBefore, ttLastBefore;
	public Money mAmount;
	public boolean bQuartal, bMarked, bSkip;
	
	public KonsZumVerrechnenWizardDialog(final Shell parentShell){
		super(parentShell);
		
	}
	
	@Override
	protected Control createDialogArea(final Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(4, false));
		cbMarked = new Button(ret, SWT.CHECK);
		cbMarked.setText(ALLMARKED);
		cbMarked.setLayoutData(SWTHelper.getFillGridData(4, true, 1, false));
		cbMarked.setSelection(true);
		cbBefore = new Button(ret, SWT.CHECK);
		cbBefore.setText(TREATMENTBEGINBEFORE);
		ddc1 = new DayDateCombo(ret, "", TAGEN_BZW_DEM); //$NON-NLS-1$
		cbTime = new Button(ret, SWT.CHECK);
		cbTime.setText(TREATMENTENDBEFORE);
		
		ddc2 = new DayDateCombo(ret, "", TAGEN_BZW_DEM); //$NON-NLS-1$
		int prev = CoreHub.localCfg.get(CONFIG + "beginBefore", 30) * -1; //$NON-NLS-1$
		TimeTool ttNow = new TimeTool();
		ttNow.addDays(prev);
		ddc1.setDays(prev);
		
		prev = CoreHub.localCfg.get(CONFIG + "endBefore", 20) * -1; //$NON-NLS-1$
		ddc2.setDays(prev);
		ddc1.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		
		ddc2.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		cbAmount = new Button(ret, SWT.CHECK);
		cbAmount.setText(TREATMENT_AMOUNTHIGHER);
		mi1 = new MoneyInput(ret);
		mi1.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		new Label(ret, SWT.NONE);
		cbQuartal = new Button(ret, SWT.CHECK);
		cbQuartal.setText(TREATMENT_TRIMESTER);
		new Label(ret, SWT.NONE);
		new Label(ret, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(SWTHelper.getFillGridData(4,
			true, 1, false));
		cbSkip = new Button(ret, SWT.CHECK);
		cbSkip.setText(SKIPSELECTION);
		cbSkip.setSelection(CoreHub.globalCfg.get(CFG_SKIP, false));
		cbBefore.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				ddc1.setEnabled(cbBefore.getSelection());
			}
			
		});
		cbTime.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				ddc2.setEnabled(cbTime.getSelection());
			}
			
		});
		ddc1.setEnabled(false);
		ddc2.setEnabled(false);
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		setTitle(RECHNUNGS_AUTOMATIK);
		setMessage(BEHANDLUNGEN_ZUM_VERRECHNEN_AUTOMATISCH_AUSWAEHLEN);
		getShell().setText(RECHNUNGEN_ERSTELLEN);
	}
	
	@Override
	protected void okPressed(){
		
		if (cbBefore.getSelection()) {
			ttFirstBefore = ddc1.getDate();
		}
		if (cbTime.getSelection()) {
			ttLastBefore = ddc2.getDate();
		}
		if (cbAmount.getSelection()) {
			mAmount = mi1.getMoney(false);
		}
		bQuartal = cbQuartal.getSelection();
		bMarked = cbMarked.getSelection();
		bSkip = cbSkip.getSelection();
		CoreHub.globalCfg.set(CFG_SKIP, bSkip);
		super.okPressed();
	}
	
}
