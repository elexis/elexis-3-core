package ch.elexis.core.eigendiagnosen.model;

import java.util.List;
import java.util.Map;

import ch.elexis.core.eigendiagnosen.Messages;
import ch.elexis.core.eigendiagnosen.model.service.ModelUtil;
import ch.elexis.core.jpa.entities.Eigendiagnose;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.ExtInfoHandler;
import ch.elexis.core.model.IDiagnosisTree;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.WithExtInfo;
import ch.elexis.core.services.holder.XidServiceHolder;

public class CustomDiagnosis extends
		AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Eigendiagnose>
		implements IDiagnosisTree, WithExtInfo, Identifiable {
	
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
	public void setCode(String value){
		getEntity().setCode(value);
	}
	
	@Override
	public void setText(String value){
		getEntity().setTitle(value);
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
	
	@Override
	public Map<Object, Object> getMap(){
		return extInfoHandler.getMap();
	}
	
	@Override
	public String getDescription(){
		return getEntity().getComment();
	}
	
	@Override
	public void setDescription(String value){
		getEntity().setComment(value);
	}
	
	@Override
	public IDiagnosisTree getParent(){
		String parentCode = getEntity().getParent();
		if (parentCode != null && "NIL".equals(parentCode)) {
			return ModelUtil.loadDiagnosisWithCode(parentCode).orElse(null);
		}
		return null;
	}
	
	@Override
	public void setParent(IDiagnosisTree value){
		if (value != null) {
			getEntity().setParent(value.getCode());
		} else {
			getEntity().setParent(null);
		}
	}
	
	@Override
	public List<IDiagnosisTree> getChildren(){
		return ModelUtil.loadDiagnosisWithParent(getCode());
	}

	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists){
		return XidServiceHolder.get().addXid(this, domain, id, updateIfExists);
	}

	@Override
	public IXid getXid(String domain){
		return XidServiceHolder.get().getXid(this, domain);
	}
}
