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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

import ch.elexis.core.ui.UiDesk;

public class SpinnerField extends ActiveControl {
	
	public SpinnerField(Composite parent, int displayBits, String displayName, int min, int max){
		super(parent, displayBits, displayName);
		final Spinner spinner = new Spinner(this, SWT.NONE);
		spinner.setMaximum(max);
		spinner.setMinimum(min);
		spinner.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				int v = spinner.getSelection();
				textContents = Integer.toString(v);
				fireChangedEvent();
			}
			
		});
		setControl(spinner);
		
	}
	
	@Override
	protected void push(){
		UiDesk.syncExec(new Runnable() {
			public void run(){
				Spinner spinner = (Spinner) ctl;
				spinner.setSelection(Integer.parseInt(textContents));
			}
		});
	}
	
}
