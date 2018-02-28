package ch.elexis.core.ui.startup;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.startup.messages"; //$NON-NLS-1$
	public static String UiStartup_errormessage;
	public static String UiStartup_errortitle;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages(){}
}
