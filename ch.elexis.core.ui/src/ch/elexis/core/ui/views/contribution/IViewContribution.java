package ch.elexis.core.ui.views.contribution;

import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.ui.locks.IUnlockable;

/**
 * Contribute a composite to a specific view and position in the view in Elexis
 * 
 * @since 3.2
 */
public interface IViewContribution extends IUnlockable {
	
	/**
	 * @return The id of the position in the respective view to contribute to.
	 */
	public default int getContributionPosition(){
		return 0;
	}
	
	/**
	 * 
	 * @return the positioning order on the respective contribution position. Allows to rank the
	 *         inclusion of contributions according to the number. Defaults to 0. May not be used.
	 */
	public default int getContributionPositionOrder(){
		return 0;
	}
	
	/**
	 * Test if the contribution is available and can be included.
	 * 
	 * @return
	 */
	public default boolean isAvailable(){
		return true;
	}
	
	/**
	 * Get the localized title of the contribution.
	 * 
	 * @return
	 */
	public String getLocalizedTitle();
	
	/**
	 * Create the contributed {@link Composite} implementation on the parent. Always test
	 * {@link IViewContribution#isAvailable()} before initializing the composite. Tip: use
	 * {@link ViewContributionHelper#getFilteredAndPositionSortedContributions(java.util.List, int)}
	 * to filter the list of contributions before calling this method.
	 * 
	 * @param parent
	 * @return
	 */
	public Composite initComposite(Composite parent);
	
	/**
	 * Handle the selection of the detail object in the respective parent view.
	 * 
	 * @param detailObject
	 *            the detail object selected
	 * @param additionalData
	 *            optional additional data
	 */
	public void setDetailObject(Object detailObject, Object additionalData);
	
}
