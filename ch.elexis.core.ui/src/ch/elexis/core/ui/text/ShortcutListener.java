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
package ch.elexis.core.ui.text;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;

public class ShortcutListener implements VerifyKeyListener {
	private EnhancedTextField mine;
	
	public ShortcutListener(EnhancedTextField etf){
		mine = etf;
	}
	
	public void verifyKey(VerifyEvent event){
		if (event.stateMask == SWT.MOD1) {
			switch (event.keyCode) {
			// 'z'
			case 122:
				System.out.println("undo");
				mine.undo();
				event.doit = false;
				break;
			/*
			 * already handled by StyledText itself // 'c' case 99: System.out.println("copy");
			 * mine.text.copy(); break; // 'v' case 118: System.out.print("paste");
			 * mine.text.paste(); break;
			 */
			default:
				System.out.println(event.toString());
			}
			/*
			 * don't ignore any other CTRL shortcuts! event.doit=false;
			 */
		} else
			event.doit = true;
	}
	
}
