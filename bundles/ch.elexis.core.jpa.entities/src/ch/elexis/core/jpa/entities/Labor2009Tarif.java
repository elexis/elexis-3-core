package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.DoubleStringConverter;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import ch.rgw.tools.StringTool;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "CH_MEDELEXIS_LABORTARIF2009")
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "Labor2009Tarif.code", query = "SELECT lt FROM Labor2009Tarif lt WHERE lt.deleted = false AND lt.code = :code")
public class Labor2009Tarif extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Column(length = 255)
	private String chapter;

	@Column(length = 12)
	private String code;

	@Convert(converter = DoubleStringConverter.class)
	private double tp;

	@Column(length = 255)
	private String name;

	@Lob
	private String limitatio;

	@Column(length = 10)
	private String fachbereich;

	@Column(length = 8)
	private LocalDate gueltigVon;

	@Column(length = 8)
	private LocalDate gueltigBis;

	@Column(length = 2)
	private String praxistyp;

	public String getChapter() {
		return chapter;
	}

	public void setChapter(String chapter) {
		this.chapter = chapter;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public double getTp() {
		return tp;
	}

	public void setTp(double tp) {
		this.tp = tp;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLimitatio() {
		return limitatio;
	}

	public void setLimitatio(String limitatio) {
		this.limitatio = limitatio;
	}

	public String getFachbereich() {
		return fachbereich;
	}

	public void setFachbereich(String fachbereich) {
		this.fachbereich = fachbereich;
	}

	public LocalDate getGueltigVon() {
		return gueltigVon;
	}

	public void setGueltigVon(LocalDate gueltigVon) {
		this.gueltigVon = gueltigVon;
	}

	public LocalDate getGueltigBis() {
		return gueltigBis;
	}

	public void setGueltigBis(LocalDate gueltigBis) {
		this.gueltigBis = gueltigBis;
	}

	public String getPraxistyp() {
		return praxistyp;
	}

	public void setPraxistyp(String praxistyp) {
		this.praxistyp = praxistyp;
	}

	public String getCodeSystemName() {
		return "EAL 2009";
	}

	public String getText() {
		return StringTool.getFirstLine(getName(), 80);
	}

	public String getCodeSystemCode() {
		return "317";
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
}
