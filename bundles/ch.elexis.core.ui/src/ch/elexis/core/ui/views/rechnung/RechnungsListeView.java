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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.constants.UiResourceConstants;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.CommonViewer.DoubleClickListener;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.views.FallDetailView;
import ch.elexis.core.ui.views.rechnung.invoice.InvoiceActions;
import ch.elexis.core.ui.views.rechnung.invoice.InvoiceListBottomComposite;
import ch.elexis.data.Anwender;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Rechnung;
import ch.rgw.io.Settings;
import ch.rgw.tools.Tree;

/**
 * Display a listing of all bills selected after several user selectable criteria. The selected
 * bills can be modified or exported.
 * 
 * @author gerry
 * 
 */
public class RechnungsListeView extends ViewPart implements ElexisEventListener {
	
	public final static String ID = "ch.elexis.RechnungsListeView"; //$NON-NLS-1$
	
	CommonViewer cv;
	ViewerConfigurer vc;
	RnActions actions;
	RnContentProvider cntp;
	RnControlFieldProvider cfp;
	
	private Settings rnStellerSettings;
	
	private InvoiceListBottomComposite invoiceListeBottomComposite;
	
	private ElexisEventListener eeli_mandant =
		new ElexisUiEventListenerImpl(Mandant.class, ElexisEvent.EVENT_MANDATOR_CHANGED) {
			
			@Override
			public void runInUi(ElexisEvent ev){
				Mandant m = (Mandant) ElexisEventDispatcher.getSelected(Mandant.class);
				if (m != null) {
					rnStellerSettings = CoreHub.getUserSetting(m.getRechnungssteller());
					checkRnStellerSettingsValidity(m);
					//				cv.notify(CommonViewer.Message.update);
					if (invoiceListeBottomComposite != null) {
						invoiceListeBottomComposite.updateMahnAutomatic();
					}
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
			
			KontaktSelektor ksDialog = new KontaktSelektor(UiDesk.getTopShell(), Anwender.class,
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
		vc = new ViewerConfigurer(cntp, new ViewerConfigurer.TreeLabelProvider(), cfp,
			new ViewerConfigurer.DefaultButtonProvider(),
			new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_TREE, SWT.V_SCROLL | SWT.MULTI, cv));
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
		
		invoiceListeBottomComposite =
			new InvoiceListBottomComposite(comp, SWT.NONE, rnStellerSettings);
		
		ElexisEventDispatcher.getInstance().addListeners(this);
		cv.getViewerWidget().getControl()
			.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ViewMenus menu = new ViewMenus(getViewSite());
		actions = new RnActions(this);
		menu.createToolbar(actions.reloadAction, actions.mahnWizardAction, actions.rnFilterAction,
			null, actions.rnExportAction);
		menu.createMenu(actions.expandAllAction, actions.collapseAllAction,
			actions.printListeAction, actions.exportListAction, actions.addAccountExcessAction);
		MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new RnMenuListener(this));
		cv.setContextMenu(mgr);
		cntp.startListening();
	}
	
	public InvoiceListBottomComposite getInvoiceListeBottomComposite(){
		return invoiceListeBottomComposite;
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
						Rechnung rn = (Rechnung) tf.contents;
						if (!ret.contains(rn)) {
							ret.add(rn);
						}
					}
				}
			} else if (t.contents instanceof Fall) {
				for (Tree<PersistentObject> tr : t.getChildren()) {
					Rechnung rn = (Rechnung) tr.contents;
					if (!ret.contains(rn)) {
						ret.add(rn);
					}
				}
			} else if (t.contents instanceof Rechnung) {
				Rechnung rn = (Rechnung) t.contents;
				if (!ret.contains(rn)) {
					ret.add(rn);
				}
			}
		}
		return ret;
	}
	
	public void catchElexisEvent(ElexisEvent ev){
		cv.notify(CommonViewer.Message.update);
	}
	
	private final ElexisEvent eetmpl =
		new ElexisEvent(null, Rechnung.class, ElexisEvent.EVENT_RELOAD);
	
	public ElexisEvent getElexisEventFilter(){
		return eetmpl;
	}
	
}
