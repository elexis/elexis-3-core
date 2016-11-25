package ch.elexis.core.common;

public class ElexisEventTopics {

	public static final String TOPIC_BASE = "info/elexis/";

	public static final String TOPIC_BASE_STOCK_COMMISSIONING = TOPIC_BASE + "stockCommissioning/";

	public static final String TOPIC_STOCK_COMMISSIONING_OUTLAY = TOPIC_BASE_STOCK_COMMISSIONING + "outlay";
	public static final String TOPIC_STOCK_COMMISSIONING_PROPKEY_STOCKENTRY_ID = "stockEntryId";
	public static final String TOPIC_STOCK_COMMISSIONING_PROPKEY_QUANTITY = "quantity";

	/**
	 * Perform a stock count (inventory) of the articles in the respective
	 * stock, or all if
	 * {@link #TOPIC_STOCK_COMMISSIONING_PROPKEY_LIST_ARTICLE_ID} not provided
	 */
	public static final String TOPIC_STOCK_COMMISSIONING_SYNC_STOCK = TOPIC_BASE_STOCK_COMMISSIONING + "updateStock";
	/**
	 * List<String> of article identifiers
	 */
	public static final String TOPIC_STOCK_COMMISSIONING_PROPKEY_LIST_ARTICLE_ID = "articleIds";
	/**
	 * The ID of the stock the request is targeted to
	 */
	public static final String TOPIC_STOCK_COMMISSIONING_PROPKEY_STOCK_ID = "stockId";
}
