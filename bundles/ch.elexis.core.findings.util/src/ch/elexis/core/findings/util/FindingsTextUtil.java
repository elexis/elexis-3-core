package ch.elexis.core.findings.util;

import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
			String title = getNameText(observation);
			stringBuilder.append(title).append(": ");
			List<IObservation> children = observation.getTargetObseravtions(ObservationLinkType.REF);
			for (int i = 0; i < children.size(); i++) {
				if (i > 0) {
					stringBuilder.append(", ");
				}
				stringBuilder.append(getObservationText(children.get(i), shouldSet));
			}

			observation.getComment().ifPresent(comment -> stringBuilder.append(StringUtils.SPACE).append(comment));
		} else {
			stringBuilder.append(getObservationText(observation, shouldSet));
		}
		if (shouldSet) {
			observation.setText(stringBuilder.toString());
		}
		return stringBuilder.toString();
	}

    public static String getObservationText(IObservation observation, boolean shouldSet) {
        String title = getNameText(observation);
        String value = getValueText(observation);

        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(title)) {
			stringBuilder.append(title).append(StringUtils.SPACE);
        }
        stringBuilder.append(value);

        if (shouldSet) {
            observation.setText(stringBuilder.toString());
        }

        return stringBuilder.toString();
    }

	public static String getNameText(IObservation observation) {
        return ModelUtil.getCodeBySystem(observation.getCoding(), CodingSystem.ELEXIS_LOCAL_CODESYSTEM)
                .map(ICoding::getDisplay).orElse(StringUtils.EMPTY);
    }

	public static String getValueText(IObservation observation) {
        switch (observation.getObservationType()) {
            case TEXT:
                return getStringValue(observation);
            case NUMERIC:
                return getNumericValue(observation);
            case BOOLEAN:
                return getBooleanValue(observation);
            case DATE:
                return getDateValue(observation);
            case COMP:
                return getCompValue(observation);
            default:
                LoggerFactory.getLogger(FindingsTextUtil.class).warn("Unknown ObservationType " + observation.getObservationType());
                return StringUtils.EMPTY;
        }
    }

	public static String getStringValue(IObservation observation) {
		StringBuilder sb = new StringBuilder(observation.getStringValue().orElse(StringUtils.EMPTY));
		sb.append(getCommentText(observation));
		return sb.toString();
	}

	public static String getNumericValue(IObservation observation) {
		StringBuilder sb = new StringBuilder();
		observation.getNumericValue().ifPresent(value -> sb.append(value.toPlainString()));
		observation.getNumericValueUnit().ifPresent(unit -> sb.append(StringUtils.SPACE).append(unit));
		sb.append(getCommentText(observation));
		return sb.toString();
	}

	public static String getBooleanValue(IObservation observation) {
		StringBuilder sb = new StringBuilder();
		observation.getBooleanValue().ifPresent(value -> sb.append(value ? "Ja" : "Nein"));
		sb.append(getCommentText(observation));
		return sb.toString();
	}

	public static String getDateValue(IObservation observation) {
		StringBuilder sb = new StringBuilder();
		observation.getDateTimeValue().ifPresent(date -> sb.append(new SimpleDateFormat("dd.MM.yyyy").format(date)));
		sb.append(getCommentText(observation));
		return sb.toString();
	}

	public static String getCompValue(IObservation observation) {
        StringBuilder sb = new StringBuilder();
        String textSplitter = observation.getFormat("textSeparator");
        if (StringUtils.isEmpty(textSplitter)) {
            textSplitter = ", ";
        }

        List<ObservationComponent> components = observation.getComponents();
        String exactUnit = ModelUtil.getExactUnitOfComponent(components);
        for (int i = 0; i < components.size(); i++) {
            if (i > 0) {
                sb.append(textSplitter);
            }
            sb.append(getComponentText(components.get(i), exactUnit == null));
        }
        if (exactUnit != null) {
            sb.append(" ").append(exactUnit);
        }
		observation.getComment().ifPresent(comment -> sb.append(" [").append(comment).append("]"));

        return sb.toString();
    }

	private static String getCommentText(IObservation observation) {
		return observation.getComment().filter(StringUtils::isNotBlank).map(comment -> " [" + comment + "]")
				.orElse(StringUtils.EMPTY);
	}

	public static String getComponentText(ObservationComponent component, boolean showUnitInComponent) {
        StringBuilder sb = new StringBuilder();
        component.getNumericValue().ifPresent(value -> sb.append(value.toPlainString()));
        if (showUnitInComponent) {
			component.getNumericValueUnit().ifPresent(unit -> sb.append(StringUtils.SPACE).append(unit));
        }
        return sb.toString();
    }
}
