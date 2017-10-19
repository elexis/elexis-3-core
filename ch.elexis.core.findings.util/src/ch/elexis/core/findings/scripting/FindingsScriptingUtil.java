package ch.elexis.core.findings.scripting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.LoggerFactory;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationType;
import ch.elexis.core.findings.IObservationLink.ObservationLinkType;
import ch.elexis.core.findings.ObservationComponent;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.findings.util.ScriptingServiceHolder;
import ch.elexis.core.services.IScriptingService;

public class FindingsScriptingUtil {
	
	public static boolean hasScript(IObservation iObservation){
		return iObservation.getScript().isPresent();
	}
	
	/**
	 * Evaluate the script provided by the {@link IObservation} unsing a {@link IScriptingService}.
	 * The result is set via one to the set[x]Value methods of the {@link IObservation}.
	 * 
	 * @param iObservation
	 */
	public static void evaluate(IObservation iObservation){
		Optional<String> script = iObservation.getScript();
		
		if (script.isPresent()) {
			String preparedScript = prepareScript(script.get(), iObservation);
			try {
				Object result = ScriptingServiceHolder.getService().execute(preparedScript);
				if (result instanceof String) {
					if (iObservation.getObservationType() == ObservationType.TEXT) {
						iObservation.setStringValue((String) result);
					} else if (iObservation.getObservationType() == ObservationType.NUMERIC) {
						if (NumberUtils.isNumber((String) result)) {
							BigDecimal newValue = new BigDecimal((String) result);
							iObservation.setNumericValue(
								applyDecimal(newValue, iObservation.getDecimalPlace()),
								iObservation.getNumericValueUnit().orElse(""));
						} else {
							LoggerFactory.getLogger(FindingsScriptingUtil.class)
								.debug("Could not set not numeric result [" + result + "]");
						}
					}
				} else if (result instanceof Double && !((Double) result).isNaN()) {
					if (iObservation.getObservationType() == ObservationType.NUMERIC) {
						BigDecimal newValue = new BigDecimal((Double) result);
						iObservation.setNumericValue(
							applyDecimal(newValue, iObservation.getDecimalPlace()),
							iObservation.getNumericValueUnit().orElse(""));
					} else if (iObservation.getObservationType() == ObservationType.TEXT) {
						iObservation
							.setStringValue(new BigDecimal((Double) result).toPlainString());
					}
				} else if (result instanceof Integer) {
					if (iObservation.getObservationType() == ObservationType.NUMERIC) {
						BigDecimal newValue = new BigDecimal((Integer) result);
						iObservation.setNumericValue(
							applyDecimal(newValue, iObservation.getDecimalPlace()),
							iObservation.getNumericValueUnit().orElse(""));
					} else if (iObservation.getObservationType() == ObservationType.TEXT) {
						iObservation
							.setStringValue(new BigDecimal((Integer) result).toPlainString());
					}
				} else {
					LoggerFactory.getLogger(FindingsScriptingUtil.class)
						.debug("Error cant handle script result [" + result + "]");
				}
			} catch (ElexisException e) {
				LoggerFactory.getLogger(FindingsScriptingUtil.class)
					.debug("Error executing script [" + script.get() + "] " + e.getMessage());
			}
		}
	}
	
	private static BigDecimal applyDecimal(BigDecimal newValue, int decimalPlace){
		if (decimalPlace != -1) {
			return newValue.setScale(decimalPlace, RoundingMode.HALF_UP);
		}
		return newValue;
	}
	
	private static String prepareScript(String script, IObservation iObservation){
		StringBuffer preparedScript = new StringBuffer();
		
		Pattern pattern = Pattern.compile("\\[.*?\\]");
		Matcher matcher = pattern.matcher(script);
		
		// search for replacements we can satisfy with other finding values
		while (matcher.find()) {
			String var = matcher.group().replaceAll("[\\[\\]]", "");
			
			String value = getVarValue(var, iObservation);
			if (value != null && !value.isEmpty()) {
				matcher.appendReplacement(preparedScript, value);
			} else {
				matcher.appendReplacement(preparedScript, matcher.group());
			}
		}
		matcher.appendTail(preparedScript);
		return preparedScript.toString();
	}
	
	private static String getVarValue(String var, IObservation iObservation){
		// first get to the top observation
		List<IObservation> parents = iObservation.getSourceObservations(ObservationLinkType.REF);
		for (IObservation parentObservation : parents) {
			// try to resolve the var in parent group
			if (parentObservation.getObservationType() == ObservationType.REF) {
				String value = getVarValue(var, parentObservation);
				if (value != null && !value.isEmpty()) {
					return value;
				}
			}
		}
		// now lookup in children
		List<IObservation> children = iObservation.getTargetObseravtions(ObservationLinkType.REF);
		for (IObservation childObservation : children) {
			Optional<String> code = getLocalCode(childObservation);
			if (code.isPresent() && var.equals(code.get())) {
				if (childObservation.getObservationType() == ObservationType.NUMERIC) {
					String numericValue = getNumericValue(childObservation);
					if (numericValue != null) {
						return numericValue;
					}
				} else if (childObservation.getObservationType() == ObservationType.TEXT) {
					String stringValue = getStringValue(childObservation);
					if (stringValue != null) {
						return stringValue;
					}
				}
			} else if (childObservation.getObservationType() == ObservationType.COMP) {
				List<ObservationComponent> components = childObservation.getComponents();
				for (ObservationComponent observationComponent : components) {
					code = getLocalCode(observationComponent);
					if (code.isPresent() && var.equals(code.get())) {
						if (observationComponent.getTypeFromExtension(
							ObservationType.class) == ObservationType.NUMERIC) {
							String numericValue = getNumericValue(childObservation);
							if (numericValue != null) {
								return numericValue;
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	private static String getStringValue(IObservation childObservation){
		return childObservation.getStringValue().orElse(null);
	}
	
	private static String getNumericValue(IObservation childObservation){
		Optional<BigDecimal> numericValue = childObservation.getNumericValue();
		if (numericValue.isPresent()) {
			return numericValue.get().toPlainString() + "d";
		}
		return null;
	}
	
	private static Optional<String> getLocalCode(IObservation iObservation){
		Optional<ICoding> coding = ModelUtil.getCodeBySystem(iObservation.getCoding(),
			CodingSystem.ELEXIS_LOCAL_CODESYSTEM);
		if (coding.isPresent()) {
			return Optional.of(coding.get().getCode());
		}
		return Optional.empty();
	}
	
	private static Optional<String> getLocalCode(ObservationComponent component){
		Optional<ICoding> coding =
			ModelUtil.getCodeBySystem(component.getCoding(), CodingSystem.ELEXIS_LOCAL_CODESYSTEM);
		if (coding.isPresent()) {
			return Optional.of(coding.get().getCode());
		}
		return Optional.empty();
	}
}
