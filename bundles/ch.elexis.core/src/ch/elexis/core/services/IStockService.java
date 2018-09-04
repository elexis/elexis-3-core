package ch.elexis.core.services;

import java.util.List;

import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;

public interface IStockService {

	public enum Availability {
		IN_STOCK, CRITICAL_STOCK, OUT_OF_STOCK
	}

	/**
	 * Store an article in stock. This operation is idempotent. Each article may
	 * exist only once per stock.
	 * 
	 * @param stock
	 *            the stock to store the article in
	 * @param article
	 *            the store to string of the article
	 * @return an {@link IStockEntry} if created or found existing, or
	 *         <code>null</code> in case of error
	 * 
	 */
	public IStockEntry storeArticleInStock(IStock stock, String article);

	/**
	 * Unstocks an article, that is - the given article is not further being
	 * considered as an article to be held on stock. Effectively removes all
	 * {@link IStockEntry} for the given article on this {@link IStock}.
	 * 
	 * @param stock
	 * @param article
	 */
	public void unstoreArticleFromStock(IStock stock, String article);

	/**
	 * The accumulated (over all stocks) summed stock amount of an article. If
	 * there is no stock for this article, returns <code>null</code>
	 * 
	 * @param article
	 * @return <code>null</code> if not stocked, else the accumulated and summed
	 *         number of packages over all stocks (including mandator owned)
	 */
	public Long getCumulatedStockForArticle(IArticle article);

	/**
	 * The accumulated (over all stocks) availability of an article.
	 * 
	 * @param article
	 * @return determined by the highest stock availability, returns
	 *         {@link Availability} or <code>null</code> if not on stock. E.g.
	 *         as long as one stock has sufficient items, returns
	 *         {@link Availability#IN_STOCK}
	 */
	public Availability getCumulatedAvailabilityForArticle(IArticle article);

	/**
	 * @param stock
	 * @param article
	 * @return the {@link Availability} of an article in a specific stock
	 */
	public Availability getArticleAvailabilityForStock(IStock stock, String article);

	/**
	 * Find all stock entries for the provided stock
	 * 
	 * @param stock
	 * @return
	 */
	public List<? extends IStockEntry> findAllStockEntriesForStock(IStock stock);

	/**
	 * 
	 * @param article
	 * @return all {@link IStockEntry} existing for a given article
	 */
	public List<? extends IStockEntry> findAllStockEntriesForArticle(String article);

	/**
	 * Find the preferred stock entry for an article. That is, if there is a
	 * single stock entry for an article, return it. If there exist multiple,
	 * return the one located in the stock with the highest global priority. If
	 * a mandator is provided, and he owns a private stock, it will always be
	 * preffered.
	 * 
	 * @param article
	 *            the store to string of the article
	 * @param mandatorId
	 *            may be <code>null</code> to not consider the mandator
	 * @return <code>null</code> if no stock entry found, else the respective
	 *         entry
	 */
	public IStockEntry findPreferredStockEntryForArticle(String article, String mandatorId);

	/**
	 * 
	 * @param stock
	 * @param article
	 * @return <code>null</code> if article is not stocked in this
	 *         {@link IStock}, else the respective {@link IStockEntry}
	 */
	public IStockEntry findStockEntryForArticleInStock(IStock stock, String article);

	/**
	 * Perform a single disposal of an article. The article will be withdrawn
	 * from the Stock with the highest priority owning this article (if
	 * multiple).
	 * 
	 * @param article
	 * @param mandatorId
	 *            may be <code>null</code> to not consider the mandator
	 * @param count
	 * @return
	 */
	public IStatus performSingleDisposal(IArticle article, int count, String mandatorId);
	
	/**
	 * Perform a single disposal of an article. Use this method if only the store to string of the
	 * article is available.
	 * 
	 * @param articleStoreToString
	 * @param count
	 * @param mandatorId
	 * @return
	 */
	public IStatus performSingleDisposal(String articleStoreToString, int count, String mandatorId);

	/**
	 * Perform a single return of an article. The article will be returned to stock, where selection
	 * of the stock entry follows {@link #findPreferredStockEntryForArticle(String, String)}
	 * 
	 * @param article
	 * @param count
	 * @param mandatorId
	 * @return
	 */
	public IStatus performSingleReturn(IArticle article, int count, String mandatorId);

	/**
	 * Perform a single return of an article. Use this method if only the store to string of the
	 * article is available.
	 * 
	 * @param articleStoreToString
	 * @param count
	 * @param mandatorId
	 * @return
	 */
	public IStatus performSingleReturn(String articleStoreToString, int count, String mandatorId);
	
	/**
	 * Determine the availability "level".
	 * 
	 * @param current
	 *            the current amount of packages
	 * @param min
	 *            the minimum amount of packages to be on stock
	 * @param triggerOnIsBelow
	 *            checks if the current stock is below
	 * @return
	 */
	public default Availability determineAvailability(int current, int min,
		boolean triggerOnIsBelow){
		
		if (current <= 0) {
			return Availability.OUT_OF_STOCK;
		}

		if (triggerOnIsBelow && current >= min || !triggerOnIsBelow && current > min) {
			return Availability.IN_STOCK;
		}

		return Availability.CRITICAL_STOCK;
	}
	
	/**
	 * Determine the availability "level".
	 * 
	 * @param current
	 *            the current amount of packages
	 * @param min
	 *            the minimum amount of packages to be on stock
	 * @return
	 */
	public default Availability determineAvailability(int current, int min){
		return determineAvailability(current, min, false);
	}
	
	public default Availability determineAvailability(IStockEntry stockEntry){
		return determineAvailability(stockEntry.getCurrentStock(), stockEntry.getMinimumStock(),
			true);
	}
	
	public List<IStock> getAllStocks(boolean includeCommissioningSystems);
	
	public List<IStockEntry> getAllStockEntries();
	
	public IStock getDefaultStock();
}