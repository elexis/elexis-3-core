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

package ch.elexis.core.ui.util.viewers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;

import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.PersistentObjectDragSource;
import ch.elexis.core.ui.util.PersistentObjectDragSource.ISelectionRenderer;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ControlFieldProvider;
import ch.rgw.tools.Tree;

/**
 * Basis des Viewer-Systems. Ein Viewer zeigt eine Liste von Objekten einer bestimmten
 * PersistentObject -Unterklasse an und ermöglicht das Filtern der Anzeige sowie das Erstellen neuer
 * Objekte dieser Klasse. Der CommonViewer stellt nur die Oberfläche bereit (oben ein Feld zum
 * Filtern, in der Mitte die Liste und unten ein Button zum Erstellen eines neuen Objekts). Die
 * Funktionalität muss von einem ViewerConfigurer bereitgestellt werden. Dieser ist wiederum nur ein
 * Container zur Breitstellung verschiedener Provider. NB: CommonViewer ist eigentlich ein
 * Antipattern (nämlich ein Golden Hammer). Er verkürzt Entwicklungszeit, aber auf Kosten der
 * Flexibilität und der optimalen Anpassung Wann immer Zeit und Ressourcen genügen, sollte einer
 * individuellen Lösung der Vorzug gegeben werden.
 * 
 * @see ViewerConfigurer
 * @author Gerry
 */
public class CommonViewer implements ISelectionChangedListener, IDoubleClickListener {
	
	protected ViewerConfigurer vc;
	protected StructuredViewer viewer;
	protected Button bNew;
	private IAction createObjectAction;
	private Composite parent;
	
	public enum Message {
		update, empty, notempty, update_keeplabels
	}
	
	private HashSet<DoubleClickListener> dlListeners;
	private MenuManager mgr;
	private Composite composite;
	
	public Composite getParent(){
		return parent;
	}
	
	public CommonViewer(){
		
	}
	
	public void setObjectCreateAction(IViewSite site, IAction action){
		site.getActionBars().getToolBarManager().add(action);
		action.setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
		createObjectAction = action;
	}
	
	/**
	 * Den Viewer erstellen
	 * 
	 * @param c
	 *            ViewerConfigurer, der die Funktionalität bereitstellt. Alle Felder des Configurers
	 *            müssen vor Aufruf von create() gültig gesetzt sein.
	 * @param parent
	 *            Parent.Komponente
	 * @param style
	 *            SWT-Stil für das umgebende Composite
	 * @param input
	 *            Input Objekt für den Viewer
	 */
	public void create(ViewerConfigurer c, Composite parent, int style, Object input){
		vc = c;
		this.parent = parent;
		Composite ret = new Composite(parent, style);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		ret.setLayout(layout);
		
		if (parent.getLayout() instanceof GridLayout) {
			GridData gd =
				new GridData(GridData.GRAB_VERTICAL | GridData.FILL_VERTICAL
					| GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
			ret.setLayoutData(gd);
		}
		ControlFieldProvider cfp = vc.getControlFieldProvider();
		if (cfp != null) {
			Composite ctlf = vc.getControlFieldProvider().createControl(ret);
			ctlf.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		}
		viewer = vc.getWidgetProvider().createViewer(ret);
		GridData gdView =
			new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL
				| GridData.GRAB_VERTICAL | GridData.FILL_VERTICAL);
		gdView.verticalAlignment = SWT.FILL;
		viewer.setUseHashlookup(true);
		viewer.getControl().setLayoutData(gdView);
		viewer.setContentProvider(vc.getContentProvider());
		viewer.setLabelProvider(vc.getLabelProvider());
		viewer.addSelectionChangedListener(this);
		bNew = vc.getButtonProvider().createButton(ret);
		if (bNew != null) {
			GridData gdNew = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
			bNew.setLayoutData(gdNew);
			if (vc.getButtonProvider().isAlwaysEnabled() == false) {
				bNew.setEnabled(false);
			}
		}
		/*
		 * 3 viewer.getControl().addMouseListener(new MouseAdapter(){ public void
		 * mouseDoubleClick(MouseEvent e) { log.log("Doppelklick",Log.DEBUGMSG);
		 * ctl.doubleClicked(getSelection()); }});
		 */
		/*
		 * viewer.addDragSupport(DND.DROP_COPY,new Transfer[] {TextTransfer.getInstance()},
		 */
		new PersistentObjectDragSource(viewer.getControl(), new ISelectionRenderer() {
			
			public List<PersistentObject> getSelection(){
				Object[] sel = CommonViewer.this.getSelection();
				ArrayList<PersistentObject> ret = new ArrayList<PersistentObject>(sel.length);
				for (Object o : sel) {
					if (o instanceof PersistentObject) {
						ret.add((PersistentObject) o);
					} else if (o instanceof Tree<?>) {
						Object b = ((Tree<?>) o).contents;
						if (b instanceof PersistentObject) {
							ret.add((PersistentObject) b);
						}
					}
				}
				return ret;
			}
			
		});
		if (mgr != null) {
			viewer.getControl().setMenu(mgr.createContextMenu(viewer.getControl()));
		}
		vc.getContentProvider().init();
		viewer.setInput(input);
		viewer.getControl().pack();
		composite = ret;
	}
	
	public Composite getComposite(){
		return composite;
	}
	
	/**
	 * Die aktuelle Auswahl des Viewers liefern
	 * 
	 * @return null oder ein Array mit den selektierten Objekten-
	 */
	public Object[] getSelection(){
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		if (sel != null) {
			return sel.toArray();
		}
		return null;
		
	}
	
	/**
	 * Das selektierte Element des Viewers einstellen
	 * 
	 * @param o
	 *            Das Element
	 */
	public void setSelection(Object o, boolean fireEvents){
		if (fireEvents == false) {
			viewer.removeSelectionChangedListener(this);
			viewer.setSelection(new StructuredSelection(o), true);
			viewer.addSelectionChangedListener(this);
		} else {
			viewer.setSelection(new StructuredSelection(o), true);
		}
		
	}
	
	/**
	 * Den darunterliegenden JFace-Viewer liefern
	 */
	public StructuredViewer getViewerWidget(){
		return viewer;
	}
	
	public ViewerConfigurer getConfigurer(){
		return vc;
	}
	
	/**
	 * den Viewer über eine Änderung benachrichtigen
	 * 
	 * @param m
	 *            eine Message: update: der Viewer muss neu eingelesen werden empty: Die Auswahl ist
	 *            leer. notempty: Die Auswahl ist nicht (mehr) leer.
	 */
	public void notify(final Message m){
		if (viewer.getControl().isDisposed()) {
			return;
		}
		UiDesk.getDisplay().asyncExec(new Runnable() {
			public void run(){
				switch (m) {
				case update:
					if (!viewer.getControl().isDisposed()) {
						viewer.refresh(true);
					}
					break;
				case update_keeplabels:
					if (!viewer.getControl().isDisposed()) {
						viewer.refresh(false);
					}
					break;
				case empty:
					if (bNew != null) {
						if (vc.getButtonProvider().isAlwaysEnabled() == false) {
							bNew.setEnabled(false);
						}
					}
					if (createObjectAction != null) {
						createObjectAction.setEnabled(false);
					}
					break;
				case notempty:
					if (bNew != null) {
						bNew.setEnabled(true);
					}
					if (createObjectAction != null) {
						createObjectAction.setEnabled(true);
					}
					break;
				}
			}
		});
		
	}
	
	public void selectionChanged(SelectionChangedEvent event){
		Object[] sel = getSelection();
		if (sel != null && sel.length != 0) {
			if (sel[0] instanceof Tree<?>) {
				sel[0] = ((Tree<?>) sel[0]).contents;
			}
			if (sel[0] instanceof PersistentObject) {
				ElexisEventDispatcher.fireSelectionEvent((PersistentObject) sel[0]);
			}
		}
	}
	
	public void dispose(){
		viewer.removeSelectionChangedListener(this);
	}
	
	public void addDoubleClickListener(DoubleClickListener dl){
		if (dlListeners == null) {
			dlListeners = new HashSet<DoubleClickListener>();
			getViewerWidget().addDoubleClickListener(this);
		}
		dlListeners.add(dl);
	}
	
	public void removeDoubleClickListener(DoubleClickListener dl){
		if (dlListeners == null) {
			return;
		}
		dlListeners.remove(dl);
		if (dlListeners.isEmpty()) {
			getViewerWidget().removeDoubleClickListener(this);
			dlListeners = null;
		}
	}
	
	/**
	 * Kontextmenu an den unterliegenden Viewer binden. Falls dieser zum Zeitpunkt des Aufrufs
	 * dieser Methode noch nicht existiert, wird das Einbinden verzögert.
	 * 
	 * @param mgr
	 *            ein fertig konfigurierter jface-MenuManager
	 */
	public void setContextMenu(MenuManager mgr){
		this.mgr = mgr;
		if (viewer != null) {
			viewer.getControl().setMenu(mgr.createContextMenu(viewer.getControl()));
		}
	}
	
	public Button getButton(){
		return bNew;
	}
	
	public interface DoubleClickListener {
		public void doubleClicked(PersistentObject obj, CommonViewer cv);
	}
	
	public void doubleClick(DoubleClickEvent event){
		if (dlListeners != null) {
			Iterator<DoubleClickListener> it = dlListeners.iterator();
			while (it.hasNext()) {
				DoubleClickListener dl = it.next();
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if ((sel != null) && (!sel.isEmpty())) {
					Object element = sel.getFirstElement();
					PersistentObject po;
					if (element instanceof Tree<?>) {
						po = (PersistentObject) ((Tree<?>) element).contents;
					} else {
						po = (PersistentObject) element;
					}
					dl.doubleClicked(po, this);
				}
			}
			
		}
		
	}
	
	public MenuManager getMgr(){
		return mgr;
	}
}
