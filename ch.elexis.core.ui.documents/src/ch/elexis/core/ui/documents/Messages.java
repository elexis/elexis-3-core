package ch.elexis.core.ui.documents;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.documents.messages"; //$NON-NLS-1$

	

	public static String DocumentsView_Title;
	public static String DocumentView_categoryColumn;
	public static String DocumentView_lastChangedColumn;
	public static String DocumentView_dateCreatedColumn;
	public static String DocumentView_stateColumn;
	public static String DocumentView_titleColumn;
	public static String DocumentView_keywordsColumn;
	public static String DocumentView_searchLabel;
	public static String DocumentView_reallyDeleteCaption;
	public static String DocumentView_reallyDeleteContents;
	public static String DocumentView_cantReadCaption;
	public static String DocumentView_cantReadText;
	public static String DocumentView_fileNameTooLong;
	public static String DocumentView_importErrorCaption;
	public static String DocumentView_importErrorText2;
	public static String DocumentView_saveErrorCaption;
	public static String DocumentView_saveErrorText;
	public static String DocumentView_exportErrorCaption;
	public static String DocumentView_exportErrorText;
	public static String DocumentsView_extensionColumn;
	public static String DocumentView_exportErrorEmptyText;
	
	public static String DocumentMetaDataDialog_title;
	public static String DocumentMetaDataDialog_titleMessage;
	public static String DocumentMetaDataDialog_newCategory;
	public static String DocumentMetaDataDialog_renameCategory;
	public static String DocumentMetaDataDialog_renameCategoryConfirm;
	public static String DocumentMetaDataDialog_renameCategoryText;
	public static String DocumentMetaDataDialog_deleteCategory;
	public static String DocumentMetaDataDialog_deleteCategoryConfirm;
	public static String DocumentMetaDataDialog_deleteCategoryText;
	public static String DocumentMetaDataDialog_deleteCategoryError;
	public static String DocumentMetaDataDialog_deleteCategoryErrorText;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages(){}
}
