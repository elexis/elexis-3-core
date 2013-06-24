/*******************************************************************************
 * Copyright (c) 2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.datatypes;

/**
 * Merely a marker for an Object that allows User interaction. Implementations can make menus,
 * toolbar buttons and such from such IActionDefinitions
 * 
 * @author gerry
 * 
 */
public interface IActionDefinition {
	public String getName();
	
	public String getToolTip();
	
}
