package ch.elexis.core.model;

import ch.elexis.core.jpa.entities.Artikel;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.article.Constants;
import ch.elexis.core.model.eigenartikel.EigenartikelTyp;
import ch.elexis.core.model.mixin.ExtInfoHandler;
import ch.elexis.core.model.mixin.IdentifiableWithXid;
import ch.elexis.core.types.ArticleTyp;

public class TypedArticle extends AbstractIdModelAdapter<ch.elexis.core.jpa.entities.Artikel>
		implements IdentifiableWithXid, ITypedArticle {
	
	private ExtInfoHandler extInfoHandler;
	
	public TypedArticle(Artikel entity){
		super(entity);
		extInfoHandler = new ExtInfoHandler(this);
	}
	
	@Override
	public String getCodeSystemName(){
		return getTyp().getCodeSystemName();
	}
	
	@Override
	public String getCodeSystemCode(){
		ArticleTyp typ = getTyp();
		if (typ == ArticleTyp.EIGENARTIKEL) {
			EigenartikelTyp subTyp = EigenartikelTyp.byCharSafe(getSubTyp());
			if (subTyp == EigenartikelTyp.COMPLEMENTARY) {
				return "590";
			} else if (subTyp == EigenartikelTyp.ADDITIVE) {
				return "406";
			}
		}
		return ITypedArticle.super.getCodeSystemCode();
	}
	
	@Override
	public String getCode(){
		String ret = getEntity().getSubId();
		if (ret == null || ret.isEmpty()) {
			ret = getId();
		}
		return ret;
	}
	
	@Override
	public void setCode(String code){
		getEntity().setSubId(code);
	}
	
	@Override
	public String getText(){
		return getEntity().getName();
	}
	
	@Override
	public String getGtin(){
		return getEntity().getEan();
	}
	
	@Override
	public void setGtin(String value){
		getEntity().setEan(value);
	}
	
	@Override
	public String getName(){
		return getEntity().getName();
	}
	
	@Override
	public void setName(String value){
		getEntity().setName(value);
	}
	
	@Override
	public int getSellingUnit(){
		String unit = (String) getExtInfo(Constants.FLD_EXT_SELL_UNIT);
		if (unit != null) {
			try {
				return Integer.parseInt(unit);
			} catch (NumberFormatException e) {
				// ignore and return 1
			}
		}
		return 1;
	}
	
	@Override
	public void setSellingUnit(int value){
		setExtInfo(Constants.FLD_EXT_SELL_UNIT, Integer.toString(value));
	}
	
	@Override
	public int getPackageUnit(){
		String unit = (String) getExtInfo(Constants.FLD_EXT_PACKAGE_UNIT_INT);
		if (unit == null && getTyp() == ArticleTyp.EIGENARTIKEL) {
			unit = (String) getExtInfo(
				ch.elexis.core.model.eigenartikel.Constants.FLD_EXT_PACKAGE_SIZE_STRING);
		}
		if (unit != null) {
			try {
				return Integer.parseInt(unit);
			} catch (NumberFormatException e) {
				// ignore and return 1
			}
		}
		return 1;
	}
	
	@Override
	public void setPackageUnit(int value){
		setExtInfo(Constants.FLD_EXT_PACKAGE_UNIT_INT, Integer.toString(value));
	}
	
	@Override
	public boolean isProduct(){
		String extId = getEntity().getExtId();
		return (extId == null || extId.length() == 0);
	}
	
	@Override
	public ArticleTyp getTyp(){
		return getEntity().getTyp();
	}
	
	@Override
	public void setTyp(ArticleTyp value){
		getEntity().setTyp(value);
	}
	
	@Override
	public String getSubTyp(){
		return getEntity().getCodeclass();
	}
	
	@Override
	public void setSubTyp(String value){
		getEntity().setCodeclass(value);
	}
	
	@Override
	public Object getExtInfo(Object key){
		return extInfoHandler.getExtInfo(key);
	}
	
	@Override
	public void setExtInfo(Object key, Object value){
		extInfoHandler.setExtInfo(key, value);
	}
}
