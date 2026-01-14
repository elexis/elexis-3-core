package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.Condition;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.mapper.r4.ISickCertificateConditionAttributeMapper;
import ch.elexis.core.fhir.mapper.r4.util.FhirUtil;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.model.ISickCertificate;
import ch.elexis.core.services.IModelService;

@Component(property = IFhirTransformer.TRANSFORMERID + "=Condition.ISickCertificate")
public class ConditionISickCertificateTransformer implements IFhirTransformer<Condition, ISickCertificate> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	private ISickCertificateConditionAttributeMapper attributeMapper;

	public ConditionISickCertificateTransformer() {
		attributeMapper = new ISickCertificateConditionAttributeMapper(coreModelService);
	}

	@Override
	public Optional<Condition> getFhirObject(ISickCertificate localObject, SummaryEnum summaryEnum,
			Set<Include> includes) {
		Condition condition = new Condition();
		attributeMapper.elexisToFhir(localObject, condition, summaryEnum);
		FhirUtil.setVersionedIdPartLastUpdatedMeta(Condition.class, condition, localObject);
		return Optional.of(condition);
	}

	@Override
	public Optional<ISickCertificate> getLocalObject(Condition fhirObject) {
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<String> localId = FhirUtil.getLocalId(fhirObject.getId());
			if (localId.isPresent()) {
				return coreModelService.load(localId.get(), ISickCertificate.class);
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<ISickCertificate> updateLocalObject(Condition fhirObject, ISickCertificate localObject) {
		attributeMapper.fhirToElexis(fhirObject, localObject);
		coreModelService.save(localObject);
		return Optional.of(localObject);
	}

	@Override
	public Optional<ISickCertificate> createLocalObject(Condition fhirObject) {
		ISickCertificate create = coreModelService.create(ISickCertificate.class);
		attributeMapper.fhirToElexis(fhirObject, create);
		coreModelService.save(create);
		return Optional.of(create);
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Condition.class.equals(fhirClazz) && ISickCertificate.class.equals(localClazz);

	}

}
