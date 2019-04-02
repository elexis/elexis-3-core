package ch.elexis.core.eigendiagnosen.model.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.services.ICodeElementService.CodeElementTyp;
import ch.elexis.core.services.ICodeElementServiceContribution;

@Component
public class CodeElementContribution implements ICodeElementServiceContribution {
	
	@Override
	public String getSystem(){
		return "ED";
	}
	
	@Override
	public CodeElementTyp getTyp(){
		return CodeElementTyp.DIAGNOSE;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Optional<ICodeElement> loadFromCode(String code, Map<Object, Object> context){
		return (Optional<ICodeElement>) (Optional<?>) ModelUtil.loadDiagnosisWithCode(code);
	}
	
	@Override
	public List<ICodeElement> getElements(Map<Object, Object> context){
		// TODO Auto-generated method stub
		return null;
	}
	
}
