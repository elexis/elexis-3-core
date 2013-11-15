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
package ch.elexis.core.console.application;

import ch.elexis.core.data.extension.AbstractCoreOperationAdvisor;

public class CoreOperationAdvisor extends AbstractCoreOperationAdvisor {
	
	@Override
	public void requestDatabaseConnectionConfiguration(){
		System.out.println("CoreOperationAdvisor: requestDatabaseConnectionConfiguration()");
		
	}
	
	@Override
	public void requestInitialMandatorConfiguration(){
		System.out.println("CoreOperationAdvisor: requestInitialMandatorConfiguration()");
		
	}
	
	@Override
	public void adaptForUser(){
		System.out.println("CoreOperationAdvisor: adaptForUser()");
	}
	
	@Override
	public boolean openQuestion(String title, String message){
		System.out.println("CoreOperationAdvisor: openQuestion()");
		return false;
	}
	
	public void performLogin(Object shell){
		System.out.println("CoreOperationAdvisor: performLogin()");
	}
	
	@Override
	public String getInitialPerspective(){
		System.out.println("CoreOperationAdvisor: getInitialPerspective()");
		return null;
	}
	
}
