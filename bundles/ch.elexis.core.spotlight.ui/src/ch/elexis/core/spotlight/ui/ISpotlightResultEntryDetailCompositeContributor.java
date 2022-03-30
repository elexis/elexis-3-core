package ch.elexis.core.spotlight.ui;

import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.spotlight.ISpotlightResultEntry.Category;

public interface ISpotlightResultEntryDetailCompositeContributor {

	public ISpotlightResultEntryDetailComposite createDetailComposite(Composite parent, int style);

	/**
	 * The {@link Category} this contributor is meant to generate instances for
	 */
	public Category appliedForCategory();
}
