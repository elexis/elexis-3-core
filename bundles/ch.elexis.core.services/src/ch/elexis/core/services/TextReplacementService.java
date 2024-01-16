package ch.elexis.core.services;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.LoggerFactory;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.text.ITextPlaceholderResolver;
import ch.elexis.core.types.Gender;

@Component
public class TextReplacementService implements ITextReplacementService {

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY)
	private List<ITextPlaceholderResolver> placeholderResolvers;

	private Pattern matchTemplate;

	private Pattern matchGenderize;

	private Pattern matchDataAccess;

	@Activate
	public void activate() {
		matchTemplate = Pattern.compile(MATCH_TEMPLATE);
		matchGenderize = Pattern.compile(MATCH_GENDERIZE);
		matchDataAccess = Pattern.compile(MATCH_IDATACCESS);
	}

	private String replacePlaceholder(IContext context, String placeholder) {
		String substring = placeholder.substring(1, placeholder.length() - 1);
		String[] split = substring.split("\\.");
		if (split.length > 1) {
			for (ITextPlaceholderResolver resolver : placeholderResolvers) {
				if (resolver.getSupportedType().equalsIgnoreCase(split[0])) {
					Optional<String> result = resolver.replaceByTypeAndAttribute(context,
							substring.substring(split[0].length() + 1));
					if (result.isPresent()) {
						return result.get();
					}
				}
			}
		}
		return "?";
	}

	@Override
	public List<ITextPlaceholderResolver> getResolvers() {
		return placeholderResolvers;
	}

	@Override
	public String performReplacement(IContext context, String template, String newLinePattern) {
		Matcher matcher = matchTemplate.matcher(template);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String found = matcher.group();
			matcher.appendReplacement(sb, Matcher.quoteReplacement(replacePlaceholder(context, found)));
		}
		matcher.appendTail(sb);

		matcher = matchGenderize.matcher(sb.toString());
		sb = new StringBuffer();
		while (matcher.find()) {
			String found = matcher.group();
			matcher.appendReplacement(sb, Matcher.quoteReplacement(replaceGenderize(context, found)));
		}
		matcher.appendTail(sb);

		matcher = matchDataAccess.matcher(sb.toString());
		sb = new StringBuffer();
		while (matcher.find()) {
			String found = matcher.group();
			matcher.appendReplacement(sb, Matcher.quoteReplacement(replaceDataAccess(context, found)));
		}
		matcher.appendTail(sb);

		return sb.toString().replaceAll("(\r\n|\n\r|\r|\n)", newLinePattern);
	}

	private String replaceGenderize(IContext context, String found) {
		String inl = found.substring(1, found.length() - 1);
		boolean showErrors = true;
		if (inl.substring(0, 1).equalsIgnoreCase(DONT_SHOW_REPLACEMENT_ERRORS)) {
			inl = inl.substring(1);
			showErrors = false;
		}
		Identifiable identifiable = null;
		String[] split = inl.split(":"); //$NON-NLS-1$
		for (ITextPlaceholderResolver resolver : placeholderResolvers) {
			if (resolver.getSupportedType().equalsIgnoreCase(split[0])) {
				identifiable = resolver.getIdentifiable(context).orElse(null);
				if (identifiable != null) {
					break;
				}
			}
		}
		if (identifiable == null) {
			if (showErrors) {
				return "???"; //$NON-NLS-1$
			} else {
				return StringUtils.EMPTY;
			}
		}
		if (split.length != 3) {
			LoggerFactory.getLogger(getClass()).error("Invalid genderize Format [" + inl + "]"); //$NON-NLS-1$
			return null;
		}
		if (!(identifiable instanceof IContact)) {
			if (showErrors) {
				return Messages.TextContainer_FieldTypeForContactsOnly;
			} else {
				return StringUtils.EMPTY;
			}
		}
		IContact contact = (IContact) identifiable;
		String[] g = split[2].split("/"); //$NON-NLS-1$
		if (g.length < 2) {
			if (showErrors) {
				return Messages.TextContainer_BadFieldDefinition;
			} else {
				return StringUtils.EMPTY;
			}
		}
		if (contact.isPerson()) {
			IPerson person = contact.asIPerson();

			if (person.getGender() == Gender.MALE) {
				if (split[1].startsWith("m")) { //$NON-NLS-1$
					return g[0].trim();
				}
				return g[1].trim();
			} else {
				if (split[1].startsWith("w")) { //$NON-NLS-1$
					return g[0].trim();
				}
				return g[1].trim();
			}
		} else {
			if (g.length < 3) {
				if (showErrors) {
					return Messages.TextContainer_FieldTypeForPersonsOnly;
				} else {
					return StringUtils.EMPTY;
				}
			}
			return g[2];
		}
	}

	private String replaceDataAccess(IContext context, String placeholder) {
		String substring = placeholder.substring(1, placeholder.length() - 1);

		String[] adr = substring.split(":");
		if (adr.length < 4) {
			LoggerFactory.getLogger(getClass()).warn("Invalid data access placeholder [" + substring + "]");
			return null;
		}
		String type = adr[0];
		String dependendObject = adr[1];
		String dates = adr[2];
		String desc = adr[3];
		String[] params = null;
		if (adr.length == 5) {
			params = adr[4].split("\\.");
		}

		if (StringUtils.isNoneBlank(type) && StringUtils.isNoneBlank(desc)) {
			for (ITextPlaceholderResolver resolver : placeholderResolvers) {
				if (resolver.getSupportedType().equalsIgnoreCase(type)) {
					Optional<String> result = resolver.replaceByTypeAndAttribute(context, desc);
					if (result.isPresent()) {
						return result.get();
					}
				}
			}
		}
		return "?";
	}
}
