package ch.elexis.core.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jpa.entities.Artikel;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.article.Constants;
import ch.elexis.core.model.billable.AbstractOptifier;
import ch.elexis.core.model.billable.DefaultVerifier;
import ch.elexis.core.model.service.holder.CoreModelServiceHolder;
import ch.elexis.core.model.util.ModelUtil;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.types.ArticleSubTyp;
import ch.elexis.core.types.ArticleTyp;
import ch.elexis.core.types.VatInfo;
import ch.rgw.tools.Money;

public class TypedArticle extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Artikel>
		implements IdentifiableWithXid, IArticle {

	private static IBillableVerifier verifier;

	private static IBillableOptifier<TypedArticle> optifier;

	public TypedArticle(Artikel entity) {
		super(entity);
	}

	@Override
	public String getCodeSystemName() {
		return getTyp().getCodeSystemName();
	}

	@Override
	public String getCodeSystemCode() {
		ArticleTyp typ = getTyp();
		if (typ == ArticleTyp.EIGENARTIKEL) {
			ArticleSubTyp subTyp = getSubTyp();
			if (subTyp == ArticleSubTyp.COMPLEMENTARY || subTyp == ArticleSubTyp.COMPLEMENTARY_REDUCED) {
				return "590";
			} else if (subTyp == ArticleSubTyp.ADDITIVE || subTyp == ArticleSubTyp.COVID) {
				return "406";
			} else if (subTyp == ArticleSubTyp.PHARMA) {
				return "402";
			} else if (subTyp == ArticleSubTyp.NONPHARMA) {
				return "406";
			} else if (subTyp == ArticleSubTyp.MAGISTERY) {
				return "410";
			} else if (subTyp == ArticleSubTyp.MIGEL) {
				return "452";
			}
		} else if (typ == ArticleTyp.MIGEL) {
			return "452";
		}
		return IArticle.super.getCodeSystemCode();
	}

	@Override
	public String getCode() {
		String ret = getEntity().getSubId();
		if (ret == null || ret.isEmpty()) {
			ret = getId();
		}
		return ret;
	}

	@Override
	public void setCode(String code) {
		getEntityMarkDirty().setSubId(code);
	}

	@Override
	public String getText() {
		String name = getEntity().getName();
		if (!isProduct()) {
			StringBuilder label = new StringBuilder();
			String packageSizeString = getPackageSizeString();
			if (StringUtils.isNotBlank(packageSizeString)) {
				label.append(packageSizeString + StringUtils.SPACE);
			} else {
				label.append(getPackageSize() + StringUtils.SPACE
						+ (getPackageUnit() != null ? getPackageUnit() : StringUtils.EMPTY));
			}
			return name + StringUtils.SPACE + label.toString();
		}
		return name;
	}

	@Override
	public void setText(String value) {
		getEntityMarkDirty().setName(value);
	}

	@Override
	public String getGtin() {
		return getEntity().getEan();
	}

	@Override
	public void setGtin(String value) {
		getEntityMarkDirty().setEan(value);
	}

	@Override
	public String getName() {
		return getEntity().getName();
	}

	@Override
	public void setName(String value) {
		getEntityMarkDirty().setName(value);
	}

	@Override
	public int getSellingSize() {
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
	public void setSellingSize(int value) {
		setExtInfo(Constants.FLD_EXT_SELL_UNIT, Integer.toString(value));
	}

	@Override
	public int getPackageSize() {
		String size = (String) getExtInfo(Constants.FLD_EXT_PACKAGE_UNIT_INT);
		if (size == null && getTyp() == ArticleTyp.EIGENARTIKEL) {
			size = (String) getExtInfo(ch.elexis.core.model.localarticle.Constants.FLD_EXT_PACKAGE_SIZE_STRING);
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
	public void setPackageSize(int value) {
		setExtInfo(Constants.FLD_EXT_PACKAGE_UNIT_INT, Integer.toString(value));
	}

	@Override
	public String getPackageUnit() {
		if (isTyp(ArticleTyp.EIGENARTIKEL)) {
			return (String) getExtInfo(ch.elexis.core.model.localarticle.Constants.FLD_EXT_MEASUREMENT_UNIT);
		} else if (isTyp(ArticleTyp.MIGEL)) {
			return (String) getExtInfo("unit");
		}
		return StringUtils.EMPTY;
	}

	@Override
	public void setPackageUnit(String value) {
		if (isTyp(ArticleTyp.EIGENARTIKEL)) {
			setExtInfo(ch.elexis.core.model.localarticle.Constants.FLD_EXT_MEASUREMENT_UNIT, value);
		} else if (isTyp(ArticleTyp.MIGEL)) {
			setExtInfo("unit", value);
		}
	}

	@Override
	public void setPackageSizeString(String value) {
		setExtInfo(ch.elexis.core.model.localarticle.Constants.FLD_EXT_PACKAGE_SIZE_STRING, value);
	}

	@Override
	public String getPackageSizeString() {
		return StringUtils.defaultString(
				(String) getExtInfo(ch.elexis.core.model.localarticle.Constants.FLD_EXT_PACKAGE_SIZE_STRING));
	}

	@Override
	public boolean isProduct() {
		if (isTyp(ArticleTyp.EIGENARTIKEL)) {
			return getEntity().getProduct() == null;
		}
		return false;
	}

	@Override
	public ArticleTyp getTyp() {
		return getEntity().getTyp() != null ? getEntity().getTyp() : ArticleTyp.ARTIKEL;
	}

	@Override
	public void setTyp(ArticleTyp value) {
		getEntityMarkDirty().setTyp(value);
	}

	@Override
	public ArticleSubTyp getSubTyp() {
		return ArticleSubTyp.byCharSafe(getEntity().getCodeclass());
	}

	@Override
	public void setSubTyp(ArticleSubTyp value) {
		getEntityMarkDirty().setCodeclass(Character.toString(value.getTypeChar()));
	}

	@Override
	public Object getExtInfo(Object key) {
		return extInfoHandler.getExtInfo(key);
	}

	@Override
	public void setExtInfo(Object key, Object value) {
		extInfoHandler.setExtInfo(key, value);
	}

	@Override
	public Map<Object, Object> getMap() {
		return extInfoHandler.getMap();
	}

	@Override
	public VatInfo getVatInfo() {
		if (isTyp(ArticleTyp.EIGENARTIKEL)) {
			ArticleSubTyp subTyp = getSubTyp();
			switch (subTyp) {
			case PHARMA:
			case MAGISTERY:
			case COMPLEMENTARY_REDUCED:
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

	private boolean isTyp(ArticleTyp typ) {
		return getTyp() == typ;
	}

	@Override
	public IArticle getProduct() {
		return ch.elexis.core.model.util.internal.ModelUtil.getAdapter(getEntity().getProduct(), IArticle.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setProduct(IArticle value) {
		if (value != null) {
			getEntityMarkDirty().setProduct(((AbstractIdModelAdapter<Artikel>) value).getEntity());
		} else {
			getEntityMarkDirty().setProduct(null);
		}
	}

	@Override
	public String getAtcCode() {
		return getEntity().getAtcCode();
	}

	@Override
	public void setAtcCode(String value) {
		getEntityMarkDirty().setAtcCode(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IArticle> getPackages() {
		IQuery<IArticle> query = ch.elexis.core.model.util.internal.ModelUtil.getQuery(IArticle.class);
		query.and(ModelPackage.Literals.IARTICLE__TYP, COMPARATOR.EQUALS, getTyp());
		query.and(ModelPackage.Literals.IARTICLE__PRODUCT, COMPARATOR.EQUALS, this);
		return (List<IArticle>) (List<?>) query.execute();
	}

	@Override
	public synchronized IBillableOptifier<TypedArticle> getOptifier() {
		if (optifier == null) {
			optifier = new AbstractOptifier<TypedArticle>(CoreModelServiceHolder.get(), ContextServiceHolder.get()) {

				@Override
				protected void setPrice(TypedArticle billable, IBilled billed) {
					billed.setFactor(1.0);
					billed.setNetPrice(billable.getPurchasePrice());
					Money sellingPrice = billable.getSellingPrice();

					int vkPreis = sellingPrice != null ? sellingPrice.getCents() : 0;
					double pkgSize = Math.abs(billable.getPackageSize());
					double vkUnits = billable.getSellingSize();
					if ((pkgSize > 0.0) && (vkUnits > 0.0) && (pkgSize != vkUnits)) {
						billed.setPoints((int) Math.round(vkUnits * (vkPreis / pkgSize)));
					} else {
						billed.setPoints((int) Math.round(vkPreis));
					}

				}

				@Override
				public Optional<IBillingSystemFactor> getFactor(IEncounter encounter) {
					return Optional.empty();
				}
			};
		}
		return optifier;
	}

	@Override
	public synchronized IBillableVerifier getVerifier() {
		if (verifier == null) {
			verifier = new DefaultVerifier();
		}
		return verifier;
	}

	@Override
	public Money getPurchasePrice() {
		String priceString = getEntity().getEkPreis();
		if (StringUtils.isNumeric(priceString)) {
			return ModelUtil.getMoneyForCentString(priceString).orElse(null);
		}
		return new Money();
	}

	@Override
	public void setPurchasePrice(Money value) {
		getEntityMarkDirty().setEkPreis((value != null) ? value.getCentsAsString() : null);
	}

	@Override
	public Money getSellingPrice() {
		String priceString = getEntity().getVkPreis();
		if (StringUtils.isNumeric(priceString)) {
			return ModelUtil.getMoneyForCentString(priceString).orElse(null);
		}
		return new Money();
	}

	@Override
	public void setSellingPrice(Money value) {
		getEntityMarkDirty().setVkPreis((value != null) ? value.getCentsAsString() : null);
	}

	@Override
	public boolean isObligation() {
		if (isTyp(ArticleTyp.EIGENARTIKEL)) {
			return Boolean.valueOf(
					(String) getExtInfo(ch.elexis.core.model.localarticle.Constants.FLD_EXT_HI_COST_ABSORPTION));
		}
		return false;
	}

	@Override
	public void setObligation(boolean value) {
		if (isTyp(ArticleTyp.EIGENARTIKEL)) {
			setExtInfo(ch.elexis.core.model.localarticle.Constants.FLD_EXT_HI_COST_ABSORPTION, Boolean.toString(value));
		}
	}

	@Override
	public String getLabel() {
		return getText();
	}

	@Override
	public boolean isVaccination() {
		String atcCode = getAtcCode();
		if (atcCode != null) {
			if (atcCode.toUpperCase().startsWith("J07") && !atcCode.toUpperCase().startsWith("J07AX")) {
				return true;
			}
		}
		return false;
	}
}
