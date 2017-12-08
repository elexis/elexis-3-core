/*******************************************************************************
 * Copyright (c) 2008-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.selectors;

import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.util.SWTHelper;

/**
 * An Element consisting of a label and a control that is able to link itself to the database and
 * act on user input. An activeControl can store arbitrary field=value pairs and get and set methods
 * for the display can be called independently of the display Thread.
 * 
 * @author Gerry
 * 
 */
public abstract class ActiveControl extends Composite {
	private Label lbl;
	protected Control ctl;
	protected Composite controllers;
	protected String textContents = "";
	private String labelContents = "";
	private LinkedList<ActiveControlListener> listeners;
	private HashMap<String, Object> properties = new HashMap<String, Object>();
	private int flags;
	
	/** Constant to hide the label (Default: Label is visible) */
	public static final int HIDE_LABEL = 0x0001;
	/** Display label and control lined up horizontally (default: vertically) */
	public static final int DISPLAY_HORIZONTAL = 0x0002;
	/** Label reacts on mouse clicks (and informs listeners) */
	public static final int LABEL_IS_HYPERLINK = 0x0004;
	/** Contents can not be edited by user */
	public static final int READONLY = 0x0008;
	/** Field links itself to the database */
	public static final int LINK_TO_DB = 0x0010;
	
	/** Displayed label of the field */
	public static final String PROP_DISPLAYNAME = "displayName"; //$NON-NLS-1$
	/** Internal name od the field in the PersistentObject */
	public static final String PROP_FIELDNAME = "fieldName"; //$NON-NLS-1$
	/** Name in the Hashtable if fieldName denotes a hash field */
	public static final String PROP_HASHNAME = "hashName"; //$NON-NLS-1$
	/** Message to display if the field contents is invalid */
	public static final String PROP_ERRMSG = "invalidContents"; //$NON-NLS-1$
	/** Pattern for valid contents */
	public static final String PROP_VALID_PATTERN = "validPattern"; //$NON-NLS-1$
	
	/**
	 * create a new field
	 * 
	 * @param parent
	 *            the parent Composite
	 * @param show
	 *            ho to display the label
	 */
	public ActiveControl(Composite parent, int displayBits, String displayName){
		super(parent, SWT.NONE);
		if ((displayBits & (DISPLAY_HORIZONTAL | HIDE_LABEL)) == DISPLAY_HORIZONTAL) {
			setLayout(new GridLayout(3, false));
		} else {
			setLayout(new GridLayout(2, false));
		}
		flags = displayBits;
		labelContents = displayName == null ? "" : displayName;
		if ((displayBits & HIDE_LABEL) == 0) {
			lbl = new Label(this, SWT.NONE);
			lbl.setText(displayName);
			
			setData(PROP_DISPLAYNAME, displayName);
			controllers = new Composite(this, SWT.NONE);
			// controllers.setBackground(Desk.getColor(Desk.COL_GREEN));
			GridData gd = new GridData(SWT.RIGHT, SWT.BOTTOM, false, false);
			Point size = lbl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			gd.heightHint = size.y;
			controllers.setLayoutData(gd);
			controllers.setLayout(new FillLayout());
			lbl.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		}
	}
	
	public void addListener(ActiveControlListener listen){
		if (listeners == null) {
			listeners = new LinkedList<ActiveControlListener>();
		}
		listeners.add(listen);
	}
	
	public void removeSelectorListener(ActiveControlListener listen){
		if (listeners != null) {
			listeners.remove(listen);
		}
	}
	
	public boolean isValid(){
		String validPattern = (String) properties.get(PROP_VALID_PATTERN);
		if (validPattern != null) {
			if (!getText().matches(validPattern)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isReadonly(){
		return (flags & READONLY) == READONLY;
	}
	
	public void fireChangedEvent(){
		if (!isValid()) {
			if (listeners != null) {
				for (ActiveControlListener sl : listeners) {
					sl.invalidContents(this);
				}
				
			}
		}
		if (listeners != null) {
			for (ActiveControlListener sl : listeners) {
				sl.contentsChanged(this);
			}
		}
	}
	
	protected abstract void push();
	
	public void setText(String text){
		textContents = text;
		push();
	}
	
	public String getText(){
		return textContents;
	}
	
	public void clear(){
		textContents = "";
		push();
	}
	
	public String getLabelText(){
		return labelContents;
		
	}
	
	public void setLabelText(final String text){
		UiDesk.asyncExec(new Runnable() {
			@Override
			public void run(){
				lbl.setText(text);
			}
			
		});
	}
	
	protected void setControl(Control control){
		ctl = control;
		ctl.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		ctl.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e){
				if (isValid()) {
					
				}
			}
		});
	}
	
	/*
	 * public Label getLbl() { return lbl; }
	 * 
	 * public void setLabel(Label lbl) { this.lbl = lbl; }
	 */
	public Control getCtl(){
		return ctl;
	}
	
	public String getDisplayName(){
		return (String) getData(PROP_DISPLAYNAME);
	}
	
	public void setDisplayName(String displayName){
		setData(PROP_DISPLAYNAME, displayName);
		lbl.setText(displayName);
	}
	
	public void setEnabled(boolean bEnable){
		if (ctl != null) {
			ctl.setEnabled(bEnable);
		}
	}
	
	public Composite getControllerComposite(){
		return controllers;
	}
	
	@Override
	public void setData(String name, Object value){
		properties.put(name, value);
	}
	
	@Override
	public Object getData(String name){
		return properties.get(name);
	}
	
	public String getProperty(String name){
		return (String) getData(name);
	}
	
	public void setValidPattern(String pattern, String errmsg){
		setData(PROP_VALID_PATTERN, pattern);
		setData(PROP_ERRMSG, errmsg);
	}
	
	public String getErrMsg(){
		return (String) properties.get(PROP_ERRMSG);
	}
}
