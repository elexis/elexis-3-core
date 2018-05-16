/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/
package ch.elexis.core.ui.views;

import static ch.elexis.core.ui.text.TextTemplateRequirement.TT_BILLING_LIST;

import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import com.tiff.common.ui.datepicker.DatePickerCombo;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.AbstractDataLoaderJob;
import ch.elexis.core.ui.actions.BackgroundJob;
import ch.elexis.core.ui.actions.BackgroundJob.BackgroundJobListener;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.text.ITextPlugin;
import ch.elexis.core.ui.text.ITextPlugin.ICallback;
import ch.elexis.core.ui.text.TextContainer;
import ch.elexis.core.ui.util.ListDisplay;
import ch.elexis.core.ui.util.Log;
import ch.elexis.core.ui.util.PersistentObjectDropTarget;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.CommonViewer.DoubleClickListener;
import ch.elexis.core.ui.util.viewers.DefaultContentProvider;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.views.codesystems.LeistungenView;
import ch.elexis.data.BillingSystem;
import ch.elexis.data.Brief;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Verrechnet;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.IFilter;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class PatHeuteView extends ViewPart implements IActivationListener, ISaveablePart2,
		BackgroundJobListener {
	public static final String ID = "ch.elexis.PatHeuteView"; //$NON-NLS-1$
	static final String LEISTUNG_HINZU = Messages.PatHeuteView_add; //$NON-NLS-1$
	static final String STAT_LEEREN = Messages.PatHeuteView_empty; //$NON-NLS-1$
	private IAction printAction, reloadAction, filterAction, statAction;
	CommonViewer cv;
	ViewerConfigurer vc;
	FormToolkit tk = UiDesk.getToolkit();
	Form form;
	Text tPat, tTime, tMoney, tTime2, tMoney2;
	TimeTool datVon, datBis;
	// boolean bOnlyOpen;
	boolean bOpen = true;
	boolean bClosed = true;
	private String accountSys;
	private Konsultation[] kons;
	private final KonsLoader kload;
	private int numPat;
	private double sumTime;
	private double sumAll;
	PersistentObjectDropTarget dropTarget;
	ListDisplay<IVerrechenbar> ldFilter;
	// private double sumSelected;
	private final Query<Konsultation> qbe;
	Composite parent;
	private final ElexisEventListener eeli_kons =
		new ElexisUiEventListenerImpl(Konsultation.class) {
			
			public void runInUi(ElexisEvent ev){
				selection((Konsultation) ev.getObject());
				
			}
		};
	
	public PatHeuteView(){
		super();
		datVon = new TimeTool();
		datBis = new TimeTool();
		qbe = new Query<Konsultation>(Konsultation.class);
		kload = new KonsLoader(qbe);
		kload.addListener(this);
	}
	
	@Override
	public void createPartControl(final Composite parent){
		parent.setLayout(new GridLayout());
		this.parent = parent;
		makeActions();
		ldFilter = new ListDisplay<IVerrechenbar>(parent, SWT.NONE, new ListDisplay.LDListener() {
			
			public String getLabel(final Object o){
				return ((IVerrechenbar) o).getCode();
			}
			
			public void hyperlinkActivated(final String l){
				if (l.equals(LEISTUNG_HINZU)) {
					try {
						if (StringTool.isNothing(LeistungenView.ID)) {
							SWTHelper.alert(Messages.PatHeuteView_error, //$NON-NLS-1$
								"LeistungenView.ID"); //$NON-NLS-1$
						}
						getViewSite().getPage().showView(LeistungenView.ID);
						CodeSelectorHandler.getInstance().setCodeSelectorTarget(dropTarget);
					} catch (Exception ex) {
						ExHandler.handle(ex);
					}
				} else if (l.equals(STAT_LEEREN)) {
					ldFilter.clear();
				}
				
			}
			
		});
		ldFilter.addHyperlinks(LEISTUNG_HINZU, STAT_LEEREN);
		ldFilter.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		((GridData) ldFilter.getLayoutData()).heightHint = 0;
		dropTarget = new PersistentObjectDropTarget("Statfilter", ldFilter, //$NON-NLS-1$
			new DropReceiver());
		Composite top = new Composite(parent, SWT.BORDER);
		top.setLayout(new RowLayout());
		final DatePickerCombo dpc = new DatePickerCombo(top, SWT.BORDER);
		dpc.setDate(datVon.getTime());
		dpc.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(final SelectionEvent e){
				datVon.setTimeInMillis(dpc.getDate().getTime());
			}
			
		});
		final DatePickerCombo dpb = new DatePickerCombo(top, SWT.BORDER);
		dpb.setDate(datBis.getTime());
		dpb.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(final SelectionEvent e){
				datBis.setTimeInMillis(dpb.getDate().getTime());
			}
			
		});
		final Button bOpenKons = new Button(top, SWT.CHECK);
		bOpenKons.setText(Messages.PatHeuteView_open); //$NON-NLS-1$
		bOpenKons.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e){
				bOpen = bOpenKons.getSelection();
			}
		});
		final Button bClosedKons = new Button(top, SWT.CHECK);
		bClosedKons.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e){
				bClosed = bClosedKons.getSelection();
			}
		});
		ComboViewer cAccountingSys = new ComboViewer(top, SWT.READ_ONLY);
		cAccountingSys.setContentProvider(ArrayContentProvider.getInstance());
		cAccountingSys.setLabelProvider(new LabelProvider());
		
		String allCases = Messages.PatHeuteView_all;
		List<String> faelle = Arrays.asList(BillingSystem.getAbrechnungsSysteme());
		
		List<String> accountingSys = new ArrayList<String>();
		accountingSys.add(allCases);
		accountingSys.addAll(faelle);
		
		cAccountingSys.setInput(accountingSys);
		cAccountingSys.setSelection(new StructuredSelection(allCases));
		
		cAccountingSys.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				accountSys = (String) selection.getFirstElement();
				
				// call reload
				if (kons != null) {
					kload.schedule();
				}
			}
		});
		
		bClosedKons.setText(Messages.PatHeuteView_billed); //$NON-NLS-1$
		bOpenKons.setSelection(bOpen);
		bClosedKons.setSelection(bClosed);
		cv = new CommonViewer();
		vc =
			new ViewerConfigurer(new DefaultContentProvider(cv, Patient.class) {
				@Override
				public Object[] getElements(final Object inputElement){
					if (!CoreHub.acl.request(AccessControlDefaults.ACCOUNTING_STATS)) {
						return new Konsultation[0];
					}
					if (kons == null) {
						kons = new Konsultation[0];
						kload.schedule();
					}
					
					return kons;
				}
			}, new DefaultLabelProvider() {
				
				@Override
				public String getText(final Object element){
					if (element instanceof Konsultation) {
						Fall fall = ((Konsultation) element).getFall();
						if (fall == null) {
							return Messages.PatHeuteView_noCase
								+ ((Konsultation) element).getLabel(); //$NON-NLS-1$
						}
						Patient pat = fall.getPatient();
						return pat.getLabel();
					}
					return super.getText(element);
				}
				
			}, null, new ViewerConfigurer.DefaultButtonProvider(), new SimpleWidgetProvider(
				SimpleWidgetProvider.TYPE_LIST, SWT.V_SCROLL, cv));
		cv.create(vc, parent, SWT.BORDER, getViewSite());
		
		form = tk.createForm(parent);
		form.setText(Messages.PatHeuteView_all); //$NON-NLS-1$
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Composite bottom = form.getBody();
		bottom.setLayout(new GridLayout(2, false));
		tk.createLabel(bottom, Messages.PatHeuteView_consultations); //$NON-NLS-1$
		tPat = tk.createText(bottom, "", SWT.BORDER); //$NON-NLS-1$
		tPat.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tPat.setEditable(false);
		tk.createLabel(bottom, Messages.PatHeuteView_accTime); //$NON-NLS-1$
		tTime = tk.createText(bottom, "", SWT.BORDER); //$NON-NLS-1$
		tTime.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tTime.setEditable(false);
		tk.createLabel(bottom, Messages.PatHeuteView_accAmount); //$NON-NLS-1$
		tMoney = tk.createText(bottom, "", SWT.BORDER); //$NON-NLS-1$
		tMoney.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tMoney.setEditable(false);
		// Group grpSel=new Group(parent,SWT.BORDER);
		// grpSel.setText("Markierte");
		// grpSel.setLayoutData(SWTHelper.getFillGridData(1,true,1,true));
		Form fSel = tk.createForm(parent);
		fSel.setText(Messages.PatHeuteView_marked); //$NON-NLS-1$
		fSel.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Composite cSel = fSel.getBody();
		cSel.setLayout(new GridLayout(2, false));
		tk.createLabel(cSel, Messages.PatHeuteView_accTime); //$NON-NLS-1$
		tTime2 = tk.createText(cSel, "", SWT.BORDER); //$NON-NLS-1$
		tTime2.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tk.createLabel(cSel, Messages.PatHeuteView_accAmount); //$NON-NLS-1$
		tMoney2 = tk.createText(cSel, "", SWT.BORDER); //$NON-NLS-1$
		tMoney2.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tTime2.setEditable(false);
		tMoney2.setEditable(false);
		ViewMenus menus = new ViewMenus(getViewSite());
		
		menus.createMenu(printAction, reloadAction, statAction);
		menus.createToolbar(reloadAction, filterAction);
		
		// setFocus();
		cv.getConfigurer().getContentProvider().startListening();
		GlobalEventDispatcher.addActivationListener(this, this);
		kload.schedule();
		
		cv.addDoubleClickListener(new DoubleClickListener() {
			@Override
			public void doubleClicked(PersistentObject obj, CommonViewer cv){
				Konsultation k = (Konsultation) obj;
				ElexisEventDispatcher.fireSelectionEvent(k);
				ElexisEventDispatcher.fireSelectionEvent(k.getFall());
				ElexisEventDispatcher.fireSelectionEvent(k.getFall().getPatient());
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(KonsDetailView.ID);
				} catch (PartInitException e) {
					ExHandler.handle(e);
				}
			}
		});
	}
	
	@Override
	public void dispose(){
		cv.getConfigurer().getContentProvider().stopListening();
		kload.removeListener(this);
		GlobalEventDispatcher.removeActivationListener(this, this);
		
	}
	
	@Override
	public void setFocus(){
		cv.notify(CommonViewer.Message.update);
		
	}
	
	private void selection(final Konsultation k){
		if (k == null) {
			tTime2.setText("");
			tMoney2.setText("");
		} else {
			try {
				tTime2.setText(Integer.toString(k.getMinutes()));
				Money mon = new Money();
				for (Verrechnet ver : k.getLeistungen()) {
					mon = mon.addMoney(ver.getNettoPreis().multiply(ver.getZahl()));
				}
				tMoney2.setText(mon.getAmountAsString());
			} catch (NullPointerException e) {
				// ignore -> handled during update
			}
		}
	}
	
	public void activation(final boolean mode){ /* leer */
	}
	
	public void visible(final boolean mode){
		if (mode == true) {
			ElexisEventDispatcher.getInstance().addListeners(eeli_kons);
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_kons);
		}
		
	}
	
	/*
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2 Wir benötigen das
	 * Interface nur, um das Schliessen einer View zu verhindern, wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */
	public int promptToSaveOnClose(){
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL
				: ISaveablePart2.NO;
	}
	
	public void doSave(final IProgressMonitor monitor){ /* leer */
	}
	
	public void doSaveAs(){ /* leer */
	}
	
	public boolean isDirty(){
		return true;
	}
	
	public boolean isSaveAsAllowed(){
		return false;
	}
	
	public boolean isSaveOnCloseNeeded(){
		return true;
	}
	
	class StatLoader extends Job {
		
		StatLoader(){
			super(Messages.PatHeuteView_calculateStats); //$NON-NLS-1$
			setPriority(Job.LONG);
			setUser(true);
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor){
			HashMap<IVerrechenbar, StatCounter> counter = new HashMap<IVerrechenbar, StatCounter>();
			monitor.beginTask(Messages.PatHeuteView_calculateStats, kons.length + 20); //$NON-NLS-1$
			
			System.out.println(Messages.PatHeuteView_consElexis + kons.length); //$NON-NLS-1$
			int serviceCounter = 0;
			
			for (Konsultation k : kons) {
				List<Verrechnet> list = k.getLeistungen();
				serviceCounter += list.size();
				for (Verrechnet v : list) {
					StatCounter sc = counter.get(v.getVerrechenbar());
					if (sc == null) {
						sc = new StatCounter(v.getVerrechenbar());
						counter.put(v.getVerrechenbar(), sc);
					}
					sc.add(v.getZahl(), v.getNettoPreis(), v.getKosten());
				}
				monitor.worked(1);
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
			}
			
			System.out.println(Messages.PatHeuteView_servicesElexis + serviceCounter); //$NON-NLS-1$
			
			final List<StatCounter> sums = new LinkedList<StatCounter>(counter.values());
			Collections.sort(sums);
			monitor.worked(20);
			monitor.done();
			UiDesk.asyncExec(new Runnable() {
				public void run(){
					FileDialog fd = new FileDialog(getSite().getShell(), SWT.SAVE);
					fd.setFilterExtensions(new String[] {
						"*.csv", "*.*"}); //$NON-NLS-1$ //$NON-NLS-2$
					fd.setFilterNames(new String[] {
						"CSV", Messages.PatHeuteView_allFiles}); //$NON-NLS-1$ //$NON-NLS-2$
					fd.setFileName("elexis-stat.csv"); //$NON-NLS-1$
					String fname = fd.open();
					if (fname != null) {
						try {
							FileWriter fw = new FileWriter(fname);
							fw.write(Messages.PatHeuteView_csvHeader); //$NON-NLS-1$
							for (StatCounter st : sums) {
								StringBuilder sb = new StringBuilder();
								String code = "unknown";
								String text = "unknown";
								if (st.v != null) {
									code = st.v.getCode();
									text = st.v.getText();
								}
								if (text == null) {
									text = ""; //$NON-NLS-1$
								} else {
									text = text.replaceAll(";", ","); //$NON-NLS-1$ //$NON-NLS-2$
								}
								if (st.v != null && st.v.getCodeSystemName() != null) {
									sb.append(st.v.getCodeSystemName());
								} else {
									sb.append("Codesystem?");
								}
								sb.append("; ").append( //$NON-NLS-1$
									code == null ? "" : code) //$NON-NLS-1$
									.append("; ").append(text).append(";") //$NON-NLS-1$ //$NON-NLS-2$
									.append(st.num).append(";").append( //$NON-NLS-1$
										st.cost.getAmountAsString()).append(";").append( //$NON-NLS-1$
										st.sum.getAmountAsString()).append(";").append( //$NON-NLS-1$
										st.getGewinn().getAmountAsString()).append("\r\n"); //$NON-NLS-1$
								fw.write(sb.toString());
							}
							fw.close();
						} catch (Exception ex) {
							ExHandler.handle(ex);
							SWTHelper.showError(Messages.PatHeuteView_errorWriting, ex //$NON-NLS-1$
								.getMessage());
						}
					}
					
				}
			});
			return Status.OK_STATUS;
		}
		
	}
	
	class KonsLoader extends AbstractDataLoaderJob {
		IVerrechenbar[] lfiltered;
		Set<Konsultation> corruptKons;
		Set<Konsultation> missingCaseKons;
		
		KonsLoader(final Query<Konsultation> qbe){
			super(Messages.PatHeuteView_loadConsultations, qbe, new String[] {
				Messages.PatHeuteView_date
			}); //$NON-NLS-1$ //$NON-NLS-2$
			setPriority(Job.LONG);
			setUser(true);
		}
		
		@Override
		public IStatus execute(final IProgressMonitor monitor){
			if (CoreHub.actUser == null) {
				return Status.CANCEL_STATUS;
			}
			corruptKons = new HashSet<Konsultation>();
			missingCaseKons = new HashSet<Konsultation>();
			monitor.beginTask(Messages.PatHeuteView_loadKons, 1000); //$NON-NLS-1$
			qbe.clear();
			qbe.add(Konsultation.DATE, Query.GREATER_OR_EQUAL,
				datVon.toString(TimeTool.DATE_COMPACT));
			qbe.add(Konsultation.DATE, Query.LESS_OR_EQUAL, datBis.toString(TimeTool.DATE_COMPACT));
			if (CoreHub.acl.request(AccessControlDefaults.ACCOUNTING_GLOBAL) == false) {
				if (CoreHub.actMandant == null) {
					monitor.done();
					return Status.OK_STATUS;
				}
				qbe.add(Konsultation.FLD_MANDATOR_ID, Query.EQUALS, CoreHub.actMandant.getId());
			}
			
			if (bOpen && !bClosed) {
				qbe.add("RechnungsID", StringConstants.EMPTY, null); //$NON-NLS-1$
			}
			if (bClosed && !bOpen) {
				qbe.add("RechnungsID", "NOT", null); //$NON-NLS-2$
			}
			if (!bClosed && !bOpen) {
				qbe.insertFalse();
			}
			qbe.addPostQueryFilter(new IFilter() {
				public boolean select(final Object toTest){
					if (filterAction.isChecked()) {
						Konsultation k = (Konsultation) toTest;
						List<IVerrechenbar> lFilt = ldFilter.getAll();
						for (Verrechnet v : k.getLeistungen()) {
							if (lFilt.contains(v.getVerrechenbar())) {
								return true;
							}
						}
						return false;
					}
					return true;
				}
				
			});
			@SuppressWarnings("unchecked")
			List<Konsultation> list = qbe.execute();
			monitor.worked(100);
			numPat = 0;
			sumAll = 0.0;
			sumTime = 0.0;
			if (list == null) {
				result = new Konsultation[0];
			} else {
				if (accountSys != null && !accountSys.isEmpty()
					&& !accountSys.equals(Messages.PatHeuteView_all)) {
					List<Konsultation> konsRet = new ArrayList<Konsultation>();
					for (Konsultation kons : list) {
						if (kons.getFall().getAbrechnungsSystem().equals(accountSys)) {
							konsRet.add(kons);
						}
					}
					list = konsRet;
				}
				Konsultation[] ret = new Konsultation[list.size()];
				
				if (filterAction.isChecked()) {
					lfiltered = ldFilter.getAll().toArray(new IVerrechenbar[0]);
					if (lfiltered.length == 0) {
						lfiltered = null;
					}
				} else {
					lfiltered = null;
				}
				int i = 0;
				for (PersistentObject o : list) {
					if (((Konsultation) o).getFall() == null) {
						missingCaseKons.add((Konsultation) o);
					}
					try {
						ret[i++] = (Konsultation) o;
						if (lfiltered != null) {
							List<Verrechnet> lstg = ((Konsultation) o).getLeistungen();
							for (Verrechnet v : lstg) {
								int num = v.getZahl();
								Money preis = v.getEffPreis().multiply(num);
								for (int j = 0; j < lfiltered.length; j++) {
									if (lfiltered[j].equals(v.getVerrechenbar())) {
										sumAll += preis.getCents();
										sumTime += v.getVerrechenbar().getMinutes();
									}
								}
							}
						} else {
							for (Verrechnet verr : ((Konsultation) o).getLeistungen()) {
								sumAll +=
									verr.getNettoPreis().multiply(verr.getZahl()).doubleValue();
							}
							sumTime += ((Konsultation) o).getMinutes();
						}
						monitor.worked(1);
						
						if (monitor.isCanceled()) {
							monitor.done();
							result = new Konsultation[0];
							return Status.CANCEL_STATUS;
						}
					} catch (NullPointerException np) {
						corruptKons.add((Konsultation) o);
					}
				}
				numPat = ret.length;
				result = ret;
				monitor.done();
			}
			
			// show message in case there are corrupt consultations
			StringBuilder sb = new StringBuilder();
			if (corruptKons.size() > 0) {
				sb.append("Folgende Konsultationen enthalten ungültige Leistungen: \n");
				for (Konsultation k : corruptKons) {
					sb.append(k.getLabel() + ", " + k.getFall().getPatient().getLabel());
					sb.append("\n");
				}
				sb.append("\n");
			}
			
			if (missingCaseKons.size() > 0) {
				sb.append("Folgende Konsultationen sind keinem Fall zugewiesen: \n");
				for (Konsultation k : missingCaseKons) {
					sb.append(k.getLabel());
					sb.append("\n");
				}
			}
			
			if (sb.length() > 0) {
				SWTHelper.showError("Achtung",
					"Fehlerhafte Konsultation(en) vorhanden!\n" + sb.toString());
				log.log("The following consultations contain invalid values...\n" + sb.toString(),
					Log.ERRORS);
			}
			
			return Status.OK_STATUS;
		}
		
		@Override
		public int getSize(){
			return 100;
		}
		
	}
	
	public void jobFinished(final BackgroundJob j){
		if (j.isValid()) {
			kons = (Konsultation[]) j.getData();
			if (!CoreHub.acl.request(AccessControlDefaults.ACCOUNTING_STATS)) {
				tPat.setText("Sie haben keine Rechte für diese View");
			} else {
				tPat.setText(Integer.toString(numPat));
				tTime.setText(Double.toString(sumTime));
				DecimalFormat df = new DecimalFormat("0.00"); //$NON-NLS-1$
				tMoney.setText(df.format(sumAll));
				if (kons.length >= 1) {
					selection(kons[kons.length - 1]);
				}
				cv.notify(CommonViewer.Message.update);
			}
		} else {
			kons = new Konsultation[0];
		}
		
	};
	
	private static class StatCounter implements Comparable<StatCounter> {
		IVerrechenbar v;
		Money sum;
		Money cost;
		int num;
		
		StatCounter(IVerrechenbar vv){
			v = vv;
			sum = new Money();
			cost = new Money();
			num = 0;
		}
		
		void add(int num, Money price, Money cost){
			
			Money totalPrice = price.multiply(num);
			Money totalCost = cost.multiply(num);
			this.num += num;
			sum.addMoney(totalPrice);
			this.cost.addMoney(totalCost);
			
		}
		
		public int compareTo(StatCounter o){
			String v1 = null, v2 = null;
			String vc1 = null, vc2 = null;
			if (v != null) {
				v1 = v.getCodeSystemName();
				vc1 = v.getCode();
			}
			IVerrechenbar iv = o.v;
			if (iv != null) {
				v2 = iv.getCodeSystemName();
				vc2 = iv.getCode();
			}
			int vgroup = StringTool.compareWithNull(v1, v2);
			if (vgroup != 0) {
				return vgroup;
			}
			
			int vCode = StringTool.compareWithNull(vc1, vc2);
			if (vCode != 0) {
				return vCode;
			}
			return sum.getCents() - o.sum.getCents();
		}
		
		public Money getGewinn(){
			Money ret = new Money(sum);
			ret.subtractMoney(cost);
			return ret;
		}
	}
	
	private void makeActions(){
		statAction = new Action(Messages.PatHeuteView_statisticsAction) { //$NON-NLS-1$
				{
					setToolTipText(Messages.PatHeuteView_statisticsToolTip); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					StatLoader loader = new StatLoader();
					loader.schedule();
				}
			};
		printAction = new Action(Messages.PatHeuteView_printList) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_PRINTER.getImageDescriptor());
					setToolTipText(Messages.PatHeuteView_printListToolTip); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					TerminListeDialog tld = new TerminListeDialog(getViewSite().getShell());
					tld.open();
				}
			};
		
		reloadAction = new Action(Messages.PatHeuteView_reloadAction) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_REFRESH.getImageDescriptor());
					setToolTipText(Messages.PatHeuteView_reloadToolTip); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					kons = null;
					kload.schedule();
				}
			};
		
		filterAction = new Action(Messages.PatHeuteView_filterAction, Action.AS_CHECK_BOX) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_FILTER.getImageDescriptor());
					setToolTipText(Messages.PatHeuteView_filterToolTip); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					GridData gd = (GridData) ldFilter.getLayoutData();
					if (filterAction.isChecked()) {
						gd.heightHint = 50;
						// gd.minimumHeight=15;
					} else {
						gd.heightHint = 0;
					}
					parent.layout(true);
				}
				
			};
		
	}
	
	class TerminListeDialog extends TitleAreaDialog implements ICallback {
		IVerrechenbar[] lfiltered;
		int[] numLeistung;
		Money[] perLeistung;
		private TextContainer text;
		
		public TerminListeDialog(final Shell shell){
			super(shell);
		}
		
		@Override
		protected Control createDialogArea(final Composite parent){
			Composite ret = new Composite(parent, SWT.NONE);
			text = new TextContainer(getShell());
			ret.setLayout(new FillLayout());
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			text.getPlugin().createContainer(ret, this);
			text.getPlugin().showMenu(false);
			text.getPlugin().showToolbar(false);
			int add = 2;
			if (filterAction.isChecked()) {
				lfiltered = ldFilter.getAll().toArray(new IVerrechenbar[0]);
				numLeistung = new int[lfiltered.length];
				add += lfiltered.length;
				perLeistung = new Money[lfiltered.length];
				for (int i = 0; i < lfiltered.length; i++) {
					perLeistung[i] = new Money();
				}
			}
			text.createFromTemplateName(null, TT_BILLING_LIST, //$NON-NLS-1$
				Brief.UNKNOWN, CoreHub.actUser, Messages.PatHeuteView_billing); //$NON-NLS-1$
			String[][] table = new String[kons.length + add][];
			table[0] = new String[2];
			table[0][0] = Messages.PatHeuteView_consultation; //$NON-NLS-1$
			table[0][1] = Messages.PatHeuteView_amountBilled; //$NON-NLS-1$
			Money total = new Money();
			for (int i = 0; i < kons.length; i++) {
				table[i + 1] = new String[2];
				Konsultation k = kons[i];
				table[i + 1][0] = k.getFall().getPatient().getLabel() + "\n" //$NON-NLS-1$
					+ k.getLabel();
				StringBuilder sb = new StringBuilder();
				List<Verrechnet> lstg = k.getLeistungen();
				Money subsum = new Money();
				for (Verrechnet v : lstg) {
					int num = v.getZahl();
					Money preis = v.getEffPreis().multiply(num);
					if (lfiltered != null) {
						for (int j = 0; j < lfiltered.length; j++) {
							if (lfiltered[j].equals(v.getVerrechenbar())) {
								numLeistung[j] += num;
								perLeistung[j].addMoney(preis);
							}
						}
					}
					subsum.addMoney(preis);
					sb.append(num).append(" ").append(v.getLabel()).append(" ") //$NON-NLS-1$ //$NON-NLS-2$
						.append(preis.getAmountAsString()).append("\n"); //$NON-NLS-1$
				}
				sb.append(Messages.PatHeuteView_total).append(subsum.getAmountAsString()); //$NON-NLS-1$
				total.addMoney(subsum);
				table[i + 1][1] = sb.toString();
			}
			table[kons.length + 1] = new String[2];
			table[kons.length + 1][0] = Messages.PatHeuteView_sum; //$NON-NLS-1$
			table[kons.length + 1][1] = total.getAmountAsString();
			if (lfiltered != null) {
				for (int i = 0; i < lfiltered.length; i++) {
					table[kons.length + 2 + i] = new String[2];
					table[kons.length + 2 + i][0] = lfiltered[i].getCode();
					StringBuilder sb = new StringBuilder();
					sb.append(Messages.PatHeuteView_billedTotal).append(numLeistung[i]).append( //$NON-NLS-1$
						Messages.PatHeuteView_times).append( //$NON-NLS-1$
						perLeistung[i].getAmountAsString());
					table[kons.length + 2 + i][1] = sb.toString();
				}
			}
			text.getPlugin().setFont("Helvetica", SWT.NORMAL, 9); //$NON-NLS-1$
			text.getPlugin().insertTable("[Liste]", //$NON-NLS-1$
				ITextPlugin.FIRST_ROW_IS_HEADER, table, new int[] {
					30, 70
				});
			return ret;
		}
		
		@Override
		public void create(){
			super.create();
			getShell().setText(Messages.PatHeuteView_billingList); //$NON-NLS-1$
			setTitle(Messages.PatHeuteView_printBillingList); //$NON-NLS-1$
			setMessage(Messages.PatHeuteView_printBillingExpl); //$NON-NLS-1$
			getShell().setSize(900, 700);
			SWTHelper.center(Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getShell(),
				getShell());
		}
		
		@Override
		protected void okPressed(){
			super.okPressed();
		}
		
		public void save(){
			// TODO Auto-generated method stub
			
		}
		
		public boolean saveAs(){
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	private final class DropReceiver implements PersistentObjectDropTarget.IReceiver {
		public void dropped(final PersistentObject o, final DropTargetEvent ev){
			if (o instanceof IVerrechenbar) {
				ldFilter.add((IVerrechenbar) o);
			}
		}
		
		public boolean accept(final PersistentObject o){
			if (o instanceof IVerrechenbar) {
				return true;
			}
			return false;
		}
	}
}
