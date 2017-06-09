package ch.elexis.core.findings.util.internal;

import java.util.Optional;

public class FindingsFormatUtil {
	
	private static FindingsFormat24 currentFormat = new FindingsFormat24();
	
	private static FindingsFormat[] oldFormats = {
		new FindingsFormat20()
	};
	
	public static FindingsFormat getCurrentFormat(){
		return currentFormat;
	}
	
	public static boolean isCurrentFindingsFormat(String rawContent){
		return currentFormat.isFindingsFormat(rawContent);
	}
	
	public static Optional<String> convertToCurrentFindingsFormat(String rawContent){
		for (FindingsFormat findingsFormat : oldFormats) {
			if (findingsFormat.isFindingsFormat(rawContent)) {
				return findingsFormat.convertToCurrentFormat(rawContent);
			}
		}
		return Optional.empty();
	}
}
