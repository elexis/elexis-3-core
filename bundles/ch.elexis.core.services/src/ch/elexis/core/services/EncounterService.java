package ch.elexis.core.services;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.ac.AccessControlDefaults;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IBillingSystemFactor;
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
import ch.elexis.core.model.builder.ICoverageBuilder;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.holder.CodeElementServiceHolder;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.CoverageServiceHolder;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.text.model.Samdas;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;
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

	private Optional<?> fallBackConsumer;

	@Override
	public boolean isEditable(IEncounter encounter) {
		boolean editable = false;
		if (encounter != null) {
			boolean hasRight = accessControlService.request(AccessControlDefaults.ADMIN_KONS_EDIT_IF_BILLED);
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

		Result<IEncounter> result = new Result<IEncounter>(encounter);

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

		Result<IEncounter> result = new Result<IEncounter>(encounter);

		ICoverage encounterCovearage = encounter.getCoverage();
		// transfer encounter and save to clear dirty flag
		encounter.setCoverage(coverage);
		coreModelService.save(encounter);
		if (encounterCovearage != null) {
			result = reBillEncounter(encounter);
		}
		coreModelService.save(encounter);
		ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, encounter);
		fallBackConsumer = ContextServiceHolder.get().getRootContext()
				.getNamed(IContextService.SELECTIONFALLBACK);
		if (fallBackConsumer.isPresent() && fallBackConsumer.get() instanceof Consumer) {
			ContextServiceHolder.get().getRootContext().setNamed(IContextService.SELECTIONFALLBACK, coverage);
		} else {
			ContextServiceHolder.get().setActiveCoverage(coverage);
		}
		return result;
	}

	@Override
	public Result<IEncounter> reBillEncounter(IEncounter encounter) {
		Result<IEncounter> result = new Result<IEncounter>(encounter);

		ch.elexis.core.services.ICodeElementService codeElementService = CodeElementServiceHolder.get();
		HashMap<Object, Object> context = getCodeElementServiceContext(encounter);
		List<IBilled> encounterBilled = encounter.getBilled();

		// test if all required IBillable types are resolvable
		for (IBilled billed : encounterBilled) {
			IBillable billable = billed.getBillable();
			if (billable == null) {
				String message = "Could not resolve billable for billed [" + billed + "]";
				return new Result<IEncounter>(SEVERITY.ERROR, 0, message, encounter, false);
			}
		}

		for (IBilled billed : encounterBilled) {
			IBillable billable = billed.getBillable();

			// TODO there should be a central codeSystemName registry
			if ("Tarmed".equals(billable.getCodeSystemName())) {
				Optional<ICodeElement> matchingIBillable = codeElementService
						.loadFromString(billable.getCodeSystemName(), billable.getCode(), context);
				if (matchingIBillable.isPresent()) {
					double amount = billed.getAmount();
					// do not use billing service / optifier to remove the billed, as that could
					// also modify other billed (tarmed bezug)
					encounter.removeBilled(billed);
					for (int i = 0; i < amount; i++) {
						billingService.bill((IBillable) matchingIBillable.get(), encounter, 1);
					}
				} else {
					encounter.removeBilled(billed);
					String message = "Achtung: durch den Fall wechsel wurde die Position " + billable.getCode()
							+ " automatisch entfernt, da diese im neuen Fall nicht vorhanden ist.";
					result.addMessage(SEVERITY.WARNING, message, encounter);
				}

			} else {
				@SuppressWarnings("unchecked")
				Optional<IBillingSystemFactor> billableFactor = billable.getOptifier().getFactor(encounter);
				if (billableFactor.isPresent()) {
					billed.setFactor(billableFactor.get().getFactor());
				} else {
					billed.setFactor(1.0);
				}
				coreModelService.save(billed);
			}
		}
		return result;
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
		boolean checkMandant = !accessControlService.request(AccessControlDefaults.LSTG_CHARGE_FOR_ALL);
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
		if (ok) {
			return true;
		} else {
			return false;
		}
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
		ICoverage coverage = new ICoverageBuilder(CoreModelServiceHolder.get(), patient,
				CoverageServiceHolder.get().getDefaultCoverageLabel(),
				CoverageServiceHolder.get().getDefaultCoverageReason(),
				CoverageServiceHolder.get().getDefaultCoverageLaw()).buildAndSave();
		Optional<IMandator> activeMandator = ContextServiceHolder.get().getActiveMandator();
		if (activeMandator.isPresent()) {
			return Optional.of(
					new IEncounterBuilder(CoreModelServiceHolder.get(), coverage, activeMandator.get()).buildAndSave());
		}
		return Optional.empty();
	}

	// private static final String ENCOUNTER_LAST_QUERY =
	// " ON BH.FallID = FA.id AND BH.deleted = FA.deleted WHERE FA.PatientID =
	// :patientid and FA.deleted = '0' order by BH.Datum desc, BH.lastupdate desc
	// limit 1";

	// @formatter:off
	private static final String ENCOUNTER_LAST_QUERY = "SELECT behandlungen.ID FROM behandlungen, faelle"
			+ " WHERE behandlungen.FallID = faelle.id"
			+ " AND behandlungen.deleted = faelle.deleted"
			+ " AND faelle.deleted = '0'"
			+ " AND faelle.patientID = ?1"
			+ " ORDER BY behandlungen.datum desc, behandlungen.lastupdate desc"
			+ " LIMIT 1";
	// @formatter:on

	@Override
	public Optional<IEncounter> getLatestEncounter(IPatient patient) {
		INativeQuery nativeQuery = CoreModelServiceHolder.get().getNativeQuery(ENCOUNTER_LAST_QUERY);
		Iterator<?> result = nativeQuery
				.executeWithParameters(nativeQuery.getIndexedParameterMap(Integer.valueOf(1), patient.getId()))
				.iterator();
		if (result.hasNext()) {
			String next = result.next().toString();
			return CoreModelServiceHolder.get().load(next, IEncounter.class);
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
}
