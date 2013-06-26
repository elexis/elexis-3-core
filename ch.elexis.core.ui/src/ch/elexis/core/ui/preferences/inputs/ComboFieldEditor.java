/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
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

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import ch.rgw.tools.StringTool;

public class ComboFieldEditor extends FieldEditor {
	Combo combo;
	String[] values;
	
	public Combo getCombo(){
		return combo;
	}
	
	public ComboFieldEditor(String preferenceName, String title, String[] values, Composite parent){
		init(preferenceName, title);
		this.values = values;
		createControl(parent);
	}
	
	@Override
	protected void adjustForNumColumns(int numColumns){
		GridData fd = (GridData) combo.getLayoutData();
		fd.horizontalSpan = numColumns - 1;
	}
	
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns){
		getLabelControl(parent);
		
		if (combo == null) {
			combo = new Combo(parent, SWT.NONE);
			combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			combo.setItems(values);
		}
	}
	
	@Override
	protected void doLoad(){
		if (combo != null) {
			String value = getPreferenceStore().getString(getPreferenceName());
			// int idx=StringTool.getIndex(values, value);
			combo.setText(value);
			// combo.select(idx);
		}
		
	}
	
	@Override
	protected void doLoadDefault(){
		if (combo != null) {
			String value = getPreferenceStore().getDefaultString(getPreferenceName());
			int idx = StringTool.getIndex(values, value);
			combo.select(idx);
		}
		// valueChanged();
		
	}
	
	@Override
	protected void doStore(){
		getPreferenceStore().setValue(getPreferenceName(), combo.getText());
	}
	
	@Override
	public int getNumberOfControls(){
		return 2;
	}
	
}
