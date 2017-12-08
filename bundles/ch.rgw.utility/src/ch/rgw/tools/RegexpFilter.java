/*******************************************************************************
 * Copyright (c) 2005-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.rgw.tools;

import java.util.regex.Pattern;

public class RegexpFilter implements IFilter {
	Pattern pattern;
	
	public RegexpFilter(String regexp){
		pattern = Pattern.compile(regexp);
	}
	
	public boolean select(Object element){
		String m = element.toString();
		return pattern.matcher(m).matches();
	}
	
}