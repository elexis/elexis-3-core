package ch.elexis.core.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entities.TagesNachricht;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;

public class DayMessage extends AbstractIdDeleteModelAdapter<TagesNachricht>
		implements IdentifiableWithXid, IDayMessage {

	private Logger log = LoggerFactory.getLogger(DayMessage.class);

	private final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");

	public DayMessage(TagesNachricht entity) {
		super(entity);
	}

	@Override
	public void setId(String id) {
		getEntityMarkDirty().setId(id);
	}

	@Override
	public String getTitle() {
		return getEntity().getKurz();
	}

	@Override
	public void setTitle(String value) {
		getEntityMarkDirty().setKurz(value);
	}

	@Override
	public String getMessage() {
		return getEntity().getMsg();
	}

	@Override
	public void setMessage(String value) {
		getEntityMarkDirty().setMsg(value);
	}

	@Override
	public LocalDate getDate() {
		if (StringUtils.isNotBlank(getId())) {
			try {
				return LocalDate.parse(getId(), yyyyMMdd);
			} catch (DateTimeParseException e) {
				log.warn("Error parsing [{}]", getId(), e);
			}
		}
		return null;
	}

	@Override
	public void setDate(LocalDate value) {
		if (value == null) {
			return;
		}
		getEntityMarkDirty().setId(value.format(yyyyMMdd));
	}
}
