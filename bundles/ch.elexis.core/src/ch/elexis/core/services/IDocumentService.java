package ch.elexis.core.services;

import java.util.Map;
import java.util.Optional;

import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IDocumentTemplate;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.status.ObjectStatus;
import ch.elexis.core.text.ITextPlaceholderResolver;
import ch.elexis.core.text.ITextPlugin;

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
	ObjectStatus<IDocument> createDocument(IDocumentTemplate template, IContext context);

	/**
	 * Validate if all placeholders in the {@link IDocumentTemplate} can be resolved
	 * with the {@link IContext}. All placeholders are in the returned {@link Map}
	 * with the corresponding resolving result.
	 * 
	 * @param template
	 * @param context
	 * @return
	 */
	Map<String, Boolean> validateTemplate(IDocumentTemplate template, IContext context);

	/**
	 * Add an {@link IDirectTemplateReplacement} that will be called for matching
	 * template found in a {@link IDocumentTemplate}.
	 * 
	 * @param template
	 * @param textTemplateConsumer
	 */
	void addDirectTemplateReplacement(String template,
			IDirectTemplateReplacement textTemplateConsumer);

	/**
	 * Interface for direct template replacement using provided {@link ITextPlugin}
	 * and {@link IContext}.
	 */
	interface IDirectTemplateReplacement {

		/**
		 * Perform the replacement in the document. Return false if something went
		 * wrong.
		 * 
		 * @param textPlugin
		 * @param context
		 * @return
		 */
		boolean replace(ITextPlugin textPlugin, IContext context);

		/**
		 * Get the {@link Identifiable} matching the type of this
		 * {@link ITextPlaceholderResolver} from the provided {@link IContext}.
		 * 
		 * @param context
		 * @return
		 */
		default Optional<? extends Identifiable> getIdentifiable(IContext context) {
			return Optional.empty();
		}
	}

}
