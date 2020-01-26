package ch.elexis.core.coding.internal.model;

import java.util.List;

import ch.elexis.core.coding.internal.JsonValueSet;
import ch.elexis.core.findings.ICoding;

public class Concept implements ICoding {
	
	public String code;
	public String codeSystem;
	public List<Designation> designation;
	
	@Override
	public String getSystem(){
		return codeSystem;
	}
	
	@Override
	public String getCode(){
		return code;
	}
	
	@Override
	public String getDisplay(){
		return designation != null
				? designation.stream().filter(i -> JsonValueSet.LANGUAGE_DEFAULT.equals(i.language))
					.findFirst()
					.map(i -> i.displayName).orElse(null)
				: null;
	}
	
	@Override
	public String toString(){
		return "Concept [code=" + code + ", codeSystem=" + codeSystem + ", designation="
			+ designation + "]";
	}
	
}
