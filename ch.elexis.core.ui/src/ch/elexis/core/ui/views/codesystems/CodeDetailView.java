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
package ch.elexis.core.ui.views.codesystems;

import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.statushandlers.StatusManager;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListenerImpl;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.util.ImporterPage;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.views.IDetailDisplay;

public class CodeDetailView extends ViewPart implements IActivationListener, ISaveablePart2 {
	public final static String ID = "ch.elexis.codedetailview"; //$NON-NLS-1$
	private CTabFolder ctab;
	private IAction importAction;
	private ViewMenus viewmenus;
	private Hashtable<String, ImporterPage> importers;
	
	@Override
	public void createPartControl(Composite parent){
		parent.setLayout(new FillLayout());
		ctab = new CTabFolder(parent, SWT.NONE);
		importers = new Hashtable<String, ImporterPage>();
		addCustomBlocksPage();
		importers.put(ctab.getItem(0).getText(), new BlockImporter());
		
		addPagesFor(ExtensionPointConstantsUi.DIAGNOSECODE);
		addPagesFor(ExtensionPointConstantsUi.VERRECHNUNGSCODE);
		if (ctab.getItemCount() > 0) {
			ctab.setSelection(0);
			
		}
		ctab.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				CTabItem top = ctab.getSelection();
				if (top != null) {
					String t = top.getText();
					importAction.setEnabled(importers.get(t) != null);
					MasterDetailsPage page = (MasterDetailsPage) top.getControl();
					ViewerConfigurer vc = page.cv.getConfigurer();
					vc.getControlFieldProvider().setFocus();
				}
			}
			
		});
		makeActions();
		viewmenus = new ViewMenus(getViewSite());
		viewmenus.createMenu(importAction /* ,deleteAction */);
		GlobalEventDispatcher.addActivationListener(this, this);
		
	}
	
	private void addCustomBlocksPage(){
		BlockSelector cs = new BlockSelector();
		BlockDetailDisplay bdd = new BlockDetailDisplay();
		MasterDetailsPage page = new MasterDetailsPage(ctab, cs, bdd);
		CTabItem ct = new CTabItem(ctab, SWT.NONE);
		ct.setText(bdd.getTitle());
		ct.setControl(page);
		ct.setData(bdd);
		page.sash.setWeights(new int[] {
			30, 70
		});
		
		getSite().registerContextMenu(cs.getMgr(), cs.getCv().getViewerWidget());
		getSite().setSelectionProvider(cs.getCv().getViewerWidget());
	}
	
	private void makeActions(){
		importAction = new Action(Messages.CodeDetailView_importActionTitle) { //$NON-NLS-1$
				@Override
				public void run(){
					CTabItem it = ctab.getSelection();
					if (it != null) {
						ImporterPage top = importers.get(it.getText());
						if (top != null) {
							ImportDialog dlg = new ImportDialog(getViewSite().getShell(), top);
							dlg.create();
							dlg.setTitle(top.getTitle());
							dlg.setMessage(top.getDescription());
							dlg.getShell().setText(
								Messages.CodeDetailView_importerCaption); //$NON-NLS-1$
							if (dlg.open() == Dialog.OK) {
								top.run(false);
							}
						}
					}
					
				}
				
			};
		
	}
	
	private class ImportDialog extends TitleAreaDialog {
		ImporterPage importer;
		
		public ImportDialog(Shell parentShell, ImporterPage i){
			super(parentShell);
			importer = i;
		}
		
		@Override
		protected Control createDialogArea(Composite parent){
			return importer.createPage(parent);
		}
		
	}
	
	private void addPagesFor(String point){
		List<IConfigurationElement> list = Extensions.getExtensions(point);
		for (IConfigurationElement ce : list) {
			try {
				System.out.println(ce.getName());
				if ("Artikel".equals(ce.getName())) { //$NON-NLS-1$
					continue;
				}
				IDetailDisplay d =
					(IDetailDisplay) ce.createExecutableExtension("CodeDetailDisplay"); //$NON-NLS-1$
				CodeSelectorFactory cs =
					(CodeSelectorFactory) ce.createExecutableExtension("CodeSelectorFactory"); //$NON-NLS-1$
				String a = ce.getAttribute("ImporterClass"); //$NON-NLS-1$
				ImporterPage ip = null;
				if (a != null) {
					ip = (ImporterPage) ce.createExecutableExtension("ImporterClass"); //$NON-NLS-1$
					if (ip != null) {
						importers.put(d.getTitle(), ip);
					}
				}
				MasterDetailsPage page = new MasterDetailsPage(ctab, cs, d);
				CTabItem ct = new CTabItem(ctab, SWT.NONE);
				ct.setText(d.getTitle());
				ct.setControl(page);
				ct.setData(d);
				
			} catch (Exception ex) {
				ElexisStatus status =
					new ElexisStatus(ElexisStatus.WARNING, Hub.PLUGIN_ID, ElexisStatus.CODE_NONE,
						"Fehler beim Initialisieren von " + ce.getName(), ex,
						ElexisStatus.LOG_WARNINGS);
				StatusManager.getManager().handle(status, StatusManager.SHOW);
			}
		}
	}
	
	@Override
	public void setFocus(){
		if (ctab.getItemCount() > 0) {
			ctab.setFocus();
		}
	}
	
	/*
	 * public void selectionEvent(PersistentObject obj) { if (obj != null) { CTabItem top =
	 * ctab.getSelection(); if (top != null) { IDetailDisplay ids = (IDetailDisplay) top.getData();
	 * Class cl = ids.getElementClass(); String o1 = obj.getClass().getName(); String o2 =
	 * cl.getName(); if (o1.equals(o2)) { ids.display(obj); } } } }
	 */
	
	class MasterDetailsPage extends Composite {
		SashForm sash;
		CommonViewer cv;
		IDetailDisplay detailDisplay;
		
		ElexisEventListenerImpl eeli_div;
		
		MasterDetailsPage(Composite parent, CodeSelectorFactory master, IDetailDisplay detail){
			super(parent, SWT.NONE);
			eeli_div = new ElexisUiEventListenerImpl(detail.getElementClass()) {
				@Override
				public void runInUi(ElexisEvent ev){
					detailDisplay.display(ev.getObject());
				}
			};
			setLayout(new FillLayout());
			sash = new SashForm(this, SWT.NONE);
			cv = new CommonViewer();
			cv.create(master.createViewerConfigurer(cv), sash, SWT.NONE, getViewSite());
			// cv.getViewerWidget().addSelectionChangedListener(
			// GlobalEventDispatcher.getInstance().getDefaultListener());
			/* Composite page= */detail.createDisplay(sash, getViewSite());
			cv.getConfigurer().getContentProvider().startListening();
			detailDisplay = detail;
			ElexisEventDispatcher.getInstance().addListeners(eeli_div);
		}
		
		public void dispose(){
			ElexisEventDispatcher.getInstance().removeListeners(eeli_div);
		}
	}
	
	@Override
	public void dispose(){
		GlobalEventDispatcher.removeActivationListener(this, this);
		if ((ctab != null) && (!ctab.isDisposed())) {
			for (CTabItem ct : ctab.getItems()) {
				MasterDetailsPage page = (MasterDetailsPage) ct.getControl();
				// page.cv.getViewerWidget().removeSelectionChangedListener(
				// GlobalEventDispatcher.getInstance().getDefaultListener());
				page.cv.getConfigurer().getContentProvider().stopListening();
				page.dispose();
			}
		}
		
	}
	
	/** Vom ActivationListener */
	public void activation(boolean mode){
		CTabItem top = ctab.getSelection();
		if (top != null) {
			MasterDetailsPage page = (MasterDetailsPage) top.getControl();
			ViewerConfigurer vc = page.cv.getConfigurer();
			if (mode == true) {
				vc.getControlFieldProvider().setFocus();
			} else {
				vc.getControlFieldProvider().clearValues();
			}
		}
		
	}
	
	public void visible(boolean mode){
		
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
	
	public void doSave(IProgressMonitor monitor){ /* leer */
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
}
