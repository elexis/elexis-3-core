package ch.elexis.core.findings.util.fhir.transformer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Type;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.MedicamentCoding;
import ch.elexis.core.findings.util.fhir.transformer.helper.FhirUtil;
import ch.elexis.core.findings.util.fhir.transformer.mapper.IVaccinationImmunizationAttributeMapper;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IVaccination;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.builder.IVaccinationBuilder;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.utils.CoreUtil;

@Component
public class ImmunizationVaccinationTransformer implements IFhirTransformer<Immunization, IVaccination> {

	@org.osgi.service.component.annotations.Reference(target = "(" + IModelService.SERVICEMODELNAME
			+ "=ch.elexis.core.model)")
	private IModelService modelService;

	@org.osgi.service.component.annotations.Reference
	private IContextService contextService;

	@org.osgi.service.component.annotations.Reference
	private ICodeElementService codeElemetService;

	private IVaccinationImmunizationAttributeMapper attributeMapper;

	@Activate
	public void activate() {
		attributeMapper = new IVaccinationImmunizationAttributeMapper(modelService);
	}

	@Override
	public Optional<Immunization> getFhirObject(IVaccination localObject, SummaryEnum summaryEnum,
			Set<Include> includes) {
		Immunization fhirObject = new Immunization();
		attributeMapper.elexisToFhir(localObject, fhirObject, summaryEnum, includes);
		return Optional.of(fhirObject);
	}

	@Override
	public Optional<IVaccination> getLocalObject(Immunization fhirObject) {
		String id = fhirObject.getIdElement().getIdPart();
		if (id != null && !id.isEmpty()) {
			return modelService.load(id, IVaccination.class);
		}
		return Optional.empty();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Immunization.class.equals(fhirClazz) && IVaccination.class.equals(localClazz);
	}

	@Override
	public Optional<IVaccination> updateLocalObject(Immunization fhirObject, IVaccination localObject) {
		attributeMapper.fhirToElexis(fhirObject, localObject);
		modelService.save(localObject);
		return Optional.of(localObject);
	}

	@Override
	public Optional<IVaccination> createLocalObject(Immunization fhirObject) {
		Optional<IArticle> item = Optional.empty();
		Optional<String> gtin = getImmunizationGtin(fhirObject);
		Optional<String> atc = getImmunizationAtc(fhirObject);
		Optional<String> displayName = getImmunizationDisplay(fhirObject);
		if (gtin.isPresent()) {
			// lookup item
			item = codeElemetService.findArticleByGtin(gtin.get());
			if (item.isEmpty() && CoreUtil.isTestMode()) {
				IQuery<IArticle> query = modelService.getQuery(IArticle.class);
				query.and(ModelPackage.Literals.IARTICLE__GTIN, COMPARATOR.EQUALS, gtin.get());
				item = query.executeSingleResult();
			}
		} else {
			LoggerFactory.getLogger(getClass()).warn("Immunization with no gtin");
		}
		// lookup patient
		Optional<IPatient> patient = modelService.load(FhirUtil.getId(fhirObject.getPatient()).orElse(null),
				IPatient.class);
		if (patient.isPresent()) {
			IVaccination localObject = null;
			if (item.isPresent()) {
				localObject = new IVaccinationBuilder(modelService, contextService, item.get(), patient.get()).build();
			} else {
				localObject = new IVaccinationBuilder(modelService, contextService,
						displayName.orElse("unknown vaccine"), gtin.orElse(null), atc.orElse(null), patient.get())
						.build();
			}
			attributeMapper.fhirToElexis(fhirObject, localObject);
			modelService.save(localObject);
			return Optional.of(localObject);
		} else {
			LoggerFactory.getLogger(getClass()).error(
					"Immunization with unknown patient [" + FhirUtil.getId(fhirObject.getPatient()).orElse(null) + "]");
		}
		return Optional.empty();
	}

	private Optional<String> getImmunizationDisplay(Immunization fhirObject) {
		Type vaccination = fhirObject.getVaccineCode();
		if (vaccination instanceof CodeableConcept) {
			List<Coding> codings = ((CodeableConcept) vaccination).getCoding();
			for (Coding coding : codings) {
				String codeSystem = coding.getSystem();
				if (MedicamentCoding.GTIN.getUrl().equals(codeSystem)
						|| MedicamentCoding.GTIN.getOid().equals(codeSystem)) {
					return Optional.of(coding.getDisplay());
				}
			}
		}
		return Optional.empty();
	}

	private Optional<String> getImmunizationAtc(Immunization fhirObject) {
		Type vaccination = fhirObject.getVaccineCode();
		if (vaccination instanceof CodeableConcept) {
			List<Coding> codings = ((CodeableConcept) vaccination).getCoding();
			for (Coding coding : codings) {
				String codeSystem = coding.getSystem();
				if (MedicamentCoding.ATC.getUrl().equals(codeSystem)
						|| MedicamentCoding.ATC.getOid().equals(codeSystem)) {
					return Optional.of(coding.getCode());
				}
			}
		}
		return Optional.empty();
	}

	private Optional<String> getImmunizationGtin(Immunization fhirObject) {
		Type vaccination = fhirObject.getVaccineCode();
		if (vaccination instanceof CodeableConcept) {
			List<Coding> codings = ((CodeableConcept) vaccination).getCoding();
			for (Coding coding : codings) {
				String codeSystem = coding.getSystem();
				if (MedicamentCoding.GTIN.getUrl().equals(codeSystem)
						|| MedicamentCoding.GTIN.getOid().equals(codeSystem)) {
					return Optional.of(coding.getCode());
				}
			}
		}
		return Optional.empty();
	}

}
