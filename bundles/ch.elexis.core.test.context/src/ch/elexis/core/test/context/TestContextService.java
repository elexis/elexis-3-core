package ch.elexis.core.test.context;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.services.IContext;
import ch.elexis.core.services.IContextService;

@Component
public class TestContextService implements IContextService {
	
	private ThreadLocal<IContext> rootContext;
	
	@Activate
	public void activate(){
		rootContext = new ThreadLocal<IContext>() {
			@Override
			protected IContext initialValue(){
				return new TestContext();
			}
		};
	}
	
	private ConcurrentHashMap<String, TestContext> contexts;
	
	@Override
	public IContext getRootContext(){
		return rootContext.get();
	}
	
	@Override
	public Optional<IContext> getNamedContext(String name){
		return Optional.ofNullable(contexts.get(name));
	}
	
	@Override
	public IContext createNamedContext(String name){
		TestContext context = new TestContext((TestContext) rootContext.get(), name);
		contexts.put(name, context);
		return context;
	}
	
	@Override
	public void releaseContext(String name){
		TestContext context = contexts.get(name);
		if (context != null) {
			context.setParent(null);
			contexts.remove(name);
		}
		
	}
	
	@Override
	public void postEvent(String eventTopic, Object object){
		// TODO Auto-generated method stub
		
	}
	
}
