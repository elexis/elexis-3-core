package ch.elexis.core.ui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.views.codesystems.ContributionAction;

public class CoreCommandUiUtil {

	private static Logger logger = LoggerFactory.getLogger(CoreUiUtil.class);


	/**
	 * This method queries the <i>org.eclipse.ui.menus</i> extensions, and looks for
	 * menu contributions with a locationURI <i>popup:classname</i>. Found
	 * contributions are added to the {@link IMenuManager}.
	 *
	 * @param manager
	 * @param objects
	 */
	public static void addCommandContributions(IMenuManager manager, Object[] selection, String location) {
		java.util.List<IConfigurationElement> contributions = Extensions.getExtensions("org.eclipse.ui.menus"); //$NON-NLS-1$
		List<ContributionAction> contributionActions = new ArrayList<>();
		for (IConfigurationElement contributionElement : contributions) {
			String locationUri = contributionElement.getAttribute("locationURI"); //$NON-NLS-1$
			if (location.equals(locationUri)) {
				IConfigurationElement[] commands = contributionElement.getChildren("command"); //$NON-NLS-1$
				if (commands.length > 0) {
					for (IConfigurationElement iConfigurationElement : commands) {
						getMenuContribution(iConfigurationElement, selection)
								.ifPresent(a -> contributionActions.add(a));
					}
				}
			}
		}
		if (!contributionActions.isEmpty()) {
			manager.add(new Separator());
			contributionActions.forEach(a -> manager.add(a));
		}
	}

	private static Optional<ContributionAction> getMenuContribution(IConfigurationElement commandElement,
			Object[] selection) {
		ContributionAction action = new ContributionAction(commandElement);
		// set selection before testing visibility
		action.setSelection(selection);
		if (action.isValid() && action.isVisible()) {
			return Optional.of(action);
		}
		return Optional.empty();
	}





}
