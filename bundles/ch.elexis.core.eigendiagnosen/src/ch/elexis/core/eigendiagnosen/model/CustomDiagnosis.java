package ch.elexis.core.eigendiagnosen.model;

import ch.elexis.core.eigendiagnosen.Messages;
import ch.elexis.core.jpa.entities.Eigendiagnose;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.ExtInfoHandler;
import ch.elexis.core.jpa.model.adapter.mixin.IdentifiableWithXid;
import ch.elexis.core.model.IDiagnosis;
import ch.elexis.core.model.WithExtInfo;

public class CustomDiagnosis extends
		AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Eigendiagnose>
		implements IDiagnosis, WithExtInfo, IdentifiableWithXid {
	
	private ExtInfoHandler extInfoHandler;
	
	public CustomDiagnosis(Eigendiagnose entity){
		super(entity);
		extInfoHandler = new ExtInfoHandler(this);
	}
	
	@Override
	public String getCodeSystemName(){
		return Messages.Eigendiagnosen_CodeSystemName;
	}
	
	@Override
	public String getCodeSystemCode(){
		return "ED";
	}
	
	@Override
	public String getCode(){
		return getEntity().getCode();
	}
	
	@Override
	public String getText(){
		return getEntity().getTitle();
	}
	
	@Override
	public String getLabel(){
		return getCode() + " - " + getText();
	}
	
	@Override
	public Object getExtInfo(Object key){
		return extInfoHandler.getExtInfo(key);
	}
	
	@Override
	public void setExtInfo(Object key, Object value){
		extInfoHandler.setExtInfo(key, value);
	}
}
