/*******************************************************************************
 * Copyright (c) 2013 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.data.interfaces;

/**
 * A job that executes during stop() of the plugin (that means after the workbench is shut down
 * 
 * @author gerry
 * 
 */
public interface ShutdownJob {
	/**
	 * do whatever you like
	 */
	public void doit() throws Exception;
}
