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
package ch.elexis.core.ui.selectors;

import org.eclipse.swt.widgets.Composite;

public class DateField extends TextField {
	
	public DateField(Composite parent, int displayBits, String displayName){
		super(parent, displayBits, displayName);
	}
	
}
