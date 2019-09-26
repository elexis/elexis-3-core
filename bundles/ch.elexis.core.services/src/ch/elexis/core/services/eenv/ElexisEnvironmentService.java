package ch.elexis.core.services.eenv;

import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.services.holder.ConfigServiceHolder;

public class ElexisEnvironmentService implements IElexisEnvironmentService {
	
	private String elexisEnvironmentHost;
	
	public ElexisEnvironmentService(String elexisEnvironmentHost){
		this.elexisEnvironmentHost = elexisEnvironmentHost;
	}
	
	@Override
	public String getVersion(){
		return "unused_unimplemented";
	}
	
	@Override
	public String getProperty(String key){
		return ConfigServiceHolder.get().get(key, null);
	}
	
	@Override
	public String getHostname(){
		return elexisEnvironmentHost;
	}
	
}
