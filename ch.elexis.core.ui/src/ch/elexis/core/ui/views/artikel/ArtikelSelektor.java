/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.views.artikel;

import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.commands.EditEigenartikelUi;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory;
import ch.elexis.data.Artikel;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.ExHandler;

public class ArtikelSelektor extends ViewPart implements ISaveablePart2 {
	public static final String ID = "ch.elexis.ArtikelSelektor"; //$NON-NLS-1$
	CTabFolder ctab;
	TableViewer tv;
	
	@Override
	public void createPartControl(final Composite parent){
		parent.setLayout(new GridLayout());
		ctab = new CTabFolder(parent, SWT.NONE);
		ctab.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		java.util.List<IConfigurationElement> list =
			Extensions.getExtensions(ExtensionPointConstantsUi.VERRECHNUNGSCODE); //$NON-NLS-1$
		ctab.addSelectionListener(new TabSelectionListener());
		for (IConfigurationElement ice : list) {
			if ("Artikel".equals(ice.getName())) { //$NON-NLS-1$
				try {
					CodeSelectorFactory cs =
						(CodeSelectorFactory) ice.createExecutableExtension(ExtensionPointConstantsUi.VERRECHNUNGSCODE_CSF);
					CTabItem ci = new CTabItem(ctab, SWT.NONE);
					ci.setText(cs.getCodeSystemName());
					ci.setData("csf", cs); //$NON-NLS-1$
				} catch (Exception ex) {
					ExHandler.handle(ex);
				}
			}
		}
		CTabItem ci = new CTabItem(ctab, SWT.NONE);
		Composite c = new Composite(ctab, SWT.NONE);
		c.setLayout(new GridLayout());
		ci.setControl(c);
		ci.setText("Lagerartikel"); //$NON-NLS-1$
		Table table = new Table(c, SWT.SIMPLE | SWT.V_SCROLL);
		table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tv = new TableViewer(table);
		tv.setContentProvider(new IStructuredContentProvider() {
			
			public Object[] getElements(final Object inputElement){
				List<Artikel> lLager = Artikel.getLagerartikel();
				if (lLager != null) {
					return lLager.toArray();
				} else {
					return new Object[0];
				}
			}
			
			public void dispose(){}
			
			public void inputChanged(final Viewer viewer, final Object oldInput,
				final Object newInput){}
			
		});
		// tv.setLabelProvider(new LagerLabelProvider());
		tv.setLabelProvider(new ArtikelLabelProvider());
		tv.addDragSupport(DND.DROP_COPY, new Transfer[] {
			TextTransfer.getInstance()
		}, new DragSourceAdapter() {
			@Override
			public void dragStart(final DragSourceEvent event){
				IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
				if ((sel == null) || sel.isEmpty()) {
					event.doit = false;
				} else {
					Object s = sel.getFirstElement();
					if (s instanceof PersistentObject) {
						PersistentObject po = (PersistentObject) s;
						event.doit = po.isDragOK();
					} else {
						event.doit = false;
					}
				}
			}
			
			@Override
			public void dragSetData(final DragSourceEvent event){
				IStructuredSelection isel = (IStructuredSelection) tv.getSelection();
				StringBuilder sb = new StringBuilder();
				Object[] sel = isel.toArray();
				for (Object s : sel) {
					if (s instanceof PersistentObject) {
						sb.append(((PersistentObject) s).storeToString()).append(","); //$NON-NLS-1$
					} else {
						sb.append("error").append(","); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				event.data = sb.toString().replace(",$", ""); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
		});
		tv.addDoubleClickListener(new IDoubleClickListener() {
			
			public void doubleClick(final DoubleClickEvent event){
				IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
				if ((sel != null) && (!sel.isEmpty())) {
					Artikel art = (Artikel) sel.getFirstElement();
					EditEigenartikelUi.executeWithParams(art);
				}
			}
			
		});
		tv.setInput(this);
	}
	
	@Override
	public void setFocus(){
		// TODO Automatisch erstellter Methoden-Stub
		
	}
	
	@Override
	public void dispose(){
		
	}
	
	// replaced by ArtikelLabelProvider
	
	class LagerLabelProvider extends DefaultLabelProvider implements ITableLabelProvider {
		
		@Override
		public Image getColumnImage(final Object element, final int columnIndex){
			if (element instanceof Artikel) {
				return null;
			} else {
				return Images.IMG_ACHTUNG.getImage();
			}
		}
		
		@Override
		public String getColumnText(final Object element, final int columnIndex){
			if (element instanceof Artikel) {
				Artikel art = (Artikel) element;
				String ret = art.getInternalName();
				if (art.isLagerartikel()) {
					ret += " (" + Integer.toString(art.getTotalCount()) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				return ret;
			}
			return super.getColumnText(element, columnIndex);
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
	
	public void doSave(final IProgressMonitor monitor){ /* leer */}
	
	public void doSaveAs(){ /* leer */}
	
	public boolean isDirty(){
		return true;
	}
	
	public boolean isSaveAsAllowed(){
		return false;
	}
	
	public boolean isSaveOnCloseNeeded(){
		return true;
	}
	
	private class TabSelectionListener extends SelectionAdapter {
		
		@Override
		public void widgetSelected(SelectionEvent e){
			CTabItem top = ctab.getSelection();
			if (top != null) {
				if (top.getControl() == null) {
					CommonViewer cv = new CommonViewer();
					CodeSelectorFactory cs = (CodeSelectorFactory) top.getData("csf"); //$NON-NLS-1$
					ViewerConfigurer vc = cs.createViewerConfigurer(cv);
					Composite c = new Composite(ctab, SWT.NONE);
					c.setLayout(new GridLayout());
					cv.create(vc, c, SWT.V_SCROLL, getViewSite());
					top.setControl(c);
					top.setData(cv);
					
					cv.addDoubleClickListener(new CommonViewer.DoubleClickListener() {
						
						public void doubleClicked(final PersistentObject obj, final CommonViewer cv){
							EditEigenartikelUi.executeWithParams(obj);
						}
					});
					vc.getContentProvider().startListening();
				}
			}
			
		}
		
	}
}
