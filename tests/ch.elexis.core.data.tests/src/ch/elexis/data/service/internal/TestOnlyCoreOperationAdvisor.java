package ch.elexis.data.service.internal;

import java.util.List;

import javax.security.auth.login.LoginException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.extension.CoreOperationAdvisorHolder;
import ch.elexis.core.data.extension.ICoreOperationAdvisor;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.data.util.IRunnableWithProgress;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.ILoginContributor;
import ch.elexis.data.Anwender;

@Component
public class TestOnlyCoreOperationAdvisor implements ICoreOperationAdvisor {
	
	@Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE)
	private List<ILoginContributor> loginServices;
	
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
	public boolean performLogin(Object shell){
		
		CoreHub.reconfigureServices();
		CoreHub.logoffAnwender();
		
		IUser user;
		try {
			user = loginServices.get(0).performLogin(null);
		} catch (LoginException e) {
			e.printStackTrace();
			return false;
		}
		
		if (user != null && user.isActive()) {
			// set user in system
			ContextServiceHolder.get().setActiveUser(user);
			ElexisEventDispatcher.getInstance().fire(new ElexisEvent(CoreHub.getLoggedInContact(),
				Anwender.class, ElexisEvent.EVENT_USER_CHANGED));
			
			CoreOperationAdvisorHolder.get().adaptForUser();
			CoreHub.getLoggedInContact().setInitialMandator();
			CoreHub.userCfg = CoreHub.getUserSetting(CoreHub.getLoggedInContact());
			CoreHub.heart.resume(true);
			
			return true;
		}
		
		return false;
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
