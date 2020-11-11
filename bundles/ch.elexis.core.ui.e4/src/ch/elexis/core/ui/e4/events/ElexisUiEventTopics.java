package ch.elexis.core.ui.e4.events;

import java.io.InputStream;

public class ElexisUiEventTopics {
	
	public static final String EVENT_BASE_UI = "ui/";

	public static final String EVENT_PREVIEW_MIMETYPE = EVENT_BASE_UI+"preview/mimetype/";
	/**
	 * Expects {@link InputStream}
	 */
	public static final String EVENT_PREVIEW_MIMETYPE_PDF = EVENT_PREVIEW_MIMETYPE+"application/pdf";

}
