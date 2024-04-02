package ch.elexis.core.jpa.model.adapter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.emf.ecore.EStructuralFeature;

import ch.elexis.core.jpa.entities.EntityWithExtInfo;
import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.jpa.model.adapter.mixin.ExtInfoHandler;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.WithExtInfo;
import ch.elexis.core.services.IModelService;

public abstract class AbstractIdModelAdapter<T extends EntityWithId> implements Identifiable {

	protected List<Identifiable> refreshList;
	protected List<Identifiable> changedList;
	protected List<EStructuralFeature> updatedList;
	protected ExtInfoHandler extInfoHandler;

	/**
	 * Used in json serialization
	 */
	@SuppressWarnings("unused")
	private final String entityType;

	private T entity;

	private boolean dirty;

	@SuppressWarnings("unchecked")
	public AbstractIdModelAdapter(T entity) {
		this.dirty = false;
		this.entity = entity;
		this.entityType = entity.getClass().getName();
		// make sure model supports id and delete
		if (!(entity instanceof EntityWithId)) {
			throw new IllegalStateException(
					"Model " + entity + " is no subclass of " + EntityWithId.class.getSimpleName()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (this instanceof WithExtInfo && entity instanceof EntityWithExtInfo) {
			extInfoHandler = new ExtInfoHandler((AbstractIdModelAdapter<? extends EntityWithExtInfo>) this);
		}
	}

	/**
	 * Get the entity object. Should only be used for reading from the entity, if
	 * the entity properties are changed, always use
	 * {@link AbstractIdModelAdapter#getEntityMarkDirty()}.
	 *
	 * @return
	 */
	public T getEntity() {
		return entity;
	}

	/**
	 * Get the entity object and mark this model adapter as dirty. In dirty state
	 * {@link AbstractIdModelAdapter#setEntity(EntityWithId)} will have no effect.
	 * Dirty state is reset by {@link AbstractModelService#save(Identifiable)} and
	 * {@link AbstractModelService#save(List)}.// TODO Auto-generated method stub
	 * Identifiable.super.clearUpdated();
	 *
	 * @return
	 */
	public T getEntityMarkDirty() {
		this.dirty = true;
		return entity;
	}

	/**
	 * <b>IMPORTANT:</b> this method should only be used {@link IModelService}
	 * implementations to update the entity on merge. This is needed if entity
	 * listeners update values of the entity on persist, to update with the modified
	 * entity. The method has no effect it this model adapter is in dirty state.
	 *
	 * @param entity
	 * @param resetDirty
	 */
	@SuppressWarnings("unchecked")
	public synchronized void setEntity(EntityWithId entity, boolean resetDirty) {
		if (!dirty || (dirty && resetDirty)) {
			this.entity = (T) entity;
			dirty = false;
		}
	}

	@Override
	public String getId() {
		return getEntity().getId();
	}

	@Override
	public String getLabel() {
		return getEntity().toString();
	}

	@Override
	public Long getLastupdate() {
		return getEntity().getLastupdate() != null ? getEntity().getLastupdate() : 0L;
	}

	@Override
	public void addUpdated(EStructuralFeature feature) {
		if (updatedList == null) {
			updatedList = new ArrayList<>();
		}
		updatedList.add(feature);
	}

	@Override
	public List<EStructuralFeature> getUpdated() {
		return updatedList;
	}

	@Override
	public void clearUpdated() {
		updatedList = null;
	}

	@Override
	public void addRefresh(Identifiable changed) {
		if (refreshList == null) {
			refreshList = new ArrayList<>();
		}
		refreshList.add(changed);
	}

	@Override
	public List<Identifiable> getRefresh() {
		return refreshList;
	}

	@Override
	public void clearRefresh() {
		refreshList = null;
	}

	@Override
	public void addChanged(Identifiable changed) {
		if (changedList == null) {
			changedList = new ArrayList<>();
			changedList.add(this);
		}
		changedList.add(changed);
	}

	@Override
	public List<Identifiable> getChanged() {
		return changedList;
	}

	@Override
	public void clearChanged() {
		changedList = null;
	}

	public boolean isDirty() {
		return dirty;
	}

	protected Date toDate(LocalDateTime localDateTime) {
		ZonedDateTime atZone = localDateTime.atZone(ZoneId.systemDefault());
		return Date.from(atZone.toInstant());
	}

	protected Date toDate(LocalDate localDate) {
		ZonedDateTime atZone = localDate.atStartOfDay(ZoneId.systemDefault());
		return Date.from(atZone.toInstant());
	}

	protected LocalDateTime toLocalDate(Date date) {
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	@Override
	public String toString() {
		return getClass().getName() + " [getEntity()=" + getEntity() + ", isDrity()=" + isDirty() + ", getLastupdate()=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ getLastupdate() + "]"; //$NON-NLS-1$
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractIdModelAdapter<?> other = (AbstractIdModelAdapter<?>) obj;
		if (entity == null) {
			if (other.entity != null)
				return false;
		} else if (!entityEqualId(entity, other.entity))
			return false;
		return true;
	}

	private boolean entityEqualId(EntityWithId left, EntityWithId right) {
		if (left.getId() != null && right.getId() != null) {
			return left.getId().equals(right.getId());
		}
		return false;
	}
}
