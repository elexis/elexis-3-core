package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.QueryHint;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.QueryHints;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;

@Entity
@Table(name = "STOCK_ENTRY")
@EntityListeners(EntityWithIdListener.class)
@Cache(expiry = 15000)
@NamedQueries({
		@NamedQuery(name = "StockEntry.articleId.articleType", query = "SELECT se FROM StockEntry se WHERE se.articleId = :articleId AND se.articleType = :articleType AND se.deleted = false"),
		@NamedQuery(name = "StockEntry_SumCurrentStock.articleId.articleType", query = "SELECT SUM(se.currentStock) FROM StockEntry se WHERE se.articleId = :articleId AND se.articleType = :articleType AND se.deleted = false", hints = {
				@QueryHint(name = QueryHints.CACHE_USAGE, value = CacheUsage.DoNotCheckCache) }),
		@NamedQuery(name = "StockEntry_AvailableCurrentStock.articleId.articleType", query = "SELECT MAX(CASE WHEN se.currentStock <= 0 THEN 0 WHEN (ABS(se.minimumStock)-se.currentStock) >=0 THEN 1 ELSE 2 END) FROM StockEntry se WHERE se.articleId = :articleId AND se.articleType = :articleType AND se.deleted = false", hints = {
				@QueryHint(name = QueryHints.CACHE_USAGE, value = CacheUsage.DoNotCheckCache) }),
		@NamedQuery(name = "StockEntry_AvailableCurrentBelowStock.articleId.articleType", query = "SELECT MAX(CASE WHEN se.currentStock <= 0 THEN 0 WHEN (ABS(se.minimumStock)-se.currentStock) >0 THEN 1 ELSE 2 END) FROM StockEntry se WHERE se.articleId = :articleId AND se.articleType = :articleType AND se.deleted = false", hints = {
				@QueryHint(name = QueryHints.CACHE_USAGE, value = CacheUsage.DoNotCheckCache) }), })
public class StockEntry extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	/**
	 * Stock this entry is shelved in
	 */
	@ManyToOne
	@JoinColumn(name = "STOCK", insertable = true)
	private Stock stock;

	@Column(name = "article_type", length = 255, nullable = false)
	String articleType;

	@Column(name = "article_id", length = 25, nullable = false)
	String articleId;

	/**
	 * The minimum number of items packages on stock. If lower than {@link #curr} we
	 * consider the stockage of this item to be critical.
	 */
	@Column(name = "min")
	int minimumStock;

	/**
	 * The current number of item packages on stock.
	 */
	@Column(name = "current")
	int currentStock;

	/**
	 * The maximum number of item packages targeted to be on stock. Acts as
	 * information value to dispatch re-orders.
	 */
	@Column(name = "max")
	int maximumStock;

	/**
	 * If there is an open package, this represents the number of fraction units
	 * available by this open package. The package itself is not part of current any
	 * more.
	 */
	@Column
	int fractionUnits;

	/**
	 * The contact this article may be purchased from.
	 */
	@OneToOne
	@JoinColumn(name = "PROVIDER", insertable = false)
	Kontakt provider;

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString() + " articleId=[" + getArticleId() + "]  min=[" + getMinimumStock() + "] current=["
				+ getCurrentStock() + "]";
	}

	public Stock getStock() {
		return stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	public String getArticleType() {
		return articleType;
	}

	public void setArticleType(String articleType) {
		this.articleType = articleType;
	}

	public String getArticleId() {
		return articleId;
	}

	public void setArticleId(String articleId) {
		this.articleId = articleId;
	}

	public Kontakt getProvider() {
		return provider;
	}

	public void setProvider(Object provider) {
		this.provider = (Kontakt) provider;
	}

	public int getCurrentStock() {
		return currentStock;
	}

	public void setCurrentStock(int currentStock) {
		this.currentStock = currentStock;
	}

	public int getMinimumStock() {
		return minimumStock;
	}

	public void setMinimumStock(int minimumStock) {
		this.minimumStock = minimumStock;
	}

	public int getMaximumStock() {
		return maximumStock;
	}

	public void setMaximumStock(int maximumStock) {
		this.maximumStock = maximumStock;
	}

	public int getFractionUnits() {
		return fractionUnits;
	}

	public void setFractionUnits(int fractionUnits) {
		this.fractionUnits = fractionUnits;
	}

	@Override
	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public Long getLastupdate() {
		return lastupdate;
	}

	@Override
	public void setLastupdate(Long lastupdate) {
		this.lastupdate = lastupdate;
	}
}
