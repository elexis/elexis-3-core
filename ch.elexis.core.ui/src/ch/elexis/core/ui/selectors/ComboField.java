/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.ui.UiDesk;

public class ComboField extends ActiveControl {
	Combo combo;
	
	public ComboField(Composite parent, int displayBits, String displayName, String... values){
		super(parent, displayBits, displayName);
		int swtflag = SWT.READ_ONLY | SWT.SINGLE;
		if (isReadonly()) {
			swtflag |= SWT.READ_ONLY;
		}
		combo = new Combo(parent, swtflag);
		combo.setItems(values);
		combo.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				textContents = combo.getText();
			}
			
		});
		setControl(combo);
	}
	
	@Override
	public boolean isValid(){
		int idx = combo.getSelectionIndex();
		return idx != -1;
	}
	
	@Override
	public void push(){
		UiDesk.syncExec(new Runnable() {
			public void run(){
				combo.setText(textContents);
			}
		});
	}
	
}
