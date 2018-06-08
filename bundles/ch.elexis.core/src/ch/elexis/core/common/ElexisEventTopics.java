package ch.elexis.core.common;

public class ElexisEventTopics {
	
	public static final String BASE = "info/elexis/";
	
	public static final String PROPKEY_ID = "id";
	public static final String PROPKEY_CLASS = "class";
	public static final String PROPKEY_USER = "user";
	public static final String PROPKEY_OBJ = "object";
	
	/**
	 * Topics concerning persistent object events
	 */
	public static final String PERSISTENCE_EVENT = BASE + "po/";
	/**
	 * A persistent object was added
	 */
	public static final String PERSISTENCE_EVENT_CREATE = PERSISTENCE_EVENT + "create";
	
	/**
	 * Topics concerning stock commissioning systems
	 */
	public static final String BASE_STOCK_COMMISSIONING = BASE + "stockCommissioning/";
	public static final String STOCK_COMMISSIONING_OUTLAY = BASE_STOCK_COMMISSIONING + "outlay";
	public static final String STOCK_COMMISSIONING_PROPKEY_STOCKENTRY_ID = "stockEntryId";
	public static final String STOCK_COMMISSIONING_PROPKEY_QUANTITY = "quantity";
	
	/**
	 * Perform a stock count (inventory) of the articles in the respective stock, or all if
	 * {@link #STOCK_COMMISSIONING_PROPKEY_LIST_ARTICLE_ID} not provided
	 */
	public static final String STOCK_COMMISSIONING_SYNC_STOCK =
		BASE_STOCK_COMMISSIONING + "updateStock";
	/**
	 * List<String> of article identifiers
	 */
	public static final String STOCK_COMMISSIONING_PROPKEY_LIST_ARTICLE_ID = "articleIds";
	/**
	 * The ID of the stock the request is targeted to
	 */
	public static final String STOCK_COMMISSIONING_PROPKEY_STOCK_ID = "stockId";
}
