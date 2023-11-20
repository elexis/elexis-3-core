package ch.elexis.core.services;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import ch.elexis.core.text.ITextPlaceholderResolver;

@Component
public class TextReplacementService implements ITextReplacementService {

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY)
	private List<ITextPlaceholderResolver> placeholderResolvers;

	private Pattern matchTemplate;

	@Activate
	public void activate() {
		matchTemplate = Pattern.compile(MATCH_TEMPLATE);
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
		return sb.toString().replaceAll("(\r\n|\n\r|\r|\n)", newLinePattern);
	}
}
