package ch.elexis.core.fhir.mapper.r4.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

import ch.elexis.core.fhir.CodeSystem;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.codes.MedicamentCoding;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.ICodeElementServiceContribution;

public class CodeSystemUtil {

	private static Map<String, String> systemIdMap = new HashMap<String, String>() {
		{
			put(CodingSystem.ELEXIS_COVERAGE_TYPE.getSystem(), "coveragetype");
			put(CodingSystem.ELEXIS_DIAGNOSE_TESSINERCODE.getSystem(), "tessinercode");
		}
	};

	private static boolean isSystemString(String string) {
		return string.startsWith("http://") || string.startsWith("www.elexis.info/");
	}

	public static Optional<String> getIdForString(String string) {
		if (isSystemString(string)) {
			return Optional.ofNullable(systemIdMap.get(string));
		}
		return Optional.of(string);
	}

	public static Optional<String> getSystemForId(String idString) {
		Set<String> keys = systemIdMap.keySet();
		for (String key : keys) {
			if (systemIdMap.get(key).equals(idString)) {
				return Optional.of(key);
			}
		}
		return Optional.empty();
	}

	public static Coding getGtinCoding(String gtin) {
		if (StringUtils.isNumeric(gtin)) {
			return new Coding(MedicamentCoding.GTIN.getUrl(), gtin, null);
		}
		return null;
	}

	// public static Coding getElexisCodeSystemCoding(String codeSystemCode, String
	// codeSystemName,
	// String codeSystemCodeValue, String displayName){
	// if (StringUtils.isNotBlank(codeSystemName) &&
	// StringUtils.isNotBlank(codeSystemCodeValue)) {
	// Coding coding = new Coding("www.elexis.info/codesystem", codeSystemName,
	// displayName);
	// coding.setId(codeSystemCodeValue);
	// return coding;
	// }
	// return null;
	// }

	// public static Optional<Coding> findCodeElementEntry(CodeableConcept
	// codeableConcept,
	// String system){
	// return codeableConcept.getCoding().stream()
	// .filter(c -> StringUtils.startsWith(system, c.getSystem())).findFirst();
	// }

	/**
	 * Return a Coding that is resolvable via the {@link ICodeElementService}
	 *
	 * @param codeElementService
	 * @param codeElement
	 * @return
	 */
	public static Coding getCodeElementCoding(ICodeElementService codeElementService, ICodeElement codeElement) {

		Optional<ICodeElementServiceContribution> contribution = codeElementService.getContribution(null,
				codeElement.getCodeSystemName());
		String typName = contribution.get().getTyp().name().toLowerCase();

		String theSystem = CodeSystem.CODEELEMENT.getUrl() + "/" + typName + "/"
				+ codeElement.getCodeSystemName().toLowerCase();
		Coding coding = new Coding(theSystem, StringUtils.EMPTY, codeElement.getText());
		coding.setCode(codeElement.getCode());
		return coding;
	}

	/**
	 * Loads an {@link ICodeElement} if the resp. entry is available within the
	 * {@link CodeableConcept}
	 *
	 * @param codeElementService required to load the entry
	 * @param codeableConcept
	 * @return
	 */
	public static Optional<ICodeElement> loadCodeElementEntryInCodeableConcept(ICodeElementService codeElementService,
			CodeableConcept codeableConcept) {

		List<Coding> codings = codeableConcept.getCoding();
		for (Coding coding : codings) {
			if (StringUtils.startsWith(coding.getSystem(), CodeSystem.CODEELEMENT.getUrl())) {
				String codeElementTypAndcodeSystemName = StringUtils.substring(coding.getSystem(),
						CodeSystem.CODEELEMENT.getUrl().length() + 1);
				String[] codes = codeElementTypAndcodeSystemName.split("/");
				return codeElementService.loadFromString(codes[1], coding.getCode(), null);

			}
		}

		return Optional.empty();
	}

}
