package ch.elexis.data;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IOrder;
import ch.elexis.core.data.interfaces.IOrderEntry;
import ch.elexis.core.jdt.Nullable;

/**
 * @since 3.2
 */
public class BestellungEntry extends PersistentObject implements IOrderEntry {
	
	public static final String TABLENAME = "BESTELLUNG_ENTRY";
	
	public static final String FLD_BESTELLUNG = "BESTELLUNG";
	public static final String FLD_ARTICLE_TYPE = "ARTICLE_TYPE";
	public static final String FLD_ARTICLE_ID = "ARTICLE_ID";
	public static final String FLD_STOCK = "STOCK";
	public static final String FLD_STATE = "STATE";
	public static final String FLD_COUNT = "COUNT";
	public static final String FLD_PROVIDER = "PROVIDER";
	
	public static final int STATE_OPEN = 0;
	public static final int STATE_ORDERED = 1;
	public static final int STATE_PARTIAL_DELIVER = 2;
	public static final int STATE_DONE = 3;
	
	static {
		addMapping(TABLENAME, FLD_BESTELLUNG, FLD_ARTICLE_TYPE, FLD_ARTICLE_ID, FLD_STOCK,
			FLD_STATE + "=S:N:" + FLD_STATE, FLD_COUNT + "=S:N:" + FLD_COUNT, FLD_PROVIDER);
	}
	
	protected BestellungEntry(){}
	
	protected BestellungEntry(final String id){
		super(id);
	}
	
	public static BestellungEntry load(final String id){
		return new BestellungEntry(id);
	}
	
	public BestellungEntry(Bestellung order, Artikel article, @Nullable Stock stock,
		@Nullable Kontakt provider, int count){
		String[] fields = new String[] {
			FLD_BESTELLUNG, FLD_ARTICLE_TYPE, FLD_ARTICLE_ID, FLD_PROVIDER, FLD_STOCK
		};
		String[] values = new String[] {
			order.getId(), article.getClass().getName(), article.getId(),
			(provider != null) ? provider.getId() : null, (stock != null) ? stock.getId() : null
		};
		create(null, fields, values);
		set(FLD_COUNT, Integer.toString(count));
	}
	
	@Override
	public String getLabel(){
		return toString();
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	public int getCount(){
		return getInt(FLD_COUNT);
	}
	
	public void setCount(int count){
		setInt(FLD_COUNT, count);
	}
	
	public void setState(int state){
		setInt(FLD_STATE, state);
	}
	
	public int getState(){
		return getInt(FLD_STATE);
	}
	
	public Artikel getArticle(){
		String[] vals = get(false, FLD_ARTICLE_TYPE, FLD_ARTICLE_ID);
		if (StringUtils.isNotBlank(vals[1])) {
			return (Artikel) CoreHub.poFactory
				.createFromString(vals[0] + StringConstants.DOUBLECOLON + vals[1]);
		}
		return null;
	}
	
	public Kontakt getProvider(){
		String providerId = get(FLD_PROVIDER);
		if (StringUtils.isNotBlank(providerId)) {
			return Kontakt.load(providerId);
		}
		return null;
	}
	
	@Override
	public Stock getStock(){
		String stockId = get(FLD_STOCK);
		if (StringUtils.isNotBlank(stockId)) {
			return Stock.load(stockId);
		}
		return null;
	}
	
	public void setProvider(Kontakt provider){
		if (provider == null) {
			set(FLD_PROVIDER, null);
		} else {
			set(FLD_PROVIDER, provider.getId());
		}
		
	}
	
	@Override
	public IOrder getOrder(){
		String orderId = get(FLD_BESTELLUNG);
		if (StringUtils.isNotBlank(orderId)) {
			return Bestellung.load(orderId);
		}
		return null;
	}
}
