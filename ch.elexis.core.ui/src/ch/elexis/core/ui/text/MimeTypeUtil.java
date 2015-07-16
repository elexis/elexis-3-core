package ch.elexis.core.ui.text;

import java.util.HashMap;
import java.util.Map;

public class MimeTypeUtil {
	public static final String MIME_TYPE_ALL = "Alle";
	public static final String MIME_TYPE_OPENOFFICE = "application/vnd.oasis.opendocument.text";
	public static final String MIME_TYPE_MSWORD_97_2003 = "doc";
	public static final String MIME_TYPE_MSWORD = "docx";
	public static final String MIME_TYPE_TEMPLATOR = "text/xml";
	
	public static final String SIMPLE_NAME_MSWORD = "MSWord";
	public static final String SIMPLE_NAME_OPENOFFICE = "OpenOffice";
	public static final String SIMPLE_NAME_TEMPLATOR = "Templator";
	
	private static final Map<String, String> mimeNameMap;
	private static final Map<String, String> mimeExtensionsMap;
	private static final Map<String, String> mimeSimpleNameMap;
	
	static {
		// init mimeType-human readable name map
		mimeNameMap = new HashMap<String, String>();
		mimeNameMap.put(MIME_TYPE_MSWORD_97_2003, "Word 97-2003 Document (*.doc)");
		mimeNameMap.put(MIME_TYPE_MSWORD, "Word Document (*.docx)");
		mimeNameMap.put(MIME_TYPE_OPENOFFICE, "OpenDocument Text (*.odt)");
		mimeNameMap.put(MIME_TYPE_TEMPLATOR, "Schablonenprozessor (*.xml)");
		
		// init mimeType-Extensions map
		mimeExtensionsMap = new HashMap<String, String>();
		mimeExtensionsMap.put(MIME_TYPE_MSWORD_97_2003, "*.doc");
		mimeExtensionsMap.put(MIME_TYPE_MSWORD, "*.docx");
		mimeExtensionsMap.put(MIME_TYPE_OPENOFFICE, "*.odt");
		mimeExtensionsMap.put(MIME_TYPE_TEMPLATOR, "*.xml");
		
		// init mimeType-SimpleName map
		mimeSimpleNameMap = new HashMap<String, String>();
		mimeSimpleNameMap.put(MIME_TYPE_MSWORD, SIMPLE_NAME_MSWORD);
		mimeSimpleNameMap.put(MIME_TYPE_MSWORD_97_2003, SIMPLE_NAME_MSWORD);
		mimeSimpleNameMap.put(MIME_TYPE_OPENOFFICE, SIMPLE_NAME_OPENOFFICE);
		mimeSimpleNameMap.put(MIME_TYPE_TEMPLATOR, SIMPLE_NAME_TEMPLATOR);
	}
	
	public static String getPrettyPrintName(String mimeType){
		String readableName = mimeNameMap.get(mimeType);
		if (readableName == null || readableName.isEmpty()) {
			readableName = "unbekannt";
		}
		return readableName;
	}
	
	public static String getExtensions(String mimeType){
		String extension = mimeExtensionsMap.get(mimeType);
		if (extension == null || extension.isEmpty()) {
			return "*.*";
		}
		return extension;
	}
	
	public static String getSimpleName(String mimeType){
		String simpleName = mimeSimpleNameMap.get(mimeType);
		if (simpleName == null || simpleName.isEmpty()) {
			return "unbekannt";
		}
		return simpleName;
	}
}
