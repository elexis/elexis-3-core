package ch.elexis.core.jpa.model.adapter;

import java.util.function.Consumer;
import java.util.function.Predicate;

import ch.elexis.core.jpa.entities.EntityWithId;

/**
 * Tie interface, adapter and entity classes together. Needed to get the correct adapter class, if
 * the entity maps to multiple adapters.
 * 
 * @author thomas
 *
 */
public class MappingEntry {
	
	private Class<?> interfaceClass;
	private Class<? extends AbstractIdModelAdapter<?>> adapterClass;
	private Class<? extends EntityWithId> entityClass;
	
	private Predicate<AbstractIdModelAdapter<?>> preCondition;
	private Consumer<AbstractIdModelAdapter<?>> initializer;
	
	public MappingEntry(Class<?> interfaceClass,
		Class<? extends AbstractIdModelAdapter<?>> adapterClass,
		Class<? extends EntityWithId> entityClass){
		this.interfaceClass = interfaceClass;
		this.adapterClass = adapterClass;
		this.entityClass = entityClass;
	}
	
	/**
	 * Add a {@link Predicate} that will be tested on load of a {@link AbstractIdModelAdapter}. See
	 * {@link AbstractModelAdapterFactory#getModelAdapter(AbstractDBObjectId, Class, boolean)}
	 * 
	 * @param predicate
	 * @return
	 */
	public MappingEntry adapterPreCondition(Predicate<AbstractIdModelAdapter<?>> predicate){
		this.preCondition = predicate;
		return this;
	}
	
	/**
	 * Add a {@link Consumer} that will be applied on creation of a new
	 * {@link AbstractIdModelAdapter}. See {@link AbstractModelAdapterFactory#createAdapter(Class)}
	 * 
	 * @param consumer
	 * @return
	 */
	public MappingEntry adapterInitializer(Consumer<AbstractIdModelAdapter<?>> consumer){
		initializer = consumer;
		return this;
	}
	
	/**
	 * Apply a initializer is available it is applied on the adapter.
	 * 
	 * @param adapter
	 */
	public void initialize(AbstractIdModelAdapter<?> adapter){
		if (initializer != null) {
			initializer.accept(adapter);
		}
	}
	
	/**
	 * If a precondition is available, it is tested on the adapter. Else always true.
	 * 
	 * @param adapter
	 * @return
	 */
	public boolean testAdapterPrecondition(AbstractIdModelAdapter<?> adapter){
		if (preCondition != null) {
			return preCondition.test(adapter);
		}
		return true;
	}
	
	public Class<?> getInterfaceClass(){
		return interfaceClass;
	}
	
	public Class<? extends AbstractIdModelAdapter<?>> getAdapterClass(){
		return adapterClass;
	}
	
	public Class<? extends EntityWithId> getEntityClass(){
		return entityClass;
	}
}
