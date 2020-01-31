package ch.elexis.core.importer.div.tasks;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.ILabMapping;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.SerializableBoolean;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.ICodeElementService.ContextKeys;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.holder.BillingServiceHolder;
import ch.elexis.core.services.holder.CodeElementServiceHolder;
import ch.elexis.core.services.holder.EncounterServiceHolder;
import ch.elexis.core.services.holder.LabServiceHolder;
import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;

/**
 * @see at.medevit.elexis.roche.labor.billing.AddLabToKons for original implementation
 */
public class BillLabResultOnCreationIdentifiedRunnable implements IIdentifiedRunnable {
	
	/**
	 * @see at.medevit.elexis.roche.labor.preference.RochePreferencePage
	 */
	public class Parameters {
		public static final String ADDCONS_SAMEDAY =
			"at.medevit.elexis.roche.labor.bill.addcons.sameday";
		public static final String ADDCONS = "at.medevit.elexis.roche.labor.bill.addcons";
	}
	
	public static final String RUNNABLE_ID = "billLabResultOnCreation";
	
	private static final int MAX_WAIT = 40;
	
	private static Logger logger =
		LoggerFactory.getLogger(BillLabResultOnCreationIdentifiedRunnable.class);
	
	private static Object addTarifLock = new Object();
	
	private final IModelService coreModelService;
	
	private final EncounterSelector encounterSelector;
	
	private boolean billAddCons = false;
	private boolean billAddConsSameDay = true;
	
	public BillLabResultOnCreationIdentifiedRunnable(IModelService coreModelService,
		EncounterSelector encounterSelection){
		this.coreModelService = coreModelService;
		this.encounterSelector = encounterSelection;
	}
	
	public interface EncounterSelector {
		String createOrOpenConsultation(IPatient patient);
	}
	
	private IBillable getKonsVerrechenbar(IEncounter kons){
		if (kons.getCoverage() != null) {
			TimeTool date = new TimeTool(kons.getDate());
			String law = kons.getCoverage().getBillingSystem().getName();
			Map<Object, Object> context = new HashMap<>();
			context.put(ICodeElementService.ContextKeys.LAW, law);
			context.put(ICodeElementService.ContextKeys.DATE, date.toLocalDate());
			Optional<ICodeElement> tarmed =
				CodeElementServiceHolder.get().loadFromString("Tarmed", "00.0010", context);
			if (tarmed.isPresent()) {
				return (IBillable) tarmed.get();
			}
		}
		return null;
	}
	
	private boolean isLabResultReady(ILabResult labResult){
		// TODO move to model?
		// TODO still required?
		List<Object> values = new ArrayList<>();
		values.add(labResult.getOrigin());
		values.add(labResult.getItem());
		values.add(labResult.getPatient());
		
		for (Object string : values) {
			if (string == null) {
				return false;
			}
		}
		return true;
	}
	
	private Optional<IEncounter> getKonsultation(IPatient patient){
		IEncounter kons = EncounterServiceHolder.get().getLatestEncounter(patient).orElse(null);
		Result<IEncounter> editable = BillingServiceHolder.get().isEditable(kons);
		
		boolean failsEncounterHasToBeTodayConstraint = failsEncounterHasToBeTodayConstraint(kons);
		boolean valIsOnlyOneKonsToday = isOnlyOneKonsToday(patient);
		
		if (kons == null || !editable.isOK() || failsEncounterHasToBeTodayConstraint
			|| !valIsOnlyOneKonsToday) {
			
			if (encounterSelector != null) {
				String konsId = encounterSelector.createOrOpenConsultation(patient);
				if (konsId != null) {
					return coreModelService.load(konsId, IEncounter.class);
				}
			} else {
				logger.warn(
					"encounterSelector==null: kons={}, editable={}, failsEncounterHasToBeTodayConstraint={}, isOnlyOneKonsToday={}",
					kons, editable, failsEncounterHasToBeTodayConstraint, valIsOnlyOneKonsToday);
				if(!editable.isOK()) {
					logger.warn("editable = false, message = "+editable);
				}
			}
			
		}
		return Optional.ofNullable(kons);
	}
	
	private List<IEncounter> getOpenKons(IPatient patient){
		List<ICoverage> coverages = patient.getCoverages();
		if (coverages.isEmpty()) {
			return null;
		}
		
		IQuery<IEncounter> qbe = coreModelService.getQuery(IEncounter.class);
		qbe.startGroup();
		
		boolean termInserted = false;
		for (ICoverage fall : coverages) {
			if (fall.isOpen()) {
				qbe.or(ModelPackage.Literals.IENCOUNTER__COVERAGE, COMPARATOR.EQUALS, fall);
				termInserted = true;
			}
		}
		if (!termInserted) {
			return null;
		}
		//		qbe.endGroup();
		qbe.orderBy(ModelPackage.Literals.IENCOUNTER__DATE, ORDER.DESC);
		return qbe.execute();
	}
	
	private List<IEncounter> getTodaysOpenKons(IPatient patient){
		List<IEncounter> ret = new ArrayList<IEncounter>();
		List<IEncounter> list = getOpenKons(patient);
		
		for (IEncounter konsultation : list) {
			TimeTool konsDate = new TimeTool(konsultation.getDate());
			if (konsDate.isSameDay(new TimeTool())) {
				ret.add(konsultation);
			}
		}
		return ret;
	}
	
	private boolean isOnlyOneKonsToday(IPatient patient){
		// if today kons option is not set do not lookup ...
		if (!billAddConsSameDay) {
			// TODO task? who is the active mandator?
			return true;
		}
		// relevant is not the kons but the fall
		// 2 kons of the same fall are ok
		HashSet<String> set = new HashSet<String>();
		List<IEncounter> list = getTodaysOpenKons(patient);
		for (IEncounter konsultation : list) {
			ICoverage fall = konsultation.getCoverage();
			set.add(fall.getId());
		}
		if (set.size() == 1) {
			return true;
		}
		return false;
	}
	
	private boolean failsEncounterHasToBeTodayConstraint(IEncounter kons){
		// see RochePreferencePage.LABORRESULTS_BILL_ADDCONS_SAMEDAY
		if (billAddConsSameDay) {
			// constraint is active
			TimeTool konsDate = new TimeTool(kons.getDate());
			if (!konsDate.isSameDay(new TimeTool())) {
				return true;
			}
		}
		return false;
	}
	
	private Result<?> addTarifToKons(IBillable tarif, IEncounter kons) throws TaskException{
		// see RochePreferencePage.LABORRESULTS_BILL_ADDCONS
		if (billAddCons) {
			synchronized (kons) {
				List<IBilled> leistungen = kons.getBilled();
				boolean addCons = true;
				for (IBilled verrechnet : leistungen) {
					IBillable verrechenbar = verrechnet.getBillable();
					if (verrechenbar != null && verrechenbar.getCodeSystemName().equals("Tarmed")
						&& verrechenbar.getCode().equals("00.0010")) {
						addCons = false;
						break;
					}
				}
				if (addCons) {
					IBillable consVerrechenbar = getKonsVerrechenbar(kons);
					if (consVerrechenbar != null) {
						Result<?> result =
							BillingServiceHolder.get().bill(consVerrechenbar, kons, 1);
						if (!result.isOK()) {
							throw new TaskException(TaskException.EXECUTION_ERROR, result);
						}
					}
				}
			}
		}
		
		logger
			.info(String.format("Adding EAL tarif [%s] to [%s]", tarif.getCode(), kons.getLabel()));
		Result<?> result = BillingServiceHolder.get().bill(tarif, kons, 1);
		if (!result.isOK()) {
			throw new TaskException(TaskException.EXECUTION_ERROR, result);
		}
		return result;
	}
	
	private IBillable getLabor2009TarifByCode(String ealCode){
		Map<Object, Object> context = CodeElementServiceHolder.createContext();
		context.put(ContextKeys.DATE, LocalDate.now());
		Optional<ICodeElement> tarif =
			CodeElementServiceHolder.get().loadFromString("EAL 2009", ealCode, context);
		if (tarif.isPresent() && tarif.get() instanceof IBillable) {
			return (IBillable) tarif.get();
		}
		return null;
	}
	
	@Override
	public String getId(){
		return RUNNABLE_ID;
	}
	
	@Override
	public String getLocalizedDescription(){
		return "Bill an EAL 2009 service on a patients encounter on creation of a Labresult";
	}
	
	@Override
	public Map<String, Serializable> getDefaultRunContext(){
		Map<String, Serializable> defaultRunContext = new HashMap<>();
		defaultRunContext.put(RunContextParameter.IDENTIFIABLE_ID,
			RunContextParameter.VALUE_MISSING_REQUIRED);
		defaultRunContext.put(Parameters.ADDCONS, Boolean.FALSE);
		defaultRunContext.put(Parameters.ADDCONS_SAMEDAY, Boolean.TRUE);
		return defaultRunContext;
	}
	
	@Override
	public Map<String, Serializable> run(Map<String, Serializable> runContext,
		IProgressMonitor progressMonitor, Logger logger) throws TaskException{
		
		String labresultId = (String) runContext.get(RunContextParameter.IDENTIFIABLE_ID);
		Optional<ILabResult> _labResult = coreModelService.load(labresultId, ILabResult.class);
		if (!_labResult.isPresent()) {
			throw new TaskException(TaskException.EXECUTION_ERROR,
				"LabResult [" + labresultId + "] could not be loaded");
		}
		
		billAddCons = SerializableBoolean.valueOf(runContext, Parameters.ADDCONS);
		billAddConsSameDay = SerializableBoolean.valueOf(runContext, Parameters.ADDCONS_SAMEDAY);
		
		ILabResult labResult = _labResult.get();
		
		// we have to wait for the fields to be set, as this gets called on creation,
		// and the fields
		// are set afterwards
		if (!isLabResultReady(labResult)) {
			int waitForFields = 0;
			while (waitForFields < MAX_WAIT) {
				try {
					waitForFields++;
					Thread.sleep(500);
					if (isLabResultReady(labResult)) {
						break;
					}
				} catch (InterruptedException e) {
					// ignore
				}
			}
			if (waitForFields == MAX_WAIT) {
				String errorMessage =
					String.format("Could not get data from result [%s].", labResult.getId());
				logger.warn(errorMessage);
				throw new TaskException(TaskException.EXECUTION_ERROR, errorMessage);
			}
		}
		
		// all properties are available
		Optional<ILabMapping> mapping = LabServiceHolder.get()
			.getLabMappingByContactAndItem(labResult.getOrigin(), labResult.getItem());
		if (mapping.isPresent() && mapping.get().isCharge()) {
			String ealCode = labResult.getItem().getBillingCode();
			logger.info(String.format("Adding EAL tarif [%s] from [%s]", ealCode,
				labResult.getOrigin().getLabel()));
			if (ealCode != null && !ealCode.isEmpty()) {
				IBillable tarif = getLabor2009TarifByCode(ealCode);
				if (tarif != null) {
					synchronized (addTarifLock) {
						Optional<IEncounter> kons = getKonsultation(labResult.getPatient());
						if (kons.isPresent()
							&& EncounterServiceHolder.get().isEditable(kons.get())) {
							Result<?> addTarifToKons = addTarifToKons(tarif, kons.get());
							return Collections.singletonMap(ReturnParameter.RESULT_DATA,
								addTarifToKons.toString());
						} else {
							String errorString = String.format(
								"Could not add tarif [%s] for result of patient [%s] because no valid kons found.",
								ealCode, labResult.getPatient().getLabel());
							logger.warn(errorString);
							throw new TaskException(TaskException.EXECUTION_ERROR, errorString);
						}
					}
				} else {
					String errorString = String.format("Item %s: EAL tarif [%s] does not exist.",
						labResult.getItem().getLabel(), ealCode);
					logger.warn(errorString);
					throw new TaskException(TaskException.EXECUTION_ERROR, errorString);
				}
			}
		}
		// no mapping found or not to charge
		return Collections.singletonMap(ReturnParameter.MARKER_DO_NOT_PERSIST, true);
	}
}
