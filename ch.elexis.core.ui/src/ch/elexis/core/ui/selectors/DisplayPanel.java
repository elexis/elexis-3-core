/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.selectors;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.ColumnLayout;

import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.LimitSizeStack;

/**
 * A Panel to display ActiveControls as views to fields of a single PersistentObject
 * 
 * @author gerry
 * 
 */
public class DisplayPanel extends Composite implements ActiveControlListener {
	private boolean bCeaseFire, bExclusive, bAutosave;
	private LinkedList<ActiveControlListener> listeners = new LinkedList<ActiveControlListener>();
	private LimitSizeStack<TraceElement> undoList = new LimitSizeStack<TraceElement>(50);
	private Composite cFields;
	private ToolBarManager tActions;
	private ToolBar tb;
	private IAction aClr;
	private PersistentObject actObject;
	
	public DisplayPanel(Composite parent, FieldDescriptor<? extends PersistentObject>[] fields,
		int minCols, int maxCols, IAction... actions){
		super(parent, SWT.NONE);
		bAutosave = false;
		setBackground(parent.getBackground());
		FormLayout layout = new FormLayout();
		layout.marginLeft = 0;
		layout.marginRight = 0;
		setLayout(layout);
		tActions = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL | SWT.WRAP);
		
		aClr = new Action(Messages.SelectorPanel_clearFields) {
			{
				setImageDescriptor(Images.IMG_CLEAR.getImageDescriptor());
			}
			
			@Override
			public void run(){
				// clearValues();
			}
		};
		tActions.add(aClr);
		for (IAction ac : actions) {
			if (ac != null) {
				tActions.add(ac);
			} else {
				tActions.add(new Separator());
			}
		}
		tb = tActions.createControl(this);
		FormData fdActions = new FormData();
		fdActions.top = new FormAttachment(0, 0);
		fdActions.right = new FormAttachment(100, 0);
		tb.setLayoutData(fdActions);
		cFields = new Composite(this, SWT.NONE);
		FormData fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.top = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		cFields.setLayoutData(fd);
		
		ColumnLayout cl = new ColumnLayout();
		cl.minNumColumns = minCols > 0 ? minCols : 1;
		cl.maxNumColumns = maxCols > minCols ? maxCols : minCols + 2;
		cFields.setLayout(cl);
		
		for (FieldDescriptor<? extends PersistentObject> field : fields) {
			ActiveControl ac = null;
			switch (field.getFieldType()) {
			case HYPERLINK:
			case STRING:
				ac = new TextField(cFields, 0, field.getLabel());
				break;
			case CURRENCY:
				ac = new MoneyField(cFields, 0, field.getLabel());
				break;
			case DATE:
				ac = new DateField(cFields, 0, field.getLabel());
				break;
			
			case COMBO:
				ac = new ComboField(cFields, 0, field.getLabel(), (String[]) field.getExtension());
				break;
			case INT:
				ac = new IntegerField(cFields, 0, field.getLabel());
			}
			ac.setData(ActiveControl.PROP_FIELDNAME, field.getFieldname());
			ac.setData(ActiveControl.PROP_HASHNAME, field.getHashname());
			addField(ac);
			pack();
		}
	}
	
	/**
	 * Set the Object to display
	 * 
	 * @param po
	 *            a PersistentObject that must have all fields defined, that are referenced by
	 *            ActiveControls of this Panel
	 */
	public void setObject(PersistentObject po){
		actObject = po;
		List<ActiveControl> ctls = getControls();
		for (ActiveControl ac : ctls) {
			String field = ac.getProperty(ActiveControl.PROP_FIELDNAME);
			ac.setText(po.get(field));
		}
		layout();
	}
	
	/**
	 * Set autosave behaviour
	 * 
	 * @param doSave
	 *            if true: changed fields are written back to the database. false: No weiting occurs
	 */
	public void setAutosave(boolean doSave){
		bAutosave = doSave;
	}
	
	/**
	 * Add a field to the panel
	 * 
	 * @param ac
	 */
	public void addField(ActiveControl ac){
		ac.addListener(this);
	}
	
	/**
	 * Add a number of fields to the Panel
	 * 
	 * @param activeControls
	 */
	public void addFields(ActiveControl... activeControls){
		ActiveControl last = null;
		for (ActiveControl ac : activeControls) {
			ac.addListener(this);
			last = ac;
		}
		if (tb.isReparentable() && last != null) {
			Composite ctl = last.getControllerComposite();
			ctl.setLayout(new FormLayout());
			tb.setParent(ctl);
			// last.setLayout(new FormLayout());
		}
		layout();
	}
	
	public void contentsChanged(ActiveControl ac){
		if (ac != null) {
			new TraceElement(ac);
		}
		if (!bCeaseFire) {
			bCeaseFire = true;
			if (bAutosave) {
				if (actObject != null) {
					String field = ac.getProperty(ActiveControl.PROP_FIELDNAME);
					actObject.set(field, ac.getText());
				}
			}
			for (ActiveControlListener lis : listeners) {
				lis.contentsChanged(ac);
			}
			bCeaseFire = false;
		}
	}
	
	public void invalidContents(ActiveControl field){
		aClr.setImageDescriptor(Images.IMG_ACHTUNG.getImageDescriptor());
		aClr.setToolTipText((String) field.getData(ActiveControl.PROP_ERRMSG));
		
	}
	
	/**
	 * Add a listener to the list of listeners that will be notified, if one of the fields has been
	 * changed
	 * 
	 * @param l
	 */
	public void addSelectorListener(ActiveControlListener l){
		listeners.add(l);
	}
	
	/**
	 * Remove a listener from the list of SelectorListeners
	 * 
	 * @param l
	 */
	public void removeSelectorListener(ActiveControlListener l){
		listeners.remove(l);
	}
	
	/**
	 * From ActiveControlListener: Notify that the user clicked the label of a field This will in
	 * turn notify the SelectorListeners attached to this panel
	 */
	
	public void titleClicked(final ActiveControl field){
		if (!bCeaseFire) {
			bCeaseFire = true;
			for (ActiveControlListener lis : listeners) {
				lis.titleClicked(field);
			}
			bCeaseFire = true;
		}
		
	}
	
	/**
	 * Return all ActiveControls attached to this panel
	 * 
	 * @return al List that might be empty but is never null
	 */
	public List<ActiveControl> getControls(){
		LinkedList<ActiveControl> ret = new LinkedList<ActiveControl>();
		for (Control c : cFields.getChildren()) {
			if (c instanceof ActiveControl) {
				ActiveControl ac = (ActiveControl) c;
				ret.add(ac);
			}
		}
		return ret;
	}
	
	private class TraceElement {
		ActiveControl control;
		String value;
		
		TraceElement(ActiveControl ac){
			control = ac;
			value = ac.getText();
			undoList.push(this);
		}
	}
	
}
