package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;

@Entity
@Table(name = "TARDOC")
@EntityListeners(EntityWithIdListener.class)
@NamedQueries({
		@NamedQuery(name = "TardocLeistungDistinctLaws", query = "SELECT DISTINCT tl.law FROM TardocLeistung tl WHERE tl.id <> 'VERSION'"),
		@NamedQuery(name = "TardocLeistung.title.validUntil", query = "SELECT tl FROM TardocLeistung tl WHERE tl.deleted = false AND tl.tx255 LIKE :title AND tl.gueltigBis = :validUntil ORDER BY tl.code_ ASC"),
		@NamedQuery(name = "TardocLeistung.parent", query = "SELECT tl FROM TardocLeistung tl WHERE tl.deleted = false AND tl.parent = :parent ORDER BY tl.code_ ASC"),
		@NamedQuery(name = "TardocLeistung.parent.chapter", query = "SELECT tl FROM TardocLeistung tl WHERE tl.deleted = false AND tl.parent = :parent AND tl.isChapter = :chapter ORDER BY tl.code_ ASC") })

public class TardocLeistung extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	public static final String CODESYSTEM_NAME = "Tarmed";

	public static String MANDANT_TYPE_EXTINFO_KEY = "ch.elexis.data.tarmed.mandant.type";

	public enum MandantType {
		SPECIALIST, PRACTITIONER, TARPSYAPPRENTICE
	}

	public static final String EXT_FLD_TP_IPL = "TP_IPL";
	public static final String EXT_FLD_TP_AL = "TP_AL";
	public static final String EXT_FLD_F_AL_R = "F_AL_R";
	public static final String EXT_FLD_HIERARCHY_SLAVES = "HierarchySlaves";
	public static final String EXT_FLD_SERVICE_GROUPS = "ServiceGroups";
	public static final String EXT_FLD_SERVICE_AGE = "ServiceAge";

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Column(length = 32)
	private String parent;

	@Column(length = 5)
	private String digniQuanti;

	@Column(length = 4)
	private String digniQuali;

	@Column(length = 4)
	private String sparte;

	@Column(length = 4)
	private LocalDate gueltigVon;

	@Column(length = 4)
	private LocalDate gueltigBis;

	@Column(length = 25)
	private String nickname;

	@Column(length = 255)
	private String tx255;

	@Column(length = 25, name = "code")
	private String code_;

	@Column(length = 3)
	private String law;

	@Column(length = 1)
	@Convert(converter = BooleanCharacterConverterSafe.class)
	private boolean isChapter;

	// /**
	// * Get the AL value of the {@link TardocLeistung}.<br>
	// * <b>IMPORTANT:</b> No scaling according to the Dignität of the {@link
	// Mandant}
	// * is performed. Use {@link TardocLeistung#getAL(Mandant)} for AL value with
	// * scaling included.
	// *
	// * @return
	// */
	// @Transient
	// public int getAL() {
	// if (extension != null) {
	// Object object = extension.getLimits().get(EXT_FLD_TP_AL);
	// if (object != null) {
	// try {
	// double val = Double.parseDouble((String) object);
	// return (int) Math.round(val * 100);
	// } catch (NumberFormatException nfe) {
	// /* ignore */
	// }
	// }
	// }
	// return 0;
	// }

	// /**
	// * Get the AL scaling value to be used when billing this {@link
	// TardocLeistung} for the provided
	// * {@link Mandant}.
	// *
	// * @param mandant
	// * @return
	// */
	// @Transient
	// public double getALScaling(Kontakt mandant){
	// double scaling = 100;
	// if (mandant != null) {
	// MandantType type = getMandantType(mandant);
	// if (type == MandantType.PRACTITIONER) {
	// double alScaling =
	// EntitiesUtil.checkZeroDouble(getExtension().getLimits().get(EXT_FLD_F_AL_R));
	// if (alScaling > 0.1) {
	// scaling *= alScaling;
	// }
	// }
	// }
	// return scaling;
	// }
	//
	// /**
	// * Get the {@link MandantType} of the {@link Mandant}. If not found the
	// default value is
	// * {@link MandantType#SPECIALIST}.
	// *
	// * @param mandant
	// * @return
	// */
	// @Transient
	// public static MandantType getMandantType(Kontakt mandant){
	// Object typeObj = mandant.getExtInfo(MANDANT_TYPE_EXTINFO_KEY);
	// if (typeObj instanceof String) {
	// return MandantType.valueOf((String) typeObj);
	// }
	// return MandantType.SPECIALIST;
	// }

	// @Transient
	// public int getTL() {
	// if (extension != null) {
	// Object object = extension.getLimits().get(EXT_FLD_TP_TL);
	// if (object != null) {
	// try {
	// double val = Double.parseDouble((String) object);
	// return (int) Math.round(val * 100);
	// } catch (NumberFormatException nfe) {
	// /* ignore */
	// }
	// }
	// }
	// return 0;
	// }
	//
	// public boolean requiresSide() {
	// if (extension != null) {
	// Object object = extension.getLimits().get("SEITE");
	// if (object != null && Integer.parseInt((String) object) == 1) {
	// return true;
	// }
	// }
	// return false;
	// }

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getDigniQuanti() {
		return digniQuanti;
	}

	public void setDigniQuanti(String digniQuanti) {
		this.digniQuanti = digniQuanti;
	}

	public String getDigniQuali() {
		return digniQuali;
	}

	public void setDigniQuali(String digniQuali) {
		this.digniQuali = digniQuali;
	}

	public String getSparte() {
		return sparte;
	}

	public void setSparte(String sparte) {
		this.sparte = sparte;
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

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getTx255() {
		return tx255;
	}

	public void setTx255(String tx255) {
		this.tx255 = tx255;
	}

	public void setCode_(String code_) {
		this.code_ = code_;
	}

	public String getCode_() {
		return code_;
	}

	public String getCode() {
		return (code_ != null) ? code_ : getId();
	}

	public String getCodeSystemName() {
		return CODESYSTEM_NAME;
	}

	public String getText() {
		return getTx255();
	}

	public String getLaw() {
		return law;
	}

	public void setLaw(String law) {
		this.law = law;
	}

	public boolean isChapter() {
		return isChapter;
	}

	public void setChapter(boolean isChapter) {
		this.isChapter = isChapter;
	}

	// @Transient
	// public List<String> getExtStringListField(String extKey) {
	// List<String> ret = new ArrayList<>();
	// Map<String, String> map = getExtension().getLimits();
	// String values = map.get(extKey);
	// if (values != null && !values.isEmpty()) {
	// String[] parts = values.split(", ");
	// for (String string : parts) {
	// ret.add(string);
	// }
	// }
	// return ret;
	// }
	//
	// @Transient
	// public String getServiceTyp(){
	// return getExtension().getLimits().get("LEISTUNG_TYP");
	// }

	// /**
	// * Get the list of service groups this service is part of.
	// *
	// * @return
	// */
	// @Transient
	// public List<String> getServiceGroups(TimeTool date) {
	// List<String> ret = new ArrayList<>();
	// List<String> groups =
	// getExtStringListField(TardocLeistung.EXT_FLD_SERVICE_GROUPS);
	// if (!groups.isEmpty()) {
	// for (String string : groups) {
	// int dateStart = string.indexOf('[');
	// String datesString = string.substring(dateStart + 1, string.length() - 1);
	// String groupString = string.substring(0, dateStart);
	// if (isDateWithinDatesString(date, datesString)) {
	// ret.add(groupString);
	// }
	// }
	// }
	// return ret;
	// }

	// /**
	// * Get the list of service blocks this service is part of.
	// *
	// * @return
	// */
	// @Transient
	// public List<String> getServiceBlocks(TimeTool date){
	// List<String> ret = new ArrayList<>();
	// List<String> blocks =
	// getExtStringListField(TardocLeistung.EXT_FLD_SERVICE_BLOCKS);
	// if (!blocks.isEmpty()) {
	// for (String string : blocks) {
	// int dateStart = string.indexOf('[');
	// String datesString = string.substring(dateStart + 1, string.length() - 1);
	// String blockString = string.substring(0, dateStart);
	// if (isDateWithinDatesString(date, datesString)) {
	// ret.add(blockString);
	// }
	// }
	// }
	// return ret;
	// }
	//
	// /**
	// * Get the list of codes of the possible slave services allowed by tarmed.
	// *
	// * @return
	// */
	// public List<String> getHierarchy(TimeTool date){
	// List<String> ret = new ArrayList<>();
	// List<String> hierarchy =
	// getExtStringListField(TardocLeistung.EXT_FLD_HIERARCHY_SLAVES);
	// if (!hierarchy.isEmpty()) {
	// for (String string : hierarchy) {
	// int dateStart = string.indexOf('[');
	// String datesString = string.substring(dateStart + 1, string.length() - 1);
	// String codeString = string.substring(0, dateStart);
	// if (isDateWithinDatesString(date, datesString)) {
	// ret.add(codeString);
	// }
	// }
	// }
	// return ret;
	// }

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
