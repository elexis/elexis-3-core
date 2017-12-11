package ch.elexis.core.ui.laboratory.actions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.laboratory.actions.messages"; //$NON-NLS-1$
	public static String LaborResultEditDetailAction_title;
	public static String TogglePathologic_title;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages(){}
}
