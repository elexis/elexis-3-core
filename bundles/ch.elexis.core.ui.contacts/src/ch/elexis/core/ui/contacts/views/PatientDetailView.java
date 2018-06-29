/*******************************************************************************
Ø * Copyright (c) 2012 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.contacts.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.contacts.dialogs.BezugsKontaktAuswahl;
import ch.elexis.core.ui.dialogs.KontaktDetailDialog;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.dialogs.ZusatzAdresseEingabeDialog;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.locks.IUnlockable;
import ch.elexis.core.ui.locks.ToggleCurrentPatientLockHandler;
import ch.elexis.core.ui.medication.views.FixMediDisplay;
import ch.elexis.core.ui.util.ListDisplay;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.WidgetFactory;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.core.ui.views.controls.ClientCustomTextComposite;
import ch.elexis.core.ui.views.controls.StickerComposite;
import ch.elexis.data.BezugsKontakt;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.elexis.data.ZusatzAdresse;
import ch.rgw.tools.StringTool;

public class PatientDetailView extends ViewPart implements IUnlockable, IActivationListener {

	public static final String ID = "at.medevit.elexis.contacts.views.PatientDetail"; //$NON-NLS-1$

	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());

	private final static String FIXMEDIKATION = Messages.Patientenblatt2_fixmedication;
	private static final String KEY_PATIENTENBLATT = "PatientenDetailView/"; //$NON-NLS-1$

	private ScrolledForm scrldfrm;
	private ViewMenus viewmenu;
	private Text txtDiagnosen;
	private Text txtAnamnese;
	private Text txtFamAnamnese;
	private Text txtAllergien;
	private Text txtRisiken;
	private Text txtBemerkungen;
	private ClientCustomTextComposite compClientCustomText;
	private StickerComposite stickerComposite;
	private IAction removeZAAction, showZAAction, showBKAction, removeAdditionalAddressAction,
			showAdditionalAddressAction;
	private ListDisplay<BezugsKontakt> inpZusatzAdresse;
	private ListDisplay<ZusatzAdresse> additionalAddresses;
	private IObservableValue patientObservable = new WritableValue(null, Patient.class);
	private boolean bLocked = true;
	
	private ElexisEventListener eeli_pat = new ElexisUiEventListenerImpl(Patient.class) {
		public void runInUi(ElexisEvent ev) {
			Patient pat = (Patient) ev.getObject();

			switch (ev.getType()) {
			case ElexisEvent.EVENT_SELECTED:
				IPersistentObject deselected = (IPersistentObject) patientObservable.getValue();
				setPatient(pat);
				releaseAndRefreshLock(deselected, ToggleCurrentPatientLockHandler.COMMAND_ID);
				break;
			case ElexisEvent.EVENT_LOCK_AQUIRED:
			case ElexisEvent.EVENT_LOCK_RELEASED:
				if (pat.equals(patientObservable.getValue())) {
					setUnlocked(ev.getType() == ElexisEvent.EVENT_LOCK_AQUIRED);
				}
				break;
			default:
				break;
			}
		}
	};

	private void releaseAndRefreshLock(IPersistentObject object, String commandId){
		if (object != null && CoreHub.getLocalLockService().isLockedLocal(object)) {
			CoreHub.getLocalLockService().releaseLock(object);
		}
		ICommandService commandService =
			(ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		commandService.refreshElements(commandId, null);
	}
	
	private FixMediDisplay dmd;

	private DataBindingContext bindingContext;
	
	public PatientDetailView() {
		toolkit.setBorderStyle(SWT.NULL); // Deactivate borders for the widgets
		makeActions();
		ElexisEventDispatcher.getInstance().addListeners(eeli_pat);
	}

	@Override
	public void setUnlocked(boolean unlocked) {
		bLocked = !unlocked;
		txtDiagnosen.setEditable(unlocked);
		txtAnamnese.setEditable(unlocked);
		txtFamAnamnese.setEditable(unlocked);
		txtAllergien.setEditable(unlocked);
		txtRisiken.setEditable(unlocked);
		txtBemerkungen.setEditable(unlocked);
		dmd.setUnlocked(unlocked);
		inpZusatzAdresse.setUnlocked(unlocked);
		additionalAddresses.setUnlocked(unlocked);
		removeAdditionalAddressAction.setEnabled(unlocked);
		removeZAAction.setEnabled(unlocked);
	}

	void setPatient(Patient p) {
		patientObservable.setValue(p);
		scrldfrm.setText(StringTool.unNull(p.getName()) + StringConstants.SPACE + StringTool.unNull(p.getVorname())
				+ " (" + p.getPatCode() + ")");
		compClientCustomText.updateClientCustomArea();
		stickerComposite.setPatient(p);
		inpZusatzAdresse.clear();
		for (BezugsKontakt za : p.getBezugsKontakte()) {
			inpZusatzAdresse.add(za);
		}
		additionalAddresses.clear();
		for (ZusatzAdresse zusatzAdresse : p.getZusatzAdressen()) {
			additionalAddresses.add(zusatzAdresse);
		}
		dmd.reload();
		scrldfrm.reflow(true);

		setUnlocked(CoreHub.getLocalLockService().isLockedLocal(p));
	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		// getSite().getPage().addSelectionListener(patientSelectionListener);

		parent.setLayout(new GridLayout(1, false));

		scrldfrm = toolkit.createScrolledForm(parent);
		scrldfrm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		TableWrapLayout td = new TableWrapLayout();
		scrldfrm.setText(Messages.Patientenblatt2_noPatientSelected);
		scrldfrm.getBody().setLayout(td);
		Composite scrldfrmComposite = scrldfrm.getBody();

		// custom area for user
		{
			compClientCustomText = new ClientCustomTextComposite(scrldfrm.getBody(), SWT.None, toolkit, scrldfrm);
		}

		// sticker composite
		{
			stickerComposite = new StickerComposite(scrldfrm.getBody(), SWT.NONE, toolkit);
		}

		// bezugs kontakte
		{
			ExpandableComposite ecZA = WidgetFactory.createExpandableComposite(toolkit, scrldfrm,
					Messages.Patientenblatt2_additionalAdresses); // $NON-NLS-1$
			ecZA.setExpanded(CoreHub.localCfg.get(KEY_PATIENTENBLATT + ecZA.getText(), false));
			ecZA.addExpansionListener(new SectionExpansionHandler());
			inpZusatzAdresse = new ListDisplay<BezugsKontakt>(ecZA, SWT.NONE, new ListDisplay.LDListener() {
				/*
				 * public boolean dropped(final PersistentObject dropped) {
				 * return false; }
				 */

				public void hyperlinkActivated(final String l) {
					Patient sp = ElexisEventDispatcher.getSelectedPatient();
					if (sp == null) {
						return;
					}

					final String[] sortFields = new String[] { Kontakt.FLD_NAME1, Kontakt.FLD_NAME2,
							Kontakt.FLD_STREET };
					KontaktSelektor ksl = new KontaktSelektor(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
							Kontakt.class, Messages.Patientenblatt2_contactForAdditionalAddress,
							Messages.Patientenblatt2_pleaseSelectardress, sortFields); // $NON-NLS-1$
																						// //$NON-NLS-2$
					if (ksl.open() == Dialog.OK) {
						Kontakt k = (Kontakt) ksl.getSelection();
							if (k != null) {
							BezugsKontaktAuswahl bza =
									new BezugsKontaktAuswahl(sp.getLabel(true),
										k.istPerson() ? Person.load(k.getId()).getLabel(true)
												: k.getLabel(true));
								if (bza.open() == Dialog.OK && sp != null) {
									BezugsKontakt bk =
										sp.addBezugsKontakt(k, bza.getBezugKonkaktRelation());
									inpZusatzAdresse.add(bk);
									scrldfrm.reflow(true);
								}
						}
					}
				}

				public String getLabel(Object o) {
					BezugsKontakt bezugsKontakt = (BezugsKontakt) o;

					StringBuffer sb = new StringBuffer();
					sb.append(bezugsKontakt.getLabel());

					Kontakt other = Kontakt.load(bezugsKontakt.get(BezugsKontakt.OTHER_ID));
					if (other.exists()) {
						List<String> tokens = new ArrayList<String>();

						String telefon1 = other.get(Kontakt.FLD_PHONE1);
						String telefon2 = other.get(Kontakt.FLD_PHONE2);
						String mobile = other.get(Kontakt.FLD_MOBILEPHONE);
						String eMail = other.get(Kontakt.FLD_E_MAIL);
						String fax = other.get(Kontakt.FLD_FAX);

						if (!StringTool.isNothing(telefon1)) {
							tokens.add("T1: " + telefon1); //$NON-NLS-1$
						}
						if (!StringTool.isNothing(telefon2)) {
							tokens.add("T2: " + telefon2); //$NON-NLS-1$
						}
						if (!StringTool.isNothing(mobile)) {
							tokens.add("M: " + mobile); //$NON-NLS-1$
						}
						if (!StringTool.isNothing(fax)) {
							tokens.add("F: " + fax); //$NON-NLS-1$
						}
						if (!StringTool.isNothing(eMail)) {
							tokens.add(eMail);
						}
						for (String token : tokens) {
							sb.append(", "); //$NON-NLS-1$
							sb.append(token);
						}
						return sb.toString();
					}
					return "?"; //$NON-NLS-1$
				}
			});
			inpZusatzAdresse.addHyperlinks(Messages.Patientenblatt2_add); // $NON-NLS-1$
			// inpZusatzAdresse.setMenu(createZusatzAdressMenu());
			inpZusatzAdresse.setMenu(removeZAAction, showZAAction, showBKAction);

			ecZA.setClient(inpZusatzAdresse);
		}
		
		// zusatz adressen
		{
			ExpandableComposite compAdditionalAddresses =
				WidgetFactory.createExpandableComposite(toolkit, scrldfrm,
					Messages.Patientenblatt2_additionalAdresses);
			compAdditionalAddresses.addExpansionListener(new SectionExpansionHandler());
			
			additionalAddresses = new ListDisplay<ZusatzAdresse>(compAdditionalAddresses, SWT.NONE,
				new ListDisplay.LDListener() {
					public void hyperlinkActivated(final String l){
						Patient actPatient = ElexisEventDispatcher.getSelectedPatient();
						if (actPatient != null) {
							ZusatzAdresseEingabeDialog aed =
								new ZusatzAdresseEingabeDialog(
									PlatformUI.getWorkbench().getDisplay().getActiveShell(),
									actPatient);
							if (aed.open() == Dialog.OK) {
								additionalAddresses.add(aed.getZusatzAdresse());
								scrldfrm.reflow(true);
							}
						}
					}
					
					public String getLabel(Object o){
						ZusatzAdresse address = (ZusatzAdresse) o;
						if (address != null) {
							return address.getLabel();
						}
						return "?"; //$NON-NLS-1$
					}
				});
			
			// Hyperlink "Hinzu..." über der Adressliste hinzufügen
			additionalAddresses.addHyperlinks(Messages.Patientenblatt2_add); // $NON-NLS-1$
			
			// Das Kontext-Menü jedes Eintrags in der Adressliste erzeugen
			
			// inpZusatzAdresse.setMenu(createZusatzAdressMenu());
			makeAdditionalAddressActions();
			additionalAddresses.setMenu(removeAdditionalAddressAction, showAdditionalAddressAction);
			
			compAdditionalAddresses.setClient(additionalAddresses);
		}

		// diagnosis
		{
			Section sectDiagnosen = toolkit.createSection(scrldfrmComposite, Section.EXPANDED | Section.TWISTIE);
			TableWrapData twd_sectDiagnosen = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 1);
			twd_sectDiagnosen.grabHorizontal = true;
			twd_sectDiagnosen.valign = TableWrapData.FILL;
			twd_sectDiagnosen.align = TableWrapData.FILL;
			sectDiagnosen.setLayoutData(twd_sectDiagnosen);
			sectDiagnosen.setText("Diagnosen");
			sectDiagnosen.setExpanded(CoreHub.localCfg.get(KEY_PATIENTENBLATT + sectDiagnosen.getText(), false));

			txtDiagnosen = toolkit.createText(sectDiagnosen, "", SWT.WRAP | SWT.MULTI);
			txtDiagnosen.addListener(SWT.Modify, new MultiLineAutoGrowListener(txtDiagnosen));

			sectDiagnosen.setClient(txtDiagnosen);
			sectDiagnosen.addExpansionListener(new SectionExpansionHandler());
		}

		// personal anamnesis
		{
			Section sectAnamnese = toolkit.createSection(scrldfrmComposite, Section.EXPANDED | Section.TWISTIE);
			TableWrapData twd_sectDiagnosen = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 1);
			twd_sectDiagnosen.grabHorizontal = true;
			twd_sectDiagnosen.align = TableWrapData.FILL;
			sectAnamnese.setLayoutData(twd_sectDiagnosen);
			sectAnamnese.setText("Persönliche Anamnese");
			sectAnamnese.setExpanded(CoreHub.localCfg.get(KEY_PATIENTENBLATT + sectAnamnese.getText(), false));

			txtAnamnese = toolkit.createText(sectAnamnese, "", SWT.WRAP | SWT.MULTI);
			txtAnamnese.addListener(SWT.Modify, new MultiLineAutoGrowListener(txtAnamnese));
			sectAnamnese.setClient(txtAnamnese);
			sectAnamnese.addExpansionListener(new SectionExpansionHandler());
		}

		// family anamnesis
		{
			Section sectFamAnamnese = toolkit.createSection(scrldfrmComposite, Section.EXPANDED | Section.TWISTIE);
			TableWrapData twd_sectDiagnosen = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 1);
			twd_sectDiagnosen.grabHorizontal = true;
			twd_sectDiagnosen.align = TableWrapData.FILL;
			sectFamAnamnese.setLayoutData(twd_sectDiagnosen);
			sectFamAnamnese.setText("Familien-Anamnese");
			sectFamAnamnese.setExpanded(CoreHub.localCfg.get(KEY_PATIENTENBLATT + sectFamAnamnese.getText(), false));

			txtFamAnamnese = toolkit.createText(sectFamAnamnese, "", SWT.WRAP | SWT.MULTI);
			txtFamAnamnese.setText("");
			txtFamAnamnese.addListener(SWT.Modify, new MultiLineAutoGrowListener(txtFamAnamnese));
			sectFamAnamnese.setClient(txtFamAnamnese);
			sectFamAnamnese.addExpansionListener(new SectionExpansionHandler());
		}

		// allergy
		{
			Section sectAllergien = toolkit.createSection(scrldfrmComposite, Section.EXPANDED | Section.TWISTIE);
			TableWrapData twd_sectDiagnosen = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 1);
			twd_sectDiagnosen.grabHorizontal = true;
			twd_sectDiagnosen.align = TableWrapData.FILL;
			sectAllergien.setLayoutData(twd_sectDiagnosen);
			sectAllergien.setText("Allergien");
			sectAllergien.setExpanded(CoreHub.localCfg.get(KEY_PATIENTENBLATT + sectAllergien.getText(), false));

			txtAllergien = toolkit.createText(sectAllergien, "", SWT.WRAP | SWT.MULTI);
			txtAllergien.setText("");
			txtAllergien.addListener(SWT.Modify, new MultiLineAutoGrowListener(txtAllergien));
			sectAllergien.setClient(txtAllergien);
			sectAllergien.addExpansionListener(new SectionExpansionHandler());
		}

		// risks
		{
			Section sectRisiken = toolkit.createSection(scrldfrmComposite, Section.EXPANDED | Section.TWISTIE);
			TableWrapData twd_sectDiagnosen = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 1);
			twd_sectDiagnosen.grabHorizontal = true;
			twd_sectDiagnosen.align = TableWrapData.FILL;
			sectRisiken.setLayoutData(twd_sectDiagnosen);
			sectRisiken.setText("Risiken");
			sectRisiken.setExpanded(CoreHub.localCfg.get(KEY_PATIENTENBLATT + sectRisiken.getText(), false));

			txtRisiken = toolkit.createText(sectRisiken, "", SWT.WRAP | SWT.MULTI);
			txtRisiken.setText("");
			txtRisiken.addListener(SWT.Modify, new MultiLineAutoGrowListener(txtRisiken));
			sectRisiken.setClient(txtRisiken);
			sectRisiken.addExpansionListener(new SectionExpansionHandler());
		}

		// remarks
		{
			Section sectBemerkungen = toolkit.createSection(scrldfrmComposite, Section.EXPANDED | Section.TWISTIE);
			TableWrapData twd_sectDiagnosen = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 1);
			twd_sectDiagnosen.grabHorizontal = true;
			twd_sectDiagnosen.align = TableWrapData.FILL;
			sectBemerkungen.setLayoutData(twd_sectDiagnosen);
			sectBemerkungen.setText("Bemerkungen");
			sectBemerkungen.setExpanded(CoreHub.localCfg.get(KEY_PATIENTENBLATT + sectBemerkungen.getText(), false));

			txtBemerkungen = toolkit.createText(sectBemerkungen, "", SWT.WRAP | SWT.MULTI);
			txtBemerkungen.setText("");
			txtBemerkungen.addListener(SWT.Modify, new MultiLineAutoGrowListener(txtBemerkungen));
			sectBemerkungen.setClient(txtBemerkungen);
			sectBemerkungen.addExpansionListener(new SectionExpansionHandler());
		}

		{
			ExpandableComposite ecdm = WidgetFactory.createExpandableComposite(toolkit, scrldfrm, FIXMEDIKATION);
			ecdm.addExpansionListener(new SectionExpansionHandler());
			dmd = new FixMediDisplay(ecdm, getViewSite());
			ecdm.setClient(dmd);
		}

		viewmenu = new ViewMenus(getViewSite());
		viewmenu.createMenu(GlobalActions.printEtikette, GlobalActions.printAdresse, GlobalActions.printBlatt,
				GlobalActions.printRoeBlatt);

		initDataBindings();
		
		GlobalEventDispatcher.addActivationListener(this, this);
	}

	public void dispose() {
		ElexisEventDispatcher.getInstance().removeListeners(eeli_pat);
		toolkit.dispose();
		super.dispose();
	}

	@Override
	public void setFocus() {
		// COMPAT
		// Initialize the current patient if view is about to be opened and no
		// selection yet
		if (patientObservable.getValue() == null) {
			Patient p = ElexisEventDispatcher.getSelectedPatient();
			if (p != null)
				setPatient(p);
		}
	}

	/**
	 * This {@link Listener} automatically grows and shrinks a {@link Text}
	 * according to the number of lines contained. It handles {@link SWT#Modify}
	 * events only.
	 * 
	 * @see http://stackoverflow.com/questions/8287853/text-widget-with-self-
	 *      adjusting-height-based-on-interactively-entered-text
	 * 
	 */
	private final class MultiLineAutoGrowListener implements Listener {
		protected int lines = 0;
		private Text t;

		public MultiLineAutoGrowListener(Text t) {
			this.t = t;
		}

		@Override
		public void handleEvent(Event event) {
			if (event.type != SWT.Modify)
				return;
			if (t.getLineCount() != lines) {
				lines = t.getLineCount();

				t.setSize(t.getSize().x, lines * (int) t.getFont().getFontData()[0].height);
				scrldfrm.reflow(true);
			}
		}
	}

	/**
	 * Handle section expansion events by advising the form composite to reflow
	 * itself.
	 */
	private final class SectionExpansionHandler extends ExpansionAdapter {
		@Override
		public void expansionStateChanged(ExpansionEvent e) {
			ExpandableComposite src = (ExpandableComposite) e.getSource();
			CoreHub.localCfg.set(KEY_PATIENTENBLATT + src.getText(), src.isExpanded());
			scrldfrm.reflow(true);
		}
	}

	private void refreshUi(){
		bindingContext.updateTargets();
	}
	
	protected void initDataBindings() {
		bindingContext = new DataBindingContext();

		Text[] control = { txtAllergien, txtAnamnese, txtBemerkungen, txtDiagnosen, txtRisiken, txtFamAnamnese };
		String[] property = { "allergies", "personalAnamnese", "comment", "diagnosen", "risk", "familyAnamnese" };

		for (int i = 0; i < control.length; i++) {
			bindValue(control[i], property[i], bindingContext);
		}
	}

	private void bindValue(Text text, String property, DataBindingContext bindingContext) {
		IObservableValue textObserveWidget = SWTObservables.observeDelayedValue(5,
				SWTObservables.observeText(text, SWT.Modify));
		IObservableValue observeValue = PojoObservables.observeDetailValue(patientObservable, property, String.class);
		bindingContext.bindValue(textObserveWidget, observeValue, null, null);
	}

	private void makeActions() {
		removeZAAction = new Action(Messages.Patientenblatt2_removeAddress) {
			@Override
			public void run() {
				BezugsKontakt a = (BezugsKontakt) inpZusatzAdresse.getSelection();
				a.delete();
				setPatient(ElexisEventDispatcher.getSelectedPatient());
			}
		};

		showZAAction = new RestrictedAction(AccessControlDefaults.PATIENT_DISPLAY,
				Messages.Patientenblatt2_showAddress) {
			@Override
			public void doRun() {
				Kontakt a = Kontakt.load(
					((BezugsKontakt) inpZusatzAdresse.getSelection()).get(BezugsKontakt.OTHER_ID));
				KontaktDetailDialog kdd = new KontaktDetailDialog(scrldfrm.getShell(), a, bLocked);
				if (kdd.open() == Dialog.OK) {
					setPatient(ElexisEventDispatcher.getSelectedPatient());
				}
			}
		};
		
		showBKAction = new RestrictedAction(AccessControlDefaults.PATIENT_DISPLAY,
			Messages.Patientenblatt2_showBezugKontaktRelation) {
			@Override
			public void doRun(){
				Patient actPatient = ElexisEventDispatcher.getSelectedPatient();
				if (actPatient != null && actPatient.exists()) {
					BezugsKontakt bezugsKontakt = (BezugsKontakt) inpZusatzAdresse.getSelection();
					if (bezugsKontakt != null) {
						Kontakt k = Kontakt.load(bezugsKontakt.get(BezugsKontakt.OTHER_ID));
						BezugsKontaktAuswahl bza = new BezugsKontaktAuswahl(
							actPatient.getLabel(true), k.istPerson()
									? Person.load(k.getId()).getLabel(true) : k.getLabel(true),
							bezugsKontakt, bLocked);
						if (bezugsKontakt != null && bza.open() == Dialog.OK
							&& bza.getBezugKonkaktRelation() != null) {
							bezugsKontakt.updateRelation(bza.getBezugKonkaktRelation());
							setPatient(actPatient);
						}
					}
				}
			}
		};
	}
	
	private void makeAdditionalAddressActions(){
		removeAdditionalAddressAction = new Action(Messages.Patientenblatt2_removeAddress) {
			@Override
			public void run(){
					ZusatzAdresse a = (ZusatzAdresse) additionalAddresses.getSelection();
					a.delete();
				setPatient(ElexisEventDispatcher.getSelectedPatient());
				
			}
		};
		
		showAdditionalAddressAction = new Action(Messages.Patientenblatt2_showAddress) {
			@Override
			public void run(){
				Patient actPatient = ElexisEventDispatcher.getSelectedPatient();
				ZusatzAdresse zusatzAdresse = (ZusatzAdresse) additionalAddresses.getSelection();
				ZusatzAdresseEingabeDialog aed =
					new ZusatzAdresseEingabeDialog(scrldfrm.getShell(), actPatient, zusatzAdresse,
						bLocked);
				if (aed.open() == Dialog.OK) {
					setPatient(actPatient);
				}
			}
		};
	}

	@Override
	public void activation(boolean mode){
		if (mode) {
			refreshUi();
		}
	}
	
	@Override
	public void visible(boolean mode){
		// TODO Auto-generated method stub
		
	}
	
}
