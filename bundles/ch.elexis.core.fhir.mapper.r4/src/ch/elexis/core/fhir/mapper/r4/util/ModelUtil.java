package ch.elexis.core.fhir.mapper.r4.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservationLink;
import ch.elexis.core.findings.IObservationLink.ObservationLinkType;
import ch.elexis.core.findings.ObservationComponent;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.services.IModelService;

public class ModelUtil {

	public static <T> Optional<T> loadFinding(IModelService modelService, String id, Class<T> clazz) {
		if (id != null) {
			return modelService.load(id, clazz);
		}
		return Optional.empty();
	}

	public static void setCodingsToConcept(CodeableConcept codeableConcept, List<ICoding> coding) {
		codeableConcept.getCoding().clear();
		for (ICoding iCoding : coding) {
			setCodingToConcept(codeableConcept, iCoding);
		}
	}

	public static void setCodingToConcept(CodeableConcept codeableConcept, ICoding iCoding) {
		codeableConcept.addCoding(new Coding(iCoding.getSystem(), iCoding.getCode(), iCoding.getDisplay()));
	}

	public static List<ICoding> getCodingsFromConcept(CodeableConcept codeableConcept) {
		ArrayList<ICoding> ret = new ArrayList<>();
		List<Coding> coding = codeableConcept.getCoding();
		for (Coding code : coding) {
			ret.add(new CodingWrapper(code));
		}
		return ret;
	}

	public static boolean isCodeInList(String system, String code, List<ICoding> list) {
		if (list != null && !list.isEmpty()) {
			for (ICoding iCoding : list) {
				if (iCoding.getSystem().equals(system) && iCoding.getCode().equals(code)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isSystemInList(String system, List<ICoding> list) {
		if (list != null && !list.isEmpty()) {
			for (ICoding iCoding : list) {
				if (iCoding.getSystem().equals(system)) {
					return true;
				}
			}
		}
		return false;
	}

	public static Optional<ICoding> getCodeBySystem(List<ICoding> coding, CodingSystem codingSystem) {
		for (ICoding iCoding : coding) {
			if (codingSystem.getSystem().equals(iCoding.getSystem())) {
				return Optional.of(iCoding);
			}
		}
		return Optional.empty();
	}

	public static Optional<CodeableConcept> getCodeableConceptBySystem(List<CodeableConcept> concepts,
			CodingSystem codingSystem) {
		for (CodeableConcept concept : concepts) {
			Optional<ICoding> found = getCodeBySystem(getCodingsFromConcept(concept), codingSystem);
			if (found.isPresent()) {
				return Optional.of(concept);
			}
		}
		return Optional.empty();
	}

	public static Optional<CodeableConcept> getCodeableConceptBySystem(List<CodeableConcept> concepts,
			String codingSystem) {
		for (CodeableConcept concept : concepts) {
			Optional<ICoding> found = getCodeBySystem(getCodingsFromConcept(concept), codingSystem);
			if (found.isPresent()) {
				return Optional.of(concept);
			}
		}
		return Optional.empty();
	}

	public static @Nullable String getIdentifierBySystem(List<Identifier> identifiers, String system) {
		int identifierIndexBySystem = getIdentifierIndexBySystem(identifiers, system);
		if (identifierIndexBySystem >= 0) {
			return identifiers.get(identifierIndexBySystem).getValue();
		}
		return null;
	}

	public static int getIdentifierIndexBySystem(List<Identifier> identifiers, String system) {
		for (int i = 0; i < identifiers.size(); i++) {
			Identifier _identifier = identifiers.get(i);
			if (Objects.equals(system, _identifier.getSystem())) {
				return i;
			}
		}
		return -1;
	}

	public static Optional<ICoding> getCodeBySystem(List<ICoding> coding, String codingSystem) {
		for (ICoding iCoding : coding) {
			if (codingSystem.equals(iCoding.getSystem())) {
				return Optional.of(iCoding);
			}
		}
		return Optional.empty();
	}

	public static Optional<String> getNarrativeAsString(Narrative narrative) {
		String text = narrative.getDivAsString();
		if (text != null) {
			String divDecodedText = text.replaceAll("[<](/)?div[^>]*[>]", StringUtils.EMPTY);
			divDecodedText = divDecodedText.replaceAll("<br/>|<br />", StringUtils.LF).replaceAll("&amp;", "&")
					.replaceAll("&gt;", ">").replaceAll("&lt;", "<").replaceAll("'&sect;'", "§");
			return Optional.of(divDecodedText);
		}
		return Optional.empty();
	}

	public static void setNarrativeFromString(Narrative narrative, String text) {
		try {
			text = fixXhtmlContent(text);
			String divEncodedText = text.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("§", "'&sect;'")
					.replaceAll("&", "&amp;").replaceAll("(\r\n|\r|\n)", "<br />");
			divEncodedText = addDivToEncodedText(divEncodedText);
			narrative.setDivAsString(divEncodedText);
			narrative.setStatus(Narrative.NarrativeStatus.GENERATED);
		} catch (Exception e) {
			LoggerFactory.getLogger(ModelUtil.class).error("Could not set narrative text [" + text + "]");
			throw (e);
		}
	}

	private static String addDivToEncodedText(String divEncodedText) {
		if (!divEncodedText.startsWith("<div")) {
			divEncodedText = "<div" + " xmlns=\"" + XhtmlNode.XMLNS + "\"" + ">" + divEncodedText + "</div>";
		}
		return divEncodedText;
	}

	/**
	 * Remove characters which cause problems in xhtml.
	 *
	 * @param content
	 * @return content without problem characters
	 */
	private static String fixXhtmlContent(String content) {
		// replace unicode nbsp with space character
		content = content.replace((char) 0xa0, ' ');
		return content;
	}

	public static boolean isSameCoding(ICoding left, ICoding right) {
		return left.getSystem().equals(right.getSystem()) && left.getCode().equals(right.getCode());
	}

	public static void addCodingIfNotPresent(List<ICoding> coding, ICoding iCoding) {
		// check if this iCoding is already present
		for (ICoding presentCoding : coding) {
			if (isSameCoding(presentCoding, iCoding)) {
				return;
			}
		}
		coding.add(iCoding);
	}

	/**
	 * Checks if all units the same
	 *
	 * @param iObservations
	 * @return
	 */
	public static String getExactUnitOfComponent(List<ObservationComponent> observationComponents) {
		Set<String> units = new HashSet<>();
		for (ObservationComponent child : observationComponents) {
			Optional<String> valueUnit = child.getNumericValueUnit();
			if (valueUnit.isPresent()) {
				units.add(valueUnit.get());
			} else {
				return null;
			}
		}
		return units.size() == 1 ? units.iterator().next() : null;
	}

	/**
	 * Get all the children of the {@link IObservation} reachable via target
	 * {@link IObservationLink} links.
	 *
	 * @param iObservation
	 * @param list
	 * @param maxDepth
	 * @return
	 */
	public static List<IObservation> getObservationChildren(IObservation iObservation, List<IObservation> list,
			int maxDepth) {
		if (maxDepth > 0) {
			List<IObservation> refChildrens = iObservation.getTargetObseravtions(ObservationLinkType.REF);
			list.addAll(refChildrens);
			for (IObservation child : refChildrens) {
				getObservationChildren(child, list, --maxDepth);
			}
		}
		return list;
	}

	public static IObservation getRootObservationRecursive(IObservation observation) {
		IObservation rootObservation = observation;
		List<IObservation> sources = observation.getSourceObservations(ObservationLinkType.REF);
		if (sources != null && !sources.isEmpty()) {
			for (IObservation iObservation : sources) {
				rootObservation = getRootObservationRecursive(iObservation);
			}
		}
		return rootObservation;
	}

}
