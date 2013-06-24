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
package ch.elexis.core.data.util;

public abstract class RunnableWithParamAndReturnValue implements Runnable {
	private Object param;
	protected Object retval;
	
	public RunnableWithParamAndReturnValue(Object param){
		this.param = param;
	}
	
	public Object getValue(){
		return retval;
	}
	
	public void run(){
		try {
			retval = doRun(param);
		} catch (Exception ex) {
			retval = ex;
		}
	}
	
	public abstract Object doRun(Object param) throws Exception;
}
