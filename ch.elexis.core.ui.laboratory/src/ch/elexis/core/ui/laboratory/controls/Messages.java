package ch.elexis.core.ui.laboratory.controls;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.laboratory.controls.messages"; //$NON-NLS-1$
	public static String MultiLineTextCellEditor_title;
	public static String LaborOrdersComposite_actionTitelRemoveWithResult;
	public static String LaborOrdersComposite_columnDate;
	public static String LaborOrdersComposite_columnGroup;
	public static String LaborOrdersComposite_columnOrdernumber;
	public static String LaborOrdersComposite_columnParameter;
	public static String LaborOrdersComposite_columnState;
	public static String LaborOrdersComposite_columnValue;
	public static String LaborOrdersComposite_validatorNotNumber;
	public static String LaborOrdersComposite_NoPatientSelected;
	public static String LaborOrdersComposite_actionTooltipShowHistory;
	
	public static String LaborResultsComposite_columnParameter;
	public static String LaborResultsComposite_columnReference;
	public static String LaborResultsComposite_Documents;
	public static String LaborResultsComposite_textResultTitle;
	public static String LaborResultsComposite_Open;
	
	public static String LaborMappingComposite_columnLabor;
	public static String LaborMappingComposite_columnShortname;
	public static String LaborMappingComposite_labelMappings;
	public static String LaborMappingComposite_labelSelektorMessage;
	public static String LaborMappingComposite_labelSelektorTitle;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages(){}
}
