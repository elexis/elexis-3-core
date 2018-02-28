package ch.elexis.core.findings.util;

import java.util.Optional;

import ch.elexis.core.findings.util.internal.FindingsFormat;
import ch.elexis.core.findings.util.internal.FindingsFormat20;
import ch.elexis.core.findings.util.internal.FindingsFormat24;

public class FindingsFormatUtil {

	public static final String CFG_HAPI_FHIR_VERSION =
		"es.findings.fhir.jpa.service/hapifhirversion";
	public static final String HAPI_FHIR_CURRENT_VERSION = "24";
	
	private static FindingsFormat24 currentFormat = new FindingsFormat24();

	private static FindingsFormat[] oldFormats = { new FindingsFormat20() };

	public static FindingsFormat getCurrentFormat() {
		return currentFormat;
	}

	public static boolean isCurrentFindingsFormat(String rawContent) {
		int currentFormatMatches = currentFormat.isFindingsFormat(rawContent);
		int highestFormatMatches = 0;
		for (FindingsFormat findingsFormat : oldFormats) {
			int matches = findingsFormat.isFindingsFormat(rawContent);
			if (matches > highestFormatMatches) {
				highestFormatMatches = matches;
			}
		}
		return currentFormatMatches >= highestFormatMatches;
	}

	public static Optional<String> convertToCurrentFindingsFormat(String rawContent) {
		FindingsFormat highestFormat = null;
		int highestFormatMatches = 0;
		for (FindingsFormat findingsFormat : oldFormats) {
			int matches = findingsFormat.isFindingsFormat(rawContent);
			if (matches >= highestFormatMatches) {
				highestFormat = findingsFormat;
				highestFormatMatches = matches;
			}
		}
		if (highestFormat != null) {
			return highestFormat.convertToCurrentFormat(rawContent);
		} else {
			return Optional.empty();
		}
	}
}
