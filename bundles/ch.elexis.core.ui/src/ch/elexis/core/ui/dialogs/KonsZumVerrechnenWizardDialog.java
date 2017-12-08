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

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.util.DayDateCombo;
import ch.elexis.core.ui.util.MoneyInput;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Fall;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

public class KonsZumVerrechnenWizardDialog extends TitleAreaDialog {
	private static final String CONFIG = "dialogs/konszumverrechnen/"; //$NON-NLS-1$
	private static final String ALLMARKED =
		Messages.KonsZumVerrechnenWizardDialog_selectCasesToCharge; //$NON-NLS-1$
	private static final String TAGEN_BZW_DEM = Messages.KonsZumVerrechnenWizardDialog_daysOrDate; //$NON-NLS-1$
	private static final String RECHNUNGEN_ERSTELLEN =
		Messages.KonsZumVerrechnenWizardDialog_createBills; //$NON-NLS-1$
	private static final String BEHANDLUNGEN_ZUM_VERRECHNEN_AUTOMATISCH_AUSWAEHLEN =
		Messages.KonsZumVerrechnenWizardDialog_createProposal; //$NON-NLS-1$
	private static final String RECHNUNGS_AUTOMATIK =
		Messages.KonsZumVerrechnenWizardDialog_billingAUtomation; //$NON-NLS-1$
	private static final String TREATMENT_TRIMESTER =
		Messages.KonsZumVerrechnenWizardDialog_choseAllQuartal; //$NON-NLS-1$
	private static final String TREATMENT_AMOUNTHIGHER =
		Messages.KonsZumVerrechnenWizardDialog_chooseFromAmount; //$NON-NLS-1$
	private static final String TREATMENTENDBEFORE =
		Messages.KonsZumVerrechnenWizardDialog_choseEndeDate; //$NON-NLS-1$
	private final static String TREATMENTBEGINBEFORE =
		Messages.KonsZumVerrechnenWizardDialog_chooseBeginningDate; //$NON-NLS-1$
	private final static String TREATMENT_TIMESPAN =
		Messages.KonsZumVerrechnenWizardDialog_timespan;
	private final static String TREATMENT_TIMESPAN_TILL =
		Messages.KonsZumVerrechnenWizardDialog_timespanTill;
	private final static String TREATMENT_ACCOUNTING_SYS =
		Messages.KonsZumVerrechnenWizardDialog_chooseAccountingSystem;
	
	private static final String SKIPSELECTION = Messages.KonsZumVerrechnenWizardDialog_skipProposal; //$NON-NLS-1$
	private static final String CFG_SKIP = CONFIG + "skipselection"; //$NON-NLS-1$
	
	Button cbMarked, cbBefore, cbAmount, cbTime, cbQuartal, cbSkip, cbTimespan, cbAccountingSys;
	// DatePickerCombo dp1, dp2;
	// Spinner sp1, sp2;
	MoneyInput mi1;
	DayDateCombo ddc1, ddc2;
	
	ComboViewer cAccountingSys;
	public String accountSys;
	public TimeTool ttFirstBefore, ttLastBefore, ttFrom, ttTo;
	public Money mAmount;
	public boolean bQuartal, bMarked, bSkip;
	private DateTime timespanFrom, timespanTo;
	
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
		cbBefore = new Button(ret, SWT.CHECK);
		cbBefore.setText(TREATMENTBEGINBEFORE);
		ddc1 = new DayDateCombo(ret, "", TAGEN_BZW_DEM); //$NON-NLS-1$
		ddc1.spinDaysBack();
		cbTime = new Button(ret, SWT.CHECK);
		cbTime.setText(TREATMENTENDBEFORE);
		
		ddc2 = new DayDateCombo(ret, "", TAGEN_BZW_DEM); //$NON-NLS-1$
		ddc2.spinDaysBack();
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
		mi1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		mi1.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		
		cbQuartal = new Button(ret, SWT.CHECK);
		cbQuartal.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		cbQuartal.setText(TREATMENT_TRIMESTER);
		
		cbTimespan = new Button(ret, SWT.CHECK);
		cbTimespan.setText(TREATMENT_TIMESPAN);
		timespanFrom = new DateTime(ret, SWT.NONE);
		Label lblTill = new Label(ret, SWT.NONE);
		lblTill.setText(TREATMENT_TIMESPAN_TILL);
		timespanTo = new DateTime(ret, SWT.NONE);
		cbTimespan.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				timespanFrom.setEnabled(cbTimespan.getSelection());
				timespanTo.setEnabled(cbTimespan.getSelection());
			}
		});
		
		cbAccountingSys = new Button(ret, SWT.CHECK);
		cbAccountingSys.setText(TREATMENT_ACCOUNTING_SYS);
		cbAccountingSys.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				cAccountingSys.getCombo().setEnabled(cbAccountingSys.getSelection());
			}
		});
		cAccountingSys = new ComboViewer(ret, SWT.NONE);
		Combo combo = cAccountingSys.getCombo();
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		cAccountingSys.setContentProvider(ArrayContentProvider.getInstance());
		cAccountingSys.setLabelProvider(new LabelProvider());
		String[] accSystems = Fall.getAbrechnungsSysteme();
		cAccountingSys.setInput(accSystems);
		cAccountingSys.setSelection(new StructuredSelection(accSystems[0]));
		
		new Label(ret, SWT.NONE);
		new Label(ret, SWT.NONE);
		new Label(ret, SWT.NONE);
		new Label(ret, SWT.NONE);
		new Label(ret, SWT.NONE);
		new Label(ret, SWT.NONE);
		new Label(ret, SWT.NONE);
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
		
		cAccountingSys.getCombo().setEnabled(false);
		timespanFrom.setEnabled(false);
		timespanTo.setEnabled(false);
		ddc1.setEnabled(false);
		ddc2.setEnabled(false);
		new Label(ret, SWT.NONE);
		new Label(ret, SWT.NONE);
		new Label(ret, SWT.NONE);
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
		if (cbTimespan.getSelection()) {
			ttFrom = getDate(timespanFrom, 0, 0, 0);
			ttTo = getDate(timespanTo, 23, 59, 59);
		}
		if (cbAccountingSys.getSelection()) {
			IStructuredSelection sel = (IStructuredSelection) cAccountingSys.getSelection();
			accountSys = (String) sel.getFirstElement();
		}
		bQuartal = cbQuartal.getSelection();
		bMarked = cbMarked.getSelection();
		bSkip = cbSkip.getSelection();
		CoreHub.globalCfg.set(CFG_SKIP, bSkip);
		super.okPressed();
	}
	
	private TimeTool getDate(DateTime selDate, int hour, int minute, int second){
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, selDate.getDay());
		cal.set(Calendar.MONTH, selDate.getMonth());
		cal.set(Calendar.YEAR, selDate.getYear());
		
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		
		TimeTool date = new TimeTool(cal.getTime());
		
		return date;
	}
}
