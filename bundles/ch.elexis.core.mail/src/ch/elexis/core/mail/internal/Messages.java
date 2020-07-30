package ch.elexis.core.mail.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.mail.internal.messages"; //$NON-NLS-1$
	public static String SendMailRunnable_1;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages(){}
}
