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

package ch.elexis.core.ui.util;

import java.util.ArrayList;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.locks.IUnlockable;

/**
 * A List of objects with UI (definable hyperlinks) Replaces DynamicListDisplay
 */
public class ListDisplay<T> extends Composite implements IUnlockable {
	public interface LDListener {
		public void hyperlinkActivated(String l);
		
		public String getLabel(Object o);
	}
	
	private IHyperlinkListener listen;
	protected List list;
	private final ArrayList<T> objects;
	private LDListener dlisten;
	private final Composite cLinks;
	private final FormToolkit tk = UiDesk.getToolkit();
	Transfer myTransfer = TextTransfer.getInstance();
	
	public void toClipBoard(boolean bAsString){
		Clipboard clip = new Clipboard(UiDesk.getDisplay());
		StringBuilder sb = new StringBuilder();
		for (String s : list.getItems()) {
			sb.append(s).append("\n");
		}
		clip.setContents(new Object[] {
			sb.toString()
		}, new Transfer[] {
			myTransfer
		});
	}
	
	public void setDLDListener(final LDListener dld){
		dlisten = dld;
	}
	
	public ListDisplay(final Composite parent, final int flags, final LDListener dld){
		super(parent, flags);
		objects = new ArrayList<T>();
		dlisten = dld;
		setLayout(new GridLayout(1, false));
		cLinks = new Composite(this, SWT.NONE);
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.spacing = 2;
		cLinks.setLayout(rl);
		
		list = new List(this, SWT.SINGLE | SWT.V_SCROLL);
		list.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tk.adapt(this);
	}
	
	public void addHyperlinks(final String... titles){
		if (listen == null) {
			listen = new HyperlinkAdapter() {
				@Override
				public void linkActivated(final HyperlinkEvent e){
					if (dlisten != null) {
						dlisten.hyperlinkActivated(e.getLabel());
					}
				}
			};
		}
		for (String title : titles) {
			Hyperlink mhl = tk.createHyperlink(cLinks, title, SWT.NONE);
			mhl.addHyperlinkListener(listen);
		}
	}
	
	@Override
	public void setUnlocked(boolean unlock) {
		enableHyperlinks(unlock);
	}

	private void enableHyperlinks(boolean bEnable){
		cLinks.setEnabled(bEnable);
		for (Control c : cLinks.getChildren()) {
			Hyperlink hl = (Hyperlink) c;
			if (bEnable) {
				hl.setForeground(UiDesk.getColor(UiDesk.COL_BLUE));
			} else {
				hl.setForeground(UiDesk.getColor(UiDesk.COL_GREY));
			}
			c.setEnabled(bEnable);
		}
		cLinks.redraw();
	}
	
	/**
	 * Ein Objekt der Liste hinzufügen
	 * 
	 * @param item
	 *            das Objekt. Muss getLabel() implementieren
	 */
	public void add(final T item){
		objects.add(item);
		list.add(dlisten.getLabel(item));
	}
	
	/**
	 * Ein Objekt aus der Liste entfernen
	 * 
	 * @param item
	 *            das Objekt
	 */
	public void remove(final T item){
		objects.remove(item);
		list.remove(dlisten.getLabel(item));
	}
	
	/** Die Liste leeren */
	public void clear(){
		list.removeAll();
		objects.clear();
	}
	
	/** Ein Kontextmenu für die Liste sezen */
	
	public void setMenu(final IAction... actions){
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager){
				fillContextMenu(manager, actions);
			}
		});
		Menu menu = menuMgr.createContextMenu(list);
		list.setMenu(menu);
	}
	
	protected void fillContextMenu(IMenuManager manager, IAction... actions){
		manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		for (IAction ac : actions) {
			if (ac == null) {
				manager.add(new Separator());
			} else {
				if (ac instanceof RestrictedAction) {
					((RestrictedAction) ac).reflectRight();
				}
				manager.add(ac);
			}
		}
	}
	
	/**
	 * Das momentan ausgewählte Objekt holen
	 */
	public T getSelection(){
		String[] obj = list.getSelection();
		if ((obj == null) || (obj.length == 0)) {
			return null;
		}
		for (T po : objects) {
			if (dlisten.getLabel(po).equals(obj[0])) {
				return po;
			}
		}
		return null;
		
	}
	
	public void setSelection(final T object){
		if (object == null) {
			list.deselectAll();
		} else {
			for (T t : objects) {
				if (t.equals(object)) {
					list.setSelection(new String[] {
						dlisten.getLabel(t)
					});
					break;
				}
			}
		}
	}
	
	public void setSelection(final int index){
		list.setSelection(index);
	}
	
	public java.util.List<T> getAll(){
		return objects;
	}
	
	public void addListener(final SelectionListener l){
		list.addSelectionListener(l);
	}
	
	public void removeListener(final SelectionListener l){
		list.removeSelectionListener(l);
	}
	
	/**
	 *
	 * @param eventType
	 * @param listener
	 * @since 3.6
	 */
	public void addListenerToSelectionList(final int eventType, final Listener listener) {
		list.addListener(eventType, listener);
	}
	
}
