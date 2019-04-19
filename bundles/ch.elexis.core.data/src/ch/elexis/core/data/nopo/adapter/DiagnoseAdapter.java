package ch.elexis.core.data.nopo.adapter;

import ch.elexis.core.model.IDiagnosis;

public class DiagnoseAdapter implements ch.elexis.core.data.interfaces.IDiagnose {
	
	private IDiagnosis adaptee;
	
	public DiagnoseAdapter(IDiagnosis adaptee){
		this.adaptee = adaptee;
	}
	
	@Override
	public String getCodeSystemName(){
		return adaptee.getCodeSystemName();
	}
	
	@Override
	public String getId(){
		return adaptee.getId();
	}
	
	@Override
	public String getCode(){
		return adaptee.getCode();
	}
	
	@Override
	public String getText(){
		return adaptee.getText();
	}
	
	@Override
	public String getLabel(){
		return adaptee.getLabel();
	}
}
