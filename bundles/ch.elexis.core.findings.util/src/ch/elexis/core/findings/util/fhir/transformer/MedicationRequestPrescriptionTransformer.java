package ch.elexis.core.findings.util.fhir.transformer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Dosage.DosageDoseAndRateComponent;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestDispenseRequestComponent;
import org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestIntent;
import org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestPriority;
import org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestStatus;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Narrative.NarrativeStatus;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Timing.EventTiming;
import org.hl7.fhir.r4.model.Timing.TimingRepeatComponent;
import org.hl7.fhir.r4.model.Type;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.IdentifierSystem;
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.MedicamentCoding;
import ch.elexis.core.findings.util.fhir.transformer.helper.FhirUtil;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IRecipe;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.builder.IPrescriptionBuilder;
import ch.elexis.core.model.builder.IRecipeBuilder;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.utils.CoreUtil;

@Component
public class MedicationRequestPrescriptionTransformer implements IFhirTransformer<MedicationRequest, IPrescription> {

	public static final String EXTENSION_PRESCRIPTION_ENTRYTYPE_URL = "www.elexis.info/extensions/prescription/entrytype";

	@org.osgi.service.component.annotations.Reference(target = "(" + IModelService.SERVICEMODELNAME
			+ "=ch.elexis.core.model)")
	private IModelService modelService;

	@org.osgi.service.component.annotations.Reference
	private IContextService contextService;

	@org.osgi.service.component.annotations.Reference
	private ICodeElementService codeElemetService;

	private PrescriptionEntryTypeFactory entryTypeFactory = new PrescriptionEntryTypeFactory();

	@Override
	public Optional<MedicationRequest> getFhirObject(IPrescription localObject, SummaryEnum summaryEnum,
			Set<Include> includes) {
		MedicationRequest fhirObject = new MedicationRequest();
		FhirUtil.setVersionedIdPartLastUpdatedMeta(MedicationRequest.class, fhirObject, localObject);

		MedicationRequestStatus statusEnum = MedicationRequestStatus.ACTIVE;
		MedicationRequestIntent intentEnum = MedicationRequestIntent.ORDER;
		if (localObject.getEntryType() == EntryType.RECIPE) {
			fhirObject.setGroupIdentifier(getRecipeGroupIdentifier(localObject));
		} else {
			intentEnum = MedicationRequestIntent.PLAN;
		}

		fhirObject.addIdentifier(getElexisObjectIdentifier(localObject));

		fhirObject.setSubject(FhirUtil.getReference(localObject.getPatient()));

		if (localObject.getPrescriptor() != null && localObject.getPrescriptor().isMandator()) {
			IMandator mandator = CoreModelServiceHolder.get()
					.load(localObject.getPrescriptor().getId(), IMandator.class).get();
			fhirObject.setRecorder(FhirUtil.getReference(mandator));
		}

		StringBuilder textBuilder = new StringBuilder();

		CodeableConcept medication = new CodeableConcept();
		String gtin = getArticleGtin(localObject);
		String atc = getArticleAtc(localObject);
		String articelLabel = getArticleLabel(localObject);
		if (gtin != null) {
			Coding coding = medication.addCoding();
			coding.setSystem(MedicamentCoding.GTIN.getOid());
			coding.setCode(gtin);
			coding.setDisplay(articelLabel);
		}
		if (atc != null) {
			Coding coding = medication.addCoding();
			coding.setSystem(MedicamentCoding.ATC.getOid());
			coding.setCode(atc);
		}
		medication.setText(articelLabel);
		textBuilder.append(articelLabel);

		medication.setText(textBuilder.toString());
		fhirObject.setMedication(medication);

		Period dispensePeriod = new Period();
		LocalDateTime dateFrom = localObject.getDateFrom();
		if (dateFrom != null) {
			Date time = Date.from(dateFrom.atZone(ZoneId.systemDefault()).toInstant());
			dispensePeriod.setStart(time);
		}
		LocalDateTime dateUntil = localObject.getDateTo();
		if (dateUntil != null) {
			Date time = Date.from(dateUntil.atZone(ZoneId.systemDefault()).toInstant());
			dispensePeriod.setEnd(time);

			String reasonText = localObject.getStopReason();
			if (reasonText != null && !reasonText.isEmpty()) {
				Annotation note = fhirObject.addNote();
				note.setText("Stop: " + reasonText);
				statusEnum = MedicationRequestStatus.STOPPED;
			}
		}

		MedicationRequestDispenseRequestComponent dispenseRequest = new MedicationRequestDispenseRequestComponent();
		dispenseRequest.setValidityPeriod(dispensePeriod);
		fhirObject.setDispenseRequest(dispenseRequest);

		if (dateUntil != null) {
			if (dateUntil.isBefore(LocalDateTime.now()) || dateUntil.isEqual(dateFrom)) {
				if (statusEnum != MedicationRequestStatus.STOPPED) {
					statusEnum = MedicationRequestStatus.COMPLETED;
				}
			}
		}

		String dose = localObject.getDosageInstruction();
		Dosage dosage = null;
		if (dose != null && !dose.isEmpty()) {
			textBuilder.append(", ").append(dose);
			if (dosage == null) {
				dosage = fhirObject.addDosageInstruction();
			}
			dosage.setText(dose);
		}
		String disposalComment = localObject.getDisposalComment();
		if (disposalComment != null && !disposalComment.isEmpty()) {
			textBuilder.append(", ").append(disposalComment);
			if (dosage == null) {
				dosage = fhirObject.addDosageInstruction();
			}
			CodeableConcept additional = dosage.addAdditionalInstruction();
			additional.setText(disposalComment);
		}
		String remark = localObject.getRemark();
		if (remark != null && !remark.isEmpty()) {
			textBuilder.append(", ").append(remark);
			Annotation annotation = new Annotation();
			annotation.setText(remark);
			fhirObject.addNote(annotation);
		}

		fhirObject.setStatus(statusEnum);
		fhirObject.setIntent(intentEnum);
		fhirObject.setPriority(MedicationRequestPriority.ROUTINE);

		Narrative narrative = new Narrative();
		narrative.setStatus(NarrativeStatus.GENERATED);
		ModelUtil.setNarrativeFromString(narrative, textBuilder.toString());

		Extension elexisEntryType = new Extension();
		elexisEntryType.setUrl(EXTENSION_PRESCRIPTION_ENTRYTYPE_URL);
		EntryType entryType = localObject.getEntryType();
		elexisEntryType.setValue(new Enumeration<>(entryTypeFactory, entryType));
		fhirObject.addExtension(elexisEntryType);
		return Optional.of(fhirObject);
	}

	private Identifier getRecipeGroupIdentifier(IPrescription localObject) {
		if (localObject.getRecipe() != null) {
			Identifier ret = new Identifier();
			ret.setSystem(IdentifierSystem.ELEXIS_RECIPE.getSystem());
			ret.setValue(localObject.getRecipe().getId());
			return ret;
		}
		return null;
	}

	@Override
	public Optional<IPrescription> getLocalObject(MedicationRequest fhirObject) {
		String id = fhirObject.getIdElement().getIdPart();
		if (id != null && !id.isEmpty()) {
			return modelService.load(id, IPrescription.class);
		}
		return Optional.empty();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return MedicationRequest.class.equals(fhirClazz) && IPrescription.class.equals(localClazz);
	}

	@Override
	public Optional<IPrescription> updateLocalObject(MedicationRequest fhirObject, IPrescription localObject) {
		Optional<MedicationRequest> localFhirObject = getFhirObject(localObject);
		if (!fhirObject.equalsDeep(localFhirObject.get())) {
			if (isStopUpdate(fhirObject)) {
				Date dateTo = fhirObject.getDispenseRequest().getValidityPeriod().getEnd();
				localObject.setDateTo(dateTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
				localObject.setStopReason("Gestoppt");
				modelService.save(localObject);
				return Optional.of(localObject);
			} else {
				// a change means we need to stop the current prescription
				localObject.setDateTo(LocalDateTime.now());
				localObject.setStopReason("Geändert durch FHIR Server");
				modelService.save(localObject);
				// and create a new one with the changed properties
				return createLocalObject(fhirObject);
			}
		}
		return Optional.empty();
	}

	private boolean isStopUpdate(MedicationRequest fhirObject) {
		if ((fhirObject.getStatus() == MedicationRequestStatus.STOPPED)) {
			if (fhirObject.hasDispenseRequest() && fhirObject.getDispenseRequest().hasValidityPeriod()) {
				return fhirObject.getDispenseRequest().getValidityPeriod().hasEnd();
			}
		}
		return false;
	}

	private String getArticleGtin(IPrescription localObject) {
		IArticle localArticle = localObject.getArticle();
		if (localArticle != null) {
			return localArticle.getGtin();
		}
		return null;
	}

	private String getArticleAtc(IPrescription localObject) {
		IArticle localArticle = localObject.getArticle();
		if (localArticle != null) {
			return localArticle.getAtcCode();
		}
		return null;
	}

	private String getArticleLabel(IPrescription localObject) {
		IArticle localArticle = localObject.getArticle();
		if (localArticle != null) {
			return localArticle.getLabel();
		}
		return "Unknown article";
	}

	@Override
	public Optional<IPrescription> createLocalObject(MedicationRequest fhirObject) {
		Optional<IArticle> item = Optional.empty();
		Optional<String> gtin = getMedicationRequestGtin(fhirObject);
		if (gtin.isPresent()) {
			// lookup item
			item = codeElemetService.findArticleByGtin(gtin.get());
			if (item.isEmpty() && CoreUtil.isTestMode()) {
				IQuery<IArticle> query = modelService.getQuery(IArticle.class);
				query.and(ModelPackage.Literals.IARTICLE__GTIN, COMPARATOR.EQUALS, gtin.get());
				item = query.executeSingleResult();
			}
		} else {
			LoggerFactory.getLogger(getClass()).error("MedicationRequest with no gtin");
		}
		// lookup patient
		Optional<IPatient> patient = modelService.load(FhirUtil.getId(fhirObject.getSubject()).orElse(null),
				IPatient.class);
		Optional<IMandator> mandator = modelService.load(FhirUtil.getId(fhirObject.getRecorder()).orElse(null),
				IMandator.class);
		if (!mandator.isPresent()) {
			mandator = modelService.load(FhirUtil.getId(fhirObject.getRequester()).orElse(null), IMandator.class);
		}
		if (item.isPresent() && patient.isPresent() && mandator.isPresent()) {
			IPrescription localObject = new IPrescriptionBuilder(modelService, contextService, item.get(),
					patient.get(), getMedicationRequestDosage(fhirObject)).build();

			Optional<LocalDateTime> startDateTime = getMedicationRequestStartDateTime(fhirObject);
			startDateTime.ifPresent(date -> localObject.setDateFrom(date));

			Optional<LocalDateTime> endDateTime = getMedicationRequestEndDateTime(fhirObject);
			endDateTime.ifPresent(date -> localObject.setDateFrom(date));

			localObject.setDisposalComment(getMedicationRequestAdditionalInstructions(fhirObject));

			localObject.setRemark(getMedicationRequestRemark(fhirObject));

			Optional<EntryType> prescriptionType = getMedicationRequestPrescriptionType(fhirObject);
			prescriptionType.ifPresent(entryType -> localObject.setEntryType(entryType));

			if (fhirObject.getIntent() == MedicationRequestIntent.ORDER) {
				localObject.setEntryType(EntryType.RECIPE);
				if (!fhirObject.hasGroupIdentifier() && mandator.isPresent()) {
					IRecipe recipe = new IRecipeBuilder(modelService, patient.get(), mandator.get()).buildAndSave();
					localObject.setRecipe(recipe);
					LoggerFactory.getLogger(getClass())
							.info("MedicationRequest created recipe [" + recipe.getId() + "] for request");
				} else if (fhirObject.hasGroupIdentifier()) {
					Identifier groupIdentifier = fhirObject.getGroupIdentifier();
					if (groupIdentifier != null
							&& IdentifierSystem.ELEXIS_RECIPE.getSystem().equals(groupIdentifier.getSystem())) {
						modelService.load(groupIdentifier.getValue(), IRecipe.class).ifPresent(recipe -> {
							localObject.setRecipe(recipe);
							LoggerFactory.getLogger(getClass())
									.info("MedicationRequest added request to recipe [" + recipe.getId() + "]");
						});
					}
				}
			}

			modelService.save(localObject);

			return Optional.of(localObject);
		} else {
			if (!item.isPresent()) {
				StringJoiner medicationCodes = new StringJoiner(",");
				Type medication = fhirObject.getMedication();
				if (medication instanceof CodeableConcept) {
					List<Coding> codings = ((CodeableConcept) medication).getCoding();
					for (Coding coding : codings) {
						medicationCodes.add(coding.getSystem() + "|" + coding.getCode());
					}
				}
				LoggerFactory.getLogger(getClass()).error("MedicationRequest with unknown medication ["
						+ medicationCodes.toString() + "]");
			}
			if (!patient.isPresent()) {
				LoggerFactory.getLogger(getClass()).error("MedicationRequest with unknown patient ["
						+ FhirUtil.getId(fhirObject.getSubject()).orElse(null) + "]");
			}
			if (!mandator.isPresent()) {
				LoggerFactory.getLogger(getClass()).error("MedicationRequest with unknown mandator ["
						+ FhirUtil.getId(fhirObject.getRecorder()).orElse(null) + "]");
			}
		}
		return Optional.empty();
	}

	private Optional<EntryType> getMedicationRequestPrescriptionType(MedicationRequest fhirObject) {
		List<Extension> extensionsEntryType = fhirObject.getExtensionsByUrl(EXTENSION_PRESCRIPTION_ENTRYTYPE_URL);
		for (Extension extension : extensionsEntryType) {
			try {
				EntryType entryType = EntryType.valueOf(((CodeType) extension.getValue()).getValue());
				return Optional.of(entryType);
			} catch (IllegalArgumentException iae) {
			}
		}
		return Optional.empty();
	}

	private Optional<LocalDateTime> getMedicationRequestEndDateTime(MedicationRequest fhirObject) {
		MedicationRequestDispenseRequestComponent dispenseRequest = fhirObject.getDispenseRequest();
		if (dispenseRequest != null) {
			Period period = dispenseRequest.getValidityPeriod();
			if (period != null && period.hasEnd()) {
				Date endDate = period.getEnd();
				return Optional.of(LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault()));
			}
		}
		return Optional.empty();
	}

	private Optional<LocalDateTime> getMedicationRequestStartDateTime(MedicationRequest fhirObject) {
		MedicationRequestDispenseRequestComponent dispenseRequest = fhirObject.getDispenseRequest();
		if (dispenseRequest != null) {
			Period period = dispenseRequest.getValidityPeriod();
			if (period != null && period.hasStart()) {
				Date startDate = period.getStart();
				return Optional.of(LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault()));
			}
		}
		return Optional.empty();
	}

	private String getMedicationRequestRemark(MedicationRequest fhirObject) {
		List<Annotation> notes = fhirObject.getNote();
		StringBuilder sb = new StringBuilder();
		for (Annotation annotation : notes) {
			String text = annotation.getText();
			if (text != null) {
				if (sb.length() == 0) {
					sb.append(text);
				} else {
					sb.append(", ").append(text);
				}
			}
		}
		return sb.toString();
	}

	private String getMedicationRequestAdditionalInstructions(MedicationRequest fhirObject) {
		List<Dosage> instructions = fhirObject.getDosageInstruction();
		StringBuilder sb = new StringBuilder();
		for (Dosage dosage : instructions) {
			List<CodeableConcept> additionals = dosage.getAdditionalInstruction();
			for (CodeableConcept codeableConcept : additionals) {
				String text = codeableConcept.getText();
				if (text != null) {
					if (sb.length() == 0) {
						sb.append(text);
					} else {
						sb.append(", ").append(text);
					}
				}
			}
		}
		return sb.toString();
	}

	private Optional<String> getMedicationRequestGtin(MedicationRequest fhirObject) {
		Type medication = fhirObject.getMedication();
		if (medication instanceof CodeableConcept) {
			List<Coding> codings = ((CodeableConcept) medication).getCoding();
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

	private String getMedicationRequestDosage(MedicationRequest fhirObject) {
		List<Dosage> instructions = fhirObject.getDosageInstruction();
		StringBuilder sb = new StringBuilder();
		double morn = 0.0;
		double noon = 0.0;
		double aft = 0.0;
		double eve = 0.0;
		for (Dosage dosage : instructions) {
			String text = dosage.getText();
			if (text != null) {
				if (sb.length() == 0) {
					sb.append(text);
				} else {
					sb.append(", ").append(text);
				}
			} else if (dosage.hasTiming() && dosage.getTiming().hasRepeat() && dosage.hasDoseAndRate()) {
				List<DosageDoseAndRateComponent> doseAnRate = dosage.getDoseAndRate();
				for (DosageDoseAndRateComponent doseAndRate : doseAnRate) {
					double amount = 0.0;
					if (doseAndRate.hasRateQuantity()) {
						amount = doseAndRate.getRateQuantity().getValue().doubleValue();
					}
					if (doseAndRate.hasDoseQuantity()) {
						amount = doseAndRate.getDoseQuantity().getValue().doubleValue();
					}
					if (isDosageAt(EventTiming.MORN, dosage)) {
						morn = amount;
					} else if (isDosageAt(EventTiming.NOON, dosage)) {
						noon = amount;
					} else if (isDosageAt(EventTiming.AFT, dosage)) {
						aft = amount;
					} else if (isDosageAt(EventTiming.EVE, dosage)) {
						eve = amount;
					}
				}
			}
		}
		if (morn > 0.0 || noon > 0.0 || aft > 0.0 || eve > 0.0) {
			StringJoiner sj = new StringJoiner("-");
			sj.add(getDoubleString(morn)).add(getDoubleString(noon)).add(getDoubleString(aft))
					.add(getDoubleString(eve));
			if (sb.length() == 0) {
				sb.append(sj.toString());
			} else {
				sb.append(", ").append(sj.toString());
			}
		}
		return sb.toString();
	}

	private String getDoubleString(Double value) {
		if (value % 1 == 0) {
			return Integer.toString(value.intValue());
		} else {
			return Double.toString(value);
		}
	}

	private boolean isDosageAt(EventTiming eventTiming, Dosage dosage) {
		TimingRepeatComponent repeat = dosage.getTiming().getRepeat();
		if (repeat != null) {
			for (Enumeration<EventTiming> when : repeat.getWhen()) {
				if (when.getValue() == eventTiming) {
					return true;
				}
			}
		}
		return false;
	}
}
