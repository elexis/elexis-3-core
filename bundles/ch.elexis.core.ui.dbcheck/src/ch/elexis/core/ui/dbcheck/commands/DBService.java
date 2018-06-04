package ch.elexis.core.ui.dbcheck.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.ui.dbcheck.ui.DBTestDialog;

public class DBService implements IHandler {
	
	@Override
	public void addHandlerListener(IHandlerListener handlerListener){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void dispose(){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		DBTestDialog dbt = new DBTestDialog(Display.getCurrent().getActiveShell());
		return dbt.open();
	}
	
	@Override
	public boolean isEnabled(){
		// TODO Only if user has sufficient access rights
		return true;
	}
	
	@Override
	public boolean isHandled(){
		return true;
	}
	
	@Override
	public void removeHandlerListener(IHandlerListener handlerListener){
		// TODO Auto-generated method stub
		
	}
	
}
