package ch.elexis.core.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class DocumentStatusMapper {
	
	public static List<DocumentStatus> map(int status){
		List<DocumentStatus> stati = new ArrayList<>();
		
		if (status == DocumentStatus.NEW_VALUE) {
			return Collections.singletonList(DocumentStatus.NEW);
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
	
	public static int map(List<DocumentStatus> stati){
		return map(new HashSet<>(stati));
	}
	
	public static int map(HashSet<DocumentStatus> _statusSet){
		int value = 0;
		for (DocumentStatus documentStatus : _statusSet) {
			value ^= documentStatus.getValue();
		}
		return value;
	}
	
}
