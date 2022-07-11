package ch.elexis.core.ui.e4.events;

import ch.elexis.core.model.IDocument;

public class ElexisUiEventTopics {

	public static final String EVENT_BASE_UI = "ui/"; //$NON-NLS-1$

	public static final String EVENT_PREVIEW_MIMETYPE = EVENT_BASE_UI + "preview/mimetype/"; //$NON-NLS-1$
	/**
	 * Expects {@link IDocument}, will call {@link IDocument#getContent()}
	 */
	public static final String EVENT_PREVIEW_MIMETYPE_PDF = EVENT_PREVIEW_MIMETYPE + "application/pdf"; //$NON-NLS-1$

}
