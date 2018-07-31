package ch.elexis.core.findings.fhir.model;

import java.util.Optional;

import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservationLink;
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.model.IXid;

public class ObservationLink
		extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.ObservationLink>
		implements IObservationLink {
	
	public ObservationLink(ch.elexis.core.jpa.entities.ObservationLink entity){
		super(entity);
	}
	
	@Override
	public Optional<IObservation> getSource(){
		return ModelUtil.loadFinding(getEntity().getSourceid(), IObservation.class);
	}
	
	@Override
	public Optional<IObservation> getTarget(){
		return ModelUtil.loadFinding(getEntity().getTargetid(), IObservation.class);
	}
	
	@Override
	public void setTarget(IObservation observation){
		if (observation != null) {
			getEntity().setTargetid(observation.getId());
		} else {
			getEntity().setTargetid(null);
		}
	}
	
	@Override
	public void setSource(IObservation observation){
		if (observation != null) {
			getEntity().setSourceid(observation.getId());
		} else {
			getEntity().setSourceid(null);
		}
	}
	
	@Override
	public void setType(ObservationLinkType type){
		if (type != null) {
			getEntity().setType(type.name());
		} else {
			getEntity().setType(null);
		}
	}
	
	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists){
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public IXid getXid(String domain){
		// TODO Auto-generated method stub
		return null;
	}
}
