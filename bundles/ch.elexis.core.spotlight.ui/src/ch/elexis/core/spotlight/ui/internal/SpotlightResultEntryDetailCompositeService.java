package ch.elexis.core.spotlight.ui.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import ch.elexis.core.spotlight.ISpotlightResultEntry.Category;
import ch.elexis.core.spotlight.ui.ISpotlightResultEntryDetailComposite;
import ch.elexis.core.spotlight.ui.ISpotlightResultEntryDetailCompositeContributor;

@Component(immediate = true)
public class SpotlightResultEntryDetailCompositeService
		implements ISpotlightResultEntryDetailCompositeService {
	
	private Map<Category, ISpotlightResultEntryDetailCompositeContributor> instantiationMapper =
		new HashMap<>();
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, bind = "bind", unbind = "unbind")
	private volatile List<ISpotlightResultEntryDetailCompositeContributor> spotlightResultEntryDetailDialogCompositContributors;
	
	protected void bind(ISpotlightResultEntryDetailCompositeContributor contributor){
		instantiationMapper.put(contributor.appliedForCategory(), contributor);
	}
	
	protected void unbind(ISpotlightResultEntryDetailCompositeContributor contributor){
		instantiationMapper.remove(contributor.appliedForCategory());
	}
	
	@Override
	public ISpotlightResultEntryDetailComposite instantiate(Category category, Composite parent,
		int style){
		
		ISpotlightResultEntryDetailCompositeContributor contributor =
			instantiationMapper.get(category);
		
		if(contributor != null) {
			return contributor.createDetailComposite(parent, style);
		}
		
		// TODO fallback
		return new FallbackDetailComposite(parent, style);
	}

}
