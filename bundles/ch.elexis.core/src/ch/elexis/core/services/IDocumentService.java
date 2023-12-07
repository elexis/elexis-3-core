package ch.elexis.core.services;

import java.util.Map;

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

	/**
	 * Validate if all placeholders in the {@link IDocumentTemplate} can be resolved
	 * with the {@link IContext}. All placeholders are in the returned {@link Map}
	 * with the corresponding resolving result.
	 * 
	 * @param template
	 * @param context
	 * @return
	 */
	public Map<String, Boolean> validateTemplate(IDocumentTemplate template, IContext context);
}
