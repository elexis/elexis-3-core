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
package ch.elexis.core.ui.preferences.inputs;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class FileSelectorField extends Composite {
	String result;
	
	public FileSelectorField(String title, Composite parent, int flags){
		super(parent, flags);
		setLayout(new GridLayout(3, false));
		
	}
	
}
