package ch.elexis.core.spotlight.ui;

import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.spotlight.ISpotlightResultEntry;
import ch.elexis.core.spotlight.ISpotlightResultEntry.Category;

/**
 * Required to be {@link Composite}
 */
public interface ISpotlightResultEntryDetailComposite {
	
	public abstract boolean setFocus();
	
	public abstract void dispose();
	
	public abstract void setLayoutData(Object object);
	
	/**
	 * 
	 * @param resultEntry
	 *            set the entry to show, or <code>null</code> to clean the current values
	 */
	public abstract void setSpotlightEntry(@Nullable ISpotlightResultEntry resultEntry);
	
	/**
	 * The {@link Category} this Composite is meant to be applied on
	 */
	public abstract Category appliedForCategory();

	public abstract boolean handleAltKeyPressed(int keyCode);
	
}