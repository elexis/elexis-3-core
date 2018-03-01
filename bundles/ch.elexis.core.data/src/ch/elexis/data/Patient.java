/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    M. Descher - added loadByPatientID
 * 
 *******************************************************************************/
package ch.elexis.data;

import static ch.elexis.core.model.PatientConstants.FLD_EXTINFO_LEGAL_GUARDIAN;
import static ch.elexis.core.model.PatientConstants.FLD_EXTINFO_STAMMARZT;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.prescription.EntryType;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.TimeTool.TimeFormatException;

/**
 * Ein Patient ist eine Person (und damit auch ein Kontakt), mit folgenden zusätzlichen
 * Eigenschaften
 * <ul>
 * <li>Anamnesen : PA, SA, FA</li>
 * <li>Fixe Diagnosen</li>
 * <li>Fixe Medikation</li>
 * <li>Risiken</li>
 * <li>Einer Liste der Fälle, die zu diesem Patienten existieren</li>
 * <li>Einer Liste der Garanten, die diesem Patienten zugeordnet wurden</li>
 * <li>Einer Liste aller Forderungen und Zahlungen im Verkehr mit diesem Patienten</li>
 * </ul>
 * 
 * @author gerry
 * 
 */
public class Patient extends Person {
	
	public static final String FLD_ALLERGIES = "Allergien";
	public static final String FLD_RISKS = "Risiken";
	public static final String FLD_GROUP = "Gruppe";
	public static final String FLD_DIAGNOSES = "Diagnosen";
	public static final String FLD_PATID = "PatientNr";
	public final static String FLD_NAME = "Name";
	public static final String FLD_FIRSTNAME = "Vorname";
	public static final String FLD_SEX = "Geschlecht";
	public static final String FLD_DOB = "Geburtsdatum";
	public static final String FLD_STREET = "Strasse";
	public static final String FLD_ZIP = "Plz";
	public static final String FLD_PLACE = "Ort";
	public static final String FLD_PHONE1 = "Telefon1";
	public static final String FLD_FAX = "Fax";
	public static final String FLD_BALANCE = "Konto";
	public static final String FLD_PERS_ANAMNESE = "PersAnamnese";
	public static final String FLD_SYS_ANAMNESE = "SysAnamnese";
	public static final String FLD_FAM_ANAMNESE = "FamilienAnamnese";
	
	public static final String[] DEFAULT_SORT = {
		FLD_NAME, FLD_FIRSTNAME, FLD_DOB
	};
	
	static {
		addMapping(Kontakt.TABLENAME, FLD_DIAGNOSES + "    	=S:C:Diagnosen",
			FLD_PERS_ANAMNESE + "	=S:C:PersAnamnese", "SystemAnamnese	 	=S:C:SysAnamnese",
			"FamilienAnamnese	=S:C:FamAnamnese", FLD_RISKS, FLD_ALLERGIES,
			"Faelle				=LIST:PatientID:FAELLE:DatumVon",
			"Garanten			=JOINT:GarantID:PatientID:PATIENT_GARANT_JOINT:"
				+ Kontakt.class.getCanonicalName(),
			"Dauermedikation	=JOINT:ArtikelID:PatientID:PATIENT_ARTIKEL_JOINT:"
				+ Artikel.class.getCanonicalName(),
			FLD_BALANCE + "			=LIST:PatientID:KONTO", FLD_GROUP, FLD_PATID,
			Kontakt.FLD_IS_PATIENT);
	}
	
	public String getDiagnosen(){
		return get(FLD_DIAGNOSES);
	}
	
	public String getPersAnamnese(){
		return get("PersAnamnese");
	}
	
	/**
	 * @deprecated unused, to be removed
	 */
	public String getSystemAnamnese(){
		return get("Systemanamnese");
	}
	
	protected Patient(){/* leer */
	}
	
	@Override
	public boolean isValid(){
		if (!super.isValid()) {
			return false;
		}
		String geb = (get(Person.BIRTHDATE));
		if (geb.equals("WERT?")) {
			return false;
		}
		String g = get(Person.SEX);
		if (g.equals(Person.MALE) || g.equals(Person.FEMALE)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Dieser oder der folgende Konstruktor sollte normalerweise verwendet werden, um einen neuen,
	 * bisher noch nicht in der Datenbank vorhandenen Patienten anzulegen.
	 * 
	 * @param Name
	 * @param Vorname
	 * @param Geburtsdatum
	 *            Als String in Notation dd.mm.jj
	 * @param s
	 *            Geschlecht m oder w
	 */
	public Patient(final String Name, final String Vorname, final String Geburtsdatum,
		final String s){
		super(Name, Vorname, Geburtsdatum, s);
		getPatCode();
	}
	
	/**
	 * This constructor is more critical than the previous one
	 * 
	 * @param name
	 *            will be checked for non-alphabetic characters
	 * @param vorname
	 *            will be checked for non alphabetiic characters
	 * @param gebDat
	 *            will be checked for unplausible values
	 * @param s
	 *            will be checked for undefined values
	 * @throws TimeFormatException
	 */
	public Patient(final String name, final String vorname, final TimeTool gebDat, final String s)
		throws PersonDataException{
		super(name, vorname, gebDat, s);
		getPatCode();
	}
	
	/**
	 * Eine Liste aller zu diesem Patient gehörenden Fälle liefern
	 * 
	 * @return Array mit allen Fällen (das die Länge null haben kann)
	 */
	public Fall[] getFaelle(){
		List<String> cas = getList("Faelle", true);
		Fall[] ret = new Fall[cas.size()];
		int i = 0;
		for (String id : cas) {
			Fall fall = Fall.load(id);
			fall.setDBConnection(getDBConnection());
			ret[i++] = fall;
		}
		return ret;
	}
	
	/**
	 * Get the patients active medication filtered by {@link EntryType}.
	 * 
	 * @param filterType
	 *            or null
	 * @return
	 */
	public List<Prescription> getMedication(@Nullable EntryType... filterType){
		// prefetch the values needed for filter operations
		Query<Prescription> qbe = new Query<Prescription>(Prescription.class, null, null,
			Prescription.TABLENAME, new String[] {
				Prescription.FLD_DATE_UNTIL, Prescription.FLD_REZEPT_ID,
				Prescription.FLD_PRESC_TYPE, Prescription.FLD_ARTICLE
			});
		qbe.add(Prescription.FLD_PATIENT_ID, Query.EQUALS, getId());
		List<Prescription> prescriptions = qbe.execute();
		// make sure just now closed are not included
		TimeTool now = new TimeTool();
		now.add(TimeTool.SECOND, 10);
		
		if (filterType != null && filterType.length > 0) {
			EnumSet<EntryType> entryTypes = EnumSet.copyOf(Arrays.asList(filterType));
			return prescriptions.parallelStream()
				.filter(p -> entryTypes.contains(p.getEntryType()) && !p.isStopped(now))
				.collect(Collectors.toList());
		} else {
			return prescriptions.parallelStream().filter(p -> !p.isStopped(now))
				.collect(Collectors.toList());
		}
	}
	
	/**
	 * Get the patients medication filtered by {@link EntryType} as text.
	 * 
	 * @param filterType
	 *            or null
	 * @return
	 */
	public String getMedicationText(@Nullable EntryType filterType){
		List<Prescription> prescriptions = getMedication(filterType);
		StringBuilder sb = new StringBuilder();
		
		prescriptions.stream().forEach(p -> {
			if(sb.length() > 0) {
				sb.append(StringTool.lf);
			}
			sb.append(p.getLabel());	
		});
		return sb.toString();
	}
	
	/**
	 * Fixmedikation dieses Patienten einlesen
	 * 
	 * @return ein Array aus {@link Prescription}
	 * @deprecated does not filter by EntryType, use {@link Patient#getMedication(EntryType)}
	 *             instead.
	 */
	public Prescription[] getFixmedikation(){
		Query<Prescription> qbe = new Query<Prescription>(Prescription.class);
		qbe.add(Prescription.FLD_PATIENT_ID, Query.EQUALS, getId());
		qbe.add(Prescription.FLD_REZEPT_ID, StringTool.leer, null);
		String today = new TimeTool().toString(TimeTool.DATE_COMPACT);
		qbe.startGroup();
		qbe.add(Prescription.FLD_DATE_UNTIL, Query.GREATER_OR_EQUAL, today);
		qbe.or();
		qbe.add(Prescription.FLD_DATE_UNTIL, StringTool.leer, null);
		qbe.endGroup();
		List<Prescription> l = qbe.execute();
		
		return l.toArray(new Prescription[0]);
	}
	
	/**
	 * ReserveMedikation als Text wird unter anderem fuer Platzhalter verwendet
	 * 
	 * @return
	 */
	public String getReserveMedikation(){
		return getMedicationText(EntryType.RESERVE_MEDICATION);
	}
	
	/**
	 * Fixmedikation als Text
	 * 
	 * @return
	 */
	public String getMedikation(){
		return getMedicationText(EntryType.FIXED_MEDICATION);
	}
	
	/**
	 * Die neueste Konsultation dieses Patienten holen, soweit eruierbar
	 * 
	 * @param create
	 *            : eine Kons erstellen, falls keine existiert
	 * @return die letzte Konsultation oder null
	 */
	
	public Konsultation getLetzteKons(final boolean create){
		if (ElexisEventDispatcher.getSelectedMandator() == null) {
			MessageEvent.fireError("Kein Mandant angemeldet", "Es ist kein Mandant angemeldet.");
			return null;
		}
		Query<Konsultation> qbe = new Query<Konsultation>(Konsultation.class);
		
		// if not configured otherwise load only consultations of active mandant
		if (!CoreHub.userCfg.get(Preferences.USR_DEFLOADCONSALL, false)) {
			Mandant mandator = ElexisEventDispatcher.getSelectedMandator();
			if (mandator != null) {
				qbe.add(Konsultation.FLD_MANDATOR_ID, Query.EQUALS, mandator.getId());
			}
		}
		
		// qbe.add("Datum", "=", new
		// TimeTool().toString(TimeTool.DATE_COMPACT));
		
		Fall[] faelle = getFaelle();
		if ((faelle == null) || (faelle.length == 0)) {
			return create ? createFallUndKons() : null;
		}
		qbe.startGroup();
		boolean termInserted = false;
		for (Fall fall : faelle) {
			if (fall.isOpen()) {
				qbe.add(Konsultation.FLD_CASE_ID, Query.EQUALS, fall.getId());
				qbe.or();
				termInserted = true;
			}
		}
		if (!termInserted) {
			return create ? createFallUndKons() : null;
		}
		qbe.endGroup();
		qbe.orderBy(true, Konsultation.DATE);
		List<Konsultation> list = qbe.execute();
		if ((list == null) || list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
	}
	
	/**
	 * Finds the last non deleted {@link Konsultation} over all mandants
	 * 
	 * @return
	 */
	public Konsultation getLastKonsultation(){
		Konsultation fromDB = null;
		if (getId() != null) {
			PreparedStatement preparedStatement =
				PersistentObject.getDefaultConnection().getPreparedStatement(
					"SELECT BH.id FROM BEHANDLUNGEN BH LEFT JOIN FAELLE FA ON BH.FallID = FA.id AND BH.deleted = FA.deleted WHERE FA.PatientID = ? and FA.deleted = '0' order by BH.Datum desc, BH.lastupdate desc limit 1");
			try {
				preparedStatement.setString(1, getId());
				ResultSet results = preparedStatement.executeQuery();
				// map key date string, list string ids of results
				if ((results != null) && (results.next() == true)) {
					String consId = results.getString(1);
					fromDB = Konsultation.load(consId);
				}
			} catch (SQLException e) {
				LoggerFactory.getLogger(Patient.class)
					.error("Could not load consultations of patient [" + getId() + "]", e);
			} finally {
				PersistentObject.getDefaultConnection().releasePreparedStatement(preparedStatement);
			}
		}
		return fromDB;
	}
	
	public Konsultation createFallUndKons(){
		Fall fall = neuerFall(Fall.getDefaultCaseLabel(), Fall.getDefaultCaseReason(),
			Fall.getDefaultCaseLaw());
		Konsultation k = fall.neueKonsultation();
		k.setMandant(ElexisEventDispatcher.getSelectedMandator());
		return k;
	}
	
	/**
	 * Einen neuen Fall erstellen und an den Patienten binden
	 * 
	 * @return der eben erstellte Fall oder null bei Fehler
	 */
	public Fall neuerFall(final String Bezeichnung, final String grund,
		final String Abrechnungsmethode){
		Fall fall = new Fall(getId(), Bezeichnung, grund, Abrechnungsmethode);
		ElexisEventDispatcher.reload(Fall.class);
		return fall;
	}
	
	/**
	 * Einen Kurzcode, der diesen Patienten identifiziert, zurückliefern. Der Kurzcode kann je nach
	 * Voreinstellung eine eindeutige, jeweils nur einmal vergebene Nummer sein, oder ein aus den
	 * Personalien gebildetes Kürzel. Dieser Code kann beispielsweise als Index für die Archivierung
	 * der KG's in Papierform verwendet werden.
	 * 
	 * @return einen String, (der eine Zahl sein kann), und der innerhalb dieser Installation
	 *         eindeutig ist.
	 */
	public String getPatCode(){
		String rc = get(FLD_PATID);
		if (!StringTool.isNothing(rc)) {
			return rc;
		}
		while (true) {
			String lockid = PersistentObject.lock("PatNummer", true);
			String pid = getDBConnection()
				.queryString("SELECT WERT FROM CONFIG WHERE PARAM='PatientNummer'");
			if (StringTool.isNothing(pid)) {
				pid = "0";
				getDBConnection()
					.exec("INSERT INTO CONFIG (PARAM,WERT) VALUES ('PatientNummer','0')");
			}
			int lastNum = Integer.parseInt(pid) + 1;
			rc = Integer.toString(lastNum);
			getDBConnection().exec("UPDATE CONFIG set wert='" + rc + "', lastupdate="
				+ Long.toString(System.currentTimeMillis()) + " where param='PatientNummer'");
			PersistentObject.unlock("PatNummer", lockid);
			String exists = getDBConnection()
				.queryString("SELECT ID FROM KONTAKT WHERE PatientNr=" + JdbcLink.wrap(rc));
			if (exists == null) {
				break;
			}
		}
		set(FLD_PATID, rc);
		return rc;
	}
	
	public Money getKontostand(){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT betrag FROM KONTO WHERE PatientID=").append(getWrappedId());
		Stm stm = getDBConnection().getStatement();
		Money konto = new Money();
		try {
			ResultSet res = stm.query(sql.toString());
			while (res.next()) {
				int buchung = res.getInt(1);
				konto.addCent(buchung);
			}
			return konto;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		} finally {
			getDBConnection().releaseStatement(stm);
		}
	}
	
	/**
	 * to be used by the platzhalter system, allows for presentation of the current balance of this
	 * patient by using [Patient.Konto]
	 * 
	 * @return the current balance of the patient
	 */
	public String getBalance(){
		return getKontostand().getAmountAsString();
	}
	
	/**
	 * Calculates a possibly available account excess. (This value may be added to a bill as
	 * prepayment.)
	 * <p>
	 * Considers all overpaid bills and account transactions not bound to a bill. The garant of the
	 * bill must be the patient itself. (Bills not yet paid or partly paid are not considered.)
	 * <p>
	 * This value is not the same as the current account balance, since we ignore outstanding debts
	 * of not yet paid bills.
	 * 
	 * @return the account excess (may be zero or positive)
	 */
	public Money getAccountExcess(){
		Money prepayment = new Money();
		
		// overpaid bills of this patient
		// TODO do an optimized query over KONTAKT/FALL/RECHNUNG
		Query<Rechnung> rQuery = new Query<Rechnung>(Rechnung.class);
		
		// normally do not display other mandator's balance
		if (CoreHub.acl.request(AccessControlDefaults.ACCOUNTING_GLOBAL) == false) {
			Mandant mandator = ElexisEventDispatcher.getSelectedMandator();
			if (mandator != null) {
				rQuery.add(Rechnung.MANDATOR_ID, Query.EQUALS, mandator.getId());
			}
		}
		
		// let the database engine do the filtering
		Fall[] faelle = getFaelle();
		if ((faelle != null) && (faelle.length > 0)) {
			rQuery.startGroup();
			for (Fall fall : faelle) {
				rQuery.add(Rechnung.CASE_ID, Query.EQUALS, fall.getId());
				rQuery.or();
			}
			rQuery.endGroup();
		}
		
		List<Rechnung> rechnungen = rQuery.execute();
		if (rechnungen != null) {
			for (Rechnung rechnung : rechnungen) {
				Fall fall = rechnung.getFall();
				if (fall != null) { // of course this should never happen
					Query<AccountTransaction> atQuery =
						new Query<AccountTransaction>(AccountTransaction.class);
					atQuery.add(AccountTransaction.FLD_PATIENT_ID, Query.EQUALS, getId());
					atQuery.add(AccountTransaction.FLD_BILL_ID, Query.EQUALS, rechnung.getId());
					
					List<AccountTransaction> transactions = atQuery.execute();
					if (transactions != null) {
						Money sum = new Money();
						for (AccountTransaction transaction : transactions) {
							sum.addMoney(transaction.getAmount());
						}
						if (sum.getCents() > 0) {
							prepayment.addMoney(sum);
						}
					}
				}
			}
		}
		
		// account (sum over all account transactions not assigned to a bill)
		Query<AccountTransaction> atQuery = new Query<AccountTransaction>(AccountTransaction.class);
		atQuery.add(AccountTransaction.FLD_PATIENT_ID, Query.EQUALS, getId());
		List<AccountTransaction> transactions = atQuery.execute();
		if (transactions != null) {
			Money sum = new Money();
			for (AccountTransaction transaction : transactions) {
				Rechnung rechnung = transaction.getRechnung();
				if ((rechnung == null) || !rechnung.exists()) {
					sum.addMoney(transaction.getAmount());
				}
			}
			prepayment.addMoney(sum);
		}
		
		return prepayment;
	}
	
	/** Einen Patienten mit gegebener ID aus der Datenbank einlesen */
	public static Patient load(final String id){
		Patient ret = new Patient(id);
		return ret;
	}
	
	/**
	 * Einen Patienten aufgrund seiner PatientenNr laden
	 * 
	 * @param patientNr
	 * @return Patient falls gefunden, <code>null</code> wenn nicht gefunden
	 */
	public static Patient loadByPatientID(String patientNr){
		String patID = new Query<Patient>(Patient.class).findSingle(Patient.FLD_PATID, Query.EQUALS,
			patientNr);
		return Patient.load(patID);
	}
	
	private Patient(final String id){
		super(id);
	}
	
	@Override
	protected String getConstraint(){
		return new StringBuilder(Kontakt.FLD_IS_PATIENT).append(Query.EQUALS)
			.append(JdbcLink.wrap(StringConstants.ONE)).toString();
	}
	
	@Override
	protected void setConstraint(){
		set(new String[] {
			Kontakt.FLD_IS_PATIENT, Kontakt.FLD_IS_PERSON
		}, StringConstants.ONE, StringConstants.ONE);
	}
	
	@Override
	/*
	 * * Return a short or long label for this Patient
	 * 
	 * This implementation returns "<Vorname> <Name>" for the sort label, and calls getPersonalia()
	 * for the long label.
	 * 
	 * @return a label describing this Patient
	 */
	public String getLabel(final boolean shortLabel){
		if (shortLabel) {
			return super.getLabel(true);
		} else {
			return getPersonalia();
		}
	}
	
	/**
	 * We do not allow direct deletion -> use remove instead
	 */
	@Override
	public boolean delete(){
		return delete(false);
	}
	
	/**
	 * Einen Patienten aus der Datenbank entfernen. Dabei werden auch alle verknüpften Daten
	 * gelöscht (Labor, Rezepte, AUF, Rechnungen etc.) Plugins, welche patientenspezifische Daten
	 * speichern, sollten diese ebenfalls löschen (sie erhalten einen ObjectEvent)
	 * 
	 * @param force
	 *            bei true wird der Patient auf jeden Faöll gelöscht, bei false nur, wenn keine
	 *            Fälle von ihm existieren.
	 * @return false wenn der Patient nicht gelöscht werden konnte.
	 */
	public boolean delete(final boolean force){
		Fall[] fl = getFaelle();
		if ((fl.length == 0) || ((force == true)
			&& (CoreHub.acl.request(AccessControlDefaults.DELETE_FORCED) == true))) {
			for (Fall f : fl) {
				f.delete(true);
			}
			delete_dependent();
			return super.delete();
		}
		return false;
	}
	
	private boolean delete_dependent(){
		for (LabResult lr : new Query<LabResult>(LabResult.class, LabResult.PATIENT_ID, getId())
			.execute()) {
			lr.delete();
		}
		for (Rezept rp : new Query<Rezept>(Rezept.class, Rezept.PATIENT_ID, getId()).execute()) {
			rp.delete();
		}
		for (Brief br : new Query<Brief>(Brief.class, Brief.FLD_PATIENT_ID, getId()).execute()) {
			br.delete();
		}
		for (AccountTransaction at : new Query<AccountTransaction>(AccountTransaction.class,
			AccountTransaction.FLD_PATIENT_ID, getId()).execute()) {
			at.delete();
		}
		return true;
	}
	
	@Override
	public boolean isDragOK(){
		return true;
	}
	
	/**
	 * Eine Auftragsnummer erstellen. Diese enthält die Patientennummer ergänzt mit der
	 * Modulo10-Prüfsumme über diese Nummer, plus die aktuelle Uhrzeit als -hhmm
	 * 
	 * @return eine verifizierbare Auftragsnummer.
	 */
	public String getAuftragsnummer(){
		String pid = StringTool.addModulo10(getPatCode()) + "-" //$NON-NLS-1$
			+ new TimeTool().toString(TimeTool.TIME_COMPACT);
		return pid;
	}
	
	/**
	 * Das Alter des Patienten in Jahren errechnen
	 * 
	 * @return Das Alter in ganzen Jahren als String
	 */
	public String getAlter(){
		return Long.toString(getAgeAt(LocalDateTime.now(), ChronoUnit.YEARS));
	}
	
	/**
	 * Return all bills of this patient
	 * 
	 * @return a list of bills of this patient
	 */
	public List<Rechnung> getRechnungen(){
		List<Rechnung> rechnungen = new ArrayList<Rechnung>();
		
		Fall[] faelle = getFaelle();
		if ((faelle != null) && (faelle.length > 0)) {
			Query<Rechnung> query = new Query<Rechnung>(Rechnung.class);
			query.insertTrue();
			query.startGroup();
			for (Fall fall : faelle) {
				query.add(Rechnung.CASE_ID, Query.EQUALS, fall.getId());
				query.or();
			}
			query.endGroup();
			
			List<Rechnung> rnList = query.execute();
			if (rnList != null) {
				rechnungen.addAll(rnList);
			}
		}
		
		return rechnungen;
	}
	
	// PatientDetailView backport from 2.2 - databinding bean compatibility
	public String getAllergies(){
		return get(FLD_ALLERGIES);
	}
	
	public void setAllergies(String allergien){
		set(FLD_ALLERGIES, allergien);
	}
	
	public String getPersonalAnamnese(){
		return get(FLD_PERS_ANAMNESE);
	}
	
	public void setPersonalAnamnese(String anamnese){
		set(FLD_PERS_ANAMNESE, anamnese);
	}
	
	public String getComment(){
		return get(FLD_REMARK);
	}
	
	public void setComment(String bemerkungen){
		set(FLD_REMARK, bemerkungen);
	}
	
	public String getFamilyAnamnese(){
		return get(FLD_FAM_ANAMNESE);
	}
	
	public void setFamilyAnamnese(String anamnese){
		set(FLD_FAM_ANAMNESE, anamnese);
	}
	
	public void setDiagnosen(String diagnosen){
		set(FLD_DIAGNOSES, diagnosen);
	}
	
	public String getRisk(){
		return get(FLD_RISKS);
	}
	
	public void setRisk(String risk){
		set(FLD_RISKS, risk);
	}
	
	public void removeStammarzt(){
		removeFromExtInfo(FLD_EXTINFO_STAMMARZT);
	}
	
	public void setStammarzt(Kontakt stammarzt){
		if (stammarzt == null)
			return;
		// we override the name to force PersistentObject#get(String) to revert
		// to the method getStammarzt to fetch the entry
		setExtInfoStoredObjectByKey(FLD_EXTINFO_STAMMARZT, stammarzt.getId());
	}
	
	/**
	 * @return Stammarzt for the patient if defined, else <code>null</code>
	 */
	public Kontakt getStammarzt(){
		// we override the name to force PersistentObject#get(String) to revert
		// to the method getStammarzt to fetch the entry
		// unfortunately lots of PersistentObject: field is not mapped Stammarzt
		// will be thrown ..
		return (getExtInfoStoredObjectByKey(FLD_EXTINFO_STAMMARZT) != null)
				? Kontakt.load((String) getExtInfoStoredObjectByKey(FLD_EXTINFO_STAMMARZT)) : null;
	}
	
	public void setLegalGuardian(Kontakt legalGuardian){
		if (legalGuardian == null) {
			removeFromExtInfo(FLD_EXTINFO_LEGAL_GUARDIAN);
			return;
		}
		setExtInfoStoredObjectByKey(FLD_EXTINFO_LEGAL_GUARDIAN, legalGuardian.getId());
	}
	
	public Kontakt getLegalGuardian(){
		Object guardianId = getExtInfoStoredObjectByKey(FLD_EXTINFO_LEGAL_GUARDIAN);
		if (guardianId != null && !((String) guardianId).isEmpty()) {
			return Kontakt.load((String) guardianId);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private void removeFromExtInfo(String key){
		Map h = getMap(FLD_EXTINFO);
		h.remove(key);
		setMap(FLD_EXTINFO, h);
	}
	
	public long getAgeAt(LocalDateTime dateTime, ChronoUnit chronoUnit){
		LocalDateTime birthDateTime = new TimeTool(getGeburtsdatum()).toLocalDateTime();
		return chronoUnit.between(birthDateTime, dateTime);
	}
}
