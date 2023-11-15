package ch.elexis.core.text;

import java.util.HashMap;
import java.util.Map;

public class MimeTypeUtil {
	public static final String MIME_TYPE_ALL = "Alle"; //$NON-NLS-1$
	public static final String MIME_TYPE_OPENOFFICE = "application/vnd.oasis.opendocument.text"; //$NON-NLS-1$
	public static final String MIME_TYPE_MSWORD_97_2003 = "doc"; //$NON-NLS-1$
	public static final String MIME_TYPE_MSWORD = "docx"; //$NON-NLS-1$
	public static final String MIME_TYPE_TEMPLATOR = "text/xml"; //$NON-NLS-1$

	public static final String SIMPLE_NAME_MSWORD = "MSWord"; //$NON-NLS-1$
	public static final String SIMPLE_NAME_OPENOFFICE = "OpenOffice"; //$NON-NLS-1$
	public static final String SIMPLE_NAME_TEMPLATOR = "Templator"; //$NON-NLS-1$

	private static final Map<String, String> mimeNameMap;
	private static final Map<String, String> mimeExtensionsMap;
	private static final Map<String, String> mimeSimpleNameMap;

	static {
		// init mimeType-human readable name map
		mimeNameMap = new HashMap<String, String>();
		mimeNameMap.put(MIME_TYPE_MSWORD_97_2003, "Word 97-2003 Document (*.doc)"); //$NON-NLS-1$
		mimeNameMap.put(MIME_TYPE_MSWORD, "Word Document (*.docx)"); //$NON-NLS-1$
		mimeNameMap.put(MIME_TYPE_OPENOFFICE, "OpenDocument Text (*.odt)"); //$NON-NLS-1$
		mimeNameMap.put(MIME_TYPE_TEMPLATOR, "Schablonenprozessor (*.xml)"); //$NON-NLS-1$

		// init mimeType-Extensions map
		mimeExtensionsMap = new HashMap<String, String>();
		mimeExtensionsMap.put(MIME_TYPE_MSWORD_97_2003, "*.doc"); //$NON-NLS-1$
		mimeExtensionsMap.put(MIME_TYPE_MSWORD, "*.docx"); //$NON-NLS-1$
		mimeExtensionsMap.put(MIME_TYPE_OPENOFFICE, "*.odt"); //$NON-NLS-1$
		mimeExtensionsMap.put(MIME_TYPE_TEMPLATOR, "*.xml"); //$NON-NLS-1$

		// init mimeType-SimpleName map
		mimeSimpleNameMap = new HashMap<String, String>();
		mimeSimpleNameMap.put(MIME_TYPE_MSWORD, SIMPLE_NAME_MSWORD);
		mimeSimpleNameMap.put(MIME_TYPE_MSWORD_97_2003, SIMPLE_NAME_MSWORD);
		mimeSimpleNameMap.put(MIME_TYPE_OPENOFFICE, SIMPLE_NAME_OPENOFFICE);
		mimeSimpleNameMap.put(MIME_TYPE_TEMPLATOR, SIMPLE_NAME_TEMPLATOR);
	}

	public static String getPrettyPrintName(String mimeType) {
		String readableName = mimeNameMap.get(mimeType);
		if (readableName == null || readableName.isEmpty()) {
			readableName = "unbekannt";
		}
		return readableName;
	}

	public static String getExtensions(String mimeType) {
		String extension = mimeExtensionsMap.get(mimeType);
		if (extension == null || extension.isEmpty()) {
			return "*.*"; //$NON-NLS-1$
		}
		return extension;
	}

	public static String getSimpleName(String mimeType) {
		String simpleName = mimeSimpleNameMap.get(mimeType);
		if (simpleName == null || simpleName.isEmpty()) {
			return "unbekannt";
		}
		return simpleName;
	}
}
