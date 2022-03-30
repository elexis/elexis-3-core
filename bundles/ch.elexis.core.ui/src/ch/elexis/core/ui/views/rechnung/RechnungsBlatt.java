/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
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

import static ch.elexis.core.ui.constants.ExtensionPointConstantsUi.VIEWCONTRIBUTION;
import static ch.elexis.core.ui.constants.ExtensionPointConstantsUi.VIEWCONTRIBUTION_CLASS;
import static ch.elexis.core.ui.constants.ExtensionPointConstantsUi.VIEWCONTRIBUTION_VIEWID;
import static ch.elexis.core.ui.constants.UiPreferenceConstants.USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_CLOSED;
import static ch.elexis.core.ui.constants.UiPreferenceConstants.USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_OPEN;
import static ch.elexis.core.ui.constants.UiPreferenceConstants.USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_REMEMBER_STATE;
import static ch.elexis.core.ui.constants.UiPreferenceConstants.USERSETTINGS2_EXPANDABLE_COMPOSITES;
import static ch.elexis.core.ui.constants.UiPreferenceConstants.USERSETTINGS2_EXPANDABLE_COMPOSITES_STATES;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.handlers.IHandlerService;
import org.slf4j.LoggerFactory;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListenerImpl;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.documents.DocumentStore;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IDiagnosisReference;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IInvoiceBilled;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.e4.controls.IIdentifiableModifiableListComposite;
import ch.elexis.core.ui.e4.dialog.GenericSelectionDialog;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.util.LabeledInputField;
import ch.elexis.core.ui.util.LabeledInputField.InputData;
import ch.elexis.core.ui.util.LabeledInputField.InputData.Typ;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.WidgetFactory;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.core.ui.views.contribution.IViewContribution;
import ch.elexis.core.ui.views.contribution.ViewContributionHelper;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.elexis.data.Anwender;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.data.Zahlung;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class RechnungsBlatt extends Composite implements IActivationListener {

	private static final String KEY_RECHNUNGSBLATT = "RechnungsBlatt/"; //$NON-NLS-1$
	IViewSite site;
	IIdentifiableModifiableListComposite<IDocument> attachments;
	ListViewer buchungen;
	org.eclipse.swt.widgets.List lbJournal;
	org.eclipse.swt.widgets.List lbOutputs;
	Rechnung actRn;
	ScrolledForm form;
	FormToolkit tk = UiDesk.getToolkit();
	// Button bBuchung,bPrint,bStorno,bGebuehr,bGutschrift;
	Text tRejects, tBemerkungen, tInternalRemarks;
	Label rnAdressat;
	ListViewer konsultationenViewer;
	ListViewer stornoViewer;

	private final ExpandableComposite ecAttachments;
	private final ExpandableComposite ecBuchungen;
	private final ExpandableComposite ecBemerkungen;
	private final ExpandableComposite ecInternalRemarks;
	private final ExpandableComposite ecStatus;
	private final ExpandableComposite ecFehler;
	private final ExpandableComposite ecAusgaben;
	private final ExpandableComposite ecKons;
	private final ExpandableComposite ecStorno;

	@SuppressWarnings("unchecked")
	private final List<IViewContribution> detailComposites = Extensions.getClasses(VIEWCONTRIBUTION,
			VIEWCONTRIBUTION_CLASS, VIEWCONTRIBUTION_VIEWID, RnDetailView.ID);

	private static LabeledInputField.IContentProvider openAmountContentProvider = new LabeledInputField.IContentProvider() {

		@Override
		public void displayContent(Object po, InputData ltf) {
			Rechnung invoice = (Rechnung) po;
			Money openAmount = invoice.getOffenerBetrag();
			ltf.setText(openAmount.getAmountAsString());
			if (InvoiceState.CANCELLED.numericValue() == invoice.getStatus()) {
				ltf.setLabel(Messages.RechnungsBlatt_compensateAmount);
			} else {
				ltf.setLabel(Messages.RechnungsBlatt_amountOpen);
			}
		}

		@Override
		public void reloadContent(Object po, InputData ltf) {
			Rechnung invoice = (Rechnung) po;
			if (InvoiceState.CANCELLED.numericValue() == invoice.getStatus()) {
				Money openAmount = invoice.getOffenerBetrag();
				if (openAmount.isZero()) {
					return;
				}
				if (!CoreHub.acl.request(AccessControlDefaults.ACCOUNTING_BILLMODIFY)) {
					MessageDialog.openError(Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"Insufficient rights", "You are not authorized to perform this action");
					return;
				}
			}

			try {
				RnDialogs.BuchungHinzuDialog comp = new RnDialogs.BuchungHinzuDialog(
						Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getShell(), invoice, true);
				comp.open();
				ElexisEventDispatcher.update(invoice);
			} catch (ElexisException e) {
				SWTHelper.showError("Buchung kann nicht hinzugefügt werden", e.getLocalizedMessage());
			}
		}

	};

	private static final InputData[] rndata = {
			new InputData(Messages.RechnungsBlatt_billNumber, Rechnung.BILL_NUMBER, Typ.STRING, null),
			new InputData(Messages.RechnungsBlatt_billDate, Rechnung.BILL_DATE, Typ.STRING, null), new InputData(
					Messages.RechnungsBlatt_billState, Rechnung.BILL_STATE, new LabeledInputField.IContentProvider() {

						@Override
						public void displayContent(Object po, InputData ltf) {
							Rechnung r = (Rechnung) po;
							ltf.setText(RnStatus.getStatusText(r.getStatus()));

						}

						@Override
						public void reloadContent(Object po, InputData ltf) {
							if (new RnDialogs.StatusAendernDialog(
									Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getShell(), (Rechnung) po)
									.open() == Dialog.OK) {
								ElexisEventDispatcher.update((PersistentObject) po);
							}
						}

					}),
			new InputData(Messages.RechnungsBlatt_treatmentsFrom, Rechnung.BILL_DATE_FROM, Typ.STRING, null),
			new InputData(Messages.RechnungsBlatt_treatmentsUntil, Rechnung.BILL_DATE_UNTIL, Typ.STRING, null),
			new InputData(Messages.RechnungsBlatt_amountTotal, Rechnung.BILL_AMOUNT_CENTS, Typ.CURRENCY, null),
			new InputData(Messages.RechnungsBlatt_amountOpen, Rechnung.BILL_AMOUNT_CENTS, openAmountContentProvider)};
	private LabeledInputField.AutoForm rnform;

	private final ElexisEventListenerImpl eeli_rn = new ElexisUiEventListenerImpl(Rechnung.class,
			ElexisEvent.EVENT_CREATE | ElexisEvent.EVENT_DELETE | ElexisEvent.EVENT_UPDATE | ElexisEvent.EVENT_SELECTED
					| ElexisEvent.EVENT_DESELECTED) {

		@Override
		public void runInUi(ElexisEvent ev) {
			switch (ev.getType()) {
				case ElexisEvent.EVENT_UPDATE :
					doSelect((Rechnung) ev.getObject());
					break;
				case ElexisEvent.EVENT_DESELECTED : // fall thru
					doSelect(null);
					break;
				case ElexisEvent.EVENT_DELETE :
					if (actRn != null && actRn.getId().equals(ev.getObject().getId())) {
						doSelect(null);
					}
					break;
				case ElexisEvent.EVENT_SELECTED :
					doSelect((Rechnung) ev.getObject());
					break;
			}
		}
	};

	private final ElexisEventListenerImpl eeli_user = new ElexisUiEventListenerImpl(Anwender.class,
			ElexisEvent.EVENT_USER_CHANGED) {

		@Override
		public void runInUi(ElexisEvent ev) {
			display();
		}
	};

	private final ElexisEventListenerImpl eeli_patient = new ElexisUiEventListenerImpl(Patient.class,
			ElexisEvent.EVENT_SELECTED | ElexisEvent.EVENT_DESELECTED) {

		@Override
		public void runInUi(ElexisEvent ev) {
			Patient pat = (Patient) ev.getObject();
			switch (ev.getType()) {
				case ElexisEvent.EVENT_DESELECTED : // fall thru
					doSelect(null);
					break;
				case ElexisEvent.EVENT_SELECTED :
					if (actRn != null) {
						Fall fall = actRn.getFall();
						if (fall.exists()) {
							Patient patient = fall.getPatient();
							if (!Objects.equals(pat, patient)) {
								doSelect(null);
							}
						}
					}
					break;
			}
		}
	};

	@SuppressWarnings("unchecked")
	public RechnungsBlatt(Composite parent, IViewSite site) {
		super(parent, SWT.NONE);
		this.site = site;
		setLayout(new GridLayout());
		form = tk.createScrolledForm(this);
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		// TableWrapLayout twl=new TableWrapLayout();
		Composite body = form.getBody();
		body.setLayout(new GridLayout());
		// body.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		rnform = new LabeledInputField.AutoForm(body, rndata, 2, 3);
		// rnform.setEnabled(false);
		for (InputData li : rndata) {
			li.setEditable(false);
		}
		rnform.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		rnAdressat = new Label(body, SWT.NONE);
		rnAdressat.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));

		IExpansionListener ecExpansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanging(final ExpansionEvent e) {
				ExpandableComposite src = (ExpandableComposite) e.getSource();
				saveExpandedState(KEY_RECHNUNGSBLATT + src.getText(), e.getState());
			}

		};

		ecAttachments = WidgetFactory.createExpandableComposite(tk, form, Messages.RechnungsBlatt_attachments);
		ecAttachments.addExpansionListener(ecExpansionListener);
		attachments = new IIdentifiableModifiableListComposite<IDocument>(ecAttachments, getStyle());
		GridData gd = SWTHelper.setGridDataHeight(attachments.getStructuredViewer().getControl(), 4, true);
		gd.widthHint = 300;
		attachments.getStructuredViewer().setContentProvider(new IStructuredContentProvider() {

			@Override
			public Object[] getElements(Object inputElement) {
				Rechnung actRn = (Rechnung) inputElement;
				if (actRn != null) {
					IInvoice invoice = actRn.toIInvoice();
					return invoice.getAttachments().toArray(new IDocument[]{});
				}
				return null;
			}
		});
		attachments.setAddElementHandler(() -> {
			if (actRn != null) {
				IPatient patient = actRn.getFall().getPatient().toIPatient();
				DocumentStore documentStore = OsgiServiceUtil.getService(DocumentStore.class).orElse(null);
				if (documentStore != null) {
					List<IDocument> documents = documentStore.getDocuments(patient.getId(), null, null, null);
					List<IDocument> pdfDocuments = documents.stream().filter(doc -> doc.getMimeType().endsWith("pdf"))
							.collect(Collectors.toList());

					GenericSelectionDialog gsd = new GenericSelectionDialog(getShell(), pdfDocuments,
							"Dokument w\u00E4hlen", "Nur PDF k\u00F6nnen angeh\u00E4ngt werden");
					int result = gsd.open();
					if (result == Dialog.OK) {
						IInvoice invoice = actRn.toIInvoice();
						IStructuredSelection selection = gsd.getSelection();
						if (!selection.isEmpty()) {
							selection.forEach(obj -> invoice.addAttachment((IDocument) obj));
							CoreModelServiceHolder.get().save(invoice);
						}
					}
				}
			}

		});
		attachments.setRemoveElementHandler((element) -> {
			if (actRn != null) {
				IInvoice invoice = actRn.toIInvoice();
				invoice.removeAttachment(element);
				CoreModelServiceHolder.get().save(invoice);
			}
		});
		tk.adapt(attachments, true, true);
		ecAttachments.setClient(attachments);
		attachments.getStructuredViewer().setInput(null);

		ecBuchungen = WidgetFactory.createExpandableComposite(tk, form, Messages.RechnungsBlatt_bookings); // $NON-NLS-1$
		ecBuchungen.addExpansionListener(ecExpansionListener);
		// tk.createLabel(body, "Buchungen");
		buchungen = new ListViewer(ecBuchungen, SWT.V_SCROLL | SWT.BORDER);
		// TableWrapData twd=new TableWrapData(TableWrapData.FILL_GRAB);
		SWTHelper.setGridDataHeight(buchungen.getControl(), 4, true);
		buchungen.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				Rechnung actRn = (Rechnung) inputElement;
				if (actRn == null) {
					return new String[]{Messages.RechnungsBlatt_noBillSelected};
				}
				List<Zahlung> lz = actRn.getZahlungen();
				return lz.toArray();
			}

		});
		tk.adapt(buchungen.getControl(), true, true);
		ecBuchungen.setClient(buchungen.getControl());
		buchungen.setLabelProvider(new DefaultLabelProvider() {
			@Override
			public String getColumnText(Object element, int columnIndex) {
				return getText(element);
			}

			@Override
			public String getText(Object element) {
				if (element instanceof Zahlung) {
					Zahlung zahlung = (Zahlung) element;

					StringBuilder sb = new StringBuilder();
					sb.append(zahlung.getLabel());
					String bemerkung = zahlung.getBemerkung();
					if (!StringTool.isNothing(bemerkung)) {
						sb.append(" ("); //$NON-NLS-1$
						sb.append(bemerkung);
						sb.append(")"); //$NON-NLS-1$
					}
					return sb.toString();
				} else {
					return element.toString();
				}
			}
		});
		buchungen.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof StructuredSelection && !((StructuredSelection) selection).isEmpty()) {
					Zahlung zahlung = (Zahlung) ((StructuredSelection) selection).getFirstElement();
					// get the command
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					ICommandService cmdService = window.getService(ICommandService.class);
					Command cmd = cmdService.getCommand("ch.elexis.ebanking_ch.command.openESRWithInvoiceId");
					if (cmd != null) {
						try {
							// create the parameter
							HashMap<String, Object> param = new HashMap<String, Object>();
							param.put("ch.elexis.ebanking_ch.command.openESR.InvoiceId", zahlung.getRechnung().getId());
							param.put("ch.elexis.ebanking_ch.command.openESR.PaymentDate",
									new TimeTool(zahlung.getDatum()).toString(TimeTool.DATE_COMPACT));
							// build the parameterized command
							ParameterizedCommand pc = ParameterizedCommand.generateCommand(cmd, param);
							// execute the command
							IHandlerService handlerService = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
									.getService(IHandlerService.class);
							handlerService.executeCommand(pc, null);
						} catch (ExecutionException | NotDefinedException | NotEnabledException
								| NotHandledException e) {
							LoggerFactory.getLogger(getClass()).error("Error executing open esr command", e);
						}
					} else {
						LoggerFactory.getLogger(getClass()).warn("No open esr command found, ebanking not installed");
					}
				}
			}
		});
		buchungen.setInput(null);
		// new Label(body,SWT.SEPARATOR|SWT.HORIZONTAL);

		ecBemerkungen = WidgetFactory.createExpandableComposite(tk, form, Messages.RechnungsBlatt_remarks); // $NON-NLS-1$
		ecBemerkungen.addExpansionListener(ecExpansionListener);
		tBemerkungen = SWTHelper.createText(tk, ecBemerkungen, 5, SWT.BORDER);
		tBemerkungen.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (actRn != null) {
					actRn.setBemerkung(tBemerkungen.getText());
				}
			}

		});
		ecBemerkungen.setClient(tBemerkungen);

		ecInternalRemarks = WidgetFactory.createExpandableComposite(tk, form, Messages.RechnungsBlatt_internalRemarks); // $NON-NLS-1$
		ecInternalRemarks.addExpansionListener(ecExpansionListener);
		tInternalRemarks = SWTHelper.createText(tk, ecInternalRemarks, 5, SWT.BORDER);
		tInternalRemarks.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (actRn != null) {
					actRn.setInternalRemarks(tInternalRemarks.getText());
				}
			}
		});
		ecInternalRemarks.setClient(tInternalRemarks);

		// tk.createLabel(body, "Statusänderungen");
		ecStatus = WidgetFactory.createExpandableComposite(tk, form, Messages.RechnungsBlatt_sateChangements); // $NON-NLS-1$
		ecStatus.addExpansionListener(ecExpansionListener);
		lbJournal = new org.eclipse.swt.widgets.List(ecStatus, SWT.V_SCROLL | SWT.BORDER);
		SWTHelper.setGridDataHeight(lbJournal, 4, true);
		tk.adapt(lbJournal, true, true);
		ecStatus.setClient(lbJournal);

		ecFehler = WidgetFactory.createExpandableComposite(tk, form, Messages.RechnungsBlatt_errorMessages); // $NON-NLS-1$
		ecFehler.addExpansionListener(ecExpansionListener);
		tRejects = SWTHelper.createText(tk, ecFehler, 4, SWT.READ_ONLY | SWT.V_SCROLL);
		ecFehler.setClient(tRejects);
		ecAusgaben = WidgetFactory.createExpandableComposite(tk, form, Messages.RechnungsBlatt_outputs); // $NON-NLS-1$
		ecAusgaben.addExpansionListener(ecExpansionListener);
		lbOutputs = new org.eclipse.swt.widgets.List(ecAusgaben, SWT.V_SCROLL | SWT.BORDER);
		ecAusgaben.setClient(lbOutputs);
		SWTHelper.setGridDataHeight(lbOutputs, 4, true);
		tk.adapt(lbOutputs, true, true);

		ecKons = WidgetFactory.createExpandableComposite(tk, form, Messages.RechnungsBlatt_consultations); // $NON-NLS-1$
		ecKons.addExpansionListener(ecExpansionListener);
		konsultationenViewer = new ListViewer(ecKons, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		ecKons.setClient(konsultationenViewer.getList());

		konsultationenViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				List<Object> elements = new ArrayList<Object>();
				if (actRn != null) {
					IInvoice invoice = NoPoUtil.loadAsIdentifiable(actRn, IInvoice.class).get();
					for (IEncounter encounter : invoice.getEncounters()) {
						elements.add(encounter);
						for (IDiagnosisReference diagnose : encounter.getDiagnoses()) {
							elements.add(diagnose);
						}
						for (IBilled verrechnet : encounter.getBilled()) {
							elements.add(verrechnet);
						}
					}
				}
				return elements.toArray();
			}

			@Override
			public void dispose() {
				// nothing to do
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// nothing to do
			}
		});
		konsultationenViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IEncounter) {

					Money sum = new Money(0);
					for (IBilled billed : ((IEncounter) element).getBilled()) {
						sum.addMoney(billed.getTotal());
					}
					return ((IEncounter) element).getLabel() + " (" + sum.toString() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				} else if (element instanceof IDiagnosisReference) {
					IDiagnosisReference diagnose = (IDiagnosisReference) element;
					return "  - " + diagnose.getLabel(); //$NON-NLS-1$
				} else if (element instanceof IBilled) {
					IBilled billed = (IBilled) element;
					return "  - " + billed.getAmount() + " " + billed.getLabel() + " (" //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
							+ billed.getTotal().toString() + ")"; //$NON-NLS-1$
				} else {
					return element.toString();
				}
			}
		});
		konsultationenViewer.setInput(this);
		// form.getToolBarManager().add()

		ecStorno = WidgetFactory.createExpandableComposite(tk, form, Messages.RechnungsBlatt_storno);
		ecStorno.addExpansionListener(ecExpansionListener);
		stornoViewer = new ListViewer(ecStorno, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		ecStorno.setClient(stornoViewer.getList());

		stornoViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// nothing to do
			}

			@Override
			public void dispose() {
				// nothing to do
			}

			@Override
			public Object[] getElements(Object inputElement) {
				List<Object> elements = new ArrayList<Object>();
				if (actRn != null) {
					List<Konsultation> konsultationen = actRn.getKonsultationen();
					if (konsultationen == null || konsultationen.isEmpty()) {
						HashMap<IEncounter, List<IInvoiceBilled>> elementsMap = new HashMap<IEncounter, List<IInvoiceBilled>>();
						// prepare heading label that will look like this dd.MM.yyyy (cancelled) -
						// amountOfMoney
						StringBuilder sbHeadingLabel = new StringBuilder();
						sbHeadingLabel.append(Messages.AccountView_bill + " " + actRn.getDatumRn()); //$NON-NLS-1$
						sbHeadingLabel.append(Messages.RechnungsBlatt_stornoLabel);

						// store all verrechnetCopies and add label with sum of all cancelled items
						IQuery<IInvoiceBilled> query = CoreModelServiceHolder.get().getQuery(IInvoiceBilled.class);
						query.and(ModelPackage.Literals.IINVOICE_BILLED__INVOICE, COMPARATOR.EQUALS,
								NoPoUtil.loadAsIdentifiable(actRn, IInvoice.class).get());
						List<IInvoiceBilled> vcList = query.execute();
						Money sum = new Money(0);
						for (IInvoiceBilled vc : vcList) {
							// add amount of money this item/s cost
							sum.addMoney(vc.getTotal());
							// add verrechnet to map
							addToMap(vc, elementsMap);
						}
						// add the map to the elements
						Set<IEncounter> keys = elementsMap.keySet();
						for (IEncounter konsultation : keys) {
							if (konsultation != null) {
								elements.add(konsultation);
							} else {
								elements.add("?"); //$NON-NLS-1$
							}
							elements.addAll(elementsMap.get(konsultation));
						}

						// finalize heading label by adding sum of money of all cancellations
						sbHeadingLabel.append(sum.toString());
						elements.add(0, sbHeadingLabel.toString());
					}
				}
				return elements.toArray();
			}

			private void addToMap(IInvoiceBilled copy, HashMap<IEncounter, List<IInvoiceBilled>> elementsMap) {
				IEncounter encounter = copy.getEncounter();
				if (encounter != null) {
					List<IInvoiceBilled> list = elementsMap.get(encounter);
					if (list == null) {
						list = new ArrayList<IInvoiceBilled>();
					}
					list.add(copy);
					elementsMap.put(encounter, list);
				} else {
					List<IInvoiceBilled> list = elementsMap.get(null);
					if (list == null) {
						list = new ArrayList<IInvoiceBilled>();
					}
					list.add(copy);
					elementsMap.put(null, list);
				}
			}
		});
		stornoViewer.setLabelProvider(new LabelProvider() {

			private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

			@Override
			public String getText(Object element) {
				if (element instanceof IInvoiceBilled) {
					IInvoiceBilled vc = (IInvoiceBilled) element;
					return "  - " + vc.getAmount() + " " + vc.getLabel() + " (" //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
							+ vc.getTotal().toString() + ")"; //$NON-NLS-1$
				} else if (element instanceof Konsultation) {
					return "Konsultation " + ((Konsultation) element).getDatum();
				} else if (element instanceof IEncounter) {
					return "Konsultation " + dateFormatter.format(((IEncounter) element).getDate());
				} else {
					return element.toString();
				}
			}
		});
		stornoViewer.setInput(this);

		List<IViewContribution> filtered = ViewContributionHelper
				.getFilteredAndPositionSortedContributions(detailComposites, 0);
		for (IViewContribution ivc : filtered) {
			ExpandableComposite ec = WidgetFactory.createExpandableComposite(tk, form, ivc.getLocalizedTitle());
			ec.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			ec.addExpansionListener(ecExpansionListener);
			Composite mainComposite = new Composite(ec, SWT.None);
			mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			mainComposite.setLayout(new GridLayout(1, false));
			Composite ret = ivc.initComposite(mainComposite);
			tk.adapt(ret);
			ec.setClient(ret);
		}

		GlobalEventDispatcher.addActivationListener(this, site.getPart());
	}

	private void saveExpandedState(String field, boolean state) {
		if (state) {
			ConfigServiceHolder.setUser(USERSETTINGS2_EXPANDABLE_COMPOSITES_STATES + field,
					USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_OPEN);
		} else {
			ConfigServiceHolder.setUser(USERSETTINGS2_EXPANDABLE_COMPOSITES_STATES + field,
					USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_CLOSED);
		}
	}

	private void setExpandedState(ExpandableComposite ec, String field) {
		String mode = ConfigServiceHolder.getUser(USERSETTINGS2_EXPANDABLE_COMPOSITES,
				USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_REMEMBER_STATE);
		if (mode.equals(USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_OPEN)) {
			ec.setExpanded(true);
		} else if (mode.equals(USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_CLOSED)) {
			ec.setExpanded(false);
		} else {
			String state = ConfigServiceHolder.getUser(USERSETTINGS2_EXPANDABLE_COMPOSITES_STATES + field,
					USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_CLOSED);
			if (state.equals(USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_CLOSED)) {
				ec.setExpanded(false);
			} else {
				ec.setExpanded(true);
			}
		}
	}

	@Override
	public void dispose() {
		GlobalEventDispatcher.removeActivationListener(this, site.getPart());
		super.dispose();
	}

	@Override
	public void activation(boolean mode) {
		/* egal */
	}

	@Override
	public void visible(boolean mode) {
		if (mode) {
			ElexisEventDispatcher.getInstance().addListeners(eeli_rn, eeli_user, eeli_patient);
			Rechnung selected = (Rechnung) ElexisEventDispatcher.getSelected(Rechnung.class);
			if (selected != null) {
				doSelect(selected);
			}
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_rn, eeli_user, eeli_patient);
		}
	}

	private void doSelect(Rechnung rn) {
		actRn = rn;
		UiDesk.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				display();
			}
		});

	}

	public void display() {
		rnform.reload(actRn);

		lbJournal.removeAll();
		lbOutputs.removeAll();

		if (actRn != null) {
			Kontakt adressat = actRn.getFall().getInvoiceRecipient();
			rnAdressat.setText(Messages.RechnungsBlatt_adressee + ((adressat != null) ? adressat.getLabel() : ""));
			form.setText(actRn.getLabel());
			List<String> trace = actRn.getTrace(Rechnung.STATUS_CHANGED);
			for (String s : trace) {
				String[] stm = s.split("\\s*:\\s"); //$NON-NLS-1$
				StringBuilder sb = new StringBuilder();
				sb.append(stm[0]).append(" : ").append( //$NON-NLS-1$
						RnStatus.getStatusText(Integer.parseInt(stm[1])));
				lbJournal.add(sb.toString());
			}
			if (actRn.getStatus() == InvoiceState.DEFECTIVE.numericValue()) {
				List<String> rejects = actRn.getTrace(Rechnung.REJECTED);
				StringBuilder rjj = new StringBuilder();
				for (String r : rejects) {
					rjj.append(r).append("\n------\n"); //$NON-NLS-1$
				}
				tRejects.setText(rjj.toString());
			} else {
				tRejects.setText("");
			}
			List<String> outputs = actRn.getTrace(Rechnung.OUTPUT);
			for (String o : outputs) {
				lbOutputs.add(o);
			}
			tBemerkungen.setText(actRn.getBemerkung());
			tInternalRemarks.setText(actRn.getInternalRemarks());
		} else {
			rnAdressat.setText(StringConstants.EMPTY);
			tRejects.setText(StringConstants.EMPTY);
			form.setText(null);
		}

		attachments.getStructuredViewer().setInput(actRn);
		buchungen.setInput(actRn);
		konsultationenViewer.refresh();
		stornoViewer.refresh();

		detailComposites.forEach(dc -> dc.setDetailObject(actRn, null));

		setExpandedState(ecBuchungen, KEY_RECHNUNGSBLATT + ecBuchungen.getText());
		setExpandedState(ecBemerkungen, KEY_RECHNUNGSBLATT + ecBemerkungen.getText());
		setExpandedState(ecStatus, KEY_RECHNUNGSBLATT + ecStatus.getText());
		setExpandedState(ecFehler, KEY_RECHNUNGSBLATT + ecFehler.getText());
		setExpandedState(ecAusgaben, KEY_RECHNUNGSBLATT + ecAusgaben.getText());
		setExpandedState(ecKons, KEY_RECHNUNGSBLATT + ecKons.getText());
		setExpandedState(ecStorno, KEY_RECHNUNGSBLATT + ecStorno.getText());

		form.reflow(true);
	}

}
