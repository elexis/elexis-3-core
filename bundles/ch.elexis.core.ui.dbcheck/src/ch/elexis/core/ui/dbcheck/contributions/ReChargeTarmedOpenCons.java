package ch.elexis.core.ui.dbcheck.contributions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.ICodeElementService.ContextKeys;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Query;
import ch.elexis.data.Verrechnet;
import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;

public class ReChargeTarmedOpenCons extends ExternalMaintenance {
	
	private List<String> problems = new ArrayList<>();
	
	protected ICodeElementService codeElementService;
	
	private ServiceReference<ICodeElementService> serviceRef;
	
	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion){
		Integer count = 0;
		
		if (initCodeElementService()) {
			// make sure not billing strict
			boolean presetBillingStrict =
				CoreHub.userCfg.get(Preferences.LEISTUNGSCODES_BILLING_STRICT, false);
			CoreHub.userCfg.set(Preferences.LEISTUNGSCODES_BILLING_STRICT, false);
			
			TimeTool beginOfYear = new TimeTool();
			beginOfYear.set(TimeTool.MONTH, 0);
			beginOfYear.set(TimeTool.DAY_OF_MONTH, 1);
			List<Konsultation> consultations = getKonsultation(beginOfYear);
			pm.beginTask("Bitte warten, Tarmed Leistungen werden neu verrechnet",
				consultations.size());
			for (Konsultation konsultation : consultations) {
				if (pm.isCanceled()) {
					addProblem("Cancelled.", konsultation);
					return getProblemsString();
				}
				
				List<Verrechnet> verrechnete = konsultation.getLeistungen();
				List<Verrechnet> tarmedVerrechnet = getTarmedOnly(verrechnete);
				for (Verrechnet tarmedVerr : tarmedVerrechnet) {
					IVerrechenbar verrechenbar = tarmedVerr.getVerrechenbar();
					if (verrechenbar != null) {
						// make sure we verrechenbar is matching for the kons
						Optional<ICodeElement> matchingVerrechenbar =
							codeElementService.createFromString(verrechenbar.getCodeSystemName(),
								verrechenbar.getCode(), getContext(konsultation));
						if (matchingVerrechenbar.isPresent()) {
							int amount = tarmedVerr.getZahl();
							removeVerrechnet(konsultation, tarmedVerr);
							addVerrechnet(konsultation, matchingVerrechenbar, amount);
						} else {
							addProblem("Could not find matching Verrechenbar for ["
								+ verrechenbar.getCodeSystemName() + "->" + verrechenbar.getCode()
								+ "]", konsultation);
						}
					} else {
						addProblem(
							"Could not find Verrechenbar for [" + tarmedVerr.getLabel() + "]",
							konsultation);
					}
				}
				count++;
				pm.worked(1);
			}
			
			CoreHub.userCfg.set(Preferences.LEISTUNGSCODES_BILLING_STRICT, presetBillingStrict);
			
			pm.done();
			deInitCodeElementService();
		}
		
		return "Tarmed Leistungen von [" + count + "] Konsultationen neu verrechnet"
			+ getProblemsString();
	}
	
	private void addVerrechnet(Konsultation konsultation,
		Optional<ICodeElement> matchingVerrechenbar, int amount){
		// no locking required, PersistentObject create events are passed to server (RH)
		for (int i = 0; i < amount; i++) {
			Result<IVerrechenbar> addRes =
				konsultation.addLeistung((IVerrechenbar) matchingVerrechenbar.get());
			if (!addRes.isOK()) {
				addProblem("Could not add Verrechenbar [" + matchingVerrechenbar.get().getCode()
					+ "]" + "[" + addRes.toString() + "]", konsultation);
			}
		}
	}
	
	private void removeVerrechnet(Konsultation konsultation, Verrechnet tarmedVerr){
		// acquire lock before removing
		LockResponse result = CoreHub.getLocalLockService().acquireLockBlocking(tarmedVerr, 10,
			new NullProgressMonitor());
		if (result.isOk()) {
			Result<Verrechnet> removeRes = konsultation.removeLeistung(tarmedVerr);
			if (!removeRes.isOK()) {
				addProblem("Could not remove Verrechnet [" + tarmedVerr.getLabel() + "]" + "["
					+ removeRes.toString() + "]", konsultation);
			}
			LockResponse releaseLock =
				CoreHub.getLocalLockService().releaseLock(result.getLockInfo());
			if (!releaseLock.isOk()) {
				addProblem("Could not release lock for Verrechnet [" + tarmedVerr.getLabel() + "]"
					+ "[" + removeRes.toString() + "]", konsultation);
			}
		} else {
			addProblem("Could not remove Verrechnet [" + tarmedVerr.getLabel() + "]"
				+ "[ could not acquire lock ]", konsultation);
		}
	}
	
	private HashMap<Object, Object> getContext(Konsultation consultation){
		HashMap<Object, Object> ret = new HashMap<>();
		if (consultation != null) {
			ret.put(ContextKeys.CONSULTATION, consultation);
			IPersistentObject coverage = consultation.getFall();
			if (coverage != null) {
				ret.put(ContextKeys.COVERAGE, coverage);
			}
		}
		return ret;
	}
	
	private void deInitCodeElementService(){
		BundleContext context =
			FrameworkUtil.getBundle(ReChargeTarmedOpenCons.class).getBundleContext();
		if (serviceRef != null) {
			context.ungetService(serviceRef);
			codeElementService = null;
		}
	}
	
	private boolean initCodeElementService(){
		BundleContext context =
			FrameworkUtil.getBundle(ReChargeTarmedOpenCons.class).getBundleContext();
		serviceRef = context.getServiceReference(ICodeElementService.class);
		if (serviceRef != null) {
			codeElementService = context.getService(serviceRef);
			return true;
		} else {
			return false;
		}
	}
	
	private List<Verrechnet> getTarmedOnly(List<Verrechnet> verrechnete){
		List<Verrechnet> ret = new ArrayList<>();
		for (Verrechnet verrechnet : verrechnete) {
			String klasse = verrechnet.get(Verrechnet.CLASS);
			if (klasse.endsWith("TarmedLeistung")) {
				ret.add(verrechnet);
			}
		}
		return ret;
	}
	
	public static List<Konsultation> getKonsultation(TimeTool from){
		Query<Konsultation> qbe = new Query<Konsultation>(Konsultation.class);
		qbe.add(Konsultation.DATE, Query.GREATER_OR_EQUAL, from.toString(TimeTool.DATE_COMPACT));
		return qbe.execute().stream().filter(c -> c.isEditable(false)).collect(Collectors.toList());
	}
	
	private String getProblemsString(){
		if (problems != null && !problems.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("\nProblems:\n");
			problems.stream().forEach(problem -> sb.append(problem + "\n"));
			return sb.toString();
		}
		return "";
	}
	
	private void addProblem(String prefix, Konsultation cons){
		problems.add("[" + prefix + "]" + "[" + cons.getId() + "] - [" + cons.getLabel() + "] of ["
			+ cons.getFall().getPatient().getLabel() + "]");
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "Tarmed Leistungen aller offenen Konsutlationen dieses Jahres neu verrechnen.";
	}
}
