/*******************************************************************************
 * Copyright (c) 2016 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.findings;

public interface ICoding {
	
	/**
	 * Get the code system can be a uri or oid.
	 * 
	 * @return
	 */
	public String getSystem();
	
	/**
	 * Get the code.
	 * 
	 * @return
	 */
	public String getCode();

	/**
	 * Get the string that describes the code.
	 * 
	 * @return
	 */
	public String getDisplay();
}
