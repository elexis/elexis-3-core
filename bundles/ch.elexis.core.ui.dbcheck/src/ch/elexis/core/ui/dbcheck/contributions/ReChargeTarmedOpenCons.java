package ch.elexis.core.ui.dbcheck.contributions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.ICodeElementService.ContextKeys;
import ch.elexis.core.services.holder.BillingServiceHolder;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Query;
import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;

public class ReChargeTarmedOpenCons extends ExternalMaintenance {

	private List<String> problems = new ArrayList<>();

	protected ICodeElementService codeElementService;

	private ServiceReference<ICodeElementService> serviceRef;

	private boolean currentMandantOnly;

	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion) {
		Integer count = 0;

		if (initCodeElementService()) {
			getCurrentMandantOnly();

			// make sure not billing strict
			boolean presetBillingStrict = ConfigServiceHolder.getUser(Preferences.LEISTUNGSCODES_BILLING_STRICT, false);
			ConfigServiceHolder.setUser(Preferences.LEISTUNGSCODES_BILLING_STRICT, false);

			List<Konsultation> consultations = getKonsultation(getBeginOfYear(), getEndOfYear());
			pm.beginTask("Bitte warten, Tarmed Leistungen werden neu verrechnet", consultations.size());
			for (Konsultation konsultation : consultations) {
				// only still open Konsultation
				if (konsultation.getRechnung() != null)
					continue;

				IEncounter encounter = NoPoUtil.loadAsIdentifiable(konsultation, IEncounter.class).get();

				if (pm.isCanceled()) {
					addProblem("Cancelled.", encounter);
					return getProblemsString();
				}
				List<IBilled> tarmedVerrechnet = getTarmedOnly(encounter.getBilled());
				for (IBilled tarmedVerr : tarmedVerrechnet) {
					IBillable verrechenbar = tarmedVerr.getBillable();
					if (verrechenbar != null) {
						// make sure we verrechenbar is matching for the kons
						Optional<ICodeElement> matchingVerrechenbar = codeElementService.loadFromString(
								verrechenbar.getCodeSystemName(), verrechenbar.getCode(), getContext(encounter));
						if (matchingVerrechenbar.isPresent()) {
							double amount = tarmedVerr.getAmount();
							removeVerrechnet(encounter, tarmedVerr);
							addVerrechnet(encounter, matchingVerrechenbar, amount);
						} else {
							addProblem("Could not find matching Verrechenbar for [" + verrechenbar.getCodeSystemName()
									+ "->" + verrechenbar.getCode() + "]", encounter);
						}
					} else {
						addProblem("Could not find Verrechenbar for [" + tarmedVerr.getLabel() + "]", encounter);
					}
				}
				count++;
				pm.worked(1);
			}

			ConfigServiceHolder.setUser(Preferences.LEISTUNGSCODES_BILLING_STRICT, presetBillingStrict);

			pm.done();
			deInitCodeElementService();
		}

		return "Tarmed Leistungen von [" + count + "] Konsultationen des Jahres [" + getBeginOfYear().get(TimeTool.YEAR)
				+ "] neu verrechnet" + getProblemsString();
	}

	private void getCurrentMandantOnly() {
		Display.getDefault().syncExec(() -> {
			currentMandantOnly = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
					"Nur Mandant neu verrechnen",
					"Sollen die offenen Konsultationen aller Mandanten (Nein), oder nur des aktiven (Ja) neu verrechnet werden?");
		});
	}

	protected TimeTool getBeginOfYear() {
		TimeTool beginOfYear = new TimeTool();
		beginOfYear.set(TimeTool.MONTH, 0);
		beginOfYear.set(TimeTool.DAY_OF_MONTH, 1);
		return beginOfYear;
	}

	protected TimeTool getEndOfYear() {
		TimeTool endOfYear = getBeginOfYear();
		endOfYear.set(TimeTool.MONTH, 11);
		endOfYear.set(TimeTool.DAY_OF_MONTH, 31);
		return endOfYear;
	}

	private void addVerrechnet(IEncounter encounter, Optional<ICodeElement> matchingVerrechenbar, double amount) {
		// no locking required, PersistentObject create events are passed to server (RH)
		for (int i = 0; i < amount; i++) {
			Result<IBilled> addRes = BillingServiceHolder.get().bill((IBillable) matchingVerrechenbar.get(), encounter,
					1);
			if (!addRes.isOK()) {
				addProblem("Could not add Verrechenbar [" + matchingVerrechenbar.get().getCode() + "]" + "["
						+ addRes.toString() + "]", encounter);
			}
		}
	}

	private void removeVerrechnet(IEncounter encounter, IBilled tarmedVerr) {
		// acquire lock before removing
		LockResponse result = LocalLockServiceHolder.get().acquireLockBlocking(tarmedVerr, 10,
				new NullProgressMonitor());
		if (result.isOk()) {

			Result<?> removeRes = BillingServiceHolder.get().removeBilled(tarmedVerr, encounter);
			if (!removeRes.isOK()) {
				addProblem("Could not remove Verrechnet [" + tarmedVerr.getLabel() + "]" + "[" + removeRes.toString()
						+ "]", encounter);
			}
			LockResponse releaseLock = LocalLockServiceHolder.get().releaseLock(result.getLockInfo());
			if (!releaseLock.isOk()) {
				addProblem("Could not release lock for Verrechnet [" + tarmedVerr.getLabel() + "]" + "["
						+ removeRes.toString() + "]", encounter);
			}
		} else {
			addProblem("Could not remove Verrechnet [" + tarmedVerr.getLabel() + "]" + "[ could not acquire lock ]",
					encounter);
		}
	}

	private HashMap<Object, Object> getContext(IEncounter encounter) {
		HashMap<Object, Object> ret = new HashMap<>();
		if (encounter != null) {
			ret.put(ContextKeys.CONSULTATION, encounter);
			ICoverage coverage = encounter.getCoverage();
			if (coverage != null) {
				ret.put(ContextKeys.COVERAGE, coverage);
			}
		}
		return ret;
	}

	private void deInitCodeElementService() {
		BundleContext context = FrameworkUtil.getBundle(ReChargeTarmedOpenCons.class).getBundleContext();
		if (serviceRef != null) {
			context.ungetService(serviceRef);
			codeElementService = null;
		}
	}

	private boolean initCodeElementService() {
		BundleContext context = FrameworkUtil.getBundle(ReChargeTarmedOpenCons.class).getBundleContext();
		serviceRef = context.getServiceReference(ICodeElementService.class);
		if (serviceRef != null) {
			codeElementService = context.getService(serviceRef);
			return true;
		} else {
			return false;
		}
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

	public List<Konsultation> getKonsultation(TimeTool from, TimeTool to) {
		Query<Konsultation> qbe = new Query<>(Konsultation.class);
		qbe.add(Konsultation.DATE, Query.GREATER_OR_EQUAL, from.toString(TimeTool.DATE_COMPACT));
		if (to != null) {
			qbe.add(Konsultation.DATE, Query.LESS_OR_EQUAL, to.toString(TimeTool.DATE_COMPACT));
		}
		if (currentMandantOnly) {
			qbe.add(Konsultation.FLD_MANDATOR_ID, Query.EQUALS, ContextServiceHolder.getActiveMandatorOrNull().getId());
		}
		return qbe.execute();
	}

	private String getProblemsString() {
		if (problems != null && !problems.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("\nProblems:\n");
			problems.stream().forEach(problem -> sb.append(problem + StringUtils.LF));
			return sb.toString();
		}
		return StringUtils.EMPTY;
	}

	private void addProblem(String prefix, IEncounter cons) {
		problems.add("[" + prefix + "]" + "[" + cons.getId() + "] - [" + cons.getLabel() + "] of ["
				+ cons.getPatient().getLabel() + "]");
	}

	@Override
	public String getMaintenanceDescription() {
		return "Tarmed Leistungen aller offenen Konsultationen dieses Jahres neu verrechnen";
	}
}
