/*******************************************************************************
 * Copyright (c) 2006-2016, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    M. Descher - extracted from elexis main and adapted for usage
 *    <office@medevit.at> - 3.2 format introduction (products and items)
 *******************************************************************************/

package ch.elexis.core.eigenartikel;

import static ch.elexis.core.model.article.Constants.FLD_EXT_SELL_UNIT;
import static ch.elexis.core.model.eigenartikel.Constants.FLD_EXT_HI_COST_ABSORPTION;
import static ch.elexis.core.model.eigenartikel.Constants.FLD_EXT_MEASUREMENT_UNIT;
import static ch.elexis.core.model.eigenartikel.Constants.FLD_EXT_PACKAGE_SIZE_STRING;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IOptifier;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.util.IRunnableWithProgress;
import ch.elexis.core.model.eigenartikel.Constants;
import ch.elexis.core.model.eigenartikel.EigenartikelTyp;
import ch.elexis.data.Artikel;
import ch.elexis.data.Konsultation;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;

public class Eigenartikel extends Artikel {
	
	private static IOptifier OPTIFIER;
	
	static {
		final String isConvertedTo32Key = "Eigenartikel32Format";
		boolean converted = CoreHub.globalCfg.get(isConvertedTo32Key, false);
		if (!converted) {
			log.info("Migrating Eigenartikel to v3.2");
			
			IRunnableWithProgress irwp = new IRunnableWithProgress() {
				
				@Override
				public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException{
					EigenartikelDatabaseConverter.performConversionTo32Format(monitor);
				}
			};
			PersistentObject.cod.showProgress(irwp);
			
			CoreHub.globalCfg.set(isConvertedTo32Key, true);
		}
		
		transferAllStockInformationToNew32StockModel(new Query<Eigenartikel>(Eigenartikel.class),
			Eigenartikel.class);
	}
	
	public static final String TYPNAME = Constants.TYPE_NAME;
	
	public Eigenartikel(final String articleName, final String productName){
		create(null);
		set(new String[] {
			FLD_NAME, EIGENNAME, FLD_TYP
		}, new String[] {
			articleName, productName, TYPNAME
		});
	}
	
	@Override
	protected String getConstraint(){
		return new StringBuilder(Artikel.FLD_TYP).append(Query.EQUALS)
			.append(JdbcLink.wrap(TYPNAME)).toString();
	}
	
	protected void setConstraint(){
		set(Artikel.FLD_TYP, TYPNAME);
	}
	
	@Override
	public String getCodeSystemName(){
		return TYPNAME;
	}
	
	@Override
	public String getCodeSystemCode(){
		if (getTyp() == EigenartikelTyp.COMPLEMENTARY) {
			return "590";
		} else if (getTyp() == EigenartikelTyp.ADDITIVE) {
			return "406";
		}
		return super.getCodeSystemCode();
	}
	
	@Override
	public String getLabel(){
		String name = get(Artikel.FLD_NAME);
		if (!isProduct()) {
			name += StringConstants.SPACE + getPackageSizeLabel();
		}
		return name;
	}
	
	@Override
	public String getCode(){
		String ret = get(Artikel.FLD_SUB_ID);
		if (ret == null || ret.isEmpty()) {
			ret = getId();
		}
		return ret;
	}
	
	public String getGroup(){
		return checkNull(get(Artikel.FLD_CODECLASS));
	}
	
	public static Eigenartikel load(String id){
		return new Eigenartikel(id);
	}
	
	protected Eigenartikel(){}
	
	protected Eigenartikel(String id){
		super(id);
	}
	
	@Override
	public boolean delete(){
		if (isProduct()) {
			List<Eigenartikel> packages = getPackages();
			for (Eigenartikel p : packages) {
				p.delete();
			}
		}
		return super.delete();
	}
	
	@Override
	public boolean isProduct(){
		String extId = get(FLD_EXTID);
		return (extId == null || extId.length() == 0);
	}
	
	@Override
	public boolean isDragOK(){
		return true;
	}
	
	@Override
	public String getInternalName(){
		return getLabel();
	}
	
	@Override
	public VatInfo getVatInfo(){
		EigenartikelTyp eat = getTyp();
		switch (eat) {
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
	
	/**
	 * Synchronizes a products description with is "child" packages and adds as package to product
	 * 
	 * @param product
	 * @param eaPackage
	 *            if <code>null</code> all current children are fetched and updated, if an
	 *            Eigenartikel is provided it is added as a package
	 */
	public static void copyProductAttributesToArticleSetAsChild(Eigenartikel product,
		Eigenartikel eaPackage){
		
		List<Eigenartikel> eaPackages = new ArrayList<Eigenartikel>();
		if (eaPackage != null) {
			eaPackages.add(eaPackage);
		} else {
			Query<Eigenartikel> qre = new Query<Eigenartikel>(Eigenartikel.class);
			qre.add(Eigenartikel.FLD_EXTID, Query.EQUALS, product.getId());
			eaPackages.addAll(qre.execute());
		}
		
		for (Eigenartikel ea : eaPackages) {
			String[] keys = new String[] {
				FLD_EXTID, FLD_CODECLASS, FLD_ATC_CODE, FLD_TYP, FLD_NAME,
			};
			String[] values = new String[] {
				product.getId(), product.get(FLD_CODECLASS), product.getATC_code(),
				product.get(FLD_TYP), product.getName()
			};
			ea.set(keys, values);
		}
	}
	
	public String getPackageSizeLabel(){
		if (getPackageSizeString() != null && getPackageSizeString().length() > 0) {
			return getPackageSizeString();
		} else if (getPackageSize() != null && getMeasurementUnit() != null) {
			return getPackageSize() + " " + getMeasurementUnit();
		} else if (getPackageSize() != null) {
			return Integer.toString(getPackageSize());
		}
		return StringConstants.EMPTY;
	}
	
	/**
	 * 
	 * @return all {@link Eigenartikel} packages belonging to this product, if {@link #isProduct()}
	 */
	public List<Eigenartikel> getPackages(){
		if (isProduct()) {
			Query<Eigenartikel> qbe = new Query<Eigenartikel>(Eigenartikel.class);
			qbe.add(Eigenartikel.FLD_EXTID, Query.EQUALS, getId());
			return qbe.execute();
		}
		return Collections.emptyList();
	}
	
	public EigenartikelTyp getTyp(){
		String value = get(Artikel.FLD_CODECLASS);
		return EigenartikelTyp.byCharSafe(value);
	}
	
	public void setTyp(EigenartikelTyp type){
		set(Artikel.FLD_CODECLASS, Character.toString(type.getTypeChar()));
	}
	
	/**
	 * 
	 * @param value
	 * @return a positive Integer (natural number) or <code>null</code>
	 */
	private Integer returnNaturalNumberIntegerOrNull(int value){
		if (value > 0) {
			return Integer.valueOf(value);
		}
		return null;
	}
	
	private int getNaturalIntOrZeroIfNull(Integer intVal){
		if (intVal == null) {
			return 0;
		}
		return intVal.intValue();
	}
	
	public Integer getPackageSize(){
		return returnNaturalNumberIntegerOrNull(getPackungsGroesse());
	}
	
	public void setPackageSize(Integer packageSize){
		setPackungsGroesse(getNaturalIntOrZeroIfNull(packageSize));
	}
	
	public void setPackageSizeString(String pss){
		setExtInfoStoredObjectByKey(FLD_EXT_PACKAGE_SIZE_STRING, pss);
	}
	
	public String getPackageSizeString(){
		return (String) getExtInfoStoredObjectByKey(FLD_EXT_PACKAGE_SIZE_STRING);
	}
	
	public String getSellUnit(){
		return (String) getExtInfoStoredObjectByKey(FLD_EXT_SELL_UNIT);
	}
	
	public void setSellUnit(String sellUnit){
		setExtInfoStoredObjectByKey(FLD_EXT_SELL_UNIT, sellUnit);
	}
	
	public void setMeasurementUnit(String mu){
		setExtInfoStoredObjectByKey(FLD_EXT_MEASUREMENT_UNIT, mu);
	}
	
	public String getMeasurementUnit(){
		return (String) checkNull(getExtInfoStoredObjectByKey(FLD_EXT_MEASUREMENT_UNIT));
	}
	
	public String getExfPrice(){
		return getEKPreis().getCentsAsString();
	}
	
	public void setExfPrice(String exfPrice){
		Money m = new Money();
		m.addCent(exfPrice);
		setEKPreis(m);
	}
	
	@Override
	public String getPharmaCode(){
		return get(FLD_SUB_ID);
	}
	
	@Override
	public void setPharmaCode(String pharmacode){
		set(FLD_SUB_ID, pharmacode);
	}
	
	public String getPubPrice(){
		return getVKPreis().getCentsAsString();
	}
	
	public void setPubPrice(String pubPrice){
		Money m = new Money();
		m.addCent(pubPrice);
		setVKPreis(m);
	}
	
	public boolean isHealthInsuranceCostAbsorption(){
		return Boolean.valueOf((String) getExtInfoStoredObjectByKey(FLD_EXT_HI_COST_ABSORPTION));
	}
	
	public void setHealthInsuranceCostAbsorption(boolean hica){
		setExtInfoStoredObjectByKey(FLD_EXT_HI_COST_ABSORPTION, Boolean.toString(hica));
	}
	
	public boolean isValidPackage(){
		String extId = get(FLD_EXTID);
		return (Eigenartikel.load(extId).isValid());
	}
	
	@Override
	public IOptifier getOptifier(){
		if (OPTIFIER == null) {
			OPTIFIER = new DefaultOptifier() {
				@Override
				public Result<IVerrechenbar> add(IVerrechenbar code, Konsultation kons){
					boolean valid = true;
					// test VVG if is typ EigenartikelTyp.COMPLEMENTARY
					if (code instanceof Eigenartikel) {
						Eigenartikel article = (Eigenartikel) code;
						if (article.getTyp() == EigenartikelTyp.COMPLEMENTARY) {
							String gesetz = kons.getFall().getConfiguredBillingSystemLaw().name();
							String system = kons.getFall().getAbrechnungsSystem();
							if (gesetz.isEmpty()) {
								if (!"vvg".equalsIgnoreCase(system)) {
									valid = false;
								}
							} else {
								if (!"vvg".equalsIgnoreCase(gesetz)) {
									valid = false;
								}
							}
						}
					}
					return valid ? super.add(code, kons)
							: new Result<IVerrechenbar>(Result.SEVERITY.WARNING, 0,
								"Komplementärmedizinische Artikel können nur auf eine Fall mit Gesetz oder Name VVG verrechnet werden.",
								null, false);
				}
			};
		}
		return OPTIFIER;
	}
}
