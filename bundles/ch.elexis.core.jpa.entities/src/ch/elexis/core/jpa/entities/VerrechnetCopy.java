package ch.elexis.core.jpa.entities;

import java.beans.Transient;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.IntegerStringConverter;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "VERRECHNETCOPY")
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "VerrechnetCopy.encounter", query = "SELECT ve FROM VerrechnetCopy ve WHERE ve.deleted = false AND ve.behandlung = :encounter")
public class VerrechnetCopy extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted, EntityWithExtInfo {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Column(length = 80)
	private String klasse;

	@Column(length = 25, name = "leistg_code")
	private String leistungenCode;

	@Column(length = 255, name = "leistg_txt")
	private String leistungenText;

	@OneToOne
	@JoinColumn(name = "rechnungid")
	private Invoice invoice;

	@OneToOne
	@JoinColumn(name = "behandlungid")
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

	@Lob
	private byte[] detail;

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

	public byte[] getDetail() {
		return detail;
	}

	public void setDetail(byte[] detail) {
		this.detail = detail;
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

	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
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

	@Override
	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public Long getLastupdate() {
		return lastupdate;
	}

	@Override
	public void setLastupdate(Long lastupdate) {
		this.lastupdate = lastupdate;
	}

	@Override
	public byte[] getExtInfo() {
		return detail;
	}

	@Override
	public void setExtInfo(byte[] extInfo) {
		this.detail = extInfo;
	}
}
