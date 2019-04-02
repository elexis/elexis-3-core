package ch.elexis.core.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.IdentifiableWithXid;

public class DiagnosisReference
		extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Diagnosis>
		implements IdentifiableWithXid, IDiagnosisReference {
	
	private static Map<String, String> codeSystemClassMap = new HashMap<>();
	
	public DiagnosisReference(ch.elexis.core.jpa.entities.Diagnosis entity){
		super(entity);
		codeSystemClassMap.put("ch.elexis.data.TICode", "TI-Code");
	}
	
	@Override
	public String getCode(){
		return getEntity().getCode();
	}
	
	@Override
	public void setCode(String value){
		getEntity().setCode(value);
	}
	
	@Override
	public String getText(){
		return getEntity().getText();
	}
	
	@Override
	public void setText(String value){
		getEntity().setText(value);
	}
	
	@Override
	public String getDescription(){
		return "";
	}
	
	@Override
	public void setDescription(String value){
	}
	
	@Override
	public String getReferredClass(){
		return getEntity().getDiagnosisClass();
	}
	
	@Override
	public void setReferredClass(String value){
		getEntity().setDiagnosisClass(value);
	}
	
	@Override
	public String getCodeSystemName(){
		String referredClass = getReferredClass();
		if (StringUtils.isNoneBlank(referredClass)) {
			String codeSystem = codeSystemClassMap.get(referredClass);
			if (StringUtils.isNoneBlank(codeSystem)) {
				return codeSystem;
			}
		}
		return null;
	}
}
