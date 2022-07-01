package ch.elexis.core.findings.util.fhir.transformer.helper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.LocalLockServiceHolder;

public class AbstractHelper {

	private static Logger logger = LoggerFactory.getLogger(AbstractHelper.class);

	protected Date getDate(LocalDateTime localDateTime) {
		ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
		return Date.from(zdt.toInstant());
	}

	protected Date getDate(LocalDate localDate) {
		ZonedDateTime zdt = localDate.atStartOfDay(ZoneId.systemDefault());
		return Date.from(zdt.toInstant());
	}

	protected LocalDateTime getLocalDateTime(Date date) {
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	public Optional<ZonedDateTime> getLastUpdateAsZonedDateTime(Long lastUpdate) {
		if (lastUpdate != null) {
			ZonedDateTime zonedDateTime = Instant.ofEpochMilli(lastUpdate).atZone(ZoneId.systemDefault());
			return Optional.of(zonedDateTime);

		}
		return Optional.empty();
	}

	public Optional<Date> getLastUpdateAsDate(Long lastUpdate) {
		if (lastUpdate != null) {
			Date lastUpdateDate = Date.from(getLastUpdateAsZonedDateTime(lastUpdate).get().toInstant());
			return Optional.of(lastUpdateDate);
		}
		return Optional.empty();
	}

	public Reference getReference(String resourceType, Identifiable dbObject) {
		return new Reference(new IdDt("Patient", dbObject.getId()));
	}

	public static void acquireAndReleaseLock(Identifiable dbObj) {
		LockResponse lr = LocalLockServiceHolder.get().acquireLockBlocking(dbObj, 5, new NullProgressMonitor());
		if (lr.isOk()) {
			LockResponse lrs = LocalLockServiceHolder.get().releaseLock(lr);
			if (!lrs.isOk()) {
				logger.warn("Could not release lock for [{}] [{}]", dbObj.getClass().getName(), dbObj.getId());
			}
		} else {
			logger.warn("Could not acquire lock for [{}] [{}]", dbObj.getClass().getName(), dbObj.getId());
		}
	}

	public static LockResponse acquireLock(Identifiable dbObj) {
		return LocalLockServiceHolder.get().acquireLockBlocking(dbObj, 5, new NullProgressMonitor());
	}

	public static void releaseLock(LockInfo lockInfo) {
		LocalLockServiceHolder.get().releaseLock(lockInfo);
	}

	public void setVersionedIdPartLastUpdatedMeta(Class<?> resourceClass, DomainResource domainResource,
			Identifiable localObject) {
		domainResource.setId(new IdDt(resourceClass.getSimpleName(), localObject.getId(),
				Long.toString(localObject.getLastupdate())));
		domainResource.getMeta().setLastUpdated(getLastUpdateAsDate(localObject.getLastupdate()).orElse(null));
	}

	public void setNarrative(DomainResource domainResource, String text) {
		Narrative narrative = domainResource.getText();
		if (narrative == null) {
			narrative = new Narrative();
		}
		if ("".equals(text)) {
			text = "[EMPTY]";
		}
		String divEncodedText = text.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("&", "&amp;")
				.replaceAll("(\r\n|\r|\n)", "<br />");
		narrative.setDivAsString(divEncodedText);
		domainResource.setText(narrative);
	}

	public Optional<String> getText(DomainResource domainResource) {
		Narrative narrative = domainResource.getText();
		if (narrative != null && narrative.getDivAsString() != null) {
			return ModelUtil.getNarrativeAsString(narrative);
		}
		return Optional.empty();
	}
}
