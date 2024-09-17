package ch.elexis.core.model;

import java.util.Optional;

import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.services.IStoreToStringContribution;

public class StockEntry extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.StockEntry>
		implements IdentifiableWithXid, IStockEntry {

	public StockEntry(ch.elexis.core.jpa.entities.StockEntry entity) {
		super(entity);
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
	public int getMinimumStock() {
		return getEntity().getMinimumStock();
	}

	@Override
	public void setMinimumStock(int minStock) {
		getEntityMarkDirty().setMinimumStock(minStock);
	}

	@Override
	public int getCurrentStock() {
		return getEntity().getCurrentStock();
	}

	@Override
	public void setCurrentStock(int currentStock) {
		getEntityMarkDirty().setCurrentStock(currentStock);
	}

	@Override
	public int getMaximumStock() {
		return getEntity().getMaximumStock();
	}

	@Override
	public void setMaximumStock(int maxStock) {
		getEntityMarkDirty().setMaximumStock(maxStock);
	}

	@Override
	public int getFractionUnits() {
		return getEntity().getFractionUnits();
	}

	@Override
	public void setFractionUnits(int fractionUnits) {
		getEntityMarkDirty().setFractionUnits(fractionUnits);
	}

	@Override
	public IContact getProvider() {
		if (getEntity().getProvider() != null) {
			return ModelUtil.getAdapter(getEntity().getProvider(), IContact.class);
		}
		return null;
	}

	@Override
	public void setProvider(IContact provider) {
		if (provider instanceof AbstractIdDeleteModelAdapter) {
			getEntityMarkDirty().setProvider((Kontakt) ((AbstractIdDeleteModelAdapter<?>) provider).getEntity());
		} else if (provider == null) {
			getEntityMarkDirty().setProvider(null);
		}
	}

	@Override
	public IStock getStock() {
		if (getEntity().getStock() != null) {
			return ModelUtil.getAdapter(getEntity().getStock(), IStock.class);
		}
		return null;
	}

	@Override
	public void setStock(IStock stock) {
		if (stock instanceof AbstractIdModelAdapter) {
			getEntityMarkDirty()
					.setStock((ch.elexis.core.jpa.entities.Stock) ((AbstractIdModelAdapter<?>) stock).getEntity());
		} else if (stock == null) {
			getEntityMarkDirty().setStock(null);
		}
	}

	@Override
	public String getLabel() {
		IArticle article = getArticle();
		if (article != null) {
			return article.getName();
		} else {
			return getEntity().getArticleType() + "[" + getEntity().getArticleId() + "]";
		}
	}

	@Override
	public boolean getMediorder() {
		return getEntity().isMediorder();
	}

	@Override
	public void setMediorder(boolean Value) {
		getEntityMarkDirty().setMediorder(Value);
	}
}
