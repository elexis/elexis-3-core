package ch.elexis.core.jpa;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static final String BUNDLE_NAME = "ch.elexis.core.jpa.messages";

	public static String ElexisEntityManger_Database_Init;
	public static String ElexisEntityManger_Database_Update;
	public static String LiquibaseDBInitializer_At;
	public static String LiquibaseDBInitializer_Database_Locked;
	public static String LiquibaseDBInitializer_Init_Execute;
	public static String LiquibaseDBUpdater_Update_execute;

	static { // load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
