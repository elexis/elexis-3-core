/*******************************************************************************
 * Copyright (c) 2006-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    H. Marlovits - added more field types (multiple lines text, styled text, combos, checkboxes, lists)
 *    				 added optional and unused/deleted fields editor
 *******************************************************************************/

package ch.elexis.core.ui.views;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.preferences.Leistungscodes;
import ch.elexis.core.ui.preferences.UserCasePreferences;
import ch.elexis.core.ui.text.ITextPlugin;
import ch.elexis.core.ui.text.ITextPlugin.ICallback;
import ch.elexis.core.ui.text.TextContainer;
import ch.elexis.core.ui.util.DayDateCombo;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

import com.tiff.common.ui.datepicker.DatePickerCombo;

/**
 * Display detail data of a Fall
 */
public class FallDetailBlatt2 extends Composite {
	private static final String SELECT_CONTACT_BODY =
		Messages.FallDetailBlatt2_PleaseSelectContactFor; //$NON-NLS-1$
	private static final String SELECT_CONTACT_CAPTION =
		Messages.FallDetailBlatt2_PleaseSelectCpntactCaption; //$NON-NLS-1$
	private static final String LABEL = Messages.FallDetailBlatt2_Labek; //$NON-NLS-1$
	private static final String RECHNUNGSEMPFAENGER = Messages.FallDetailBlatt2_BillAdressee; //$NON-NLS-1$
	private static final String VERSICHERUNGSNUMMER = Messages.FallDetailBlatt2_InsuranceNumber; //$NON-NLS-1$
	private static final String KOSTENTRAEGER = Messages.FallDetailBlatt2_Guarantor; //$NON-NLS-1$
	private static final String ABRECHNUNGSMETHODE = Messages.FallDetailBlatt2_BillingMethod; //$NON-NLS-1$
	private static final String DEFINITIONSDELIMITER = ";"; //$NON-NLS-1$
	private static final String ARGUMENTSSDELIMITER = ":"; //$NON-NLS-1$
	private static final String ITEMDELIMITER = "\t"; //$NON-NLS-1$
	private final FormToolkit tk;
	private final ScrolledForm form;
	String[] Abrechnungstypen = UserCasePreferences
		.sortBillingSystems(Fall.getAbrechnungsSysteme());
	private Fall actFall;
	DayDateCombo ddc;
	
	String itemsErrorMessage = "parameters not supplied;please control parameters;in preferences"; //$NON-NLS-1$
	
	public static final String[] Reasons = {
		Fall.TYPE_DISEASE, Fall.TYPE_ACCIDENT, Fall.TYPE_MATERNITY, Fall.TYPE_PREVENTION,
		Fall.TYPE_BIRTHDEFECT, Fall.TYPE_OTHER
	};
	public static final String[] dgsys = null;
	Combo cAbrechnung, cReason;
	DatePickerCombo dpVon, dpBis;
	Text tBezeichnung, tGarant;
	Hyperlink autoFill, hlGarant;
	List<Control> lReqs = new ArrayList<Control>();
	List<Control> keepEditable = new ArrayList<Control>();
	Button btnCopyForPatient;
	
	public FallDetailBlatt2(final Composite parent){
		super(parent, SWT.NONE);
		tk = UiDesk.getToolkit();
		form = tk.createScrolledForm(this);
		Composite top = form.getBody();
		setLayout(new FillLayout());
		top.setLayout(new GridLayout(2, false));
		tk.createLabel(top, ABRECHNUNGSMETHODE);
		Composite cpAbrechnung = new Composite(top, SWT.NONE);
		// *** want to have no-color-background...
		cpAbrechnung.setBackground(new Color(cpAbrechnung.getDisplay(), 255, 255, 255));
		GridLayout grid = new GridLayout(2, false);
		grid.marginWidth = 0;
		cpAbrechnung.setLayout(grid);
		cAbrechnung = new Combo(cpAbrechnung, SWT.READ_ONLY);
		autoFill = tk.createHyperlink(cpAbrechnung, Messages.FallDetailBlatt2_ApplyData, SWT.NONE); //$NON-NLS-1$
		autoFill.addHyperlinkListener(new HyperlinkAdapter() {
			
			@Override
			public void linkActivated(final HyperlinkEvent e){
				Fall f = getFall();
				if (f == null) {
					return;
				}
				String abr = f.getAbrechnungsSystem();
				// make sure compatibility methods are called
				
				String ktNew = f.getInfoString(KOSTENTRAEGER);
				String ktOld = f.get(Messages.FallDetailBlatt2_GuarantorNoSpecialChars); //$NON-NLS-1$
				
				if (StringTool.isNothing(ktNew)) {
					Kontakt k = Kontakt.load(ktOld);
					if (k.isValid()) {
						f.setRequiredContact(KOSTENTRAEGER, k);
					}
				}
				String vnNew = f.getInfoString(VERSICHERUNGSNUMMER);
				// String vnOld=f.getVersNummer();
				String vnOld = f.get(Messages.FallDetailBlatt2_InsNumber); //$NON-NLS-1$
				if (StringTool.isNothing(vnNew)) {
					f.setRequiredString(VERSICHERUNGSNUMMER, vnOld);
				}
				
				Fall[] faelle = f.getPatient().getFaelle();
				for (Fall f0 : faelle) {
					if (f0.getId().equals(f.getId())) {
						// ignore current Fall
						continue;
					}
					
					if (f0.getAbrechnungsSystem().equals(abr)) {
						if (f.getInfoString(RECHNUNGSEMPFAENGER).isEmpty()) {
							f.setInfoString(RECHNUNGSEMPFAENGER, f0.get("GarantID")); //$NON-NLS-1$
						}
						if (f.getInfoString(KOSTENTRAEGER).isEmpty()) {
							f.setInfoString(KOSTENTRAEGER,
								f0.get(Messages.FallDetailBlatt2_GuarantorNoSpecialChars)); //$NON-NLS-1$
						}
						if (f.getInfoString(VERSICHERUNGSNUMMER).isEmpty()) {
							f.setInfoString(VERSICHERUNGSNUMMER,
								f0.getInfoString(VERSICHERUNGSNUMMER));
						}
						// TODO break? or looking for the most current Fall?
						break;
					}
				}
				setFall(f);
			}
		});
		cAbrechnung.setItems(Abrechnungstypen);
		
		cAbrechnung.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e){
				int i = cAbrechnung.getSelectionIndex();
				String abrechungsMethodeStr = cAbrechnung.getItem(i);
				int separatorPos =
					UserCasePreferences.getBillingSystemsMenuSeparatorPos(Abrechnungstypen);
				boolean isDisabled = Leistungscodes.isBillingSystemDisabled(abrechungsMethodeStr);
				Fall fall = getFall();
				// get previously selected item/gesetz if we need to reset
				String gesetz = ""; //$NON-NLS-1$
				if (fall != null)
					gesetz = fall.getAbrechnungsSystem();
				if (ch.rgw.tools.StringTool.isNothing(gesetz))
					gesetz = Messages.FallDetailBlatt2_free; //$NON-NLS-1$
				if (i == separatorPos) {
					// this is the separator - cannot select - simply reset to previous selection
					cAbrechnung.select(cAbrechnung.indexOf(gesetz));
				} else if (isDisabled) {
					// selection not allowed - reset previous selection after message
					SWTHelper.alert(Messages.FallDetailBlatt2_ChangeBillingSystemNotAllowedCaption, //$NON-NLS-1$
						Messages.FallDetailBlatt2_ChangeBillingSystemNotAllowedBody); //$NON-NLS-1$
					cAbrechnung.select(cAbrechnung.indexOf(gesetz));
				} else {
					if (fall != null) {
						if (fall.getBehandlungen(false).length > 0) {
							if (CoreHub.acl.request(AccessControlDefaults.CASE_MODIFY)) {
								if (SWTHelper.askYesNo(
									Messages.FallDetailBlatt2_DontChangeBillingSystemCaption, //$NON-NLS-1$
									Messages.FallDetailBlatt2_DontChangeBillingSystemBody)) { //$NON-NLS-1$
									fall.setAbrechnungsSystem(cAbrechnung.getItem(i));
									setFall(fall);
									ElexisEventDispatcher.fireSelectionEvent(fall.getPatient());
									return;
								}
							} else {
								SWTHelper.alert(
									Messages.FallDetailBlatt2_CantChangeBillingSystemCaption, //$NON-NLS-1$
									Messages.FallDetailBlatt2_CantChangeBillingSystemBody); //$NON-NLS-1$
							}
							cAbrechnung.select(cAbrechnung.indexOf(gesetz));
							
						} else {
							fall.setAbrechnungsSystem(Abrechnungstypen[i]);
							setFall(fall);
							ElexisEventDispatcher.fireSelectionEvent(fall.getPatient());
							// Falls noch kein Garant gesetzt ist: Garanten des
							// letzten Falles zum
							// selben Gesetz nehmen
						}
					}
					
				}
			}
			
		});
		
		// focus listener needed because view may be created BEFORE a user is active
		// but for the sorting we need the user prefs for sorting
		// AND if the prefs have just been modified...
		cAbrechnung.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e){
				// only set items if there ARE changes to avoid unnecessary flickering
				String[] currItems = cAbrechnung.getItems();
				String[] newItems =
					UserCasePreferences.sortBillingSystems(Fall.getAbrechnungsSysteme());
				if (!Arrays.equals(currItems, newItems)) {
					String savedItem = cAbrechnung.getText();
					cAbrechnung.setItems(newItems);
					cAbrechnung.setText(savedItem);
				}
			}
			
			@Override
			public void focusLost(FocusEvent e){}
		});
		
		tk.createLabel(top, LABEL);
		tBezeichnung = tk.createText(top, StringTool.leer);
		tBezeichnung.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent e){
				String newval = ((Text) e.getSource()).getText();
				Fall fall = getFall();
				if (fall != null) {
					fall.set(LABEL, newval);
					ElexisEventDispatcher.fireSelectionEvent(fall.getPatient());
				}
				super.focusLost(e);
			}
		});
		tBezeichnung.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tk.createLabel(top, Messages.FallDetailBlatt2_ReasonForInsurance); //$NON-NLS-1$
		cReason = new Combo(top, SWT.READ_ONLY);
		cReason.setItems(Reasons);
		cReason.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e){
				int i = cReason.getSelectionIndex();
				Fall fall = getFall();
				if (fall != null) {
					fall.setGrund(Reasons[i]);
					ElexisEventDispatcher.fireSelectionEvent(fall.getPatient());
				}
			}
		});
		cReason.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tk.createLabel(top, Messages.FallDetailBlatt2_StartDate); //$NON-NLS-1$
		dpVon = new DatePickerCombo(top, SWT.NONE);
		dpVon.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(final SelectionEvent e){
				Fall fall = getFall();
				fall.setBeginnDatum(new TimeTool(dpVon.getDate().getTime())
					.toString(TimeTool.DATE_GER));
				ElexisEventDispatcher.fireSelectionEvent(fall.getPatient());
			}
			
		});
		tk.createLabel(top, Messages.FallDetailBlatt2_EndDate); //$NON-NLS-1$
		dpBis = new DatePickerCombo(top, SWT.NONE);
		dpBis.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(final SelectionEvent e){
				Fall fall = getFall();
				fall.setEndDatum(new TimeTool(dpBis.getDate().getTime())
					.toString(TimeTool.DATE_GER));
				ElexisEventDispatcher.fireSelectionEvent(fall.getPatient());
			}
			
		});
		ddc =
			new DayDateCombo(top, Messages.FallDetailBlatt2_ProposeForBillingIn,
				Messages.FallDetailBlatt2_DaysOrAfter,
				Messages.FallDetailBlatt2_ProposeForBillingNeg,
				Messages.FallDetailBlatt2_DaysOrAfterNeg);
		ddc.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		ddc.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				TimeTool nDate = ddc.getDate();
				Fall fall = getFall();
				if (fall != null) {
					fall.setBillingDate(nDate);
				}
			}
		});
		tk.adapt(ddc);
		
		Composite separatorBar = new Composite(top, SWT.NONE);
		separatorBar.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginTop = -1;
		separatorBar.setLayout(gridLayout);
		Label lbReq = new Label(separatorBar, SWT.NONE);
		lbReq.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		lbReq.setText(Messages.Leistungscodes_necessaryData);
		
		btnCopyForPatient = new Button(top, SWT.CHECK);
		btnCopyForPatient.setText(Messages.FallDetailBlatt2_CopyToPatient);
		btnCopyForPatient.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				boolean b = btnCopyForPatient.getSelection();
				getFall().setCopyForPatient(b);
			};
		});
		new Label(top, SWT.NONE);
		
		hlGarant = tk.createHyperlink(top, RECHNUNGSEMPFAENGER, SWT.NONE);
		hlGarant.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(final HyperlinkEvent e){
				KontaktSelektor ksl =
					new KontaktSelektor(getShell(), Kontakt.class,
						Messages.FallDetailBlatt2_SelectGuarantorCaption, //$NON-NLS-1$
						Messages.FallDetailBlatt2_SelectGuarantorBody, true, Kontakt.DEFAULT_SORT); //$NON-NLS-1$
				if (ksl.open() == Dialog.OK) {
					Kontakt sel = (Kontakt) ksl.getSelection();
					Fall fall = getFall();
					if (fall != null) {
						fall.setGarant(sel);
						setFall(fall);
						ElexisEventDispatcher.fireSelectionEvent(fall.getPatient());
					}
				}
			}
		});
		
		tGarant = tk.createText(top, StringTool.leer);
		
		tGarant.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tk.paintBordersFor(top);
		setFall(getFall());
	}
	
	/**
	 * reload the billing systems menu (user dependent) and ensure that the right item is still
	 * selected
	 */
	public void reloadBillingSystemsMenu(){
		Abrechnungstypen = UserCasePreferences.sortBillingSystems(Fall.getAbrechnungsSysteme());
		String currItem = cAbrechnung.getText();
		cAbrechnung.setItems(Abrechnungstypen);
		cAbrechnung.setText(currItem);
	}
	
	class TristateSelection implements SelectionListener {
		TristateSelection(){}
		
		/**
		 * changing state: empty -> checked -> unchecked (cycling through)
		 */
		@Override
		public void widgetSelected(SelectionEvent e){
			Button button = ((Button) e.getSource());
			boolean selection = !button.getSelection();
			boolean grayed = button.getGrayed();
			if (selection) {
				if (grayed) {
					button.setSelection(true);
					button.setGrayed(false);
				} else {
					button.setSelection(false);
					button.setGrayed(false);
				}
			} else {
				button.setSelection(true);
				button.setGrayed(true);
			}
		}
		
		@Override
		public void widgetDefaultSelected(SelectionEvent e){}
		
	}
	
	class Focusreact implements FocusListener {
		
		private final String field;
		
		Focusreact(final String dbField){
			field = dbField;
		}
		
		public void focusGained(final FocusEvent e){ /* nichts */
		}
		
		public void focusLost(final FocusEvent e){
			String newval = StringTool.leer;
			String widgetType = e.widget.getClass().getName();
			widgetType = widgetType.split("\\.")[widgetType.split("\\.").length - 1]; //$NON-NLS-1$ //$NON-NLS-2$
			if (widgetType.equalsIgnoreCase("StyledText")) { //$NON-NLS-1$
				Object plugin = ((TextContainer) (e.getSource())).getPlugin();
				newval = ((ITextPlugin) plugin).storeToByteArray().toString();
			} else if (widgetType.equalsIgnoreCase("Combo")) { //$NON-NLS-1$
				String kind = (String) ((Combo) e.getSource()).getData("kind"); //$NON-NLS-1$
				if (kind.equalsIgnoreCase("S")) { //$NON-NLS-1$
					newval = ((Combo) e.getSource()).getText(); // save as
					// string
				} else {
					newval = StringTool.leer + ((Combo) e.getSource()).getSelectionIndex(); // save
					// as
					// index
					// in
					// combo
				}
			} else if (widgetType.equalsIgnoreCase("List")) { //$NON-NLS-1$
				int[] selection =
					((org.eclipse.swt.widgets.List) e.getSource()).getSelectionIndices();
				String delim = StringTool.leer;
				String kind =
					(String) ((org.eclipse.swt.widgets.List) e.getSource()).getData("kind"); //$NON-NLS-1$
				if (kind.equalsIgnoreCase("S")) { // save as string list, tab delimited   //$NON-NLS-1$
					for (int ii = 0; ii < selection.length; ii++) {
						newval =
							newval
								+ delim
								+ ((org.eclipse.swt.widgets.List) e.getSource())
									.getItem(selection[ii]);
						delim = ITEMDELIMITER;
					}
				} else { // save as numeric index list, tab delimited
					for (int ii = 0; ii < selection.length; ii++) {
						newval = newval + delim + selection[ii];
						delim = ITEMDELIMITER;
					}
				}
			} else if (widgetType.equalsIgnoreCase("Button")) { //$NON-NLS-1$
				Button button = ((Button) e.getSource());
				if ((button.getStyle() & SWT.RADIO) != 0) {
					String kind = (String) button.getData("kind"); //$NON-NLS-1$
					if (kind.equalsIgnoreCase("S")) { // save as string    //$NON-NLS-1$
						if (button.getSelection())
							newval = button.getText();
					} else { // save as numeric (index)
						if (button.getSelection()) {
							newval = button.getData("index").toString(); //$NON-NLS-1$
						}
					}
				}
				if ((button.getStyle() & SWT.CHECK) != 0) {
					boolean grayed = button.getGrayed();
					boolean selected = button.getSelection();
					if (selected) {
						if (grayed) {
							newval = StringTool.leer;
						} else {
							newval = "1"; //$NON-NLS-1$
						}
					} else {
						newval = "0"; //$NON-NLS-1$
					}
				}
			} else {
				newval = ((Text) e.getSource()).getText();
			}
			Fall fall = getFall();
			if (fall != null) {
				if (newval != null) {
					int savedCacheTime = Fall.getDefaultCacheLifetime();
					Fall.setDefaultCacheLifetime(0);
					fall.setInfoString(field, newval);
					ElexisEventDispatcher.update(fall);
					ElexisEventDispatcher.fireSelectionEvent(fall.getPatient());
					Fall.setDefaultCacheLifetime(savedCacheTime);
				}
			}
		}
		
	}
	
	/**
	 * disposes of required and optional fields on the bottom of the page.<br>
	 * re-reads the contents for this case for the upper part<br>
	 * recreates the required and optional fields on the bottom of the page (call to
	 * setExtendedFields)
	 * 
	 * @param f
	 *            caseID
	 */
	@SuppressWarnings("unchecked")
	public void setFall(final Fall f){
		// *** dispose of currently displayed fields
		actFall = f;
		for (Control c : lReqs) {
			c.dispose();
		}
		lReqs.clear();
		keepEditable.clear();
		
		boolean allowFieldUpdate = true;
		if (actFall != null) {
			Query<Rechnung> rQuery = new Query<Rechnung>(Rechnung.class);
			rQuery.add(Rechnung.CASE_ID, Query.EQUALS, actFall.getId());
			List<Rechnung> billMatch = rQuery.execute();
			
			if (billMatch != null && !billMatch.isEmpty()) {
				allowFieldUpdate = false;
			}
		}
		
		// *** fill billing systems into combo, set current system
		cAbrechnung.setItems(Abrechnungstypen);
		if (f == null) {
			form.setText(Messages.FallDetailBlatt2_NoCaseSelected); //$NON-NLS-1$
			tBezeichnung.setText(Messages.FallDetailBlatt2_29); //$NON-NLS-1$
			cReason.select(0);
			return;
		}
		
		form.setText(f.getLabel());
		
		// *** set Fallbezeichnung
		tBezeichnung.setText(f.getBezeichnung());
		
		// *** set Grund (Krankheit/Unfall/...)
		String grund = f.getGrund();
		int ix = cReason.indexOf(grund);
		if (ix == -1) {
			ix = 0;
		}
		cReason.select(ix);
		String abr = f.getAbrechnungsSystem();
		cAbrechnung.setText(abr);
		
		// *** set startDate/EndDate
		TimeTool tt = new TimeTool();
		if (tt.set(f.getBeginnDatum()) == true) {
			dpVon.setDate(tt.getTime());
		} else {
			dpVon.setDate(null);
		}
		if (tt.set(f.getEndDatum()) == true) {
			dpBis.setDate(tt.getTime());
		} else {
			dpBis.setDate(null);
		}
		
		// *** set copy for patient
		btnCopyForPatient.setSelection(f.getCopyForPatient());
		
		// *** set Garant
		tGarant.setText(f.getGarant().getLabel());
		
		// *** adding required fields defined in prefs
		String reqs = f.getRequirements();
		if ((reqs != null) && (reqs.length() > 0)) {
			// *** do not display a title bar since this is already displayed
			// above Rechnungsempfänger!
			setExtendedFields(f, reqs, StringTool.leer, false, false, false);
		}
		// *** adding optional fields defined in prefs
		String optionals = f.getOptionals();
		if ((optionals != null) && (optionals.length() > 0)) {
			setExtendedFields(f, optionals, Messages.FallDetailBlatt2_optionalData, false, false,
				true); //$NON-NLS-1$
		}
		
		// ****** show any other fields from extinfo - ONLY FOR ADMINS, NOT
		// INTENDED FOR NORMAL USERS !!!
		// first part fields with definition, second part without definition
		
		// *** display all unused field having a display specification
		String[] reqsArray = f.getRequirements().split(DEFINITIONSDELIMITER);
		for (int reqI = 0; reqI < reqsArray.length; reqI++) {
			reqsArray[reqI] = reqsArray[reqI].split(ARGUMENTSSDELIMITER)[0];
		}
		String[] optsArray = f.getOptionals().split(DEFINITIONSDELIMITER);
		for (int reqI = 0; reqI < optsArray.length; reqI++) {
			optsArray[reqI] = optsArray[reqI].split(ARGUMENTSSDELIMITER)[0];
		}
		// *** read field definitions for unused fields (previously required or
		// optional)
		List<String> unused = new ArrayList<String>();
		LinkedHashMap<String, String> unusedHash = new LinkedHashMap<String, String>();
		String strUnused = f.getUnused();
		if ((strUnused != null) && (!strUnused.isEmpty())) {
			String[] allUnused = strUnused.split(DEFINITIONSDELIMITER); //$NON-NLS-1$
			Arrays.sort(allUnused, String.CASE_INSENSITIVE_ORDER); // *** sort
			// alphabetically
			for (String unusedPart : allUnused) {
				int posColon = unusedPart.indexOf(ARGUMENTSSDELIMITER);
				String key = unusedPart.substring(0, posColon);
				// *** do not show if already displayed in required or optional
				// fields
				boolean alreadyDisplayed = false;
				for (int reqI = 0; reqI < reqsArray.length; reqI++) {
					if (key.equalsIgnoreCase(reqsArray[reqI])) {
						alreadyDisplayed = true;
					}
				}
				for (int reqI = 0; reqI < optsArray.length; reqI++) {
					if (key.equalsIgnoreCase(optsArray[reqI])) {
						alreadyDisplayed = true;
					}
				}
				if (!alreadyDisplayed) {
					String value = unusedPart.substring(posColon + 1);
					unusedHash.put(key, value);
					unused.add(unusedPart);
				}
			}
		}
		
		Map<String, String> httmp = getFall().getMap(PersistentObject.FLD_EXTINFO);
		
		HashMap<String, String> ht = new HashMap<String, String>(httmp);
		
		String[] unusedHashStringArray = {};
		if (unusedHash.size() > 0) {
			String unusedHashString = unusedHash.toString();
			unusedHashString = unusedHashString.substring(1);
			unusedHashString = unusedHashString.substring(0, unusedHashString.length() - 1);
			unusedHashStringArray = unusedHashString.split(", "); //$NON-NLS-1$
		}
		String otherFieldsList_2 = StringTool.leer;
		String delim = StringTool.leer;
		for (int uhi = 0; uhi < unusedHashStringArray.length; uhi++) {
			String unusedItem = unusedHashStringArray[uhi];
			String[] itemParts = unusedItem.split("="); //$NON-NLS-1$
			String controlName = itemParts[0];
			String[] controlDefParts = itemParts[1].split(ARGUMENTSSDELIMITER);
			String controlType = controlDefParts[0];
			
			String[] itemList = {
				StringTool.leer
			};
			if (controlType.equalsIgnoreCase("X")) { //$NON-NLS-1$
				if (controlDefParts.length > 1) {
					itemList = controlDefParts[1].split(ITEMDELIMITER);
				}
			}
			boolean isAdded = false;
			// *** special handling if multiple items
			for (int ili = 0; ili < itemList.length; ili++) {
				String item = itemList[ili];
				if (!item.isEmpty()) {
					item = "_" + item; //$NON-NLS-1$
				}
				String combControlName = controlName + item;
				if (ht.containsKey(combControlName)) {
					ht.remove(combControlName);
					String values = StringTool.leer;
					if (controlDefParts.length > 1) {
						values = controlDefParts[1];
					}
					if (!isAdded) {
						otherFieldsList_2 =
							otherFieldsList_2 + delim + controlName + ARGUMENTSSDELIMITER
								+ controlType + ARGUMENTSSDELIMITER + values;
					}
					delim = DEFINITIONSDELIMITER;
					isAdded = true;
				}
			}
		}
		// *** only for admins!
		if (otherFieldsList_2.length() > 0) {
			if (CoreHub.acl.request(AccessControlDefaults.CASE_MODIFY_SPECIALS) == true) {
				setExtendedFields(f, otherFieldsList_2,
					Messages.FallDetailBlatt2_unusedFieldsWithDefinition, true, true, false); //$NON-NLS-1$
			}
		}
		
		// *** collect all other fields that are not yet shown anywhere else,
		// display as text
		String otherFieldsList = otherFieldsList_2;
		otherFieldsList = StringTool.leer;
		Set<String> keySet = ht.keySet();
		Object[] arr = keySet.toArray();
		for (int i = 0; i < arr.length; i++) {
			String subkey = (String) arr[i];
			String abrSystem = getFall().getAbrechnungsSystem();
			String key = Preferences.LEISTUNGSCODES_CFG_KEY + "/" + abrSystem; //$NON-NLS-1$
			String bed = CoreHub.globalCfg.get(key + "/bedingungen", StringTool.leer); //$NON-NLS-1$
			boolean isAlreadyShown = false;
			if (subkey.equalsIgnoreCase(Fall.FLD_BILLING))
				isAlreadyShown = true;
			// if (subkey.equalsIgnoreCase("payment")) isAlreadyShown = true;
			// if (subkey.equalsIgnoreCase("Fallnummer")) isAlreadyShown = true;
			// if (subkey.equalsIgnoreCase("Gesetz")) isAlreadyShown = true;
			String[] bedArr = bed.split(DEFINITIONSDELIMITER);
			if (!bed.isEmpty()) {
				for (int ii = 0; ii < bedArr.length; ii++) {
					String fldParts = bedArr[ii];
					String[] flds = fldParts.split(ARGUMENTSSDELIMITER);
					String fld = flds[0];
					if ((flds[1].equalsIgnoreCase("X")) && ((flds.length > 2)) && (!flds[2].isEmpty())) { //$NON-NLS-1$
						String checkBoxes = flds[2];
						String[] checkBoxArray = checkBoxes.split(ITEMDELIMITER);
						for (int cb_i = 0; cb_i < checkBoxArray.length; cb_i++) {
							if ((fld + "_" + checkBoxArray[cb_i]).equalsIgnoreCase(subkey)) { //$NON-NLS-1$
								isAlreadyShown = true;
								break;
							}
						}
					} else {
						if (fld.equalsIgnoreCase(subkey)) {
							isAlreadyShown = true;
							break;
						}
					}
				}
			}
			String opt = CoreHub.globalCfg.get(key + "/fakultativ", StringTool.leer); //$NON-NLS-1$
			if (!isAlreadyShown) {
				String[] optArr = opt.split(DEFINITIONSDELIMITER);
				if (!opt.isEmpty()) {
					for (int ii = 0; ii < optArr.length; ii++) {
						String fld = optArr[ii].split(ARGUMENTSSDELIMITER)[0];
						if (fld.equalsIgnoreCase(subkey)) {
							isAlreadyShown = true;
							break;
						}
					}
				}
			}
			if (!isAlreadyShown) {
				if (unusedHash.containsKey(subkey)) {
					// *** try to find def
					String theVal = (String) unusedHash.get(subkey);
					String[] vals = theVal.split(ARGUMENTSSDELIMITER);
					otherFieldsList =
						otherFieldsList + delim + subkey + ARGUMENTSSDELIMITER + vals[0];
					if (vals.length > 1) {
						otherFieldsList = otherFieldsList + ARGUMENTSSDELIMITER + vals[1];
					}
				} else {
					// *** if no spec found, then show as text
					otherFieldsList = otherFieldsList + delim + subkey + ":T"; //$NON-NLS-1$
				}
				delim = DEFINITIONSDELIMITER;
			}
		}
		
		if (otherFieldsList.length() > 0) {
			// *** want to sort alphabetically here
			String[] tmpArr = otherFieldsList.split(DEFINITIONSDELIMITER);
			Arrays.sort(tmpArr, String.CASE_INSENSITIVE_ORDER);
			otherFieldsList = StringTool.leer;
			String tmpDel = StringTool.leer;
			for (int i = 0; i < tmpArr.length; i++) {
				otherFieldsList = otherFieldsList + tmpDel + tmpArr[i];
				tmpDel = DEFINITIONSDELIMITER;
			}
			// *** only for admins!
			if (CoreHub.acl.request(AccessControlDefaults.CASE_MODIFY_SPECIALS) == true) {
				setExtendedFields(f, otherFieldsList,
					Messages.FallDetailBlatt2_unusedFieldsWithoutDefinition, true, true, false); //$NON-NLS-1$
			}
		}
		allowFieldUpdate(allowFieldUpdate);
	}
	
	private void allowFieldUpdate(boolean enable){
		cAbrechnung.setEnabled(enable);
		cReason.setEnabled(enable);
		hlGarant.setEnabled(enable);
		tGarant.setEnabled(enable);
		autoFill.setEnabled(enable);
		
		for (Control req : lReqs) {
			if (req instanceof Label) {
				continue;
			}
			
			// keep editable in case it's an optional parameter of accident date/no
			if (keepEditable.contains(req)) {
				req.setEnabled(true);
			} else {
				req.setEnabled(enable);
			}
		}
	}
	
	/**
	 * creates the required or optional fields on the bottom of the page and fills them with the
	 * appropriate values.
	 * 
	 * @param f
	 *            caseID
	 * @param fieldList
	 *            this is the fieldList from the prefs for required OR optional fields
	 * @param TitleBarText
	 *            if not "" and not null then show a bar-separator on top of the fields
	 * @param deletable
	 *            is this deletable? - show delete button
	 *            <p>
	 *            The fieldList param contains a ;-separated list of fieldName:fieldType[:items]
	 *            pairs/triplets. the first item is the name for the field used in the database
	 *            (extInfo-Field) <br>
	 *            the second item is the fieldType <br>
	 *            the third item is the tab-delimited itemList to be used for combos, lists, radios
	 *            and checkboxes
	 *            <p>
	 *            fieldType is one of the following:
	 *            <ul>
	 *            <li>
	 *            T - Text</li>
	 *            <li>
	 *            D - Date</li>
	 *            <li>
	 *            K - Kontakt</li>
	 *            <li>
	 * 
	 *            TM - Text Multiline Text</li>
	 *            <li>
	 *            TS - Text Styled (still in development)</li>
	 *            <li>
	 *            CS - Combo saved as string</li>
	 *            <li>
	 *            CN - Combo saved as numeric (selected index)</li>
	 *            <li>
	 *            LS - List items saved as strings, tab-delimited</li>
	 *            <li>
	 *            LN - List items saved as numerics, tab-delimited (selected indexes)</li>
	 *            <li>
	 *            X - CheckBox, always saved as 0 or 1</li>
	 *            <li>
	 *            RS - Radios</li>
	 *            <li>
	 *            RN - Radios saved as numeric (selected index)</li>
	 *            </ul>
	 */
	private void setExtendedFields(final Fall f, final String fieldList, String TitleBarText,
		boolean deletable, boolean dangerous, final boolean optional){
		// *** kind "numeric" or "string" is saved in the dataField of the
		// control
		
		// *** show separator bar (title bar) if requested
		if ((TitleBarText != null) && (!TitleBarText.isEmpty())) {
			Composite separatorBar = new Composite(form.getBody(), SWT.NONE);
			separatorBar.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
			Color dangerousColor = new Color(separatorBar.getDisplay(), 255, 150, 150);
			GridLayout gridLayout = new GridLayout(1, false);
			gridLayout.marginTop = -1;
			gridLayout.marginBottom = -1;
			if (dangerous)
				separatorBar.setBackground(dangerousColor);
			separatorBar.setLayout(gridLayout);
			lReqs.add(separatorBar);
			Label lbReq = new Label(separatorBar, SWT.NONE);
			lbReq.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
			lbReq.setText(TitleBarText);
			if (dangerous)
				lbReq.setBackground(dangerousColor);
			lReqs.add(lbReq);
		}
		// *** loop through field list, creating the controls
		for (String req : fieldList.split(DEFINITIONSDELIMITER)) {
			final String[] r = req.split(ARGUMENTSSDELIMITER);
			if (r.length < 2) { // *** needs at least fieldName and fieldType...
				continue;
			}
			
			// *** read the saved value from the db
			String val = f.getInfoString(r[0]);
			
			// *** create label or hyperlink for this field
			Hyperlink hl = null;
			if (r[1].equals("K")) { //$NON-NLS-1$  // *** Kontakt
				hl = tk.createHyperlink(form.getBody(), r[0], SWT.NONE);
				addControl(hl, optional, r[0]);
				
				if (!val.startsWith("**ERROR")) { //$NON-NLS-1$
					Kontakt k = Kontakt.load(val);
					val = k.getLabel();
				}
			} else {
				addControl(tk.createLabel(form.getBody(), r[0]), optional, r[0]);
			}
			
			// *** create/get parent for data part (right part)
			Composite subParent = createRightComposite(deletable);
			// lReqs.add(subParent);
			
			// *** create String List for combos, lists and checkboxes
			String[] items = itemsErrorMessage.split(";"); //$NON-NLS-1$
			if ((r[1].equals("CS")) //$NON-NLS-1$
				|| (r[1].equals("CN")) //$NON-NLS-1$
				|| (r[1].equals("LS")) //$NON-NLS-1$
				|| (r[1].equals("LN")) //$NON-NLS-1$
				|| (r[1].equals("RS")) //$NON-NLS-1$
				|| (r[1].equals("RN")) //$NON-NLS-1$
				|| (r[1].equals("X"))) { //$NON-NLS-1$
				if (r.length >= 4) { // *** must have an sql- or
					// script-statement
					if (r[2].equalsIgnoreCase("SQL")) { //$NON-NLS-1$
						// ITextPlugin plugin = null;
						// final Brief dummyBrief = new Brief("DummyBetreff",
						// null, null, null, null, "DummyBrief");
						// TextContainer textContainer = new TextContainer();
						// String tmp = (String)
						// textContainer.replaceSQLClause(dummyBrief, "SQL:" +
						// r[3]);
						String itemsStr = (String) TextContainer.replaceSQLClause("SQL:" + r[3]); //$NON-NLS-1$
						itemsStr = itemsStr.replaceAll("\r\n", ITEMDELIMITER); //$NON-NLS-1$
						itemsStr =
							(itemsStr.replaceAll("\n", ITEMDELIMITER)).replaceAll("\r", ITEMDELIMITER); //$NON-NLS-1$  //$NON-NLS-2$
						items = itemsStr.split(ITEMDELIMITER);
					}
				} else if (r.length >= 3) {
					if (r.length >= 3)
						items = r[2].split(ITEMDELIMITER);
				}
			}
			
			Control dataField = null;
			Composite stretchComposite = null;
			// *** create data field
			if (r[1].equals("T")) { //$NON-NLS-1$  // *** simple Text, single line
				dataField = tk.createText(subParent, val);
				dataField.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
				dataField.addFocusListener(new Focusreact(r[0]));
			} else if (r[1].equals("D")) { //$NON-NLS-1$  // *** Date
				final DatePickerCombo dp = new DatePickerCombo(subParent, SWT.NONE);
				TimeTool tt = new TimeTool();
				if (tt.set(val)) {
					dp.setDate(tt.getTime());
				}
				dp.setLayoutData(SWTHelper.getFillGridData(1, false, 1, false));
				dp.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e){
						TimeTool tt = new TimeTool(dp.getDate().getTime());
						f.setInfoString(r[0], tt.toString(TimeTool.DATE_GER));
					}
				});
				dataField = dp;
			} else if (r[1].equals("K")) { //$NON-NLS-1$  // *** Kontakt
				dataField = tk.createText(subParent, val);
				hl.addHyperlinkListener(new HyperlinkAdapter() {
					@Override
					public void linkActivated(final HyperlinkEvent e){
						KontaktSelektor ksl =
							new KontaktSelektor(getShell(), Kontakt.class, SELECT_CONTACT_CAPTION,
								MessageFormat.format(SELECT_CONTACT_BODY, new Object[] {
									r[0]
								}), true, Kontakt.DEFAULT_SORT);
						if (optional) {
							ksl.enableEmptyFieldButton();
						}
						// "Bitte wählen Sie den Kontakt für " + r[0] +
						// " aus", true);
						if (ksl.open() == Dialog.OK) {
							Kontakt sel = (Kontakt) ksl.getSelection();
							Fall fall = getFall();
							if (fall != null) {
								if (sel != null) {
									fall.setInfoString(r[0], sel.getId());
								} else {
									fall.setInfoString(r[0], StringTool.leer);
								}
								setFall(fall);
								ElexisEventDispatcher.fireSelectionEvent(fall.getPatient());
							}
						}
					}
				});
				((Text) dataField).setEditable(false);
				dataField.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			} else if (r[1].equals("TM")) { //$NON-NLS-1$  // *** multiline text
				Text multiText = new Text(subParent, SWT.MULTI + SWT.BORDER);
				multiText.setText(val);
				multiText.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
				multiText.addFocusListener(new Focusreact(r[0]));
				multiText.addKeyListener(new KeyListener() {
					@Override
					public void keyPressed(KeyEvent e){}
					
					@Override
					public void keyReleased(KeyEvent e){
						// *** this simply makes the field grow or shrink to the
						// needed size
						// and repositions the following controls, too
						Point sizePt = ((Text) (e.widget)).computeSize(SWT.DEFAULT, SWT.DEFAULT);
						((Text) (e.widget)).setSize(((Text) (e.widget)).getBounds().width, sizePt.y);
						form.getBody().layout(true);
					}
				});
				dataField = multiText;
			} else if (r[1].equals("TS")) { //$NON-NLS-1$
				dataField = tk.createLabel(subParent, "Styled Text disabled - still experimental"); //$NON-NLS-1$
				if (CoreHub.actUser.getLabel().equalsIgnoreCase("addsdfgss")) { // +++++ my testing  //$NON-NLS-1$
					// get the extension for styled text
					String extensionToUse = "Interner Text"; //$NON-NLS-1$
					IExtensionRegistry exr = Platform.getExtensionRegistry();
					IExtensionPoint exp =
						exr.getExtensionPoint(ExtensionPointConstantsUi.TEXTPROCESSINGPLUGIN);
					ITextPlugin plugin = null;
					if (exp != null) {
						IExtension[] extensions = exp.getExtensions();
						for (IExtension ex : extensions) {
							IConfigurationElement[] elems = ex.getConfigurationElements();
							for (IConfigurationElement el : elems) {
								if ((extensionToUse == null)
									|| el.getAttribute("name").equals(extensionToUse)) { //$NON-NLS-1$
									try {
										plugin =
											(ITextPlugin) el.createExecutableExtension("Klasse"); //$NON-NLS-1$
										plugin.setSaveOnFocusLost(true);
									} catch (Exception e) {
										ExHandler.handle(e);
									}
								}
							}
						}
					}
					if (plugin != null) {
						SaveCallback saveCallback = new SaveCallback();
						Composite textContainer = plugin.createContainer(subParent, saveCallback);
						// boolean ok =
						// ((ITextPlugin)plugin).loadFromByteArray(val.getBytes(),
						// false);
						
						// I can get the actual elexisEditor if really needed...
						Control[] children = textContainer.getChildren();
						Composite elexisEditor = null;
						for (int iii = 0; iii < children.length; iii++) {
							Control child = children[iii];
							if (child.getClass().getName()
								.equalsIgnoreCase("ch.elexis.textplugin.ElexisEditor")) { //$NON-NLS-1$
								elexisEditor = (Composite) child;
								elexisEditor.addFocusListener(new Focusreact(r[0]));
							}
						}
						Control[] children2 = elexisEditor.getChildren();
						Composite page = null;
						for (int iii = 0; iii < children2.length; iii++) {
							Control child = children2[iii];
							if (child.getClass().getName()
								.equalsIgnoreCase("org.eclipse.swt.custom.ScrolledComposite")) { //$NON-NLS-1$
								page = (Composite) child;
								page.addFocusListener(new Focusreact(r[0]));
							}
						}
						dataField = textContainer;
					}
				}
				/*
				 * // *** styled Text, variant onPositive RichTextViewer final RichTextViewer
				 * richTextViewer = new RichTextViewer(subParent, SWT.BORDER); GridData gridData =
				 * new GridData(GridData.FILL_BOTH); gridData.horizontalSpan = 1;
				 * //gridData.minimumWidth = 200; gridData.minimumHeight = 200;
				 * richTextViewer.getControl().setLayoutData(gridData); File myFile; try { myFile =
				 * File.createTempFile("RTV", ".html"); myFile.deleteOnExit(); FileOutputStream fout
				 * = new FileOutputStream(myFile); fout.write(val.getBytes()); fout.close();
				 * richTextViewer.getLayerManager().openHTMLFile(myFile .getPath()); } catch
				 * (IOException e) { e.printStackTrace(); }
				 * richTextViewer.addRichDocumentListener(new IRichDocumentListener(){ public void
				 * documentAboutToBeChanged(DocumentEvent event) { } public void
				 * documentChanged(DocumentEvent event, RichDocumentChange change) { String s
				 * =richTextViewer.getLayerManager().getSerializedString(); System.out.println(s);
				 * Fall fall = getFall(); if (fall != null) { fall.setInfoString(r[0], s);
				 * ElexisEventDispatcher.update(fall);
				 * ElexisEventDispatcher.fireSelectionEvent(fall.getPatient()); } } });
				 * //richTextViewer.getTextWidget().addFocusListener(new Focusreact(r[0]));
				 */
			} else if (r[1].equals("CS")) { //$NON-NLS-1$  // *** combo, selected value saved as selected string
				stretchComposite = new Composite(subParent, SWT.NONE);
				stretchComposite.setBackground(new Color(stretchComposite.getDisplay(), 255, 255,
					255));
				stretchComposite.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
				addControl(stretchComposite, optional, r[0]);
				GridLayout radioLayout = new GridLayout(1, false);
				radioLayout.marginWidth = 0;
				stretchComposite.setLayout(radioLayout);
				Combo combo = new Combo(stretchComposite, SWT.NONE);
				combo.setItems(items);
				combo.setText(val);
				combo.setData("kind", "S"); //$NON-NLS-1$ //$NON-NLS-2$
				combo.setLayoutData(SWTHelper.getFillGridData(1, false, 1, false));
				combo.addFocusListener(new Focusreact(r[0]));
				dataField = combo;
			} else if (r[1].equals("CN")) { //$NON-NLS-1$  // *** combo, selected value saved as selected index (zero-based)
				stretchComposite = new Composite(subParent, SWT.NONE);
				stretchComposite.setBackground(new Color(stretchComposite.getDisplay(), 255, 255,
					255));
				stretchComposite.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
				GridLayout radioLayout = new GridLayout(1, false);
				radioLayout.marginWidth = 0;
				stretchComposite.setLayout(radioLayout);
				Combo combo = new Combo(stretchComposite, SWT.NONE);
				combo.setItems(items);
				if (!val.isEmpty()) {
					try {
						combo.setText(items[Integer.parseInt(val)]);
					} catch (Exception e) {}
				}
				combo.setData("kind", "N"); //$NON-NLS-1$ //$NON-NLS-2$
				combo.setLayoutData(SWTHelper.getFillGridData(1, false, 1, false));
				combo.addFocusListener(new Focusreact(r[0]));
				dataField = combo;
			} else if (r[1].equals("LS")) { //$NON-NLS-1$  // *** selection list, selection saved as tab-delimited string-list
				stretchComposite = new Composite(subParent, SWT.NONE);
				stretchComposite.setBackground(new Color(stretchComposite.getDisplay(), 255, 255,
					255));
				stretchComposite.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
				GridLayout radioLayout = new GridLayout(items.length, false);
				radioLayout.marginWidth = 0;
				stretchComposite.setLayout(radioLayout);
				org.eclipse.swt.widgets.List list =
					new org.eclipse.swt.widgets.List(stretchComposite, SWT.BORDER | SWT.MULTI
						| SWT.V_SCROLL);
				String[] vals = val.split(ITEMDELIMITER);
				for (int lIx = 0; lIx < items.length; lIx++) {
					list.add(items[lIx]);
					for (int ii = 0; ii < vals.length; ii++) {
						if (vals[ii].equalsIgnoreCase(items[lIx])) {
							list.select(lIx);
						}
					}
				}
				list.setData("kind", "S"); //$NON-NLS-1$ //$NON-NLS-2$
				list.setLayoutData(SWTHelper.getFillGridData(1, false, 1, false));
				list.addFocusListener(new Focusreact(r[0]));
				dataField = list;
			} else if (r[1].equals("LN")) { //$NON-NLS-1$  // *** selection list numeric, selection saved as tab-delimited list of selected-item-nums
				stretchComposite = new Composite(subParent, SWT.NONE);
				stretchComposite.setBackground(new Color(stretchComposite.getDisplay(), 255, 255,
					255));
				stretchComposite.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
				GridLayout radioLayout = new GridLayout(items.length, false);
				radioLayout.marginWidth = 0;
				stretchComposite.setLayout(radioLayout);
				org.eclipse.swt.widgets.List list =
					new org.eclipse.swt.widgets.List(stretchComposite, SWT.BORDER | SWT.MULTI
						| SWT.V_SCROLL);
				String[] vals = val.split(ITEMDELIMITER);
				for (int lIx = 0; lIx < items.length; lIx++) {
					list.add(items[lIx]);
					for (int ii = 0; ii < vals.length; ii++) {
						if (vals[ii].equalsIgnoreCase(StringTool.leer + lIx)) {
							list.select(lIx);
						}
					}
				}
				list.setData("kind", "N"); //$NON-NLS-1$ //$NON-NLS-2$
				list.setLayoutData(SWTHelper.getFillGridData(1, false, 1, false));
				list.addFocusListener(new Focusreact(r[0]));
				dataField = list;
			} else if (r[1].equals("RS")) { // radio group //$NON-NLS-1$  // *** radio group, selection saved string
				Composite radioComposite = new Composite(subParent, SWT.NONE);
				radioComposite.setBackground(new Color(radioComposite.getDisplay(), 255, 255, 255));
				radioComposite.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
				GridLayout radioLayout = new GridLayout(items.length, false);
				radioLayout.marginWidth = 0;
				radioComposite.setLayout(radioLayout);
				Button[] radios = new Button[items.length];
				for (int rIx = 0; rIx < items.length; rIx++) {
					radios[rIx] = new Button(radioComposite, SWT.RADIO + SWT.NONE);
					radios[rIx].setBackground(new Color(radios[rIx].getDisplay(), 255, 255, 255));
					radios[rIx].setText(items[rIx]);
					radios[rIx].setData("index", rIx); //$NON-NLS-1$
					radios[rIx].setData("kind", "S"); //$NON-NLS-1$ //$NON-NLS-2$
					if (val.equalsIgnoreCase(items[rIx])) {
						radios[rIx].setSelection(true);
					} else {
						radios[rIx].setSelection(false);
					}
					radios[rIx].addFocusListener(new Focusreact(r[0]));
				}
				dataField = radioComposite;
			} else if (r[1].equals("RN")) { // radio group //$NON-NLS-1$  // *** radio group
				Composite radioComposite = new Composite(subParent, SWT.NONE);
				radioComposite.setBackground(new Color(radioComposite.getDisplay(), 255, 255, 255));
				radioComposite.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
				GridLayout radioLayout = new GridLayout(items.length, false);
				radioLayout.marginWidth = 0;
				radioComposite.setLayout(radioLayout);
				Button[] radios = new Button[items.length];
				for (int rIx = 0; rIx < items.length; rIx++) {
					radios[rIx] = new Button(radioComposite, SWT.RADIO + SWT.NONE);
					radios[rIx].setBackground(new Color(radios[rIx].getDisplay(), 255, 255, 255));
					radios[rIx].setText(items[rIx]);
					radios[rIx].setData("index", rIx); //$NON-NLS-1$
					radios[rIx].setData("kind", "N"); //$NON-NLS-1$ //$NON-NLS-2$
					if (val.equalsIgnoreCase(StringTool.leer + rIx)) {
						radios[rIx].setSelection(true);
					} else {
						radios[rIx].setSelection(false);
					}
					radios[rIx].addFocusListener(new Focusreact(r[0]));
				}
				dataField = radioComposite;
			} else if (r[1].equals("X")) { //$NON-NLS-1$  // *** checkBox, always saved as numeric - 0 or 1
				// if no items supplied: use fieldName r[0] as fieldName in the
				// database
				// if items supplied: use the supplied item names as fieldNames
				// in the database
				// in this case the fieldName r[0] is just used as a label on
				// the left side
				if (r.length < 3)
					items = StringTool.leer.split(ITEMDELIMITER);
				if (items.length > 1) {
					Composite checkBoxComposite = new Composite(subParent, SWT.NONE);
					checkBoxComposite.setBackground(new Color(checkBoxComposite.getDisplay(), 255,
						255, 255));
					checkBoxComposite.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
					GridLayout checkBoxLayout = new GridLayout(items.length, false);
					checkBoxLayout.marginWidth = 0;
					checkBoxComposite.setLayout(checkBoxLayout);
					Button[] checks = new Button[items.length];
					for (int rIx = 0; rIx < items.length; rIx++) {
						String val2 = f.getInfoString(r[0] + "_" + items[rIx]); //$NON-NLS-1$
						checks[rIx] = new Button(checkBoxComposite, SWT.CHECK + SWT.NONE);
						checks[rIx]
							.setBackground(new Color(checks[rIx].getDisplay(), 255, 255, 255));
						checks[rIx].setText(items[rIx]);
						if (val2.isEmpty()) {
							checks[rIx].setSelection(true);
							checks[rIx].setGrayed(true);
						} else if (val2.equalsIgnoreCase("0")) { //$NON-NLS-1$
							checks[rIx].setSelection(false);
							checks[rIx].setGrayed(false);
						} else {
							checks[rIx].setSelection(true);
							checks[rIx].setGrayed(false);
						}
						checks[rIx].addFocusListener(new Focusreact(r[0] + "_" + items[rIx])); //$NON-NLS-1$
						checks[rIx].addSelectionListener(new TristateSelection());
						addControl(checks[rIx], optional, r[0]);
					}
					dataField = checkBoxComposite;
				} else {
					Button check = new Button(subParent, SWT.CHECK + SWT.NONE);
					// *** support tristate
					if (val.isEmpty()) {
						check.setSelection(true);
						check.setGrayed(true);
					} else if (val.equalsIgnoreCase("0")) { //$NON-NLS-1$
						check.setSelection(false);
						check.setGrayed(false);
					} else {
						check.setSelection(true);
						check.setGrayed(false);
					}
					check.addFocusListener(new Focusreact(r[0]));
					check.addSelectionListener(new TristateSelection());
					check.setBackground(new Color(check.getDisplay(), 255, 255, 255));
					check.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
					dataField = check;
				}
			} else { // *** not supported...
				dataField =
					tk.createLabel(subParent,
						"FallDetailBlatt2().setExtendedFields(): field type not supported: " + r[1]); //$NON-NLS-1$
			}
			
			// *** add delete button if deletable
			addDeleteButton(deletable, subParent, r, f);
			
			// *** add stretchComposite and dataField to controlList
			if (stretchComposite != null) {
				addControl(stretchComposite, optional, r[0]);
			}
			addControl(dataField, optional, r[0]);
		}
		
		TimeTool bt = f.getBillingDate();
		ddc.setDates(bt);
		form.reflow(true);
		form.redraw();
	}
	
	/**
	 * create the composite on the right side with two columns
	 * 
	 * @return
	 */
	protected Composite createRightComposite(boolean deletable){
		// *** if deletable, then just use form.getBody()
		if (deletable) {
			Composite tmpComp = new Composite(form.getBody(), SWT.NONE);
			GridLayout tmpGridLayout = new GridLayout(2, false);
			tmpGridLayout.marginWidth = 0;
			tmpGridLayout.marginTop = -7;
			tmpGridLayout.marginBottom = -7;
			tmpGridLayout.marginTop = -5;
			tmpGridLayout.marginBottom = -5;
			tmpComp.setBackground(new Color(tmpComp.getDisplay(), 255, 255, 255));
			tmpComp.setLayout(tmpGridLayout);
			tmpComp.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			return tmpComp;
		} else {
			return form.getBody();
		}
	}
	
	protected void addDeleteButton(boolean deletable, Composite parent, String[] r, final Fall f){
		if (deletable) {
			Button tmpButton = new Button(parent, SWT.NONE);
			tmpButton.setText(Messages.FallDetailBlatt2_deleteData); //$NON-NLS-1$
			tmpButton.setData("KeyForDataToBeDeleted_Marlovits", r[0]); //$NON-NLS-1$
			tmpButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e){
					String key = (String) e.widget.getData("KeyForDataToBeDeleted_Marlovits"); //$NON-NLS-1$
					int savedCacheTime = Fall.getDefaultCacheLifetime();
					try {
						Fall.setDefaultCacheLifetime(0);
						@SuppressWarnings("unchecked")
						Map<Object, Object> ht = f.getMap("extinfo"); //$NON-NLS-1$
						if (SWTHelper.askYesNo(
							StringTool.leer,
							Messages.FallDetailBlatt2_DoYouWantToDeleteThisData + key
								+ "/" + ht.get(key) + Messages.FallDetailBlatt2_reallyFromTheCase)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							ht.remove(key);
							f.setMap("extinfo", ht); //$NON-NLS-1$
							setFall(f);
						}
					} finally {
						Fall.setDefaultCacheLifetime(savedCacheTime);
					}
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e){}
			});
			lReqs.add(tmpButton);
			lReqs.add(parent);
		}
	}
	
	private boolean addControl(Control control, boolean optional, String value){
		String accidentNo = "Unfallnummer";
		String accidentDate = "Unfalldatum";
		
		if (optional || accidentNo.equalsIgnoreCase(value) || accidentDate.equalsIgnoreCase(value)) {
			keepEditable.add(control);
		}
		return lReqs.add(control);
	}
	
	public Fall getFall(){
		if (actFall == null) {
			actFall = (Fall) ElexisEventDispatcher.getSelected(Fall.class);
		}
		return actFall;
	}
	
	public class SaveCallback implements ICallback {
		@Override
		public void save(){}
		
		@Override
		public boolean saveAs(){
			return true;
		}
	}
}
