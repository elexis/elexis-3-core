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

package ch.elexis.core.datatypes;

import java.util.List;

public interface ICodeElement extends ISelectable {
	/** Name des zugrundeliegenden Codesystems liefern */
	public String getCodeSystemName();
	
	/** Kurzcode für das System liefern */
	public String getCodeSystemCode();
	
	/** Eine eindeutige ID für das Element liefern */
	public String getId();
	
	/**
	 * Das Element in Code-Form. Aus dem Code und der Klasse muss das Element sich wieder erstellen
	 * lassen
	 */
	public String getCode();
	
	/** Das Element in Klartext-Form */
	public String getText();
	
	/**
	 * Kontext-Aktionen für dieses Code-Element
	 * 
	 * @param kontext
	 * @return a list castable to IAction using <code>(Iterable<IAction>)(Iterable<?>)</code>
	 * @deprecated
	 * @since 3.0.0 method is bound for removal, please refactor
	 */
	public List<Object> getActions(Object kontext);
	
}
