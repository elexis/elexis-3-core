package ch.elexis.core.findings.util.fhir.transformer.helper;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Coding;

import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.util.fhir.MedicamentCoding;

public class CodeSystemUtil  {
	
	@SuppressWarnings("serial")
	private static HashMap<String, String> systemIdMap = new HashMap<String, String>() {
		{
			put(CodingSystem.ELEXIS_COVERAGE_TYPE.getSystem(), "coveragetype");
			put(CodingSystem.ELEXIS_DIAGNOSE_TESSINERCODE.getSystem(), "tessinercode");
		}
	};
	
	private static boolean isSystemString(String string){
		return string.startsWith("http://") || string.startsWith("www.elexis.info/");
	}
	
	public static Optional<String> getIdForString(String string){
		if (isSystemString(string)) {
			return Optional.ofNullable(systemIdMap.get(string));
		}
		return Optional.of(string);
	}
	
	public static Optional<String> getSystemForId(String idString){
		Set<String> keys = systemIdMap.keySet();
		for (String key : keys) {
			if (systemIdMap.get(key).equals(idString)) {
				return Optional.of(key);
			}
		}
		return Optional.empty();
	}
	
	public static Coding getGtinCoding(String gtin){
		if (StringUtils.isNumeric(gtin)) {
			return new Coding(MedicamentCoding.GTIN.getUrl(), gtin, null);
		}
		return null;
	}

	public static Coding getElexisCodeSystemCoding(String codeSystemCode, String codeSystemName,
		String codeSystemCodeValue, String displayName){
		if(StringUtils.isNotBlank(codeSystemName) && StringUtils.isNotBlank(codeSystemCodeValue)) {
			Coding coding = new Coding("www.elexis.info/codesystem", codeSystemName, displayName);
			coding.setId(codeSystemCodeValue);
			return coding;
		}
		return null;
	}
	
}
