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
package ch.elexis.core.ui.views.textsystem;

public class KategorieProperties extends AbstractProperties {
	private static final long serialVersionUID = 9181779544703607658L;
	
	private final static String KATEGORIE_FILENAME = "Kategorie.txt"; //$NON-NLS-1$
	
	protected String getFilename(){
		return KATEGORIE_FILENAME;
	}
	
	public String getDescription(final String kategorie){
		Object value = get(kategorie);
		if (value != null) {
			return value.toString();
		}
		return kategorie;
	}
	
}
