package ch.elexis.data;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IStockEntry;
import ch.elexis.core.services.holder.ConfigServiceHolder;

/**
 * 
 * @since 3.2
 */
public class StockEntry extends PersistentObject implements IStockEntry {
	
	public static final String TABLENAME = "STOCK_ENTRY";
	
	public static final String FLD_STOCK = "STOCK";
	public static final String FLD_ARTICLE_TYPE = "ARTICLE_TYPE";
	public static final String FLD_ARTICLE_ID = "ARTICLE_ID";
	public static final String FLD_MIN = "MIN";
	public static final String FLD_CURRENT = "CURRENT";
	public static final String FLD_MAX = "MAX";
	public static final String FLD_FRACTIONUNITS = "FRACTIONUNITS";
	public static final String FLD_PROVIDER = "PROVIDER";
	
	static {
		addMapping(TABLENAME, FLD_STOCK, FLD_ARTICLE_TYPE, FLD_ARTICLE_ID,
			FLD_MIN + "=S:N:" + FLD_MIN, FLD_CURRENT + "=S:N:" + FLD_CURRENT,
			FLD_MAX + "=S:N:" + FLD_MAX, FLD_FRACTIONUNITS + "=S:N:" + FLD_FRACTIONUNITS,
			FLD_PROVIDER);
	}
	
	protected StockEntry(){}
	
	protected StockEntry(final String id){
		super(id);
	}
	
	public StockEntry(Stock stock, PersistentObject article){
		String provider =
			ConfigServiceHolder.getGlobal(Preferences.INVENTORY_DEFAULT_ARTICLE_PROVIDER, null);
		String[] fields = new String[] {
			FLD_STOCK, FLD_ARTICLE_TYPE, FLD_ARTICLE_ID, FLD_PROVIDER
		};
		String[] values = new String[] {
			stock.getId(), article.getClass().getName(), article.getId(),
			(provider != null) ? provider : null
		};
		create(null, fields, values);
	}
	
	public static StockEntry load(final String id){
		return new StockEntry(id);
	}
	
	@Override
	public boolean isDragOK(){
		return true;
	}
	
	@Override
	public String getLabel(){
		return null;
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	@Override
	public Artikel getArticle(){
		String[] vals = get(false, FLD_ARTICLE_TYPE, FLD_ARTICLE_ID);
		if (StringUtils.isNotBlank(vals[1])) {
			return (Artikel) CoreHub.poFactory
				.createFromString(vals[0] + StringConstants.DOUBLECOLON + vals[1]);
		}
		return null;
	}
	
	public void setArticle(Artikel article){
		if (article != null) {
			set(new String[] {
				FLD_ARTICLE_TYPE, FLD_ARTICLE_ID
			}, article.getClass().getName(), article.getId());
		} else {
			set(new String[] {
				FLD_ARTICLE_TYPE, FLD_ARTICLE_ID
			}, null, null);
		}
	}
	
	@Override
	public Stock getStock(){
		return Stock.load(get(FLD_STOCK));
	}
	
	public void setStock(Stock stock){
		set(FLD_STOCK, stock.getId());
	}
	
	@Override
	public int getCurrentStock(){
		return getInt(FLD_CURRENT);
	}
	
	@Override
	public void setCurrentStock(int currentStock){
		// ACHTUNG ConfigServiceHolder.getGlobal(Preferences.INVENTORY_CHECK_ILLEGAL_VALUES
		/**
		 * Istbestand setzen. Wenn INVENTORY_CHECK_ILLEGAL-VALUES gesetzt ist, erscheint eine
		 * Warnung, wenn der Istbestand unter null komt.
		 * 
		 * @param s
		 *            Wieviele Packungen tatsächlich auf Lager sind
		 */
		setInt(FLD_CURRENT, currentStock);
	}
	
	@Override
	public int getMinimumStock(){
		return getInt(FLD_MIN);
	}
	
	@Override
	public int getMaximumStock(){
		return getInt(FLD_MAX);
	}
	
	@Override
	public int getFractionUnits(){
		return getInt(FLD_FRACTIONUNITS);
	}
	
	@Override
	public void setFractionUnits(int rest){
		setInt(FLD_FRACTIONUNITS, rest);
	}
	
	@Override
	public void setMinimumStock(int minStock){
		setInt(FLD_MIN, minStock);
	}
	
	@Override
	public void setMaximumStock(int maxStock){
		setInt(FLD_MAX, maxStock);
	}
	
	@Override
	public Kontakt getProvider(){
		String providerId = get(FLD_PROVIDER);
		if (StringUtils.isNotBlank(providerId)) {
			return Kontakt.load(providerId);
		}
		return null;
	}
	
	@Override
	public void setProvider(Object provider){
		if (provider instanceof Kontakt) {
			set(FLD_PROVIDER, ((Kontakt) provider).getId());
		}
		
	}
}
