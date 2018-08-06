package ch.elexis.core.findings.fhir.model;

import java.util.Optional;

import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservationLink;
import ch.elexis.core.findings.fhir.model.service.FindingsModelService;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.IXid;

public class ObservationLink
		extends AbstractIdModelAdapter<ch.elexis.core.jpa.entities.ObservationLink>
		implements IObservationLink {
	
	public ObservationLink(ch.elexis.core.jpa.entities.ObservationLink entity){
		super(entity);
	}
	
	@Override
	public Optional<IObservation> getSource(){
		return Optional.ofNullable(
			FindingsModelService.getAdapter(getEntity().getSource(), IObservation.class));
	}
	
	@Override
	public Optional<IObservation> getTarget(){
		return Optional.ofNullable(
			FindingsModelService.getAdapter(getEntity().getTarget(), IObservation.class));
	}
	
	@Override
	public void setTarget(IObservation observation){
		if (observation != null) {
			getEntity().setTarget(FindingsModelService.getDBObject(observation,
				ch.elexis.core.jpa.entities.Observation.class));
		} else {
			getEntity().setTarget(null);
		}
	}
	
	@Override
	public void setSource(IObservation observation){
		if (observation != null) {
			getEntity().setSource(FindingsModelService.getDBObject(observation,
				ch.elexis.core.jpa.entities.Observation.class));
		} else {
			getEntity().setSource(null);
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
