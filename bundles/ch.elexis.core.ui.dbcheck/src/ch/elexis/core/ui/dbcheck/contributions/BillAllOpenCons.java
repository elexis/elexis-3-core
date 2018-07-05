package ch.elexis.core.ui.dbcheck.contributions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.ui.dbcheck.contributions.dialogs.SelectBillingStrategyDialog;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.elexis.data.Rechnungssteller;
import ch.rgw.tools.TimeTool;

public class BillAllOpenCons extends ExternalMaintenance {
	
	public enum BillStrategies implements IBillStrategy {
			DEFAULT {
				@Override
				public int start(IProgressMonitor monitor){
					Integer count = 0;
					
					Query<Fall> qbe = new Query<Fall>(Fall.class);
					List<Fall> qre = qbe.execute();
					
					monitor.beginTask("Offene Konsultationen abrechnen und Fälle schliessen",
						qre.size());
					TimeTool now = new TimeTool();
					for (Fall fall : qre) {
						if (fall.isOpen()) {
							Rechnung.build(Arrays.asList(fall.getBehandlungen(false)));
							fall.setEndDatum(now.toString(TimeTool.DATE_GER));
							count++;
						}
						monitor.worked(1);
						if (monitor.isCanceled()) {
							return count;
						}
					}
					monitor.done();
					return count;
				}
				
				@Override
				public List<String> getProblems(){
					return Collections.emptyList();
				}
			},
			VITODATA {
				private List<String> fallProblems;
				
				@Override
				public int start(IProgressMonitor monitor){
					Integer count = 0;
					fallProblems = new ArrayList<>();
					
					Query<Fall> qbe = new Query<Fall>(Fall.class);
					List<Fall> faelle = qbe.execute();
					
					monitor.beginTask(
						"Offene, von Vitodata importierte, Konsultationen abrechnen und Fälle schliessen",
						faelle.size());
					for (Fall fall : faelle) {
						// check cancel first
						if (monitor.isCanceled()) {
							return count;
						} else {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// ignore ...
							}
						}
						
						if(isEmpty(fall)) {
							monitor.worked(1);
							continue;
						} else {
							FallConsInfo info = getFallConsInfo(fall);
							if(info.isVitoOnly()) {
								if (!isBilled(fall)) {
									createBill(fall);
									count++;
								}
								monitor.worked(1);
							} else if (info.isNonVitoOnly()) {
								monitor.worked(1);
								continue;
							} else {
								addFallProblem("Mixed cons in Fall", fall);
								monitor.worked(1);
								continue;
							}
						}
					}
					
					monitor.done();
					return count;
				}
				
				private void addFallProblem(String prefix, Fall fall){
					fallProblems.add("[" + prefix + "]" + "[" + fall.getId() + "] - ["
						+ fall.getLabel() + "] von [" + fall.getPatient().getLabel() + "]");
				}
				
				private void createBill(Fall fall){
					LockResponse lr = CoreHub.getLocalLockService().acquireLockBlocking(fall, 60,
						new NullProgressMonitor());
					if (lr.isOk()) {
						HashMap<Rechnungssteller, List<Konsultation>> consByRechnungssteller =
							getConsultationByRechnungssteller(fall);
						Set<Rechnungssteller> keys = consByRechnungssteller.keySet();
						for (Rechnungssteller key : keys) {
							Rechnung.build(consByRechnungssteller.get(key));
						}
						fall.setEndDatum(new TimeTool().toString(TimeTool.DATE_GER));
						CoreHub.getLocalLockService().releaseLock((fall));
						if (!isBilled(fall)) {
							addFallProblem("Billing failed", fall);
						}
					} else {
						addFallProblem("No lock", fall);
					}
				}
				
				private HashMap<Rechnungssteller, List<Konsultation>> getConsultationByRechnungssteller(
					Fall fall){
					HashMap<Rechnungssteller, List<Konsultation>> ret = new HashMap<>();
					List<Konsultation> fallConsultations =
						Arrays.asList(fall.getBehandlungen(false));
					for (Konsultation consultation : fallConsultations) {
						Rechnungssteller rs = consultation.getMandant().getRechnungssteller();
						List<Konsultation> list = ret.get(rs);
						if (list == null) {
							list = new ArrayList<>();
						}
						list.add(consultation);
						ret.put(rs, list);
					}
					return ret;
				}
				
				private boolean isBilled(Fall fall){
					List<Konsultation> fallConsultations =
						Arrays.asList(fall.getBehandlungen(false));
					if (!fallConsultations.isEmpty()) {
						String billId = fallConsultations.get(0).get(Konsultation.FLD_BILL_ID);
						return billId != null && !billId.isEmpty();
					}
					return true;
				}
				
				private boolean isEmpty(Fall fall){
					List<Konsultation> fallConsultations =
						Arrays.asList(fall.getBehandlungen(false));
					return fallConsultations.isEmpty();
				}
				
				private FallConsInfo getFallConsInfo(Fall fall){
					List<Konsultation> fallConsultations =
						Arrays.asList(fall.getBehandlungen(false));
					FallConsInfo ret = new FallConsInfo();
					for (Konsultation consultation : fallConsultations) {
						if (isVitoConsultation(consultation)) {
							ret.vitoCount++;
						} else {
							ret.nonVitoCount++;
						}
					}
					return ret;
				}
				
				private boolean isVitoConsultation(Konsultation consultation){
					return !consultation.getXid("www.elexis.info/vitodata/import/consultation/id")
						.isEmpty();
				}
				
				@Override
				public List<String> getProblems(){
					return fallProblems != null ? fallProblems : Collections.emptyList();
				}
			}
	}
	
	private static class FallConsInfo {
		int vitoCount = 0;
		int nonVitoCount = 0;
		
		public boolean isVitoOnly(){
			return nonVitoCount == 0;
		}
		
		public boolean isNonVitoOnly(){
			return vitoCount == 0;
		}
	}
	
	private SelectBillingStrategyDialog dialog;
	
	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion){
		Integer count = 0;
		List<String> problems = null;
		
		// make sure not billing strict
		boolean presetBillingStrict =
			CoreHub.userCfg.get(Preferences.LEISTUNGSCODES_BILLING_STRICT, false);
		CoreHub.userCfg.set(Preferences.LEISTUNGSCODES_BILLING_STRICT, false);
		
		Display display = Display.getDefault();
		if (display != null) {
			display.syncExec(() -> {
				dialog = new SelectBillingStrategyDialog(display.getActiveShell());
				dialog.open();
			});
			IBillStrategy strategy = dialog.getStrategy();
			if (strategy != null) {
				count = strategy.start(pm);
				problems = strategy.getProblems();
			}
		}
		
		CoreHub.userCfg.set(Preferences.LEISTUNGSCODES_BILLING_STRICT, presetBillingStrict);
		
		return "[" + count + "] Fälle abgerechnet" + getProblemsString(problems);
	}
	
	private String getProblemsString(List<String> problems){
		if (problems != null && !problems.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("\nProblems:\n");
			problems.stream().forEach(problem -> sb.append(problem + "\n"));
			return sb.toString();
		}
		return "";
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "Alle offenen Konsutlationen abrechnen und Fälle schliessen.";
	}
	
	public static interface IBillStrategy {
		public int start(IProgressMonitor monitor);
		
		public List<String> getProblems();
	}
}
