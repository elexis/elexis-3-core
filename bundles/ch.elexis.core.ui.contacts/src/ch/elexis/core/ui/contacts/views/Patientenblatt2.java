/*******************************************************************************
 * Copyright (c) 2008-2010, G. Weirich and Elexis
 * Portions (c) 2012-2013, Joerg M. Sigle (js, jsigle)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich -  initial implementation
 *    Joerg Sigle - Added ability to copy selected addresses to the clipboard
 *
 *******************************************************************************/

package ch.elexis.core.ui.contacts.views;

import static ch.elexis.core.ui.constants.ExtensionPointConstantsUi.VIEWCONTRIBUTION;
import static ch.elexis.core.ui.constants.ExtensionPointConstantsUi.VIEWCONTRIBUTION_CLASS;
import static ch.elexis.core.ui.constants.ExtensionPointConstantsUi.VIEWCONTRIBUTION_VIEWID;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.statushandlers.StatusManager;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.data.interfaces.IPersistentObject;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.MaritalStatus;
import ch.elexis.core.model.PatientConstants;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.contacts.dialogs.BezugsKontaktAuswahl;
import ch.elexis.core.ui.dialogs.AddBuchungDialog;
import ch.elexis.core.ui.dialogs.AnschriftEingabeDialog;
import ch.elexis.core.ui.dialogs.KontaktDetailDialog;
import ch.elexis.core.ui.dialogs.KontaktExtDialog;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.dialogs.ZusatzAdresseEingabeDialog;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.events.ElexisUiSyncEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.IUnlockable;
import ch.elexis.core.ui.locks.ToggleCurrentPatientLockHandler;
import ch.elexis.core.ui.medication.views.FixMediDisplay;
import ch.elexis.core.ui.settings.UserSettings;
import ch.elexis.core.ui.util.FilterNonPrintableModifyListener;
import ch.elexis.core.ui.util.InputPanel;
import ch.elexis.core.ui.util.LabeledInputField;
import ch.elexis.core.ui.util.LabeledInputField.IStructuredSelectionResolver;
import ch.elexis.core.ui.util.LabeledInputField.InputData;
import ch.elexis.core.ui.util.LabeledInputField.InputData.Typ;
import ch.elexis.core.ui.util.ListDisplay;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.WidgetFactory;
import ch.elexis.core.ui.views.contribution.IViewContribution;
import ch.elexis.core.ui.views.contribution.ViewContributionHelper;
import ch.elexis.core.ui.views.controls.StickerComposite;
import ch.elexis.core.utils.CoreUtil;
import ch.elexis.data.Anwender;
import ch.elexis.data.BezugsKontakt;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Labor;
import ch.elexis.data.Organisation;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.elexis.data.Xid;
import ch.elexis.data.Xid.XIDDomain;
import ch.elexis.data.ZusatzAdresse;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Detailansicht eines Patientrecords Ersatz für Patientenblatt mit erweiterter
 * Funktionalität (Lock, Nutzung von InputPanel)
 */
public class Patientenblatt2 extends Composite implements IUnlockable {
	private static final String KEY_DBFIELD = "dbfield"; //$NON-NLS-1$
	private static final String KEY_PATIENTENBLATT = "Patientenblatt/"; //$NON-NLS-1$
	private final FormToolkit tk;
	private InputPanel ipp;
	private IAction removeZAAction, showZAAction, showBKAction, copySelectedContactInfosToClipboardAction,
			copySelectedAddressesToClipboardAction, removeAdditionalAddressAction, showAdditionalAddressAction;
	// MenuItem delZA;
	public final static String CFG_BEZUGSKONTAKTTYPEN = "views/patientenblatt/Bezugskontakttypen"; //$NON-NLS-1$
	public final static String CFG_EXTRAFIELDS = "views/patientenblatt/extrafelder"; //$NON-NLS-1$
	public final static String SPLITTER = "#!>"; //$NON-NLS-1$

	@SuppressWarnings("unchecked")
	private final List<IViewContribution> detailComposites = Extensions.getClasses(VIEWCONTRIBUTION,
			VIEWCONTRIBUTION_CLASS, VIEWCONTRIBUTION_VIEWID, PatientDetailView2.ID);
	@SuppressWarnings("unchecked")
	private final List<IViewContribution> buttonTabContributions = Extensions.getClasses(VIEWCONTRIBUTION,
			VIEWCONTRIBUTION_CLASS, VIEWCONTRIBUTION_VIEWID, PatientDetailView2.ID + ":buttonTab");

	private ElexisEventListener eeli_pat = new ElexisUiEventListenerImpl(Patient.class) {
		public void runInUi(ElexisEvent ev) {
			Patient pat = (Patient) ev.getObject();

			switch (ev.getType()) {
			case ElexisEvent.EVENT_SELECTED:
				Patient deselectedPatient = actPatient;
				// release before change, triggers save via prerelease
				releaseAndRefreshLock(deselectedPatient, ToggleCurrentPatientLockHandler.COMMAND_ID);
				setPatient(pat);
				break;
			case ElexisEvent.EVENT_LOCK_AQUIRED:
			case ElexisEvent.EVENT_LOCK_RELEASED:
				if (pat.equals(actPatient)) {
					setUnlocked(ev.getType() == ElexisEvent.EVENT_LOCK_AQUIRED);
				}
				break;
			default:
				break;
			}
		}
	};

	private void releaseAndRefreshLock(IPersistentObject object, String commandId) {
		if (object != null && LocalLockServiceHolder.get().isLockedLocal(object)) {
			LocalLockServiceHolder.get().releaseLock(object);
		}
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		commandService.refreshElements(commandId, null);
	}

	private final ElexisEventListener eeli_pat_sync = new ElexisUiSyncEventListenerImpl(Patient.class,
			ElexisEvent.EVENT_LOCK_PRERELEASE) {
		@Override
		public void runInUi(ElexisEvent ev) {
			Patient pat = (Patient) ev.getObject();
			if (pat.equals(actPatient)) {
				save();
			}
		}
	};

	private ElexisEventListener eeli_user = new ElexisUiEventListenerImpl(Anwender.class,
			ElexisEvent.EVENT_USER_CHANGED) {
		public void runInUi(ElexisEvent ev) {
			setPatient(ElexisEventDispatcher.getSelectedPatient());
			recreateUserpanel();
		}
	};

	private ArrayList<String> lbExpandable = new ArrayList<>(Arrays.asList(Messages.Core_Diagnosis,
			Messages.Patientenblatt2_persAnamnesisLbl, Messages.Patientenblatt2_famAnamnesisLbl, Messages.Allergies,
			Messages.Patientenblatt2_risksLbl, Messages.Core_Remarks));
	private final List<Text> txExpandable = new ArrayList<>();
	private ArrayList<String> dfExpandable = new ArrayList<>(
			Arrays.asList(Patient.FLD_DIAGNOSES, Patient.FLD_PERS_ANAMNESE, Patient.FLD_FAM_ANAMNESE, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					Patient.FLD_ALLERGIES, Patient.FLD_RISKS, Kontakt.FLD_REMARK //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	));
	private final List<ExpandableComposite> ec = new ArrayList<>();
	private final static String FIXMEDIKATION = Messages.Core_Fixed_medication; // $NON-NLS-1$
	// private final static String[] lbLists={"Fixmedikation"/*,"Reminders" */};
	private final FormText inpAdresse;
	private final ListDisplay<BezugsKontakt> inpZusatzAdresse;
	private final ListDisplay<ZusatzAdresse> additionalAddresses; /* , dlReminder */;
	private final FixMediDisplay dmd;
	Patient actPatient;
	IViewSite viewsite;
	private final Hyperlinkreact hr = new Hyperlinkreact();
	private final ScrolledForm form;
	private final ViewMenus viewmenu;
	private final ExpandableComposite ecdm, ecZA, compAdditionalAddresses;
	private boolean bLocked = true;
	private Composite cUserfields;
	Hyperlink hHA;
	private InputData comboGeschlecht;
	StickerComposite stickerComposite;
	private Button deceasedBtn;
	private CDateTime deceasedDate;
	private Button increasedTreatmentBtn;

	void recreateUserpanel() {
		// cUserfields.setRedraw(false);
		if (ipp != null) {
			ipp.dispose();
			ipp = null;
		}

		ArrayList<InputData> fields = new ArrayList<InputData>(20);
		fields.add(new InputData(Messages.Core_Name, Patient.FLD_NAME, InputData.Typ.STRING, null)); // $NON-NLS-1$
		fields.add(
				new InputData(Messages.Core_Firstname, Patient.FLD_FIRSTNAME, InputData.Typ.STRING, null)); // $NON-NLS-1$
		fields.add(new InputData(Messages.Core_Enter_Birthdate, Patient.BIRTHDATE, InputData.Typ.DATE, null)); // $NON-NLS-1$
		IStructuredSelectionResolver ssr = new IStructuredSelectionResolver() {
			@Override
			public StructuredSelection resolveStructuredSelection(String value) {
				if (value.contentEquals(Patient.FEMALE)) {
					return new StructuredSelection(Messages.Patient_female_short);
				} else {
					return new StructuredSelection(Messages.Patient_male_short);
				}
			}
		};
		comboGeschlecht = new InputData(Messages.Sex, Patient.FLD_SEX, null, Typ.COMBO_VIEWER,
				ArrayContentProvider.getInstance(), new LabelProvider() {

					@Override
					public String getText(Object element) {
						String geschlecht = element.toString();
						if (geschlecht.contentEquals(Patient.FEMALE)) {
							return Messages.Patient_female_short;
						} else {
							return Messages.Patient_male_short;
						}
					}
				}, ssr, new String[] { Patient.MALE, Patient.FEMALE });
		String toolTip = String.format(Messages.Patient_male_female_tooltip, Messages.Patient_male_short,
				Messages.Patient_female_short, Messages.Patient_male_long, Messages.Patient_female_long);
		fields.add(comboGeschlecht);
		comboGeschlecht.setTooltipText(toolTip);

		IStructuredSelectionResolver isr = new IStructuredSelectionResolver() {
			@Override
			public StructuredSelection resolveStructuredSelection(String value) {
				MaritalStatus selection = MaritalStatus.byNumericSafe(value);
				return new StructuredSelection(selection);
			}
		};
		fields.add(new InputData(Messages.Patientenblatt2_civilState, Patient.FLD_EXTINFO,
				PatientConstants.FLD_EXTINFO_MARITAL_STATUS, Typ.COMBO_VIEWER, ArrayContentProvider.getInstance(),
				new LabelProvider() {
					@Override
					public String getText(Object element) {
						MaritalStatus ms = (MaritalStatus) element;
						if (ms != null) {
							return ms.getLocaleText();
						}
						return super.getText(element);
					}
				}, isr, MaritalStatus.values()));
		fields.add(new InputData(Messages.Core_Mobilephone, Patient.MOBILE, InputData.Typ.STRING, null, 30)); // $NON-NLS-1$

		fields.add(new InputData(Messages.Patientenblatt2_phone1, Patient.FLD_PHONE1, InputData.Typ.STRING, null, 30)); // $NON-NLS-1$
		fields.add(new InputData(Messages.Patientenblatt2_phone2, Patient.FLD_PHONE2, InputData.Typ.STRING, null, 30)); // $NON-NLS-1$
		fields.add(new InputData(Messages.Core_Fax, Patient.FLD_FAX, InputData.Typ.STRING, null, 30)); // $NON-NLS-1$
		fields.add(new InputData(Messages.Core_E_Mail, Patient.FLD_E_MAIL, // $NON-NLS-1$
				new LabeledInputField.IExecLinkProvider() {
					@Override
					public void executeString(InputData ltf) {
						if (ltf.getText().length() == 0)
							return;
						try {
							URI uriMailTo = new URI("mailto", ltf.getText(), null); //$NON-NLS-1$
							Desktop.getDesktop().mail(uriMailTo);
						} catch (URISyntaxException e1) {
							Status status = new Status(IStatus.WARNING, Hub.PLUGIN_ID,
									"Error in using mail address " + ltf);
							StatusManager.getManager().handle(status, StatusManager.SHOW);
						} catch (IOException e2) {
							Status status = new Status(IStatus.WARNING, Hub.PLUGIN_ID,
									"Error in using mail address " + ltf);
							StatusManager.getManager().handle(status, StatusManager.SHOW);
						}
					}
				}));
		fields.add(new InputData(Messages.Core_Group, Patient.FLD_GROUP, InputData.Typ.STRING, null)); // $NON-NLS-1$
		fields.add(new InputData(Messages.Core_Account, Patient.FLD_BALANCE, new LabeledInputField.IContentProvider() { // $NON-NLS-1$

			@Override
			public void displayContent(Object po, InputData ltf) {
				ltf.setText(actPatient.getKontostand().getAmountAsString());
			}

			@Override
			public void reloadContent(Object po, InputData ltf) {
				if (new AddBuchungDialog(getShell(), actPatient).open() == Dialog.OK) {
					ltf.setText(actPatient.getKontostand().getAmountAsString());
				}
			}

		}));
		fields.add(new InputData(Messages.Core_RegularPhysiscion, PatientConstants.FLD_EXTINFO_STAMMARZT,
				new LabeledInputField.IContentProvider() { // $NON-NLS-1$

					@Override
					public void displayContent(Object po, InputData ltf) {
						Patient p = (Patient) po;
						String result = StringUtils.EMPTY;
						if (p.getStammarzt() != null && p.getStammarzt().exists()) {
							Kontakt stammarzt = p.getStammarzt();
							if (stammarzt.istPerson()) {
								String[] labels = stammarzt.get(true, Kontakt.FLD_NAME1, Kontakt.FLD_NAME2,
										Person.TITLE);
								result = labels[2] + StringConstants.SPACE + labels[0] + StringConstants.SPACE
										+ labels[1];
							} else {
								result = stammarzt.getLabel(true);
							}

							String telephoneLabel = stammarzt.getTelephoneLabel();
							String label = stammarzt.getLabel()
									+ ((telephoneLabel.length() > 0) ? " (" + telephoneLabel + ")" : StringUtils.EMPTY); //$NON-NLS-1$ //$NON-NLS-2$
							ltf.setTooltipText(label);
						} else {
							ltf.setTooltipText(null);
						}
						ltf.setText(result);
					}

					@Override
					public void reloadContent(Object po, InputData ltf) {
						if (bLocked) {
							return;
						}
						KontaktSelektor ks = new KontaktSelektor(getShell(), Kontakt.class,
								Messages.Patientenblatt2_selectRegularPhysicianTitle,
								Messages.Patientenblatt2_selectRegularPhysicianMessage, false, true);
						ks.enableEmptyFieldButton();
						if (ks.open() == Dialog.OK) {
							Object contactSel = ks.getSelection();
							if (contactSel == null) {
								((Patient) po).removeStammarzt();
							} else {
								Kontakt k = (Kontakt) contactSel;
								((Patient) po).setStammarzt(k);
							}
							displayContent(po, ltf);
						}
					}
				}));

		fields.add(new InputData(Messages.Patientenblatt2_ahvNumber, XidConstants.DOMAIN_AHV,
				new LabeledInputField.IContentProvider() {
					@Override
					public void displayContent(Object po, InputData ltf) {
						Patient p = (Patient) po;
						ltf.setText(p.getXid(XidConstants.DOMAIN_AHV));
					}

					@Override
					public void reloadContent(final Object po, final InputData ltf) {
						if (bLocked) {
							return;
						}
						ArrayList<String> extFlds = new ArrayList<String>();
						Kontakt k = (Kontakt) po;
						for (String dom : Xid.getXIDDomains()) {
							XIDDomain xd = Xid.getDomain(dom);
							if ((k.istPerson() && xd.isDisplayedFor(Person.class))
									|| (k.istOrganisation() && xd.isDisplayedFor(Organisation.class))) {
								extFlds.add(Xid.getSimpleNameForXIDDomain(dom) + "=" + dom); //$NON-NLS-1$
							} else if (k.istOrganisation() && xd.isDisplayedFor(Labor.class)) {
								extFlds.add(Xid.getSimpleNameForXIDDomain(dom) + "=" + dom); //$NON-NLS-1$
							}
						}

						KontaktExtDialog dlg = new KontaktExtDialog(UiDesk.getTopShell(),
								NoPoUtil.loadAsIdentifiable((Kontakt) po, IContact.class).orElse(null),
								extFlds.toArray(new String[0]));
						dlg.open();
						Patient p = (Patient) po;
						ltf.setText(p.getXid(XidConstants.DOMAIN_AHV));
					}
				}));

		fields.add(new InputData(Messages.Patientenblatt2_legalGuardian, PatientConstants.FLD_EXTINFO_LEGAL_GUARDIAN,
				new LabeledInputField.IContentProvider() {
					@Override
					public void displayContent(Object po, InputData ltf) {
						Patient p = (Patient) po;
						String guardianLabel = StringUtils.EMPTY;
						Kontakt legalGuardian = p.getLegalGuardian();
						if (legalGuardian != null && legalGuardian.exists()) {
							guardianLabel = legalGuardian.get(Kontakt.FLD_NAME1) + StringUtils.SPACE
									+ legalGuardian.get(Kontakt.FLD_NAME2);
						}
						ltf.setText(guardianLabel);
					}

					@Override
					public void reloadContent(Object po, InputData ltf) {
						if (bLocked) {
							return;
						}
						KontaktSelektor ks = new KontaktSelektor(getShell(), Kontakt.class,
								Messages.Patientenblatt2_selectLegalGuardianTitle,
								Messages.Patientenblatt2_selectLegalGuardianMessage, null);
						ks.enableEmptyFieldButton();
						if (ks.open() == Dialog.OK) {
							String guardianLabel = StringUtils.EMPTY;
							Object contactSel = ks.getSelection();
							Kontakt legalGuardian = null;

							// get legal guardian if one is defined
							if (contactSel != null) {
								legalGuardian = (Kontakt) contactSel;
								guardianLabel = legalGuardian.get(Kontakt.FLD_NAME1) + StringUtils.SPACE
										+ legalGuardian.get(Kontakt.FLD_NAME2);
							}
							((Patient) po).setLegalGuardian(legalGuardian);
							ltf.setText(guardianLabel);
						}
					}
				}));

		String[] userfields = ConfigServiceHolder.getUser(CFG_EXTRAFIELDS, StringConstants.EMPTY)
				.split(StringConstants.COMMA);
		for (String extfield : userfields) {
			if (!StringTool.isNothing(extfield)) {
				fields.add(new InputData(extfield, Patient.FLD_EXTINFO, InputData.Typ.STRING, extfield));
			}
		}
		ipp = new InputPanel(cUserfields, 3, 3, fields.toArray(new InputData[0]));
		ipp.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		ipp.changed(ipp.getChildren());
		// cUserfields.setRedraw(true);
		cUserfields.setBounds(ipp.getBounds());

		refresh();
		if (actPatient != null) {
			setPatient(actPatient);
		}
		layout(true);
	}

	Patientenblatt2(final Composite parent, final IViewSite site) {
		super(parent, SWT.NONE);
		viewsite = site;
		makeActions();
		parent.setLayout(new FillLayout());
		setLayout(new GridLayout());
		tk = UiDesk.getToolkit();
		form = tk.createScrolledForm(this);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		form.getBody().setLayout(new GridLayout());

		parent.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				updateExpandableLayoutWidth();
			}
		});

		stickerComposite = StickerComposite.createWrappedStickerComposite(form.getBody(), tk);

		cUserfields = new Composite(form.getBody(), SWT.NONE);
		cUserfields.setLayout(new GridLayout());
		cUserfields.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		recreateUserpanel();

		Composite cPersonalien = tk.createComposite(form.getBody());
		cPersonalien.setLayout(new GridLayout(2, false));
		cPersonalien.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		deceasedBtn = tk.createButton(cPersonalien, Messages.Patient_deceased, SWT.CHECK);
		deceasedBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (actPatient != null) {
					IPatient patient = NoPoUtil.loadAsIdentifiable(actPatient, IPatient.class).get();
					patient.setDeceased(deceasedBtn.getSelection());
					CoreModelServiceHolder.get().save(patient);
					if (deceasedBtn.getSelection()) {
						((GridData) deceasedDate.getLayoutData()).exclude = false;
						deceasedDate.setVisible(true);
						deceasedDate.setFocus();
					} else {
						((GridData) deceasedDate.getLayoutData()).exclude = true;
						deceasedDate.setVisible(false);
					}
					refreshUi();
				}
			}
		});
		deceasedDate = new CDateTime(cPersonalien, CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TEXT_TRAIL);
		deceasedDate.setLayoutData(new GridData());
		((GridData) deceasedDate.getLayoutData()).exclude = false;
		deceasedDate.setVisible(false);
		deceasedDate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IPatient patient = NoPoUtil.loadAsIdentifiable(actPatient, IPatient.class).get();
				Date selected = deceasedDate.getSelection();
				if (selected != null) {
					patient.setDateOfDeath(LocalDateTime.ofInstant(selected.toInstant(), ZoneId.systemDefault()));
				} else {
					patient.setDateOfDeath(null);
				}
				CoreModelServiceHolder.get().save(patient);
			}
		});

		// "erhöhter Behandlungsbedarf"
		increasedTreatmentBtn = tk.createButton(cPersonalien, Messages.Patientenblatt2_increasedTreatment, SWT.CHECK);
		increasedTreatmentBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (actPatient != null) {
					IPatient patient = NoPoUtil.loadAsIdentifiable(actPatient, IPatient.class).get();
					patient.setExtInfo(PatientConstants.FLD_EXTINFO_INCREASEDTREATMENT,
							Boolean.toString(increasedTreatmentBtn.getSelection()));
					CoreModelServiceHolder.get().save(patient);
				}
			}
		});
		increasedTreatmentBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		List<IViewContribution> _buttonTabContributions = ViewContributionHelper
				.getFilteredAndPositionSortedContributions(buttonTabContributions, 0);
		for (IViewContribution ivc : _buttonTabContributions) {
			Composite ret = ivc.initComposite(cPersonalien);
			ret.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			tk.adapt(ret);
		}

		hHA = tk.createHyperlink(cPersonalien, Messages.Core_Postal_Address, SWT.NONE); // $NON-NLS-1$
		hHA.addHyperlinkListener(hr);
		hHA.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		inpAdresse = tk.createFormText(cPersonalien, false);
		inpAdresse.setText("---\n", false, false); //$NON-NLS-1$
		inpAdresse.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));

		IExpansionListener ecExpansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanging(final ExpansionEvent e) {
				ExpandableComposite src = (ExpandableComposite) e.getSource();
				UserSettings.saveExpandedState(KEY_PATIENTENBLATT + src.getText(), e.getState());
			}
		};

		List<IViewContribution> filtered = ViewContributionHelper
				.getFilteredAndPositionSortedContributions(detailComposites, 0);
		for (IViewContribution ivc : filtered) {
			if (ivc.getClass().getPackage().getName().startsWith("ch.elexis.core.findings.ui.viewcontributions")) { //$NON-NLS-1$
				if (ivc.isAvailable()) {
					// remove unstructured diagnosis ui
					if (ivc.getClass().getSimpleName().equals("DiagnoseViewContribution")) { //$NON-NLS-1$
						lbExpandable.remove(Messages.Core_Diagnosis);
						dfExpandable.remove(Patient.FLD_DIAGNOSES); //$NON-NLS-1$
					}
					if (ivc.getClass().getSimpleName().equals("PersonalAnamnesisViewContribution")) { //$NON-NLS-1$
						lbExpandable.remove(Messages.Patientenblatt2_persAnamnesisLbl);
						dfExpandable.remove(Patient.FLD_PERS_ANAMNESE); //$NON-NLS-1$
					}
					if (ivc.getClass().getSimpleName().equals("FamilyAnamnesisViewContribution")) { //$NON-NLS-1$
						lbExpandable.remove(Messages.Patientenblatt2_famAnamnesisLbl);
						dfExpandable.remove(Patient.FLD_FAM_ANAMNESE); //$NON-NLS-1$
					}
					if (ivc.getClass().getSimpleName().equals("RiskViewContribution")) { //$NON-NLS-1$
						lbExpandable.remove(Messages.Patientenblatt2_risksLbl);
						dfExpandable.remove(Patient.FLD_RISKS); //$NON-NLS-1$
					}
					if (ivc.getClass().getSimpleName().equals("AllergyIntoleranceViewContribution")) { //$NON-NLS-1$
						lbExpandable.remove(Messages.Allergies);
						dfExpandable.remove(Patient.FLD_ALLERGIES); //$NON-NLS-1$
					}
				}
			}
			ExpandableComposite ec = WidgetFactory.createExpandableComposite(tk, form, ivc.getLocalizedTitle());
			ec.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			UserSettings.setExpandedState(ec, KEY_PATIENTENBLATT + ec.getText());
			ec.addExpansionListener(ecExpansionListener);
			Composite ret = ivc.initComposite(ec);
			// MacOs specific redraw bug workaround since 3.9
			if (CoreUtil.isMac() && ivc.getClass().getSimpleName().equals("DiagnoseViewContribution")) {
				form.getVerticalBar().addListener(SWT.Selection, e -> ret.redraw());
			}
			// end
			tk.adapt(ret);
			ec.setClient(ret);
		}

		ecZA = WidgetFactory.createExpandableComposite(tk, form, Messages.Patientenblatt2_contactForAdditionalAddress); // $NON-NLS-1$
		UserSettings.setExpandedState(ecZA, Messages.Patientenblatt2_contactForAdditionalAddress); // $NON-NLS-1$

		ecZA.addExpansionListener(ecExpansionListener);

		inpZusatzAdresse = new ListDisplay<BezugsKontakt>(ecZA, SWT.NONE, new ListDisplay.LDListener() {
			/*
			 * public boolean dropped(final PersistentObject dropped) { return false; }
			 */

			@Override
			public void hyperlinkActivated(final String l) {
				final String[] sortFields = new String[] { Kontakt.FLD_NAME1, Kontakt.FLD_NAME2, Kontakt.FLD_STREET };
				KontaktSelektor ksl = new KontaktSelektor(getShell(), Kontakt.class,
						Messages.Patientenblatt2_contactForAdditionalAddress,
						Messages.Patientenblatt2_pleaseSelectardress, sortFields);
				if (ksl.open() == Dialog.OK && actPatient != null) {
					Kontakt k = (Kontakt) ksl.getSelection();
					if (k != null) {
						BezugsKontaktAuswahl bza = new BezugsKontaktAuswahl(actPatient.getLabel(true),
								k.istPerson() ? Person.load(k.getId()).getLabel(true) : k.getLabel(true));
						if (bza.open() == Dialog.OK) {
							BezugsKontakt bk = actPatient.addBezugsKontakt(k, bza.getBezugKonkaktRelation());
							inpZusatzAdresse.add(bk);
							form.reflow(true);
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
					List<String> tokens = new ArrayList<String>();
					String mobile = other.get(Kontakt.FLD_MOBILEPHONE);

					String telefon1 = other.get(Kontakt.FLD_PHONE1);
					String telefon2 = other.get(Kontakt.FLD_PHONE2);
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

		// Hyperlink "Hinzu..." über der Adressliste hinzufügen
		inpZusatzAdresse.addHyperlinks(Messages.Core_Add); // $NON-NLS-1$

		// Das Kontext-Menü jedes Eintrags in der Adressliste erzeugen

		// inpZusatzAdresse.setMenu(createZusatzAdressMenu());
		inpZusatzAdresse.setMenu(removeZAAction, showZAAction, showBKAction, copySelectedContactInfosToClipboardAction,
				copySelectedAddressesToClipboardAction);

		ecZA.setClient(inpZusatzAdresse);

		// zusatz adressen
		compAdditionalAddresses = WidgetFactory.createExpandableComposite(tk, form,
				Messages.Patientenblatt2_additionalAdresses); // $NON-NLS-1$
		compAdditionalAddresses.addExpansionListener(ecExpansionListener);

		additionalAddresses = new ListDisplay<ZusatzAdresse>(compAdditionalAddresses, SWT.NONE,
				new ListDisplay.LDListener() {
					/*
					 * public boolean dropped(final PersistentObject dropped) { return false; }
					 */

					@Override
					public void hyperlinkActivated(final String l) {
						if (actPatient != null) {
							ZusatzAdresseEingabeDialog aed = new ZusatzAdresseEingabeDialog(form.getShell(),
									actPatient);
							if (aed.open() == Dialog.OK) {
								additionalAddresses.add(aed.getZusatzAdresse());
								form.reflow(true);
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

		// -------------------------------------------------------------

		for (int i = 0; i < lbExpandable.size(); i++) {
			ec.add(WidgetFactory.createExpandableComposite(tk, form, lbExpandable.get(i)));
			UserSettings.setExpandedState(ec.get(i), KEY_PATIENTENBLATT + lbExpandable.get(i));
			Text text = tk.createText(ec.get(i), StringUtils.EMPTY, SWT.MULTI | SWT.WRAP);
			FilterNonPrintableModifyListener.addTo(text);
			text.setData("index", Integer.valueOf(i));
			text.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					saveExpandable((Integer)text.getData("index"));
				}
			});
			txExpandable.add(text);
			ec.get(i).setData(KEY_DBFIELD, dfExpandable.get(i));
			ec.get(i).addExpansionListener(new ExpansionAdapter() {
				@Override
				public void expansionStateChanging(final ExpansionEvent e) {
					ExpandableComposite src = (ExpandableComposite) e.getSource();
					if (e.getState() == true) {
						Text tx = (Text) src.getClient();
						if (actPatient != null) {
							tx.setText(StringTool.unNull(actPatient.get((String) src.getData(KEY_DBFIELD))));
						} else {
							tx.setText(StringUtils.EMPTY);
						}
					} else {
						if (actPatient != null) {
							Text tx = (Text) src.getClient();
							if (tx.getText() != null) {
								actPatient.set((String) src.getData(KEY_DBFIELD), tx.getText());
							}
						}
					}
					UserSettings.saveExpandedState(KEY_PATIENTENBLATT + src.getText(), e.getState());
				}

			});
			txExpandable.get(i).addKeyListener(new KeyListener() {

				@Override
				public void keyReleased(KeyEvent e) {
					Text tx = (Text) e.getSource();
					tx.redraw();
					form.getBody().layout(true);
				}

				@Override
				public void keyPressed(KeyEvent e) {
				}
			});

			ec.get(i).setClient(txExpandable.get(i));
		}
		ecdm = WidgetFactory.createExpandableComposite(tk, form, FIXMEDIKATION);
		UserSettings.setExpandedState(ecdm, KEY_PATIENTENBLATT + FIXMEDIKATION);
		ecdm.addExpansionListener(ecExpansionListener);
		dmd = new FixMediDisplay(ecdm, site);
		ecdm.setClient(dmd);

		List<IViewContribution> lContrib = ViewContributionHelper
				.getFilteredAndPositionSortedContributions(detailComposites, 1);
		for (IViewContribution ivc : lContrib) {
			ExpandableComposite ec = WidgetFactory.createExpandableComposite(tk, form, ivc.getLocalizedTitle());
			ec.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			UserSettings.setExpandedState(ec, KEY_PATIENTENBLATT + ec.getText());
			ec.addExpansionListener(ecExpansionListener);
			Composite ret = ivc.initComposite(ec);
			tk.adapt(ret);
			ec.setClient(ret);
		}

		viewmenu = new ViewMenus(viewsite);
		viewmenu.createMenu(GlobalActions.printEtikette, GlobalActions.printAdresse, GlobalActions.printBlatt,
				GlobalActions.showBlatt, GlobalActions.printRoeBlatt, copySelectedContactInfosToClipboardAction,
				copySelectedAddressesToClipboardAction);

		viewmenu.createToolbar(copySelectedContactInfosToClipboardAction);
		viewmenu.createToolbar(copySelectedAddressesToClipboardAction);
		ElexisEventDispatcher.getInstance().addListeners(eeli_pat_sync, eeli_pat, eeli_user);
		tk.paintBordersFor(form.getBody());
	}

	protected void save() {
		if (actPatient != null) {
			if (ipp != null) {
				ipp.save();
			}
			for (int i = 0; i < txExpandable.size(); i++) {
				saveExpandable(i);
			}
		}
	}

	protected void saveExpandable(Integer i) {
		if(i != null) {
			String field = dfExpandable.get(i);
			String oldvalue = StringTool.unNull(actPatient.get(field));
			String newvalue = txExpandable.get(i).getText();
			if (bLocked) {
				txExpandable.get(i).setText(oldvalue);
			} else {
				actPatient.set(field, newvalue);
			}			
		}
	}

	
	@Override
	public void dispose() {
		ElexisEventDispatcher.getInstance().removeListeners(eeli_pat_sync, eeli_pat, eeli_user);
		super.dispose();
	}

	/*
	 * private Menu createZusatzAdressMenu() { Menu ret = new
	 * Menu(inpZusatzAdresse); delZA = new MenuItem(ret, SWT.NONE);
	 * delZA.setText(Messages.getString("Patientenblatt2.removeAddress"));
	 * //$NON-NLS-1$ delZA.addSelectionListener(new SelectionAdapter() {
	 *
	 * @Override public void widgetSelected(final SelectionEvent e) { if (!bLocked)
	 * { BezugsKontakt a = (BezugsKontakt) inpZusatzAdresse .getSelection();
	 * actPatient.removeBezugsKontakt(Kontakt.load(a .get(BezugsKontakt.OTHER_ID)));
	 * setPatient(actPatient); } }
	 *
	 * }); MenuItem showZA = new MenuItem(ret, SWT.NONE);
	 * showZA.setText(Messages.getString("Patientenblatt2.showAddress"));
	 * //$NON-NLS-1$ showZA.addSelectionListener(new SelectionAdapter() {
	 *
	 * @Override public void widgetSelected(final SelectionEvent e) { Kontakt a =
	 * Kontakt.load(((BezugsKontakt) inpZusatzAdresse
	 * .getSelection()).get(BezugsKontakt.OTHER_ID)); KontaktDetailDialog kdd = new
	 * KontaktDetailDialog(form .getShell(), a); kdd.open(); } }); return ret; }
	 */

	class Hyperlinkreact extends HyperlinkAdapter {

		@Override
		@SuppressWarnings("synthetic-access")
		public void linkActivated(final HyperlinkEvent e) {
			if (actPatient != null) {
				AnschriftEingabeDialog aed = new AnschriftEingabeDialog(form.getShell(), actPatient);
				aed.open();
				inpAdresse.setText(actPatient.getPostAnschrift(false), false, false);
			}
		}
	}

	public void setPatient(final Patient p) {
		actPatient = p;

		refreshUi();

		setUnlocked(LocalLockServiceHolder.get().isLockedLocal(p));
	}

	public void refreshUi() {
		ipp.getAutoForm().reload(actPatient);

		detailComposites.forEach(dc -> dc.setDetailObject(actPatient, null));
		buttonTabContributions.forEach(dc -> dc.setDetailObject(actPatient, null));

		if (actPatient == null) {
			form.setText(Messages.Core_No_patient_selected); // $NON-NLS-1$
			inpAdresse.setText(StringConstants.EMPTY, false, false);
			deceasedBtn.setSelection(false);
			increasedTreatmentBtn.setSelection(false);
			inpZusatzAdresse.clear();
			setUnlocked(false);
			return;
		}
		IPatient patient = NoPoUtil.loadAsIdentifiable(actPatient, IPatient.class).get();
		deceasedBtn.setSelection(patient.isDeceased());
		if (patient.getExtInfo(PatientConstants.FLD_EXTINFO_INCREASEDTREATMENT) instanceof String) {
			increasedTreatmentBtn.setSelection(
					Boolean.parseBoolean((String) patient.getExtInfo(PatientConstants.FLD_EXTINFO_INCREASEDTREATMENT)));
		} else {
			increasedTreatmentBtn.setSelection(false);
		}
		if (patient.isDeceased()) {
			if (patient.getDateOfDeath() != null) {
				deceasedDate
						.setSelection(Date.from(patient.getDateOfDeath().atZone(ZoneId.systemDefault()).toInstant()));
			} else {
				deceasedDate.setSelection(null);
			}
			((GridData) deceasedDate.getLayoutData()).exclude = false;
			deceasedDate.setVisible(true);
		} else {
			deceasedDate.setSelection(null);
			((GridData) deceasedDate.getLayoutData()).exclude = true;
			deceasedDate.setVisible(false);
		}

		stickerComposite.setPatient(CoreModelServiceHolder.get().load(actPatient.getId(), IPatient.class).orElse(null));
		form.setText(StringTool.unNull(actPatient.getName()) + StringConstants.SPACE
				+ StringTool.unNull(actPatient.getVorname()) + " (" //$NON-NLS-1$
				+ actPatient.getPatCode() + ")"); //$NON-NLS-1$
		inpAdresse.setText(actPatient.getPostAnschrift(false), false, false);
		UserSettings.setExpandedState(ecZA, "Patientenblatt/Zusatzadressen"); //$NON-NLS-1$
		inpZusatzAdresse.clear();
		for (BezugsKontakt za : actPatient.getBezugsKontakte()) {
			inpZusatzAdresse.add(za);
		}

		additionalAddresses.clear();
		for (ZusatzAdresse zusatzAdresse : actPatient.getZusatzAdressen()) {
			additionalAddresses.add(zusatzAdresse);
		}

		for (int i = 0; i < dfExpandable.size(); i++) {
			UserSettings.setExpandedState(ec.get(i), KEY_PATIENTENBLATT + ec.get(i).getText());
			txExpandable.get(i).setText(StringTool.unNull(actPatient.get(dfExpandable.get(i))));
		}
		dmd.reload();

		updateExpandableLayoutWidth();
		refresh();
	}

	public void refresh() {
		form.reflow(true);
	}

	private void makeAdditionalAddressActions() {
		removeAdditionalAddressAction = new Action(Messages.Patientenblatt2_removeAddress) {
			@Override
			public void run() {
				if (!bLocked) {
					ZusatzAdresse a = additionalAddresses.getSelection();
					a.delete();
					setPatient(actPatient);
				}
			}
		};

		showAdditionalAddressAction = new Action(Messages.Patientenblatt2_showAddress) {
			@Override
			public void run() {
				ZusatzAdresse zusatzAdresse = additionalAddresses.getSelection();
				ZusatzAdresseEingabeDialog aed = new ZusatzAdresseEingabeDialog(form.getShell(), actPatient,
						zusatzAdresse, bLocked);
				if (aed.open() == Dialog.OK) {
					setPatient(actPatient);
				}
			}
		};
	}

	private void makeActions() {
		removeZAAction = new Action(Messages.Patientenblatt2_removeAddress) {
			@Override
			public void run() {
				if (!bLocked) {
					BezugsKontakt a = inpZusatzAdresse.getSelection();
					a.delete();
					setPatient(actPatient);
				}
			}
		};

		showZAAction = new RestrictedAction(AccessControlDefaults.PATIENT_DISPLAY,
				Messages.Patientenblatt2_showAddress) {
			@Override
			public void doRun() {
				Kontakt a = Kontakt.load(inpZusatzAdresse.getSelection().get(BezugsKontakt.OTHER_ID));
				KontaktDetailDialog kdd = new KontaktDetailDialog(form.getShell(), a, bLocked);
				if (kdd.open() == Dialog.OK) {
					setPatient(actPatient);
				}
			}
		};

		showBKAction = new RestrictedAction(AccessControlDefaults.PATIENT_DISPLAY,
				Messages.Patientenblatt2_showBezugKontaktRelation) {
			@Override
			public void doRun() {
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
		};

		/*
		 * adopted from KontakteView.java Copy selected contact data (complete) to the
		 * clipboard, so it/they can be easily pasted into a target document for various
		 * further usage. This variant produces a more complete data set than
		 * copySelectedAddresses... below; it also includes the phone numbers and does
		 * not use the postal address, but all the individual data fields. Two actions
		 * with identical / similar code has also been added to PatientenListeView.java
		 */
		copySelectedContactInfosToClipboardAction = new Action(
				Messages.KontakteView_copySelectedContactInfosToClipboard) { // $NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_CLIPBOARD.getImageDescriptor());
				setToolTipText(Messages.KontakteView_copySelectedContactInfosToClipboard); // $NON-NLS-1$
			}

			@Override
			public void run() {

				// Adopted from KontakteView.printList:
				// Convert the selected contacts into a list

				StringBuffer SelectedContactInfosText = new StringBuffer();

				// Here's a handling difference between Patientenblatt2.java and
				// KontakteView.java:
				// Apparently, the method getSelection() from ListDisplay.java
				// returns only the
				// first selected entry.
				// Actually, in the List of addresses in section "Hinzu", it is
				// only possible to
				// select one address at a time.
				// Moreover, it doesn't return Object[], but T, which is a list
				// to be displayed
				// with GUI, extending composite.
				// Object[] sel = inpZusatzAdresse.getSelection(); //This would
				// not work for
				// inpZusatzAdresse
				BezugsKontakt sel = inpZusatzAdresse.getSelection(); // This
				// works, but returns only one entry.

				// If you enable the following line for debug output,
				// you should also enable the
				// SelectedContactInfosText.setLength(0) line below,
				// and enable output of SelectedContactInfosText even for the
				// case of an empty
				// selection further below.
				// SelectedContactInfosText.append("jsdebug: Sorry, your
				// selection is empty.");

				// if (sel != null && sel.length > 0) { //This would not work
				// for
				// inpZusatzAdresse
				if (sel != null) {
					// SelectedContactInfosText.setLength(0);
					// SelectedContactInfosText.append("jsdebug: Your selection
					// includes "+sel.length+"
					// element(s):"+System.getProperty("line.separator"));

					// for (int i = 0; i < sel.length; i++) { //This would not
					// work for
					// inpZusatzAdresse
					// Kontakt k = (Kontakt) sel[i]; //This would not work for
					// inpZusatzAdresse
					Kontakt k = sel.getBezugsKontakt();

					// System.out.print("jsdebug:
					// SelectedContactInfos.k.toString():
					// \n"+k.toString()+StringUtils.LF);

					// The following code is adopted from
					// Kontakt.createStdAnschrift for a
					// different purpose/layout:
					// ggf. hier zu Person.getPersonalia() eine abgewandelte
					// Fassung hinzufügen
					// und von hier aus aufrufen.

					// This highly similar (but still different) code has been
					// adopted from my
					// addition
					// to PatientenListeView.java
					// CopySelectedPatInfosToClipboard...
					// 201202161313js

					// optional code; this could be made configurable. For now:
					// disabled by if
					// (false)...
					if (false) {
						// I put the field of "Kürzel" in front. It contains a
						// Patient ID
						// number,
						// and optionally kk... for health insurances, or vn
						// initials as Vorname
						// Nachname for physicians.
						String thisKontaktFLD_SHORT_LABEL = k.get(k.FLD_SHORT_LABEL); // $NON-NLS-1$
						if (!StringTool.isNothing(thisKontaktFLD_SHORT_LABEL)) {
							SelectedContactInfosText.append(thisKontaktFLD_SHORT_LABEL).append(",") //$NON-NLS-1$
									.append(StringTool.space);
						}
					}

					if (k.istPerson()) {
						// Here, we need to look at the Person variant of a
						// Kontakt to obtain
						// their sex; 201202161326js
						// Kontakt cannot simply be cast to Person - if we try,
						// we'll throw an
						// error, and the remainder of this action will be
						// ignored.
						// Person p = (Person) sel[i]; //THIS WILL NOT WORK.
						// So obtain the corresponding Person for a Kontakt via
						// the ID:
						Person p = Person.load(k.getId());

						String salutation;
						// TODO default salutation might be configurable (or a
						// "Sex missing!"
						// Info might appear) js
						if (p.getGeschlecht().equals(Person.MALE)) {
							salutation = Messages.Contact_SalutationM; // $NON-NLS-1$
						} else // We do not use any default salutation for
								// unknown sex to avoid
								// errors!
						if (p.getGeschlecht().equals(Person.FEMALE)) {
							salutation = Messages.Contact_SalutationF; // $NON-NLS-1$
						} else {
							salutation = StringUtils.EMPTY;
						}

						if (!StringTool.isNothing(salutation)) { // salutation
																	// should
																	// currently
																	// never be empty, but paranoia...
							SelectedContactInfosText.append(salutation);
							SelectedContactInfosText.append(StringTool.space);
						}

						String titel = p.get(p.TITLE); // $NON-NLS-1$
						if (!StringTool.isNothing(titel)) {
							SelectedContactInfosText.append(titel).append(StringTool.space);
						}
						// A comma between Family Name and Given Name would be
						// generally helpful
						// to reliably tell them apart:
						// SelectedContactInfosText.append(k.getName()+","+StringTool.space+k.getVorname());
						// But Jürg Hamacher prefers this in his letters without
						// a comma in
						// between:
						// SelectedContactInfosText.append(p.getName()+StringTool.space+p.getVorname());
						// Whereas I use the above variant for
						// PatientenListeView.java;
						// I put the Vorname first in KontakteView. And I only
						// use a spacer, if
						// the first field is not empty!
						// SelectedContactInfosText.append(p.getVorname()+StringTool.space+p.getName());
						if (!StringTool.isNothing(p.getVorname())) {
							SelectedContactInfosText.append(p.getVorname() + StringTool.space);
						}
						if (!StringTool.isNothing(p.getName())) {
							SelectedContactInfosText.append(p.getName());
						}

						// Also, in KontakteView, I copy the content of fields
						// "Bemerkung" and
						// "Zusatz" as well.
						// "Zusatz" is mapped to "Bezeichnung3" in Person.java.
						String thisPersonFLD_REMARK = p.get(p.FLD_REMARK); // $NON-NLS-1$
						if (!StringTool.isNothing(thisPersonFLD_REMARK)) {
							SelectedContactInfosText.append(",").append(StringTool.space).append(thisPersonFLD_REMARK); //$NON-NLS-1$
						}
						String thisPersonFLD_NAME3 = p.get(p.FLD_NAME3); // $NON-NLS-1$
						if (!StringTool.isNothing(thisPersonFLD_NAME3)) {
							SelectedContactInfosText.append(",").append(StringTool.space).append(thisPersonFLD_NAME3); //$NON-NLS-1$
						}

						String thisPatientBIRTHDATE = p.get(p.BIRTHDATE);
						if (!StringTool.isNothing(thisPatientBIRTHDATE)) {
							// This would add the term "geb." (born on the)
							// before the date of
							// birth:
							// SelectedContactInfosText.append(","+StringTool.space+"geb."+StringTool.space+new
							// TimeTool(thisPatientBIRTHDATE).toString(TimeTool.DATE_GER));
							// But Jürg Hamacher prefers the patient information
							// in his letters
							// without that term:
							SelectedContactInfosText.append("," + StringTool.space //$NON-NLS-1$
									+ new TimeTool(thisPatientBIRTHDATE).toString(TimeTool.DATE_GER));
						}
					} else { // if (k.istPerson())... else...
						String thisAddressFLD_NAME1 = k.get(k.FLD_NAME1);
						String thisAddressFLD_NAME2 = k.get(k.FLD_NAME2);
						String thisAddressFLD_NAME3 = k.get(k.FLD_NAME3);
						if (!StringTool.isNothing(thisAddressFLD_NAME1)) {
							SelectedContactInfosText.append(thisAddressFLD_NAME1);
							if (!StringTool.isNothing(thisAddressFLD_NAME2 + thisAddressFLD_NAME3)) {
								SelectedContactInfosText.append(StringTool.space);
							}
						}
						if (!StringTool.isNothing(thisAddressFLD_NAME2)) {
							SelectedContactInfosText.append(thisAddressFLD_NAME2);
						}
						if (!StringTool.isNothing(thisAddressFLD_NAME3)) {
							SelectedContactInfosText.append(thisAddressFLD_NAME3);
						}
						if (!StringTool.isNothing(thisAddressFLD_NAME3)) {
							SelectedContactInfosText.append(StringTool.space);
						}
					}

					String thisAddressFLD_STREET = k.get(k.FLD_STREET);
					if (!StringTool.isNothing(thisAddressFLD_STREET)) {
						SelectedContactInfosText.append("," + StringTool.space + thisAddressFLD_STREET); //$NON-NLS-1$
					}

					String thisAddressFLD_COUNTRY = k.get(k.FLD_COUNTRY);
					if (!StringTool.isNothing(thisAddressFLD_COUNTRY)) {
						SelectedContactInfosText.append("," + StringTool.space + thisAddressFLD_COUNTRY + "-"); //$NON-NLS-1$ //$NON-NLS-2$
					}

					String thisAddressFLD_ZIP = k.get(k.FLD_ZIP);
					if (!StringTool.isNothing(thisAddressFLD_ZIP)) {
						if (StringTool.isNothing(thisAddressFLD_COUNTRY)) {
							SelectedContactInfosText.append("," + StringTool.space); //$NON-NLS-1$
						}
						;
						SelectedContactInfosText.append(thisAddressFLD_ZIP);
					}
					;

					String thisAddressFLD_PLACE = k.get(k.FLD_PLACE);
					if (!StringTool.isNothing(thisAddressFLD_PLACE)) {
						if (StringTool.isNothing(thisAddressFLD_COUNTRY) && StringTool.isNothing(thisAddressFLD_ZIP)) {
							SelectedContactInfosText.append(","); //$NON-NLS-1$
						}
						;
						SelectedContactInfosText.append(StringTool.space + thisAddressFLD_PLACE);
					}

					String thisAddressFLD_MOBILEPHONE = k.get(k.FLD_MOBILEPHONE);
					if (!StringTool.isNothing(thisAddressFLD_MOBILEPHONE)) {
						// With a colon after the label:
						// SelectedContactInfosText.append(","+StringTool.space+k.FLD_MOBILEPHONE+":"+StringTool.space+thisAddressFLD_MOBILEPHONE);
						// Without a colon after the label:
						SelectedContactInfosText.append("," + StringTool.space + k.FLD_MOBILEPHONE + StringTool.space //$NON-NLS-1$
								+ thisAddressFLD_MOBILEPHONE);
					}
					String thisAddressFLD_PHONE1 = k.get(k.FLD_PHONE1);
					if (!StringTool.isNothing(thisAddressFLD_PHONE1)) {
						SelectedContactInfosText
								.append("," + StringTool.space + StringTool.space + thisAddressFLD_PHONE1); //$NON-NLS-1$
					}

					String thisAddressFLD_PHONE2 = k.get(k.FLD_PHONE2);
					if (!StringTool.isNothing(thisAddressFLD_PHONE2)) {
						SelectedContactInfosText
								.append("," + StringTool.space + StringTool.space + thisAddressFLD_PHONE2); //$NON-NLS-1$
					}

					String thisAddressFLD_FAX = k.get(k.FLD_FAX);
					if (!StringTool.isNothing(thisAddressFLD_FAX)) {
						// With a colon after the label:
						// SelectedContactInfosText.append(","+StringTool.space+k.FLD_FAX+":"+StringTool.space+thisAddressFLD_FAX);
						// Without a colon after the label:
						SelectedContactInfosText
								.append("," + StringTool.space + k.FLD_FAX + StringTool.space + thisAddressFLD_FAX); //$NON-NLS-1$
					}

					String thisAddressFLD_E_MAIL = k.get(k.FLD_E_MAIL);
					if (!StringTool.isNothing(thisAddressFLD_E_MAIL)) {
						SelectedContactInfosText.append("," + StringTool.space + thisAddressFLD_E_MAIL); //$NON-NLS-1$
					}

					/*
					 * //This would not work for inpZusatzAdresse //Add another empty line (or
					 * rather: paragraph), if at least one more address will follow. if
					 * (i<sel.length-1) { SelectedContactInfosText.append(System.getProperty(
					 * "line.separator")); }
					 */

					// } // for each element in sel do //This would not work for
					// inpZusatzAdresse

					/*
					 * I would prefer to move the following code portions down behind the
					 * "if sel not empty" block, so that (a) debugging output can be produced and
					 * (b) the clipboard will be emptied when NO Contacts have been selected. I did
					 * this to avoid the case where a user would assume they had selected some
					 * address, copied data to the clipboard, and pasted them - and, even when they
					 * erred about their selection, which was indeed empty, they would not
					 * immediately notice that because some (old, unchanged) content would still
					 * come out of the clipboard.
					 *
					 * But if I do so, and there actually is no address selected, I get an error
					 * window: Unhandled Exception ... not valid. So to avoid that message without
					 * any further research (I need to get this work fast now), I move the code back
					 * up and leave the clipboard unchanged for now, if no Contacts had been
					 * selected to process.
					 *
					 * (However, I may disable the toolbar icon / menu entry for this action in that
					 * case later on.)
					 */

					// System.out.print("jsdebug: SelectedContactInfosText:
					// \n"+SelectedContactInfosText+StringUtils.LF);

					// Adopted from BestellView.exportClipboardAction:
					// Copy some generated object.toString() to the clipoard

					Clipboard clipboard = new Clipboard(UiDesk.getDisplay());
					TextTransfer textTransfer = TextTransfer.getInstance();
					Transfer[] transfers = new Transfer[] { textTransfer };
					Object[] data = new Object[] { SelectedContactInfosText.toString() };
					clipboard.setContents(data, transfers);
					clipboard.dispose();
				} // if sel not empty
			}; // copySelectedContactInfosToClipboardAction.run()
		};

		/*
		 * adopted from KontakteView.java Copy selected address(es) to the clipboard, so
		 * it/they can be easily pasted into a letter for printing. Two actions with
		 * identical / similar code has also been added to PatientenListeView.java
		 */
		copySelectedAddressesToClipboardAction = new Action(Messages.KontakteView_copySelectedAddressesToClipboard) { // $NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_CLIPBOARD.getImageDescriptor());
				setToolTipText(Messages.KontakteView_copySelectedAddressesToClipboard); // $NON-NLS-1$
			}

			@Override
			public void run() {

				// Adopted from KontakteView.printList:
				// Convert the selected addresses into a list

				StringBuffer selectedAddressesText = new StringBuffer();

				// Here's a handling difference between Patientenblatt2.java and
				// KontakteView.java:
				// Apparently, the method getSelection() from ListDisplay.java
				// returns only the
				// first selected entry.
				// Actually, in the List of addresses in section "Hinzu", it is
				// only possible to
				// select one address at a time.
				// Moreover, it doesn't return Object[], but T, which is a list
				// to be displayed
				// with GUI, extending composite.
				// Object[] sel = inpZusatzAdresse.getSelection(); //This would
				// not work for
				// inpZusatzAdresse
				BezugsKontakt sel = inpZusatzAdresse.getSelection(); // This
				// works, but returns only one entry.

				// If you enable the following line for debug output,
				// you should also enable the
				// SelectedContactInfosText.setLength(0) line below,
				// and enable output of SelectedContactInfosText even for the
				// case of an empty
				// selection further below.
				// SelectedContactInfosText.append("jsdebug: Sorry, your
				// selection is empty.");

				// if (sel != null && sel.length > 0) { //This would not work
				// for
				// inpZusatzAdresse
				if (sel != null) {
					// SelectedContactInfosText.setLength(0);
					// SelectedContactInfosText.append("jsdebug: Your selection
					// includes "+sel.length+"
					// element(s):"+System.getProperty("line.separator"));

					// for (int i = 0; i < sel.length; i++) { //This would not
					// work for
					// inpZusatzAdresse
					// Kontakt k = (Kontakt) sel[i]; //This would not work for
					// inpZusatzAdresse
					Kontakt k = sel.getBezugsKontakt();

					/*
					 * Synthesize the address lines to output from the entries in Kontakt k; added
					 * to implement the output format desired for the copyAddressToClipboard()
					 * buttons added to version 2.1.6.js as of 2012-01-28ff
					 *
					 * We might synthesize our own "Anschrift" for each Kontakt, completely
					 * according to our own requirements, OR use any of the methods defined for
					 * Kontakt like: getLabel...(), getPostAnschrift, createStandardAnschrift,
					 * List<BezugsKontakt>... -
					 *
					 * The Declaration of Kontakt with field definitions is available in
					 * Kontakt.java, please look therein for additional details, please. Click-Right
					 * -> Declaration on Kontakt in Eclipse works. You can also look above to see
					 * the fields that printList would use.
					 */

					// selectedAddressesText.append("jsdebug: Item
					// "+Integer.toString(i)+"
					// "+k.toString()+System.getProperty("line.separator"));

					// getPostAnschriftPhoneFaxEmail() already returns a line
					// separator after
					// the address
					// The first parameter controls multiline or single line
					// output
					// The second parameter controls whether the phone numbers
					// shall be included
					selectedAddressesText.append(k.getPostAnschriftPhoneFaxEmail(true, true));

					/*
					 * //This would not work for inpZusatzAdresse //Add another empty line (or
					 * rather: paragraph), if at least one more address will follow. if
					 * (i<sel.length-1) { selectedAddressesText.append(System.getProperty(
					 * "line.separator"));
					 *
					 * }
					 */

					// } // for each element in sel do //This would not work for
					// inpZusatzAdresse

					/*
					 * I would prefer to move the following code portions down behind the
					 * "if sel not empty" block, so that (a) debugging output can be produced and
					 * (b) the clipboard will be emptied when NO addresses have been selected. I did
					 * this to avoid the case where a user would assume they had selected some
					 * address, copied data to the clipboard, and pasted them - and, even when they
					 * erred about their selection, which was indeed empty, they would not
					 * immediately notice that because some (old, unchanged) content would still
					 * come out of the clipboard.
					 *
					 * But if I do so, and there actually is no address selected, I get an error
					 * window: Unhandled Exception ... not valid. So to avoid that message without
					 * any further research (I need to get this work fast now), I move the code back
					 * up and leave the clipboard unchanged for now, if no addresses had been
					 * selected to process.
					 *
					 * (However, I may disable the toolbar icon / menu entry for this action in that
					 * case later on.)
					 */

					// System.out.print("jsdebug: selectedAddressesText:
					// \n"+selectedAddressesText+StringUtils.LF);

					// Adopted from BestellView.exportClipboardAction:
					// Copy some generated object.toString() to the clipoard

					Clipboard clipboard = new Clipboard(UiDesk.getDisplay());
					TextTransfer textTransfer = TextTransfer.getInstance();
					Transfer[] transfers = new Transfer[] { textTransfer };
					Object[] data = new Object[] { selectedAddressesText.toString() };
					clipboard.setContents(data, transfers);
					clipboard.dispose();
				} // if sel not empty
			}; // copySelectedAddressesToClipboardAction.run()

		};
	}

	private void updateExpandableLayoutWidth() {
		if (ec != null && form != null && !form.isDisposed()) {
			for (ExpandableComposite expandable : ec) {
				if (expandable.getLayoutData() instanceof GridData) {
					((GridData) expandable.getLayoutData()).widthHint = Patientenblatt2.this.getClientArea().width;
				}
			}
			form.getBody().layout(true);
		}
	}

	@Override
	public void setUnlocked(boolean unlocked) {
		// update enabled / locked before resetting bLocked
		// else new value is reset in saveExpandable on focus lost
		for (ExpandableComposite ex : ec) {
			ex.getClient().setEnabled(unlocked);
		}
		detailComposites.forEach(dc -> dc.setUnlocked(unlocked));
		bLocked = !unlocked;
		ipp.setUnlocked(unlocked);
		inpZusatzAdresse.setUnlocked(unlocked);
		hHA.setEnabled(unlocked);
		// delZA.setEnabled(!bLock);
		removeZAAction.setEnabled(unlocked);
		removeAdditionalAddressAction.setEnabled(unlocked);
		additionalAddresses.setUnlocked(unlocked);
		dmd.setUnlocked(unlocked);
		if (unlocked) {
			hHA.setForeground(UiDesk.getColor(UiDesk.COL_BLUE));
		} else {
			hHA.setForeground(UiDesk.getColor(UiDesk.COL_GREY));

		}
	}
}
