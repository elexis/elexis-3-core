package ch.elexis.core.test.service;

import java.util.Optional;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.services.IContext;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;

@Component
public class TestContextService implements IContextService {
	
	private IContext rootContext;
	
	private IModelService coreModelService;
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private void setModelService(IModelService coreModelService){
		this.coreModelService = coreModelService;
	}
	
	@Activate
	public void activate() {
		rootContext = new TestContext(coreModelService);
		Logger logger = LoggerFactory.getLogger(getClass());
		logger.error("Initializing test context service");
	}
	
	@Override
	public IContext getRootContext(){
		return rootContext;
	}
	
	@Override
	public Optional<IContext> getNamedContext(String name){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public IContext createNamedContext(String name){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void releaseContext(String name){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void postEvent(String eventTopic, Object object){
		// TODO Auto-generated method stub
		
	}
	
}
