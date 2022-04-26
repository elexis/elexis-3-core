package ch.elexis.core.types;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DocumentStatusMapper {

	public static Set<DocumentStatus> map(int status) {
		Set<DocumentStatus> stati = new HashSet<>();
		if (status == DocumentStatus.NEW_VALUE) {
			stati.add(DocumentStatus.NEW);
			return stati;
		}

		DocumentStatus[] values = DocumentStatus.values();
		for (DocumentStatus documentStatus : values) {
			if (documentStatus.getValue() == DocumentStatus.NEW_VALUE) {
				continue;
			}
			int result = status & documentStatus.getValue();
			if (result == documentStatus.getValue()) {
				stati.add(documentStatus);
			}
		}

		return stati;
	}

	public static int map(List<DocumentStatus> stati) {
		return map(new HashSet<>(stati));
	}

	public static int map(Set<DocumentStatus> _statusSet) {
		int value = 0;
		for (DocumentStatus documentStatus : _statusSet) {
			value ^= documentStatus.getValue();
		}
		return value;
	}

}
