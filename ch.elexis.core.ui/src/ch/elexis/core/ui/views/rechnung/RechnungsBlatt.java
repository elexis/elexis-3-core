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

import static ch.elexis.core.ui.constants.UiPreferenceConstants.USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_CLOSED;
import static ch.elexis.core.ui.constants.UiPreferenceConstants.USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_OPEN;
import static ch.elexis.core.ui.constants.UiPreferenceConstants.USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_REMEMBER_STATE;
import static ch.elexis.core.ui.constants.UiPreferenceConstants.USERSETTINGS2_EXPANDABLE_COMPOSITES;
import static ch.elexis.core.ui.constants.UiPreferenceConstants.USERSETTINGS2_EXPANDABLE_COMPOSITES_STATES;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListenerImpl;
import ch.elexis.core.data.interfaces.IDiagnose;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.util.LabeledInputField;
import ch.elexis.core.ui.util.LabeledInputField.InputData;
import ch.elexis.core.ui.util.LabeledInputField.InputData.Typ;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.WidgetFactory;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.data.Anwender;
import ch.elexis.data.Konsultation;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.data.Verrechnet;
import ch.elexis.data.VerrechnetCopy;
import ch.elexis.data.Zahlung;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;

public class RechnungsBlatt extends Composite implements IActivationListener {
	
	private static final String KEY_RECHNUNGSBLATT = "RechnungsBlatt/"; //$NON-NLS-1$
	IViewSite site;
	ListViewer buchungen;
	org.eclipse.swt.widgets.List lbJournal;
	org.eclipse.swt.widgets.List lbOutputs;
	Rechnung actRn;
	ScrolledForm form;
	FormToolkit tk = UiDesk.getToolkit();
	// Button bBuchung,bPrint,bStorno,bGebuehr,bGutschrift;
	Text tRejects, tBemerkungen;
	Label rnAdressat;
	ListViewer konsultationenViewer;
	ListViewer stornoViewer;
	
	private final ExpandableComposite ecBuchungen;
	private final ExpandableComposite ecBemerkungen;
	private final ExpandableComposite ecStatus;
	private final ExpandableComposite ecFehler;
	private final ExpandableComposite ecAusgaben;
	private final ExpandableComposite ecKons;
	private final ExpandableComposite ecStorno;
	
	static final InputData[] rndata = {
		new InputData(Messages.RechnungsBlatt_billNumber, Rechnung.BILL_NUMBER, Typ.STRING, null), //$NON-NLS-1$
		new InputData(Messages.RechnungsBlatt_billDate, Rechnung.BILL_DATE, Typ.STRING, null), //$NON-NLS-1$
		new InputData(Messages.RechnungsBlatt_billState, Rechnung.BILL_STATE, //$NON-NLS-1$
			new LabeledInputField.IContentProvider() {
				
				public void displayContent(PersistentObject po, InputData ltf){
					Rechnung r = (Rechnung) po;
					ltf.setText(RnStatus.getStatusText(r.getStatus()));
					
				}
				
				public void reloadContent(PersistentObject po, InputData ltf){
					if (new RnDialogs.StatusAendernDialog(Hub.plugin.getWorkbench()
						.getActiveWorkbenchWindow().getShell(), (Rechnung) po).open() == Dialog.OK) {
						ElexisEventDispatcher.update(po);
					}
				}
				
			}),
		new InputData(Messages.RechnungsBlatt_treatmentsFrom, Rechnung.BILL_DATE_FROM, Typ.STRING,
			null), //$NON-NLS-1$
		new InputData(Messages.RechnungsBlatt_treatmentsUntil, Rechnung.BILL_DATE_UNTIL,
			Typ.STRING, null), //$NON-NLS-1$
		new InputData(Messages.RechnungsBlatt_amountTotal, Rechnung.BILL_AMOUNT_CENTS,
			Typ.CURRENCY, null), //$NON-NLS-1$
		new InputData(Messages.RechnungsBlatt_amountOpen, Rechnung.BILL_AMOUNT_CENTS, //$NON-NLS-1$
			new LabeledInputField.IContentProvider() {
				
				public void displayContent(PersistentObject po, InputData ltf){
					Rechnung rn = (Rechnung) po;
					Money offen = rn.getOffenerBetrag();
					ltf.setText(offen.getAmountAsString());
				}
				
				public void reloadContent(PersistentObject po, InputData ltf){
					try {
						if (new RnDialogs.BuchungHinzuDialog(Hub.plugin.getWorkbench()
							.getActiveWorkbenchWindow().getShell(), (Rechnung) po).open() == Dialog.OK) {
							ElexisEventDispatcher.update(po);
						}
					} catch (ElexisException e) {
						SWTHelper.showError("Buchung kann nicht hinzugefügt werden",
							e.getLocalizedMessage());
					}
				}
				
			})
	};
	LabeledInputField.AutoForm rnform;
	
	private final ElexisEventListenerImpl eeli_rn = new ElexisUiEventListenerImpl(Rechnung.class,
		ElexisEvent.EVENT_CREATE | ElexisEvent.EVENT_DELETE | ElexisEvent.EVENT_UPDATE
			| ElexisEvent.EVENT_SELECTED) {
		
		public void runInUi(ElexisEvent ev){
			switch (ev.getType()) {
			case ElexisEvent.EVENT_UPDATE:
				doSelect((Rechnung) ev.getObject());
				break;
			case ElexisEvent.EVENT_DESELECTED: // fall thru
			case ElexisEvent.EVENT_DELETE:
				if (actRn != null && actRn.getId().equals(ev.getObject().getId())) {
					actRn = null;
					display();
				}
				break;
			case ElexisEvent.EVENT_SELECTED:
				doSelect((Rechnung) ev.getObject());
				break;
			}
			
		}
	};
	
	private final ElexisEventListenerImpl eeli_user = new ElexisUiEventListenerImpl(Anwender.class,
		ElexisEvent.EVENT_USER_CHANGED) {
		
		public void runInUi(ElexisEvent ev){
			display();
		}
	};
	
	public RechnungsBlatt(Composite parent, IViewSite site){
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
			public void expansionStateChanging(final ExpansionEvent e){
				ExpandableComposite src = (ExpandableComposite) e.getSource();
				saveExpandedState(KEY_RECHNUNGSBLATT + src.getText(), e.getState());
			}
			
		};
		
		ecBuchungen =
			WidgetFactory.createExpandableComposite(tk, form, Messages.RechnungsBlatt_bookings); //$NON-NLS-1$
		ecBuchungen.addExpansionListener(ecExpansionListener);
		// tk.createLabel(body, "Buchungen");
		buchungen = new ListViewer(ecBuchungen, SWT.V_SCROLL | SWT.BORDER);
		// TableWrapData twd=new TableWrapData(TableWrapData.FILL_GRAB);
		SWTHelper.setGridDataHeight(buchungen.getControl(), 4, true);
		buchungen.setContentProvider(new IStructuredContentProvider() {
			public void dispose(){}
			
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput){}
			
			public Object[] getElements(Object inputElement){
				Rechnung actRn =
					(Rechnung) ElexisEventDispatcher.getInstance().getSelected(Rechnung.class);
				if (actRn == null) {
					return new String[] {
						Messages.RechnungsBlatt_noBillSelected
					};
				}
				List<Zahlung> lz = actRn.getZahlungen();
				return lz.toArray();
			}
			
		});
		tk.adapt(buchungen.getControl(), true, true);
		ecBuchungen.setClient(buchungen.getControl());
		buchungen.setLabelProvider(new DefaultLabelProvider() {
			public String getColumnText(Object element, int columnIndex){
				return getText(element);
			}
			
			@Override
			public String getText(Object element){
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
		// new Label(body,SWT.SEPARATOR|SWT.HORIZONTAL);
		ecBemerkungen =
			WidgetFactory.createExpandableComposite(tk, form, Messages.RechnungsBlatt_remarks); //$NON-NLS-1$
		ecBemerkungen.addExpansionListener(ecExpansionListener);
		tBemerkungen = SWTHelper.createText(tk, ecBemerkungen, 5, SWT.BORDER);
		tBemerkungen.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e){
				actRn.setBemerkung(tBemerkungen.getText());
			}
			
		});
		ecBemerkungen.setClient(tBemerkungen);
		// tk.createLabel(body, "Statusänderungen");
		ecStatus =
			WidgetFactory.createExpandableComposite(tk, form,
				Messages.RechnungsBlatt_sateChangements); //$NON-NLS-1$
		ecStatus.addExpansionListener(ecExpansionListener);
		lbJournal = new org.eclipse.swt.widgets.List(ecStatus, SWT.V_SCROLL | SWT.BORDER);
		SWTHelper.setGridDataHeight(lbJournal, 4, true);
		tk.adapt(lbJournal, true, true);
		ecStatus.setClient(lbJournal);
		
		ecFehler =
			WidgetFactory
				.createExpandableComposite(tk, form, Messages.RechnungsBlatt_errorMessages); //$NON-NLS-1$
		ecFehler.addExpansionListener(ecExpansionListener);
		tRejects = SWTHelper.createText(tk, ecFehler, 4, SWT.READ_ONLY | SWT.V_SCROLL);
		ecFehler.setClient(tRejects);
		ecAusgaben =
			WidgetFactory.createExpandableComposite(tk, form, Messages.RechnungsBlatt_outputs); //$NON-NLS-1$
		ecAusgaben.addExpansionListener(ecExpansionListener);
		lbOutputs = new org.eclipse.swt.widgets.List(ecAusgaben, SWT.V_SCROLL | SWT.BORDER);
		ecAusgaben.setClient(lbOutputs);
		SWTHelper.setGridDataHeight(lbOutputs, 4, true);
		tk.adapt(lbOutputs, true, true);
		
		ecKons =
			WidgetFactory
				.createExpandableComposite(tk, form, Messages.RechnungsBlatt_consultations); //$NON-NLS-1$
		ecKons.addExpansionListener(ecExpansionListener);
		konsultationenViewer = new ListViewer(ecKons, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		ecKons.setClient(konsultationenViewer.getList());
		
		konsultationenViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement){
				List<Object> elements = new ArrayList<Object>();
				if (actRn != null) {
					List<Konsultation> konsultationen = actRn.getKonsultationen();
					if (konsultationen != null) {
						for (Konsultation konsultation : konsultationen) {
							elements.add(konsultation);
							
							List<IDiagnose> diagnosen = konsultation.getDiagnosen();
							if (diagnosen != null) {
								for (IDiagnose diagnose : diagnosen) {
									elements.add(diagnose);
								}
							}
							
							List<Verrechnet> leistungen = konsultation.getLeistungen();
							if (leistungen != null) {
								for (Verrechnet verrechnet : leistungen) {
									elements.add(verrechnet);
								}
							}
						}
					}
				}
				
				return elements.toArray();
			}
			
			public void dispose(){
				// nothing to do
			}
			
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
				// nothing to do
			}
		});
		konsultationenViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element){
				if (element instanceof Konsultation) {
					Konsultation konsultation = (Konsultation) element;
					
					Money sum = new Money(0);
					List<Verrechnet> leistungen = konsultation.getLeistungen();
					if (leistungen != null) {
						for (Verrechnet verrechnet : leistungen) {
							int zahl = verrechnet.getZahl();
							Money preis = verrechnet.getNettoPreis();
							preis.multiply(zahl);
							sum.addMoney(preis);
						}
					}
					return konsultation.getLabel() + " (" + sum.toString() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				} else if (element instanceof IDiagnose) {
					IDiagnose diagnose = (IDiagnose) element;
					return "  - " + diagnose.getLabel(); //$NON-NLS-1$
				} else if (element instanceof Verrechnet) {
					Verrechnet verrechnet = (Verrechnet) element;
					int zahl = verrechnet.getZahl();
					Money preis = verrechnet.getNettoPreis();
					preis.multiply(zahl);
					return "  - " + zahl + " " + verrechnet.getLabel() + " (" + preis.toString() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ ")"; //$NON-NLS-1$
				} else {
					return element.toString();
				}
			}
		});
		konsultationenViewer.setInput(this);
		// form.getToolBarManager().add()
		
		ecStorno =
			WidgetFactory.createExpandableComposite(tk, form, Messages.RechnungsBlatt_storno);
		ecStorno.addExpansionListener(ecExpansionListener);
		stornoViewer = new ListViewer(ecStorno, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		ecStorno.setClient(stornoViewer.getList());
		
		stornoViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
				//nothing to do
			}
			
			@Override
			public void dispose(){
				//nothing to do
			}
			
			@Override
			public Object[] getElements(Object inputElement){
				List<Object> elements = new ArrayList<Object>();
				if (actRn != null) {
					List<Konsultation> konsultationen = actRn.getKonsultationen();
					if (konsultationen == null || konsultationen.isEmpty()) {
						// prepare heading label that will look like this dd.MM.yyyy (cancelled) - amountOfMoney
						StringBuilder sbHeadingLabel = new StringBuilder();
						sbHeadingLabel.append(actRn.getDatumRn());
						sbHeadingLabel.append(Messages.RechnungsBlatt_stornoLabel);
						
						// store all verrechnetCopies and add label with sum of all cancelled items
						Query<VerrechnetCopy> vcQuery =
							new Query<VerrechnetCopy>(VerrechnetCopy.class);
						vcQuery.add(VerrechnetCopy.RECHNUNGID, Query.EQUALS, actRn.getId());
						List<VerrechnetCopy> vcList = vcQuery.execute();
						Money sum = new Money(0);
						for (VerrechnetCopy vc : vcList) {
							// add verrechnet to list
							elements.add(vc);
							// add amount of money this item/s cost
							Money price = vc.getNettoPreis();
							price.multiply(vc.getZahl());
							sum.addMoney(price);
						}
						
						// finalize heading label by adding sum of money of all cancellations
						sbHeadingLabel.append(sum.toString());
						elements.add(0, sbHeadingLabel.toString());
					}
				}
				return elements.toArray();
			}
		});
		stornoViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof VerrechnetCopy) {
					VerrechnetCopy vc = (VerrechnetCopy) element;
					int amount = vc.getZahl();
					Money price = vc.getNettoPreis();
					price.multiply(amount);
					return "  - " + amount + " " + vc.getLabel() + " (" + price.toString() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ ")"; //$NON-NLS-1$
				} else {
					return element.toString();
				}
			}
		});
		stornoViewer.setInput(this);
		
		buchungen.setInput(site);
		
		// add extension points
		@SuppressWarnings("unchecked")
		List<IRechnungsdetail> invDetails =
			Extensions.getClasses(ExtensionPointConstantsUi.RECHNUNGSDETAIL, "Class");
		for (IRechnungsdetail invDetail : invDetails) {
			invDetail.getExpandableComposite(tk, form);
		}
		
		GlobalEventDispatcher.addActivationListener(this, site.getPart());
	}
	
	private void saveExpandedState(String field, boolean state){
		if (state) {
			CoreHub.userCfg.set(USERSETTINGS2_EXPANDABLE_COMPOSITES_STATES + field,
				USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_OPEN);
		} else {
			CoreHub.userCfg.set(USERSETTINGS2_EXPANDABLE_COMPOSITES_STATES + field,
				USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_CLOSED);
		}
	}
	
	private void setExpandedState(ExpandableComposite ec, String field){
		String mode =
			CoreHub.userCfg.get(USERSETTINGS2_EXPANDABLE_COMPOSITES,
				USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_REMEMBER_STATE);
		if (mode.equals(USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_OPEN)) {
			ec.setExpanded(true);
		} else if (mode.equals(USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_CLOSED)) {
			ec.setExpanded(false);
		} else {
			String state =
				CoreHub.userCfg.get(USERSETTINGS2_EXPANDABLE_COMPOSITES_STATES + field,
					USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_CLOSED);
			if (state.equals(USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_CLOSED)) {
				ec.setExpanded(false);
			} else {
				ec.setExpanded(true);
			}
		}
	}
	
	@Override
	public void dispose(){
		GlobalEventDispatcher.removeActivationListener(this, site.getPart());
		super.dispose();
	}
	
	public void activation(boolean mode){
		/* egal */
	}
	
	public void visible(boolean mode){
		if (mode) {
			ElexisEventDispatcher.getInstance().addListeners(eeli_rn, eeli_user);
			Rechnung selected = (Rechnung) ElexisEventDispatcher.getSelected(Rechnung.class);
			if (selected != null) {
				doSelect(selected);
			}
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_rn, eeli_user);
		}
	}
	
	private void doSelect(Rechnung rn){
		actRn = rn;
		UiDesk.getDisplay().syncExec(new Runnable() {
			public void run(){
				display();
			}
		});
		
	}
	
	public void display(){
		rnform.reload(actRn);
		rnAdressat.setText(StringConstants.EMPTY);
		buchungen.refresh(true);
		lbJournal.removeAll();
		tRejects.setText(StringConstants.EMPTY);
		lbOutputs.removeAll();
		if (actRn != null) {
			rnAdressat.setText(Messages.RechnungsBlatt_adressee
				+ actRn.getFall().getGarant().getLabel()); //$NON-NLS-1$
			form.setText(actRn.getLabel());
			List<String> trace = actRn.getTrace(Rechnung.STATUS_CHANGED);
			for (String s : trace) {
				String[] stm = s.split("\\s*:\\s"); //$NON-NLS-1$
				StringBuilder sb = new StringBuilder();
				sb.append(stm[0]).append(" : ").append( //$NON-NLS-1$
					RnStatus.getStatusText(Integer.parseInt(stm[1])));
				lbJournal.add(sb.toString());
			}
			if (actRn.getStatus() == RnStatus.FEHLERHAFT) {
				List<String> rejects = actRn.getTrace(Rechnung.REJECTED);
				StringBuilder rjj = new StringBuilder();
				for (String r : rejects) {
					rjj.append(r).append("\n------\n"); //$NON-NLS-1$
				}
				tRejects.setText(rjj.toString());
			}
			List<String> outputs = actRn.getTrace(Rechnung.OUTPUT);
			for (String o : outputs) {
				lbOutputs.add(o);
			}
			tBemerkungen.setText(actRn.getBemerkung());
		}
		
		konsultationenViewer.refresh();
		stornoViewer.refresh();
		
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
