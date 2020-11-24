package ch.elexis.core.spotlight.ui.internal;

import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.spotlight.ISpotlightResultEntry.Category;
import ch.elexis.core.spotlight.ui.ISpotlightResultEntryDetailComposite;

public interface ISpotlightResultEntryDetailCompositeService {
	
	ISpotlightResultEntryDetailComposite instantiate(Category category, Composite parent, int none);
	
}
