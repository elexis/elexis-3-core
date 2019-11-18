package ch.elexis.core.services;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import ch.elexis.core.text.ITextPlaceholderResolver;

@Component
public class TextReplacementService implements ITextReplacementService {
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE)
	private List<ITextPlaceholderResolver> placeholderResolvers;
	
	private static final String DONT_SHOW_REPLACEMENT_ERRORS = "*";
	public static final String MATCH_TEMPLATE = "\\[[" + DONT_SHOW_REPLACEMENT_ERRORS //$NON-NLS-1$
		+ "]?[-a-zA-ZäöüÄÖÜéàè_ ]+\\.[-a-zA-Z0-9äöüÄÖÜéàè_ ]+\\]";
	
	private Pattern matchTemplate;
	
	@Activate
	public void activate(){
		matchTemplate = Pattern.compile(MATCH_TEMPLATE);
	}
	
	@Override
	public String performReplacement(IContext context, String template){
		Matcher matcher = matchTemplate.matcher(template);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String found = matcher.group();
			matcher.appendReplacement(sb,
				Matcher.quoteReplacement((String) replacePlaceholder(context, found)));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
	
	private String replacePlaceholder(IContext context, String placeholder){
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
	
}
