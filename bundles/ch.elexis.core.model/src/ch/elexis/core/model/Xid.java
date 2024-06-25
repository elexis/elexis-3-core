package ch.elexis.core.model;

import java.util.Optional;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.model.service.holder.StoreToStringServiceHolder;
import ch.elexis.core.services.IStoreToStringContribution;

public class Xid extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Xid> implements Identifiable, IXid {

	public Xid(ch.elexis.core.jpa.entities.Xid entity) {
		super(entity);
	}

	@Override
	public String getDomain() {
		return getEntity().getDomain();
	}

	@Override
	public void setDomain(String value) {
		getEntityMarkDirty().setDomain(value);
	}

	@Override
	public String getDomainId() {
		return getEntity().getDomainId();
	}

	@Override
	public void setDomainId(String value) {
		getEntityMarkDirty().setDomainId(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Class<T> clazz) {
		// load using storeToString
		String storeToString = getEntity().getType() + IStoreToStringContribution.DOUBLECOLON + getEntity().getObject();
		Optional<Identifiable> loadedObject = StoreToStringServiceHolder.get().loadFromString(storeToString);
		if (loadedObject.isPresent()) {
			if (clazz.isAssignableFrom(loadedObject.get().getClass())) {
				return (T) loadedObject.get();
			} else {
				// try loading as provided type
				String type = StoreToStringServiceHolder.get().getTypeForModel(clazz);
				loadedObject = StoreToStringServiceHolder.get()
						.loadFromString(type + IStoreToStringContribution.DOUBLECOLON + getEntity().getObject());
				if (clazz.isAssignableFrom(loadedObject.get().getClass())) {
					return (T) loadedObject.get();
				}
			}
		}
		return null;
	}

	@Override
	public void setObject(Object object) {
		if (object instanceof Identifiable) {
			Identifiable identifiable = ((Identifiable) object);
			Optional<String> type = getStoreToStringType(object);
			if (type.isPresent()) {
				getEntityMarkDirty().setObject(identifiable.getId());
				getEntityMarkDirty().setType(type.get());
			}
		} else {
			throw new IllegalStateException("Object must be an Identifiable");
		}
	}

	private Optional<String> getStoreToStringType(Object object) {
		String storeToString = StoreToStringServiceHolder.getStoreToString(object);
		if (storeToString != null) {
			String[] parts = storeToString.split(IStoreToStringContribution.DOUBLECOLON);
			if (parts.length == 2) {
				return Optional.of(parts[0]);
			}
		}
		return Optional.empty();
	}

	@Override
	public XidQuality getQuality() {
		return getEntity().getQuality();
	}

	@Override
	public void setQuality(XidQuality value) {
		getEntityMarkDirty().setQuality(value);
	}

	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IXid getXid(String domain) {
		throw new UnsupportedOperationException();
	}
}
