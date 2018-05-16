package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.Convert;

@Entity
@Table(name = "artikelstamm_ch")
public class ArtikelstammItem extends AbstractDBObjectIdDeletedExtInfo {
	public static final String CODESYSTEM_NAME = "Artikelstamm";

	@Column(length = 1)
	private String type;

	@Column(length = 1)
	private String bb;

	@Column(name = "CUMM_VERSION", length = 4)
	private String cummVersion;

	@Column(length = 14)
	private String gtin;

	@Column(length = 7)
	private String phar;

	@Column(length = 50)
	private String dscr;

	@Column(length = 50)
	private String adddscr;

	@Column(length = 10)
	private String atc;

	@Column(length = 13)
	private String comp_gln;

	@Column(length = 255)
	private String comp_name;

	@Column(length = 10)
	private String pexf;

	/**
	 * user-defined prices are stored as negative value, hence Math.abs should be
	 * applied for billing
	 */
	@Column(length = 10)
	private String ppub;

	@Convert(value = "IntegerStringConverter")
	private int pkg_size = 0;

	@Convert(value = "booleanStringConverter")
	private boolean sl_entry;

	@Column(length = 1)
	private String ikscat;

	@Convert(value = "booleanStringConverter")
	private boolean limitation = false;

	// spell error in creating table :(
	@Column(length = 4, name = "limitiation_pts")
	private String limitation_pts;

	@Column
	@Lob
	private String limitation_txt;

	@Column(length = 1)
	private String generic_type;

	@Convert(value = "booleanStringConverter")
	private boolean has_generic;

	@Convert(value = "booleanStringConverter")
	private boolean lppv;

	@Column(length = 6)
	private String deductible;

	@Convert(value = "booleanStringConverter")
	private boolean narcotic;

	@Column(length = 20)
	private String narcotic_cas;

	@Column(length = 1)
	private String vaccine;

	@Convert(value = "IntegerStringConverter")
	private int verkaufseinheit;

	@Column(length = 10)
	private String prodno;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBb() {
		return bb;
	}

	public void setBb(String bb) {
		this.bb = bb;
	}

	public String getCummVersion() {
		return cummVersion;
	}

	public void setCummVersion(String cummVersion) {
		this.cummVersion = cummVersion;
	}

	@Transient
	public String getGTIN() {
		return getGtin();
	};

	public String getGtin() {
		return gtin;
	}

	public void setGtin(String gtin) {
		this.gtin = gtin;
	}

	public String getPhar() {
		return phar;
	}

	public void setPhar(String phar) {
		this.phar = phar;
	}

	public String getDscr() {
		return dscr;
	}

	public void setDscr(String dscr) {
		this.dscr = dscr;
	}

	public String getAdddscr() {
		return adddscr;
	}

	public void setAdddscr(String adddscr) {
		this.adddscr = adddscr;
	}

	public String getAtc() {
		return atc;
	}

	public void setAtc(String atc) {
		this.atc = atc;
	}

	public String getComp_gln() {
		return comp_gln;
	}

	public void setComp_gln(String comp_gln) {
		this.comp_gln = comp_gln;
	}

	public String getComp_name() {
		return comp_name;
	}

	public void setComp_name(String comp_name) {
		this.comp_name = comp_name;
	}

	public String getPexf() {
		return pexf;
	}

	public void setPexf(String pexf) {
		this.pexf = pexf;
	}

	public String getPpub() {
		return ppub;
	}

	public void setPpub(String ppub) {
		this.ppub = ppub;
	}

	public int getPkg_size() {
		return pkg_size;
	}

	public void setPkg_size(int pkg_size) {
		this.pkg_size = pkg_size;
	}

	public String getIkscat() {
		return ikscat;
	}

	public boolean isLimitation() {
		return limitation;
	}

	public void setLimitation(boolean limitation) {
		this.limitation = limitation;
	}

	public boolean isLppv() {
		return lppv;
	}

	public boolean isSl_entry() {
		return sl_entry;
	}

	public void setSl_entry(boolean sl_entry) {
		this.sl_entry = sl_entry;
	}

	public void setLppv(boolean lppv) {
		this.lppv = lppv;
	}

	public void setIkscat(String ikscat) {
		this.ikscat = ikscat;
	}

	public String getLimitation_pts() {
		return limitation_pts;
	}

	public void setLimitation_pts(String limitation_pts) {
		this.limitation_pts = limitation_pts;
	}

	public String getLimitation_txt() {
		return limitation_txt;
	}

	public void setLimitation_txt(String limitation_txt) {
		this.limitation_txt = limitation_txt;
	}

	public String getGeneric_type() {
		return generic_type;
	}

	public void setGeneric_type(String generic_type) {
		this.generic_type = generic_type;
	}

	public String getDeductible() {
		return deductible;
	}

	public void setDeductible(String deductible) {
		this.deductible = deductible;
	}

	public String getNarcotic_cas() {
		return narcotic_cas;
	}

	public void setNarcotic_cas(String narcotic_cas) {
		this.narcotic_cas = narcotic_cas;
	}

	public String getVaccine() {
		return vaccine;
	}

	public void setVaccine(String vaccine) {
		this.vaccine = vaccine;
	}

	public int getVerkaufseinheit() {
		return verkaufseinheit;
	}

	public void setVerkaufseinheit(int verkaufseinheit) {
		this.verkaufseinheit = verkaufseinheit;
	}

	public String getProdno() {
		return prodno;
	}

	public void setProdno(String prodno) {
		this.prodno = prodno;
	}

	@Override
	public String toString() {
		return super.toString() + "name=[" + getDscr() + "]";
	}

	public String getLabel() {
		return (getAdddscr() != null && getAdddscr().length() > 0) ? getDscr() + " (" + getAdddscr() + ")" : getDscr();
	}

	//	@Transient
	//	@Override
	//	public int getPackageUnit() {
	//		return getPkg_size();
	//	}
	//
	//	@Transient
	//	@Override
	//	public int getSellingUnit() {
	//		return getVerkaufseinheit();
	//	}
	//
	//	@Transient
	//	@Override
	//	public boolean isProduct() {
	//		return (TYPE.X == getTYPE());
	//	}
	//
	//	@Transient
	//	@Override
	//	public String getName() {
	//		return getDscr();
	//	}
	//
	//	@Transient
	//	public TYPE getTYPE() {
	//		try {
	//			String string = getType();
	//			if (string != null && string.length() > 0) {
	//				return Enum.valueOf(TYPE.class, Character.toString(string.charAt(0)).toUpperCase());
	//			}
	//			return TYPE.N;
	//		} catch (IllegalArgumentException iae) {
	//			return null;
	//		}
	//	}
	//
	//	@Override
	//	public String getCodeSystemName() {
	//		return CODESYSTEM_NAME;
	//	}
	//
	//	@Override
	//	public String getCode() {
	//		return getPhar();
	//	}
	//
	//	@Override
	//	public String getText() {
	//		return getLabel();
	//	}
	//
	//	@Override
	//	public String getCodeSystemCode() {
	//		String gtin = getGTIN();
	//		if (gtin != null && gtin.length() > 3) {
	//			if (getTYPE() == TYPE.P) {
	//				return "402";
	//			} else if (getTYPE() == TYPE.N) {
	//				return "406";
	//			}
	//		}
	//		return ICodeElement.super.getCodeSystemCode();
	//	}
}
