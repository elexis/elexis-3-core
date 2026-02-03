package ch.elexis.core.ui.dbcheck.contributions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBillableOptifier;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IBillingSystemFactor;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.services.IBilledAdjuster;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.ICodeElementService.ContextKeys;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Query;
import ch.rgw.tools.TimeTool;

public class SetAmbulatoryFactorOpenCons extends ExternalMaintenance {

	private List<String> problems = new ArrayList<>();

	protected ICodeElementService codeElementService;

	private ServiceReference<ICodeElementService> serviceRef;

	protected IBilledAdjuster vatAdjuster;

	private List<ServiceReference<IBilledAdjuster>> vatServiceRef;

	private boolean currentMandantOnly;

	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion) {
		Integer count = 0;
		if (initCodeElementService()) {
			initVatAdjuster();
			getCurrentMandantOnly();

			// make sure not billing strict
			boolean presetBillingStrict = ConfigServiceHolder.getUser(Preferences.LEISTUNGSCODES_BILLING_STRICT, false);
			ConfigServiceHolder.setUser(Preferences.LEISTUNGSCODES_BILLING_STRICT, false);

			List<Konsultation> consultations = getKonsultation(getBeginOfYear(), getEndOfYear());
			pm.beginTask("Bitte warten, Taxpunktwert und MWSt Info Ambulantepauschalen werden neu gesetzt",
					consultations.size());
			for (Konsultation konsultation : consultations) {
				// only still open Konsultation
				if (konsultation.getRechnung() != null)
					continue;

				IEncounter encounter = NoPoUtil.loadAsIdentifiable(konsultation, IEncounter.class).get();

				if (pm.isCanceled()) {
					addProblem("Cancelled.", encounter);
					return getProblemsString();
				}
				List<IBilled> ambulatoryVerrechnet = getAmbulatoryOnly(encounter.getBilled());
				for (IBilled ambulatoryVerr : ambulatoryVerrechnet) {
					IBillable verrechenbar = ambulatoryVerr.getBillable();
					if (verrechenbar != null) {
						// make sure we verrechenbar is matching for the kons
						Optional<ICodeElement> matchingVerrechenbar = codeElementService.loadFromString(
								verrechenbar.getCodeSystemName(), verrechenbar.getCode(), getContext(encounter));
						if (matchingVerrechenbar.isPresent()) {
							if (vatAdjuster != null) {
								vatAdjuster.adjust(ambulatoryVerr);
							}
							IBillableOptifier<?> optifier = verrechenbar.getOptifier();
							Optional<IBillingSystemFactor> factor = optifier.getFactor(encounter);
							if (factor.isPresent()) {
								ambulatoryVerr.setFactor(factor.get().getFactor());
								CoreModelServiceHolder.get().save(ambulatoryVerr);
							}
						} else {
							addProblem("Could not find matching Verrechenbar for [" + verrechenbar.getCodeSystemName()
									+ "->" + verrechenbar.getCode() + "]", encounter);
						}
					} else {
						addProblem("Could not find Verrechenbar for [" + ambulatoryVerr.getLabel() + "]", encounter);
					}
				}
				count++;
				pm.worked(1);
			}

			ConfigServiceHolder.setUser(Preferences.LEISTUNGSCODES_BILLING_STRICT, presetBillingStrict);

			pm.done();
			deInitCodeElementService();
			deInitVatAdjuster();
		}

		return "Ambulantepauschalen von [" + count + "] Konsultationen des Jahres ["
				+ getBeginOfYear().get(TimeTool.YEAR) + "] neuer Taxpunktwet gesetzt" + getProblemsString();
	}

	private void getCurrentMandantOnly() {
		Display.getDefault().syncExec(() -> {
			currentMandantOnly = MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
					"Nur Ambulantepauschalen des Mandant neu setzen",
					"Sollen bei offenen Konsultationen aller Mandanten (Nein), oder nur des aktiven (Ja) die Ambulantenpauschalen neu gesetzt werden?");
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
		BundleContext context = FrameworkUtil.getBundle(SetAmbulatoryFactorOpenCons.class).getBundleContext();
		if (serviceRef != null) {
			context.ungetService(serviceRef);
			codeElementService = null;
		}
	}

	private boolean initCodeElementService() {
		BundleContext context = FrameworkUtil.getBundle(SetAmbulatoryFactorOpenCons.class).getBundleContext();
		serviceRef = context.getServiceReference(ICodeElementService.class);
		if (serviceRef != null) {
			codeElementService = context.getService(serviceRef);
			return true;
		} else {
			return false;
		}
	}

	private void deInitVatAdjuster() {
		BundleContext context = FrameworkUtil.getBundle(SetAmbulatoryFactorOpenCons.class).getBundleContext();
		if (vatServiceRef != null && !vatServiceRef.isEmpty()) {
			context.ungetService(vatServiceRef.get(0));
			vatAdjuster = null;
		}
	}

	private void initVatAdjuster() {
		try {
			BundleContext context = FrameworkUtil.getBundle(SetAmbulatoryFactorOpenCons.class).getBundleContext();
			vatServiceRef = new ArrayList<>(
					context.getServiceReferences(IBilledAdjuster.class, "(id=VatVerrechnetAdjuster)"));
			if (vatServiceRef != null && !vatServiceRef.isEmpty()) {
				vatAdjuster = context.getService(vatServiceRef.get(0));
			}
		} catch (InvalidSyntaxException e) {
			LoggerFactory.getLogger(getClass()).error("Error getting vat adjuster");
		}
	}

	private List<IBilled> getAmbulatoryOnly(List<IBilled> list) {
		List<IBilled> ret = new ArrayList<>();
		for (IBilled verrechnet : list) {
			IBillable billable = verrechnet.getBillable();
			if (billable.getCodeSystemName().contains("Ambulantepauschalen")) {
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
		return "Taxpunktwert und MWSt Info Ambulanterpauschalen aller offenen Konsultationen dieses Jahres neu setzen";
	}
}
