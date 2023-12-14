package ch.elexis.core.findings.util;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;

import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.services.IModelService;

public class CodeTypeUtil {

	public static Optional<String> getSystem(CodeType code) {
		String codeValue = code.getValue();
		if (codeValue != null) {
			String[] parts = codeValue.split("\\|");
			if (parts.length == 2) {
				return Optional.of(parts[0]);
			}
		}
		return Optional.empty();
	}

	public static Optional<String> getCode(CodeType code) {
		String codeValue = code.getValue();
		if (codeValue != null) {
			String[] parts = codeValue.split("\\|");
			if (parts.length == 2) {
				return Optional.of(parts[1]);
			} else if (parts.length == 1) {
				return Optional.of(parts[0]);
			}
		}
		return Optional.empty();
	}

	public static boolean isVitoLabkey(IModelService modelService, Observation observation, String codeString) {
		String labresultId = observation.getIdElement().getIdPart();
		Optional<ILabResult> result = modelService.load(labresultId, ILabResult.class);
		if (result.isPresent()) {
			ILabItem item = result.get().getItem();
			if (item != null) {
				String export = item.getExport();
				if (export != null && export.startsWith("vitolabkey:")) {
					String[] parts = export.split(":");
					if (parts.length == 2) {
						parts = parts[1].split(",");
						if (parts.length > 0) {
							for (String string : parts) {
								if (string.equals(codeString)) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	public static boolean isCodeInConcept(CodeableConcept concept, String system, String code) {
		List<Coding> codings = concept.getCoding();
		for (Coding coding : codings) {
			if (coding.getSystem() != null && coding.getCode() != null && coding.getSystem().equals(system)
					&& coding.getCode().equals(code)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isCodeInConceptList(List<CodeableConcept> conceptList, String system, String code) {
		for (CodeableConcept codeableConcept : conceptList) {
			if (isCodeInConcept(codeableConcept, system, code)) {
				return true;
			}
		}
		return false;
	}
}
