/*******************************************************************************
 * Copyright (c) 2017 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.data.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IFall;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.IDiagnose;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Mandant;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.elexis.data.Rechnungssteller;
import ch.elexis.data.Verrechnet;
import ch.elexis.data.dto.DiagnosesDTO;
import ch.elexis.data.dto.InvoiceCorrectionDTO;
import ch.elexis.data.dto.InvoiceHistoryEntryDTO;
import ch.elexis.data.dto.InvoiceHistoryEntryDTO.OperationType;
import ch.elexis.data.dto.KonsultationDTO;
import ch.elexis.data.dto.LeistungDTO;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;
import ch.rgw.tools.Result.msg;
import ch.rgw.tools.TimeTool;

/**
 * Util class with methods for checking and preparing {@link Konsultation}, with the goal to include
 * them in a bill {@link Rechnung#build(List)}.
 * 
 * @author thomas
 *
 */
public class BillingUtil {
	
	public static String BILLINGCHECK_ENABLED_CFG = "ch.elexis.core.data/billablecheck/";
	
	private static final Logger log = LoggerFactory.getLogger(BillingUtil.class);
	
	/**
	 * Interface definition for checking a {@link Konsultation} if it can be included on a bill.
	 * 
	 */
	public static interface IBillableCheck {
		/**
		 * Get a unique id of the check.
		 * 
		 * @return
		 */
		public String getId();
		
		/**
		 * Get a human readable description of the check.
		 * 
		 * @return
		 */
		public String getDescription();
		
		/**
		 * Test if the {@link Konsultation} is bill able. If no the error is added to the
		 * {@link Result}.
		 * 
		 * @param konsultation
		 * @param result
		 * @return
		 */
		public boolean isBillable(Konsultation konsultation, Result<Konsultation> result);
	}
	
	/**
	 * Array of {@link IBillableCheck} implementations. Implementations can be disabled or enabled
	 * using {@link BillingUtil#setCheckEnabled(IBillableCheck, boolean)}.
	 * 
	 */
	public static IBillableCheck[] billableChecks = {
		// Check for zero sales.
		new IBillableCheck() {
			@Override
			public boolean isBillable(Konsultation konsultation, Result<Konsultation> result){
				boolean fail = getTotal(konsultation).isZero();
				if (fail) {
					result.add(SEVERITY.ERROR, 1, getDescription(), konsultation, false);
				}
				return !fail;
			}
			
			@Override
			public String getId(){
				return "zeroSales";
			}
			
			@Override
			public String getDescription(){
				return "Behandlung mit Umsatz 0";
			}
		},
		// Check for invalid Mandant.
		new IBillableCheck() {
			@Override
			public boolean isBillable(Konsultation konsultation, Result<Konsultation> result){
				Mandant mandant = konsultation.getMandant();
				boolean fail = (mandant == null || !mandant.isValid());
				if (fail) {
					result.add(SEVERITY.ERROR, 1, getDescription(), konsultation, false);
				}
				return !fail;
			}
			
			@Override
			public String getId(){
				return "invalidMandant";
			}
			
			@Override
			public String getDescription(){
				return "Ungültiger Mandant";
			}
		},
		// Check for missing coverage.
		new IBillableCheck() {
			@Override
			public boolean isBillable(Konsultation konsultation, Result<Konsultation> result){
				Fall fall = konsultation.getFall();
				boolean fail = (fall == null);
				if (fail) {
					result.add(SEVERITY.ERROR, 1, getDescription(), konsultation, false);
				}
				return !fail;
			}
			
			@Override
			public String getId(){
				return "noCoverage";
			}
			
			@Override
			public String getDescription(){
				return "Fehlender Fall";
			}
		},
		// Check for invalid coverage.
		new IBillableCheck() {
			@Override
			public boolean isBillable(Konsultation konsultation, Result<Konsultation> result){
				Fall fall = konsultation.getFall();
				boolean fail = (fall != null
					&& CoreHub.userCfg.get(Preferences.LEISTUNGSCODES_BILLING_STRICT, true)
					&& !fall.isValid());
				if (fail) {
					result.add(SEVERITY.ERROR, 1, getDescription(), konsultation, false);
				}
				return !fail;
			}
			
			@Override
			public String getId(){
				return "invalidCoverage";
			}
			
			@Override
			public String getDescription(){
				return "Fall nicht gültig";
			}
		},
		// Check for missing diagnose.
		new IBillableCheck() {
			@Override
			public boolean isBillable(Konsultation konsultation, Result<Konsultation> result){
				ArrayList<IDiagnose> diagnosen = konsultation.getDiagnosen();
				boolean fail = (diagnosen == null || diagnosen.isEmpty());
				if (fail) {
					result.add(SEVERITY.ERROR, 1, getDescription(), konsultation, false);
				}
				return !fail;
			}
			
			@Override
			public String getId(){
				return "noDiagnose";
			}
			
			@Override
			public String getDescription(){
				return "Keine Diagnose";
			}
		},
		// Check for invalid date.
		new IBillableCheck() {
			private TimeTool checkTool = new TimeTool();
			
			@Override
			public boolean isBillable(Konsultation konsultation, Result<Konsultation> result){
				boolean fail = (checkTool.set(konsultation.getDatum()) == false);
				if (fail) {
					result.add(SEVERITY.ERROR, 1, getDescription(), konsultation, false);
				}
				return !fail;
			}
			
			@Override
			public String getId(){
				return "invalidDate";
			}
			
			@Override
			public String getDescription(){
				return "Ungültiges Datum";
			}
		},
		// Check for missing diagnose in open Konsultation series. 
		new IBillableCheck() {
			@Override
			public boolean isBillable(Konsultation konsultation, Result<Konsultation> result){
				boolean fail = false;
				ArrayList<IDiagnose> diagnosen = konsultation.getDiagnosen();
				if (diagnosen == null || diagnosen.isEmpty()) {
					fail = true;
					// get other open konsultation of the case
					Query<Konsultation> query = new Query<>(Konsultation.class);
					query.add(Konsultation.FLD_BILL_ID, Query.EQUALS, null);
					query.add(Konsultation.FLD_CASE_ID, Query.EQUALS,
						konsultation.getFall().getId());
					List<Konsultation> openKonsultationen = query.execute();
					for (Konsultation openKons : openKonsultationen) {
						ArrayList<IDiagnose> diag = openKons.getDiagnosen();
						if (diag != null && !diag.isEmpty()) {
							fail = false;
							break;
						}
					}
					if (fail) {
						result.add(SEVERITY.ERROR, 1, getDescription(), konsultation, false);
					}
				}
				return !fail;
			}
			
			@Override
			public String getId(){
				return "noDiagnoseInSeries";
			}
			
			@Override
			public String getDescription(){
				return "Keine Diagnose in der Behandlungsserie";
			}
		}
	};
	
	public static boolean isCheckEnabled(IBillableCheck check){
		return CoreHub.globalCfg.get(BILLINGCHECK_ENABLED_CFG + check.getId(), true);
	}
	
	public static void setCheckEnabled(IBillableCheck check, boolean enabled){
		CoreHub.globalCfg.set(BILLINGCHECK_ENABLED_CFG + check.getId(), enabled);
	}
	
	/**
	 * Test if the {@link Konsultation} can be billed, and return a {@link Result} containing
	 * possible error messages. {@link IBillableCheck} are applied if enabled.
	 * 
	 * @param konsultation
	 * @return
	 */
	public static Result<Konsultation> getBillableResult(Konsultation konsultation){
		
		Result<Konsultation> result = new Result<>(konsultation);
		
		for (IBillableCheck iBillableCheck : billableChecks) {
			if (isCheckEnabled(iBillableCheck)) {
				iBillableCheck.isBillable(konsultation, result);
			}
		}
		return result;
	}
	
	/**
	 * Calculate the total amount of all {@link Verrechnet} of the {@link Konsultation}.
	 * 
	 * @param konsultation
	 * @return
	 */
	public static Money getTotal(Konsultation konsultation){
		Money total = new Money(0);
		List<Verrechnet> leistungen = konsultation.getLeistungen();
		for (Verrechnet verrechnet : leistungen) {
			total.addMoney(verrechnet.getNettoPreis().multiply(verrechnet.getZahl()));
		}
		return total;
	}
	
	/**
	 * Remove all not bill able {@link Konsultation} from the provided {@link List}.
	 * 
	 * @param konsultationen
	 * @return filtered {@link List}
	 */
	public static List<Konsultation> filterNotBillable(List<Konsultation> konsultationen){
		return konsultationen.parallelStream().filter(k -> getBillableResult(k).isOK())
			.collect(Collectors.toList());
	}
	
	/**
	 * Get a Map representation of bill able {@link Konsultation} instances. To be bill able the
	 * list of {@link Konsultation} is split by {@link Rechnungssteller} and {@link Fall}.
	 * 
	 * @param konsultationen
	 * @return map sorted by billing criteria
	 */
	public static Map<Rechnungssteller, Map<Fall, List<Konsultation>>> getGroupedBillable(
		List<Konsultation> konsultationen){
		HashMap<Rechnungssteller, Map<Fall, List<Konsultation>>> ret = new HashMap<>();
		for (Konsultation konsultation : konsultationen) {
			Rechnungssteller invoicer = konsultation.getMandant().getRechnungssteller();
			Map<Fall, List<Konsultation>> fallMap = ret.get(invoicer);
			if (fallMap == null) {
				fallMap = new HashMap<>();
			}
			List<Konsultation> list = fallMap.get(konsultation.getFall());
			if (list == null) {
				list = new ArrayList<>();
			}
			list.add(konsultation);
			fallMap.put(konsultation.getFall(), list);
			ret.put(invoicer, fallMap);
		}
		return ret;
	}
	
	/**
	 * Create bills {@link Rechnung} for all {@link Konsultation} contained in the map. Returns al
	 * list with the {@link Result} of building the bills.
	 * 
	 * @param toBillMap
	 * @return
	 */
	public static List<Result<Rechnung>> createBills(
		Map<Rechnungssteller, Map<Fall, List<Konsultation>>> toBillMap){
		List<Result<Rechnung>> ret = new ArrayList<>();
		Set<Rechnungssteller> invoicers = toBillMap.keySet();
		for (Rechnungssteller invoicer : invoicers) {
			Set<Fall> faelle = toBillMap.get(invoicer).keySet();
			for (Fall fall : faelle) {
				ret.add(Rechnung.build(toBillMap.get(invoicer).get(fall)));
			}
		}
		return ret;
	}
	
	/**
	 * Returns only Konsultations from the same year
	 * 
	 * @param konsultations
	 * @return
	 */
	public static List<Konsultation> getKonsultationsFromSameYear(List<Konsultation> konsultations){
		List<Konsultation> items = new ArrayList<>();
		// only kons from the same year can be inside in a same bill
		int year = 0;
		for (Konsultation b : konsultations) {
			if (year == 0) {
				year = new TimeTool(b.getDatum()).get(TimeTool.YEAR);
			}
			if (year == new TimeTool(b.getDatum()).get(TimeTool.YEAR)) {
				items.add(b);
			}
		}
		return items;
	}
	
	/**
	 * Copies the actual fall, merge the copied fall with changes, transfer cons, storno the old
	 * invoice
	 */
	public static void doBillCorrection(InvoiceCorrectionDTO invoiceCorrectionDTO,
		BillCallback billCallback){
		
		BillCorrection billCorrection = new BillCorrection(invoiceCorrectionDTO, billCallback);
		billCorrection.doCorrection();

	}
	
	public interface BillCallback {
		public List<Konsultation> storno(Rechnung rechnung);
	}
	
	/**
	 * Base class for invoice correction
	 * 
	 * @author med1
	 *
	 */
	private static class BillCorrection {
		private boolean success = true;
		private StringBuilder output = new StringBuilder();
		private final Rechnung rechnung;
		private Optional<Fall> srcFall = Optional.empty();
		private Optional<Fall> copyFall = Optional.empty();
		private List<Konsultation> releasedKonsultations = new ArrayList<>();
		private LeistungDTO leistungDTO = null;
		private DiagnosesDTO diagnosesDTO = null;
		private Konsultation konsultation = null;
		private Verrechnet verrechnet = null;
		private List<IPersistentObject> locks = new ArrayList<>();
		private final InvoiceCorrectionDTO invoiceCorrectionDTO;
		private final BillCallback billCallback;
		
		public BillCorrection(InvoiceCorrectionDTO invoiceCorrectionDTO, BillCallback billCallback){
			this.invoiceCorrectionDTO = invoiceCorrectionDTO;
			this.rechnung = Rechnung.load(invoiceCorrectionDTO.getId());
			this.billCallback = billCallback;
		}
		
		public void doCorrection(){
			for (InvoiceHistoryEntryDTO historyEntryDTO : invoiceCorrectionDTO.getHistory()) {
				try {
					if (success) {
						Object base = historyEntryDTO.getBase();
						Object item = historyEntryDTO.getItem();
						Object additional = historyEntryDTO.getAdditional();
						
						OperationType operationType = historyEntryDTO.getOperationType();
						log.debug("invoice correction: processing [{}] start ", operationType);
						// storno
						switch (operationType) {
						case RECHNUNG_STORNO:
							if (!stornoBill()) {
								return;
							}
							break;
						case RECHNUNG_NEW:
							createBill(historyEntryDTO);
							break;
						case FALL_COPY:
							copyFall();
							break;
						case FALL_CHANGE:
							changeFall();
							break;
						case FALL_KONSULTATION_TRANSER:
							transferKonsultations();
							break;
						case KONSULTATION_CHANGE_DATE:
							changeDateKonsultation(base);
							break;
						case KONSULTATION_CHANGE_MANDANT:
							changeMandantKonsultation(base);
							break;
						case KONSULTATION_TRANSFER_TO_FALL:
							transferKonsultation(base, item);
							break;
						case LEISTUNG_ADD:
							addLeistung(base, item);
							break;
						case LEISTUNG_REMOVE:
							removeLeistung(base, item);
							break;
						case LEISTUNG_TRANSFER_TO_FALL_KONS:
							transferLeistungen(base, item, additional);
							break;
						case LEISTUNG_CHANGE_COUNT:
							changeCountLeistung(item);
							break;
						case LEISTUNG_CHANGE_PRICE:
							changePriceLeistung(item);
							break;
						case DIAGNOSE_ADD:
							addDiagnose(base, item);
							break;
						case DIAGNOSE_REMOVE:
							removeDiagnose(base, item);
							break;
						default:
							break;
						}
						invoiceCorrectionDTO.setOutputText(output.toString());
					}
				} catch (Exception e) {
					log.error("invoice correction: unexpected error", e);
					success = false;
				} finally {
					log.debug("invoice correction: processing [{}] [{}] ",
						historyEntryDTO.getOperationType(),
						historyEntryDTO.isIgnored() ? "ignored" : (success ? "success" : "failed"));
					historyEntryDTO.setSuccess(success);
				}
			}
			
			log.debug("release all locks: " + locks.size());
			for (IPersistentObject po : locks) {
				CoreHub.getLocalLockService().releaseLock(po);
			}
		}

		private void removeDiagnose(Object base, Object item){
			konsultation = Konsultation.load(((KonsultationDTO) base).getId());
			diagnosesDTO = (DiagnosesDTO) item;
			konsultation.removeDiagnose(diagnosesDTO.getiDiagnose());
			log.debug(
				"invoice correction: removed diagnose id [{}] from kons id [{}]",
				diagnosesDTO.getId(), konsultation.getId());
		}

		private void addDiagnose(Object base, Object item){
			konsultation = Konsultation.load(((KonsultationDTO) base).getId());
			diagnosesDTO = (DiagnosesDTO) item;
			konsultation.addDiagnose(diagnosesDTO.getiDiagnose());
			log.debug("invoice correction: added diagnose id [{}] to kons id [{}]",
				diagnosesDTO.getId(), konsultation.getId());
		}

		private void changePriceLeistung(Object item){
			leistungDTO = (LeistungDTO) item;
			verrechnet = leistungDTO.getVerrechnet();
			if (verrechnet != null) {
				acquireLock(locks, verrechnet, false);
				int tp = leistungDTO.getTp();
				int tpOld = Verrechnet
					.checkZero(verrechnet.get(Verrechnet.SCALE_TP_SELLING));
				verrechnet.setSecondaryScaleFactor(leistungDTO.getScale2());
				if (tpOld != tp) {
					verrechnet.setTP(tp);
					log.debug(
						"invoice correction: price changed to [{}] for leistung id [{}]",
						leistungDTO.getPrice().getAmountAsString(),
						leistungDTO.getId());
				}
			} else {
				log.warn(
					"invoice correction: leistung id [{}] no verrechnet exists cannot change price",
					leistungDTO.getId());
			}
		}

		private void changeCountLeistung(Object item){
			leistungDTO = (LeistungDTO) item;
			verrechnet = leistungDTO.getVerrechnet();
			if (verrechnet != null) {
				acquireLock(locks, verrechnet, false);
				IStatus ret =
					verrechnet.changeAnzahlValidated(leistungDTO.getCount());
				log.debug("invoice correction: changed count from leistung id [{}]",
					leistungDTO.getId());
				if (ret.isOK()) {
					verrechnet.setSecondaryScaleFactor(leistungDTO.getScale2());
				} else {
					addToOutput(output, ret.getMessage());
					success = false;
					log.warn(
						"invoice correction: cannot change count from leistung with id [{}]",
						leistungDTO.getId());
				}
			}
		}
		
		public void transferKonsultation(Object base, Object item){
			KonsultationDTO konsultationDTO = (KonsultationDTO) base;
			Fall fallToTransfer = Fall.load(((IFall) item).getId());
			
			log.debug("invoice correction: transfer kons with id [{}] to fall id [{}]",
				konsultationDTO.getId(), fallToTransfer.getId());
			
			Konsultation konsultation = Konsultation.load(konsultationDTO.getId());
			
			Fall oldFall = konsultation.getFall();
			
			acquireLock(locks, oldFall, false);
			acquireLock(locks, fallToTransfer, false);
			acquireLock(locks, konsultation, false);
			
			konsultation.transferToFall(fallToTransfer, true, false);
			
			Iterator<Konsultation> it = releasedKonsultations.iterator();
			while (it.hasNext()) {
				Konsultation k = it.next();
				if (konsultation.getId().equals(k.getId())) {
					it.remove();
					log.debug(
						"invoice correction: removed transfered kons with id [{}] from released konsultations",
						k.getId());
				}
			}
			
			log.debug(
				"invoice correction: transfered kons id [{}] from fall id [{}] to fall id  [{}]",
				konsultation.getId(), oldFall.getId(), fallToTransfer.getId());
		}

		private void transferLeistungen(Object base, Object item, Object additional){
			@SuppressWarnings("unchecked")
			List<LeistungDTO> leistungenDTOs = (List<LeistungDTO>) item;
			konsultation = Konsultation.load(((KonsultationDTO) base).getId());
			Fall fallToTransfer = Fall.load(((IFall) additional).getId());
			List<LeistungDTO> removedleistungDTOs = new ArrayList<>();
			for (LeistungDTO itemLeistung : leistungenDTOs) {
				log.debug(
					"invoice correction: transfer leistung id [{}] from kons id [{}]",
					itemLeistung.getId(), konsultation.getId());
				if (itemLeistung.getVerrechnet() != null) {
					
					acquireLock(locks, itemLeistung.getVerrechnet(), false);
					Result<Verrechnet> resRemove =
						konsultation.removeLeistung(itemLeistung.getVerrechnet());
					
					log.debug(
						"invoice correction: removed leistung id [{}] from kons id [{}]",
						itemLeistung.getId(), konsultation.getId());
					if (resRemove.isOK()) {
						itemLeistung.setVerrechnet(null);
						removedleistungDTOs.add(itemLeistung);
					} else {
						addToOutput(output, "Die Leistung "
							+ itemLeistung.getVerrechnet().getText()
							+ " konnte nicht auf einen neuen Fall/Konsultation transferiert werden. Das Entfernen der Leistung ist fehlgeschlagen.");
						success = false;
						log.warn(
							"invoice correction: cannot transfer/remove leistung with id [{}] from kons id [{}]",
							itemLeistung.getId(), konsultation.getId());
					}
				} else {
					removedleistungDTOs.add(itemLeistung);
				}
			}
			if (!removedleistungDTOs.isEmpty()) {
				Konsultation newKons =
					konsultation.createCopy(fallToTransfer, rechnung);
				acquireLock(locks, newKons, true);
				log.debug(
					"invoice correction: copied kons from id [{}] to kons id [{}] and added kons to fall id [{}] ",
					konsultation.getId(), newKons.getId(),
					newKons.getFall().getId());
				for (LeistungDTO itemLeistung : removedleistungDTOs) {
					Result<IVerrechenbar> resAddLeistung =
						newKons.addLeistung(itemLeistung.getIVerrechenbar());
					log.debug(
						"invoice correction: add leistung id [{}] to kons id [{}]",
						itemLeistung.getId(), newKons.getId());
					if (resAddLeistung.isOK()) {
						verrechnet =
							newKons.getVerrechnet(itemLeistung.getIVerrechenbar());
						if (verrechnet != null) {
							itemLeistung.setVerrechnet(verrechnet);
							if (verrechnet.getZahl() != itemLeistung.getCount()) {
								IStatus ret = verrechnet
									.changeAnzahlValidated(itemLeistung.getCount());
								log.debug(
									"invoice correction: count changed from [{}] to {[]} - for leistung id [{}]",
									itemLeistung.getId());
								if (ret.isOK()) {
									verrechnet.setSecondaryScaleFactor(
										itemLeistung.getScale2());
								} else {
									verrechnet = null;
									log.warn(
										"invoice correction: cannot change count for leistung with id [{}]",
										itemLeistung.getId());
								}
							}
						}
					} else {
						addToOutput(output, resAddLeistung);
						verrechnet = null;
					}
					if (verrechnet == null) {
						addToOutput(output, "Die Leistung "
							+ itemLeistung.getIVerrechenbar().getText()
							+ " konnte nicht auf einen neuen Fall/Konsultation transferiert werden. Das Hinzufügen der Leistung ist fehlgeschlagen.");
						success = false;
						log.warn(
							"invoice correction: cannot transfer/add leistung with id [{}] to new kons id [{}]",
							itemLeistung.getId(), newKons.getId());
					}
				}
			}
			if (!success) {
				addToOutput(output,
					"Nicht alle Leistungen konnten erfolgreich transferiert werden.");
				log.warn(
					"invoice correction: not all leistungen could be transfered.");
			}
		}

		private void removeLeistung(Object base, Object item){
			leistungDTO = (LeistungDTO) item;
			if (leistungDTO.getVerrechnet() != null) {
				acquireLock(locks, leistungDTO.getVerrechnet(), false);
				Result<Verrechnet> resRemove =
					Konsultation.load(((KonsultationDTO) base).getId())
						.removeLeistung(leistungDTO.getVerrechnet());
				log.debug(
					"invoice correction: removed leistung id [{}] from kons id [{}]",
					leistungDTO.getId(), ((KonsultationDTO) base).getId());
				if (resRemove.isOK()) {
					((LeistungDTO) item).setVerrechnet(null);
				} else {
					addToOutput(output,
						"Die Leistung " + leistungDTO.getVerrechnet().getText()
							+ " konnte nicht entfernt werden.");
					success = false;
					log.warn(
						"invoice correction: cannot remove leistung with id [{}] from kons id [{}]",
						leistungDTO.getId(), ((KonsultationDTO) base).getId());
				}
			}
		}

		private void addLeistung(Object base, Object item){
			konsultation = Konsultation.load(((KonsultationDTO) base).getId());
			leistungDTO = (LeistungDTO) item;
			Result<IVerrechenbar> res =
				konsultation.addLeistung(leistungDTO.getIVerrechenbar());
			log.debug("invoice correction: added leistung id [{}] to kons id [{}]",
				leistungDTO.getId(), ((KonsultationDTO) base).getId());
			if (res.isOK()) {
				verrechnet =
					konsultation.getVerrechnet(leistungDTO.getIVerrechenbar());
				if (verrechnet != null) {
					leistungDTO.setVerrechnet(verrechnet);
					acquireLock(locks, verrechnet, false);
				}
			} else {
				addToOutput(output, res);
				verrechnet = null;
			}
			if (verrechnet == null) {
				addToOutput(output,
					"Die Leistung " + leistungDTO.getIVerrechenbar().getText()
						+ " konnte nicht verrechnet werden.");
				success = false;
				log.warn(
					"invoice correction: cannot add leistung with id [{}] to kons id [{}]",
					leistungDTO.getId(), konsultation.getId());
			}
		}

		private void changeMandantKonsultation(Object base){
			Konsultation.load(((KonsultationDTO) base).getId())
				.setMandant(((KonsultationDTO) base).getMandant());
			log.debug("invoice correction: changed mandant of kons id [{}]",
				((KonsultationDTO) base).getId());
		}

		private void changeDateKonsultation(Object base){
			Konsultation.load(((KonsultationDTO) base).getId())
				.setDatum(((KonsultationDTO) base).getDate(), true);
			log.debug("invoice correction: changed date of kons id [{}]",
				((KonsultationDTO) base).getId());
		}

		private void transferKonsultations(){
			releasedKonsultations.clear();
			Konsultation[] consultations = srcFall.get().getBehandlungen(true);
			if (consultations != null) {
				for (Konsultation openedKons : consultations) {
					if (openedKons.exists()) {
						Rechnung bill = openedKons.getRechnung();
						if (bill == null) {
							openedKons.transferToFall(copyFall.get(), true, false);
							log.debug(
								"invoice correction: transfered kons id [{}] to copied fall id  [{}] ",
								openedKons.getId(), copyFall.get().getId());
							releasedKonsultations.add(openedKons);
							
							// if validation of cons is failed the bill correction will be reseted
							Result<?> result =
								BillingUtil.getBillableResult(openedKons);
							if (!result.isOK()) {
								StringBuilder preValidatioWarnings = new StringBuilder();
								addToOutput(preValidatioWarnings, result);
								log.warn(
									"invoice correction: konsultation prevalidation failed - the invoice correction will be continued - because the current correction could fix it. Message: [{}]",
									preValidatioWarnings.toString());
							}
						}
					}
				}
			}
		}

		private void changeFall() throws ElexisException{
			copyFall.get().persistDTO(invoiceCorrectionDTO.getFallDTO());
			// at this point the fall must be opened
			copyFall.get().setEndDatum(null);
			log.debug("invoice correction: persisted fall changes to id  [{}] ",
				copyFall.get().getId());
		}

		private void copyFall(){
			srcFall = Optional.of(rechnung.getFall());
			copyFall = Optional.of(srcFall.get().createCopy());
			acquireLock(locks, copyFall.get(), true);
			log.debug("invoice correction: copied fall from id [{}] to id [{}] ",
				srcFall.get().getId(), copyFall.get().getId());
		}

		private void createBill(InvoiceHistoryEntryDTO historyEntryDTO){
			if (copyFall.isPresent()) {
				if (invoiceCorrectionDTO.getFallDTO().getEndDatum() != null) {
					copyFall.get().setEndDatum(
						invoiceCorrectionDTO.getFallDTO().getEndDatum());
					acquireLock(locks, copyFall.get(), false);
				}
				
				// close fall if no kons exists
				if ((srcFall.get().isOpen()
					|| new TimeTool(srcFall.get().getEndDatum())
						.after(new TimeTool()))
					&& srcFall.get().getBehandlungen(true).length == 0) {
					acquireLock(locks, srcFall.get(), false);
					srcFall.get()
						.setEndDatum(new TimeTool().toString(TimeTool.DATE_GER));
				}
			}
			if (releasedKonsultations.isEmpty()) {
				log.debug(
					"invoice correction: no konsultations exists for invoice id [{}]- a new invoice will not be created.",
					rechnung.getNr());
				output.append("Die Rechnung " + rechnung.getNr() + " wurde erfolgreich durch "
					+ CoreHub.actUser.getLabel()
					+ " korrigiert.\nEs wurde keine neue Rechnung erstellt.");
				historyEntryDTO.setIgnored(true);
			}
			else {
			
				Result<Rechnung> rechnungResult = Rechnung.build(releasedKonsultations);
				if (!rechnungResult.isOK()) {
					
					for (@SuppressWarnings("rawtypes")
					msg message : rechnungResult.getMessages()) {
						if (message.getSeverity() != SEVERITY.OK) {
							if (output.length() > 0) {
								output.append("\n");
							}
							output.append(message.getText());
						}
					}
					success = false;
					log.error("invoice correction: error cannot create new invoice with id "
						+ (rechnungResult.get() != null ? rechnungResult.get().getId() : "null"));
					log.error("invoice correction: error details: " + output.toString());
				} else {
					Rechnung newRechnung = rechnungResult.get();
					invoiceCorrectionDTO.setNewInvoiceNumber(newRechnung.getNr());
					log.debug(
						"invoice correction: create new invoice with number [{}] old invoice number [{}] ",
						newRechnung.getNr(), rechnung.getNr());
					output.append("Die Rechnung " + rechnung.getNr() + " wurde erfolgreich durch "
						+ CoreHub.actUser.getLabel() + " korrigiert.\nNeue Rechnungsnummer lautet: "
						+ invoiceCorrectionDTO.getNewInvoiceNumber());
				}
			}
		}

		private boolean stornoBill(){
			List<Konsultation> konsultations = billCallback.storno(rechnung);
			if (konsultations != null) {
				releasedKonsultations.addAll(konsultations);
			} else {
				success = false;
			}
			
			log.debug("invoice correction: storno invoice with number [{}] ",
				rechnung.getNr());
			return true;
		}
		
		private void addToOutput(StringBuilder output, Result<?> res){
			StringBuilder warnings = new StringBuilder();
			for (@SuppressWarnings("rawtypes")
			msg message : res.getMessages()) {
				if (message.getSeverity() != SEVERITY.OK) {
					if (output.length() > 0) {
						warnings.append(" / ");
					}
					warnings.append(message.getText());
				}
			}
			if (warnings.length() > 0) {
				output.append(warnings.toString());
			}
		}
		
		private void addToOutput(StringBuilder output, String warning){
			if (output.length() > 0) {
				output.append("\n");
			}
			if (warning.length() > 0) {
				output.append(warning);
			}
		}
		
		private boolean acquireLock(List<IPersistentObject> currentLocks,
			IPersistentObject persistentObjectToLock, boolean forceReleaseLock){
			if (!currentLocks.contains(persistentObjectToLock)) {
				if (CoreHub.getLocalLockService().acquireLock(persistentObjectToLock).isOk()) {
					if (!forceReleaseLock) {
						currentLocks.add(persistentObjectToLock);
					} else {
						CoreHub.getLocalLockService().releaseLock(persistentObjectToLock);
					}
					return true;
				}
			}
			return false;
		}

	}
}
