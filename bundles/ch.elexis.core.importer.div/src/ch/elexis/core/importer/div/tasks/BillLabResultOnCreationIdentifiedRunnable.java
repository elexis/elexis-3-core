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
import org.eclipse.core.runtime.IStatus;
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
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.model.message.MessageCode;
import ch.elexis.core.model.message.MessageCodeMessageId;
import ch.elexis.core.model.message.TransientMessage;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.SerializableBoolean;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.ICodeElementService.ContextKeys;
import ch.elexis.core.services.IMessageService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.holder.BillingServiceHolder;
import ch.elexis.core.services.holder.CodeElementServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.EncounterServiceHolder;
import ch.elexis.core.services.holder.LabServiceHolder;
import ch.elexis.core.services.holder.MessageServiceHolder;
import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;

/**
 * @see at.medevit.elexis.roche.labor.billing.AddLabToKons for original
 *      implementation
 */
public class BillLabResultOnCreationIdentifiedRunnable implements IIdentifiedRunnable {

	/**
	 * @see at.medevit.elexis.roche.labor.preference.RochePreferencePage
	 */
	public class Parameters {
		public static final String ADDCONS_SAMEDAY = "at.medevit.elexis.roche.labor.bill.addcons.sameday";
		public static final String ADDCONS = "at.medevit.elexis.roche.labor.bill.addcons";
		/**
		 * Automatically add a billable encounter if the latest encounter is not
		 * billable. The billing law of the latest encounter is copied. If there is no
		 * previous encounter, the task will fail. Default <code>true</code>
		 */
		public static final String BOOLEAN_AUTO_ADD_BILLABLE_ENCOUNTER = "autoCreateBillalbeEncounter";
		/**
		 * Send a notification that an encounter was created. Default <code>true</code>
		 */
		public static final String BOOLEAN_NOTIFY_ON_AUTO_ADD_ENCOUNTER = "notifyOnAutoAddEncounter";
	}

	public static final String RUNNABLE_ID = "billLabResultOnCreation";

	private static final int MAX_WAIT = 40;

	private static Object addTarifLock = new Object();
	private static Logger logger;

	private final IModelService coreModelService;
	private final EncounterSelector encounterSelector;

	public BillLabResultOnCreationIdentifiedRunnable(IModelService coreModelService,
			EncounterSelector encounterSelection) {
		this.coreModelService = coreModelService;
		this.encounterSelector = encounterSelection;

		logger = LoggerFactory.getLogger(BillLabResultOnCreationIdentifiedRunnable.class);
	}

	public interface EncounterSelector {
		String createOrOpenConsultation(IPatient patient);
	}

	private IBillable getKonsVerrechenbar(IEncounter kons) {
		if (kons.getCoverage() != null) {
			TimeTool date = new TimeTool(kons.getDate());
			String law = kons.getCoverage().getBillingSystem().getName();
			Map<Object, Object> context = new HashMap<>();
			context.put(ICodeElementService.ContextKeys.LAW, law);
			context.put(ICodeElementService.ContextKeys.DATE, date.toLocalDate());
			Optional<ICodeElement> tarmed = CodeElementServiceHolder.get().loadFromString("Tarmed", "00.0010", context);
			if (tarmed.isPresent()) {
				return (IBillable) tarmed.get();
			}
		}
		return null;
	}

	private boolean isLabResultReady(ILabResult labResult) {
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

	/**
	 * Retrieve a consultation that meets the requirements to be billed upon
	 * 
	 * @param patient
	 * @param autoAddBillableEncounter see
	 *                                 {@link Parameters#BOOLEAN_AUTO_ADD_BILLABLE_ENCOUNTER}
	 * @param notifyOnAutoAddEncounter see
	 *                                 {@link Parameters#BOOLEAN_NOTIFY_ON_AUTO_ADD_ENCOUNTER}
	 * @return
	 */
	private Optional<IEncounter> getBillableEncounter(IPatient patient, boolean onlyUseTodaysEncounterForBilling,
			boolean autoAddBillableEncounter, boolean notifyOnAutoAddEncounter) {
		IEncounter validEncounter = null;

		IEncounter kons = EncounterServiceHolder.get().getLatestEncounter(patient).orElse(null);
		if (kons != null) {
			Result<IEncounter> editable = BillingServiceHolder.get().isEditable(kons);
			boolean failsEncounterHasToBeTodayConstraint = true;
			boolean valIsOnlyOneKonsToday = false;
			if (editable.isOK()) {

				failsEncounterHasToBeTodayConstraint = failsEncounterHasToBeTodayConstraint(kons,
						onlyUseTodaysEncounterForBilling);
				valIsOnlyOneKonsToday = isOnlyOneKonsToday(patient, onlyUseTodaysEncounterForBilling);
				if (!failsEncounterHasToBeTodayConstraint && valIsOnlyOneKonsToday) {
					validEncounter = kons;
				}
			} else {

				if (autoAddBillableEncounter) {
					if (kons.getCoverage().isOpen()) {
						validEncounter = new IEncounterBuilder(coreModelService, kons.getCoverage(), kons.getMandator())
								.buildAndSave();
						logger.info("Added encounter [{}] to bill results", validEncounter.getId());
						if (notifyOnAutoAddEncounter) {
							IStatus result = sendMessageToOwner(patient);
							if (!result.isOK()) {
								logger.warn("Could not send notification message.");
							}
						}
					}
				}

			}
			logger.debug(
					"editable={},failsEncounterHasToBeTodayConstraint={},valIsOnlyOneKonsToday={},autoAddBillableEncounter={}",
					editable, failsEncounterHasToBeTodayConstraint, valIsOnlyOneKonsToday, autoAddBillableEncounter);
		} 

		if (validEncounter == null && encounterSelector != null) {
			String konsId = encounterSelector.createOrOpenConsultation(patient);
			if (konsId != null) {
				validEncounter = coreModelService.load(konsId, IEncounter.class).get();
			}
		}

		return Optional.ofNullable(validEncounter);
	}

	private IStatus sendMessageToOwner(IPatient patient) {
		IUser owner = ContextServiceHolder.get().getActiveUser().orElse(null);
		TransientMessage transientMessage = MessageServiceHolder.get().prepare(getClass().getSimpleName(),
				IMessageService.INTERNAL_MESSAGE_URI_SCHEME + ":" + owner.getId());
		transientMessage.addMessageCode(MessageCode.Key.MessageId,
				MessageCodeMessageId.INFO_BILLING_AUTO_CREATE_ENCOUNTER.name());
		transientMessage.addMessageCode(MessageCode.Key.MessageIdParam, patient.getPatientNr());
		transientMessage.setSenderAcceptsAnswer(false);
		transientMessage.setMessageText("No billable encounter for patient " + patient.getPatientNr()
				+ " found. A new encounter was created to bill the imported values on.");
		return MessageServiceHolder.get().send(transientMessage);
	}

	private List<IEncounter> getOpenKons(IPatient patient) {
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
		// qbe.endGroup();
		qbe.orderBy(ModelPackage.Literals.IENCOUNTER__DATE, ORDER.DESC);
		return qbe.execute();
	}

	private List<IEncounter> getTodaysOpenKons(IPatient patient) {
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

	private boolean isOnlyOneKonsToday(IPatient patient, boolean onlyUseTodaysEncounterForBilling) {
		// if today kons option is not set do not lookup ...
		if (!onlyUseTodaysEncounterForBilling) {
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

	private boolean failsEncounterHasToBeTodayConstraint(IEncounter kons, boolean onlyUseTodaysEncounterForBilling) {
		// see RochePreferencePage.LABORRESULTS_BILL_ADDCONS_SAMEDAY
		if (onlyUseTodaysEncounterForBilling) {
			// constraint is active
			TimeTool konsDate = new TimeTool(kons.getDate());
			if (!konsDate.isSameDay(new TimeTool())) {
				return true;
			}
		}
		return false;
	}

	private Result<?> addTarifToKons(IBillable tarif, IEncounter kons,
			boolean autoBillTarmed00_0010IfNotAlreadyBilledOnEncounter) throws TaskException {
		// see RochePreferencePage.LABORRESULTS_BILL_ADDCONS
		if (autoBillTarmed00_0010IfNotAlreadyBilledOnEncounter) {
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
						Result<?> result = BillingServiceHolder.get().bill(consVerrechenbar, kons, 1);
						if (!result.isOK()) {
							throw new TaskException(TaskException.EXECUTION_ERROR, result);
						}
					}
				}
			}
		}

		logger.info(String.format("Adding EAL tarif [%s] to [%s]", tarif.getCode(), kons.getLabel()));
		Result<?> result = BillingServiceHolder.get().bill(tarif, kons, 1);
		if (!result.isOK()) {
			throw new TaskException(TaskException.EXECUTION_ERROR, result);
		}
		return result;
	}

	private IBillable getLabor2009TarifByCode(String ealCode) {
		Map<Object, Object> context = CodeElementServiceHolder.createContext();
		context.put(ContextKeys.DATE, LocalDate.now());
		Optional<ICodeElement> tarif = CodeElementServiceHolder.get().loadFromString("EAL 2009", ealCode, context);
		if (tarif.isPresent() && tarif.get() instanceof IBillable) {
			return (IBillable) tarif.get();
		}
		return null;
	}

	@Override
	public String getId() {
		return RUNNABLE_ID;
	}

	@Override
	public String getLocalizedDescription() {
		return "Bill an EAL 2009 service on a patients encounter on creation of a Labresult";
	}

	@Override
	public Map<String, Serializable> getDefaultRunContext() {
		Map<String, Serializable> defaultRunContext = new HashMap<>();
		defaultRunContext.put(RunContextParameter.IDENTIFIABLE_ID, RunContextParameter.VALUE_MISSING_REQUIRED);
		defaultRunContext.put(Parameters.ADDCONS, Boolean.FALSE);
		defaultRunContext.put(Parameters.ADDCONS_SAMEDAY, Boolean.TRUE);
		defaultRunContext.put(Parameters.BOOLEAN_AUTO_ADD_BILLABLE_ENCOUNTER, Boolean.TRUE);
		defaultRunContext.put(Parameters.BOOLEAN_NOTIFY_ON_AUTO_ADD_ENCOUNTER, Boolean.TRUE);
		return defaultRunContext;
	}

	@Override
	public Map<String, Serializable> run(Map<String, Serializable> runContext, IProgressMonitor progressMonitor,
			Logger logger) throws TaskException {

		String labresultId = (String) runContext.get(RunContextParameter.IDENTIFIABLE_ID);
		Optional<ILabResult> _labResult = coreModelService.load(labresultId, ILabResult.class);
		if (!_labResult.isPresent()) {
			throw new TaskException(TaskException.EXECUTION_ERROR,
					"LabResult [" + labresultId + "] could not be loaded");
		}

		boolean autoBillTarmed00_0010IfNotAlreadyBilledOnEncounter = SerializableBoolean.valueOf(runContext,
				Parameters.ADDCONS);
		boolean onlyUseTodaysEncounterForBilling = SerializableBoolean.valueOf(runContext, Parameters.ADDCONS_SAMEDAY);
		boolean autoAddBillableEncounter = SerializableBoolean.valueOf(runContext,
				Parameters.BOOLEAN_AUTO_ADD_BILLABLE_ENCOUNTER);
		boolean notifyOnAutoAddEncounter = SerializableBoolean.valueOf(runContext,
				Parameters.BOOLEAN_NOTIFY_ON_AUTO_ADD_ENCOUNTER);

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
				String errorMessage = String.format("Could not get data from result [%s].", labResult.getId());
				logger.warn(errorMessage);
				throw new TaskException(TaskException.EXECUTION_ERROR, errorMessage);
			}
		}

		// all properties are available
		Optional<ILabMapping> mapping = LabServiceHolder.get().getLabMappingByContactAndItem(labResult.getOrigin(),
				labResult.getItem());
		if (mapping.isPresent() && mapping.get().isCharge()) {
			String ealCode = labResult.getItem().getBillingCode();
			logger.info(String.format("Adding EAL tarif [%s] from [%s]", ealCode, labResult.getOrigin().getLabel()));
			if (ealCode != null && !ealCode.isEmpty()) {
				IBillable tarif = getLabor2009TarifByCode(ealCode);
				if (tarif == null) {
					String errorString = String.format("Item %s: EAL tarif [%s] does not exist.",
							labResult.getItem().getLabel(), ealCode);
					logger.warn(errorString);
					throw new TaskException(TaskException.EXECUTION_ERROR, errorString);
				}

				synchronized (addTarifLock) {
					Optional<IEncounter> kons = getBillableEncounter(labResult.getPatient(),
							onlyUseTodaysEncounterForBilling, autoAddBillableEncounter, notifyOnAutoAddEncounter);
					if (kons.isPresent()) {
						Result<?> addTarifToKons = addTarifToKons(tarif, kons.get(),
								autoBillTarmed00_0010IfNotAlreadyBilledOnEncounter);
						return Collections.singletonMap(ReturnParameter.RESULT_DATA, addTarifToKons.toString());
					} else {
						String errorString = String.format(
								"Could not add tarif [%s] for result of patient [%s] because no billable kons found.",
								ealCode, labResult.getPatient().getLabel());
						logger.warn(errorString);
						Map<String, Serializable> resultMap = new HashMap<String, Serializable>();
						resultMap.put(ReturnParameter.MARKER_WARN, null);
						resultMap.put(ReturnParameter.RESULT_DATA, errorString);
						return resultMap;
					}
				}
			}
		} else {
			logger.debug("No mapping present or is not to charge");
		}
		// no mapping found or not to charge
		return Collections.singletonMap(ReturnParameter.MARKER_DO_NOT_PERSIST, true);
	}
}
