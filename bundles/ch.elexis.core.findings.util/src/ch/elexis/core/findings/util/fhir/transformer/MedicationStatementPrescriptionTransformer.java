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
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.MedicationStatement.MedicationStatementStatus;
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
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.MedicamentCoding;
import ch.elexis.core.findings.util.fhir.transformer.helper.FhirUtil;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.builder.IPrescriptionBuilder;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.utils.CoreUtil;

@Component
public class MedicationStatementPrescriptionTransformer
		implements IFhirTransformer<MedicationStatement, IPrescription> {

	public static final String EXTENSION_PRESCRIPTION_ENTRYTYPE_URL = "www.elexis.info/extensions/prescription/entrytype";

	@org.osgi.service.component.annotations.Reference(target = "(" + IModelService.SERVICEMODELNAME
			+ "=ch.elexis.core.model)")
	private IModelService modelService;

	@org.osgi.service.component.annotations.Reference
	private IContextService contextService;

	@org.osgi.service.component.annotations.Reference
	private ICodeElementService codeElementService;

	private PrescriptionEntryTypeFactory entryTypeFactory = new PrescriptionEntryTypeFactory();

	@Override
	public Optional<MedicationStatement> getFhirObject(IPrescription localObject, SummaryEnum summaryEnum,
			Set<Include> includes) {
		MedicationStatement fhirObject = new MedicationStatement();
		FhirUtil.setVersionedIdPartLastUpdatedMeta(MedicationStatement.class, fhirObject, localObject);

		MedicationStatementStatus statusEnum = MedicationStatementStatus.ACTIVE;
		if (localObject.getDateTo() != null && localObject.getDateTo().isBefore(LocalDateTime.now())) {
			statusEnum = MedicationStatementStatus.COMPLETED;
		}

		fhirObject.addIdentifier(getElexisObjectIdentifier(localObject));

		fhirObject.setSubject(FhirUtil.getReference(localObject.getPatient()));

		if (localObject.getPrescriptor() != null && localObject.getPrescriptor().isMandator()) {
			IMandator mandator = CoreModelServiceHolder.get()
					.load(localObject.getPrescriptor().getId(), IMandator.class).get();
			fhirObject.setInformationSource(FhirUtil.getReference(mandator));
		}

		StringBuilder textBuilder = new StringBuilder();

		CodeableConcept medication = new CodeableConcept();

		String gtin = getArticleGtin(localObject);
		String atc = getArticleAtc(localObject);
		String articleLabel = getArticleLabel(localObject);
		if (gtin != null) {
			Coding coding = medication.addCoding();
			coding.setSystem(MedicamentCoding.GTIN.getOid());
			coding.setCode(gtin);
			coding.setDisplay(articleLabel);
		}
		if (atc != null) {
			Coding coding = medication.addCoding();
			coding.setSystem(MedicamentCoding.ATC.getOid());
			coding.setCode(atc);
		}
		medication.setText(articleLabel);
		textBuilder.append(articleLabel);

		fhirObject.setMedication(medication);

		Period effectivePeriod = new Period();
		LocalDateTime dateFrom = localObject.getDateFrom();
		if (dateFrom != null) {
			Date time = Date.from(dateFrom.atZone(ZoneId.systemDefault()).toInstant());
			effectivePeriod.setStart(time);
		}
		LocalDateTime dateUntil = localObject.getDateTo();
		if (dateUntil != null) {
			Date time = Date.from(dateUntil.atZone(ZoneId.systemDefault()).toInstant());
			effectivePeriod.setEnd(time);

			String reasonText = localObject.getStopReason();
			if (reasonText != null && !reasonText.isEmpty()) {
				Annotation note = fhirObject.addNote();
				note.setText("Stop: " + reasonText);
				statusEnum = MedicationStatementStatus.STOPPED;
			}
		}

		fhirObject.setEffective(effectivePeriod);

		String dose = localObject.getDosageInstruction();
		Dosage dosage = null;
		if (dose != null && !dose.isEmpty()) {
			textBuilder.append(", ").append(dose);
			if (dosage == null) {
				dosage = fhirObject.addDosage();
			}
			dosage.setText(dose);
		}
		String disposalComment = localObject.getDisposalComment();
		if (disposalComment != null && !disposalComment.isEmpty()) {
			textBuilder.append(", ").append(disposalComment);
			if (dosage == null) {
				dosage = fhirObject.addDosage();
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

		Narrative narrative = new Narrative();
		narrative.setStatus(NarrativeStatus.GENERATED);
		ModelUtil.setNarrativeFromString(narrative, textBuilder.toString());
		fhirObject.setText(narrative);

		Extension elexisEntryType = new Extension();
		elexisEntryType.setUrl(EXTENSION_PRESCRIPTION_ENTRYTYPE_URL);
		EntryType entryType = localObject.getEntryType();
		elexisEntryType.setValue(new Enumeration<>(entryTypeFactory, entryType));
		fhirObject.addExtension(elexisEntryType);

		return Optional.of(fhirObject);
	}

	@Override
	public Optional<IPrescription> getLocalObject(MedicationStatement fhirObject) {
		String id = fhirObject.getIdElement().getIdPart();
		if (id != null && !id.isEmpty()) {
			return modelService.load(id, IPrescription.class);
		}
		return Optional.empty();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return MedicationStatement.class.equals(fhirClazz) && IPrescription.class.equals(localClazz);
	}

	@Override
	public Optional<IPrescription> updateLocalObject(MedicationStatement fhirObject, IPrescription localObject) {
		Optional<MedicationStatement> localFhirObject = getFhirObject(localObject);
		if (!fhirObject.equalsDeep(localFhirObject.get())) {
			if (isStopUpdate(fhirObject)) {
				Date dateTo = ((Period) fhirObject.getEffective()).getEnd();
				localObject.setDateTo(dateTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
				localObject.setStopReason("Gestoppt");
				modelService.save(localObject);
				return Optional.of(localObject);
			} else {
				localObject.setDateTo(LocalDateTime.now());
				localObject.setStopReason("Geändert durch FHIR Server");
				modelService.save(localObject);
				return createLocalObject(fhirObject);
			}
		}
		return Optional.empty();
	}

	private boolean isStopUpdate(MedicationStatement fhirObject) {
		if ((fhirObject.getStatus() == MedicationStatementStatus.STOPPED)) {
			if (fhirObject.hasEffective() && fhirObject.getEffective() instanceof Period) {
				Period period = (Period) fhirObject.getEffective();
				return period.hasEnd();
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
	public Optional<IPrescription> createLocalObject(MedicationStatement fhirObject) {
		Optional<IArticle> item = Optional.empty();
		Optional<String> gtin = getMedicationStatementGtin(fhirObject);
		if (gtin.isPresent()) {
			item = codeElementService.findArticleByGtin(gtin.get());
			if (item.isEmpty() && CoreUtil.isTestMode()) {
				IQuery<IArticle> query = modelService.getQuery(IArticle.class);
				query.and(ModelPackage.Literals.IARTICLE__GTIN, COMPARATOR.EQUALS, gtin.get());
				item = query.executeSingleResult();
			}
		} else {
			LoggerFactory.getLogger(getClass()).error("MedicationStatement ohne GTIN");
		}

		Optional<IPatient> patient = modelService.load(FhirUtil.getId(fhirObject.getSubject()).orElse(null),
				IPatient.class);
		Optional<IMandator> mandator = modelService.load(FhirUtil.getId(fhirObject.getInformationSource()).orElse(null),
				IMandator.class);
		if (item.isPresent() && patient.isPresent() && mandator.isPresent()) {
			IPrescription localObject = new IPrescriptionBuilder(modelService, contextService, item.get(),
					patient.get(), getMedicationStatementDosage(fhirObject)).build();

			Optional<LocalDateTime> startDateTime = getMedicationStatementStartDateTime(fhirObject);
			startDateTime.ifPresent(localObject::setDateFrom);

			Optional<LocalDateTime> endDateTime = getMedicationStatementEndDateTime(fhirObject);
			endDateTime.ifPresent(localObject::setDateTo);

			localObject.setDisposalComment(getMedicationStatementAdditionalInstructions(fhirObject));

			localObject.setRemark(getMedicationStatementRemark(fhirObject));

			Optional<EntryType> prescriptionType = getMedicationStatementPrescriptionType(fhirObject);
			prescriptionType.ifPresent(localObject::setEntryType);

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
				LoggerFactory.getLogger(getClass())
						.error("MedicationStatement mit unbekanntem Medikament [" + medicationCodes.toString() + "]");
			}
			if (!patient.isPresent()) {
				LoggerFactory.getLogger(getClass()).error("MedicationStatement mit unbekanntem Patienten ["
						+ FhirUtil.getId(fhirObject.getSubject()).orElse(null) + "]");
			}
			if (!mandator.isPresent()) {
				LoggerFactory.getLogger(getClass()).error("MedicationStatement mit unbekanntem Mandator ["
						+ FhirUtil.getId(fhirObject.getInformationSource()).orElse(null) + "]");
			}
		}
		return Optional.empty();
	}

	private Optional<EntryType> getMedicationStatementPrescriptionType(MedicationStatement fhirObject) {
		List<Extension> extensionsEntryType = fhirObject.getExtensionsByUrl(EXTENSION_PRESCRIPTION_ENTRYTYPE_URL);
		for (Extension extension : extensionsEntryType) {
			try {
				EntryType entryType = EntryType.valueOf(((CodeType) extension.getValue()).getValue());
				return Optional.of(entryType);
			} catch (IllegalArgumentException iae) {
				// Ignorieren
			}
		}
		return Optional.empty();
	}

	private Optional<LocalDateTime> getMedicationStatementEndDateTime(MedicationStatement fhirObject) {
		if (fhirObject.hasEffective() && fhirObject.getEffective() instanceof Period) {
			Period period = (Period) fhirObject.getEffective();
			if (period.hasEnd()) {
				Date endDate = period.getEnd();
				return Optional.of(LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault()));
			}
		}
		return Optional.empty();
	}

	private Optional<LocalDateTime> getMedicationStatementStartDateTime(MedicationStatement fhirObject) {
		if (fhirObject.hasEffective() && fhirObject.getEffective() instanceof Period) {
			Period period = (Period) fhirObject.getEffective();
			if (period.hasStart()) {
				Date startDate = period.getStart();
				return Optional.of(LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault()));
			}
		}
		return Optional.empty();
	}

	private String getMedicationStatementRemark(MedicationStatement fhirObject) {
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

	private String getMedicationStatementAdditionalInstructions(MedicationStatement fhirObject) {
		List<Dosage> instructions = fhirObject.getDosage();
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

	private Optional<String> getMedicationStatementGtin(MedicationStatement fhirObject) {
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

	private String getMedicationStatementDosage(MedicationStatement fhirObject) {
		List<Dosage> instructions = fhirObject.getDosage();
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
				List<Dosage.DosageDoseAndRateComponent> doseAndRate = dosage.getDoseAndRate();
				for (Dosage.DosageDoseAndRateComponent doseRate : doseAndRate) {
					double amount = 0.0;
					if (doseRate.hasRateQuantity()) {
						amount = doseRate.getRateQuantity().getValue().doubleValue();
					}
					if (doseRate.hasDoseQuantity()) {
						amount = doseRate.getDoseQuantity().getValue().doubleValue();
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
