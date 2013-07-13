package ch.elexis.core.data.beans.base;

import org.eclipse.core.runtime.Assert;

public class BeanPersistentObject<E> extends BasePropertyChangeSupport {
	protected E entity;

	/**
	 * @param entity
	 *            the entity this property support is bound to
	 */
	public BeanPersistentObject(E entity) {
		this.entity = entity;

		// TODO: Check if entity really exists
		Assert.isNotNull(entity);
	}

	public E getContainedEntity() {
		return entity;
	}

}
