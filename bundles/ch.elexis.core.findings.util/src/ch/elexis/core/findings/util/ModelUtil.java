package ch.elexis.core.findings.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.IClinicalImpression;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
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
import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.INamedQuery;

@Component
public class ModelUtil {
	
	private static IModelService findingsModelService;
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.findings.model)")
	public void setFindingsModelService(IModelService modelService){
		ModelUtil.findingsModelService = modelService;
	}
	
	public static <T> Optional<T> loadFinding(String id, Class<T> clazz){
		if (id != null) {
			return findingsModelService.load(id, clazz);
		}
		return Optional.empty();
	}
	
	public static <T> INamedQuery<T> getFindingsNamedQuery(Class<T> clazz, String...properties) {
		return findingsModelService.getNamedQuery(clazz, properties);
	}
	
	public static <T> T createFinding(Class<T> clazz){
		return findingsModelService.create(clazz);
	}
	
	public static boolean saveFinding(Identifiable identifiable){
		return findingsModelService.save(identifiable);
	}
	
	public static void deleteFinding(Deleteable deleteable){
		findingsModelService.delete(deleteable);
	}
	
	private static FhirContext context = FhirContext.forDstu3();
	
	private static IParser getJsonParser() {
		return context.newJsonParser();
	}
	
	public static IBaseResource getAsResource(String jsonResource) {
		return getJsonParser().parseResource(jsonResource);
	}

	public static Optional<IBaseResource> loadResource(IFinding finding) throws DataFormatException{
		IBaseResource resource = null;
		String rawContent = finding.getRawContent();
		if (rawContent != null && !rawContent.isEmpty()) {
			// always convert to newest json format
			if (!FindingsFormatUtil.isCurrentFindingsFormat(rawContent)) {
				Optional<String> convertedContent =
					FindingsFormatUtil.convertToCurrentFindingsFormat(rawContent);
				if (convertedContent.isPresent()) {
					rawContent = convertedContent.get();
				}
			}
			resource = getAsResource(rawContent);
		}
		return Optional.ofNullable(resource);
	}
	
	public static void saveResource(IBaseResource resource, IFinding finding)
		throws DataFormatException{
		if (resource != null) {
			String resourceJson = getJsonParser().encodeResourceToString(resource);
			finding.setRawContent(resourceJson);
		}
	}
	
	public static void setCodingsToConcept(CodeableConcept codeableConcept, List<ICoding> coding){
		codeableConcept.getCoding().clear();
		for (ICoding iCoding : coding) {
			codeableConcept.addCoding(
				new Coding(iCoding.getSystem(), iCoding.getCode(), iCoding.getDisplay()));
		}
	}
	
	public static List<ICoding> getCodingsFromConcept(CodeableConcept codeableConcept){
		ArrayList<ICoding> ret = new ArrayList<>();
		List<Coding> coding = codeableConcept.getCoding();
		for (Coding code : coding) {
			ret.add(new CodingWrapper(code));
		}
		return ret;
	}
	
	public static boolean isCodeInList(String system, String code, List<ICoding> list){
		if (list != null && !list.isEmpty()) {
			for (ICoding iCoding : list) {
				if (iCoding.getSystem().equals(system) && iCoding.getCode().equals(code)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isSystemInList(String system, List<ICoding> list){
		if (list != null && !list.isEmpty()) {
			for (ICoding iCoding : list) {
				if (iCoding.getSystem().equals(system)) {
					return true;
				}
			}
		}
		return false;
	}

	public static Optional<ICoding> getCodeBySystem(List<ICoding> coding,
		CodingSystem codingSystem){
		for (ICoding iCoding : coding) {
			if (codingSystem.getSystem().equals(iCoding.getSystem())) {
				return Optional.of(iCoding);
			}
		}
		return Optional.empty();
	}
	
	public static Optional<String> getNarrativeAsString(Narrative narrative) {
		String text = narrative.getDivAsString();
		if (text != null) {
			String divDecodedText = text.replaceAll(
				"<div>|<div xmlns=\"http://www.w3.org/1999/xhtml\">|</div>|</ div>", "");
			divDecodedText = divDecodedText.replaceAll("<br/>|<br />", "\n")
				.replaceAll("&amp;", "&").replaceAll("&gt;", ">").replaceAll("&lt;", "<")
				.replaceAll("'&sect;'", "§");
			return Optional.of(divDecodedText);
		}
		return Optional.empty();
	}
	
	public static void setNarrativeFromString(Narrative narrative, String text){
		text = fixXhtmlContent(text);
		String divEncodedText =
			text.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("§", "'&sect;'")
				.replaceAll("&", "&amp;").replaceAll("(\r\n|\r|\n)", "<br />");
		narrative.setDivAsString(divEncodedText);
	}
	
	/**
	 * Remove characters which cause problems in xhtml.
	 * 
	 * @param content
	 * @return content without problem characters
	 */
	private static String fixXhtmlContent(String content){
		// replace unicode nbsp with space character
		content = content.replace((char) 0xa0, ' ');
		return content;
	}
	
	public static boolean isSameCoding(ICoding left, ICoding right){
		return left.getSystem().equals(right.getSystem()) && left.getCode().equals(right.getCode());
	}
	
	public static void addCodingIfNotPresent(List<ICoding> coding, ICoding iCoding){
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
	public static String getExactUnitOfComponent(List<ObservationComponent> observationComponents){
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
	public static List<IObservation> getObservationChildren(IObservation iObservation,
		List<IObservation> list, int maxDepth){
		if (maxDepth > 0) {
			List<IObservation> refChildrens =
				iObservation.getTargetObseravtions(ObservationLinkType.REF);
			list.addAll(refChildrens);
			for (IObservation child : refChildrens) {
				getObservationChildren(child, list, --maxDepth);
			}
		}
		return list;
	}
	
	public static IObservation getRootObservationRecursive(IObservation observation){
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
	public static <T extends IFinding> void initFhir(T created, Class<T> type){
		if (type.equals(IEncounter.class)) {
			org.hl7.fhir.dstu3.model.Encounter fhirEncounter =
				new org.hl7.fhir.dstu3.model.Encounter();
			fhirEncounter
				.setId(new IdType(fhirEncounter.getClass().getSimpleName(), created.getId()));
			ModelUtil.saveResource(fhirEncounter, created);
		} else if (type.equals(IObservation.class)) {
			org.hl7.fhir.dstu3.model.Observation fhirObservation =
				new org.hl7.fhir.dstu3.model.Observation();
			fhirObservation
				.setId(new IdType(fhirObservation.getClass().getSimpleName(), created.getId()));
			ModelUtil.saveResource(fhirObservation, created);
		} else if (type.equals(ICondition.class)) {
			org.hl7.fhir.dstu3.model.Condition fhirCondition =
				new org.hl7.fhir.dstu3.model.Condition();
			fhirCondition
				.setId(new IdType(fhirCondition.getClass().getSimpleName(), created.getId()));
			fhirCondition.setAssertedDate(new Date());
			ModelUtil.saveResource(fhirCondition, created);
		} else if (type.equals(IProcedureRequest.class)) {
			org.hl7.fhir.dstu3.model.ProcedureRequest fhirProcedureRequest =
				new org.hl7.fhir.dstu3.model.ProcedureRequest();
			fhirProcedureRequest.setId(
				new IdType(fhirProcedureRequest.getClass().getSimpleName(), created.getId()));
			ModelUtil.saveResource(fhirProcedureRequest, created);
		} else if (type.equals(IFamilyMemberHistory.class)) {
			org.hl7.fhir.dstu3.model.FamilyMemberHistory fhirFamilyMemberHistory =
				new org.hl7.fhir.dstu3.model.FamilyMemberHistory();
			fhirFamilyMemberHistory.setId(
				new IdType(fhirFamilyMemberHistory.getClass().getSimpleName(), created.getId()));
			ModelUtil.saveResource(fhirFamilyMemberHistory, created);
		} else if (type.equals(IAllergyIntolerance.class)) {
			org.hl7.fhir.dstu3.model.AllergyIntolerance fhirAllergyIntolerance =
				new org.hl7.fhir.dstu3.model.AllergyIntolerance();
			fhirAllergyIntolerance.setId(
				new IdType(fhirAllergyIntolerance.getClass().getSimpleName(), created.getId()));
			ModelUtil.saveResource(fhirAllergyIntolerance, created);
		} else if (type.equals(IClinicalImpression.class)) {
			org.hl7.fhir.dstu3.model.ClinicalImpression fhirClinicalImpression =
				new org.hl7.fhir.dstu3.model.ClinicalImpression();
			fhirClinicalImpression.setId(
				new IdType(fhirClinicalImpression.getClass().getSimpleName(), created.getId()));
			ModelUtil.saveResource(fhirClinicalImpression, created);
		} else {
			LoggerFactory.getLogger(ModelUtil.class)
				.error("Could not initialize unknown type [" + type + "]");
		}
	}
}
