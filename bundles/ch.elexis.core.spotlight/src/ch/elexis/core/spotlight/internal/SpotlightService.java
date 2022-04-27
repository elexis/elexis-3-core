package ch.elexis.core.spotlight.internal;

import org.apache.commons.lang3.StringUtils;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import ch.elexis.core.spotlight.ISpotlightResult;
import ch.elexis.core.spotlight.ISpotlightResultContributor;
import ch.elexis.core.spotlight.ISpotlightService;
import ch.rgw.tools.TimeTool;

@Component
public class SpotlightService implements ISpotlightService {

	private static final Pattern DATE_PATTERN;

	static {
		DATE_PATTERN = Pattern.compile("([0-9]{1,2}\\.[0-9]{1,2}\\.[0-9]{2,4})");
	}

	@Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	private volatile List<ISpotlightResultContributor> resultContributors;

	Consumer<ISpotlightResult> consumer;

	private ISpotlightResult spotlightResult = new SpotlightResult();

	@Override
	public void setResultsChangedConsumer(Consumer<ISpotlightResult> resultsChangedConsumer) {
		this.consumer = resultsChangedConsumer;
	}

	@Override
	public void computeResult(String searchInput, Map<String, String> searchParams) {
		spotlightResult.clear();

		if (searchInput == null || searchInput.length() == 0) {
			// TODO or show initial set
			return;
		}

		// TODO on math operations allow resp. input
		String[] searchTerms = searchInput.trim().toLowerCase().replaceAll("[^a-z0-9 .,=%]", StringUtils.EMPTY)
				.split(StringUtils.SPACE);

		List<String> stringTerms = new ArrayList<String>(searchTerms.length);
		List<LocalDate> dateTerms = new ArrayList<LocalDate>(searchTerms.length);
		List<Number> numericTerms = new ArrayList<Number>(searchTerms.length);

		for (String term : searchTerms) {
			if (term.length() == 0) {
				continue;
			}

			if ((term.charAt(0) >= 'a' && term.charAt(0) <= 'z') || (term.length() > 1 && term.charAt(0) == '%')) {
				// early break, starts with char, must be alphanumeric
				stringTerms.add(term);
				continue;
			}

			if (NumberUtils.isCreatable(term)) {
				// numeric chars only - e.g. patient number or amount
				Number number = NumberUtils.createNumber(term);
				numericTerms.add(number);
				continue;
			}

			// numeric and dot - date match
			Matcher datePatternMatcher = DATE_PATTERN.matcher(term);
			if (datePatternMatcher.find()) {
				String group = datePatternMatcher.group();
				LocalDate localDate = new TimeTool(group).toLocalDate();
				dateTerms.add(localDate);
				continue;
			}

			System.out.println("unhandled " + term);
			// TODO calculations -> numeric and maths operator
			// TODO context setting -> if key=value add to contextParameters
			// TODO Command integration
		}

		if (stringTerms.isEmpty() && dateTerms.isEmpty() && numericTerms.isEmpty()) {
			return;
		}

		// only parallel fetch the first 5 of every category? show total count?
		resultContributors.parallelStream().forEach(contributor -> {
			contributor.computeResult(stringTerms, dateTerms, numericTerms, spotlightResult, searchParams);
			// after each contributor finished, notify the consumer
			consumer.accept(spotlightResult);
		});

	}

}
