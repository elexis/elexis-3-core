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
package ch.elexis.core.ui.views.rechnung;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.constants.UiResourceConstants;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.util.MoneyInput;
import ch.elexis.core.ui.util.NumberInput;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.CommonViewer.DoubleClickListener;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.views.FallDetailView;
import ch.elexis.data.Anwender;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Rechnung;
import ch.rgw.io.Settings;
import ch.rgw.tools.Money;
import ch.rgw.tools.Tree;

/**
 * Display a listing of all bills selected after several user selectable criteria. The selected
 * bills can be modified or exported.
 * 
 * @author gerry
 * 
 */
public class RechnungsListeView extends ViewPart implements ElexisEventListener {
	private static final String REMINDER_3 = Messages.RechnungsListeView_reminder3; //$NON-NLS-1$
	
	private static final String REMINDER_2 = Messages.RechnungsListeView_reminder2; //$NON-NLS-1$
	
	private static final String REMINDER_1 = Messages.RechnungsListeView_reminder1; //$NON-NLS-1$
	
	public final static String ID = "ch.elexis.RechnungsListeView"; //$NON-NLS-1$
	
	CommonViewer cv;
	ViewerConfigurer vc;
	RnActions actions;
	RnContentProvider cntp;
	RnControlFieldProvider cfp;
	
	Text tPat, tRn, tSum, tOpen;
	NumberInput niDaysTo1st, niDaysTo2nd, niDaysTo3rd;
	MoneyInput mi1st, mi2nd, mi3rd;
	SelectionListener mahnWizardListener;
	FormToolkit tk = UiDesk.getToolkit();
	Settings rnStellerSettings;
	
	private ElexisEventListener eeli_mandant = new ElexisUiEventListenerImpl(Mandant.class,
		ElexisEvent.EVENT_MANDATOR_CHANGED) {
		
		@Override
		public void runInUi(ElexisEvent ev){
			Mandant m = (Mandant) ElexisEventDispatcher.getSelected(Mandant.class);
			if (m != null) {
				rnStellerSettings = CoreHub.getUserSetting(m.getRechnungssteller());
				checkRnStellerSettingsValidity(m);
				cv.notify(CommonViewer.Message.update);
				updateMahnAutomatic();
			}
		}
	};
	
	public RechnungsListeView(){
		Mandant currMandant = (Mandant) ElexisEventDispatcher.getSelected(Mandant.class);
		rnStellerSettings = CoreHub.getUserSetting(currMandant.getRechnungssteller());
		checkRnStellerSettingsValidity(currMandant);
		ElexisEventDispatcher.getInstance().addListeners(eeli_mandant);
	}
	
	private void checkRnStellerSettingsValidity(Mandant mandant){
		if (rnStellerSettings == null) {
			Kontakt k = null;
			
			KontaktSelektor ksDialog =
				new KontaktSelektor(UiDesk.getTopShell(), Anwender.class,
					Messages.RechnungsListeView_selectRnSteller,
					Messages.RechnungsListeView_selectRnStellerMsg, new String[] {
						Anwender.FLD_NAME1, Anwender.FLD_NAME2
					});
			
			if (ksDialog.open() == Dialog.OK) {
				if (ksDialog.getSelection() != null) {
					k = (Kontakt) ksDialog.getSelection();
					if (k != null) {
						mandant.setRechnungssteller(k);
						rnStellerSettings = CoreHub.getUserSetting(k);
					}
				}
			}
		}
		
		if (rnStellerSettings == null) {
			MessageDialog.openError(UiDesk.getTopShell(), Messages.RechnungsListeView_error,
				Messages.RechnungsListeView_errorNoRnStellerSelected);
		}
	}
	
	@Override
	public void createPartControl(final Composite p){
		p.setLayout(new GridLayout());
		// SashForm sash=new SashForm(p,SWT.VERTICAL);
		Composite comp = new Composite(p, SWT.NONE);
		comp.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		comp.setLayout(new GridLayout());
		cv = new CommonViewer();
		cntp = new RnContentProvider(this, cv);
		cfp = new RnControlFieldProvider();
		vc =
			new ViewerConfigurer(cntp, new ViewerConfigurer.TreeLabelProvider(), cfp,
				new ViewerConfigurer.DefaultButtonProvider(), new SimpleWidgetProvider(
					SimpleWidgetProvider.TYPE_TREE, SWT.V_SCROLL | SWT.MULTI, cv));
		// rnFilter=FilterFactory.createFilter(Rechnung.class,"Rn
		// Nummer","Name","Vorname","Betrag");
		cv.create(vc, comp, SWT.BORDER, getViewSite());
		
		cv.addDoubleClickListener(new DoubleClickListener() {
			@Override
			public void doubleClicked(PersistentObject obj, CommonViewer cv){
				if (obj instanceof Patient) {
					try {
						ElexisEventDispatcher.fireSelectionEvent((Patient) obj);
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.showView(UiResourceConstants.PatientDetailView2_ID);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				} else if (obj instanceof Fall) {
					try {
						ElexisEventDispatcher.fireSelectionEvent((Fall) obj);
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.showView(FallDetailView.ID);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				} else if (obj instanceof Rechnung) {
					try {
						ElexisEventDispatcher.fireSelectionEvent((Rechnung) obj);
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.showView(RnDetailView.ID);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		Composite bottom = new Composite(comp, SWT.NONE);
		
		RowLayout rowLayout = new RowLayout();
		rowLayout.wrap = false;
		rowLayout.pack = true;
		rowLayout.justify = true;
		rowLayout.fill = true;
		rowLayout.type = SWT.HORIZONTAL;
		rowLayout.marginLeft = 0;
		rowLayout.marginTop = 0;
		rowLayout.marginRight = 0;
		rowLayout.marginBottom = 0;
		rowLayout.spacing = 5;
		
		mahnWizardListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e){
				rnStellerSettings.set(Preferences.RNN_DAYSUNTIL1ST, niDaysTo1st.getValue());
				rnStellerSettings.set(Preferences.RNN_DAYSUNTIL2ND, niDaysTo2nd.getValue());
				rnStellerSettings.set(Preferences.RNN_DAYSUNTIL3RD, niDaysTo3rd.getValue());
				rnStellerSettings.set(Preferences.RNN_AMOUNT1ST, mi1st.getMoney(false)
					.getAmountAsString());
				rnStellerSettings.set(Preferences.RNN_AMOUNT2ND, mi2nd.getMoney(false)
					.getAmountAsString());
				rnStellerSettings.set(Preferences.RNN_AMOUNT3RD, mi3rd.getMoney(false)
					.getAmountAsString());
				rnStellerSettings.flush();
			}
			
		};
		
		bottom.setLayout(rowLayout);
		Form fSum = tk.createForm(bottom);
		Form fWizard = tk.createForm(bottom);
		fSum.setText(Messages.RechnungsListeView_sum); //$NON-NLS-1$
		fWizard.setText(Messages.RechnungsListeView_dunningAutomatics); //$NON-NLS-1$
		Composite cSum = fSum.getBody();
		cSum.setLayout(new GridLayout(2, false));
		tk.createLabel(cSum, Messages.RechnungsListeView_patInList); //$NON-NLS-1$
		tPat = tk.createText(cSum, "", SWT.BORDER | SWT.READ_ONLY); //$NON-NLS-1$
		tPat.setLayoutData(new GridData(100, SWT.DEFAULT));
		tk.createLabel(cSum, Messages.RechnungsListeView_accountsInList); //$NON-NLS-1$
		tRn = tk.createText(cSum, "", SWT.BORDER | SWT.READ_ONLY); //$NON-NLS-1$
		tRn.setLayoutData(new GridData(100, SWT.DEFAULT));
		tk.createLabel(cSum, Messages.RechnungsListeView_sumInList); //$NON-NLS-1$
		tSum = SWTHelper.createText(tk, cSum, 1, SWT.BORDER | SWT.READ_ONLY);
		tSum.setLayoutData(new GridData(100, SWT.DEFAULT));
		tk.createLabel(cSum, Messages.RechnungsListeView_paidInList); //$NON-NLS-1$
		tOpen = SWTHelper.createText(tk, cSum, 1, SWT.BORDER | SWT.READ_ONLY);
		tOpen.setLayoutData(new GridData(100, SWT.DEFAULT));
		Composite cW = fWizard.getBody();
		cW.setLayout(new GridLayout(4, true));
		
		tk.createLabel(cW, Messages.RechnungsListeView_delayInDays); //$NON-NLS-1$
		
		niDaysTo1st = new NumberInput(cW, REMINDER_1);
		niDaysTo1st.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		niDaysTo1st.getControl().addSelectionListener(mahnWizardListener);
		niDaysTo1st.setValue(rnStellerSettings.get(Preferences.RNN_DAYSUNTIL1ST, 30));
		niDaysTo2nd = new NumberInput(cW, REMINDER_2);
		niDaysTo2nd.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		niDaysTo2nd.getControl().addSelectionListener(mahnWizardListener);
		niDaysTo2nd.setValue(rnStellerSettings.get(Preferences.RNN_DAYSUNTIL2ND, 10));
		niDaysTo3rd = new NumberInput(cW, REMINDER_3);
		niDaysTo3rd.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		niDaysTo3rd.getControl().addSelectionListener(mahnWizardListener);
		niDaysTo3rd.setValue(rnStellerSettings.get(Preferences.RNN_DAYSUNTIL3RD, 5));
		tk.createLabel(cW, Messages.RechnungsListeView_fine); //$NON-NLS-1$
		mi1st = new MoneyInput(cW, REMINDER_1);
		mi1st.addSelectionListener(mahnWizardListener);
		mi1st.setMoney(rnStellerSettings.get(Preferences.RNN_AMOUNT1ST,
			new Money().getAmountAsString()));
		mi2nd = new MoneyInput(cW, REMINDER_2);
		mi2nd.addSelectionListener(mahnWizardListener);
		mi2nd.setMoney(rnStellerSettings.get(Preferences.RNN_AMOUNT2ND,
			new Money().getAmountAsString()));
		mi3rd = new MoneyInput(cW, REMINDER_3);
		mi3rd.addSelectionListener(mahnWizardListener);
		mi3rd.setMoney(rnStellerSettings.get(Preferences.RNN_AMOUNT3RD,
			new Money().getAmountAsString()));
		
		ElexisEventDispatcher.getInstance().addListeners(this);
		cv.getViewerWidget().getControl()
			.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ViewMenus menu = new ViewMenus(getViewSite());
		actions = new RnActions(this);
		menu.createToolbar(actions.reloadAction, actions.mahnWizardAction, actions.rnFilterAction,
			null, actions.rnExportAction);
		menu.createMenu(actions.expandAllAction, actions.collapseAllAction,
			actions.printListeAction, actions.addAccountExcessAction);
		MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new RnMenuListener(this));
		cv.setContextMenu(mgr);
		cntp.startListening();
	}
	
	@Override
	public void dispose(){
		ElexisEventDispatcher.getInstance().removeListeners(this);
		ElexisEventDispatcher.getInstance().removeListeners(eeli_mandant);
		cntp.stopListening();
		super.dispose();
	}
	
	@Override
	public void setFocus(){
		// TODO Auto-generated method stub
		
	}
	
	@SuppressWarnings("unchecked")
	List<Rechnung> createList(){
		IStructuredSelection sel = (IStructuredSelection) cv.getViewerWidget().getSelection();
		List<Tree> at = sel.toList();
		List<Rechnung> ret = new LinkedList<Rechnung>();
		for (Tree<PersistentObject> t : at) {
			if (t.contents instanceof Patient) {
				for (Tree<PersistentObject> tp : t.getChildren()) {
					for (Tree<PersistentObject> tf : tp.getChildren()) {
						ret.add((Rechnung) tf.contents);
					}
				}
			} else if (t.contents instanceof Fall) {
				for (Tree<PersistentObject> tr : t.getChildren()) {
					ret.add((Rechnung) tr.contents);
				}
			} else if (t.contents instanceof Rechnung) {
				Rechnung r = (Rechnung) t.contents;
				ret.add(r);
			}
		}
		return ret;
	}
	
	public void catchElexisEvent(ElexisEvent ev){
		cv.notify(CommonViewer.Message.update);
	}
	
	private final ElexisEvent eetmpl = new ElexisEvent(null, Rechnung.class,
		ElexisEvent.EVENT_RELOAD);
	
	public ElexisEvent getElexisEventFilter(){
		return eetmpl;
	}
	
	private void updateMahnAutomatic(){
		niDaysTo1st.setValue(rnStellerSettings.get(Preferences.RNN_DAYSUNTIL1ST, 30));
		niDaysTo2nd.setValue(rnStellerSettings.get(Preferences.RNN_DAYSUNTIL2ND, 10));
		niDaysTo3rd.setValue(rnStellerSettings.get(Preferences.RNN_DAYSUNTIL3RD, 5));
		mi1st.setMoney(rnStellerSettings.get(Preferences.RNN_AMOUNT1ST,
			new Money().getAmountAsString()));
		mi2nd.setMoney(rnStellerSettings.get(Preferences.RNN_AMOUNT2ND,
			new Money().getAmountAsString()));
		mi3rd.setMoney(rnStellerSettings.get(Preferences.RNN_AMOUNT3RD,
			new Money().getAmountAsString()));
	}
	
}
