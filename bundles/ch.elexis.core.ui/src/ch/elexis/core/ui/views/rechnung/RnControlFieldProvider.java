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
package ch.elexis.core.ui.views.rechnung;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ControlFieldListener;
import ch.elexis.data.BillingSystem;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.data.RnStatus;
import ch.rgw.tools.IFilter;
import ch.rgw.tools.StringTool;

/**
 * Controlfieldprovider for RechnungsListeView. Creates a Composite that contains the controls to
 * select the criteria for the bills to be displayed
 * 
 * @author gerry
 * 
 */
class RnControlFieldProvider implements ViewerConfigurer.ControlFieldProvider {
	// final String[]
	// stats={"Alle","Bezahlt","Offen","Offen&Gedruckt","1. Mahnung","2. Mahnung","3. Mahnung","In Betreibung","Teilverlust","Totalverlust"};
	final static String[] stats = {
		Messages.RnControlFieldProvider_all, Messages.RnControlFieldProvider_open,
		Messages.RnControlFieldProvider_openAndPrinted, Messages.RnControlFieldProvider_partlyPaid,
		Messages.RnControlFieldProvider_paid, Messages.RnControlFieldProvider_overpaid, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		Messages.RnControlFieldProvider_reminder, Messages.RnControlFieldProvider_reminderPrinted,
		Messages.RnControlFieldProvider_reminder2, Messages.RnControlFieldProvider_reminder2Printed,
		Messages.RnControlFieldProvider_reminder3, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		Messages.RnControlFieldProvider_reminder3Printed,
		Messages.RnControlFieldProvider_enforcement, Messages.RnControlFieldProvider_partlyLost,
		Messages.RnControlFieldProvider_totallyLost, Messages.RnControlFieldProvider_storno, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		Messages.RnControlFieldProvider_erroneous, Messages.RnControlFieldProvider_toPrint,
		Messages.RnControlFieldProvider_toBePaid, Messages.RnControlFieldProvider_dontRemind,
		Messages.RnControlFieldProvider_writtenOff, Messages.RnControlFieldProvider_rejected
	};
	
	final static int[] statInts = {
		RnStatus.UNBEKANNT, RnStatus.OFFEN, RnStatus.OFFEN_UND_GEDRUCKT, RnStatus.TEILZAHLUNG,
		RnStatus.BEZAHLT, RnStatus.ZUVIEL_BEZAHLT, RnStatus.MAHNUNG_1, RnStatus.MAHNUNG_1_GEDRUCKT,
		RnStatus.MAHNUNG_2, RnStatus.MAHNUNG_2_GEDRUCKT, RnStatus.MAHNUNG_3,
		RnStatus.MAHNUNG_3_GEDRUCKT, RnStatus.IN_BETREIBUNG, RnStatus.TEILVERLUST,
		RnStatus.TOTALVERLUST, RnStatus.STORNIERT, RnStatus.FEHLERHAFT, RnStatus.ZU_DRUCKEN,
		RnStatus.AUSSTEHEND, RnStatus.MAHNSTOPP, RnStatus.ABGESCHRIEBEN, RnStatus.ZURUECKGEWIESEN
	};
	
	final static int STAT_DEFAULT_INDEX = 1;
	private final static String ALLE = Messages.RnControlFieldProvider_allPatients; //$NON-NLS-1$
	private final static String ALL = Messages.RnControlFieldProvider_all; //$NON-NLS-1$
	
	Combo cbStat;
	Combo cbZType;
	/* DatePickerCombo dpVon, dpBis; */
	private List<ControlFieldListener> listeners;
	private final SelectionAdapter csel = new CtlSelectionListener();
	private boolean bDateAsStatus;
	private HyperlinkAdapter /* hlStatus, */ hlPatient;
	private Label /* hDateFrom, hDateUntil, */ lPatient;
	Text tNr, tBetrag;
	String oldSelectedBillingSystem = ""; //$NON-NLS-1$
	
	Patient actPatient;
	
	public Composite createControl(final Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		listeners = new ArrayList<ControlFieldListener>();
		ret.setLayout(new GridLayout(5, true));
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		hlPatient = new HyperlinkAdapter() {
			@Override
			public void linkActivated(final HyperlinkEvent e){
				Patient oldPatient = actPatient;
				KontaktSelektor ksl = new KontaktSelektor(parent.getShell(), Patient.class,
					Messages.RnControlFieldProvider_selectPatientCaption, //$NON-NLS-1$
					Messages.RnControlFieldProvider_selectPatientMessage, true); //$NON-NLS-1$
				if (ksl.open() == Dialog.OK) {
					actPatient = (Patient) ksl.getSelection();
					if (actPatient != null) {
						lPatient.setText(actPatient.getLabel());
						cbStat.setText(stats[0]);
					} else {
						lPatient.setText(ALLE);
						cbStat.setText(stats[1]);
					}
				} else {
					actPatient = null;
					lPatient.setText(ALLE);
					cbStat.setText(stats[1]);
				}
				
				if (actPatient == null && oldPatient == null) {
					return;
				} else if (actPatient != null && oldPatient != null
					&& actPatient.equals(oldPatient)) {
					return;
				}
				fireChangedEvent();
			}
		};
		new Label(ret, SWT.NONE).setText(Messages.RnControlFieldProvider_state); //$NON-NLS-1$
		Label lbl =
			SWTHelper.createHyperlink(ret, Messages.RnControlFieldProvider_patient2, hlPatient); //$NON-NLS-1$
		lbl.setForeground(UiDesk.getColorRegistry().get(UiDesk.COL_BLUE));
		new Label(ret, SWT.NONE).setText(Messages.RnControlFieldProvider_PaymentSystem); //$NON-NLS-1$
		new Label(ret, SWT.NONE).setText(Messages.RnControlFieldProvider_invoideNr); //$NON-NLS-1$
		new Label(ret, SWT.NONE).setText(Messages.RnControlFieldProvider_amount); //$NON-NLS-1$
		// / ^ labels / values
		cbStat = new Combo(ret, SWT.READ_ONLY);
		cbStat.setVisibleItemCount(stats.length);
		cbStat.setItems(stats);
		cbStat.addSelectionListener(csel);
		cbStat.select(STAT_DEFAULT_INDEX);
		lPatient = new Label(ret, SWT.NONE);
		lPatient.setText(ALLE);
		cbZType = new Combo(ret, SWT.SINGLE | SWT.READ_ONLY);
		// sort items according to prefs
		cbZType.setItems(ch.elexis.core.ui.preferences.UserCasePreferences
			.sortBillingSystems(BillingSystem.getAbrechnungsSysteme()));
		cbZType.add(ALL);
		// focus listener needed because view may be created BEFORE a user is active
		// but for the sorting we need the user prefs for sorting
		// AND if the prefs have just been modified...
		cbZType.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e){
				// only set items if there ARE changes to avoid unnecessary flickering
				String[] currItems = cbZType.getItems();
				String[] newItems = ch.elexis.core.ui.preferences.UserCasePreferences
					.sortBillingSystems(BillingSystem.getAbrechnungsSysteme());
				if (!Arrays.equals(currItems, newItems)) {
					String savedItem = cbZType.getText();
					cbZType.setItems(newItems);
					cbZType.setText(savedItem);
					cbZType.add(ALL);
				}
				oldSelectedBillingSystem = cbZType.getText();
				
			}
			
			@Override
			public void focusLost(FocusEvent e){}
		});
		
		// added to prevent selection of separator
		cbZType.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e){
				int separatorPos = ch.elexis.core.ui.preferences.UserCasePreferences
					.getBillingSystemsMenuSeparatorPos(BillingSystem.getAbrechnungsSysteme());
				if (cbZType.getSelectionIndex() == separatorPos)
					cbZType.select(cbZType.indexOf(oldSelectedBillingSystem));
				else
					oldSelectedBillingSystem = cbZType.getText();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e){}
		});
		
		/*
		 * GridData gdlp=new GridData(); gdlp.widthHint=150; gdlp.minimumWidth=150;
		 */
		tNr = new Text(ret, SWT.BORDER);
		tNr.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e){
				if (tNr.getText().length() == 0) {
					cbStat.select(STAT_DEFAULT_INDEX);
				}
				fireChangedEvent();
			}
			
		});
		tBetrag = new Text(ret, SWT.BORDER);
		tBetrag.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e){
				if (tNr.getText().length() == 0) {
					cbStat.select(STAT_DEFAULT_INDEX);
				}
				fireChangedEvent();
			}
			
		});
		GridData sgd = new GridData();
		sgd.minimumWidth = 100;
		sgd.widthHint = 100;
		return ret;
	}
	
	public void addChangeListener(final ControlFieldListener cl){
		listeners.add(cl);
	}
	
	public void removeChangeListener(final ControlFieldListener cl){
		listeners.remove(cl);
	}
	
	public boolean getDateModeIsStatus(){
		return bDateAsStatus;
	}
	
	public String[] getValues(){
		String[] ret = new String[5];
		int selIdx = cbStat.getSelectionIndex();
		if (selIdx != -1) {
			ret[0] = Integer.toString(statInts[selIdx]);
		} else {
			ret[0] = StringConstants.ONE;
		}
		if (actPatient != null) {
			ret[1] = actPatient.getId();
		}
		ret[2] = tNr.getText();
		ret[3] = tBetrag.getText().replaceAll("\\.", ""); //$NON-NLS-1$ //$NON-NLS-2$
		if (StringTool.isNothing(ret[2])) {
			ret[2] = null;
		} else { // Wenn RnNummer gegeben ist, alles andere auf Standard.
			clearValues();
			tNr.setText(ret[2]);
			ret[0] = StringConstants.ZERO;
			ret[1] = null;
			ret[3] = null;
		}
		if (StringTool.isNothing(ret[3])) {
			ret[3] = null;
		} else {
			clearValues();
			tBetrag.setText(ret[3]);
			ret[0] = StringConstants.ZERO;
			ret[1] = null;
			ret[2] = null;
		}
		ret[4] = cbZType.getText();
		if (StringTool.isNothing(ret[4]) || ret[4].equals(ALL)) {
			ret[4] = null;
		}
		return ret;
	}
	
	public void clearValues(){
		cbStat.select(0);
		tNr.setText(""); //$NON-NLS-1$
		actPatient = null;
		lPatient.setText(ALLE);
	}
	
	public boolean isEmpty(){
		return false;
	}
	
	public void setQuery(final Query q){
		
	}
	
	public IFilter createFilter(){
		return new IFilter() {
			
			public boolean select(final Object element){
				return true;
			}
		};
	}
	
	public void fireChangedEvent(){
		UiDesk.getDisplay().syncExec(new Runnable() {
			public void run(){
				HashMap<String, String> hm = new HashMap<String, String>();
				hm.put(Messages.RnControlFieldProvider_state, StringConstants.ZERO); //$NON-NLS-1$
				for (ControlFieldListener lis : listeners) {
					lis.changed(hm);
					
				}
			}
		});
	}
	
	public void fireSortEvent(final String text){
		for (ControlFieldListener lis : listeners) {
			lis.reorder(text);
			
		}
	}
	
	public void setFocus(){
		
	}
	
	private static class CtlSelectionListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e){
			// fireChangedEvent(); do nothing. Only refresh by click on the refresh button
		}
	}
}