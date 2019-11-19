/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Sgam.informatics
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.preferences.inputs;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ui.util.SWTHelper;

import ch.rgw.tools.StringTool;
public class MultilineFieldEditor extends StringFieldEditor {
	Text textField;
	int numOfLines;
	int flags = SWT.BORDER;
	boolean isStringList;
	
	public MultilineFieldEditor(String name, String labelText, Composite parent){
		// super(name, labelText, UNLIMITED, parent);
		this(name, labelText, 3, 0, false, parent);
	}
	
	public MultilineFieldEditor(String name, String labelText, int numLines, int flags,
		boolean asStringList, Composite parent){
		// super(name, labelText, UNLIMITED, parent);
		numOfLines = numLines;
		this.flags = SWT.BORDER | flags;
		isStringList = asStringList;
		init(name, labelText);
		createControl(parent);
		GridData gd = (GridData) textField.getLayoutData();
		GC gc = new GC(textField);
		Point pt = gc.textExtent("X"); //$NON-NLS-1$
		gc.dispose();
		gd.minimumHeight = pt.y * numLines;
		gd.heightHint = pt.y * numLines;
		gd.grabExcessHorizontalSpace = true;
		textField.setLayoutData(gd);
	}
	
	@Override
	public Text getTextControl(Composite parent){
		if (textField == null) {
			textField = SWTHelper.createText(parent, numOfLines, flags);
			textField.setFont(parent.getFont());
			textField.addKeyListener(new KeyAdapter() {
				public void keyReleased(KeyEvent e){
					valueChanged();
				}
			});
			textField.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event){
					textField = null;
				}
			});
		} else {
			checkParent(textField, parent);
		}
		return textField;
	}
	
	@Override
	protected void doLoad(){
		if (textField != null) {
			String value = getPreferenceStore().getString(getPreferenceName());
			if (isStringList) {
				value = value.replaceAll(",", StringTool.lf); //$NON-NLS-1$ //$NON-NLS-2$
			}
			textField.setText(value);
		}
	}
	
	@Override
	protected void doStore(){
		String value = textField.getText();
		if (isStringList) {
			value = value.replaceAll("[\\r\\n]+", ","); //$NON-NLS-1$ //$NON-NLS-2$
		}
		getPreferenceStore().setValue(getPreferenceName(), value);
	}
	
	@Override
	public String getStringValue(){
		String ret = super.getStringValue();
		if (isStringList) {
			return ret.replaceAll("[\\r\\n]+", ","); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return ret;
	}
	
	@Override
	public void setStringValue(String value){
		if (isStringList) {
			super.setStringValue(value.replaceAll(",", "\\n")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			super.setStringValue(value);
		}
	}
	
}
