package ch.elexis.data.service.internal;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.data.extension.ICoreOperationAdvisor;
import ch.elexis.core.data.util.IRunnableWithProgress;

@Component
public class TestOnlyCoreOperationAdvisor implements ICoreOperationAdvisor {
	
	@Override
	public void requestDatabaseConnectionConfiguration(){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void requestInitialMandatorConfiguration(){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void adaptForUser(){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean openQuestion(String title, String message){
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void openInformation(String title, String message){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void performLogin(Object shell){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getInitialPerspective(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean performDatabaseUpdate(String[] array, String pluginId){
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void showProgress(IRunnableWithProgress irwp, String taskName){
		// TODO Auto-generated method stub
		
	}
	
}
