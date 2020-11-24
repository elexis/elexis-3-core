package ch.elexis.core.spotlight.ui.controls.detail;

import org.eclipse.swt.widgets.Composite;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.spotlight.ISpotlightResultEntry.Category;
import ch.elexis.core.spotlight.ui.ISpotlightResultEntryDetailComposite;
import ch.elexis.core.spotlight.ui.ISpotlightResultEntryDetailCompositeContributor;

@Component(immediate = true)
public class PatientSpotlightResultEntryDetailCompositeContributor
		implements ISpotlightResultEntryDetailCompositeContributor {
	
	@Override
	public ISpotlightResultEntryDetailComposite createDetailComposite(Composite parent, int style){
		return new PatientDetailComposite(parent, style);
	}
	
	@Override
	public Category appliedForCategory(){
		return Category.PATIENT;
	}
	
}
