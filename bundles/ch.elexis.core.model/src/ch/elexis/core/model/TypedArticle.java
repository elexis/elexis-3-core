package ch.elexis.core.model;

import java.util.List;

import ch.elexis.core.jpa.entities.Artikel;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.ExtInfoHandler;
import ch.elexis.core.jpa.model.adapter.mixin.IdentifiableWithXid;
import ch.elexis.core.model.article.Constants;
import ch.elexis.core.model.util.ModelUtil;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.types.ArticleSubTyp;
import ch.elexis.core.types.ArticleTyp;
import ch.elexis.core.types.VatInfo;

public class TypedArticle extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Artikel>
		implements IdentifiableWithXid, IArticle {
	
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
			ArticleSubTyp subTyp = getSubTyp();
			if (subTyp == ArticleSubTyp.COMPLEMENTARY) {
				return "590";
			} else if (subTyp == ArticleSubTyp.ADDITIVE) {
				return "406";
			}
		}
		return IArticle.super.getCodeSystemCode();
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
	public void setText(String value){
		getEntity().setName(value);
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
	public int getSellingSize(){
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
	public void setSellingSize(int value){
		setExtInfo(Constants.FLD_EXT_SELL_UNIT, Integer.toString(value));
	}
	

	@Override
	public int getPackageSize(){
		String size = (String) getExtInfo(Constants.FLD_EXT_PACKAGE_UNIT_INT);
		if (size == null && getTyp() == ArticleTyp.EIGENARTIKEL) {
			size = (String) getExtInfo(
				ch.elexis.core.model.eigenartikel.Constants.FLD_EXT_PACKAGE_SIZE_STRING);
		}
		if (size != null) {
			try {
				return Integer.parseInt(size);
			} catch (NumberFormatException e) {
				// ignore and return 1
			}
		}
		return 1;
	}
	
	@Override
	public void setPackageSize(int value){
		setExtInfo(Constants.FLD_EXT_PACKAGE_UNIT_INT, Integer.toString(value));
	}
	
	@Override
	public String getPackageUnit(){
		if (isTyp(ArticleTyp.EIGENARTIKEL)) {
			return (String) getExtInfo(
				ch.elexis.core.model.eigenartikel.Constants.FLD_EXT_MEASUREMENT_UNIT);
		}
		return "";
	}
	
	@Override
	public void setPackageUnit(String value){
		if (isTyp(ArticleTyp.EIGENARTIKEL)) {
			setExtInfo(ch.elexis.core.model.eigenartikel.Constants.FLD_EXT_MEASUREMENT_UNIT, value);
		}
	}
	
	@Override
	public boolean isProduct(){
		if (isTyp(ArticleTyp.EIGENARTIKEL)) {
			return getEntity().getProduct() == null;
		}
		return false;
	}
	
	@Override
	public ArticleTyp getTyp(){
		return getEntity().getTyp() != null ? getEntity().getTyp() : ArticleTyp.ARTIKEL;
	}
	
	@Override
	public void setTyp(ArticleTyp value){
		getEntity().setTyp(value);
	}
	
	@Override
	public ArticleSubTyp getSubTyp(){
		return ArticleSubTyp.byCharSafe(getEntity().getCodeclass());
	}
	
	@Override
	public void setSubTyp(ArticleSubTyp value){
		getEntity().setCodeclass(Character.toString(value.getTypeChar()));
	}
	
	@Override
	public Object getExtInfo(Object key){
		return extInfoHandler.getExtInfo(key);
	}
	
	@Override
	public void setExtInfo(Object key, Object value){
		extInfoHandler.setExtInfo(key, value);
	}
	
	@Override
	public VatInfo getVatInfo(){
		if (isTyp(ArticleTyp.EIGENARTIKEL)) {
			ArticleSubTyp subTyp = getSubTyp();
			switch (subTyp) {
			case PHARMA:
			case MAGISTERY:
				return VatInfo.VAT_CH_ISMEDICAMENT;
			case NONPHARMA:
			case COMPLEMENTARY:
				return VatInfo.VAT_CH_NOTMEDICAMENT;
			default:
				break;
			}
			return VatInfo.VAT_NONE;
		}
		return VatInfo.VAT_NONE;
	}
	
	private boolean isTyp(ArticleTyp typ){
		return getTyp() == typ;
	}
	
	@Override
	public IArticle getProduct(){
		return ModelUtil.getAdapter(getEntity().getProduct(), IArticle.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setProduct(IArticle value){
		if(value != null) {
			getEntity().setProduct(((AbstractIdModelAdapter<Artikel>) value).getEntity());
		} else {
			getEntity().setProduct(null);
		}
	}
	
	@Override
	public String getAtcCode(){
		return getEntity().getAtcCode();
	}
	
	@Override
	public void setAtcCode(String value){
		getEntity().setAtcCode(value);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<IArticle> getPackages(){
		IQuery<IArticle> query = ModelUtil.getQuery(IArticle.class);
		query.and(ModelPackage.Literals.IARTICLE__TYP, COMPARATOR.EQUALS, getTyp());
		query.and(ModelPackage.Literals.IARTICLE__PRODUCT, COMPARATOR.EQUALS, this);
		return (List<IArticle>) (List<?>) query.execute();
	}
	
	@Override
	public IBillableOptifier getOptifier(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public IBillableVerifier getVerifier(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getPurchasePrice(){
		return getEntity().getEkPreis();
	}
	
	@Override
	public void setPurchasePrice(String value){
		getEntity().setEkPreis(value);
	}
	
	@Override
	public String getSellingPrice(){
		return getEntity().getVkPreis();
	}
	
	@Override
	public void setSellingPrice(String value){
		getEntity().setVkPreis(value);
	}
	
	@Override
	public boolean isObligation(){
		if (isTyp(ArticleTyp.EIGENARTIKEL)) {
			return Boolean.valueOf((String) getExtInfo(
				ch.elexis.core.model.eigenartikel.Constants.FLD_EXT_HI_COST_ABSORPTION));
		}
		return false;
	}
	
	@Override
	public void setObligation(boolean value){
		if (isTyp(ArticleTyp.EIGENARTIKEL)) {
			setExtInfo(ch.elexis.core.model.eigenartikel.Constants.FLD_EXT_HI_COST_ABSORPTION,
				Boolean.toString(value));
		}
	}
	
	@Override
	public String getLabel(){
		return getName();
	}
}
