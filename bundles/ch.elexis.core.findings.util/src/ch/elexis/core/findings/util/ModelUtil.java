package ch.elexis.core.findings.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Enumerations.DocumentReferenceStatus;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.IClinicalImpression;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IDocumentReference;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFamilyMemberHistory;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservationLink;
import ch.elexis.core.findings.IObservationLink.ObservationLinkType;
import ch.elexis.core.findings.IProcedureRequest;
import ch.elexis.core.findings.ObservationComponent;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.util.model.CodingWrapper;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.INamedQuery;

public class ModelUtil {

	public static <T> Optional<T> loadFinding(String id, Class<T> clazz) {
		if (id != null) {
			return FindingsModelServiceHolder.get().load(id, clazz);
		}
		return Optional.empty();
	}

	public static <T> INamedQuery<T> getFindingsNamedQuery(Class<T> clazz, String... properties) {
		return FindingsModelServiceHolder.get().getNamedQuery(clazz, properties);
	}

	public static <T> T createFinding(Class<T> clazz) {
		return FindingsModelServiceHolder.get().create(clazz);
	}

	public static void saveFinding(Identifiable identifiable) {
		FindingsModelServiceHolder.get().save(identifiable);
	}

	public static void deleteFinding(Deleteable deleteable) {
		FindingsModelServiceHolder.get().delete(deleteable);
	}

	private static FhirContext context;

	private static IParser getJsonParser() {
		if (context == null) {
			context = FhirContext.forR4();
		}
		return context.newJsonParser();
	}

	public static IBaseResource getAsResource(String jsonResource) {
		return getJsonParser().parseResource(jsonResource);
	}

	public static Optional<IBaseResource> loadResource(IFinding finding) throws DataFormatException {
		IBaseResource resource = null;
		String rawContent = finding.getRawContent();
		if (rawContent != null && !rawContent.isEmpty()) {
			// always convert to newest json format
			if (!FindingsFormatUtil.isCurrentFindingsFormat(rawContent)) {
				Optional<String> convertedContent = FindingsFormatUtil.convertToCurrentFindingsFormat(rawContent);
				if (convertedContent.isPresent()) {
					rawContent = convertedContent.get();
				}
			}
			try {
				resource = getAsResource(rawContent);
			} catch (DataFormatException e) {
				// try to escape html entities and update raw content
				rawContent = StringEscapeUtils.unescapeHtml4(rawContent);
				resource = getAsResource(rawContent);
				finding.setRawContent(rawContent);
				FindingsModelServiceHolder.get().save(finding);
			}
		}
		return Optional.ofNullable(resource);
	}

	public static void saveResource(IBaseResource resource, IFinding finding) throws DataFormatException {
		if (resource != null) {
			String resourceJson = getJsonParser().encodeResourceToString(resource);
			finding.setRawContent(resourceJson);
		}
	}

	public static String getFhirJson(IBaseResource resource) {
		if (resource != null) {
			return getJsonParser().encodeResourceToString(resource);
		}
		return null;
	}

	public static void setCodingsToConcept(CodeableConcept codeableConcept, List<ICoding> coding) {
		codeableConcept.getCoding().clear();
		for (ICoding iCoding : coding) {
			setCodingToConcept(codeableConcept, iCoding);
		}
	}

	public static void setCodingToConcept(CodeableConcept codeableConcept, ICoding iCoding) {
		codeableConcept.addCoding(new Coding(iCoding.getSystem(), iCoding.getCode(), iCoding.getDisplay()));
	}

	public static List<ICoding> getCodingsFromConcept(CodeableConcept codeableConcept) {
		ArrayList<ICoding> ret = new ArrayList<>();
		List<Coding> coding = codeableConcept.getCoding();
		for (Coding code : coding) {
			ret.add(new CodingWrapper(code));
		}
		return ret;
	}

	public static boolean isCodeInList(String system, String code, List<ICoding> list) {
		if (list != null && !list.isEmpty()) {
			for (ICoding iCoding : list) {
				if (iCoding.getSystem().equals(system) && iCoding.getCode().equals(code)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isSystemInList(String system, List<ICoding> list) {
		if (list != null && !list.isEmpty()) {
			for (ICoding iCoding : list) {
				if (iCoding.getSystem().equals(system)) {
					return true;
				}
			}
		}
		return false;
	}

	public static Optional<ICoding> getCodeBySystem(List<ICoding> coding, CodingSystem codingSystem) {
		for (ICoding iCoding : coding) {
			if (codingSystem.getSystem().equals(iCoding.getSystem())) {
				return Optional.of(iCoding);
			}
		}
		return Optional.empty();
	}

	public static Optional<CodeableConcept> getCodeableConceptBySystem(List<CodeableConcept> concepts,
			CodingSystem codingSystem) {
		for (CodeableConcept concept : concepts) {
			Optional<ICoding> found = getCodeBySystem(getCodingsFromConcept(concept), codingSystem);
			if (found.isPresent()) {
				return Optional.of(concept);
			}
		}
		return Optional.empty();
	}

	public static Optional<CodeableConcept> getCodeableConceptBySystem(List<CodeableConcept> concepts,
			String codingSystem) {
		for (CodeableConcept concept : concepts) {
			Optional<ICoding> found = getCodeBySystem(getCodingsFromConcept(concept), codingSystem);
			if (found.isPresent()) {
				return Optional.of(concept);
			}
		}
		return Optional.empty();
	}

	public static @Nullable String getIdentifierBySystem(List<Identifier> identifiers, String system) {
		int identifierIndexBySystem = getIdentifierIndexBySystem(identifiers, system);
		if (identifierIndexBySystem >= 0) {
			return identifiers.get(identifierIndexBySystem).getValue();
		}
		return null;
	}

	public static int getIdentifierIndexBySystem(List<Identifier> identifiers, String system) {
		for (int i = 0; i < identifiers.size(); i++) {
			Identifier _identifier = identifiers.get(i);
			if (Objects.equals(system, _identifier.getSystem())) {
				return i;
			}
		}
		return -1;
	}

	public static Optional<ICoding> getCodeBySystem(List<ICoding> coding, String codingSystem) {
		for (ICoding iCoding : coding) {
			if (codingSystem.equals(iCoding.getSystem())) {
				return Optional.of(iCoding);
			}
		}
		return Optional.empty();
	}

	public static Optional<String> getNarrativeAsString(Narrative narrative) {
		String text = narrative.getDivAsString();
		if (text != null) {
			String divDecodedText = text.replaceAll("[<](/)?div[^>]*[>]", StringUtils.EMPTY);
			divDecodedText = divDecodedText.replaceAll("<br/>|<br />", StringUtils.LF).replaceAll("&amp;", "&")
					.replaceAll("&gt;", ">").replaceAll("&lt;", "<").replaceAll("'&sect;'", "§");
			return Optional.of(divDecodedText);
		}
		return Optional.empty();
	}

	public static void setNarrativeFromString(Narrative narrative, String text) {
		try {
			text = fixXhtmlContent(text);
			String divEncodedText = text.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("§", "'&sect;'")
					.replaceAll("&", "&amp;").replaceAll("(\r\n|\r|\n)", "<br />");
			divEncodedText = addDivToEncodedText(divEncodedText);
			narrative.setDivAsString(divEncodedText);
			narrative.setStatus(Narrative.NarrativeStatus.GENERATED);
		} catch (Exception e) {
			LoggerFactory.getLogger(ModelUtil.class).error("Could not set narrative text [" + text + "]");
			throw (e);
		}
	}

	private static String addDivToEncodedText(String divEncodedText) {
		if (!divEncodedText.startsWith("<div")) {
			divEncodedText = "<div" + " xmlns=\"" + XhtmlNode.XMLNS + "\"" + ">" + divEncodedText + "</div>";
		}
		return divEncodedText;
	}

	/**
	 * Remove characters which cause problems in xhtml.
	 *
	 * @param content
	 * @return content without problem characters
	 */
	private static String fixXhtmlContent(String content) {
		// replace unicode nbsp with space character
		content = content.replace((char) 0xa0, ' ');
		return content;
	}

	public static boolean isSameCoding(ICoding left, ICoding right) {
		return left.getSystem().equals(right.getSystem()) && left.getCode().equals(right.getCode());
	}

	public static void addCodingIfNotPresent(List<ICoding> coding, ICoding iCoding) {
		// check if this iCoding is already present
		for (ICoding presentCoding : coding) {
			if (isSameCoding(presentCoding, iCoding)) {
				return;
			}
		}
		coding.add(iCoding);
	}

	/**
	 * Checks if all units the same
	 *
	 * @param iObservations
	 * @return
	 */
	public static String getExactUnitOfComponent(List<ObservationComponent> observationComponents) {
		Set<String> units = new HashSet<>();
		for (ObservationComponent child : observationComponents) {
			Optional<String> valueUnit = child.getNumericValueUnit();
			if (valueUnit.isPresent()) {
				units.add(valueUnit.get());
			} else {
				return null;
			}
		}
		return units.size() == 1 ? units.iterator().next() : null;
	}

	/**
	 * Get all the children of the {@link IObservation} reachable via target
	 * {@link IObservationLink} links.
	 *
	 * @param iObservation
	 * @param list
	 * @param maxDepth
	 * @return
	 */
	public static List<IObservation> getObservationChildren(IObservation iObservation, List<IObservation> list,
			int maxDepth) {
		if (maxDepth > 0) {
			List<IObservation> refChildrens = iObservation.getTargetObseravtions(ObservationLinkType.REF);
			list.addAll(refChildrens);
			for (IObservation child : refChildrens) {
				getObservationChildren(child, list, --maxDepth);
			}
		}
		return list;
	}

	public static IObservation getRootObservationRecursive(IObservation observation) {
		IObservation rootObservation = observation;
		List<IObservation> sources = observation.getSourceObservations(ObservationLinkType.REF);
		if (sources != null && !sources.isEmpty()) {
			for (IObservation iObservation : sources) {
				rootObservation = getRootObservationRecursive(iObservation);
			}
		}
		return rootObservation;
	}

	/**
	 * Initialize the FHIR content of the {@link IFinding}.
	 *
	 * @param created
	 * @param type
	 */
	public static <T extends IFinding> void initFhir(T created, Class<T> type) {
		if (type.equals(IEncounter.class)) {
			org.hl7.fhir.r4.model.Encounter fhirEncounter = new org.hl7.fhir.r4.model.Encounter();
			fhirEncounter.setId(new IdType(fhirEncounter.getClass().getSimpleName(), created.getId()));
			fhirEncounter.setClass_(new Coding("2.16.840.1.113883.1.11.13955", "AMB", "ambulatory"));
			ModelUtil.saveResource(fhirEncounter, created);
		} else if (type.equals(IObservation.class)) {
			org.hl7.fhir.r4.model.Observation fhirObservation = new org.hl7.fhir.r4.model.Observation();
			fhirObservation.setId(new IdType(fhirObservation.getClass().getSimpleName(), created.getId()));
			fhirObservation.setStatus(ObservationStatus.FINAL);
			ModelUtil.saveResource(fhirObservation, created);
		} else if (type.equals(ICondition.class)) {
			org.hl7.fhir.r4.model.Condition fhirCondition = new org.hl7.fhir.r4.model.Condition();
			fhirCondition.setId(new IdType(fhirCondition.getClass().getSimpleName(), created.getId()));
			fhirCondition.setRecordedDate(new Date());
			ModelUtil.saveResource(fhirCondition, created);
		} else if (type.equals(IProcedureRequest.class)) {
			org.hl7.fhir.r4.model.ServiceRequest fhirProcedureRequest = new org.hl7.fhir.r4.model.ServiceRequest();
			fhirProcedureRequest.setId(new IdType(fhirProcedureRequest.getClass().getSimpleName(), created.getId()));
			ModelUtil.saveResource(fhirProcedureRequest, created);
		} else if (type.equals(IFamilyMemberHistory.class)) {
			org.hl7.fhir.r4.model.FamilyMemberHistory fhirFamilyMemberHistory = new org.hl7.fhir.r4.model.FamilyMemberHistory();
			fhirFamilyMemberHistory
					.setId(new IdType(fhirFamilyMemberHistory.getClass().getSimpleName(), created.getId()));
			ModelUtil.saveResource(fhirFamilyMemberHistory, created);
		} else if (type.equals(IAllergyIntolerance.class)) {
			org.hl7.fhir.r4.model.AllergyIntolerance fhirAllergyIntolerance = new org.hl7.fhir.r4.model.AllergyIntolerance();
			fhirAllergyIntolerance
					.setId(new IdType(fhirAllergyIntolerance.getClass().getSimpleName(), created.getId()));
			ModelUtil.saveResource(fhirAllergyIntolerance, created);
		} else if (type.equals(IClinicalImpression.class)) {
			org.hl7.fhir.r4.model.ClinicalImpression fhirClinicalImpression = new org.hl7.fhir.r4.model.ClinicalImpression();
			fhirClinicalImpression
					.setId(new IdType(fhirClinicalImpression.getClass().getSimpleName(), created.getId()));
			ModelUtil.saveResource(fhirClinicalImpression, created);
		} else if (type.equals(IDocumentReference.class)) {
			org.hl7.fhir.r4.model.DocumentReference fhirDocumentReference = new org.hl7.fhir.r4.model.DocumentReference();
			fhirDocumentReference.setId(new IdType(fhirDocumentReference.getClass().getSimpleName(), created.getId()));
			fhirDocumentReference.setStatus(DocumentReferenceStatus.CURRENT);
			ModelUtil.saveResource(fhirDocumentReference, created);
		} else {
			LoggerFactory.getLogger(ModelUtil.class).error("Could not initialize unknown type [" + type + "]");
		}
	}

	public static boolean fixFhirResource(IFinding finding) {
		boolean changed = false;
		Optional<IBaseResource> resource = loadResource(finding);
		if (resource.isPresent()) {
			if (resource.get() instanceof org.hl7.fhir.r4.model.Encounter) {
				org.hl7.fhir.r4.model.Encounter fhirEncounter = (Encounter) resource.get();
				if (fhirEncounter.getClass_() == null || fhirEncounter.getClass_().getSystem() == null) {
					fhirEncounter.setClass_(new Coding("2.16.840.1.113883.1.11.13955", "AMB", "ambulatory"));
					ModelUtil.saveResource(fhirEncounter, finding);
					changed = true;
				}
			}
		}
		return changed;
	}
}
