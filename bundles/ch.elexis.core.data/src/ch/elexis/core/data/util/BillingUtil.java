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

import static ch.elexis.core.constants.XidConstants.DOMAIN_AHV;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IDiagnose;
import ch.elexis.core.data.interfaces.IFall;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.ch.BillingLaw;
import ch.elexis.core.model.format.FormatValidator;
import ch.elexis.core.services.holder.BillingServiceHolder;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.CoverageServiceHolder;
import ch.elexis.core.services.holder.EncounterServiceHolder;
import ch.elexis.core.services.holder.InvoiceServiceHolder;
import ch.elexis.core.services.holder.XidServiceHolder;
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
 * Util class with methods for checking and preparing {@link Konsultation}, with
 * the goal to include them in a bill {@link Rechnung#build(List)}.
 *
 * @author thomas
 *
 */
public class BillingUtil {

	public static String BILLINGCHECK_ENABLED_CFG = "ch.elexis.core.data/billablecheck/";

	private static final Logger log = LoggerFactory.getLogger(BillingUtil.class);

	/**
	 * Interface definition for checking a {@link Konsultation} if it can be
	 * included on a bill.
	 *
	 */
	public static interface IBillableCheck {
		int CODE_WARNING = 2;
		int CODE_ERROR = 2;

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
		 * Test if the {@link Konsultation} is bill able. If no the error is added to
		 * the {@link Result}.
		 *
		 * @param konsultation
		 * @param result
		 * @return
		 */
		public boolean isBillable(Konsultation konsultation, Result<Konsultation> result);
	}

	/**
	 * Array of {@link IBillableCheck} implementations. Implementations can be
	 * disabled or enabled using
	 * {@link BillingUtil#setCheckEnabled(IBillableCheck, boolean)}.
	 *
	 */
	public static IBillableCheck[] billableChecks = {
			// Check already billed
			new IBillableCheck() {
				@Override
				public boolean isBillable(Konsultation konsultation, Result<Konsultation> result) {
					boolean fail = konsultation.getRechnung() != null && !Rechnung.isStorno(konsultation.getRechnung());
					if (fail) {
						result.add(SEVERITY.ERROR, CODE_ERROR, getDescription(), konsultation, false);
					}
					return !fail;
				}

				@Override
				public String getId() {
					return "alreadyBilled";
				}

				@Override
				public String getDescription() {
					return "Behandlung ist bereits verrechnet.";
				}
			},
			// Check for zero sales.
			new IBillableCheck() {
				@Override
				public boolean isBillable(Konsultation konsultation, Result<Konsultation> result) {
					boolean fail = getTotal(konsultation).isZero();
					if (fail) {
						result.add(SEVERITY.ERROR, CODE_ERROR, getDescription(), konsultation, false);
					}
					return !fail;
				}

				@Override
				public String getId() {
					return "zeroSales";
				}

				@Override
				public String getDescription() {
					return "Behandlung mit Umsatz 0";
				}
			},
			// Check for invalid Mandant.
			new IBillableCheck() {
				@Override
				public boolean isBillable(Konsultation konsultation, Result<Konsultation> result) {
					Mandant mandant = konsultation.getMandant();
					boolean fail = (mandant == null || !mandant.isValid());
					if (fail) {
						result.add(SEVERITY.ERROR, CODE_ERROR, getDescription(), konsultation, false);
					}
					return !fail;
				}

				@Override
				public String getId() {
					return "invalidMandant";
				}

				@Override
				public String getDescription() {
					return "Ungültiger Mandant";
				}
			},
			// Check for missing coverage.
			new IBillableCheck() {
				@Override
				public boolean isBillable(Konsultation konsultation, Result<Konsultation> result) {
					Fall fall = konsultation.getFall();
					boolean fail = (fall == null);
					if (fail) {
						result.add(SEVERITY.ERROR, CODE_ERROR, getDescription(), konsultation, false);
					}
					return !fail;
				}

				@Override
				public String getId() {
					return "noCoverage";
				}

				@Override
				public String getDescription() {
					return "Fehlender Fall";
				}
			},
			// Check for invalid coverage.
			new IBillableCheck() {
				@Override
				public boolean isBillable(Konsultation konsultation, Result<Konsultation> result) {
					Fall fall = konsultation.getFall();
					boolean fail = (fall != null
							&& ConfigServiceHolder.getUser(Preferences.LEISTUNGSCODES_BILLING_STRICT, true)
							&& !fall.isValid());
					if (fail) {
						result.add(SEVERITY.ERROR, CODE_ERROR, getDescription(), konsultation, false);
					}
					return !fail;
				}

				@Override
				public String getId() {
					return "invalidCoverage";
				}

				@Override
				public String getDescription() {
					return "Fall nicht gültig";
				}
			},
			// Check for missing diagnose.
			new IBillableCheck() {
				@Override
				public boolean isBillable(Konsultation konsultation, Result<Konsultation> result) {
					ArrayList<IDiagnose> diagnosen = konsultation.getDiagnosen();
					boolean fail = (diagnosen == null || diagnosen.isEmpty());
					if (fail) {
						result.add(SEVERITY.ERROR, CODE_ERROR, getDescription(), konsultation, false);
					}
					return !fail;
				}

				@Override
				public String getId() {
					return "noDiagnose";
				}

				@Override
				public String getDescription() {
					return "Keine Diagnose";
				}
			},
			// Check for invalid date.
			new IBillableCheck() {
				private TimeTool checkTool = new TimeTool();

				@Override
				public boolean isBillable(Konsultation konsultation, Result<Konsultation> result) {
					boolean fail = (checkTool.set(konsultation.getDatum()) == false);
					if (fail) {
						result.add(SEVERITY.ERROR, CODE_ERROR, getDescription(), konsultation, false);
					}
					return !fail;
				}

				@Override
				public String getId() {
					return "invalidDate";
				}

				@Override
				public String getDescription() {
					return "Ungültiges Datum";
				}
			},
			// Check for missing diagnose in open Konsultation series.
			new IBillableCheck() {
				@Override
				public boolean isBillable(Konsultation konsultation, Result<Konsultation> result) {
					boolean fail = false;
					ArrayList<IDiagnose> diagnosen = konsultation.getDiagnosen();
					if (diagnosen == null || diagnosen.isEmpty()) {
						fail = true;
						// get other open konsultation of the case
						Query<Konsultation> query = new Query<>(Konsultation.class);
						query.add(Konsultation.FLD_BILL_ID, Query.EQUALS, null);
						query.add(Konsultation.FLD_CASE_ID, Query.EQUALS, konsultation.getFall().getId());
						List<Konsultation> openKonsultationen = query.execute();
						for (Konsultation openKons : openKonsultationen) {
							ArrayList<IDiagnose> diag = openKons.getDiagnosen();
							if (diag != null && !diag.isEmpty()) {
								fail = false;
								break;
							}
						}
						if (fail) {
							result.add(SEVERITY.ERROR, CODE_ERROR, getDescription(), konsultation, false);
						}
					}
					return !fail;
				}

				@Override
				public String getId() {
					return "noDiagnoseInSeries";
				}

				@Override
				public String getDescription() {
					return "Keine Diagnose in der Behandlungsserie";
				}
			}, new IBillableCheck() {
				@Override
				public boolean isBillable(Konsultation konsultation, Result<Konsultation> result) {
					if (konsultation.getFall() != null
							&& konsultation.getFall().getConfiguredBillingSystemLaw() == BillingLaw.IV) {
						ICoverage coverage = CoreModelServiceHolder.get()
								.load(konsultation.getFall().getId(), ICoverage.class).get();
						if (coverage.getPatient() != null) {
							if (StringUtils.isBlank(getSSN(coverage.getPatient()))) {
								result.add(SEVERITY.ERROR, CODE_ERROR, getDescription(), konsultation, false);
								return false;
							}
						}
					}
					return true;
				}

				@Override
				public String getId() {
					return "ivNoSSN";
				}

				@Override
				public String getDescription() {
					return "IV Fall ohne AHV Nummer";
				}
			}, new IBillableCheck() {
				@Override
				public boolean isBillable(Konsultation konsultation, Result<Konsultation> result) {
					if (konsultation.getFall() != null) {
						ICoverage coverage = CoreModelServiceHolder.get()
								.load(konsultation.getFall().getId(), ICoverage.class).get();
						if (coverage.getPatient() != null) {
							String ssn = getSSN(coverage.getPatient());
							if (StringUtils.isNotBlank(ssn)) {
								if (!FormatValidator.isValidAHVNum(ssn)) {
									result.add(SEVERITY.ERROR, CODE_ERROR, getDescription(), konsultation, false);
									return false;
								}
							}
						}
					}
					return true;
				}

				@Override
				public String getId() {
					return "invalidSSN";
				}

				@Override
				public String getDescription() {
					return "Fehlerhafte AHV Nummer";
				}
			}, new IBillableCheck() {
				@Override
				public boolean isBillable(Konsultation konsultation, Result<Konsultation> result) {
					if (konsultation.getFall() != null) {
						ICoverage coverage = CoreModelServiceHolder.get()
								.load(konsultation.getFall().getId(), ICoverage.class).get();
						IContact guarantor = coverage.getGuarantor();
						if (guarantor == null || (!guarantor.isPerson() && !guarantor.isOrganization())) {
							result.add(SEVERITY.ERROR, CODE_ERROR, getDescription(), konsultation, false);
							return false;
						}
					}
					return true;
				}

				@Override
				public String getId() {
					return "invalidGuarantor";
				}

				@Override
				public String getDescription() {
					return "Rechnungsempfänger nicht Person oder Organisation";
				}
			}, new IBillableCheck() {
				@Override
				public boolean isBillable(Konsultation konsultation, Result<Konsultation> result) {
					if (konsultation.getFall() != null) {
						ICoverage coverage = CoreModelServiceHolder.get()
								.load(konsultation.getFall().getId(), ICoverage.class).get();
						IContact costBearer = coverage.getCostBearer();
						if (costBearer == null || (!costBearer.isPerson() && !costBearer.isOrganization())) {
							result.add(SEVERITY.ERROR, CODE_ERROR, getDescription(), konsultation, false);
							return false;
						}
					}
					return true;
				}

				@Override
				public String getId() {
					return "invalidCostBearer";
				}

				@Override
				public String getDescription() {
					return "Kostenträger nicht Person oder Organisation";
				}
			}, new IBillableCheck() {
				@Override
				public boolean isBillable(Konsultation konsultation, Result<Konsultation> result) {
					if (konsultation.getFall() != null) {
						ICoverage coverage = CoreModelServiceHolder.get()
								.load(konsultation.getFall().getId(), ICoverage.class).get();
						IContact patient = coverage.getPatient();
						if (patient != null) {
							if (StringUtils.isBlank(patient.getEmail())) {
								result.add(SEVERITY.OK, CODE_WARNING, "Patient hat keine Email Adresse", konsultation,
										false);
							}
							if (StringUtils.isBlank(patient.getMobile())) {
								result.add(SEVERITY.OK, CODE_WARNING, "Patient hat keine Mobil Nummer", konsultation,
										false);
							}
							if (StringUtils.isBlank(patient.getStreet())) {
								result.add(SEVERITY.OK, CODE_WARNING, "Patient Adresse hat keine Strasse", konsultation,
										false);
							}
							if (StringUtils.isBlank(patient.getZip())) {
								result.add(SEVERITY.OK, CODE_WARNING, "Patient Adresse hat keine PLZ", konsultation,
										false);
							}
							if (StringUtils.isBlank(patient.getCity())) {
								result.add(SEVERITY.OK, CODE_WARNING, "Patient Adresse hat keinen Ort", konsultation,
										false);
							}
						}
					}
					return true;
				}

				@Override
				public String getId() {
					return "missingPatientData";
				}

				@Override
				public String getDescription() {
					return "Patientendaten fehlen";
				}
			}, new IBillableCheck() {
				@Override
				public boolean isBillable(Konsultation konsultation, Result<Konsultation> result) {
					IEncounter encounter = NoPoUtil.loadAsIdentifiable(konsultation, IEncounter.class).orElse(null);
					if (encounter != null) {
						List<IBilled> tardocBilled = getTardocOnly(encounter.getBilled());
						List<IBilled> tardocReferenzBilled = tardocBilled.stream().filter(v -> isReferenz(v)).toList();
						if (!tardocReferenzBilled.isEmpty()) {
							List<IBilled> noBezug = tardocReferenzBilled.stream().filter(b -> hasNoBezug(b)).toList();
							if (!noBezug.isEmpty()) {
								String noBezugString = noBezug.stream().map(b -> b.getCode())
										.collect(Collectors.joining(", "));
								result.add(SEVERITY.ERROR, CODE_ERROR,
										"TARDOC Referenzleistung(en) " + noBezugString + " ohne Bezug", konsultation,
										false);
								return false;
							}
						}
					}
					return true;
				}

				private boolean hasNoBezug(IBilled billed) {
					return StringUtils.isBlank((String) billed.getExtInfo("Bezug"));
				}

				@Override
				public String getId() {
					return "tardocRefNoBezug";
				}

				@Override
				public String getDescription() {
					return "TARDOC Referenzleistung(en) ohne Bezug";
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

				private boolean isReferenz(IBilled tardocVerr) {
					IBillable verrechenbar = tardocVerr.getBillable();
					String serviceTyp = getServiceTypReflective(verrechenbar);
					return serviceTyp != null && serviceTyp.equals("R");
				}

				private String getServiceTypReflective(IBillable billable) {
					try {
						Method getterMethod = billable.getClass().getMethod("getServiceTyp", (Class[]) null);
						Object typ = getterMethod.invoke(billable, (Object[]) null);
						if (typ instanceof String) {
							return (String) typ;
						}
					} catch (NoSuchMethodException | SecurityException | IllegalAccessException
							| IllegalArgumentException | InvocationTargetException e) {
						LoggerFactory.getLogger(getClass()).warn("Could not get service typ of [" + billable + "]",
								e.getMessage());
					}
					return null;
				}

			} };

	private static String getSSN(IPerson p) {
		IXid ahv = p.getXid(DOMAIN_AHV);
		String ret = ahv != null ? ahv.getDomainId() : StringUtils.EMPTY;
		if (StringUtils.isBlank(ret)) {
			ret = StringUtils
					.defaultString((String) p.getExtInfo(XidServiceHolder.get().getDomain(DOMAIN_AHV).getSimpleName()));
		}
		return ret.trim();
	}

	public static boolean isCheckEnabled(IBillableCheck check) {
		return ConfigServiceHolder.getGlobal(BILLINGCHECK_ENABLED_CFG + check.getId(), true);
	}

	public static void setCheckEnabled(IBillableCheck check, boolean enabled) {
		ConfigServiceHolder.setGlobal(BILLINGCHECK_ENABLED_CFG + check.getId(), enabled);
	}

	/**
	 * Test if the {@link Konsultation} can be billed, and return a {@link Result}
	 * containing possible error messages. {@link IBillableCheck} are applied if
	 * enabled.
	 *
	 * @param konsultation
	 * @return
	 */
	public static Result<Konsultation> getBillableResult(Konsultation konsultation) {

		Result<Konsultation> result = new Result<>(konsultation);

		for (IBillableCheck iBillableCheck : billableChecks) {
			if (isCheckEnabled(iBillableCheck)) {
				iBillableCheck.isBillable(konsultation, result);
			}
		}
		return result;
	}

	/**
	 * Calculate the total amount of all {@link Verrechnet} of the
	 * {@link Konsultation}.
	 *
	 * @param konsultation
	 * @return
	 */
	public static Money getTotal(Konsultation konsultation) {
		IEncounter encounter = NoPoUtil.loadAsIdentifiable(konsultation, IEncounter.class).get();
		return EncounterServiceHolder.get().getSales(encounter);
	}

	/**
	 * Remove all not bill able {@link Konsultation} from the provided {@link List}.
	 *
	 * @param konsultationen
	 * @return filtered {@link List}
	 */
	public static List<Konsultation> filterNotBillable(List<Konsultation> konsultationen) {
		return konsultationen.parallelStream().filter(k -> getBillableResult(k).isOK()).collect(Collectors.toList());
	}

	/**
	 * Get a Map representation of bill able {@link Konsultation} instances. To be
	 * bill able the list of {@link Konsultation} is split by
	 * {@link Rechnungssteller} and {@link Fall}.
	 *
	 * @param konsultationen
	 * @return map sorted by billing criteria
	 */
	public static Map<Rechnungssteller, Map<Fall, List<Konsultation>>> getGroupedBillable(
			List<Konsultation> konsultationen) {
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
	 * Create bills {@link Rechnung} for all {@link Konsultation} contained in the
	 * map. Returns al list with the {@link Result} of building the bills.
	 *
	 * @param toBillMap
	 * @return
	 */
	public static List<Result<IInvoice>> createBills(Map<Rechnungssteller, Map<Fall, List<Konsultation>>> toBillMap) {
		List<Result<IInvoice>> ret = new ArrayList<>();
		Set<Rechnungssteller> invoicers = toBillMap.keySet();
		for (Rechnungssteller invoicer : invoicers) {
			Set<Fall> faelle = toBillMap.get(invoicer).keySet();
			for (Fall fall : faelle) {
				List<IEncounter> encounters = NoPoUtil.loadAsIdentifiable(toBillMap.get(invoicer).get(fall),
						IEncounter.class);
				ret.add(InvoiceServiceHolder.get().invoice(encounters));
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
	public static List<Konsultation> getKonsultationsFromSameYear(List<Konsultation> konsultations) {
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
	 * Sort the consultations by year.
	 *
	 * @deprecated
	 * @param consultations
	 * @return
	 */
	@Deprecated
	public static Map<Integer, List<Konsultation>> getSortedByYear(List<Konsultation> consultations) {
		Map<Integer, List<Konsultation>> ret = new HashMap<>();
		TimeTool konsDate = new TimeTool();
		for (Konsultation consultation : consultations) {
			konsDate.set(consultation.getDatum());
			Integer year = Integer.valueOf(konsDate.get(TimeTool.YEAR));
			List<Konsultation> list = ret.get(year);
			if (list == null) {
				list = new ArrayList<>();
			}
			list.add(consultation);
			ret.put(year, list);
		}
		return ret;
	}

	/**
	 * Sort the Encounters by year.
	 *
	 *
	 * @param
	 * @return
	 */
	public static Map<Integer, List<IEncounter>> getSortedEncountersByYear(List<IEncounter> consultations) {
		Map<Integer, List<IEncounter>> ret = new HashMap<>();
		for (IEncounter consultation : consultations) {
			Integer year = consultation.getDate().getYear();
			List<IEncounter> list = ret.get(year);
			if (list == null) {
				list = new ArrayList<>();
			}
			list.add(consultation);
			ret.put(year, list);
		}
		return ret;
	}

	private static Integer[] splitBillYears = { 2018 };

	public static boolean canBillYears(List<Integer> years) {
		for (Integer splitYear : splitBillYears) {
			boolean aboveSplitYear = false;
			boolean belowSplitYear = false;
			for (Integer year : years) {
				if (year >= splitYear) {
					aboveSplitYear = true;
				} else {
					belowSplitYear = true;
				}
				// only above or below is allowed
				if (aboveSplitYear && belowSplitYear) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Copies the actual fall, merge the copied fall with changes, transfer cons,
	 * storno the old invoice
	 */
	public static void doBillCorrection(InvoiceCorrectionDTO invoiceCorrectionDTO, BillCallback billCallback) {

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
		private List<Konsultation> transferedKonsultations = new ArrayList<>();
		private LeistungDTO leistungDTO = null;
		private DiagnosesDTO diagnosesDTO = null;
		private Konsultation konsultation = null;
		private IBilled verrechnet = null;
		private List<Object> locks = new ArrayList<>();
		private final InvoiceCorrectionDTO invoiceCorrectionDTO;
		private final BillCallback billCallback;

		public BillCorrection(InvoiceCorrectionDTO invoiceCorrectionDTO, BillCallback billCallback) {
			this.invoiceCorrectionDTO = invoiceCorrectionDTO;
			this.rechnung = Rechnung.load(invoiceCorrectionDTO.getId());
			this.billCallback = billCallback;
		}

		public void doCorrection() {
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
					log.debug("invoice correction: processing [{}] [{}] ", historyEntryDTO.getOperationType(),
							historyEntryDTO.isIgnored() ? "ignored" : (success ? "success" : "failed"));
					historyEntryDTO.setSuccess(success);
				}
			}

			log.debug("release all locks: " + locks.size());
			for (Object po : locks) {
				LocalLockServiceHolder.get().releaseLock(po);
			}
		}

		private void removeDiagnose(Object base, Object item) {
			konsultation = Konsultation.load(((KonsultationDTO) base).getId());
			diagnosesDTO = (DiagnosesDTO) item;
			IEncounter encounter = NoPoUtil.loadAsIdentifiable(konsultation, IEncounter.class).get();
			encounter.removeDiagnosis(diagnosesDTO.getiDiagnose());
			CoreModelServiceHolder.get().save(encounter);
			log.debug("invoice correction: removed diagnose id [{}] from kons id [{}]", diagnosesDTO.getId(),
					konsultation.getId());
		}

		private void addDiagnose(Object base, Object item) {
			konsultation = Konsultation.load(((KonsultationDTO) base).getId());
			diagnosesDTO = (DiagnosesDTO) item;
			IEncounter encounter = NoPoUtil.loadAsIdentifiable(konsultation, IEncounter.class).get();
			encounter.addDiagnosis(diagnosesDTO.getiDiagnose());
			CoreModelServiceHolder.get().save(encounter);
			log.debug("invoice correction: added diagnose id [{}] to kons id [{}]", diagnosesDTO.getId(),
					konsultation.getId());
		}

		private void changePriceLeistung(Object item) {
			leistungDTO = (LeistungDTO) item;
			verrechnet = leistungDTO.getVerrechnet();
			if (verrechnet != null) {
				acquireLock(locks, verrechnet, false);
				int tp = leistungDTO.getTp();
				int tpOld = verrechnet.getPoints();
				if (tpOld != tp) {
					verrechnet.setPoints(tp);
					log.debug("invoice correction: price changed to [{}] for leistung id [{}]",
							leistungDTO.getPrice().getAmountAsString(), leistungDTO.getId());
				}
				CoreModelServiceHolder.get().save(verrechnet);
			} else {
				log.warn("invoice correction: leistung id [{}] no verrechnet exists cannot change price",
						leistungDTO.getId());
			}
		}

		private void changeCountLeistung(Object item) {
			leistungDTO = (LeistungDTO) item;
			verrechnet = leistungDTO.getVerrechnet();
			if (verrechnet != null) {
				acquireLock(locks, verrechnet, false);
				IStatus ret = BillingServiceHolder.get().changeAmountValidated(verrechnet, leistungDTO.getCount());
				log.debug("invoice correction: changed count from leistung id [{}]", leistungDTO.getId());
				if (ret.isOK()) {
					CoreModelServiceHolder.get().save(verrechnet);
				} else {
					addToOutput(output, ret.getMessage());
					success = false;
					log.warn("invoice correction: cannot change count from leistung with id [{}]", leistungDTO.getId());
				}
			}
		}

		public void transferKonsultation(Object base, Object item) {
			KonsultationDTO konsultationDTO = (KonsultationDTO) base;
			Fall fallToTransfer = Fall.load(((IFall) item).getId());

			log.debug("invoice correction: transfer kons with id [{}] to fall id [{}]", konsultationDTO.getId(),
					fallToTransfer.getId());

			Konsultation konsultation = Konsultation.load(konsultationDTO.getId());

			Fall oldFall = konsultation.getFall();

			acquireLock(locks, oldFall, false);
			acquireLock(locks, fallToTransfer, false);
			acquireLock(locks, konsultation, false);

			EncounterServiceHolder.get().transferToCoverage(
					NoPoUtil.loadAsIdentifiable(konsultation, IEncounter.class).get(),
					NoPoUtil.loadAsIdentifiable(fallToTransfer, ICoverage.class).get(), true);

			Iterator<Konsultation> it = transferedKonsultations.iterator();
			while (it.hasNext()) {
				Konsultation k = it.next();
				if (konsultation.getId().equals(k.getId())) {
					it.remove();
					log.debug("invoice correction: removed transfered kons with id [{}] from released konsultations",
							k.getId());
				}
			}

			log.debug("invoice correction: transfered kons id [{}] from fall id [{}] to fall id  [{}]",
					konsultation.getId(), oldFall.getId(), fallToTransfer.getId());
		}

		private void transferLeistungen(Object base, Object item, Object additional) {
			@SuppressWarnings("unchecked")
			List<LeistungDTO> leistungenDTOs = (List<LeistungDTO>) item;
			konsultation = Konsultation.load(((KonsultationDTO) base).getId());
			Fall fallToTransfer = Fall.load(((IFall) additional).getId());
			List<LeistungDTO> removedleistungDTOs = new ArrayList<>();
			for (LeistungDTO itemLeistung : leistungenDTOs) {
				log.debug("invoice correction: transfer leistung id [{}] from kons id [{}]", itemLeistung.getId(),
						konsultation.getId());
				if (itemLeistung.getVerrechnet() != null) {

					acquireLock(locks, itemLeistung.getVerrechnet(), false);
					Result<?> resRemove = BillingServiceHolder.get().removeBilled(itemLeistung.getVerrechnet(),
							NoPoUtil.loadAsIdentifiable(konsultation, IEncounter.class).get());

					log.debug("invoice correction: removed leistung id [{}] from kons id [{}]", itemLeistung.getId(),
							konsultation.getId());
					if (resRemove.isOK()) {
						itemLeistung.setVerrechnet(null);
						removedleistungDTOs.add(itemLeistung);
					} else {
						addToOutput(output, "Die Leistung " + itemLeistung.getVerrechnet().getText()
								+ " konnte nicht auf einen neuen Fall/Konsultation transferiert werden. Das Entfernen der Leistung ist fehlgeschlagen.");
						success = false;
						log.warn("invoice correction: cannot transfer/remove leistung with id [{}] from kons id [{}]",
								itemLeistung.getId(), konsultation.getId());
					}
				} else {
					removedleistungDTOs.add(itemLeistung);
				}
			}
			if (!removedleistungDTOs.isEmpty()) {
				Konsultation newKons = konsultation.createCopy(fallToTransfer, rechnung);
				IEncounter newEncounter = NoPoUtil.loadAsIdentifiable(newKons, IEncounter.class).get();
				acquireLock(locks, newKons, true);
				log.debug(
						"invoice correction: copied kons from id [{}] to kons id [{}] and added kons to fall id [{}] ",
						konsultation.getId(), newKons.getId(), newKons.getFall().getId());
				for (LeistungDTO itemLeistung : removedleistungDTOs) {
					Result<IBilled> resAddLeistung = BillingServiceHolder.get().bill(itemLeistung.getIVerrechenbar(),
							newEncounter, 1.0);
					log.debug("invoice correction: add leistung id [{}] to kons id [{}]", itemLeistung.getId(),
							newKons.getId());
					if (resAddLeistung.isOK()) {
						verrechnet = EncounterServiceHolder.get()
								.getBilledByBillable(newEncounter, itemLeistung.getIVerrechenbar()).stream().findFirst()
								.orElse(null);
						if (verrechnet != null) {
							itemLeistung.setVerrechnet(verrechnet);
							if (verrechnet.getAmount() != itemLeistung.getCount()) {
								IStatus ret = BillingServiceHolder.get().changeAmountValidated(verrechnet,
										itemLeistung.getCount());
								log.debug("invoice correction: count changed from [{}] to {[]} - for leistung id [{}]",
										itemLeistung.getId());
								if (ret.isOK()) {
									CoreModelServiceHolder.get().save(verrechnet);
								} else {
									verrechnet = null;
									log.warn("invoice correction: cannot change count for leistung with id [{}]",
											itemLeistung.getId());
								}
							}
						}
					} else {
						addToOutput(output, resAddLeistung);
						verrechnet = null;
					}
					if (verrechnet == null) {
						addToOutput(output, "Die Leistung " + itemLeistung.getIVerrechenbar().getText()
								+ " konnte nicht auf einen neuen Fall/Konsultation transferiert werden. Das Hinzufügen der Leistung ist fehlgeschlagen.");
						success = false;
						log.warn("invoice correction: cannot transfer/add leistung with id [{}] to new kons id [{}]",
								itemLeistung.getId(), newKons.getId());
					}
				}
			}
			if (!success) {
				addToOutput(output, "Nicht alle Leistungen konnten erfolgreich transferiert werden.");
				log.warn("invoice correction: not all leistungen could be transfered.");
			}
		}

		private void removeLeistung(Object base, Object item) {
			leistungDTO = (LeistungDTO) item;
			if (leistungDTO.getVerrechnet() != null) {
				acquireLock(locks, leistungDTO.getVerrechnet(), false);
				Result<?> resRemove = BillingServiceHolder.get().removeBilled(leistungDTO.getVerrechnet(),
						CoreModelServiceHolder.get().load(((KonsultationDTO) base).getId(), IEncounter.class).get());
				log.debug("invoice correction: removed leistung id [{}] from kons id [{}]", leistungDTO.getId(),
						((KonsultationDTO) base).getId());
				if (resRemove.isOK()) {
					((LeistungDTO) item).setVerrechnet(null);
				} else {
					addToOutput(output,
							"Die Leistung " + leistungDTO.getVerrechnet().getText() + " konnte nicht entfernt werden.");
					success = false;
					log.warn("invoice correction: cannot remove leistung with id [{}] from kons id [{}]",
							leistungDTO.getId(), ((KonsultationDTO) base).getId());
				}
			}
		}

		private void addLeistung(Object base, Object item) {
			konsultation = Konsultation.load(((KonsultationDTO) base).getId());
			IEncounter encounter = NoPoUtil.loadAsIdentifiable(konsultation, IEncounter.class).get();
			leistungDTO = (LeistungDTO) item;
			Result<IBilled> res = BillingServiceHolder.get().bill(leistungDTO.getIVerrechenbar(), encounter, 1.0);
			log.debug("invoice correction: added leistung id [{}] to kons id [{}]", leistungDTO.getId(),
					((KonsultationDTO) base).getId());
			if (res.isOK()) {
				verrechnet = EncounterServiceHolder.get().getBilledByBillable(encounter, leistungDTO.getIVerrechenbar())
						.stream().findFirst().orElse(null);
				if (verrechnet != null) {
					leistungDTO.setVerrechnet(verrechnet);
					acquireLock(locks, verrechnet, false);
				}
			} else {
				addToOutput(output, res);
				verrechnet = null;
			}
			if (verrechnet == null) {
				addToOutput(output, "Die Leistung " + leistungDTO.getIVerrechenbar().getText()
						+ " konnte nicht verrechnet werden.");
				success = false;
				log.warn("invoice correction: cannot add leistung with id [{}] to kons id [{}]", leistungDTO.getId(),
						konsultation.getId());
			}
		}

		private void changeMandantKonsultation(Object base) {
			Konsultation.load(((KonsultationDTO) base).getId()).setMandant(((KonsultationDTO) base).getMandant());
			log.debug("invoice correction: changed mandant of kons id [{}]", ((KonsultationDTO) base).getId());
		}

		private void changeDateKonsultation(Object base) {
			Konsultation.load(((KonsultationDTO) base).getId()).setDatum(((KonsultationDTO) base).getDate(), true);
			log.debug("invoice correction: changed date of kons id [{}]", ((KonsultationDTO) base).getId());
		}

		private void transferKonsultations() {
			transferedKonsultations.clear();
			Konsultation[] consultations = srcFall.get().getBehandlungen(true);
			if (consultations != null) {
				for (Konsultation openedKons : consultations) {
					if (openedKons.exists()) {
						Rechnung bill = openedKons.getRechnung();
						if (bill == null) {
							EncounterServiceHolder.get().transferToCoverage(
									NoPoUtil.loadAsIdentifiable(openedKons, IEncounter.class).get(),
									NoPoUtil.loadAsIdentifiable(copyFall.get(), ICoverage.class).get(), true);
							log.debug("invoice correction: transfered kons id [{}] to copied fall id  [{}] ",
									openedKons.getId(), copyFall.get().getId());
							transferedKonsultations.add(openedKons);

							// if validation of cons is failed the bill correction will be reseted
							Result<?> result = BillingUtil.getBillableResult(openedKons);
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

		private void changeFall() throws ElexisException {
			copyFall.get().persistDTO(invoiceCorrectionDTO.getFallDTO());
			// at this point the fall must be opened
			copyFall.get().setEndDatum(null);
			log.debug("invoice correction: persisted fall changes to id  [{}] ", copyFall.get().getId());
		}

		private void copyFall() {
			srcFall = Optional.of(rechnung.getFall());
			ICoverage copy = CoverageServiceHolder.get()
					.createCopy(NoPoUtil.loadAsIdentifiable(srcFall.get(), ICoverage.class).get());
			copyFall = Optional.of((Fall) NoPoUtil.loadAsPersistentObject(copy));
			acquireLock(locks, copyFall.get(), true);
			log.debug("invoice correction: copied fall from id [{}] to id [{}] ", srcFall.get().getId(),
					copyFall.get().getId());
		}

		private void createBill(InvoiceHistoryEntryDTO historyEntryDTO) {
			if (copyFall.isPresent()) {
				if (invoiceCorrectionDTO.getFallDTO().getEndDatum() != null) {
					copyFall.get().setEndDatum(invoiceCorrectionDTO.getFallDTO().getEndDatum());
					acquireLock(locks, copyFall.get(), false);
				}

				// close fall if no kons exists
				if ((srcFall.get().isOpen() || new TimeTool(srcFall.get().getEndDatum()).after(new TimeTool()))
						&& srcFall.get().getBehandlungen(true).length == 0) {
					acquireLock(locks, srcFall.get(), false);
					srcFall.get().setEndDatum(new TimeTool().toString(TimeTool.DATE_GER));
				}
			}
			if (releasedKonsultations.isEmpty()) {
				log.debug(
						"invoice correction: no konsultations exists for invoice id [{}]- a new invoice will not be created.",
						rechnung.getNr());
				output.append("Die Rechnung " + rechnung.getNr() + " wurde erfolgreich durch "
						+ CoreHub.getLoggedInContact().getLabel()
						+ " korrigiert.\nEs wurde keine neue Rechnung erstellt.");
				historyEntryDTO.setIgnored(true);
			} else {

				Result<IInvoice> rechnungResult = InvoiceServiceHolder.get()
						.invoice(NoPoUtil.loadAsIdentifiable(releasedKonsultations, IEncounter.class));
				if (!rechnungResult.isOK()) {

					for (@SuppressWarnings("rawtypes")
					msg message : rechnungResult.getMessages()) {
						if (message.getSeverity() != SEVERITY.OK) {
							if (output.length() > 0) {
								output.append(StringUtils.LF);
							}
							output.append(message.getText());
						}
					}
					success = false;
					log.error("invoice correction: error cannot create new invoice with id "
							+ (rechnungResult.get() != null ? rechnungResult.get().getId() : "null"));
					log.error("invoice correction: error details: " + output.toString());
				} else {
					Rechnung newRechnung = Rechnung.load(rechnungResult.get().getId());
					invoiceCorrectionDTO.setNewInvoiceNumber(newRechnung.getNr());
					log.debug("invoice correction: create new invoice with number [{}] old invoice number [{}] ",
							newRechnung.getNr(), rechnung.getNr());
					output.append("Die Rechnung " + rechnung.getNr() + " wurde erfolgreich durch "
							+ CoreHub.getLoggedInContact().getLabel() + " korrigiert.\nNeue Rechnungsnummer lautet: "
							+ invoiceCorrectionDTO.getNewInvoiceNumber());
				}
			}
		}

		private boolean stornoBill() {
			List<Konsultation> konsultations = billCallback.storno(rechnung);
			if (konsultations != null) {
				// make sure all entities are refreshed
				konsultations.forEach(k -> {
					NoPoUtil.loadAsIdentifiable(k, IEncounter.class).ifPresent(e -> {
						CoreModelServiceHolder.get().refresh(e, true);
						e.getBilled().forEach(b -> {
							CoreModelServiceHolder.get().refresh(b, true);
						});
					});
				});
				releasedKonsultations.addAll(konsultations);
			} else {
				success = false;
			}

			log.debug("invoice correction: storno invoice with number [{}] ", rechnung.getNr());
			return true;
		}

		private void addToOutput(StringBuilder output, Result<?> res) {
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

		private void addToOutput(StringBuilder output, String warning) {
			if (output.length() > 0) {
				output.append(StringUtils.LF);
			}
			if (warning.length() > 0) {
				output.append(warning);
			}
		}

		private boolean acquireLock(List<Object> currentLocks, Object persistentObjectToLock,
				boolean forceReleaseLock) {
			if (!currentLocks.contains(persistentObjectToLock)) {
				if (LocalLockServiceHolder.get().acquireLock(persistentObjectToLock).isOk()) {
					if (!forceReleaseLock) {
						currentLocks.add(persistentObjectToLock);
					} else {
						LocalLockServiceHolder.get().releaseLock(persistentObjectToLock);
					}
					return true;
				}
			}
			return false;
		}

	}
}
