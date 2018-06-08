package ch.elexis.core.jpa.model.adapter;

import java.util.function.Predicate;

import ch.elexis.core.jpa.entities.AbstractDBObjectIdDeleted;

/**
 * Tie interface, adapter and entity classes together. Needed to get the correct adapter class, if
 * the entity maps to multiple adapters.
 * 
 * @author thomas
 *
 */
public class MappingEntry {
	
	private Class<?> interfaceClass;
	private Class<? extends AbstractIdDeleteModelAdapter<?>> adapterClass;
	private Class<? extends AbstractDBObjectIdDeleted> entityClass;
	
	private Predicate<AbstractIdDeleteModelAdapter<?>> preCondition;
	
	public MappingEntry(Class<?> interfaceClass,
		Class<? extends AbstractIdDeleteModelAdapter<?>> adapterClass,
		Class<? extends AbstractDBObjectIdDeleted> entityClass){
		this.interfaceClass = interfaceClass;
		this.adapterClass = adapterClass;
		this.entityClass = entityClass;
	}
	
	public MappingEntry adapterPreCondition(Predicate<AbstractIdDeleteModelAdapter<?>> predicate){
		this.preCondition = predicate;
		return this;
	}
	
	public boolean testAdapterPrecondition(AbstractIdDeleteModelAdapter<?> adapter){
		if (preCondition != null) {
			return preCondition.test(adapter);
		}
		return true;
	}
	
	public Class<?> getInterfaceClass(){
		return interfaceClass;
	}
	
	public Class<? extends AbstractIdDeleteModelAdapter<?>> getAdapterClass(){
		return adapterClass;
	}
	
	public Class<? extends AbstractDBObjectIdDeleted> getEntityClass(){
		return entityClass;
	}
}
