package ch.elexis.core.ui.text;

import java.util.HashMap;
import java.util.Map;

public class MimeTypeUtil {
	public static final String MIME_TYPE_ALL = "Alle";
	public static final String MIME_TYPE_OPENOFFICE = "application/vnd.oasis.opendocument.text";
	public static final String MIME_TYPE_MSWORD_97_2003 = "doc";
	public static final String MIME_TYPE_MSWORD = "docx";
	public static final String MIME_TYPE_TEMPLATOR = "text/xml";
	
	private static final Map<String, String> mimeNameMap;
	private static final Map<String, String> mimeExtensionsMap;
	
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
}
