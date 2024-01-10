package ch.elexis.core.services;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.BriefConstants;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IDocumentTemplate;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.text.ITextPlugin;
import ch.elexis.core.text.ReplaceCallback;

@Component
public class DocumentService implements IDocumentService {

	@Reference
	private ITextPlugin textPlugin;

	@Reference
	private ITextReplacementService textReplacementService;

	@Reference
	private List<IDocumentStore> documentStores;

	@Override
	public IDocument createDocument(IDocumentTemplate template, IContext context) {
		try {
			IDocumentStore documentStore = getDocumentStore(template.getStoreId());

			if (textPlugin.loadFromStream(template.getContent(), true)) {
				textPlugin.findOrReplace(ITextReplacementService.MATCH_TEMPLATE, new ReplaceCallback() {
					@Override
					public Object replace(final String in) {
						return textReplacementService.performReplacement(context, in);
					}
				});

				textPlugin.findOrReplace(ITextReplacementService.MATCH_GENDERIZE, new ReplaceCallback() {
					@Override
					public Object replace(final String in) {
						return textReplacementService.performReplacement(context, in);
					}
				});

				IDocument document = documentStore.createDocument(getPatientId(context), getTitle(template, context),
						getCategory(template, context));
				documentStore.saveDocument(document, new ByteArrayInputStream(textPlugin.storeToByteArray()));
				return document;
			} else {
				LoggerFactory.getLogger(getClass()).error("Could not load template " + template.getTitle());
			}
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass()).error("Error creating document from template " + template.getTitle(),
					e);
		}
		return null;
	}

	@Override
	public Map<String, Boolean> validateTemplate(IDocumentTemplate template, IContext context) {
		try {
			HashMap<String, Boolean> result = new HashMap<>();

			if (textPlugin.loadFromStream(template.getContent(), true)) {
				textPlugin.findOrReplace(ITextReplacementService.MATCH_TEMPLATE, new ReplaceCallback() {
					@Override
					public Object replace(final String in) {
						String replacement = textReplacementService.performReplacement(context, in);
						result.put(in, validateIsReplaced(replacement));
						return StringUtils.EMPTY;
					}
				});

				textPlugin.findOrReplace(ITextReplacementService.MATCH_GENDERIZE, new ReplaceCallback() {
					@Override
					public Object replace(final String in) {
						String replacement = textReplacementService.performReplacement(context, in);
						result.put(in, validateIsReplaced(replacement));
						return StringUtils.EMPTY;
					}
				});

				return result;
			} else {
				LoggerFactory.getLogger(getClass()).error("Could not load template " + template.getTitle());
			}
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass()).error("Error validating template " + template.getTitle(), e);
		}
		return Collections.emptyMap();
	}

	private Boolean validateIsReplaced(String replacement) {
		return replacement != null && !"?".equals(replacement) && !"???".equals(replacement);
	}

	@SuppressWarnings("unchecked")
	private String getTitle(IDocumentTemplate template, IContext context) {
		return ((Optional<String>) context.getNamed("title")).orElse(template.getTitle());
	}

	@SuppressWarnings("unchecked")
	private String getCategory(IDocumentTemplate template, IContext context) {
		return ((Optional<String>) context.getNamed("category")).orElse(BriefConstants.UNKNOWN);
	}

	private String getPatientId(IContext context) {
		return context.getTyped(IPatient.class).map(p -> p.getId()).orElse(null);
	}

	private IDocumentStore getDocumentStore(String storeId) {
		if (storeId != null) {
			return documentStores.stream().filter(ds -> storeId.equals(ds.getId())).findAny().orElse(null);
		}
		return null;
	}
}
