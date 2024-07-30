package ch.elexis.core.findings.util.fhir.transformer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Immunization.ImmunizationStatus;
import org.hl7.fhir.r4.model.Type;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.MedicamentCoding;
import ch.elexis.core.findings.util.fhir.transformer.helper.FhirUtil;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IVaccination;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.builder.IVaccinationBuilder;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
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

	@Override
	public Optional<Immunization> getFhirObject(IVaccination localObject, SummaryEnum summaryEnum,
			Set<Include> includes) {
		Immunization fhirObject = new Immunization();
		FhirUtil.setVersionedIdPartLastUpdatedMeta(Immunization.class, fhirObject, localObject);

		fhirObject.addIdentifier(getElexisObjectIdentifier(localObject));

		fhirObject.setStatus(ImmunizationStatus.COMPLETED);

		fhirObject.setPatient(FhirUtil.getReference(localObject.getPatient()));

		if (localObject.getPerformer() != null && localObject.getPerformer().isMandator()) {
			IMandator mandator = CoreModelServiceHolder.get()
					.load(localObject.getPerformer().getId(), IMandator.class).get();
			fhirObject.addPerformer().setActor(FhirUtil.getReference(mandator));
		}

		StringBuilder textBuilder = new StringBuilder();

		CodeableConcept vaccine = new CodeableConcept();
		String gtin = localObject.getArticleGtin();
		String atc = localObject.getArticleAtc();
		String articelLabel = localObject.getArticleName();
		if (gtin != null) {
			Coding coding = vaccine.addCoding();
			coding.setSystem(MedicamentCoding.GTIN.getOid());
			coding.setCode(gtin);
			coding.setDisplay(articelLabel);
		}
		if (atc != null) {
			Coding coding = vaccine.addCoding();
			coding.setSystem(MedicamentCoding.ATC.getOid());
			coding.setCode(atc);
		}
		vaccine.setText(articelLabel);
		textBuilder.append(articelLabel);
		vaccine.setText(textBuilder.toString());
		fhirObject.setVaccineCode(vaccine);

		fhirObject.setLotNumber(localObject.getLotNumber());

		if (StringUtils.isNotBlank(localObject.getIngredientsAtc())) {
			CodeableConcept reasonCode = fhirObject.addReasonCode();
			String[] parts = localObject.getIngredientsAtc().split(",");
			for (String ingredientAtc : parts) {
				Coding coding = reasonCode.addCoding();
				coding.setSystem(MedicamentCoding.ATC.getOid());
				coding.setCode(ingredientAtc);
			}
		}

		LocalDate dateOfAdministration = localObject.getDateOfAdministration();
		if (dateOfAdministration != null) {
			Date date = Date.from(dateOfAdministration.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
			fhirObject.setOccurrence(new DateTimeType(date));
		}
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

		return Optional.empty();
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
		Optional<IMandator> mandator = Optional.empty();
		if (fhirObject.hasPerformer() && fhirObject.getPerformerFirstRep().hasActor()) {
			mandator = modelService
					.load(FhirUtil.getId(fhirObject.getPerformerFirstRep().getActor()).orElse(null), IMandator.class);
		}
		if (patient.isPresent()) {
			IVaccination localObject = null;
			if (item.isPresent()) {
				localObject = new IVaccinationBuilder(modelService, contextService, item.get(), patient.get())
					.build();
			} else {
				localObject = new IVaccinationBuilder(modelService, contextService,
						displayName.orElse("unknown vaccine"), gtin.orElse(null), atc.orElse(null), patient.get())
						.build();
			}
			if (mandator.isPresent()) {
				localObject.setPerformer(mandator.get());
			}

			if (fhirObject.hasOccurrenceDateTimeType()) {
				LocalDate occurance = LocalDateTime
						.ofInstant(fhirObject.getOccurrenceDateTimeType().getValue().toInstant(),
								ZoneId.systemDefault())
						.toLocalDate();
				localObject.setDateOfAdministration(occurance);
			}

			if (fhirObject.hasLotNumber()) {
				localObject.setLotNumber(fhirObject.getLotNumber());
			}

			if (fhirObject.hasReasonCode()) {
				List<String> atcCodes = FhirUtil.getCodesFromConceptList(MedicamentCoding.ATC.getOid(),
						fhirObject.getReasonCode());
				if (!atcCodes.isEmpty()) {
					localObject.setIngredientsAtc(atcCodes.stream().collect(Collectors.joining(",")));
				}
			}

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
