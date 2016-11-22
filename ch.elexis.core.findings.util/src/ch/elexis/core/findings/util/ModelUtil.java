package ch.elexis.core.findings.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.util.model.CodingWrapper;

public class ModelUtil {
	
	private static FhirContext context = FhirContext.forDstu3();
	
	public static IParser getJsonParser(){
		return context.newJsonParser();
	}
	
	public static Optional<IBaseResource> loadResource(IFinding finding) throws DataFormatException{
		IBaseResource resource = null;
		if (finding.getRawContent() != null && !finding.getRawContent().isEmpty()) {
			resource = getJsonParser().parseResource(finding.getRawContent());
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
}
