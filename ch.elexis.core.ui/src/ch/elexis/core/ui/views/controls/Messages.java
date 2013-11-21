package ch.elexis.core.ui.views.controls;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.views.controls.messages"; //$NON-NLS-1$
	public static String KontaktSelectionComposite_message;
	public static String KontaktSelectionComposite_title;
	public static String LaborMappingComposite_columnLabor;
	public static String LaborMappingComposite_columnShortname;
	public static String LaborMappingComposite_labelMappings;
	public static String LaborMappingComposite_labelSelektorMessage;
	public static String LaborMappingComposite_labelSelektorTitle;
	public static String LaborSelectionComposite_message;
	public static String LaborSelectionComposite_title;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages(){}
}
