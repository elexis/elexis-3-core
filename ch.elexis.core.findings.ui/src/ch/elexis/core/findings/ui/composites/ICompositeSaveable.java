package ch.elexis.core.findings.ui.composites;

import java.time.LocalDateTime;
import java.util.List;

import org.eclipse.jface.action.Action;

import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IObservation.ObservationType;
import ch.elexis.core.findings.ObservationComponent;

public interface ICompositeSaveable {
	public IFinding saveContents(LocalDateTime localDateTime);
	
	public List<ICompositeSaveable> getChildReferences();
	
	public List<ICompositeSaveable> getChildComponents();
	
	public void hideLabel(boolean all);
	
	public String getFieldTextValue();
	
	public String getTitle();
	
	public void setToolbarActions(List<Action> toolbarActions);
	
	public List<Action> getToolbarActions();
	
	public IFinding getFinding();
	
	public ObservationComponent getObservationComponent();
	
	public ObservationType getObservationType();
}