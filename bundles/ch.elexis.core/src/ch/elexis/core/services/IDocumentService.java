package ch.elexis.core.services;

import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IDocumentTemplate;
import ch.elexis.core.text.ITextPlaceholderResolver;

public interface IDocumentService {

	/**
	 * Create a {@link IDocument} from the {@link IDocumentTemplate} replacing text
	 * placeholders using {@link ITextPlaceholderResolver} instances with the
	 * provided {@link IContext}.
	 * 
	 * @param template
	 * @param context
	 * @return
	 */
	public IDocument createDocument(IDocumentTemplate template, IContext context);

}
