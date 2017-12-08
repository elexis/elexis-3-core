package ch.elexis.core.ui.settings;

import static ch.elexis.core.ui.constants.UiPreferenceConstants.USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_CLOSED;
import static ch.elexis.core.ui.constants.UiPreferenceConstants.USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_OPEN;
import static ch.elexis.core.ui.constants.UiPreferenceConstants.USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_REMEMBER_STATE;
import static ch.elexis.core.ui.constants.UiPreferenceConstants.USERSETTINGS2_EXPANDABLE_COMPOSITES;
import static ch.elexis.core.ui.constants.UiPreferenceConstants.USERSETTINGS2_EXPANDABLE_COMPOSITES_STATES;

import org.eclipse.ui.forms.widgets.ExpandableComposite;

import ch.elexis.core.data.activator.CoreHub;

/**
 * 
 * @author marco
 * @since 3.0.0
 */
public class UserSettings {
	/**
	 * save the state of an expandable composite
	 * 
	 * @param field
	 *            name of the composite (any unique string, preferably derived from view name)
	 * @param state
	 *            the state to save
	 * @since 3.0.0 extracted from UserSettings2
	 */
	public static void saveExpandedState(final String field, final boolean state){
		if (state) {
			CoreHub.userCfg.set(USERSETTINGS2_EXPANDABLE_COMPOSITES_STATES + field,
				USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_OPEN);
		} else {
			CoreHub.userCfg.set(USERSETTINGS2_EXPANDABLE_COMPOSITES_STATES + field,
				USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_CLOSED);
		}
	}
	
	/**
	 * Set the state of an expandable Composite to the previously saved state.
	 * 
	 * @param ec
	 *            the expandable Composite to expand or collapse
	 * @param field
	 *            the unique name
	 * @since 3.0.0 extracted from UserSettings2
	 */
	public static void setExpandedState(final ExpandableComposite ec, final String field){
		String mode =
			CoreHub.userCfg.get(USERSETTINGS2_EXPANDABLE_COMPOSITES,
				USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_REMEMBER_STATE);
		if (mode.equals(USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_OPEN)) {
			ec.setExpanded(true);
		} else if (mode.equals(USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_CLOSED)) {
			ec.setExpanded(false);
		} else {
			String state =
				CoreHub.userCfg.get(USERSETTINGS2_EXPANDABLE_COMPOSITES_STATES + field,
					USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_CLOSED);
			if (state.equals(USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_CLOSED)) {
				ec.setExpanded(false);
			} else {
				ec.setExpanded(true);
			}
		}
	}
}
