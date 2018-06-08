package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.IntegerStringConverter;
import ch.elexis.core.types.LabItemTyp;
import ch.rgw.tools.StringTool;

@Entity
@Table(name = "laboritems")
public class LabItem extends AbstractDBObjectIdDeleted {

	@Column(name = "kuerzel", length = 80)
	private String code;

	@Column(name = "titel", length = 80)
	private String name;

	@OneToOne
	@JoinColumn(name = "laborID")
	private Kontakt labor;

	@Column(name = "RefMann", length = 256)
	private String referenceMale;

	@Column(name = "RefFrauOrTx", length = 256)
	private String referenceFemale;

	@Column(name = "einheit", length = 20)
	private String unit;

	@Column(name = "typ", length = 1)
	private String type;

	@Column(name = "Gruppe", length = 25)
	private String group;

	@Column(name = "prio", length = 3)
	private String priority;

	@Column(length = 128)
	private String billingCode;

	@Column(length = 100)
	private String export;

	@Column(length = 128)
	private String loinccode;

	@Convert(converter = BooleanCharacterConverterSafe.class)
	private boolean visible = true;

	@Convert(converter = IntegerStringConverter.class)
	private int digits;

	@Column(length = 255)
	private String formula;

	/**
	 * @return the variable name of this LabItem as used in LabItems of type
	 *         formula for cross-reference
	 */
	@Transient
	public String getVariableName() {
		String group = getGroup();
		if (group != null && group.contains(StringTool.space)) {
			String[] g = group.split(StringTool.space, 2);
			String prio = getPriority();
			String num = (prio != null) ? prio.trim() : "9999";
			return g[0] + "_" + num;
		}

		return "ERROR";
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Kontakt getLabor() {
		return labor;
	}

	public void setLabor(Kontakt labor) {
		this.labor = labor;
	}

	public String getReferenceMale() {
		return referenceMale;
	}

	public void setReferenceMale(String referenceMale) {
		this.referenceMale = referenceMale;
	}

	public String getReferenceFemale() {
		return referenceFemale;
	}

	public void setReferenceFemale(String referenceFemale) {
		this.referenceFemale = referenceFemale;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getBillingCode() {
		return billingCode;
	}

	public void setBillingCode(String billingCode) {
		this.billingCode = billingCode;
	}

	public String getExport() {
		return export;
	}

	public void setExport(String export) {
		this.export = export;
	}

	public String getLoinccode() {
		return loinccode;
	}

	public void setLoinccode(String loinccode) {
		this.loinccode = loinccode;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public int getDigits() {
		return digits;
	}

	public void setDigits(int digits) {
		this.digits = digits;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	//	@Override
	//	@Transient
	//	public LabItemTyp getTyp() {
	//		int typ = 1;
	//		try {
	//			typ = Integer.parseInt(getType());
	//		} catch (NumberFormatException nfe) {
	//		}
	//
	//		switch (typ) {
	//		case 0:
	//			return LabItemTyp.NUMERIC;
	//		case 1:
	//			return LabItemTyp.TEXT;
	//		case 2:
	//			return LabItemTyp.ABSOLUTE;
	//		case 3:
	//			return LabItemTyp.FORMULA;
	//		case 4:
	//			return LabItemTyp.DOCUMENT;
	//		default:
	//			return LabItemTyp.TEXT;
	//		}
	//	}
	//
	//	@Override
	//	@Transient
	//	public void setTyp(LabItemTyp type) {
	//		String tp = "1";
	//		if (type == LabItemTyp.NUMERIC) {
	//			tp = "0";
	//		} else if (type == LabItemTyp.ABSOLUTE) {
	//			tp = "2";
	//		} else if (type == LabItemTyp.FORMULA) {
	//			tp = "3";
	//		} else if (type == LabItemTyp.DOCUMENT) {
	//			tp = "4";
	//		}
	//		setType(tp);
	//	}
	//
	//	@Override
	//	@Transient
	//	public String getKuerzel() {
	//		return getCode();
	//	}
	//
	//	@Override
	//	@Transient
	//	public void setKuerzel(String value) {
	//		setCode(value);
	//	}

	@Override
	public String getLabel() {
		StringBuilder sb = new StringBuilder();
		sb.append(getCode()).append(", ").append(getName());
		if (LabItemTyp.NUMERIC == LabItemTyp.fromType(getType())) {
			sb.append(" (").append(getReferenceMale()).append("/").append(getReferenceFemale()).append(" ")
					.append(getUnit()).append(")");
		} else {
			sb.append(" (").append(getReferenceFemale()).append(")");
		}
		sb.append("[").append(getGroup()).append(", ").append(getPriority()).append("]");
		return sb.toString();
	}

}
