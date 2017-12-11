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
package ch.elexis.core.data.interfaces.events;

import org.eclipse.core.runtime.IProgressMonitor;

public class ElexisStatusProgressMonitor implements IProgressMonitor {
	
	public ElexisStatusProgressMonitor(String string, int size){
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void beginTask(String name, int totalWork){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void done(){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void internalWorked(double work){
		// TODO Auto-generated method stub
	}
	
	@Override
	public boolean isCanceled(){
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void setCanceled(boolean value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setTaskName(String name){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void subTask(String name){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void worked(int work){
		// TODO Auto-generated method stub
		
	}
	
}
