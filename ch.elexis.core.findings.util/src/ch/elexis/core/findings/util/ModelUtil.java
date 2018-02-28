package ch.elexis.core.findings.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservationLink;
import ch.elexis.core.findings.IObservationLink.ObservationLinkType;
import ch.elexis.core.findings.ObservationComponent;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.util.model.CodingWrapper;

public class ModelUtil {
	
	private static FhirContext context = FhirContext.forDstu3();
	
	private static IParser getJsonParser() {
		return context.newJsonParser();
	}
	
	public static IBaseResource getAsResource(String jsonResource) {
		return getJsonParser().parseResource(jsonResource);
	}

	public static IGenericClient getGenericClient(String theServerBase) {
		// Create a logging interceptor
		LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
		loggingInterceptor.setLogRequestSummary(true);
		loggingInterceptor.setLogRequestBody(true);

		IGenericClient client = context.newRestfulGenericClient(theServerBase);
		client.registerInterceptor(loggingInterceptor);
		return client;
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
	 * Get all the chilren of the {@link IObservation} reachable via target {@link IObservationLink}
	 * links.
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
}
