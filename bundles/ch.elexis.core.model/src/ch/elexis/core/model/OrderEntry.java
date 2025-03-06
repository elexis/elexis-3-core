package ch.elexis.core.model;

import java.util.Optional;

import ch.elexis.core.jpa.entities.Bestellung;
import ch.elexis.core.jpa.entities.BestellungEntry;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.services.IStoreToStringContribution;

public class OrderEntry extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.BestellungEntry>
		implements IdentifiableWithXid, IOrderEntry {

	public OrderEntry(BestellungEntry entity) {
		super(entity);
	}

	@Override
	public IOrder getOrder() {
		if (getEntity().getBestellung() != null) {
			return ModelUtil.getAdapter(getEntity().getBestellung(), IOrder.class);
		}
		return null;
	}

	@Override
	public void setOrder(IOrder value) {
		if (value instanceof AbstractIdDeleteModelAdapter) {
			getEntityMarkDirty().setBestellung((Bestellung) ((AbstractIdDeleteModelAdapter<?>) value).getEntity());
		} else if (value == null) {
			getEntityMarkDirty().setBestellung(null);
		}
		addRefresh(value);
	}

	@Override
	public IStock getStock() {
		String stockId = getEntity().getStockid();
		if (stockId != null && !stockId.isEmpty()) {
			return ModelUtil.load(stockId, IStock.class);
		}
		return null;
	}

	@Override
	public void setStock(IStock value) {
		getEntityMarkDirty().setStockid(value != null ? value.getId() : null);
	}

	@Override
	public int getAmount() {
		return getEntity().getCount();
	}

	@Override
	public void setAmount(int value) {
		getEntityMarkDirty().setCount(value);
	}

	@Override
	public int getDelivered() {
		return getEntity().getDelivered();
	}

	@Override
	public void setDelivered(int value) {
		getEntityMarkDirty().setDelivered(value);
	}

	@Override
	public IArticle getArticle() {
		Optional<Identifiable> loaded = ModelUtil.getFromStoreToString(
				getEntity().getArticleType() + IStoreToStringContribution.DOUBLECOLON + getEntity().getArticleId());
		if (loaded.isPresent() && loaded.get() instanceof IArticle) {
			return (IArticle) loaded.get();
		}
		return null;
	}

	@Override
	public void setArticle(IArticle article) {
		String storeToString = ModelUtil.getStoreToString(article)
				.orElseThrow(() -> new IllegalStateException("Could not get store to string for [" + article + "]"));
		String[] split = storeToString.split(IStoreToStringContribution.DOUBLECOLON);
		if (split != null && split.length == 2) {
			getEntityMarkDirty().setArticleType(split[0]);
			getEntityMarkDirty().setArticleId(split[1]);
		} else {
			throw new IllegalStateException("Could not set article [" + storeToString + "]");
		}
	}

	@Override
	public IContact getProvider() {
		String providerId = getEntity().getProviderId();
		if (providerId != null && !providerId.isEmpty()) {
			return ModelUtil.load(providerId, IContact.class);
		}
		return null;
	}

	@Override
	public void setProvider(IContact value) {
		getEntityMarkDirty().setProviderId(value != null ? value.getId() : null);
	}

	@Override
	public OrderEntryState getState() {
		return OrderEntryState.ofValue(getEntity().getState());
	}

	@Override
	public void setState(OrderEntryState value) {
		getEntityMarkDirty().setState(value.getValue());
	}
}
