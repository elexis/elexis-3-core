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
package ch.elexis.core.data.interfaces.scripting;

import ch.elexis.core.exceptions.ElexisException;

public interface Interpreter {
	
	public void setValue(String name, Object value) throws ElexisException;
	
	public Object run(String script, boolean showErrors) throws ElexisException;
}
