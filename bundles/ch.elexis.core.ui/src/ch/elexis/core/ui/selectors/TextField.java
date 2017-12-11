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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ui.UiDesk;

public class TextField extends ActiveControl {
	
	public TextField(Composite parent, int displayBits, String displayName){
		super(parent, displayBits, displayName);
		int swtoption = SWT.BORDER;
		if (isReadonly()) {
			swtoption |= SWT.READ_ONLY;
		}
		setControl(new Text(this, swtoption));
		getTextControl().addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e){
				textContents = getTextControl().getText();
				fireChangedEvent();
			}
		});
	}
	
	public TextField(Composite parent, int displayBits, String displayName, int listenerType,
		Listener listener){
		this(parent, displayBits, displayName);
		if (listener != null) {
			getTextControl().addListener(listenerType, listener);
		}
	}
	
	public Text getTextControl(){
		return (Text) ctl;
	}
	
	@Override
	public void push(){
		UiDesk.syncExec(new Runnable() {
			public void run(){
				getTextControl().setText(textContents);
			}
		});
	}
	
}
