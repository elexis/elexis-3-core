package ch.elexis.core.model;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;

public class FreeTextDiagnosis
		extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.FreeTextDiagnosis>
		implements IdentifiableWithXid, IFreeTextDiagnosis {
	
	public FreeTextDiagnosis(ch.elexis.core.jpa.entities.FreeTextDiagnosis entity){
		super(entity);
	}
	
	@Override
	public String getCode(){
		return getEntity().getId();
	}
	
	@Override
	public void setCode(String value){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getText(){
		return getEntity().getText();
	}
	
	@Override
	public void setText(String value){
		getEntityMarkDirty().setText(value);
	}
	
	@Override
	public String getDescription(){
		return getText();
	}
	
	@Override
	public void setDescription(String value){
	}
	
	@Override
	public String getCodeSystemName(){
		// TODO some kind of lookup?
		return "freetext";
	}
}
