package ch.elexis.core.findings.fhir.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Reference;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.fhir.mapper.r4.findings.ObservationAccessor;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservationLink;
import ch.elexis.core.findings.IObservationLink.ObservationLinkType;
import ch.elexis.core.findings.ObservationComponent;
import ch.elexis.core.findings.fhir.model.service.FindingsModelService;
import ch.elexis.core.findings.fhir.model.service.FindingsModelServiceHolder;
import ch.elexis.core.findings.scripting.FindingsScriptingUtil;
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.jpa.entities.ObservationLink;
import ch.elexis.core.model.IXid;

public class Observation extends AbstractFindingModelAdapter<ch.elexis.core.jpa.entities.Observation>
		implements IObservation {

	private static final String FORMAT_KEY_VALUE_SPLITTER = ":-:";
	private static final String FORMAT_SPLITTER = ":split:";

	private ObservationAccessor accessor = new ObservationAccessor();

	public Observation(ch.elexis.core.jpa.entities.Observation entity) {
		super(entity);
	}

	@Override
	public String getId() {
		return getEntity().getId();
	}

	@Override
	public String getPatientId() {
		return getEntity().getPatientId();
	}

	@Override
	public void setPatientId(String patientId) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setPatientId((DomainResource) resource.get(), patientId);
			saveResource(resource.get());
		}

		getEntity().setPatientId(patientId);
	}

	@Override
	public List<ICoding> getCoding() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getCoding((DomainResource) resource.get());
		}
		return Collections.emptyList();
	}

	@Override
	public void setCoding(List<ICoding> coding) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setCoding((DomainResource) resource.get(), coding);
			saveResource(resource.get());
		}
	}

	@Override
	public RawContentFormat getRawContentFormat() {
		return RawContentFormat.FHIR_JSON;
	}

	@Override
	public String getRawContent() {
		return getEntity().getContent();
	}

	@Override
	public void setRawContent(String content) {
		getEntity().setContent(content);
	}

	@Override
	public Optional<IEncounter> getEncounter() {
		String encounterId = getEntity().getEncounterId();
		if (encounterId != null && !encounterId.isEmpty()) {
			return ModelUtil.loadFinding(encounterId, IEncounter.class);
		}
		return Optional.empty();
	}

	@Override
	public void setEncounter(IEncounter encounter) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.r4.model.Observation fhirObservation = (org.hl7.fhir.r4.model.Observation) resource.get();
			fhirObservation.setEncounter(new Reference(new IdDt("Encounter", encounter.getId())));

			saveResource(resource.get());
		}

		String patientId = encounter.getPatientId();
		if (patientId != null && !patientId.isEmpty() && getPatientId() == null) {
			setPatientId(patientId);
		}

		getEntity().setEncounterId(encounter.getId());
	}

	@Override
	public Optional<LocalDateTime> getEffectiveTime() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getEffectiveTime((DomainResource) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public void setEffectiveTime(LocalDateTime time) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setEffectiveTime((DomainResource) resource.get(), time);
			saveResource(resource.get());
		}
	}

	@Override
	public ObservationCategory getCategory() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getCategory((DomainResource) resource.get());
		}
		return ObservationCategory.UNKNOWN;
	}

	@Override
	public void setCategory(ObservationCategory category) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setCategory((DomainResource) resource.get(), category);
			saveResource(resource.get());
		}
	}

	@Override
	public List<IObservation> getTargetObseravtions(ObservationLinkType type) {
		List<ObservationLink> typeSourceLinks = getEntity().getSourceLinks();
		typeSourceLinks = typeSourceLinks.stream().filter(ol -> ol.getType().equals(type.name()))
				.collect(Collectors.toList());
		return typeSourceLinks.stream().map(ol -> FindingsModelService.getAdapter(ol.getTarget(), IObservation.class))
				.collect(Collectors.toList());
	}

	@Override
	public List<IObservation> getSourceObservations(ObservationLinkType type) {
		List<ObservationLink> typeTargetLinks = getEntity().getTargetLinks();
		typeTargetLinks = typeTargetLinks.stream().filter(ol -> ol.getType().equals(type.name()))
				.collect(Collectors.toList());
		return typeTargetLinks.stream().map(ol -> FindingsModelService.getAdapter(ol.getSource(), IObservation.class))
				.collect(Collectors.toList());
	}

	@Override
	public void addTargetObservation(IObservation target, ObservationLinkType type) {
		if (target != null && target.getId() != null && getId() != null) {
			IObservationLink observationLink = ModelUtil.createFinding(IObservationLink.class);
			ObservationLink observationLinkEntity = FindingsModelService.getDBObject(observationLink,
					ObservationLink.class);
			// add to link and observations
			observationLink.setTarget(target);
			FindingsModelService.getDBObject(target, ch.elexis.core.jpa.entities.Observation.class).getTargetLinks()
					.add(observationLinkEntity);
			observationLink.setSource(this);
			getEntity().getSourceLinks().add(observationLinkEntity);
			observationLink.setType(type);
			FindingsModelServiceHolder.get().save(Arrays.asList(observationLink, target, this));
		}
	}

	@Override
	public void addSourceObservation(IObservation source, ObservationLinkType type) {
		if (source != null && source.getId() != null && getId() != null) {
			IObservationLink observationLink = ModelUtil.createFinding(IObservationLink.class);
			ObservationLink observationLinkEntity = FindingsModelService.getDBObject(observationLink,
					ObservationLink.class);
			// add to link and observations
			observationLink.setSource(source);
			FindingsModelService.getDBObject(source, ch.elexis.core.jpa.entities.Observation.class).getSourceLinks()
					.add(observationLinkEntity);
			observationLink.setTarget(this);
			getEntity().getTargetLinks().add(observationLinkEntity);
			observationLink.setType(type);
			FindingsModelServiceHolder.get().save(Arrays.asList(observationLink, source, this));
		}
	}

	@Override
	public void removeTargetObservation(IObservation target, ObservationLinkType type) {
		if (target != null && target.getId() != null && getId() != null) {
			List<ObservationLink> typeSourceLinks = getEntity().getSourceLinks();
			typeSourceLinks = typeSourceLinks.stream().filter(ol -> ol.getType().equals(type.name()))
					.collect(Collectors.toList());
			ch.elexis.core.jpa.entities.Observation targetEntity = FindingsModelService.getDBObject(target,
					ch.elexis.core.jpa.entities.Observation.class);
			for (ObservationLink sourceLink : typeSourceLinks) {
				if (sourceLink.getTarget().equals(targetEntity)) {
					IObservationLink observationLink = FindingsModelService.getAdapter(sourceLink,
							IObservationLink.class);
					observationLink.setTarget(null);
					targetEntity.getTargetLinks().remove(sourceLink);
					observationLink.setSource(null);
					getEntity().getSourceLinks().remove(sourceLink);
					FindingsModelServiceHolder.get().save(Arrays.asList(observationLink, target, this));
					FindingsModelServiceHolder.get().remove(observationLink);
					// work is done
					break;
				}
			}
		}
	}

	@Override
	public void removeSourceObservation(IObservation source, ObservationLinkType type) {
		if (source != null && source.getId() != null && getId() != null) {
			List<ObservationLink> typeTargetLinks = getEntity().getTargetLinks();
			typeTargetLinks = typeTargetLinks.stream().filter(ol -> ol.getType().equals(type.name()))
					.collect(Collectors.toList());
			ch.elexis.core.jpa.entities.Observation sourceEntity = FindingsModelService.getDBObject(source,
					ch.elexis.core.jpa.entities.Observation.class);
			for (ObservationLink targetLink : typeTargetLinks) {
				if (targetLink.getSource().equals(sourceEntity)) {
					IObservationLink observationLink = FindingsModelService.getAdapter(targetLink,
							IObservationLink.class);
					observationLink.setTarget(null);
					sourceEntity.getSourceLinks().remove(targetLink);
					observationLink.setSource(null);
					getEntity().getTargetLinks().remove(targetLink);
					FindingsModelServiceHolder.get().save(Arrays.asList(observationLink, source, this));
					FindingsModelServiceHolder.get().remove(observationLink);
					// work is done
					break;
				}
			}
		}
	}

	@Override
	public void addComponent(ObservationComponent component) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.addComponent((DomainResource) resource.get(), component);
			saveResource(resource.get());
		}
	}

	@Override
	public void updateComponent(ObservationComponent component) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.updateComponent((DomainResource) resource.get(), component);
			saveResource(resource.get());
		}
	}

	@Override
	public List<ObservationComponent> getComponents() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getComponents((DomainResource) resource.get());
		}
		return Collections.emptyList();
	}

	@Override
	public void setNumericValue(BigDecimal bigDecimal, String unit) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setNumericValue((DomainResource) resource.get(), bigDecimal, unit);
			saveResource(resource.get());
		}
	}

	@Override
	public Optional<BigDecimal> getNumericValue() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			if (FindingsScriptingUtil.hasScript(this)) {
				FindingsScriptingUtil.evaluate(this);
				resource = loadResource();
			}
			return accessor.getNumericValue((DomainResource) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public void setStringValue(String value) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setStringValue((DomainResource) resource.get(), value);
			saveResource(resource.get());
		}
	}

	@Override
	public Optional<String> getStringValue() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getStringValue((DomainResource) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public void setBooleanValue(Boolean value) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setBooleanValue((DomainResource) resource.get(), value);
			saveResource(resource.get());
		}
	}

	@Override
	public Optional<Boolean> getBooleanValue() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getBooleanValue((DomainResource) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public void setDateTimeValue(Date value) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setDateTimeValue((DomainResource) resource.get(), value);
			saveResource(resource.get());
		}
	}

	@Override
	public Optional<Date> getDateTimeValue() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getDateTimeValue((DomainResource) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public Optional<String> getNumericValueUnit() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getNumericValueUnit((DomainResource) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public void setObservationType(ObservationType observationType) {
		if (observationType != null) {
			getEntity().setType(observationType.name());
		}
	}

	@Override
	public ObservationType getObservationType() {
		String type = getEntity().getType();
		return type != null ? ObservationType.valueOf(type) : null;
	}

	@Override
	public boolean isReferenced() {
		return getEntity().isReferenced();
	}

	@Override
	public void setReferenced(boolean referenced) {
		getEntity().setReferenced(referenced);
	}

	@Override
	public void setComment(String comment) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setNote((DomainResource) resource.get(), comment);
			saveResource(resource.get());
		}
	}

	@Override
	public Optional<String> getComment() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			List<String> notes = accessor.getNotes((DomainResource) resource.get());
			return Optional.of(String.join(StringUtils.SPACE, notes));
		}
		return Optional.empty();
	}

	@Override
	public void addFormat(String key, String value) {
		StringBuilder builder = new StringBuilder(StringUtils.defaultString(getEntity().getFormat()));
		String dbValue = getFormat(key);
		String dbKeyValue = key + FORMAT_KEY_VALUE_SPLITTER + dbValue;

		int idx = builder.indexOf(dbKeyValue);
		if (idx == -1) {
			if (builder.length() > 0) {
				builder.append(FORMAT_SPLITTER);
			}
			builder.append(key + FORMAT_KEY_VALUE_SPLITTER + value);
		} else {
			builder.replace(idx, idx + dbKeyValue.length(), key + FORMAT_KEY_VALUE_SPLITTER + value);
		}
		getEntityMarkDirty().setFormat(builder.toString());
	}

	@Override
	public String getFormat(String key) {
		String format = getEntity().getFormat();
		if (format != null && format.contains(key + FORMAT_KEY_VALUE_SPLITTER)) {
			String[] splits = format.split(key + FORMAT_KEY_VALUE_SPLITTER);
			if (splits.length > 1) {
				return splits[1].split(FORMAT_SPLITTER)[0];
			}
		}
		return StringUtils.EMPTY;
	}

	@Override
	public Optional<String> getScript() {
		String value = getEntity().getScript();
		if (value != null && !value.isEmpty()) {
			return Optional.of(value);
		}
		return Optional.empty();
	}

	@Override
	public void setScript(String script) {
		getEntity().setScript(script);
	}

	@Override
	public int getDecimalPlace() {
		String value = getEntity().getDecimalplace();
		if (value != null && !value.isEmpty()) {
			return Integer.valueOf(value);
		}
		return -1;
	}

	@Override
	public void setDecimalPlace(int value) {
		getEntity().setDecimalplace(Integer.toString(value));
	}

	@Override
	public Optional<String> getOriginUri() {
		String value = getEntity().getOriginuri();
		if (value != null && !value.isEmpty()) {
			return Optional.of(value);
		}
		return Optional.empty();
	}

	@Override
	public void setOriginUri(String uri) {
		getEntity().setOriginuri(uri);
	}

	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IXid getXid(String domain) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner(", ");
		getCoding().stream().forEach(c -> sj.add(c.getCode() + "|" + c.getSystem()));
		return super.toString() + " coding [" + sj.toString() + "]";
	}
}
