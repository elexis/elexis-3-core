package ch.elexis.core.findings.util;

import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.ObservationComponent;
import ch.elexis.core.findings.codes.CodingSystem;

public class ObservationTextUtil {

	public static String getNameText(IObservation observation) {
		return ModelUtil.getCodeBySystem(observation.getCoding(), CodingSystem.ELEXIS_LOCAL_CODESYSTEM)
				.map(ICoding::getDisplay).orElse(StringUtils.EMPTY);
	}

	public static String getValueText(IObservation observation) {
		switch (observation.getObservationType()) {
		case TEXT:
			return getTextValue(observation);
		case NUMERIC:
			return getNumericValue(observation);
		case BOOLEAN:
			return getBooleanValue(observation);
		case DATE:
			return getDateValue(observation);
		case COMP:
			return getCompValue(observation);
		default:
			return StringUtils.EMPTY;
		}
	}

	private static String getTextValue(IObservation observation) {
		StringBuilder sb = new StringBuilder();
		String textValue = observation.getStringValue().orElse(StringUtils.EMPTY);
		sb.append(textValue);
		String comment = observation.getComment().orElse(StringUtils.EMPTY).trim();
		if (StringUtils.isNotBlank(comment)) {
			sb.append(" (").append(comment).append(")");
		}

		return sb.toString();
	}

	private static String getNumericValue(IObservation observation) {
		StringBuilder sb = new StringBuilder();
		observation.getNumericValue().ifPresent(value -> sb.append(value.toPlainString()));
		observation.getNumericValueUnit().ifPresent(unit -> sb.append(" ").append(unit));
		String comment = observation.getComment().orElse(StringUtils.EMPTY).trim();
		if (StringUtils.isNotBlank(comment)) {
			sb.append(" (").append(comment).append(")");
		}

		return sb.toString();
	}

	private static String getBooleanValue(IObservation observation) {
		StringBuilder sb = new StringBuilder();
		observation.getBooleanValue().ifPresent(value -> sb.append(value ? "Ja" : "Nein"));
		String comment = observation.getComment().orElse(StringUtils.EMPTY).trim();
		if (StringUtils.isNotBlank(comment)) {
			sb.append(" (").append(comment).append(")");
		}

		return sb.toString();
	}


	private static String getDateValue(IObservation observation) {
		StringBuilder sb = new StringBuilder();
		observation.getDateTimeValue().ifPresent(date -> {
			sb.append(new SimpleDateFormat("dd.MM.yyyy").format(date));
		});
		String comment = observation.getComment().orElse(StringUtils.EMPTY).trim();
		if (StringUtils.isNotBlank(comment)) {
			sb.append(" (").append(comment).append(")");
		}

		return sb.toString();
	}

	private static String getCompValue(IObservation observation) {
		StringBuilder sb = new StringBuilder();
		String textSplitter = ", ";
		String dbTextSplitter = observation.getFormat("textSeparator");
		if (StringUtils.isNotEmpty(dbTextSplitter)) {
			textSplitter = dbTextSplitter;
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
		String comment = observation.getComment().orElse(StringUtils.EMPTY).trim();
		if (StringUtils.isNotBlank(comment)) {
			sb.append(" (").append(comment).append(")");
		}

		return sb.toString();
	}

	private static String getComponentText(ObservationComponent component, boolean showUnitInComponent) {
		StringBuilder sb = new StringBuilder();
		component.getNumericValue().ifPresent(value -> sb.append(value.toPlainString()));
		if (showUnitInComponent) {
			component.getNumericValueUnit().ifPresent(unit -> sb.append(" ").append(unit));
		}
		return sb.toString();
	}
}
