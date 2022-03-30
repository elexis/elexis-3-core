package ch.elexis.core.findings.util.fhir.transformer.helper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Observation.ObservationReferenceRangeComponent;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Quantity.QuantityComparator;
import org.hl7.fhir.r4.model.SimpleQuantity;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Type;

import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.types.Gender;
import ch.elexis.core.types.LabItemTyp;

public class ILabResultHelper extends AbstractHelper {

	public Type getEffectiveDateTime(ILabResult localObject) {
		return new DateTimeType(getDate(localObject.getObservationTime()));
	}

	public Type getResult(ILabResult localObject) {
		if (localObject.getItem().getTyp() == LabItemTyp.NUMERIC) {
			String result = localObject.getResult();

			Optional<Double> numericResult = getNumericValue(result);
			if (numericResult.isPresent()) {
				Quantity qty = new Quantity();
				qty.setValue(numericResult.get());
				qty.setUnit(Optional.ofNullable(localObject.getUnit()).orElse(""));
				getComparator(result).ifPresent(comp -> qty.setComparator(comp));
				return qty;
			} else {
				return new StringType(result + " " + (localObject.getUnit() != null ? localObject.getUnit() : ""));
			}
		} else {
			if (localObject.getItem().getTyp() == LabItemTyp.TEXT) {
				if (isLongText(localObject)) {
					String resultComment = localObject.getComment();
					return new StringType(resultComment);
				} else {
					return new StringType(localObject.getResult());
				}
			}
		}
		return new StringType("");
	}

	public boolean isLongText(ILabResult localObject) {
		String resultValue = localObject.getResult().trim().replaceAll("[()]", "");
		String resultComment = localObject.getComment();
		if (resultValue != null && localObject.getItem().getTyp() == LabItemTyp.TEXT
				&& resultValue.equalsIgnoreCase("text") && resultComment != null && !resultComment.isEmpty()) {
			return true;
		}
		return false;
	}

	private static Optional<QuantityComparator> getComparator(String result) {
		if (result.startsWith("<=")) {
			return Optional.of(QuantityComparator.LESS_OR_EQUAL);
		} else if (result.startsWith("<")) {
			return Optional.of(QuantityComparator.LESS_THAN);
		} else if (result.startsWith(">=")) {
			return Optional.of(QuantityComparator.GREATER_OR_EQUAL);
		} else if (result.startsWith(">")) {
			return Optional.of(QuantityComparator.GREATER_THAN);
		}
		return Optional.empty();
	}

	private static Optional<Double> getNumericValue(String result) {
		Double ret = null;
		result = result.replaceAll("[\\*!,<>=]", "").replaceAll(" ", "");
		try {
			ret = Double.parseDouble(result);
		} catch (NumberFormatException nfe) {
			// ignore not really numeric ...
		}
		return Optional.ofNullable(ret);
	}

	public List<ObservationReferenceRangeComponent> getReferenceComponents(ILabResult localObject) {
		String localRef = null;
		if (localObject.getPatient().getGender() == Gender.FEMALE) {
			localRef = localObject.getReferenceFemale();
			if (localRef == null) {
				localRef = localObject.getItem().getReferenceFemale();
			}
		} else {
			localRef = localObject.getReferenceMale();
			if (localRef == null) {
				localRef = localObject.getItem().getReferenceMale();
			}
		}
		if (localRef != null) {
			ObservationReferenceRangeComponent comp = new ObservationReferenceRangeComponent();
			Optional<Quantity> lowRef = getReferenceLow(localRef);
			Optional<Quantity> highRef = getReferenceHigh(localRef);
			if (lowRef.isPresent() || highRef.isPresent()) {
				lowRef.ifPresent(value -> comp.setLow(value));
				highRef.ifPresent(value -> comp.setHigh(value));
			} else {
				comp.setText(localRef);
			}
			return Collections.singletonList(comp);
		}
		return Collections.emptyList();
	}

	private Optional<Quantity> getReferenceLow(String localRef) {
		String[] range = localRef.split("\\s*-\\s*"); //$NON-NLS-1$
		if (range.length == 2) {
			try {
				double lower = Double.parseDouble(range[0]);
				return Optional.of(new SimpleQuantity().setValue(lower));
			} catch (NumberFormatException nfe) {
				// ignore
			}
		}
		return Optional.empty();
	}

	private Optional<Quantity> getReferenceHigh(String localRef) {
		String[] range = localRef.split("\\s*-\\s*"); //$NON-NLS-1$
		if (range.length == 2) {
			try {
				double high = Double.parseDouble(range[1]);
				return Optional.of(new SimpleQuantity().setValue(high));
			} catch (NumberFormatException nfe) {
				// ignore
			}
		}
		return Optional.empty();
	}

	public CodeableConcept getCodeableConcept(ILabResult localObject) {
		CodeableConcept ret = new CodeableConcept();
		ILabItem item = localObject.getItem();
		ret.addCoding(new Coding(CodingSystem.ELEXIS_LOCAL_LABORATORY.getSystem(), item.getCode(), item.getName()));
		ret.addCoding(new Coding(CodingSystem.ELEXIS_LOCAL_LABORATORY_GROUP.getSystem(),
				item.getGroup() + "/" + item.getPriority(), item.getGroup()));
		return ret;
	}

	public List<Annotation> getNote(ILabResult localObject) {
		if (!isLongText(localObject)) {
			Annotation annotation = new Annotation();
			annotation.setText(localObject.getComment());
			return Collections.singletonList(annotation);
		}
		return Collections.emptyList();
	}
}
