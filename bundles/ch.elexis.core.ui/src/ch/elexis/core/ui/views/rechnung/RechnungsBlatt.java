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

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.constants.ExtensionPointConstantsData;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IRnOutputter;
import ch.elexis.core.data.service.StoreToStringServiceHolder;
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
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.AccessControlServiceHolder;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.e4.controls.IIdentifiableModifiableListComposite;
import ch.elexis.core.ui.e4.dialog.GenericSelectionDialog;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.util.LabeledInputField;
import ch.elexis.core.ui.util.LabeledInputField.InputData;
import ch.elexis.core.ui.util.LabeledInputField.InputData.Typ;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.WidgetFactory;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.core.ui.views.contribution.IViewContribution;
import ch.elexis.core.ui.views.contribution.ViewContributionHelper;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Rechnung;
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
			if (InvoiceState.CANCELLED == invoice.getInvoiceState()) {
				ltf.setLabel(Messages.RechnungsBlatt_compensateAmount);
			} else {
				ltf.setLabel(Messages.Invoice_Amount_Unpaid);
			}
		}

		@Override
		public void reloadContent(Object po, InputData ltf) {
			Rechnung invoice = (Rechnung) po;
			if (InvoiceState.CANCELLED == invoice.getInvoiceState()) {
				Money openAmount = invoice.getOffenerBetrag();
				if (openAmount.isZero()) {
					return;
				}
				if (!AccessControlServiceHolder.get().evaluate(EvACE.of(IInvoice.class, Right.UPDATE))) {
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
			new InputData(Messages.RechnungsBlatt_billDate, Rechnung.BILL_DATE, Typ.STRING, null),
			new InputData(Messages.Core_Invoicestate, Rechnung.BILL_STATE, new LabeledInputField.IContentProvider() {

				@Override
				public void displayContent(Object po, InputData ltf) {
					Rechnung r = (Rechnung) po;
					ltf.setText(r.getInvoiceState().getLocaleText());

				}

				@Override
				public void reloadContent(Object po, InputData ltf) {
					if (new RnDialogs.StatusAendernDialog(
							Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getShell(), (Rechnung) po)
							.open() == Dialog.OK) {
						ElexisEventDispatcher.update((PersistentObject) po);
					}
				}

			}), new InputData(Messages.RechnungsBlatt_treatmentsFrom, Rechnung.BILL_DATE_FROM, Typ.STRING, null),
			new InputData(Messages.RechnungsBlatt_treatmentsUntil, Rechnung.BILL_DATE_UNTIL, Typ.STRING, null),
			new InputData(Messages.RechnungsBlatt_amountTotal, Rechnung.BILL_AMOUNT_CENTS, Typ.CURRENCY, null),
			new InputData(Messages.Invoice_Amount_Unpaid, Rechnung.BILL_AMOUNT_CENTS, openAmountContentProvider) };
	private LabeledInputField.AutoForm rnform;

	@Optional
	@Inject
	void deletedInvoice(@UIEventTopic(ElexisEventTopics.EVENT_DELETE) IInvoice invoice) {
		if (actRn != null && actRn.getId().equals(invoice.getId())) {
			doSelect(null);
		}
	}

	@Optional
	@Inject
	void updateInvoice(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IInvoice invoice) {
		doSelect(invoice);
	}

	@Inject
	void activeInvoice(@Optional IInvoice invoice) {
		Display.getDefault().asyncExec(() -> {
			if (invoice != null) {
				doSelect(invoice);
			} else {
				doSelect(null);
			}
		});
	}

	@Inject
	void activeUser(@Optional IUser user) {
		Display.getDefault().asyncExec(() -> {
			if (rnform != null && !rnform.isDisposed()) {
				adaptForUser(user);
			}
		});
	}

	private void adaptForUser(IUser user) {
		display();
	}

	@Inject
	void activePatient(@Optional IPatient patient) {
		Display.getDefault().asyncExec(() -> {
			if (patient != null) {
				if (actRn != null) {
					Fall fall = actRn.getFall();
					if (fall.exists()) {
						Patient actPatient = fall.getPatient();
						if (!Objects.equals(patient.getId(), actPatient.getId())) {
							doSelect(null);
						}
					}
				}
			} else {
				doSelect(null);
			}
		});
	}

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
		attachments = new IIdentifiableModifiableListComposite<>(ecAttachments, getStyle());
		GridData gd = SWTHelper.setGridDataHeight(attachments.getStructuredViewer().getControl(), 4, true);
		gd.widthHint = 300;
		attachments.getStructuredViewer().setContentProvider(new IStructuredContentProvider() {

			@Override
			public Object[] getElements(Object inputElement) {
				Rechnung actRn = (Rechnung) inputElement;
				if (actRn != null) {
					IInvoice invoice = actRn.toIInvoice();
					return invoice.getAttachments().toArray(new IDocument[] {});
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
					List<IDocument> pdfDocuments = documents.stream().filter(doc -> doc.getMimeType().endsWith("pdf")) //$NON-NLS-1$
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
					return new String[] { Messages.RechnungsBlatt_noBillSelected };
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
					Command cmd = cmdService.getCommand("ch.elexis.ebanking_ch.command.openESRWithInvoiceId"); //$NON-NLS-1$
					if (cmd != null) {
						try {
							// create the parameter
							HashMap<String, Object> param = new HashMap<>();
							param.put("ch.elexis.ebanking_ch.command.openESR.InvoiceId", zahlung.getRechnung().getId()); //$NON-NLS-1$
							param.put("ch.elexis.ebanking_ch.command.openESR.PaymentDate", //$NON-NLS-1$
									new TimeTool(zahlung.getDatum()).toString(TimeTool.DATE_COMPACT));
							// build the parameterized command
							ParameterizedCommand pc = ParameterizedCommand.generateCommand(cmd, param);
							// execute the command
							IHandlerService handlerService = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
									.getService(IHandlerService.class);
							handlerService.executeCommand(pc, null);
						} catch (ExecutionException | NotDefinedException | NotEnabledException
								| NotHandledException e) {
							LoggerFactory.getLogger(getClass()).error("Error executing open esr command", e); //$NON-NLS-1$
						}
					} else {
						LoggerFactory.getLogger(getClass()).warn("No open esr command found, ebanking not installed"); //$NON-NLS-1$
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
		lbOutputs.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				String[] selectedOutputTraces = lbOutputs.getSelection();
				if (selectedOutputTraces != null) {
					for (String trace : selectedOutputTraces) {
						getOutputterForTrace(trace).ifPresent(o -> {
							getOutputDateTime(trace).ifPresent(ot -> {
								o.openOutput(actRn.toIInvoice(), ot.toLocalDateTime(), getOutputInvoiceState(trace));
							});
						});
					}
				}
			}

			private java.util.Optional<IRnOutputter> getOutputterForTrace(String trace) {
				List<IRnOutputter> outputters = Extensions.getClasses(ExtensionPointConstantsData.RECHNUNGS_MANAGER,
						"outputter"); //$NON-NLS-1$
				String description = getOutputterDescription(trace);
				if (StringUtils.isNotBlank(description)) {
					for (IRnOutputter iRnOutputter : outputters) {
						if (iRnOutputter.getDescription().equalsIgnoreCase(description)) {
							return java.util.Optional.of(iRnOutputter);
						}
					}
				}
				return java.util.Optional.empty();
			}

			private String getOutputterDescription(String trace) {
				if (trace != null) {
					String[] parts = trace.split(": ");
					if (parts != null && parts.length >= 2) {
						return parts[1].trim();
					}
				}
				return null;
			}

			private InvoiceState getOutputInvoiceState(String trace) {
				if (trace != null) {
					String[] parts = trace.split(": ");
					if (parts != null && parts.length >= 3) {
						for (InvoiceState state : InvoiceState.values()) {
							if (state.getLocaleText().equals(parts[2])) {
								return state;
							}
						}
					}
				}
				return null;
			}

			private java.util.Optional<TimeTool> getOutputDateTime(String trace) {
				if (trace != null) {
					String[] parts = trace.split(": ");
					if (parts != null && parts.length >= 1) {
						TimeTool ret = new TimeTool();
						if (ret.set(parts[0].trim())) {
							return java.util.Optional.of(ret);
						}
					}
				}
				return java.util.Optional.empty();
			}
		});

		ecKons = WidgetFactory.createExpandableComposite(tk, form, Messages.Core_Consultations); // $NON-NLS-1$
		ecKons.addExpansionListener(ecExpansionListener);
		konsultationenViewer = new ListViewer(ecKons, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		ecKons.setClient(konsultationenViewer.getList());

		konsultationenViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				List<Object> elements = new ArrayList<>();
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
					return "  - " + billed.getAmount() + StringUtils.SPACE + billed.getLabel() + " (" //$NON-NLS-1$ //$NON-NLS-2$
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
				List<Object> elements = new ArrayList<>();
				if (actRn != null) {
					List<Konsultation> konsultationen = actRn.getKonsultationen();
					if (konsultationen == null || konsultationen.isEmpty()) {
						HashMap<IEncounter, List<IInvoiceBilled>> elementsMap = new HashMap<>();
						// prepare heading label that will look like this dd.MM.yyyy (cancelled) -
						// amountOfMoney
						StringBuilder sbHeadingLabel = new StringBuilder();
						sbHeadingLabel.append(Messages.Core_Bill + StringUtils.SPACE + actRn.getDatumRn());
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
						list = new ArrayList<>();
					}
					list.add(copy);
					elementsMap.put(encounter, list);
				} else {
					List<IInvoiceBilled> list = elementsMap.get(null);
					if (list == null) {
						list = new ArrayList<>();
					}
					list.add(copy);
					elementsMap.put(null, list);
				}
			}
		});
		stornoViewer.setLabelProvider(new LabelProvider() {

			private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy"); //$NON-NLS-1$

			@Override
			public String getText(Object element) {
				if (element instanceof IInvoiceBilled) {
					IInvoiceBilled vc = (IInvoiceBilled) element;
					return "  - " + vc.getAmount() + StringUtils.SPACE + vc.getLabel() //$NON-NLS-1$
							+ " (" // $NON-NLS-1 //$NON-NLS-1$
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
		CoreUiUtil.injectServicesWithContext(this);
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
			IInvoice selected = ContextServiceHolder.get().getTyped(IInvoice.class).orElse(null);
			if (selected != null) {
				doSelect(selected);
			}
		}
	}

	private void doSelect(IInvoice invoice) {
		if (invoice == null || AccessControlServiceHolder.get()
				.evaluate(EvACE.of(IInvoice.class, Right.VIEW, StoreToStringServiceHolder.getStoreToString(invoice)))) {

			actRn = (Rechnung) NoPoUtil.loadAsPersistentObject(invoice);
			UiDesk.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					display();
				}
			});
		}
	}

	public void display() {
		if (rnform != null && !rnform.isDisposed()) {
			rnform.reload(actRn);

			String[] outputsSelection = lbOutputs.getSelection();
			String[] journalSelection = lbJournal.getSelection();

			lbJournal.removeAll();
			lbOutputs.removeAll();

			if (actRn != null) {
				Kontakt adressat = actRn.getFall().getInvoiceRecipient();
				rnAdressat.setText(Messages.RechnungsBlatt_adressee
						+ ((adressat != null) ? adressat.getLabel() : StringUtils.EMPTY));
				form.setText(actRn.getLabel());
				List<String> trace = actRn.getTrace(Rechnung.STATUS_CHANGED);
				List<String> mandatorTrace = actRn.getTrace(Rechnung.MANDATOR);
				List<String> combinedTrace = combineAndSortTrace(trace, mandatorTrace);
				combinedTrace.forEach(lbJournal::add);

				if (journalSelection != null && journalSelection.length > 0) {
					for (int i = 0; i < lbJournal.getItemCount(); i++) {
						for (String selection : journalSelection) {
							if (lbJournal.getItem(i).equals(selection)) {
								lbJournal.select(i);
							}
						}
					}
				}
				if (actRn.getInvoiceState() == InvoiceState.DEFECTIVE) {
					List<String> rejects = actRn.getTrace(Rechnung.REJECTED);
					StringBuilder rjj = new StringBuilder();
					for (String r : rejects) {
						rjj.append(r).append("\n------\n"); //$NON-NLS-1$
					}
					tRejects.setText(rjj.toString());
				} else {
					tRejects.setText(StringUtils.EMPTY);
				}
				List<String> outputs = actRn.getTrace(Rechnung.OUTPUT);
				for (String o : outputs) {
					lbOutputs.add(o);
				}
				if (outputsSelection != null && outputsSelection.length > 0) {
					for (int i = 0; i < lbOutputs.getItemCount(); i++) {
						for (String selection : outputsSelection) {
							if (lbOutputs.getItem(i).equals(selection)) {
								lbOutputs.select(i);
							}
						}
					}
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

	private List<String> combineAndSortTrace(List<String> trace, List<String> mandatorTrace) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss"); //$NON-NLS-1$
		return trace.stream().map(statusEntry -> {
			// Split the status entry into a timestamp and a status code.
			String[] stm = statusEntry.split("\\s*:\\s", 2); //$NON-NLS-1$
			String timestampString = stm[0]; // Extract the timestamp part.
			String statusText = timestampString + " : " //$NON-NLS-1$
					+ InvoiceState.fromState(Integer.parseInt(stm[1])).getLocaleText();
			// Find the corresponding mandator entry that matches the timestamp.
			String mandatorLabel = mandatorTrace.stream().filter(m -> m.contains(timestampString)).findFirst()
					.map(m -> {
						// Remove the matched mandator entry from the list to avoid reuse.
						mandatorTrace.remove(m);
						// Extract the label text from the mandator entry if it contains a colon.
						if (m.contains(":")) { //$NON-NLS-1$
							String[] parts = m.split(":"); //$NON-NLS-1$
							return parts.length > 1 ? parts[parts.length - 1].trim() : ""; //$NON-NLS-1$
						}
						return ""; //$NON-NLS-1$
					}).orElse(""); //$NON-NLS-1$
			// Return the status text with or without the mandator label.
			return mandatorLabel.isEmpty() ? statusText : statusText + " / " + mandatorLabel; //$NON-NLS-1$
		}).sorted((entry1, entry2) -> {
			try {
				// Parse the dates from the entries for sorting.
				Date date1 = dateFormat.parse(entry1.split(" : ")[0]); //$NON-NLS-1$
				Date date2 = dateFormat.parse(entry2.split(" : ")[0]); //$NON-NLS-1$
				return date1.compareTo(date2);
			} catch (Exception e) {
				LoggerFactory.getLogger(getClass()).error("Error parsing dates for sorting: {}", e.getMessage(), e); //$NON-NLS-1$
				return 0;
			}
		}).collect(Collectors.toList());
	}

}
