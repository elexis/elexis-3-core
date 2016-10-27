package ch.elexis.core.findings.fhir.po.model.util;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;

import ch.elexis.core.findings.ICoding;

public class ModelUtil {
	
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
	
	private static class CodingWrapper implements ICoding {
		
		private Coding coding;
		
		public CodingWrapper(Coding coding){
			this.coding = coding;
		}
		
		@Override
		public String getSystem(){
			return coding.getSystem();
		}
		
		@Override
		public String getCode(){
			return coding.getCode();
		}
		
		@Override
		public String getDisplay(){
			return coding.getDisplay();
		}
	}
}
