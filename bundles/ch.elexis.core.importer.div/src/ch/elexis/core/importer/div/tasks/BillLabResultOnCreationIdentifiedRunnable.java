package ch.elexis.core.importer.div.tasks;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.ILabMapping;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.builder.ICoverageBuilder;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.model.ch.BillingLaw;
import ch.elexis.core.model.message.MessageCode;
import ch.elexis.core.model.message.MessageCodeMessageId;
import ch.elexis.core.model.message.TransientMessage;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.SerializableBoolean;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.ICodeElementService.ContextKeys;
import ch.elexis.core.services.ICoverageService;
import ch.elexis.core.services.IMessageService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.BillingServiceHolder;
import ch.elexis.core.services.holder.CodeElementServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.CoverageServiceHolder;
import ch.elexis.core.services.holder.EncounterServiceHolder;
import ch.elexis.core.services.holder.LabServiceHolder;
import ch.elexis.core.services.holder.MessageServiceHolder;
import ch.elexis.core.time.TimeUtil;
import ch.rgw.tools.Result;

/**
 * @see at.medevit.elexis.roche.labor.billing.AddLabToKons for original
 *      implementation
 */
public class BillLabResultOnCreationIdentifiedRunnable implements IIdentifiedRunnable {

	/**
	 * @see at.medevit.elexis.roche.labor.preference.RochePreferencePage
	 */
	public class Parameters {
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
			Map<Object, Object> context = CodeElementServiceHolder.createContext(kons);
			if (kons.getDate().isAfter(LocalDate.of(2025, 12, 31))) {
				Optional<ICodeElement> tardoc = CodeElementServiceHolder.get().loadFromString("TARDOC",
						getTardocConsCode(kons.getMandator()), context);
				if (tardoc.isPresent()) {
					return (IBillable) tardoc.get();
				}
			} else {
				Optional<ICodeElement> tarmed = CodeElementServiceHolder.get().loadFromString("Tarmed", "00.0010",
						context);
				if (tarmed.isPresent()) {
					return (IBillable) tarmed.get();
				}
			}
		}
		return null;
	}

	private boolean isLabResultReady(ILabResult labResult) {
		// TODO move to model?
		// TODO still required?
		List<Object> values = new ArrayList<>();
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
	 * @throws TaskException
	 * @see https://redmine.medelexis.ch/issues/22266
	 */
	private Optional<IEncounter> getBillableEncounter(IPatient patient, boolean autoAddBillableEncounter,
			boolean notifyOnAutoAddEncounter) throws TaskException {

		IEncounter validEncounter = null;

		IQuery<ICoverage> openCoverageQuery = CoreModelServiceHolder.get().getQuery(ICoverage.class);
		openCoverageQuery.and(ModelPackage.Literals.ICOVERAGE__PATIENT, COMPARATOR.EQUALS, patient);
		openCoverageQuery.and(ModelPackage.Literals.ICOVERAGE__DATE_TO, COMPARATOR.EQUALS, null);
		List<ICoverage> openCoverages = openCoverageQuery.execute();

		List<IEncounter> todaysOpenBillableEncounters = null;
		if (!openCoverages.isEmpty()) {
			todaysOpenBillableEncounters = openCoverages.stream().flatMap(cv -> cv.getEncounters().stream())
					.filter(encounter -> TimeUtil.isToday(encounter.getDate()))
					.filter(encounter -> BillingServiceHolder.get().isEditable(encounter).isOK())
					.collect(Collectors.toList());

			if (todaysOpenBillableEncounters.size() == 1) {
				validEncounter = todaysOpenBillableEncounters.get(0);
			} else if (todaysOpenBillableEncounters.size() > 1) {
				// KVG encounter available?
				validEncounter = todaysOpenBillableEncounters.stream()
						.filter(enc -> enc.getCoverage().getBillingSystem().getLaw() == BillingLaw.KVG).findFirst()
						.orElse(null);
				if (validEncounter == null) {
					// no - use the first available encounter
					validEncounter = todaysOpenBillableEncounters.get(0);
				}
			}
		}

		// only use encounterSelector if no validEncounter is found
		if (validEncounter == null && encounterSelector != null) {
			String konsId = encounterSelector.createOrOpenConsultation(patient);
			if (konsId != null) {
				validEncounter = coreModelService.load(konsId, IEncounter.class).get();
			}
		}

		if (validEncounter != null) {
			return Optional.of(validEncounter);
		}

		if (!autoAddBillableEncounter) {
			return Optional.empty();
		}

		boolean createdCoverage = false;
		ICoverage coverageToCreateIEncounterUpon;

		if (openCoverages.isEmpty()) {
			coverageToCreateIEncounterUpon = createDefaultCoverage(coreModelService, patient);
			createdCoverage = true;
		} else if (openCoverages.size() == 1) {
			coverageToCreateIEncounterUpon = openCoverages.get(0);
		} else {
			// KVG coverage available?
			coverageToCreateIEncounterUpon = openCoverages.stream()
					.filter(coverage -> coverage.getBillingSystem().getLaw() == BillingLaw.KVG).findFirst()
					.orElse(null);
			if (coverageToCreateIEncounterUpon == null) {
				// No - UVG coverage available?
				coverageToCreateIEncounterUpon = openCoverages.stream()
						.filter(coverage -> coverage.getBillingSystem().getLaw() == BillingLaw.UVG).findFirst()
						.orElse(null);
			}
			if (coverageToCreateIEncounterUpon == null) {
				// No create default KVG coverage
				coverageToCreateIEncounterUpon = createDefaultCoverage(coreModelService, patient);
				createdCoverage = true;
			}
		}

		if (createdCoverage) {
			logger.info("Created new coverage [{}] as no applicable found.", coverageToCreateIEncounterUpon);
		}

		IMandator mandatorToBillUpon = null;
		if (todaysOpenBillableEncounters != null && !todaysOpenBillableEncounters.isEmpty()) {
			mandatorToBillUpon = todaysOpenBillableEncounters.get(0).getMandator();
		} else {
			List<IEncounter> allEncountersForPatient = EncounterServiceHolder.get().getAllEncountersForPatient(patient);
			if (!allEncountersForPatient.isEmpty()) {
				mandatorToBillUpon = allEncountersForPatient.get(0).getMandator();
			}
		}
		if (mandatorToBillUpon == null) {
			List<IMandator> mandators = coreModelService.getQuery(IMandator.class).execute();
			if (mandators.isEmpty()) {
				logger.warn("Could not determine a mandator for patient, and no mandators available!");
				throw new TaskException(TaskException.EXECUTION_ERROR,
						"Could not determine a mandator for patient, and no mandators available!");
			} else {
				mandatorToBillUpon = mandators.get(0);
				logger.warn("Could not determine existing mandator for patient, using [{}]", mandatorToBillUpon);
			}
		}

		validEncounter = new IEncounterBuilder(coreModelService, coverageToCreateIEncounterUpon, mandatorToBillUpon)
				.buildAndSave();
		logger.info("Added encounter [{}] to bill results", validEncounter.getId());

		if (notifyOnAutoAddEncounter) {
			IStatus result = sendMessageToOwner(patient);
			if (!result.isOK()) {
				logger.warn("Could not send notification message.");
			}
		}

		return Optional.ofNullable(validEncounter);
	}

	private ICoverage createDefaultCoverage(IModelService coreModelService2, IPatient patient) {
		ICoverageService iCoverageService = CoverageServiceHolder.get();
		return new ICoverageBuilder(coreModelService, patient, iCoverageService.getDefaultCoverageLabel(),
				iCoverageService.getDefaultCoverageReason(), iCoverageService.getDefaultCoverageLaw()).buildAndSave();
	}

	private IStatus sendMessageToOwner(IPatient patient) {
		IUser owner = ContextServiceHolder.get().getActiveUser().orElse(null);
		TransientMessage transientMessage = MessageServiceHolder.get().prepare(getClass().getSimpleName(),
				IMessageService.INTERNAL_MESSAGE_URI_SCHEME + ":" + owner.getId());
		transientMessage.addMessageCode(MessageCode.Key.MessageId,
				MessageCodeMessageId.INFO_BILLING_AUTO_CREATE_ENCOUNTER.name());
		transientMessage.addMessageCode(MessageCode.Key.MessageIdParam, patient.getPatientNr());
		transientMessage.setSenderAcceptsAnswer(false);
		transientMessage.setMessageText(
				Messages.NO_BILLABLE_ENCOUNTER1 + patient.getPatientNr() + Messages.NO_BILLABLE_ENCOUNTER2);
		return MessageServiceHolder.get().send(transientMessage);
	}

	private Result<?> addTarifToKons(IBillable tarif, IEncounter kons,
			boolean autoBillTarmed00_0010IfNotAlreadyBilledOnEncounter) throws TaskException {
		// see RochePreferencePage.LABORRESULTS_BILL_ADDCONS
		if (autoBillTarmed00_0010IfNotAlreadyBilledOnEncounter) {
			synchronized (kons) {
				String tardocConsCode = getTardocConsCode(kons.getMandator());
				List<IBilled> leistungen = kons.getBilled();
				boolean addCons = true;
				for (IBilled verrechnet : leistungen) {
					IBillable verrechenbar = verrechnet.getBillable();
					if (verrechenbar != null && verrechenbar.getCodeSystemName().equals("Tarmed")
							&& verrechenbar.getCode().equals("00.0010")) {
						addCons = false;
						break;
					}
					if (verrechenbar != null && verrechenbar.getCodeSystemName().equals("TARDOC")
							&& verrechenbar.getCode().equals(tardocConsCode)) {
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
						ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, kons);
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

	private String getTardocConsCode(IMandator mandator) {
		Object typeObj = mandator.getExtInfo("ch.elexis.data.tardoc.mandant.dignitaet");
		if (typeObj instanceof String) {
			String[] codesString = ((String) typeObj).split("::");
			for (String codeString : codesString) {
				String[] codeParts = codeString.split("\\|");
				if (codeParts.length == 2) {
					if ("1100".equals(codeParts[0]) || "3010".equals(codeParts[0])) {
						return "CA.00.0010";
					}
				}
			}
		}
		return "AA.00.0010";
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
					Optional<IEncounter> kons = getBillableEncounter(labResult.getPatient(), autoAddBillableEncounter,
							notifyOnAutoAddEncounter);
					if (kons.isPresent()) {
						Result<?> addTarifToKons = addTarifToKons(tarif, kons.get(),
								autoBillTarmed00_0010IfNotAlreadyBilledOnEncounter);
						return Collections.singletonMap(ReturnParameter.RESULT_DATA, addTarifToKons.toString());
					} else {
						String errorString = String.format(
								"Could not add tarif [%s] for result of patient [%s] because no billable kons found.",
								ealCode, labResult.getPatient().getLabel());
						logger.warn(errorString);
						Map<String, Serializable> resultMap = new HashMap<>();
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
