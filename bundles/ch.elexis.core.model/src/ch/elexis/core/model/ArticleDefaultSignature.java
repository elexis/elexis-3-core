package ch.elexis.core.model;

import java.time.LocalDate;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jpa.entities.DefaultSignature;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.model.article.defaultsignature.Constants;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;

public class ArticleDefaultSignature extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.DefaultSignature>
		implements IdentifiableWithXid, IArticleDefaultSignature {

	private LocalDate startDate;
	private LocalDate endDate;

	public ArticleDefaultSignature(DefaultSignature entity) {
		super(entity);
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
	public String getAtcCode() {
		return getEntity().getAtccode();
	}

	@Override
	public void setAtcCode(String value) {
		getEntityMarkDirty().setAtccode(value);
	}

	@Override
	public String getMorning() {
		return getEntity().getMorning();
	}

	@Override
	public void setMorning(String value) {
		getEntityMarkDirty().setMorning(value);
	}

	@Override
	public String getNoon() {
		return getEntity().getNoon();
	}

	@Override
	public void setNoon(String value) {
		getEntityMarkDirty().setNoon(value);
	}

	@Override
	public String getEvening() {
		return getEntity().getEvening();
	}

	@Override
	public void setEvening(String value) {
		getEntityMarkDirty().setEvening(value);
	}

	@Override
	public String getNight() {
		return getEntity().getNight();
	}

	@Override
	public void setNight(String value) {
		getEntityMarkDirty().setNight(value);
	}

	@Override
	public String getComment() {
		return getEntity().getComment();
	}

	@Override
	public void setComment(String value) {
		getEntityMarkDirty().setComment(value);
	}

	@Override
	public void setArticle(IArticle article) {
		String articleString = article.getGtin() + "$" + article.getCode() + "$"
				+ StoreToStringServiceHolder.getStoreToString(article);
		getEntityMarkDirty().setArticle(articleString);
	}

	@Override
	public String getFreeText() {
		return (String) getExtInfo(Constants.EXT_FLD_FREETEXT);
	}

	@Override
	public void setFreeText(String value) {
		if (value == null) {
			value = StringUtils.EMPTY;
		}
		setExtInfo(Constants.EXT_FLD_FREETEXT, value);
	}

	@Override
	public EntryType getMedicationType() {
		String typeNumber = (String) getExtInfo(Constants.EXT_FLD_MEDICATIONTYPE);
		if (typeNumber != null && !typeNumber.isEmpty()) {
			return EntryType.byNumeric(Integer.parseInt(typeNumber));
		}
		return EntryType.UNKNOWN;
	}

	@Override
	public void setMedicationType(EntryType value) {
		setExtInfo(Constants.EXT_FLD_MEDICATIONTYPE, Integer.toString(value.numericValue()));
	}

	@Override
	public EntryType getDisposalType() {
		String typeNumber = (String) getExtInfo(Constants.EXT_FLD_DISPOSALTYPE);
		if (typeNumber != null && !typeNumber.isEmpty()) {
			return EntryType.byNumeric(Integer.parseInt(typeNumber));
		}
		return EntryType.UNKNOWN;
	}

	@Override
	public void setDisposalType(EntryType value) {
		setExtInfo(Constants.EXT_FLD_DISPOSALTYPE, Integer.toString(value.numericValue()));
	}

	@Override
	public boolean isAtc() {
		return (getAtcCode() != null && !getAtcCode().isEmpty());
	}

	@Override
	public String getSignatureAsDosisString() {
		String freeText = getFreeText();
		if (freeText != null && !freeText.isEmpty()) {
			return freeText;
		}

		String[] values = new String[] { getMorning(), getNoon(), getEvening(), getNight() };

		StringBuilder sb = new StringBuilder();
		if (signatureInfoExists(values)) {
			for (int i = 0; i < values.length; i++) {
				String string = values[i] == null || values[i].isEmpty() ? "0" : values[i];

				if (i > 0) {
					sb.append("-");
				}
				sb.append(string);
			}
		}
		return sb.toString();
	}

	private boolean signatureInfoExists(String[] values) {
		for (String val : values) {
			if (val != null && !val.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public LocalDate getEndDate() {
		return endDate;
	}

	@Override
	public void setEndDate(LocalDate value) {
		this.endDate = value;
	}

	@Override
	public LocalDate getStartDate() {
		return startDate;
	}

	@Override
	public void setStartDate(LocalDate value) {
		this.startDate = value;
	}
}
