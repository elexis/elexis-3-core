package ch.elexis.core.services;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.BriefConstants;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IDocumentTemplate;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.internal.text.RecipeDocumentTemplateReplacement;
import ch.elexis.core.text.ITextPlugin;
import ch.elexis.core.text.ReplaceCallback;

@Component
public class DocumentService implements IDocumentService {

	public static final String MATCH_DIRECTTEMPLATE = "\\[[-a-zA-ZäöüÄÖÜéàè_]+\\]";

	@Reference
	private ITextPlugin textPlugin;

	@Reference
	private ITextReplacementService textReplacementService;

	@Reference
	private List<IDocumentStore> documentStores;

	private Map<String, IDirectTemplateReplacement> directTemplateReplacement;

	@Activate
	public void activate() {
		directTemplateReplacement = new HashMap<>();

		addDirectTemplateReplacement("[Rezeptzeilen]",
				new RecipeDocumentTemplateReplacement("[Rezeptzeilen]", false, false));
		addDirectTemplateReplacement("[RezeptzeilenExt]",
				new RecipeDocumentTemplateReplacement("[RezeptzeilenExt]", false, true));
		addDirectTemplateReplacement("[Medikamentenliste]",
				new RecipeDocumentTemplateReplacement("[Medikamentenliste]", true, false));
		addDirectTemplateReplacement("[MedikamentenlisteExt]",
				new RecipeDocumentTemplateReplacement("[MedikamentenlisteExt]", true, true));
	}

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

				List<String> matches = textPlugin.findMatching(MATCH_DIRECTTEMPLATE);
				for (String string : matches) {
					IDirectTemplateReplacement templateReplacement = directTemplateReplacement.get(string);
					if (templateReplacement != null) {
						templateReplacement.replace(textPlugin, context);
					}
				}

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
				
				List<String> matches = textPlugin.findMatching(MATCH_DIRECTTEMPLATE);
				for (String string : matches) {
					IDirectTemplateReplacement templateReplacement = directTemplateReplacement.get(string);
					if (templateReplacement != null) {
						result.put(string, templateReplacement.replace(textPlugin, context));
					}
				}

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

	@Override
	public void addDirectTemplateReplacement(String template, IDirectTemplateReplacement textTemplateConsumer) {
		if (directTemplateReplacement.containsKey(template)) {
			LoggerFactory.getLogger(getClass()).warn(
					"Direct template consumer [" + template + "] replaced with [" + textTemplateConsumer + "]");
		}
		directTemplateReplacement.put(template, textTemplateConsumer);
	}
}
