package ch.elexis.core.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ac.ACEAccessBitMapConstraint;
import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBillableVerifier;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IDiagnosis;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.billable.DefaultVerifier;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.holder.BillingServiceHolder;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.CoverageServiceHolder;
import ch.elexis.core.services.holder.LocalLockServiceHolder;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.text.model.Samdas;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;
import ch.rgw.tools.Result.msg;
import ch.rgw.tools.VersionedResource;

@Component
public class EncounterService implements IEncounterService {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference
	private IAccessControlService accessControlService;

	@Reference
	private ICodeElementService codeElementService;

	@Reference
	private IBillingService billingService;

	@Reference
	private IConfigService configService;

	@Reference
	private IStoreToStringService storeToStringService;
	
	@Reference
	private ICoverageService coverageService;

	@Override
	public boolean isEditable(IEncounter encounter) {
		boolean editable = false;
		if (encounter != null) {
			boolean hasRight = accessControlService.evaluate(
					EvACE.of(IEncounter.class, Right.UPDATE, storeToStringService.storeToString(encounter).get()));
			if (hasRight) {
				// user has right to change encounter. in this case, the user
				// may change the text even if the encounter has already been
				// billed, so don't check if it is billed
				editable = isEditableInternal(encounter);
			} else {
				// normal case, check all
				editable = billingService.isEditable(encounter).isOK();
			}
		}
		return editable;
	}

	@Override
	public Result<IEncounter> transferToMandator(IEncounter encounter, IMandator mandator, boolean ignoreEditable) {
		if (encounter.getMandator().equals(mandator)) {
			return Result.OK();
		}
		Result<IEncounter> editableResult = billingService.isEditable(encounter);
		if (!editableResult.isOK() && !ignoreEditable) {
			return editableResult;
		}

		Result<IEncounter> result = new Result<>(encounter);

		// transfer encounter and save to clear dirty flag
		encounter.setMandator(mandator);
		CoreModelServiceHolder.get().save(encounter);

		result = reBillEncounter(encounter);
		coreModelService.save(encounter);
		ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, encounter);

		return result;
	}

	@Override
	public Result<IEncounter> transferToCoverage(IEncounter encounter, ICoverage coverage, boolean ignoreEditable) {
		if (encounter.getCoverage().equals(coverage)) {
			return Result.OK();
		}
		Result<IEncounter> editableResult = billingService.isEditable(encounter);
		if (!editableResult.isOK() && !ignoreEditable) {
			return editableResult;
		}

		Result<IEncounter> result = new Result<>(encounter);

		ICoverage encounterCovearage = encounter.getCoverage();
		// transfer encounter and save to clear dirty flag
		encounter.setCoverage(coverage);
		coreModelService.save(encounter);
		if (encounterCovearage != null) {
			result = reBillEncounter(encounter);
		}
		encounter.addUpdated(ModelPackage.Literals.IENCOUNTER__COVERAGE);
		coreModelService.save(encounter);
		ContextServiceHolder.get().setActiveCoverage(coverage);
		return result;
	}

	@Override
	public Result<IEncounter> reBillEncounter(IEncounter encounter) {
		Result<IEncounter> result = new Result<>(encounter);

		List<IBilled> encounterBilled = encounter.getBilled();

		// test if all required IBillable types are resolvable
		for (IBilled billed : encounterBilled) {
			IBillable billable = billed.getBillable();
			if (billable == null) {
				String message = "Could not resolve billable for billed [" + billed + "]";
				return new Result<>(SEVERITY.ERROR, 0, message, encounter, false);
			}
		}

		List<IBilled> tardocVerrechnet = getTardocOnly(encounter.getBilled());
		// make sure Zuschlagleistung is re charged after Hauptleistung
		List<IBilled> tardocZuschlagVerrechnet = tardocVerrechnet.stream().filter(v -> isZuschlag(v)).toList();
		tardocVerrechnet.removeAll(tardocZuschlagVerrechnet);
		// make sure Referenzleistung is re charged after Hauptleistung
		List<IBilled> tardocReferenzVerrechnet = tardocVerrechnet.stream().filter(v -> isReferenz(v)).toList();
		tardocVerrechnet.removeAll(tardocReferenzVerrechnet);

		List<IBilled> tarmedVerrechnet = getTarmedOnly(encounter.getBilled());
		// make sure Zuschlagleistung is re charged after Hauptleistung
		List<IBilled> tarmedZuschlagVerrechnet = tardocVerrechnet.stream().filter(v -> isZuschlag(v)).toList();
		tardocVerrechnet.removeAll(tardocZuschlagVerrechnet);
		// make sure Referenzleistung is re charged after Hauptleistung
		List<IBilled> tarmedReferenzVerrechnet = tardocVerrechnet.stream().filter(v -> isReferenz(v)).toList();
		tardocVerrechnet.removeAll(tardocReferenzVerrechnet);

		List<IBilled> othersVerrechnet = getOthersOnly(encounter.getBilled());

		reCharge(tardocVerrechnet, encounter, result);
		reCharge(tardocZuschlagVerrechnet, encounter, result);
		reCharge(tardocReferenzVerrechnet, encounter, result);

		reCharge(tarmedVerrechnet, encounter, result);
		reCharge(tarmedZuschlagVerrechnet, encounter, result);
		reCharge(tarmedReferenzVerrechnet, encounter, result);

		reCharge(othersVerrechnet, encounter, result);
		return result;
	}

	private void reCharge(List<IBilled> billed, IEncounter encounter, Result<IEncounter> result) {
		Map<IBilled, Double> amountMap = new HashMap<>();
		Map<IBilled, ICodeElement> verrechenbarMap = new HashMap<>();
		billed.forEach(v -> amountMap.put(v, v.getAmount()));
		billed.forEach(v -> getMatchingVerrechenbar(v, encounter, result).ifPresent(c -> verrechenbarMap.put(v, c)));
		// do not remove or add if there is no ICodeElement match found
		billed = billed.stream().filter(v -> verrechenbarMap.containsKey(v)).toList();
		// remove all
		billed.forEach(v -> removeVerrechnet(encounter, v, result));
		// add all
		billed.forEach(v -> addVerrechnet(encounter, verrechenbarMap.get(v), amountMap.get(v), result));
	}

	private void addVerrechnet(IEncounter encounter, ICodeElement billable, double amount, Result<IEncounter> result) {
		// no locking required, PersistentObject create events are passed to server (RH)
		for (int i = 0; i < amount; i++) {
			Result<IBilled> addRes = BillingServiceHolder.get().bill((IBillable) billable, encounter, 1);
			if (!addRes.isOK()) {
				String message = "Achtung: durch den Fall wechsel wurde die Position " + billable.getCode()
						+ " automatisch entfernt.\n" + addRes.toString();
				result.addMessage(SEVERITY.WARNING, message, encounter);
			}
		}
	}

	private void removeVerrechnet(IEncounter encounter, IBilled billed, Result<IEncounter> result) {
		// acquire lock before removing
		LockResponse lockResult = LocalLockServiceHolder.get().acquireLockBlocking(billed, 10,
				new NullProgressMonitor());
		if (lockResult.isOk()) {
			Result<?> removeRes = BillingServiceHolder.get().removeBilled(billed, encounter);
			if (!removeRes.isOK()) {
				String message = "Achtung: Position " + billed.getCode() + " konnte nicht entfernt werden.";
				result.addMessage(SEVERITY.WARNING, message, encounter);
			}
			LocalLockServiceHolder.get().releaseLock(lockResult.getLockInfo());
		} else {
			String message = "Achtung: Locking von Position " + billed.getCode() + " fehlgeschlagen.";
			result.addMessage(SEVERITY.WARNING, message, encounter);
		}
	}

	private Optional<ICodeElement> getMatchingVerrechenbar(IBilled billed, IEncounter encounter,
			Result<IEncounter> result) {
		IBillable billable = billed.getBillable();
		if (billable != null) {
			HashMap<Object, Object> context = getCodeElementServiceContext(encounter);
			// make sure we verrechenbar is matching for the kons
			Optional<ICodeElement> matchingVerrechenbar = codeElementService
					.loadFromString(billable.getCodeSystemName(), billable.getCode(), context);
			if (matchingVerrechenbar.isEmpty()) {
				String message = "Achtung: durch den Fall wechsel wurde die Position " + billable.getCode()
						+ " automatisch entfernt, da diese im neuen Fall nicht vorhanden ist.";
				result.addMessage(SEVERITY.WARNING, message, encounter);
			} else {
				return matchingVerrechenbar;
			}
		}
		return Optional.empty();
	}

	private List<IBilled> getTardocOnly(List<IBilled> list) {
		List<IBilled> ret = new ArrayList<>();
		for (IBilled verrechnet : list) {
			IBillable billable = verrechnet.getBillable();
			if (billable.getCodeSystemName().contains("TARDOC")) {
				ret.add(verrechnet);
			}
		}
		return ret;
	}

	private List<IBilled> getTarmedOnly(List<IBilled> list) {
		List<IBilled> ret = new ArrayList<>();
		for (IBilled verrechnet : list) {
			IBillable billable = verrechnet.getBillable();
			if (billable.getCodeSystemName().contains("Tarmed")) {
				ret.add(verrechnet);
			}
		}
		return ret;
	}

	private List<IBilled> getOthersOnly(List<IBilled> list) {
		List<IBilled> ret = new ArrayList<>();
		for (IBilled verrechnet : list) {
			IBillable billable = verrechnet.getBillable();
			if (!billable.getCodeSystemName().contains("Tarmed") && !billable.getCodeSystemName().contains("TARDOC")) {
				ret.add(verrechnet);
			}
		}
		return ret;
	}

	private boolean isReferenz(IBilled tardocVerr) {
		IBillable verrechenbar = tardocVerr.getBillable();
		String serviceTyp = getServiceTypReflective(verrechenbar);
		return serviceTyp != null && serviceTyp.equals("R");
	}

	private boolean isZuschlag(IBilled tardocVerr) {
		IBillable verrechenbar = tardocVerr.getBillable();
		Boolean serviceTyp = getIsZuschlagsleistungReflective(verrechenbar);
		return serviceTyp != null && serviceTyp;
	}

	private String getServiceTypReflective(IBillable billable) {
		try {
			Method getterMethod = billable.getClass().getMethod("getServiceTyp", (Class[]) null);
			Object typ = getterMethod.invoke(billable, (Object[]) null);
			if (typ instanceof String) {
				return (String) typ;
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			LoggerFactory.getLogger(getClass()).warn("Could not get service typ of [" + billable + "]", e.getMessage());
		}
		return null;
	}

	private Boolean getIsZuschlagsleistungReflective(IBillable billable) {
		try {
			Method getterMethod = billable.getClass().getMethod("isZuschlagsleistung", (Class[]) null);
			Object typ = getterMethod.invoke(billable, (Object[]) null);
			if (typ instanceof Boolean) {
				return (Boolean) typ;
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			LoggerFactory.getLogger(getClass()).warn("Could not get service typ of [" + billable + "]", e.getMessage());
		}
		return null;
	}

	private HashMap<Object, Object> getCodeElementServiceContext(IEncounter encounter) {
		HashMap<Object, Object> ret = new HashMap<>();
		ret.put(ICodeElementService.ContextKeys.CONSULTATION, encounter);
		ICoverage coverage = encounter.getCoverage();
		if (coverage != null) {
			ret.put(ICodeElementService.ContextKeys.COVERAGE, coverage);
		}
		return ret;
	}

	private boolean isEditableInternal(IEncounter encounter) {
		ICoverage coverage = encounter.getCoverage();
		if (coverage != null) {
			if (!coverage.isOpen()) {
				return false;
			}
		}

		IMandator encounterMandator = encounter.getMandator();
		boolean checkMandant = !accessControlService.evaluate(EvACE.of("LSTG_CHARGE_FOR_ALL"));
		boolean mandatorOK = true;
		IMandator activeMandator = ContextServiceHolder.get().getActiveMandator().orElse(null);
		boolean mandatorLoggedIn = (activeMandator != null);

		// if m is null, ignore checks (return true)
		if (encounterMandator != null && activeMandator != null) {
			if (checkMandant && !(encounterMandator.getId().equals(activeMandator.getId()))) {
				mandatorOK = false;
			}
		}

		boolean ok = mandatorOK && mandatorLoggedIn;
		return ok ? true : false;
	}

	@Override
	public Optional<IEncounter> getLatestEncounter(IPatient patient, boolean create) {
		if (!ContextServiceHolder.get().getActiveMandator().isPresent()) {
			return Optional.empty();
		}
		IMandator activeMandator = ContextServiceHolder.get().getActiveMandator().get();
		IContact userContact = ContextServiceHolder.get().getActiveUserContact().get();
		IQuery<IEncounter> encounterQuery = CoreModelServiceHolder.get().getQuery(IEncounter.class);

		// if not configured otherwise load only consultations of active mandant
		if (!ConfigServiceHolder.get().get(userContact, Preferences.USR_DEFLOADCONSALL, false)) {
			encounterQuery.and(ModelPackage.Literals.IENCOUNTER__MANDATOR, COMPARATOR.EQUALS, activeMandator);
		}

		List<ICoverage> coverages = patient.getCoverages();
		if (coverages == null || coverages.isEmpty()) {
			return create ? createCoverageAndEncounter(patient) : Optional.empty();
		}
		encounterQuery.startGroup();
		boolean termInserted = false;
		for (ICoverage coverage : coverages) {
			if (coverage.isOpen()) {
				encounterQuery.or(ModelPackage.Literals.IENCOUNTER__COVERAGE, COMPARATOR.EQUALS, coverage);
				termInserted = true;
			}
		}
		if (!termInserted) {
			return create ? createCoverageAndEncounter(patient) : Optional.empty();
		}
		encounterQuery.andJoinGroups();
		encounterQuery.orderBy(ModelPackage.Literals.IENCOUNTER__DATE, ORDER.DESC);
		List<IEncounter> list = encounterQuery.execute();
		if ((list == null) || list.isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.of(list.get(0));
		}
	}

	private Optional<IEncounter> createCoverageAndEncounter(IPatient patient) {
		ICoverage coverage = CoverageServiceHolder.get().createDefaultCoverage(patient);
		Optional<IMandator> activeMandator = ContextServiceHolder.get().getActiveMandator();
		if (activeMandator.isPresent()) {
			return Optional.of(
					new IEncounterBuilder(CoreModelServiceHolder.get(), coverage, activeMandator.get()).buildAndSave());
		}
		return Optional.empty();
	}

	@Override
	public Optional<IEncounter> getLatestEncounter(IPatient patient) {
		List<IEncounter> result = null;
		Optional<ACEAccessBitMapConstraint> aoboOrSelf = accessControlService
				.isAoboOrSelf(EvACE.of(IEncounter.class, Right.READ));
		if(aoboOrSelf.isPresent()) {
			INamedQuery<IEncounter> query = CoreModelServiceHolder.get().getNamedQueryByName(IEncounter.class,
					IEncounter.class, "Behandlung.patient.last.aobo");
			if (aoboOrSelf.get() == ACEAccessBitMapConstraint.AOBO) {
				result = query.executeWithParameters(query.getParameterMap("patient", patient, "aoboids",
						accessControlService.getAoboMandatorIdsForSqlIn()));
			} else if (aoboOrSelf.get() == ACEAccessBitMapConstraint.SELF) {
				result = query.executeWithParameters(query.getParameterMap("patient", patient, "aoboids",
						Collections.singletonList(accessControlService.getSelfMandatorId())));
			}
		} else {
			INamedQuery<IEncounter> query = CoreModelServiceHolder.get().getNamedQueryByName(IEncounter.class,
					IEncounter.class, "Behandlung.patient.last");
			result = query.executeWithParameters(query.getParameterMap("patient", patient));
		}
		if (result != null && !result.isEmpty()) {
			return Optional.of(result.get(0));
		}
		return Optional.empty();
	}

	private String getVersionRemark() {
		String remark = "edit";
		java.util.Optional<IUser> activeUser = ContextServiceHolder.get().getActiveUser();
		if (activeUser.isPresent()) {
			remark = activeUser.get().getLabel();
		}
		return remark;
	}

	@Override
	public synchronized void updateVersionedEntry(IEncounter encounter, Samdas samdas) {
		updateVersionedEntry(encounter, samdas.toString(), getVersionRemark());
	}

	@Override
	public synchronized void updateVersionedEntry(IEncounter encounter, String entryXml, String remark) {
		// make sure we are working with latest info from the database
		coreModelService.refresh(encounter, true);
		encounter.getVersionedEntry().update(entryXml, remark);
		coreModelService.save(encounter);
	}

	@Override
	public Money getSales(IEncounter encounter) {
		Money ret = new Money();
		for (IBilled billed : encounter.getBilled()) {
			ret.addMoney(billed.getTotal());
		}
		return ret;
	}

	@Override
	public List<IEncounter> getAllEncountersForPatient(IPatient patient) {
		IQuery<ICoverage> query = CoreModelServiceHolder.get().getQuery(ICoverage.class);
		query.and(ModelPackage.Literals.ICOVERAGE__PATIENT, COMPARATOR.EQUALS, patient);
		List<ICoverage> coverages = query.execute();
		List<IEncounter> collect = coverages.stream().flatMap(cv -> cv.getEncounters().stream())
				.sorted((c1, c2) -> c2.getDate().compareTo(c1.getDate())).collect(Collectors.toList());
		return collect;
	}

	@Override
	public List<IBilled> getBilledByBillable(IEncounter encounter, IBillable billable) {
		INamedQuery<IBilled> query = CoreModelServiceHolder.get().getNamedQuery(IBilled.class, "behandlung",
				"leistungenCode");
		return query.executeWithParameters(
				query.getParameterMap("behandlung", encounter, "leistungenCode", billable.getId()));
	}

	@Override
	public void addDefaultDiagnosis(IEncounter encounter) {
		String diagnosisSts = configService.getActiveUserContact(Preferences.USR_DEFDIAGNOSE, StringUtils.EMPTY);
		if (diagnosisSts.length() > 1) {
			Optional<Identifiable> diagnose = StoreToStringServiceHolder.get().loadFromString(diagnosisSts);
			if (diagnose.isPresent()) {
				encounter.addDiagnosis((IDiagnosis) diagnose.get());
				coreModelService.save(encounter);
			}
		}
	}

	@Override
	public void addXRef(IEncounter encounter, String provider, String id, int pos, String text) {
		// fire prerelease triggers save
		ContextServiceHolder.get().sendEvent(ElexisEventTopics.EVENT_LOCK_PRERELEASE, encounter);

		VersionedResource vr = encounter.getVersionedEntry();
		String ntext = vr.getHead();
		Samdas samdas = new Samdas(ntext);
		Samdas.Record record = samdas.getRecord();
		String recText = record.getText();
		if ((pos == -1) || pos > recText.length()) {
			pos = recText.length();
			recText += StringUtils.LF + text;
		} else {
			recText = recText.substring(0, pos) + StringUtils.LF + text + recText.substring(pos);
		}
		record.setText(recText);
		// ++pos because \n has been added
		Samdas.XRef xref = new Samdas.XRef(provider, id, ++pos, text.length());
		record.add(xref);
		updateVersionedEntry(encounter, samdas); // XRefs may always be added
		// update with the added content
		ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, encounter);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Result<IEncounter> setEncounterDate(IEncounter encounter, LocalDate newDate) {
		// modify the date
		encounter.setDate(newDate);
		CoreModelServiceHolder.get().save(encounter);

		Result<IEncounter> ret = new Result<IEncounter>(encounter);
		List<IBillableVerifier> verifiers = new ArrayList<>();
		// get an optifier for the tarmed code system
		for (IBilled billed : encounter.getBilled()) {
			IBillable billable = billed.getBillable();
			if (billable != null && billable.getVerifier() != null
					&& !(billable.getVerifier() instanceof DefaultVerifier)) {
				if (!verifiers.stream().anyMatch(v -> v.getClass().equals(billable.getVerifier().getClass()))) {
					verifiers.add(billable.getVerifier());
				}
			}
		}
		if (!verifiers.isEmpty()) {
			for (IBillableVerifier verifier : verifiers) {
				Result<IBilled> result = verifier.verify(encounter);
				if (!result.isOK()) {
					// remove invalid billed on new date
					List<Result<IBilled>.msg> messages = result.getMessages();
					for (msg msg : messages) {
						if (msg.getObject() instanceof IBilled) {
							IBilled billed = (IBilled) msg.getObject();
							IBillable billable = billed.getBillable();
							if (billable != null) {
								encounter.removeBilled(billed);
								String message = "Achtung: durch die Änderung wurde die Position " + billable.getCode()
										+ " automatisch entfernt.\nLimitation: " + msg.getText();
								ret.addMessage(SEVERITY.WARNING, message, encounter);
							}
						}
					}
				}
			}
		}
		return ret;
	}

	@Override
	public boolean canDelete(IEncounter element) {
		if (element != null) {
			return element.getBilled().isEmpty() && element.getDiagnoses().isEmpty();
		}
		return false;
	}
}
