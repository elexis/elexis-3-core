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
	
	public String getLocalizedTitle();
	
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
