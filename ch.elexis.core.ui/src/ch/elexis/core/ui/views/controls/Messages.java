package ch.elexis.core.ui.views.controls;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.views.controls.messages"; //$NON-NLS-1$
	public static String ArticleDefaultSignatureComposite_applicationInstruction;
	public static String ArticleDefaultSignatureComposite_dispensation;
	public static String ArticleDefaultSignatureComposite_dosage;
	public static String ArticleDefaultSignatureComposite_evening;
	public static String ArticleDefaultSignatureComposite_fix;
	public static String ArticleDefaultSignatureComposite_morning;
	public static String ArticleDefaultSignatureComposite_night;
	public static String ArticleDefaultSignatureComposite_noon;
	public static String ArticleDefaultSignatureComposite_onArticle;
	public static String ArticleDefaultSignatureComposite_onAtc;
	public static String ArticleDefaultSignatureComposite_recipe;
	public static String ArticleDefaultSignatureComposite_reserve;
	public static String ArticleDefaultSignatureComposite_sympomatic;
	
	public static String KontaktSelectionComposite_message;
	public static String KontaktSelectionComposite_title;
	public static String LaborSelectionComposite_message;
	public static String LaborSelectionComposite_title;
	
	public static String StockDetailComposite_availableInStock;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages(){}
}
