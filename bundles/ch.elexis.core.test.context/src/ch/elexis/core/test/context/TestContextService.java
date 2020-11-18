package ch.elexis.core.test.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import ch.elexis.core.services.IContext;
import ch.elexis.core.services.IContextService;

@Component
public class TestContextService implements IContextService {
	
	private ThreadLocal<IContext> rootContext;
	
	@Reference
	private EventAdmin eventAdmin;
	
	@Activate
	public void activate(){
		rootContext = new ThreadLocal<IContext>() {
			@Override
			protected IContext initialValue(){
				return new TestContext();
			}
		};
		contexts = new ConcurrentHashMap<String, TestContext>();
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
	public void postEvent(String topic, Object object){
		if (eventAdmin != null) {
			Map<String, Object> properites = new HashMap<>();
			properites.put("org.eclipse.e4.data", object);
			Event event = new Event(topic, properites);
			eventAdmin.postEvent(event);
		} else {
			throw new IllegalStateException("No EventAdmin available");
		}
	}
	
}
