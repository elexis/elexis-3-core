package ch.elexis.core.ui.p2.policy;

import org.eclipse.equinox.p2.engine.query.UserVisibleRootQuery;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.ui.Policy;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Point;

import ch.elexis.core.ui.p2.Activator;
import ch.elexis.core.ui.p2.internal.PreferenceConstants;

/**
 * ElexisCloudPolicy defines the RCP Cloud Example policies for the p2 UI. The policy is registered
 * as an OSGi service when the bundle starts.
 */
public class ElexisCloudPolicy extends Policy {
	
	public void updateForPreferences(){
		
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		setRepositoriesVisible(prefs.getBoolean(PreferenceConstants.REPOSITORIES_VISIBLE));
		setRestartPolicy(prefs.getInt(PreferenceConstants.RESTART_POLICY));
		setShowLatestVersionsOnly(prefs.getBoolean(PreferenceConstants.SHOW_LATEST_VERSION_ONLY));
		setGroupByCategory(prefs.getBoolean(PreferenceConstants.AVAILABLE_GROUP_BY_CATEGORY));
		setShowDrilldownRequirements(prefs
			.getBoolean(PreferenceConstants.SHOW_DRILLDOWN_REQUIREMENTS));
		setFilterOnEnv(prefs.getBoolean(PreferenceConstants.FILTER_ON_ENV));
		setUpdateWizardStyle(prefs.getInt(PreferenceConstants.UPDATE_WIZARD_STYLE));
		int preferredWidth = prefs.getInt(PreferenceConstants.UPDATE_DETAILS_WIDTH);
		int preferredHeight = prefs.getInt(PreferenceConstants.UPDATE_DETAILS_HEIGHT);
		setUpdateDetailsPreferredSize(new Point(preferredWidth, preferredHeight));
		
		if (prefs.getBoolean(PreferenceConstants.AVAILABLE_SHOW_ALL_BUNDLES))
			setVisibleAvailableIUQuery(QueryUtil.ALL_UNITS);
		else
			setVisibleAvailableIUQuery(QueryUtil.createIUGroupQuery());
		if (prefs.getBoolean(PreferenceConstants.INSTALLED_SHOW_ALL_BUNDLES))
			setVisibleAvailableIUQuery(QueryUtil.ALL_UNITS);
		else
			setVisibleAvailableIUQuery(new UserVisibleRootQuery());
		setContactAllSites(false);
	}
}
