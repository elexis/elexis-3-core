/*******************************************************************************
 * Copyright (c) 2006-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 	  M. Descher - extracted Eigenartikel to ch.elexis.eigenartikel
 * 
 *******************************************************************************/

package ch.elexis.core.ui.views.artikel;

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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListenerImpl;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.GlobalEventDispatcher.IActivationListener;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.util.ImporterPage;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.views.IDetailDisplay;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory;
import ch.rgw.tools.ExHandler;

public class ArtikelView extends ViewPart implements IActivationListener, ISaveablePart2 {
	private static final String KEY_CE = "ce"; //$NON-NLS-1$
	private static final String KEY_DETAIL = "detail"; //$NON-NLS-1$
	public static final String ID = "ch.elexis.artikelview"; //$NON-NLS-1$
	private CTabFolder ctab;
	private IAction importAction /* ,deleteAction */;
	private ViewMenus viewmenus;
	private Hashtable<String, ImporterPage> importers;
	
	@Override
	public void createPartControl(Composite parent){
		parent.setLayout(new FillLayout());
		ctab = new CTabFolder(parent, SWT.NONE);
		importers = new Hashtable<String, ImporterPage>();
		addPagesFor("ch.elexis.Verrechnungscode"); //$NON-NLS-1$
		if (ctab.getItemCount() > 0) {
			ctab.setSelection(0);
			
		}
		ctab.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				CTabItem top = ctab.getSelection();
				if (top != null) {
					String t = top.getText();
					
					MasterDetailsPage page = (MasterDetailsPage) top.getControl();
					if (page == null) {
						try {
							IDetailDisplay det = (IDetailDisplay) top.getData(KEY_DETAIL);
							IConfigurationElement ce = (IConfigurationElement) top.getData(KEY_CE);
							CodeSelectorFactory cs =
								(CodeSelectorFactory) ce
									.createExecutableExtension("CodeSelectorFactory"); //$NON-NLS-1$
							String a = ce.getAttribute("ImporterClass"); //$NON-NLS-1$
							ImporterPage ip = null;
							if (a != null) {
								ip = (ImporterPage) ce.createExecutableExtension("ImporterClass"); //$NON-NLS-1$
								if (ip != null) {
									importers.put(det.getTitle(), ip);
								}
							}
							
							page = new MasterDetailsPage(ctab, cs, det);
							top.setControl(page);
							top.setData(det);
						} catch (Exception ex) {
							ExHandler.handle(ex);
						}
						
					}
					importAction.setEnabled(importers.get(t) != null);
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
	
	private void makeActions(){
		importAction = new Action(Messages.ArtikelView_importAction) {
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
						dlg.getShell().setText(Messages.ArtikelView_importCaption);
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
		
		@Override
		protected void okPressed(){
			importer.collect();
			super.okPressed();
		}
		
	}
	
	private void addPagesFor(String point){
		List<IConfigurationElement> list = Extensions.getExtensions(point);
		IDetailDisplay d;
		boolean headerDone = false;
		for (int i = 0; i < list.size(); i++) {
			IConfigurationElement ce = list.get(i);
			try {
				if (!"Artikel".equals(ce.getName()))
					continue;
				// The first page initializes the screen
				if (!headerDone) {
					d = (IDetailDisplay) ce.createExecutableExtension("CodeDetailDisplay");
					String a = ce.getAttribute("ImporterClass"); //$NON-NLS-1$
					ImporterPage ip = null;
					if (a != null) {
						ip = (ImporterPage) ce.createExecutableExtension("ImporterClass"); //$NON-NLS-1$
						if (ip != null) {
							importers.put(d.getTitle(), ip);
						}
					}
					CodeSelectorFactory csf =
						(CodeSelectorFactory) ce.createExecutableExtension("CodeSelectorFactory");
					MasterDetailsPage page = new MasterDetailsPage(ctab, csf, d);
					CTabItem ct = new CTabItem(ctab, SWT.None);
					ct.setText(d.getTitle());
					ct.setControl(page);
					ct.setData(d);
					page.sash.setWeights(new int[] {
						30, 70
					});
					headerDone = true;
					continue;
				}
				d = (IDetailDisplay) ce.createExecutableExtension("CodeDetailDisplay");
				CTabItem ct = new CTabItem(ctab, SWT.NONE);
				ct.setText(d.getTitle());
				ct.setData(KEY_CE, ce);
				ct.setData(KEY_DETAIL, d);
				
			} catch (Exception ex) {
				MessageBox mb = new MessageBox(getViewSite().getShell(), SWT.ICON_ERROR | SWT.OK);
				mb.setText(Messages.ArtikelView_errorCaption);
				mb.setMessage(Messages.ArtikelView_errorText + ce.getName() + ":\n" //$NON-NLS-1$
					+ ex.getLocalizedMessage());
				mb.open();
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
	 * public void selectionEvent(PersistentObject obj){ CTabItem top = ctab.getSelection(); if (top
	 * != null) { IDetailDisplay ids = (IDetailDisplay) top.getData(); Class cl =
	 * ids.getElementClass(); String o1 = obj.getClass().getName(); String o2 = cl.getName(); if
	 * (o1.equals(o2)) { ids.display(obj); } }
	 * 
	 * }
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
				// ((MasterDetailsPage) ct.getControl()).cv.getViewerWidget()
				// .removeSelectionChangedListener(
				// GlobalEventDispatcher.getInstance()
				// .getDefaultListener());
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
