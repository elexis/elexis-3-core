/*******************************************************************************
 * Copyright (c) 2005-2011, G. Weirich and Elexis; portions Copyright (c) 2013 Joerg Sigle.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    Joerg Sigle   - warning if a case containes no billables or a total turnover of zero
 *    
 *******************************************************************************/

package ch.elexis.data;

//WARNING: A lot of code from this file also exists in ch.elexis.arzttarife_ch.src.TarmedRechnung.Validator.java.
//And over there, maybe it is in a more advanced state, regarding modularization and internationalization.
//But this file here appears to be actually used. 

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.elexis.core.model.IDiagnose;
import ch.elexis.core.model.InvoiceState;
import ch.rgw.io.Settings;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class Rechnung extends PersistentObject {
	public static final String REMARK = "Bemerkung";
	/**
	 * The date the current state was set, if in the future, we have a temporary state
	 */
	public static final String BILL_STATE_DATE = "StatusDatum";
	public static final String BILL_DATE = "RnDatum";
	public static final String BILL_AMOUNT_CENTS = "Betragx100";
	public static final String BILL_DATE_UNTIL = "RnDatumBis";
	public static final String BILL_DATE_FROM = "RnDatumVon";
	public static final String BILL_STATE = "RnStatus";
	public static final String MANDATOR_ID = "MandantID";
	public static final String CASE_ID = "FallID";
	public static final String BILL_NUMBER = "RnNummer";
	static final String TABLENAME = "RECHNUNGEN";
	// public static final DecimalFormat geldFormat=new DecimalFormat("0.00");
	// Texte für Trace-Meldungen
	public static final String STATUS_CHANGED = "Statusänderung";
	public static final String PAYMENT = "Zahlung";
	public static final String CORRECTION = "Korrektur";
	public static final String REJECTED = "Zurückgewiesen";
	public static final String OUTPUT = "Ausgegeben";
	public static final String REMARKS = "Bemerkungen";
	public static final String INVOICE_CORRECTION = "Rechnungskorrektur";
	
	static {
		addMapping(TABLENAME, BILL_NUMBER, CASE_ID, MANDATOR_ID, "RnDatum=S:D:RnDatum", BILL_STATE,
			"StatusDatum=S:D:StatusDatum", "RnDatumVon=S:D:RnDatumVon",
			"RnDatumBis=S:D:RnDatumBis", "Betragx100=Betrag", FLD_EXTINFO,
			"Zahlungen=LIST:RechnungsID:ZAHLUNGEN:Datum");
	}
	
	public Rechnung(final String nr, final Mandant m, final Fall f, final String von,
		final String bis, final Money Betrag, final int status){
		create(null);
		String Datum = new TimeTool().toString(TimeTool.DATE_GER);
		set(new String[] {
			BILL_NUMBER, MANDATOR_ID, CASE_ID, BILL_DATE_FROM, BILL_DATE_UNTIL, BILL_AMOUNT_CENTS,
			BILL_STATE, BILL_DATE
		}, nr, m.getId(), f.getId(), von, bis, Betrag.getCentsAsString(), Integer.toString(status),
			Datum);
		
		new AccountTransaction(f.getPatient(), this, Betrag.multiply(-1.0), Datum,
			"Rechnung erstellt");
	}
	
	/**
	 * Eine Rechnung aus einer Behandlungsserie erstellen. Es werde aus dieser Serie nur diejenigen
	 * Behandlungen verwendet, die zum selben Mandanten und zum selben Fall gehören. Falls der Fall
	 * einen Rechnungssteller hat, werden alle Konsultationen ungeachtet des Mandanten verrechnet
	 * 
	 * @return Ein Result mit ggf. der erstellten Rechnung als Inhalt
	 */
	public static Result<Rechnung> build(final List<Konsultation> behandlungen){
		System.out.println("js Rechnung: build() begin");
		
		System.out
			.println("js Rechnung: build(): TO DO: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.out
			.println("js Rechnung: build(): TO DO: Apparently, Rechnung.build() uses local checking algorithms,");
		System.out
			.println("js Rechnung: build(): TO DO: Event though Validator.checkBill() offers more structured ones.");
		System.out.println("js Rechnung: build(): TO DO: Why are both of them in the code?");
		System.out
			.println("js Rechnung: build(): TO DO: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		
		Result<Rechnung> result = new Result<Rechnung>();
		
		if ((behandlungen == null) || (behandlungen.size() == 0)) {
			return result.add(Result.SEVERITY.WARNING, 1,
				"Die Rechnung enthält keine Behandlungen (Konsultationen)", null, true); // js:
			// added (Konsultationen) to match
			// nomenclature
			// elsewhere in Elexis.
		}
		
		// On the fly prüfen, ob ein Patient auch Person ist; ggf. korrigieren.
		// Alle Tarmed-Rechnungen an Patienten sollten vermutlich an Personen
		// adressiert sein;
		// mit einer Organisations-Adresse für den Patienten kommen sie
		// jedenfalls nicht durch
		// die TrustX TCTest Prüfung.
		for (Konsultation b : behandlungen) {
			Patient pat = b.getFall().getPatient();
			if (!pat.istPerson()) {
				MessageEvent
					.fireInformation(
						"Hinweis",
						"Bei Patient Nr. "
							+ pat.getPatCode()
							+ ", "
							+ pat.getName()
							+ ", "
							+ pat.getVorname()
							+ ", "
							+ pat.getGeburtsdatum()
							+ "\n"
							+ "fehlte das Häkchen für 'Person' in der Kontaktdatenbank.\n\nIch korrigiere das selbst.");
				pat.set(Kontakt.FLD_IS_PERSON, StringConstants.ONE);
			}
		}
		
		System.out.println("js Rechnung: build(): number of consultations: " + behandlungen.size());
		for (Konsultation b : behandlungen) {
			if (b.getLeistungen().isEmpty() || b.getUmsatz() == 0) {
				System.out.println("Ignoriere Behandlung mit Umsatz 0");
			}
			
			else {
				List<Verrechnet> lstg = b.getLeistungen();
				for (Verrechnet l : lstg) {
					if (l.getNettoPreis().isZero()
						&& CoreHub.userCfg
							.get(Preferences.LEISTUNGSCODES_BILLING_ZERO_CHECK, false)) {
						Patient pat = b.getFall().getPatient();
						String msg =
							"Eine Konsultation vom " + b.getDatum().toString()
								+ " für\nPatient Nr. " + pat.getPatCode() + ", " + pat.getName()
								+ ", " + pat.getVorname() + ", " + pat.getGeburtsdatum() + "\n"
								+ "enthält mindestens eine Leistung zum Preis 0.00.\n"
								+ "\nDie Ärztekasse würde so eine Rechnung zurückgeben.\n\n";
						if (cod.openQuestion("WARNUNG: Leistung zu Fr. 0.00 !", msg
							+ "Soll die Rechnung trotzdem erstellt werden?")) {
							// do nothing
						} else {
							return result
								.add(
									Result.SEVERITY.WARNING,
									1,
									msg
										+ "Diese Rechnung wird jetzt nicht erstellt."
										+ "\n\nBitte prüfen Sie die verrechneten Leistungen,"
										+ "oder verschieben Sie die Konsultation zu einem später zu verrechnenden Fall!",
									null, true);
						}
					}
				}
			}
		}
		
		Rechnung ret = new Rechnung();
		ret.create(null);
		CoreHub.getLocalLockService().acquireLock(ret);
		TimeTool startDate = new TimeTool("31.12.2999");
		TimeTool endDate = new TimeTool("01.01.2000");
		TimeTool actDate = new TimeTool();
		Mandant m = null;
		// Kontakt kostentraeger=null;
		List<IDiagnose> diagnosen = null;
		Fall f = null;
		// int summe=0;
		Money summe = new Money();
		for (Konsultation b : behandlungen) {
			Rechnung shouldntExist = b.getRechnung();
			if ((shouldntExist != null) && (shouldntExist.getStatus() != RnStatus.STORNIERT)) {
				log.warn("Tried to create bill for already billed kons " + b.getLabel());
				continue;
			}
			Mandant bm = b.getMandant();
			if ((bm == null) || (!bm.isValid())) {
				result =
					result.add(Result.SEVERITY.ERROR, 1,
						"Ungültiger Mandant bei Konsultation " + b.getLabel(), ret, true);
				continue;
			}
			if (m == null) {
				m = bm;
				ret.set(MANDATOR_ID, m.getId());
			} else {
				if (!bm.getRechnungssteller().getId().equals(m.getRechnungssteller().getId())) {
					result =
						result.add(Result.SEVERITY.ERROR, 2,
							"Die Liste enthält unterschiedliche Rechnungssteller " + b.getLabel(),
							ret, true);
					continue;
				}
			}
			Fall bf = b.getFall();
			if (bf == null) {
				result =
					result.add(Result.SEVERITY.ERROR, 3,
						"Fehlender Fall bei Konsultation " + b.getLabel(), ret, true);
				continue;
			}
			if (f == null) {
				f = bf;
				ret.set(CASE_ID, f.getId());
				f.setBillingDate(null); // ggf. Rechnungsvorschlag löschen
			} else {
				if (!f.getId().equals(bf.getId())) {
					result =
						result.add(Result.SEVERITY.ERROR, 4,
							"Die Liste enthält unterschiedliche Faelle " + b.getLabel(), ret, true);
					continue;
				}
			}
			
			if ((diagnosen == null) || (diagnosen.size() == 0)) {
				diagnosen = b.getDiagnosen();
			}
			if (actDate.set(b.getDatum()) == false) {
				result =
					result.add(Result.SEVERITY.ERROR, 5,
						"Ungültiges Datum bei Konsultation " + b.getLabel(), ret, true);
				continue;
			}
			if (actDate.isBefore(startDate)) {
				startDate.set(actDate);
			}
			if (actDate.isAfter(endDate)) {
				endDate.set(actDate);
			}
			List<Verrechnet> lstg = b.getLeistungen();
			
			for (Verrechnet l : lstg) {
				Money sz = l.getNettoPreis().multiply(l.getZahl());
				summe.addMoney(sz);
			}
		}
		if (f == null) {
			result =
				result.add(Result.SEVERITY.ERROR, 8, "Die Rechnung hat keinen gültigen Fall ("
					+ getRnDesc(ret) + ")", ret, true);
			// garant=CoreHub.actMandant;
		} else {
			if (CoreHub.userCfg.get(Preferences.LEISTUNGSCODES_BILLING_STRICT, true)
				&& !f.isValid()) {
				result =
					result.add(Result.SEVERITY.ERROR, 8, "Die Rechnung hat keinen gültigen Fall ("
						+ getRnDesc(ret) + ")", ret, true);
			}
			// garant=f.getGarant();
			
		}
		
		// check if there are any Konsultationen
		if (CoreHub.userCfg.get(Preferences.LEISTUNGSCODES_BILLING_STRICT, true)) {
			if ((diagnosen == null) || (diagnosen.size() == 0)) {
				result =
					result.add(Result.SEVERITY.ERROR, 6, "Die Rechnung enthält keine Diagnose ("
						+ getRnDesc(ret) + ")", ret, true);
			}
		}
		/*
		 * if(garant==null || !garant.isValid()){ result=result.add(Log.ERRORS,7,"Die Rechnung hat
		 * keinen Garanten ("+getRnDesc(ret)+")",ret,true); }
		 */
		
		String Datum = new TimeTool().toString(TimeTool.DATE_GER);
		ret.set(BILL_DATE_FROM, startDate.toString(TimeTool.DATE_GER));
		ret.set(BILL_DATE_UNTIL, endDate.toString(TimeTool.DATE_GER));
		ret.set(BILL_DATE, Datum);
		ret.setStatus(RnStatus.OFFEN);
		// summe.roundTo5();
		ret.set(BILL_AMOUNT_CENTS, summe.getCentsAsString());
		// ret.setExtInfo("Rundungsdifferenz", summe.getFracAsString());
		String nr = getNextRnNummer();
		ret.set(BILL_NUMBER, nr);
		if (!result.isOK()) {
			ret.delete();
			CoreHub.getLocalLockService().releaseLock(ret);
			return result;
		}
		
		for (Konsultation b : behandlungen) {
			b.setRechnung(ret);
			
			// save all verrechnet of this rechnung
			List<Verrechnet> lstg = b.getLeistungen();
			for (Verrechnet l : lstg) {
				// create copy for each verrechnet with the rechnung as reference
				l.createCopy(ret);
			}
		}
		if (ret.getOffenerBetrag().isZero()) {
			ret.setStatus(RnStatus.BEZAHLT);
		} else {
			if(f!=null) {
				new AccountTransaction(f.getPatient(), ret, summe.negate(), Datum, "Rn " + nr
					+ " erstellt.");
			}
		}
		
		CoreHub.getLocalLockService().releaseLock(ret);
		return result.add(Result.SEVERITY.OK, 0, "OK", ret, false);
	}
	
	private static String getRnDesc(final Rechnung rn){
		StringBuilder sb = new StringBuilder();
		if (rn == null) {
			sb.append("Keine Rechnungsnummer");
		} else {
			Fall fall = rn.getFall();
			sb.append("Rechnung: " + rn.getNr()).append(" / ");
			if (fall == null) {
				sb.append("Kein Fall");
			} else {
				sb.append("Fall: " + fall.getLabel()).append(" / ");
				Patient pat = fall.getPatient();
				if (pat == null) {
					sb.append("Kein Patient");
				} else {
					sb.append(pat.getLabel());
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * Get all {@link Verrechnet} of this bill. Also works if the {@link Rechnung} has been
	 * canceled. Only works if the {@link Rechnung} was created with Elexis version 3.0.0 or newer.
	 * 
	 * @since 3.0.0
	 * @return
	 */
	public List<Verrechnet> getLeistungen(){
		return VerrechnetCopy.getVerrechnetByBill(this);
	}
	
	/** Die Rechnungsnummer holen */
	public String getNr(){
		return get(BILL_NUMBER);
	}
	
	/** Den Fall dieser Rechnung holen */
	public Fall getFall(){
		return Fall.load(get(CASE_ID));
	}
	
	/** Den Mandanten zu dieser Rechnung holen */
	public Mandant getMandant(){
		return Mandant.load(get(MANDATOR_ID));
	}
	
	/** Eine Liste aller Konsultationen dieser Rechnung holen */
	public List<Konsultation> getKonsultationen(){
		Query<Konsultation> qbe = new Query<Konsultation>(Konsultation.class);
		qbe.add("RechnungsID", "=", getId());
		qbe.orderBy(false, new String[] {
			"Datum"
		});
		return qbe.execute();
	}
	
	/**
	 * @deprecated use {@link #stornoBill(boolean)} instead
	 */
	@Deprecated
	public void storno(final boolean reopen){
		stornoBill(reopen);
	}
	
	/**
	 * Rechnung stornieren. Allenfalls bereits erfolgte Zahlungen für diese Rechnungen bleiben
	 * verbucht (das Konto weist dann einen Plus-Saldo auf). Der Rechnungsbetrag wird per
	 * Stornobuchung gutgeschrieben. Sofern konsultationen freigegeben wurden, werden diese
	 * zurückgegeben.
	 * 
	 * @param reopen
	 *            wenn True werden die in dieser Rechnung enthaltenen Behandlungen wieder
	 *            freigegeben, andernfalls bleiben sie abgeschlossen.
	 * @return if reopen is true the released konsultations from the bill will be returned
	 * @since 3.3
	 */
	public List<Konsultation> stornoBill(final boolean reopen){
		InvoiceState invoiceState = InvoiceState.fromState(getStatus());
		List<Konsultation> kons = null;
		if (!InvoiceState.CANCELLED.equals(invoiceState)
			&& !InvoiceState.DEPRECIATED.equals(invoiceState)) {
			Money betrag = getBetrag();
			new Zahlung(this, betrag, "Storno", null);
			if (reopen) {
				kons = removeBillFromKons();
				setStatus(InvoiceState.CANCELLED.getState());
			} else {
				setStatus(InvoiceState.DEPRECIATED.getState());
			}
		} else if (reopen && InvoiceState.CANCELLED.equals(invoiceState)){
			// if bill is canceled ensure that all kons are opened
			kons = removeBillFromKons();
		}
		return kons;
	}

	private List<Konsultation> removeBillFromKons(){
		List<Konsultation> kons = new ArrayList<>();
		Query<Konsultation> qbe = new Query<Konsultation>(Konsultation.class);
		qbe.add(Konsultation.FLD_BILL_ID, Query.EQUALS, getId());
		for (Konsultation k : qbe.execute()) {
			k.set(Konsultation.FLD_BILL_ID, null);
			kons.add(k);
		}
		return kons;
	}
	
	/** Datum der Rechnung holen */
	public String getDatumRn(){
		return get(BILL_DATE);
	}
	
	/** Datum der ersten Konsultation dieser Rechnung holen */
	public String getDatumVon(){
		return get(BILL_DATE_FROM);
	}
	
	/** Datum der letzten Konsultation dieser Rechnung holen */
	public String getDatumBis(){
		String raw = get(BILL_DATE_UNTIL);
		return raw == null ? StringTool.leer : raw.trim();
	}
	
	/** Totalen Rechnungsbetrag holen */
	public Money getBetrag(){
		int raw = checkZero(get(BILL_AMOUNT_CENTS));
		return new Money(raw);
	}
	
	/**
	 * Since different ouputters can use different rules for rounding, the sum of the bill that an
	 * outputter created might be different from the sum, the Rechnung#build method calculated. So
	 * an outputter should always use setBetrag to correct the final amount. If the difference
	 * between the internal calculated amount and the outputter's result is more than 5 currency
	 * units or more than 2% of the sum, this method will return false an will not set the new
	 * value. Otherwise, the new value will be set, the account will be adjusted and the method
	 * returns true
	 * 
	 * @param betrag
	 *            new new sum
	 * @return true on success
	 */
	public boolean setBetrag(final Money betrag){
		// use absolute value to fix earlier bug
		
		int oldVal = Math.abs(checkZero(get(BILL_AMOUNT_CENTS)));
		if (oldVal != 0) {
			int newVal = betrag.getCents();
			int diff = Math.abs(oldVal - newVal);
			
			if ((diff > 500) || ((diff * 50) > oldVal)) {
				Money old = new Money(oldVal);
				String nr = checkNull(get(BILL_NUMBER));
				String message =
					"Der errechnete Rechnungsbetrag (" + betrag.getAmountAsString()
						+ ") weicht vom Rechnungsbetrag (" + old.getAmountAsString()
						+ ") ab. Trotzdem weiterfahren?";
				if (!cod.openQuestion("Differenz bei der Rechnung " + nr, message)) {
					return false;
				}
			}
			Query<AccountTransaction> qa = new Query<AccountTransaction>(AccountTransaction.class);
			qa.add(AccountTransaction.FLD_BILL_ID, Query.EQUALS, getId());
			qa.add(AccountTransaction.FLD_PAYMENT_ID, StringTool.leer, null);
			List<AccountTransaction> as = qa.execute();
			if ((as != null) && (as.size() == 1)) {
				AccountTransaction at = as.get(0);
				if (at.exists()) {
					Money negBetrag = new Money(betrag);
					negBetrag.negate();
					at.set(AccountTransaction.FLD_AMOUNT, negBetrag.getCentsAsString());
				}
			}
		}
		set(BILL_AMOUNT_CENTS, betrag.getCentsAsString());
		
		return true;
	}
	
	/** Offenen Betrag in Rappen/cents holen */
	public Money getOffenerBetrag(){
		List<Zahlung> lz = getZahlungen();
		// String betr=getBetrag();
		Money total = getBetrag();
		for (Zahlung z : lz) {
			Money abzahlung = z.getBetrag();
			total.subtractMoney(abzahlung);
		}
		return new Money(total);
	}
	
	/**
	 * Bereits bezahlten Betrag holen. Es werden nur positive Wert (Zahlungen) addiert, negative
	 * Werte (Gebühren) werden übergangen.
	 */
	public Money getAnzahlung(){
		List<Zahlung> lz = getZahlungen();
		Money total = new Money();
		for (Zahlung z : lz) {
			Money abzahlung = z.getBetrag();
			if (!abzahlung.isNegative()) {
				total.addMoney(abzahlung);
			}
		}
		return total;
	}
	
	/**
	 * @return the current {@link InvoiceState} numeric value
	 * @since 3.2 resolves via {@link #getInvoiceState()}
	 */
	public int getStatus(){
		return getInvoiceState().numericValue();
	}
	
	/**
	 * Resolves the current state of the invoice. If a temporary state has been set, the
	 * {@link #BILL_STATE_DATE} lies ahead. In this case the current state is resolved out of the
	 * trace histories last state value.
	 * 
	 * @return the {@link InvoiceState} of this invoice
	 * @since 3.2
	 */
	public InvoiceState getInvoiceState(){
		String[] values = new String[2];
		get(new String[] {
			BILL_STATE, BILL_STATE_DATE
		}, values);
		
		int stateNumeric = 0;
		
		TimeTool stateDate = new TimeTool(values[1]);
		if (stateDate.isAfterOrEqual(new TimeTool())) {
			// state date is in the future, hence we have a temporary state change
			// fetch current state from history
			
			@SuppressWarnings("unchecked")
			List<String> stateChanges = (List<String>) getExtInfoStoredObjectByKey(STATUS_CHANGED);
			String lastElement = stateChanges.get(stateChanges.size() - 1);
			String[] split = lastElement.split(": ");
			try {
				stateNumeric = Integer.parseInt(split[1]);
			} catch (NumberFormatException nfe) {
				log.error("Error resolving invoice state [{}] in element [{}], returning UNKNOWN.",
					split[1], lastElement);
			}
		} else {
			try {
				stateNumeric = Integer.parseInt(checkNull(get(BILL_STATE)));
			} catch (NumberFormatException nfe) {
				log.error("Error resolving invoice state [{}], returning UNKNOWN.", stateNumeric);
			}
		}
		return InvoiceState.fromState(stateNumeric);
	}
	
	/**
	 * Set a temporary state for this bill.
	 * 
	 * @param state
	 *            the temporary state to set.
	 * @param expiryDate
	 *            the date this temporary state will expire, setting the invoice back to the former
	 *            state.
	 * @since 3.2
	 */
	public void setTemporaryState(final int temporaryState, TimeTool expiryDate){
		addTrace(STATUS_CHANGED, Integer.toString(temporaryState));
		setExtInfo("TEMPORARY_STATE", Integer.toString(temporaryState));
		set(BILL_STATE_DATE, expiryDate.toString(TimeTool.DATE_GER));
	}
	
	/**
	 * Set the new invoice state. Setting the value will also update {@link #BILL_STATE_DATE} and
	 * add a trace entry.
	 * 
	 * @param state
	 *            as defined by {@link InvoiceState#numericValue()}
	 */
	public void setStatus(final int state){
		set(BILL_STATE, Integer.toString(state));
		set(BILL_STATE_DATE, new TimeTool().toString(TimeTool.DATE_GER));
		addTrace(STATUS_CHANGED, Integer.toString(state));
	}
	
	/**
	 * Eine Zahlung zufügen
	 * 
	 * @return
	 */
	public Zahlung addZahlung(final Money betrag, final String text, TimeTool date){
		if (betrag.isZero()) {
			return null;
		}
		// reset open reminder bookings if configured and bill will be fully payed
		if (CoreHub.globalCfg.get(Preferences.RNN_REMOVE_OPEN_REMINDER, false)
			&& shouldRemoveOpenReminders(betrag)) {
			removeOpenReminders();
		}
		
		Money oldOffen = getOffenerBetrag();
		int oldOffenCents = oldOffen.getCents();
		Money newOffen = new Money(oldOffen);
		newOffen.subtractMoney(betrag);
		if (newOffen.isNeglectable()) {
			setStatus(RnStatus.BEZAHLT);
		} else if (newOffen.isNegative()) {
			setStatus(RnStatus.ZUVIEL_BEZAHLT);
		} else if (newOffen.equals(getBetrag())) {
			// if the remainder is equal to the total, it was probably a
			// negative payment -> storno
			// So what might be the state of the bill after this payment?
			// It cannot simply be "OFFEN", because the bill was (almost sure)
			// printed already
			// it cannot simply be OFFEN UND GEDRUCKT, beacuse it might have
			// been 3. MAHNUNG
			// GEDRUCKT already.
			// So probably it's best to use the same state as it was before the
			// last positive
			// payment ?
			// thus let's check the last few states.
			List<String> zahlungen = getTrace(STATUS_CHANGED);
			if (zahlungen.size() < 2) {
				setStatus(RnStatus.OFFEN_UND_GEDRUCKT);
			} else {
				// status description is of the form "11.01.2008, 09:16:42: 15"
				String statusDescription = zahlungen.get(zahlungen.size() - 2);
				Matcher matcher = Pattern.compile(".*:\\s*(\\d+)").matcher(statusDescription);
				if (matcher.matches()) {
					String prevStatus = matcher.group(1);
					int newStatus = Integer.parseInt(prevStatus);
					setStatus(newStatus);
				} else {
					// we couldn't find the previous status, do nothing
				}
			}
		} else if (newOffen.getCents() < oldOffenCents) {
			setStatus(RnStatus.TEILZAHLUNG);
		}
		return new Zahlung(this, betrag, text, date);
	}
	
	private boolean shouldRemoveOpenReminders(Money betrag){
		if (hasReminders()) {
			Money open = getOffenerBetrag();
			return open.subtractMoney(betrag).equals(getRemindersBetrag());
		}
		return false;
	}
	
	public Money getRemindersBetrag(){
		Money ret = new Money(0);
		for (Zahlung zahlung : getZahlungen()) {
			String comment = zahlung.getBemerkung();
			if (comment.equals(Messages.Rechnung_Mahngebuehr1)
				|| comment.equals(Messages.Rechnung_Mahngebuehr2)
				|| comment.equals(Messages.Rechnung_Mahngebuehr3)) {
				ret.addMoney(zahlung.getBetrag());
			}
		}
		return ret.isNegative() ? ret.multiply(-1d) : ret;
	}
	
	public boolean hasReminders(){
		for (Zahlung zahlung : getZahlungen()) {
			String comment = zahlung.getBemerkung();
			if (comment.equals(Messages.Rechnung_Mahngebuehr1)
				|| comment.equals(Messages.Rechnung_Mahngebuehr2)
				|| comment.equals(Messages.Rechnung_Mahngebuehr3)) {
				return true;
			}
		}
		return false;
	}
	
	private void removeOpenReminders(){
		for (Zahlung zahlung : getZahlungen()) {
			String comment = zahlung.getBemerkung();
			if (comment.equals(Messages.Rechnung_Mahngebuehr1)
				|| comment.equals(Messages.Rechnung_Mahngebuehr2)
				|| comment.equals(Messages.Rechnung_Mahngebuehr3)) {
				zahlung.delete();
			}
		}
	}
	
	/** EIne Liste aller Zahlungen holen */
	public List<Zahlung> getZahlungen(){
		List<String> ids = getList("Zahlungen", false);
		ArrayList<Zahlung> ret = new ArrayList<Zahlung>();
		for (String id : ids) {
			Zahlung z = Zahlung.load(id);
			ret.add(z);
		}
		return ret;
	}
	
	public String getBemerkung(){
		return getExtInfo(REMARK);
	}
	
	public void setBemerkung(final String bem){
		setExtInfo(REMARK, bem);
	}
	
	public String getExtInfo(final String key){
		Hashtable<String, String> ext = loadExtension();
		String ret = ext.get(key);
		return checkNull(ret);
	}
	
	public void setExtInfo(final String key, final String value){
		Hashtable<String, String> ext = loadExtension();
		ext.put(key, value);
		flushExtension(ext);
	}
	
	/**
	 * EIn Trace-Eintrag ist eine Notiz über den Verlauf. (Z.B. Statusänderungen, Zahlungen,
	 * Rückbuchungen etc.)
	 * 
	 * @param name
	 *            Name des Eintragstyps
	 * @param text
	 *            Text zum Eintrag
	 */
	@SuppressWarnings("unchecked")
	public void addTrace(final String name, final String text){
		Hashtable hash = loadExtension();
		byte[] raw = (byte[]) hash.get(name);
		List<String> trace = null;
		if (raw != null) {
			trace = StringTool.unpack(raw);
		}
		if (trace == null) {
			trace = new ArrayList<String>();
		}
		trace.add(new TimeTool().toString(TimeTool.FULL_GER) + ": " + text);
		hash.put(name, StringTool.pack(trace));
		flushExtension(hash);
	}
	
	/**
	 * ALle Einträge zu einem bestimmten Eintragstyp holen
	 * 
	 * @param name
	 *            Name des Eintragstyps (z.B. "Zahlungen")
	 * @return eine List<String>, welche leer sein kann
	 */
	@SuppressWarnings("unchecked")
	public List<String> getTrace(final String name){
		Hashtable hash = loadExtension();
		byte[] raw = (byte[]) hash.get(name);
		List<String> trace = null;
		if (raw != null) {
			trace = StringTool.unpack(raw);
		}
		if (trace == null) {
			trace = new ArrayList<String>();
		}
		return trace;
	}
	
	public String getRnDatumFrist(){
		Mandant currMandant = (Mandant) ElexisEventDispatcher.getSelected(Mandant.class);
		Settings rnsSettings = CoreHub.getUserSetting(currMandant.getRechnungssteller());
		
		String stat = get(BILL_STATE_DATE);
		int frist = 0;
		switch (getStatus()) {
		case RnStatus.OFFEN_UND_GEDRUCKT:
			frist = rnsSettings.get(Preferences.RNN_DAYSUNTIL1ST, 30);
			break;
		case RnStatus.MAHNUNG_1_GEDRUCKT:
			frist = rnsSettings.get(Preferences.RNN_DAYSUNTIL2ND, 10);
			break;
		case RnStatus.MAHNUNG_2_GEDRUCKT:
			frist = rnsSettings.get(Preferences.RNN_DAYSUNTIL3RD, 10);
			break;
		}
		TimeTool tm = new TimeTool(stat);
		tm.add(TimeTool.DAY_OF_MONTH, frist);
		return tm.toString(TimeTool.DATE_GER);
	}
	
	/**
	 * Mark bill as rejected
	 */
	public void reject(final RnStatus.REJECTCODE reason, final String text){
		setStatus(RnStatus.FEHLERHAFT);
		addTrace(REJECTED, reason.toString() + ", " + text);
	}
	
	@SuppressWarnings("unchecked")
	// TODO weird
	public Hashtable<String, String> loadExtension(){
		return (Hashtable<String, String>) getMap(FLD_EXTINFO);
	}
	
	@SuppressWarnings("unchecked")
	public void flushExtension(final Hashtable ext){
		setMap(FLD_EXTINFO, ext);
	}
	
	public static Rechnung load(final String id){
		Rechnung ret = new Rechnung(id);
		if (ret.exists()) {
			return ret;
		}
		return null;
	}
	
	/**
	 * Eien Rechnung anhand ihrer Nummer holen
	 * 
	 * @param Rnnr
	 *            die Rechnungsnummer
	 * @return die Rechnung mit dieser Nummer oder Null, wenn keine Rechnung mit dieser Nummer
	 *         existiert.
	 */
	public static Rechnung getFromNr(final String Rnnr){
		String id = new Query<Rechnung>(Rechnung.class).findSingle(BILL_NUMBER, Query.EQUALS, Rnnr);
		Rechnung ret = load(id);
		if (ret.isValid()) {
			return ret;
		} else {
			return null;
		}
	}
	
	/** Die nächste Rechnungsnummer holen. */
	public static String getNextRnNummer(){
		JdbcLink j = getConnection();
		Stm stm = j.getStatement();
		String nr = null;
		while (true) {
			// Zugriff sperren
			String lockid = PersistentObject.lock("RechnungsNummer", true);
			// letzte Nummer holen
			String pid = j.queryString("SELECT WERT FROM CONFIG WHERE PARAM='RechnungsNr'");
			// ggf. Initialisieren
			if (StringTool.isNothing(pid)) {
				pid = "0";
				j.exec("INSERT INTO CONFIG (PARAM,WERT) VALUES ('RechnungsNr','0')");
			}
			// hochzählen
			int lastNum = Integer.parseInt(pid) + 1;
			nr = Integer.toString(lastNum);
			// neue Höchstzahl speichern
			j.exec("UPDATE CONFIG SET WERT='" + nr + "' WHERE PARAM='RechnungsNr'");
			// Sperre lösen
			PersistentObject.unlock("RechnungsNummer", lockid);
			// Nochmal vergewissern, dass diese Nummer wirklich noch nicht
			// existiert, sonst nächste
			// Nummer holen
			String exists =
				j.queryString("SELECT ID FROM RECHNUNGEN WHERE RnNummer=" + JdbcLink.wrap(nr));
			if (exists == null) {
				break;
			}
		}
		j.releaseStatement(stm);
		return nr;
	}
	
	protected Rechnung(){ /* leer */
	}
	
	protected Rechnung(final String id){
		super(id);
	}
	
	@Override
	public boolean delete(){
		for (Zahlung z : getZahlungen()) {
			z.set(Zahlung.BILL_ID, StringTool.leer); // avoid log entries
			z.delete();
			z.set(Zahlung.BILL_ID, getId());
		}
		return super.delete();
	}
	
	@Override
	public String getLabel(){
		String[] vals = get(true, CASE_ID, BILL_NUMBER, BILL_DATE, BILL_AMOUNT_CENTS);
		
		StringBuilder sb = new StringBuilder();
		sb.append(vals[1]).append(" ").append(vals[2]);
		Fall fall = Fall.load(vals[0]);
		if ((fall != null) && fall.exists()) {
			sb.append(": ").append(fall.getPatient().getLabel()).append(" ");
		}
		int value = checkZero(vals[3]);
		sb.append(new Money(value));
		return sb.toString();
	}
	
	/**
	 * Eine einfache eindeutige ID für die Rechnung liefern (Aus PatNr. und RnNr)
	 * 
	 * @return
	 */
	public String getRnId(){
		Patient p = getFall().getPatient();
		String pid;
		if (CoreHub.globalCfg.get("PatIDMode", "number").equals("number")) {
			pid = StringTool.pad(StringTool.LEFT, '0', p.getPatCode(), 6);
		} else {
			pid = new TimeTool(p.getGeburtsdatum()).toString(TimeTool.DATE_COMPACT);
		}
		String nr = StringTool.pad(StringTool.LEFT, '0', getNr(), 6);
		return pid + nr;
	}
	
	/**
	 * Retrieve the state a bill had at a given moment
	 * 
	 * @param date
	 *            the time to consider
	 * @return the Status the bill had at this moment
	 */
	public int getStatusAtDate(TimeTool date){
		List<String> trace = getTrace(Rechnung.STATUS_CHANGED);
		int ret = getStatus();
		TimeTool tt = new TimeTool();
		for (String s : trace) {
			String[] stm = s.split("\\s*:\\s");
			if (tt.set(stm[0])) {
				if (tt.isBefore(date)) {
					ret = Integer.parseInt(stm[1]);
				}
			}
		}
		return ret;
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	/**
	 * Checks if a bill is correctable by state
	 * 
	 * @return
	 */
	public boolean isCorrectable(){
		String rechnungsNr = getNr();
		if (rechnungsNr != null && rechnungsNr.isEmpty()) {
			return false;
		}
		InvoiceState invoiceState = getInvoiceState();
		
		if (invoiceState != null) {
			switch (invoiceState) {
			case OWING:
			case TO_PRINT:
			case PARTIAL_LOSS:
			case TOTAL_LOSS:
			case DEPRECIATED:
			case CANCELLED:
				return false;
			case BILLED:
			case DEFECTIVE:
			case DEMAND_NOTE_1:
			case DEMAND_NOTE_1_PRINTED:
			case DEMAND_NOTE_2:
			case DEMAND_NOTE_2_PRINTED:
			case DEMAND_NOTE_3:
			case DEMAND_NOTE_3_PRINTED:
			case EXCESSIVE_PAYMENT:
			case FROM_TODAY:
			case IN_EXECUTION:
			case NOT_BILLED:
			case NOT_FROM_TODAY:
			case NOT_FROM_YOU:
			case ONGOING:
			case OPEN:
			case OPEN_AND_PRINTED:
			case PAID:
			case PARTIAL_PAYMENT:
			case REJECTED:
			case STOP_LEGAL_PROCEEDING:
			case UNKNOWN:
				return true;
			default:
				break;
			}
		}
		return false;
	}
}
