package ch.elexis.core.data.nopo.adapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.model.IArticle;
import ch.elexis.data.Artikel;
import ch.rgw.tools.Money;

public class ArtikelAdapter extends Artikel {
	
	private IArticle article;
	
	public ArtikelAdapter(IArticle article){
		this.article = article;
	}
	
	@Override
	public String getId(){
		return article.getId();
	}
	
	@Override
	public int getAbgabeEinheit(){
		return article.getSellingSize();
	}
	
	@Override
	public String getCode(){
		return article.getCode();
	}
	
	@Override
	public String getCodeSystemCode(){
		return article.getCodeSystemCode();
	}
	
	@Override
	public String getCodeSystemName(){
		return article.getCodeSystemName();
	}
	
	@Override
	public String getName(){
		return article.getName();
	}
	
	@Override
	public String getPharmaCode(){
		String ret = "";
		try {
			Method method = article.getClass().getMethod("getPHAR");
			ret = (String) method.invoke(article);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// ignore no pharmacode available ...
		}
		if (StringUtils.isBlank(ret)) {
			Object value = article.getExtInfo(FLD_PHARMACODE);
			if (value instanceof String && ((String) value).length() == 7) {
				ret = (String) value;
			}
		}
		return ret;
	}
	
	@Override
	public String getEAN(){
		return article.getGtin();
	}
	
	@Override
	public String getGTIN(){
		return article.getGtin();
	}
	
	@Override
	public String getATC_code(){
		return article.getAtcCode();
	}
	
	@Override
	public int getPackungsGroesse(){
		return article.getPackageSize();
	}
	
	@Override
	public boolean isProduct(){
		return article.isProduct();
	}
	
	@Override
	public Money getVKPreis(){
		return article.getSellingPrice();
	}
	
	@Override
	public String getPackungsGroesseDesc(){
		return article.getPackageUnit();
	}
	
	@Override
	public String get(String field){
		if (Artikel.FLD_NAME.equals(field)) {
			return getName();
		}
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getLabel(){
		return article.getLabel();
	};
	
	@Override
	public String storeToString(){
		return StoreToStringServiceHolder.getStoreToString(article);
	}
	
	@Override
	public boolean set(String field, String value){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean set(String[] fields, String... values){
		throw new UnsupportedOperationException();
	}
}
