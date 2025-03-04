package ch.elexis.core.services;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bsh.EvalError;
import bsh.Interpreter;
import ch.elexis.core.constants.TextContainerConstants;
import ch.elexis.core.jpa.entities.LabOrder;
import ch.elexis.core.jpa.entities.LabResult;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabMapping;
import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.types.LabItemTyp;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

@Component
public class LabService implements ILabService {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	private Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public Result<String> evaluate(ILabResult labResult) {
		if (LabItemTyp.FORMULA != labResult.getItem().getTyp()) {
			return Result.OK(labResult.getResult());
		}

		ILabItem item = labResult.getItem();
		if (item == null) {
			return Result.ERROR("Missing LabItem");
		}

		String evaluationResult = null;

		IQuery<ILabOrder> query = modelService.getQuery(ILabOrder.class);
		query.and(ModelPackage.Literals.ILAB_ORDER__RESULT, COMPARATOR.EQUALS, labResult);
		Optional<ILabOrder> labOrder = query.executeSingleResult();
		if (labOrder.isPresent()) {
			List<ILabResult> labresults = findAllLabResultsForLabOrderIdGroup(labOrder.get());
			evaluationResult = evaluate(labOrder.get().getItem(), labResult.getPatient(), labresults);
		}
		if (evaluationResult == null || evaluationResult.equals("?formel?")) { //$NON-NLS-1$
			evaluationResult = evaluate(labResult.getItem(), labResult.getPatient(),
					new TimeTool(labResult.getObservationTime()));
		}

		if (evaluationResult == null || StringUtils.EMPTY.equals(evaluationResult)
				|| "?formel?".equals(evaluationResult)) { //$NON-NLS-1$
			return Result.ERROR(evaluationResult);
		}

		return Result.OK(evaluationResult);
	}

	/**
	 * Find all {@link LabResult} entries for a given {@link LabOrder} id group.
	 * Excludes {@link LabResult} marked as deleted.
	 *
	 * @param labOrder
	 * @return
	 */
	private List<ILabResult> findAllLabResultsForLabOrderIdGroup(ILabOrder labOrder) {
		List<ILabOrder> ordersWithResult = getLabOrdersInSameOrderIdGroup(labOrder, true);
		return ordersWithResult.stream().map(owr -> owr.getResult()).filter(result -> !result.isDeleted())
				.collect(Collectors.toList());
	}

	private String evaluate(ILabItem labItem, IPatient patient, TimeTool date) {
		IQuery<ILabResult> qbe = modelService.getQuery(ILabResult.class);
		qbe.and(ModelPackage.Literals.ILAB_RESULT__PATIENT, COMPARATOR.EQUALS, patient);
		qbe.and(ModelPackage.Literals.ILAB_RESULT__DATE, COMPARATOR.EQUALS, date.toLocalDate());
		List<ILabResult> results = qbe.execute();
		return evaluate(labItem, patient, results);
	}

	@Override
	public List<ILabOrder> getLabOrdersInSameOrderIdGroup(ILabOrder labOrder, boolean nonEmptyResultsOnly) {
		IQuery<ILabOrder> query = modelService.getQuery(ILabOrder.class);
		query.and(ModelPackage.Literals.ILAB_ORDER__PATIENT, COMPARATOR.EQUALS, labOrder.getPatient());
		query.and(ModelPackage.Literals.ILAB_ORDER__ORDER_ID, COMPARATOR.EQUALS, labOrder.getOrderId());
		if (nonEmptyResultsOnly) {
			query.and(ModelPackage.Literals.ILAB_ORDER__RESULT, COMPARATOR.NOT_EQUALS, null);
		}
		return query.execute();
	}

	private static final Pattern varPattern = Pattern.compile(TextContainerConstants.MATCH_TEMPLATE);

	private String evaluate(ILabItem labItem, IPatient patient, List<ILabResult> labresults) {
		String formula = labItem.getFormula();
		log.trace("Evaluating formula [" + formula + "]");
		if (formula.startsWith("SCRIPT:")) {
			log.warn("Script elements currently not supported, returning empty String. LabItem [" + labItem.getId()
					+ "]");
			return StringUtils.EMPTY;
		}
		boolean bMatched = false;
		labresults = sortResultsDescending(labresults);
		for (ILabResult result : labresults) {
			String var = result.getItem().getVariableName();
			if (formula.indexOf(var) != -1) {
				if (result.getResult() != null && !result.getResult().isEmpty() && !result.getResult().equals("?")) { //$NON-NLS-1$
					formula = formula.replaceAll(var, result.getResult());
					bMatched = true;
				}
			}
		}

		// Suche Variablen der Form [Patient.Alter]
		Matcher matcher = varPattern.matcher(formula);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String var = matcher.group();
			String[] fields = var.split("\\."); //$NON-NLS-1$
			if (fields.length > 1) {
				String val = getPersistentObjectAttributeMapping(patient,
						fields[1].replaceFirst("\\]", StringTool.leer));
				String repl = "\"" + val + "\"";
				matcher.appendReplacement(sb, repl);
				bMatched = true;
			}
		}
		matcher.appendTail(sb);
		if (!bMatched) {
			return null;
		}

		try {
			Interpreter bshInterpreter = new bsh.Interpreter();
			return bshInterpreter.eval(sb.toString()).toString();
		} catch (Exception e) {
			if (e instanceof EvalError) {
				log.info("Error evaluating formula [{}], returning ?formel?.", sb.toString());
			} else {
				log.warn("Error evaluating formula [{}], returning ?formel?.", sb.toString(), e);
			}
			return "?formel?";
		}
	}

	private List<ILabResult> sortResultsDescending(List<ILabResult> results) {
		Collections.sort(results, new Comparator<ILabResult>() {
			@Override
			public int compare(ILabResult lr1, ILabResult lr2) {
				int var1Length = lr1.getItem().getVariableName().length();
				int var2Length = lr2.getItem().getVariableName().length();

				if (var1Length < var2Length) {
					return 1;
				} else if (var1Length > var2Length) {
					return -1;
				}
				return 0;
			}
		});
		return results;
	}

	private String getPersistentObjectAttributeMapping(IPatient patient, String value) {
		value = value.toLowerCase();
		switch (value) {
		case "geschlecht":
			return patient.getGender().name();
		case "alter":
			return Integer.toString(patient.getAgeInYears());
		default:
			break;
		}

		log.warn("Could not map attribute Patient@[" + value + "], returning empty string.");
		return StringTool.leer;
	}

	@Override
	public Optional<ILabMapping> getLabMappingByContactAndItem(IContact contact, ILabItem item) {
		IQuery<ILabMapping> qbe = modelService.getQuery(ILabMapping.class);
		qbe.and(ModelPackage.Literals.ILAB_MAPPING__ORIGIN, COMPARATOR.EQUALS, contact);
		qbe.and(ModelPackage.Literals.ILAB_MAPPING__ITEM, COMPARATOR.EQUALS, item);
		return qbe.executeSingleResult();
	}

	@Override
	public List<ILabResult> getLabResultsForPatientWithItemType(IPatient patient, LabItemTyp type,
			boolean includeDeleted) {

		INamedQuery<ILabResult> query = modelService.getNamedQuery(ILabResult.class, "patient", "itemtype",
				"includesDeleted");
		Map<String, Object> parameterMap = query.getParameterMap("patient", patient, "itemtype", type);
		List<ILabResult> results = query.executeWithParameters(parameterMap);

		if (!includeDeleted) {
			return results.stream().filter(lr -> !lr.isDeleted()).collect(Collectors.toList());
		}

		return results;
	}

}
