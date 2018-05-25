package ch.elexis.core.jpa.entities;

import java.util.Hashtable;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import ch.elexis.core.jpa.entities.converter.ElexisExtInfoMapConverter;
import ch.elexis.core.jpa.entities.converter.IntegerStringConverter;

@Entity
@Table(name = "LEISTUNGEN")
public class Verrechnet extends AbstractDBObjectIdDeleted {
	
	public static final String EXT_VERRRECHNET_TL = "TL"; //$NON-NLS-1$
	public static final String EXT_VERRRECHNET_AL = "AL"; //$NON-NLS-1$

	@Column(length = 80)
	private String klasse;

	@Column(length = 25, name = "leistg_code")
	private String leistungenCode;

	@Column(length = 255, name = "leistg_txt")
	private String leistungenText;

	@OneToOne
	@JoinColumn(name = "BEHANDLUNG")
	private Behandlung behandlung;

	@Convert(converter = IntegerStringConverter.class)
	private int zahl;

	@Convert(converter = IntegerStringConverter.class)
	private int ek_kosten;

	@Convert(converter = IntegerStringConverter.class)
	private int vk_tp;

	@Column(length = 8)
	private String vk_scale;

	@Convert(converter = IntegerStringConverter.class)
	private int vk_preis;

	@Convert(converter = IntegerStringConverter.class)
	private int scale;

	@Convert(converter = IntegerStringConverter.class)
	private int scale2;

	@OneToOne
	@JoinColumn(name = "userID")
	private Kontakt user;

	@Basic(fetch = FetchType.LAZY)
	@Convert(converter = ElexisExtInfoMapConverter.class)
	private Map<Object, Object> detail;

	@Transient
	public void setTP(double tp) {
		setVk_tp((int) Math.round(tp));
	}

	/**
	 * Derives the settings for {@link #zahl}, {@link #scale} and {@link #scale2}
	 * for the provided value
	 * 
	 * @param countValue
	 */
	@Transient
	public void setDerivedCountValue(float countValue) {
		if (countValue % 1 == 0) {
			// integer -> full package
			setZahl((int) countValue);
			setScale(100);
			setScale2(100);
		} else {
			// float -> fractional package
			setZahl(1);
			setScale(100);
			int scale2 = Math.round(countValue * 100);
			setScale2(scale2);
		}
	}
	
	@Transient
	public float getDerivedCountValue() {
		if (scale2 == 100) {
			return getZahl();
		}
		return scale2 / 100f;
	}

	@Transient
	public double getPrimaryScaleFactor() {
		if (getScale() == 0) {
			return 1.0;
		}
		return ((double) getScale()) / 100.0;
	}
	
	@Transient
	public void setPrimaryScaleFactor(double scale) {
		int sca = (int) Math.round(scale * 100);
		setScale(sca);
	}

	@Transient
	public double getSecondaryScaleFactor() {
		if (getScale2() == 0) {
			return 1.0;
		}
		return ((double) getScale2()) / 100.0;
	}
	
	@Transient
	public void setSecondaryScaleFactor(double scale) {
		int sca = (int) Math.round(scale * 100);
		setScale2(sca);
	}

	@Transient
	public String getText() {
		return getLeistungenText();
	}

	@Transient
	public float getScaledCount() {
		return getZahl() * (getScale2() / 100f);
	}

	public Map<Object, Object> getDetail() {
		if (detail == null) {
			detail = new Hashtable<Object, Object>();
		}
		return detail;
	}

	public void setDetail(final String key, final String value) {
		if (value == null) {
			getDetail().remove(key);
		} else {
			getDetail().put(key, value);
		}
	}

	public String getLeistungenText() {
		return leistungenText;
	}

	public void setLeistungenText(String leistungenText) {
		this.leistungenText = leistungenText;
	}

	public Behandlung getBehandlung() {
		return behandlung;
	}

	public void setBehandlung(Behandlung behandlung) {
		this.behandlung = behandlung;
	}

	public int getZahl() {
		return zahl;
	}

	public void setZahl(int zahl) {
		this.zahl = zahl;
	}

	public int getEk_kosten() {
		return ek_kosten;
	}

	public void setEk_kosten(int ek_kosten) {
		this.ek_kosten = ek_kosten;
	}

	public int getVk_tp() {
		return vk_tp;
	}

	public void setVk_tp(int vk_tp) {
		this.vk_tp = vk_tp;
	}

	public String getVk_scale() {
		return vk_scale;
	}

	public void setVk_scale(String vk_scale) {
		this.vk_scale = vk_scale;
	}

	public int getVk_preis() {
		return vk_preis;
	}

	public void setVk_preis(int vk_preis) {
		this.vk_preis = vk_preis;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public int getScale2() {
		return scale2;
	}

	public void setScale2(int scale2) {
		this.scale2 = scale2;
	}

	public String getKlasse() {
		return klasse;
	}

	public void setKlasse(String klasse) {
		this.klasse = klasse;
	}

	public String getLeistungenCode() {
		return leistungenCode;
	}

	public void setLeistungenCode(String leistungenCode) {
		this.leistungenCode = leistungenCode;
	}

	public Kontakt getUser() {
		return user;
	}

	public void setUser(Kontakt user) {
		this.user = user;
	}
	
	@Transient
	public String getDetail(final String key){
		return (String) getDetail().get(key);
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}
}
