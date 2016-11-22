package ch.elexis.core.common;

public class ElexisEventTopics {

	public static final String TOPIC_BASE = "info/elexis/";

	public static final String TOPIC_BASE_STOCK_COMMISSIONING = TOPIC_BASE + "stockCommissioning/";

	public static final String TOPIC_STOCK_COMMISSIONING_OUTLAY = TOPIC_BASE_STOCK_COMMISSIONING + "outlay";
	public static final String TOPIC_STOCK_COMMISSIONING_PROPKEY_STOCKENTRY_ID = "stockEntryId";
	public static final String TOPIC_STOCK_COMMISSIONING_PROPKEY_QUANTITY = "quantity";

	public static final String TOPIC_STOCK_COMMISSIONING_UPDATE_STOCK = TOPIC_BASE_STOCK_COMMISSIONING + "updateStock";
	public static final String TOPIC_STOCK_COMMISSIONING_PROPKEY_ARTICLE_ID = "articleId";
}
