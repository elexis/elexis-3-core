/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.Query;
import ch.elexis.core.icons.Images;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.text.ElexisText;
import ch.elexis.core.ui.util.Messages;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ControlFieldListener;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ControlFieldProvider;
import ch.rgw.tools.IFilter;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.Tree;

/**
 * Standardimplementation des ControlFieldProviders. Erzeugt ein Composite mit je einem
 * Texteingabefeld für jedes beim Konstruktor übergebene Feld. Feuert einen ChangedEvent, wenn
 * mindestens zwei Zeichen in eins der Felder eingegeben wurden.
 * 
 * @author Gerry
 */
public class DefaultControlFieldProvider implements ControlFieldProvider {
	protected String[] dbFields, fields, lastFiltered;
	protected ElexisText[] selectors;
	protected final ModListener ml;
	protected final SelListener sl;
	protected boolean modified;
	protected final List<ControlFieldListener> listeners;
	private final FormToolkit tk;
	protected CommonViewer myViewer;
	boolean bCeaseFire;
	
	public DefaultControlFieldProvider(final CommonViewer viewer, final String[] flds){
		fields = new String[flds.length];
		dbFields = new String[fields.length];
		myViewer = viewer;
		// this.fields=new String[fields.length];
		lastFiltered = new String[fields.length];
		for (int i = 0; i < flds.length; i++) {
			lastFiltered[i] = ""; //$NON-NLS-1$
			if (flds[i].indexOf('=') != -1) {
				String[] s = flds[i].split("="); //$NON-NLS-1$
				fields[i] = s[1];
				dbFields[i] = s[0];
			} else {
				fields[i] = dbFields[i] = flds[i];
			}
		}
		ml = new ModListener();
		sl = new SelListener();
		listeners = new LinkedList<ControlFieldListener>();
		tk = UiDesk.getToolkit();
	}
	
	public Composite createControl(final Composite parent){
		// Form form=tk.createForm(parent);
		// form.setLayoutData(SWTHelper.getFillGridData(1,true,1,false));
		// Composite ret=form.getBody();
		Composite ret = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		ret.setLayout(layout);
		ret.setBackground(parent.getBackground());
		
		ImageHyperlink hClr = tk.createImageHyperlink(ret, SWT.NONE); //$NON-NLS-1$
		hClr.setImage(Images.IMG_CLEAR.getImage());
		hClr.addHyperlinkListener(new HyperlinkAdapter() {
			
			@Override
			public void linkActivated(final HyperlinkEvent e){
				clearValues();
			}
			
		});
		hClr.setBackground(parent.getBackground());
		
		Composite inner = new Composite(ret, SWT.NONE);
		GridLayout lRet = new GridLayout(fields.length, true);
		inner.setLayout(lRet);
		inner.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		
		for (String l : fields) {
			Hyperlink hl = tk.createHyperlink(inner, l, SWT.NONE);
			hl.addHyperlinkListener(new HyperlinkAdapter() {
				
				@Override
				public void linkActivated(final HyperlinkEvent e){
					Hyperlink h = (Hyperlink) e.getSource();
					fireSortEvent(h.getText());
				}
				
			});
			hl.setBackground(parent.getBackground());
		}
		
		createSelectors(fields.length);
		for (int i = 0; i < selectors.length; i++) {
			selectors[i] = new ElexisText(tk.createText(inner, "", SWT.BORDER)); //$NON-NLS-1$
			selectors[i].addModifyListener(ml);
			selectors[i].addSelectionListener(sl);
			selectors[i].setToolTipText(Messages
				.getString("DefaultControlFieldProvider.enterFilter")); //$NON-NLS-1$
			selectors[i].setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			SWTHelper.setSelectOnFocus((Text) selectors[i].getWidget());
		}
		
		return ret;
	}
	
	protected void createSelectors(int length){
		selectors = new ElexisText[fields.length];
	}
	
	public void setFocus(){
		selectors[0].setFocus();
	}
	
	public boolean isModified(){
		return modified;
	}
	
	public String[] getDBFields(){
		return dbFields;
	}
	
	/**
	 * Reaktion auf Eingaben in die Filterfelder. Reagiert erst wenn mindestens zwei Zeichen
	 * eingegeben wurden oder das Feld geleert wurde.
	 * */
	class ModListener implements ModifyListener {
		public void modifyText(final ModifyEvent e){
			modified = true;
			Text t = (Text) e.getSource();
			String s = t.getText();
			if (!StringTool.leer.equals(s)) {
				if (s.length() == 1) {
					return;
				}
			}
			for (int i = 0; i < lastFiltered.length; i++) {
				lastFiltered[i] = selectors[i].getText();
			}
			fireChangedEvent();
			
		}
	}
	
	/**
	 * Reaktion auf ENTER den Filterfelder. Weist den Viewer an, eine Selektion vorzunehmen.
	 */
	class SelListener implements SelectionListener {
		public void widgetSelected(final SelectionEvent e){
			fireSelectedEvent();
		}
		
		public void widgetDefaultSelected(final SelectionEvent e){
			widgetSelected(e);
		}
	}
	
	public void fireChangedEvent(){
		if (!bCeaseFire) {
			UiDesk.getDisplay().syncExec(new Runnable() {
				public void run(){
					HashMap<String, String> hm = new HashMap<String, String>();
					for (int i = 0; i < fields.length; i++) {
						hm.put(fields[i], lastFiltered[i]);
					}
					for (ControlFieldListener lis : listeners) {
						lis.changed(hm);
					}
				}
			});
		}
	}
	
	public void fireSortEvent(final String text){
		if (!bCeaseFire) {
			for (ControlFieldListener ls : listeners) {
				ls.reorder(text);
			}
		}
	}
	
	public void fireSelectedEvent(){
		if (!bCeaseFire) {
			for (ControlFieldListener ls : listeners) {
				ls.selected();
			}
		}
	}
	
	public void addChangeListener(final ControlFieldListener cl){
		listeners.add(cl);
	}
	
	public void removeChangeListener(final ControlFieldListener cl){
		listeners.remove(cl);
	}
	
	public String[] getValues(){
		return lastFiltered;
	}
	
	/**
	 * Alle Eingabefelder löschen und einen "changeEvent" feuern". Aber nur, wenn die Felder nicht
	 * schon vorher leer waren.
	 */
	public void clearValues(){
		if (!isEmpty()) {
			bCeaseFire = true;
			for (int i = 0; i < selectors.length; i++) {
				selectors[i].setText(StringTool.leer);
				lastFiltered[i] = StringTool.leer;
			}
			modified = false;
			bCeaseFire = false;
			fireChangedEvent();
		}
	}
	
	public void setQuery(final Query q){
		boolean ch = false;
		for (int i = 0; i < fields.length; i++) {
			if (!lastFiltered[i].equals(StringTool.leer)) {
				q.add(dbFields[i], "LIKE", lastFiltered[i] + "%", true); //$NON-NLS-1$ //$NON-NLS-2$
				q.and();
				ch = true;
			}
		}
		if (ch) {
			q.insertTrue();
		}
		
	}
	
	public IFilter createFilter(){
		return new DefaultFilter();
	}
	
	private class DefaultFilter extends ViewerFilter implements IFilter {
		
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element){
			return select(element);
		}
		
		public boolean select(Object element){
			PersistentObject po = null;
			if (element instanceof Tree) {
				po = (PersistentObject) ((Tree) element).contents;
			} else if (element instanceof PersistentObject) {
				po = (PersistentObject) element;
			} else {
				return false;
			}
			if (po.isMatching(dbFields, PersistentObject.MATCH_START, lastFiltered)) {
				return true;
			} else {
				if (element instanceof Tree) {
					Tree p = ((Tree) element).getParent();
					if (p == null) {
						return false;
					}
					return select(p);
				} else {
					return false;
				}
			}
		}
		
	}
	
	public boolean isEmpty(){
		for (String s : lastFiltered) {
			if (!s.equals(StringTool.leer)) {
				return false;
			}
		}
		return true;
	}
	
	public void ceaseFire(final boolean bCeaseFire){
		this.bCeaseFire = bCeaseFire;
	}
	
	public CommonViewer getCommonViewer(){
		return this.myViewer;
	}
}