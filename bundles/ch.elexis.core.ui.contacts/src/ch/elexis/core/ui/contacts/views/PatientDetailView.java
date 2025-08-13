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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
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

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IPersistentObject;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.contacts.dialogs.BezugsKontaktAuswahl;
import ch.elexis.core.ui.dialogs.KontaktDetailDialog;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.dialogs.ZusatzAdresseEingabeDialog;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.locks.IUnlockable;
import ch.elexis.core.ui.locks.ToggleCurrentPatientLockHandler;
import ch.elexis.core.ui.medication.views.FixMediDisplay;
import ch.elexis.core.ui.util.ListDisplay;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.WidgetFactory;
import ch.elexis.core.ui.views.IRefreshable;
import ch.elexis.core.ui.views.controls.ClientCustomTextComposite;
import ch.elexis.core.ui.views.controls.StickerComposite;
import ch.elexis.data.BezugsKontakt;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.elexis.data.ZusatzAdresse;
import ch.rgw.tools.StringTool;
import jakarta.inject.Inject;
import jakarta.inject.Named;

public class PatientDetailView extends ViewPart implements IUnlockable, IActivationListener, IRefreshable {

	public static final String ID = "at.medevit.elexis.contacts.views.PatientDetail"; //$NON-NLS-1$

	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());

	private final static String FIXMEDIKATION = Messages.Core_Fixed_medication;
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
	private IObservableValue<Patient> patientObservable = new WritableValue<>(null, Patient.class);
	private boolean bLocked = true;

	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this);

	@Inject
	void lockedPatient(@Optional @UIEventTopic(ElexisEventTopics.EVENT_LOCK_AQUIRED) IPatient patient) {
		Patient pat = (Patient) NoPoUtil.loadAsPersistentObject(patient);
		if (pat != null && pat.equals(patientObservable.getValue())) {
			setUnlocked(true);
		}
	}

	@Inject
	void unlockedPatient(@Optional @UIEventTopic(ElexisEventTopics.EVENT_LOCK_RELEASED) IPatient patient) {
		Patient pat = (Patient) NoPoUtil.loadAsPersistentObject(patient);
		if (pat != null && pat.equals(patientObservable.getValue())) {
			setUnlocked(false);
		}
	}

	@Optional
	@Inject
	void activePatient(IPatient patient) {
		CoreUiUtil.runAsyncIfActive(() -> {
			IPersistentObject deselected = patientObservable.getValue();
			setPatient((Patient) NoPoUtil.loadAsPersistentObject(patient));
			releaseAndRefreshLock(deselected, ToggleCurrentPatientLockHandler.COMMAND_ID);
		}, scrldfrm);
	}

	private void releaseAndRefreshLock(IPersistentObject object, String commandId) {
		if (object != null && LocalLockServiceHolder.get().isLockedLocal(object)) {
			LocalLockServiceHolder.get().releaseLock(object);
		}
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		commandService.refreshElements(commandId, null);
	}

	private FixMediDisplay dmd;

	private DataBindingContext bindingContext;

	public PatientDetailView() {
		toolkit.setBorderStyle(SWT.NULL); // Deactivate borders for the widgets
		makeActions();
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
				+ " (" + p.getPatCode() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		compClientCustomText.updateClientCustomArea();
		stickerComposite.setPatient(CoreModelServiceHolder.get().load(p.getId(), IPatient.class).orElse(null));
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

		setUnlocked(LocalLockServiceHolder.get().isLockedLocal(p));
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
		scrldfrm.setText(Messages.Core_No_patient_selected);
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
					Messages.Patientenblatt2_contactForAdditionalAddress); // $NON-NLS-1$
			ecZA.setExpanded(CoreHub.localCfg.get(KEY_PATIENTENBLATT + ecZA.getText(), false));
			ecZA.addExpansionListener(new SectionExpansionHandler());
			inpZusatzAdresse = new ListDisplay<>(ecZA, SWT.NONE, new ListDisplay.LDListener() {
				/*
				 * public boolean dropped(final PersistentObject dropped) { return false; }
				 */

				@Override
				public void hyperlinkActivated(final String l) {
					Patient sp = ElexisEventDispatcher.getSelectedPatient();
					if (sp == null) {
						return;
					}

					final String[] sortFields = new String[] { Kontakt.FLD_NAME1, Kontakt.FLD_NAME2,
							Kontakt.FLD_STREET };
					KontaktSelektor ksl = new KontaktSelektor(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
							Kontakt.class, Messages.Patientenblatt2_contactForAdditionalAddress,
							Messages.Patientenblatt2_pleaseSelectardress, sortFields);
					if (ksl.open() == Dialog.OK) {
						Kontakt k = (Kontakt) ksl.getSelection();
						if (k != null) {
							BezugsKontaktAuswahl bza = new BezugsKontaktAuswahl(sp.getLabel(true),
									k.istPerson() ? Person.load(k.getId()).getLabel(true) : k.getLabel(true));
							if (bza.open() == Dialog.OK && sp != null) {
								BezugsKontakt bk = sp.addBezugsKontakt(k, bza.getBezugKonkaktRelation());
								inpZusatzAdresse.add(bk);
								scrldfrm.reflow(true);
							}
						}
					}
				}

				@Override
				public String getLabel(Object o) {
					BezugsKontakt bezugsKontakt = (BezugsKontakt) o;

					StringBuffer sb = new StringBuffer();
					sb.append(bezugsKontakt.getLabel());

					Kontakt other = Kontakt.load(bezugsKontakt.get(BezugsKontakt.OTHER_ID));
					if (other.exists()) {
						List<String> tokens = new ArrayList<>();

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
			inpZusatzAdresse.addHyperlinks(Messages.Core_Add); // $NON-NLS-1$
			// inpZusatzAdresse.setMenu(createZusatzAdressMenu());
			inpZusatzAdresse.setMenu(removeZAAction, showZAAction, showBKAction);

			ecZA.setClient(inpZusatzAdresse);
		}

		// zusatz adressen
		{
			ExpandableComposite compAdditionalAddresses = WidgetFactory.createExpandableComposite(toolkit, scrldfrm,
					Messages.Patientenblatt2_additionalAdresses);
			compAdditionalAddresses.addExpansionListener(new SectionExpansionHandler());

			additionalAddresses = new ListDisplay<>(compAdditionalAddresses, SWT.NONE,
					new ListDisplay.LDListener() {
						@Override
						public void hyperlinkActivated(final String l) {
							Patient actPatient = ElexisEventDispatcher.getSelectedPatient();
							if (actPatient != null) {
								ZusatzAdresseEingabeDialog aed = new ZusatzAdresseEingabeDialog(
										PlatformUI.getWorkbench().getDisplay().getActiveShell(), actPatient);
								if (aed.open() == Dialog.OK) {
									additionalAddresses.add(aed.getZusatzAdresse());
									scrldfrm.reflow(true);
								}
							}
						}

						@Override
						public String getLabel(Object o) {
							ZusatzAdresse address = (ZusatzAdresse) o;
							if (address != null) {
								return address.getLabel();
							}
							return "?"; //$NON-NLS-1$
						}
					});

			// Hyperlink "Hinzu..." über der Adressliste hinzufügen
			additionalAddresses.addHyperlinks(Messages.Core_Add); // $NON-NLS-1$

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

			txtDiagnosen = toolkit.createText(sectDiagnosen, StringUtils.EMPTY, SWT.WRAP | SWT.MULTI);
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

			txtAnamnese = toolkit.createText(sectAnamnese, StringUtils.EMPTY, SWT.WRAP | SWT.MULTI);
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

			txtFamAnamnese = toolkit.createText(sectFamAnamnese, StringUtils.EMPTY, SWT.WRAP | SWT.MULTI);
			txtFamAnamnese.setText(StringUtils.EMPTY);
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

			txtAllergien = toolkit.createText(sectAllergien, StringUtils.EMPTY, SWT.WRAP | SWT.MULTI);
			txtAllergien.setText(StringUtils.EMPTY);
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

			txtRisiken = toolkit.createText(sectRisiken, StringUtils.EMPTY, SWT.WRAP | SWT.MULTI);
			txtRisiken.setText(StringUtils.EMPTY);
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

			txtBemerkungen = toolkit.createText(sectBemerkungen, StringUtils.EMPTY, SWT.WRAP | SWT.MULTI);
			txtBemerkungen.setText(StringUtils.EMPTY);
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

		getSite().getPage().addPartListener(udpateOnVisible);
	}

	@Override
	public void dispose() {
		getSite().getPage().removePartListener(udpateOnVisible);
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

	private void refreshUi() {
		bindingContext.updateTargets();
	}

	protected void initDataBindings() {
		bindingContext = new DataBindingContext();

		Text[] control = { txtAllergien, txtAnamnese, txtBemerkungen, txtDiagnosen, txtRisiken, txtFamAnamnese };
		String[] property = { "allergies", "personalAnamnese", "comment", "diagnosen", "risk", "familyAnamnese" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

		for (int i = 0; i < control.length; i++) {
			bindValue(control[i], property[i], bindingContext);
		}
	}

	private void bindValue(Text text, String property, DataBindingContext bindingContext) {
		ISWTObservableValue<String> textObserveWidget = WidgetProperties.text(SWT.Modify).observe(text);
		IObservableValue<Object> observeValue = PojoProperties.value(Patient.class, property)
				.observeDetail(patientObservable);
		bindingContext.bindValue(textObserveWidget, observeValue, null, null);
	}

	private void makeActions() {
		removeZAAction = new Action(Messages.Patientenblatt2_removeAddress) {
			@Override
			public void run() {
				BezugsKontakt a = inpZusatzAdresse.getSelection();
				a.delete();
				setPatient(ElexisEventDispatcher.getSelectedPatient());
			}
		};

		showZAAction = new RestrictedAction(EvACE.of(IPatient.class, Right.VIEW),
				Messages.Patientenblatt2_showAddress) {
			@Override
			public void doRun() {
				Kontakt a = Kontakt.load(inpZusatzAdresse.getSelection().get(BezugsKontakt.OTHER_ID));
				KontaktDetailDialog kdd = new KontaktDetailDialog(scrldfrm.getShell(), a, bLocked);
				if (kdd.open() == Dialog.OK) {
					setPatient(ElexisEventDispatcher.getSelectedPatient());
				}
			}
		};

		showBKAction = new RestrictedAction(EvACE.of(IPatient.class, Right.VIEW),
				Messages.Patientenblatt2_showBezugKontaktRelation) {
			@Override
			public void doRun() {
				Patient actPatient = ElexisEventDispatcher.getSelectedPatient();
				if (actPatient != null && actPatient.exists()) {
					BezugsKontakt bezugsKontakt = inpZusatzAdresse.getSelection();
					if (bezugsKontakt != null) {
						Kontakt k = Kontakt.load(bezugsKontakt.get(BezugsKontakt.OTHER_ID));
						BezugsKontaktAuswahl bza = new BezugsKontaktAuswahl(actPatient.getLabel(true),
								k.istPerson() ? Person.load(k.getId()).getLabel(true) : k.getLabel(true), bezugsKontakt,
								bLocked);
						if (bezugsKontakt != null && bza.open() == Dialog.OK && bza.getBezugKonkaktRelation() != null) {
							bezugsKontakt.updateRelation(bza.getBezugKonkaktRelation());
							setPatient(actPatient);
						}
					}
				}
			}
		};
	}

	private void makeAdditionalAddressActions() {
		removeAdditionalAddressAction = new Action(Messages.Patientenblatt2_removeAddress) {
			@Override
			public void run() {
				ZusatzAdresse a = additionalAddresses.getSelection();
				a.delete();
				setPatient(ElexisEventDispatcher.getSelectedPatient());

			}
		};

		showAdditionalAddressAction = new Action(Messages.Patientenblatt2_showAddress) {
			@Override
			public void run() {
				Patient actPatient = ElexisEventDispatcher.getSelectedPatient();
				ZusatzAdresse zusatzAdresse = additionalAddresses.getSelection();
				ZusatzAdresseEingabeDialog aed = new ZusatzAdresseEingabeDialog(scrldfrm.getShell(), actPatient,
						zusatzAdresse, bLocked);
				if (aed.open() == Dialog.OK) {
					setPatient(actPatient);
				}
			}
		};
	}

	@Override
	public void activation(boolean mode) {
		if (mode) {
			refreshUi();
		}
	}

	@Override
	public void visible(boolean mode) {
		// TODO Auto-generated method stub

	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}

	@Override
	public void refresh() {
		activePatient(ContextServiceHolder.get().getActivePatient().orElse(null));
	}

}
