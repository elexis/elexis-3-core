package ch.elexis.core.spotlight.ui.controls;

import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.spotlight.ISpotlightResultEntry;

public abstract class AbstractSpotlightResultEntryDetailComposite extends Composite {
	
	public AbstractSpotlightResultEntryDetailComposite(Composite parent, int style){
		super(parent, style);
	}
	
	/**
	 * 
	 * @param resultEntry
	 *            set the entry to show, or <code>null</code> to clean the current values
	 */
	public abstract void setSpotlightEntry(@Nullable ISpotlightResultEntry resultEntry);
	
}
