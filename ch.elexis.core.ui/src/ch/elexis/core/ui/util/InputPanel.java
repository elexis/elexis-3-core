/*******************************************************************************
 * Copyright (c) 2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    D. Lutz	 - DBBased Importer
 *    
 *******************************************************************************/

package ch.elexis.core.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.locks.IUnlockable;
import ch.elexis.core.ui.util.LabeledInputField.AutoForm;
import ch.elexis.core.ui.util.LabeledInputField.InputData;

public class InputPanel extends Composite implements IUnlockable {
	int min, max;
	Composite top;
	InputData[] fields;
	AutoForm af;
	
	public InputPanel(Composite parent, int minColumns, int maxColumns, InputData[] fields){
		super(parent, SWT.NONE);
		this.fields = fields;
		min = minColumns;
		max = maxColumns;
		for (InputData id : fields) {
			LabeledInputField widget = id.getWidget();
			if (widget != null) {
				Label lbl = widget.getLabelComponent();
				Font lblFont = UiDesk.getFont("Helvetica", 8, SWT.ITALIC);
				lbl.setFont(lblFont);
			}
		}
		setLayout(new GridLayout());
		af = new LabeledInputField.AutoForm(this, fields, min, max);
		af.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
	}
	
	public AutoForm getAutoForm(){
		return af;
	}
	
	@Override
	public void setUnlocked(boolean unlock){
		for (InputData id : fields) {
			id.setEditable(unlock);
		}
	}
	
	public void save() {
		for (InputData inputData : fields) {
			af.save(inputData);
		}
	}
}
