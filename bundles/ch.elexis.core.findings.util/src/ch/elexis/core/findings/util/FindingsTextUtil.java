package ch.elexis.core.findings.util;

import org.apache.commons.lang3.StringUtils;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationType;
import ch.elexis.core.findings.IObservationLink.ObservationLinkType;
import ch.elexis.core.findings.ObservationComponent;
import ch.elexis.core.findings.codes.CodingSystem;

public class FindingsTextUtil {

	/**
	 * Generate the text of the observation, and set the text property if shouldSet
	 * is true.
	 *
	 * @param shouldSet
	 * @param observation
	 * @return
	 */
	public static String getGroupText(IObservation observation, boolean shouldSet) {
		StringBuilder stringBuilder = new StringBuilder();

		if (ObservationType.REF.equals(observation.getObservationType())) {
			Optional<ICoding> coding = ModelUtil.getCodeBySystem(observation.getCoding(),
					CodingSystem.ELEXIS_LOCAL_CODESYSTEM);
			String title = coding.isPresent() ? coding.get().getDisplay() : StringUtils.EMPTY;
			stringBuilder.append(title + ": ");

			List<IObservation> children = observation.getTargetObseravtions(ObservationLinkType.REF);
			for (int i = 0; i < children.size(); i++) {
				if (i > 0) {
					stringBuilder.append(", ");
				}

				IObservation iObservation = children.get(i);
				if (ObservationType.REF.equals(iObservation.getObservationType())) {
					stringBuilder.append(getGroupText(iObservation, shouldSet));
				} else if (ObservationType.COMP.equals(iObservation.getObservationType())) {
					stringBuilder.append(getObservationText(iObservation, shouldSet));
				} else if (ObservationType.TEXT.equals(iObservation.getObservationType())) {
					stringBuilder.append(getObservationText(iObservation, shouldSet));
				} else if (ObservationType.NUMERIC.equals(iObservation.getObservationType())) {
					stringBuilder.append(getObservationText(iObservation, shouldSet));
				} else if (ObservationType.BOOLEAN.equals(iObservation.getObservationType())) {
					stringBuilder.append(getObservationText(iObservation, shouldSet));
				} else if (ObservationType.DATE.equals(iObservation.getObservationType())) {
					stringBuilder.append(getObservationText(iObservation, shouldSet));
				} else {
					LoggerFactory.getLogger(FindingsTextUtil.class)
							.warn("Unknown ObservationType " + iObservation.getObservationType());
				}
			}

			if (observation.getComment().isPresent()) {
				stringBuilder.append(StringUtils.SPACE + observation.getComment().get());
			}
		} else {
			stringBuilder.append(getObservationText(observation, shouldSet));
		}
		if (shouldSet) {
			observation.setText(stringBuilder.toString());
		}
		return stringBuilder.toString();
	}

	private static String getComponentText(ObservationComponent component, boolean includeUnit) {
		StringBuilder stringBuilder = new StringBuilder();

		ObservationType observationType = component.getTypeFromExtension(ObservationType.class);

		if (ObservationType.TEXT.equals(observationType)) {
			stringBuilder.append(component.getStringValue().orElse(StringUtils.EMPTY));
		} else if (ObservationType.NUMERIC.equals(observationType)) {
			try {
				stringBuilder.append(
						component.getNumericValue().isPresent() ? component.getNumericValue().get().toPlainString()
								: StringUtils.EMPTY);
				if (includeUnit) {
					stringBuilder.append(StringUtils.SPACE);
					stringBuilder.append(component.getNumericValueUnit().orElse(StringUtils.EMPTY));
				}
			} catch (NumberFormatException e) {
				LoggerFactory.getLogger(FindingsTextUtil.class).warn("number illegal format", e);
			}
		}

		return stringBuilder.toString();
	}

	public static String getObservationText(IObservation observation, boolean shouldSet) {
		StringBuilder stringBuilder = new StringBuilder();

		Optional<ICoding> coding = ModelUtil.getCodeBySystem(observation.getCoding(),
				CodingSystem.ELEXIS_LOCAL_CODESYSTEM);
		String title = coding.isPresent() ? coding.get().getDisplay() : StringUtils.EMPTY;

		if (ObservationType.TEXT.equals(observation.getObservationType())) {
			stringBuilder.append(title);
			stringBuilder.append(StringUtils.SPACE);
			stringBuilder.append(observation.getStringValue().orElse(StringUtils.EMPTY));
			if (observation.getComment().isPresent()) {
				stringBuilder.append(" [" + observation.getComment().get() + "]");
			}
		} else if (ObservationType.NUMERIC.equals(observation.getObservationType())) {
			try {
				stringBuilder.append(title);
				stringBuilder.append(StringUtils.SPACE);
				stringBuilder.append(
						observation.getNumericValue().isPresent() ? observation.getNumericValue().get().toPlainString()
								: StringUtils.EMPTY);
				if (observation.getNumericValueUnit().isPresent()) {
					stringBuilder
							.append(StringUtils.SPACE + observation.getNumericValueUnit().orElse(StringUtils.EMPTY));
				}
				if (observation.getComment().isPresent()) {
					stringBuilder.append(" [" + observation.getComment().get() + "]");
				}
			} catch (NumberFormatException e) {
				LoggerFactory.getLogger(FindingsTextUtil.class).warn("number illegal format", e);
			}
		} else if (ObservationType.BOOLEAN.equals(observation.getObservationType())) {
			stringBuilder.append(title);
			stringBuilder.append(StringUtils.SPACE);
			observation.getBooleanValue().ifPresent(value -> {
				stringBuilder.append(value ? "Ja" : "Nein");
			});

			if (observation.getComment().isPresent()) {
				stringBuilder.append(" [" + observation.getComment().get() + "]");
			}
		} else if (ObservationType.DATE.equals(observation.getObservationType())) {
			stringBuilder.append(title);
			stringBuilder.append(StringUtils.SPACE);
			observation.getDateTimeValue().ifPresent(value -> {

				stringBuilder.append(new SimpleDateFormat("dd.MM.yyyy").format(value));
			});

			if (observation.getComment().isPresent()) {
				stringBuilder.append(" [" + observation.getComment().get() + "]");
			}
		} else if (ObservationType.COMP.equals(observation.getObservationType())) {
			stringBuilder.append(title + StringUtils.SPACE);

			String textSplitter = ", ";
			String dbTextSplitter = observation.getFormat("textSeparator");
			if (!dbTextSplitter.isEmpty()) {
				textSplitter = dbTextSplitter;
			}

			List<ObservationComponent> components = observation.getComponents();
			String exactUnit = ModelUtil.getExactUnitOfComponent(components);
			for (int i = 0; i < components.size(); i++) {
				ObservationComponent component = components.get(i);
				if (i > 0) {
					stringBuilder.append(textSplitter);
				}
				stringBuilder.append(getComponentText(component, exactUnit == null));
			}
			if (exactUnit != null) {
				stringBuilder.append(StringUtils.SPACE).append(exactUnit);
			}
			if (observation.getComment().isPresent()) {
				stringBuilder.append(" [" + observation.getComment().get() + "]");
			}
		}
		if (shouldSet) {
			observation.setText(stringBuilder.toString());
			FindingsServiceHolder.getiFindingsService().saveFinding(observation);
		}
		return stringBuilder.toString();
	}
}
