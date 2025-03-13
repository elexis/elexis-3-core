package ch.elexis.core.ui.e4.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.model.RoleConstants;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IUserService;
import jakarta.inject.Inject;

public class HidePreferencesPageHandler {

	private static final List<IPreferenceNode> hiddenNodes = new ArrayList<>();
	private static final String ALLOWED_PREFERENCE_PAGE_ID = "ch.elexis.preferences.UserPreferences";

	@Inject
	private IContextService contextService;

	@Inject
	private IUserService userService;

	@Execute
	public void execute() {
		contextService.getActiveUser().ifPresent(u -> {
			boolean hasRole = userService.hasRole(u, RoleConstants.ACCESSCONTROLE_ROLE_ICT_ADMINISTRATOR);
			if (hasRole && hiddenNodes.isEmpty()) {
				return;
			}
			PreferenceManager pm = PlatformUI.getWorkbench().getPreferenceManager();
			IPreferenceNode[] nodes = pm.getRootSubNodes();
			PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
				if (!hasRole) {
					for (IPreferenceNode node : nodes) {
						if (!node.getId().equals(ALLOWED_PREFERENCE_PAGE_ID)) {
							hiddenNodes.add(node);
							pm.remove(node);
						}
					}
				} else {
					hiddenNodes.forEach(pm::addToRoot);
					hiddenNodes.clear();
				}
			});
		});
	}

}
