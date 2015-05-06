/*******************************************************************************
 * Copyright (c) 2005-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.data;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ExtensionPointConstantsData;
import ch.elexis.core.data.interfaces.IRnOutputter;
import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.elexis.core.data.util.Extensions;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Ein Fall ist eine Serie von zusammengehörigen Behandlungen. Ein Fall hat einen Garanten, ein
 * Anfangsdatum ein Enddatum, eine Bezeichnung und allenfalls ein Enddatum
 * 
 * @author Gerry
 * 
 */
public class Fall extends PersistentObject {
	
	public static final String VVG_NAME = Messages.Fall_VVG_Name;
	public static final String PRIVATE_NAME = Messages.Fall_Private_Name; //$NON-NLS-1$
	public static final String MV_NAME = Messages.Fall_MV_Name;
	public static final String IV_NAME = Messages.Fall_IV_Name;
	private static final String KVG_REQUIREMENTS = Messages.Fall_KVGRequirements; //$NON-NLS-1$
	public static final String KVG_NAME = Messages.Fall_KVG_Name;
	public static final String UVG_NAME = Messages.Fall_UVG_Name;
	public static final String UVG_REQUIREMENTS = Messages.Fall_UVGRequirements; //$NON-NLS-1$
	public static final String CONST_TARMED_DRUCKER = Messages.Fall_TarmedPrinter; //$NON-NLS-1$
	public static final String CONST_TARMED_LEISTUNG = Messages.Fall_TarmedLeistung; //$NON-NLS-1$
	public static final String FLD_BEHANDLUNGEN = "Behandlungen"; //$NON-NLS-1$
	public static final String FLD_BILLING = "billing"; //$NON-NLS-1$
	public static final String FLD_KOSTENTRAEGER = "Kostentraeger"; //$NON-NLS-1$
	public static final String FLD_RECHNUNGSSTELLER_ID = "RechnungsstellerID"; //$NON-NLS-1$
	public static final String FLD_DATUM_BIS = "DatumBis"; //$NON-NLS-1$
	public static final String FLD_DATUM_VON = "DatumVon"; //$NON-NLS-1$
	public static final String FLD_RN_PLANUNG = "RnPlanung"; //$NON-NLS-1$
	public static final String FLD_FALL_NUMMER = "FallNummer"; //$NON-NLS-1$
	public static final String FLD_VERS_NUMMER = "VersNummer"; //$NON-NLS-1$
	public static final String FLD_BEZEICHNUNG = "Bezeichnung"; //$NON-NLS-1$
	public static final String FLD_GARANT_ID = "GarantID"; //$NON-NLS-1$
	public static final String FLD_GRUND = "Grund"; //$NON-NLS-1$
	public static final String PATIENT_ID = "PatientID"; //$NON-NLS-1$
	static final String TABLENAME = "FAELLE"; //$NON-NLS-1$
	public static final String TYPE_DISEASE = Messages.Fall_Disease; //$NON-NLS-1$
	public static final String TYPE_ACCIDENT = Messages.Fall_Accident; //$NON-NLS-1$
	public static final String TYPE_MATERNITY = Messages.Fall_Maternity; //$NON-NLS-1$
	public static final String TYPE_PREVENTION = Messages.Fall_Prevention; //$NON-NLS-1$
	public static final String TYPE_BIRTHDEFECT = Messages.Fall_Birthdefect; //$NON-NLS-1$
	public static final String TYPE_OTHER = Messages.Fall_Other;
	public static final String FLD_COPY_FOR_PATIENT = "CopyForPatient";
	public static final String FLD_RES = "res";//$NON-NLS-2$
	public static final String FLD_XGESETZ = "xGesetz";//$NON-NLS-2$
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	//@formatter:off
	static {
		addMapping(TABLENAME, PATIENT_ID, 
			FLD_RES 			+"=Diagnosen", //$NON-NLS-1$
			FLD_DATUM_VON		+"=S:D:DatumVon", //$NON-NLS-2$
			FLD_DATUM_BIS 		+"=S:D:DatumBis", //$NON-NLS-2$
			FLD_GARANT_ID,  	
			FLD_BEHANDLUNGEN	+"=LIST:FallID:BEHANDLUNGEN:Datum", //$NON-NLS-2$
			FLD_BEZEICHNUNG, 	
			FLD_GRUND, 			
			FLD_XGESETZ			+"=Gesetz", 	//$NON-NLS-2$
			FLD_KOSTENTRAEGER	+"=KostentrID",  //$NON-NLS-2$
			FLD_VERS_NUMMER, 
			FLD_FALL_NUMMER, 
			FLD_RN_PLANUNG 		+"=BetriebsNummer", //$NON-NLS-1$
			FLD_EXTINFO);
	}
	//@formatter:on
	
	/**
	 * Vorgeschlagenen Zeitpunkt für Rechnungsstellung holen (Eine Vorgabe die im fall gemacht wird)
	 * 
	 * @return
	 */
	public TimeTool getBillingDate(){
		String r = get(FLD_RN_PLANUNG);
		if (StringTool.isNothing(r)) {
			return null;
		}
		TimeTool ret = new TimeTool();
		if (ret.set(r)) {
			return ret;
		}
		return null;
	}
	
	/**
	 * Zeitpunkt für Rechnungsvorschlag setzen
	 * 
	 * @param dat
	 *            Ein Zeitpunkt oder null
	 */
	public void setBillingDate(TimeTool dat){
		set(FLD_RN_PLANUNG, dat == null ? null : dat.toString(TimeTool.DATE_GER));
	}
	
	@Override
	public boolean isValid(){
		
		if (!super.isValid()) {
			return false;
		}
		Patient p = Patient.load(get(PATIENT_ID));
		if ((p == null) || (!p.isValid())) {
			return false;
		}
		
		// Check whether all user-defined requirements for this billing system
		// are met
		String reqs = getRequirements(getAbrechnungsSystem());
		if (reqs != null) {
			for (String req : reqs.split(";")) { //$NON-NLS-1$
				String localReq = ""; //$NON-NLS-1$
				String[] r = req.split(":"); //$NON-NLS-1$
				if ((r[1].equalsIgnoreCase("X")) && (r.length > 2)) { //$NON-NLS-1$
					// *** support for additional field types (checkboxes with
					// multiple items are
					// special)
					String[] items = r[2].split("\t"); //$NON-NLS-1$
					if (items.length > 1) {
						for (int rIx = 0; rIx < items.length; rIx++) {
							localReq = getInfoString(r[0] + "_" + items[rIx]); //$NON-NLS-1$
							if (StringTool.isNothing(localReq)) {
								return false;
							}
						}
					}
				} else {
					localReq = getInfoString(r[0]);
					if (StringTool.isNothing(localReq)) {
						return false;
					}
				}
				if (r[1].equals("K")) { //$NON-NLS-1$
					Kontakt k = Kontakt.load(localReq);
					if (!k.isValid()) {
						return false;
					}
				}
			}
		}
		// check whether the outputter could output a bill
		IRnOutputter outputter = getOutputter();
		if (outputter == null) {
			return false;
		} else {
			if (!outputter.canBill(this)) {
				return false;
			}
		}
		return true;
	}
	
	protected Fall(){/* leer */
	}
	
	protected Fall(final String id){
		super(id);
	}
	
	/**
	 * Einen neuen Fall zu einem Patienten mit einer Bezeichnung erstellen (Garant muss später noch
	 * ergänzt werden; Datum wird von heute genommen
	 * 
	 * @param PatientID
	 * @param Bezeichnung
	 */
	Fall(final String PatientID, final String Bezeichnung, final String Grund,
		String Abrechnungsmethode){
		create(null);
		set(new String[] {
			PATIENT_ID, FLD_BEZEICHNUNG, FLD_GRUND, FLD_DATUM_VON
		}, PatientID, Bezeichnung, Grund, new TimeTool().toString(TimeTool.DATE_GER));
		if (Abrechnungsmethode == null) {
			Abrechnungsmethode = Fall.getDefaultCaseLaw();
		}
		setAbrechnungsSystem(Abrechnungsmethode);
	}
	
	/** Einen Fall anhand der ID aus der Datenbank laden */
	public static Fall load(final String id){
		Fall ret = new Fall(id);
		if (ret.exists()) {
			return ret;
		}
		return null;
	}
	
	/** Anfangsdatum lesen (in der Form dd.mm.yy) */
	public String getBeginnDatum(){
		return checkNull(get(FLD_DATUM_VON));
	}
	
	public String getBezeichnung(){
		return checkNull(get(FLD_BEZEICHNUNG));
	}
	
	public void setBezeichnung(final String t){
		set(FLD_BEZEICHNUNG, t);
	}
	
	/**
	 * Anfangsdatum setzen Zulässige Formate: dd.mm.yy, dd.mm.yyyy, yyyymmdd, yy-mm-dd
	 */
	public void setBeginnDatum(final String dat){
		set(FLD_DATUM_VON, dat);
	}
	
	/** Enddatum lesen oder null: Fall noch nicht abgeschlossen */
	public String getEndDatum(){
		return checkNull(get(FLD_DATUM_BIS));
	}
	
	/** Enddatum setzen. Setzt zugleich den Fall auf abgeschlossen */
	public void setEndDatum(final String dat){
		set(FLD_DATUM_BIS, dat);
	}
	
	/**
	 * Den Rechnungsempfänger liefern
	 * 
	 * @return
	 */
	public Kontakt getGarant(){
		Kontakt ret = Kontakt.load(get(FLD_GARANT_ID));
		if ((ret == null) || (!ret.isValid())) {
			ret = getPatient();
		}
		return ret;
	}
	
	public void setGarant(final Kontakt garant){
		set(FLD_GARANT_ID, garant.getId());
	}
	
	public Rechnungssteller getRechnungssteller(){
		Rechnungssteller ret = Rechnungssteller.load(getInfoString(FLD_RECHNUNGSSTELLER_ID));
		if (!ret.isValid()) {
			ret = null;
		}
		return ret;
	}
	
	public void setRechnungssteller(final Kontakt r){
		setInfoString(FLD_RECHNUNGSSTELLER_ID, r.getId());
	}
	
	public boolean getCopyForPatient(){
		return StringConstants.ONE.equals(getInfoString(FLD_COPY_FOR_PATIENT));
	}
	
	public void setCopyForPatient(boolean copy){
		setInfoString(FLD_COPY_FOR_PATIENT, copy ? "1" : "0");
		
	}
	
	/**
	 * Retrieve a required Kontakt from this Fall's Billing system's requirements
	 * 
	 * @param name
	 *            the requested Kontakt's name
	 * @return the Kontakt or Null if no such Kontakt was found
	 */
	public Kontakt getRequiredContact(final String name){
		String kid = getInfoString(name);
		if (kid.equals("")) { //$NON-NLS-1$
			return null;
		}
		return Kontakt.load(kid);
	}
	
	public void setRequiredContact(final String name, final Kontakt k){
		String r = getRequirements();
		if (!StringTool.isNothing(r)) {
			String[] req = r.split(";"); //$NON-NLS-1$
			int idx = StringTool.getIndex(req, name + ":K"); //$NON-NLS-1$
			if (idx != -1) {
				if (req[idx].endsWith(":K")) { //$NON-NLS-1$
					setInfoString(name, k.getId());
				}
			}
		}
	}
	
	/**
	 * Retrieve a required String Value from this billing system's definition. If no variable with
	 * that name is found, the billings system constants will be searched
	 * 
	 * @param name
	 * @return a string that might be empty but will never be null.
	 */
	public String getRequiredString(final String name){
		String kid = getInfoString(name);
		if (StringTool.isNothing(kid)) {
			kid = getBillingSystemConstant(getAbrechnungsSystem(), name);
		}
		return kid;
	}
	
	public void setRequiredString(final String name, final String val){
		String[] req = getRequirements().split(";"); //$NON-NLS-1$
		int idx = StringTool.getIndex(req, name + ":T"); //$NON-NLS-1$
		if (idx != -1) {
			setInfoString(name, val);
		}
	}
	
	/**
	 * This is an update only for swiss installations that takes the old tarmed cases to the new
	 * system
	 */
	private static void update(){
		// String is=getInfoString("Kostenträger");
		Query<Fall> qbe = new Query<Fall>(Fall.class);
		for (Fall fall : qbe.execute()) {
			if (fall.getInfoString("Kostenträger").equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
				fall.setInfoString("Kostenträger", checkNull(fall //$NON-NLS-1$
					.get(FLD_KOSTENTRAEGER)));
			}
			if (fall.getInfoString("Rechnungsempfänger").equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
				fall.setInfoString("Rechnungsempfänger", checkNull(fall //$NON-NLS-1$
					.get(FLD_GARANT_ID)));
			}
			if (fall.getInfoString("Versicherungsnummer").equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
				fall.setInfoString("Versicherungsnummer", checkNull(fall //$NON-NLS-1$
					.get(FLD_VERS_NUMMER)));
			}
			if (fall.getInfoString("Fallnummer").equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
				fall.setInfoString("Fallnummer", checkNull(fall //$NON-NLS-1$
					.get(FLD_FALL_NUMMER)));
			}
			if (fall.getInfoString("Unfallnummer").equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
				fall.setInfoString("Unfallnummer", checkNull(fall //$NON-NLS-1$
					.get(FLD_FALL_NUMMER)));
			}
		}
	}
	
	@Deprecated
	public Kontakt getArbeitgeber(){
		String id = getInfoString("Arbeitgeber"); //$NON-NLS-1$
		Kontakt ret = null;
		if (StringTool.isNothing(id) || ((ret = Kontakt.load(id)).exists() == false)) {
			return null;
		}
		return ret;
	}
	
	@Deprecated
	public String getArbeitgeberName(){
		return getArbeitgeber().getLabel();
	}
	
	/**
	 * Versichertennummer holen
	 * 
	 * @deprecated user getRequiredString instead
	 */
	@Deprecated
	public String getVersNummer(){
		
		return checkNull(getInfoString("Versicherungsnummer")); //$NON-NLS-1$
	}
	
	/**
	 * Versichertennummer setzen public void setVersNummer(final String nr){ set("VersNummer",nr); }
	 */
	/** Fallnummer lesen */
	public String getFallNummer(){
		return checkNull(get(FLD_FALL_NUMMER));
	}
	
	/** Fallnummer setzen */
	public void setFallNummer(final String nr){
		set(FLD_FALL_NUMMER, nr);
	}
	
	/** Feststellen, ob der Fall noch offen ist */
	public boolean isOpen(){
		if (getEndDatum().equals("")) { //$NON-NLS-1$
			return true;
		}
		return false;
	}
	
	public void setAbrechnungsSystem(final String system){
		setInfoString(FLD_BILLING, system);
	}
	
	public String getAbrechnungsSystem(){
		String ret = getInfoString(FLD_BILLING);
		if (StringTool.isNothing(ret)) {
			String[] systeme = getAbrechnungsSysteme();
			String altGesetz = get("xGesetz"); //$NON-NLS-1$
			int idx = StringTool.getIndex(systeme, altGesetz);
			if (idx == -1) {
				ret = systeme[0];
			} else {
				ret = systeme[idx];
			}
			setAbrechnungsSystem(ret);
		}
		return ret;
	}
	
	public String getCodeSystemName(){
		return getCodeSystem(getAbrechnungsSystem());
	}
	
	/**
	 * Retrieve requirements of this Cases billing system
	 * 
	 * @return a ; separated String of fields name:type where type is one of K,T,D for Kontakt,
	 *         Text, Date TM Text Multiline TS Text Styled CS Combo saved as string CN Combo saved
	 *         as numeric (selected index) LS List items, saved as strings, tab-delimited LN List
	 *         items, saved as numerics, tab-delimited (selected indexes) X CheckBox always saved as
	 *         numeric RS Radios, saved as string RN Radios, saved as numeric, selected index
	 */
	
	public String getRequirements(){
		String req = getRequirements(getAbrechnungsSystem());
		return req == null ? "" : req; //$NON-NLS-1$
	}
	
	/**
	 * Retrieve optionals of this Cases billing system
	 * 
	 * @return a ; separated String of fields name:type where type is one of K,T,D for Kontakt,
	 *         Text, Date TM Text Multiline TS Text Styled CS Combo saved as string CN Combo saved
	 *         as numeric (selected index) LS List items, saved as strings, tab-delimited LN List
	 *         items, saved as numerics, tab-delimited (selected indexes) X CheckBox always saved as
	 *         numeric RS Radios, saved as string RN Radios, saved as numeric, selected index
	 */
	
	public String getOptionals(){
		String req = getOptionals(getAbrechnungsSystem());
		return req == null ? "" : req; //$NON-NLS-1$
	}
	
	/**
	 * Retrieve unused/saved definitions of previously used required and optional field of this
	 * Cases billing system
	 * 
	 * @return a ; separated String of fields name:type where type is one of K,T,D for Kontakt,
	 *         Text, Date TM Text Multiline TS Text Styled CS Combo saved as string CN Combo saved
	 *         as numeric (selected index) LS List items, saved as strings, tab-delimited LN List
	 *         items, saved as numerics, tab-delimited (selected indexes) X CheckBox always saved as
	 *         numeric RS Radios, saved as string RN Radios, saved as numeric, selected index
	 */
	
	public String getUnused(){
		String req = getUnused(getAbrechnungsSystem());
		return req == null ? "" : req; //$NON-NLS-1$
	}
	
	/**
	 * Retrieve the name of the outputter of this case's billing system
	 * 
	 * @return
	 */
	public String getOutputterName(){
		return getDefaultPrintSystem(getAbrechnungsSystem());
	}
	
	/**
	 * Retrieve the ooutputter for this case's billing system
	 * 
	 * @return the IRnOutputter that will be used or null if none was found
	 */
	public IRnOutputter getOutputter(){
		String outputterName = getOutputterName();
		if (outputterName.length() > 0) {
			List<IConfigurationElement> list =
				Extensions.getExtensions(ExtensionPointConstantsData.RECHNUNGS_MANAGER); //$NON-NLS-1$
			for (IConfigurationElement ic : list) {
				if (ic.getAttribute("name").equals(outputterName)) { //$NON-NLS-1$
					try {
						IRnOutputter ret = (IRnOutputter) ic.createExecutableExtension("outputter"); //$NON-NLS-1$
						return ret;
					} catch (CoreException e) {
						ExHandler.handle(e);
					}
				}
			}
		}
		return null;
	}
	
	/** Behandlungen zu diesem Fall holen */
	public Konsultation[] getBehandlungen(final boolean sortReverse){
		List<String> list = getList(FLD_BEHANDLUNGEN, sortReverse);
		int i = 0;
		Konsultation[] ret = new Konsultation[list.size()];
		for (String id : list) {
			ret[i++] = Konsultation.load(id);
		}
		// Arrays.sort(ret,new Konsultation.BehandlungsComparator(sortReverse));
		return ret;
	}
	
	public Konsultation getLetzteBehandlung(){
		List<String> list = getList(FLD_BEHANDLUNGEN, true);
		if (list.size() > 0) {
			return Konsultation.load(list.get(0));
		}
		return null;
	}
	
	/** Neue Konsultation zu diesem Fall anlegen */
	public Konsultation neueKonsultation(){
		if (isOpen() == false) {
			MessageEvent.fireError(Messages.Fall_CaseClosedCaption, Messages.Fall_CaseClosedText);
			return null;
		}
		if ((CoreHub.actMandant == null) || (!CoreHub.actMandant.exists())) {
			MessageEvent.fireError(Messages.Fall_NoMandatorCaption, Messages.Fall_NoMandatorText);
			return null;
		}
		return new Konsultation(this);
	}
	
	public Patient getPatient(){
		return Patient.load(get(PATIENT_ID));
	}
	
	public String getGrund(){
		return checkNull(get(FLD_GRUND));
	}
	
	public void setGrund(final String g){
		set(FLD_GRUND, g);
	}
	
	@Override
	public String getLabel(){
		String[] f = new String[] {
			FLD_GRUND, FLD_BEZEICHNUNG, FLD_DATUM_VON, FLD_DATUM_BIS
		};
		String[] v = new String[f.length];
		get(f, v);
		StringBuilder ret = new StringBuilder();
		if (!isOpen()) {
			ret.append(Messages.Fall_CLOSED);
		}
		String ges = getAbrechnungsSystem();
		ret.append(ges).append(": ").append(v[0]).append(" - "); //$NON-NLS-1$ //$NON-NLS-2$
		ret.append(v[1]).append("("); //$NON-NLS-1$
		String ed = v[3];
		if ((ed == null) || StringTool.isNothing(ed.trim())) {
			ed = Messages.Fall_Open;
		}
		ret.append(v[2]).append("-").append(ed).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
		return ret.toString();
	}
	
	@Override
	public boolean delete(){
		return delete(false);
	}
	
	/**
	 * Mark this Fall as deleted. This will fail if there exist Konsultationen fpr this Fall, unless
	 * force is set
	 * 
	 * @param force
	 *            delete even if KOnsultationene xist (in that case, all Konsultationen will be
	 *            deleted as well)
	 * @return true if this Fall could be (and has been) deleted.
	 */
	public boolean delete(final boolean force){
		Konsultation[] bh = getBehandlungen(false);
		if ((bh.length == 0)
			|| ((force == true) && (CoreHub.acl.request(AccessControlDefaults.DELETE_FORCED) == true))) {
			for (Konsultation b : bh) {
				b.delete(true);
			}
			delete_dependent();
			return super.delete();
		}
		return false;
	}
	
	private boolean delete_dependent(){
		Query<AUF> qAUF = new Query<AUF>(AUF.class);
		qAUF.add(AUF.FLD_CASE_ID, Query.EQUALS, getId());
		for (AUF auf : qAUF.execute()) {
			auf.delete();
		}
		Query<Rechnung> qRn = new Query<Rechnung>(Rechnung.class);
		qRn.add(AUF.FLD_CASE_ID, Query.EQUALS, getId());
		for (Rechnung rn : qRn.execute()) {
			rn.delete();
		}
		
		return true;
	}
	
	/**
	 * retrieve a string from ExtInfo.
	 * 
	 * @param name
	 *            the requested parameter
	 * @return the value of that parameter (which might be empty but will never be null)
	 */
	@SuppressWarnings("unchecked")
	public String getInfoString(final String name){
		Map extinfo = getMap(FLD_EXTINFO);
		if (name == null || extinfo.get(name) == null)
			return StringConstants.EMPTY;
		if (extinfo.get(name) instanceof String)
			return checkNull((String) extinfo.get(name));
		log.warn("Invalid object in Fall.getInfoString(" + name + "), not castable to String: "
			+ extinfo.get(name));
		return "";
	}
	
	@SuppressWarnings("unchecked")
	public void setInfoString(final String name, final String wert){
		Map extinfo = getMap(FLD_EXTINFO);
		extinfo.put(name, wert);
		setMap(FLD_EXTINFO, extinfo);
	}
	
	@SuppressWarnings("unchecked")
	public void clearInfoString(final String string){
		Map extinfo = getMap(FLD_EXTINFO);
		extinfo.remove(string);
		setMap(FLD_EXTINFO, extinfo);
		
	}
	
	@SuppressWarnings("unchecked")
	public Object getInfoElement(final String name){
		Map extinfo = getMap(FLD_EXTINFO);
		return extinfo.get(name);
	}
	
	@SuppressWarnings("unchecked")
	public void setInfoElement(final String name, final Object elem){
		Map extinfo = getMap(FLD_EXTINFO);
		extinfo.put(name, elem);
		setMap(FLD_EXTINFO, extinfo);
	}
	
	@Override
	public boolean isDragOK(){
		return true;
	}
	
	public static String getDefaultCaseLabel(){
		return CoreHub.userCfg.get(Preferences.USR_DEFCASELABEL,
			Preferences.USR_DEFCASELABEL_DEFAULT);
	}
	
	public static String getDefaultCaseReason(){
		return CoreHub.userCfg.get(Preferences.USR_DEFCASEREASON,
			Preferences.USR_DEFCASEREASON_DEFAULT);
	}
	
	public static String getDefaultCaseLaw(){
		return CoreHub.userCfg.get(Preferences.USR_DEFLAW, getAbrechnungsSysteme()[0]);
	}
	
	/**
	 * Find all installed billing systems. If we do not find any, we assume that this is an old
	 * installation and try to update. If we find a tarmed-Plugin installed, we create
	 * default-tarmed billings.
	 * 
	 * @return an Array with the names of all configured billing systems
	 */
	public static String[] getAbrechnungsSysteme(){
		String[] ret = CoreHub.globalCfg.nodes(Preferences.LEISTUNGSCODES_CFG_KEY);
		if ((ret == null) || (ret.length == 0)) {
			List<IConfigurationElement> list =
				Extensions.getExtensions(ExtensionPointConstantsData.RECHNUNGS_MANAGER); //$NON-NLS-1$
			for (IConfigurationElement ic : list) {
				if (ic.getAttribute("name").startsWith("Tarmed")) { //$NON-NLS-1$ //$NON-NLS-2$
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/KVG/name", //$NON-NLS-1$
						KVG_NAME);
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/KVG/gesetz", //$NON-NLS-1$
						"KVG"); //$NON-NLS-1$
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY
						+ "/KVG/leistungscodes", CONST_TARMED_LEISTUNG); //$NON-NLS-1$
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY
						+ "/KVG/standardausgabe", CONST_TARMED_DRUCKER); //$NON-NLS-1$
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/KVG/bedingungen", //$NON-NLS-1$
						KVG_REQUIREMENTS);
					
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/UVG/name", //$NON-NLS-1$
						UVG_NAME);
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY
						+ "/UVG/leistungscodes", CONST_TARMED_LEISTUNG); //$NON-NLS-1$
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY
						+ "/UVG/standardausgabe", CONST_TARMED_DRUCKER); //$NON-NLS-1$
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/UVG/bedingungen", //$NON-NLS-1$
						UVG_REQUIREMENTS);
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/UVG/gesetz", //$NON-NLS-1$
						"UVG"); //$NON-NLS-1$
					
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/IV/name", IV_NAME); //$NON-NLS-1$
					CoreHub.globalCfg
						.set(
							Preferences.LEISTUNGSCODES_CFG_KEY + "/IV/leistungscodes", CONST_TARMED_LEISTUNG); //$NON-NLS-1$
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY
						+ "/IV/standardausgabe", CONST_TARMED_DRUCKER); //$NON-NLS-1$
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/IV/bedingungen", //$NON-NLS-1$
						"Kostenträger:K;Fallnummer:T"); //$NON-NLS-1$
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/IV/gesetz", //$NON-NLS-1$
						"IVG"); //$NON-NLS-1$
					
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/MV/name", MV_NAME); //$NON-NLS-1$
					CoreHub.globalCfg
						.set(
							Preferences.LEISTUNGSCODES_CFG_KEY + "/MV/leistungscodes", CONST_TARMED_LEISTUNG); //$NON-NLS-1$
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY
						+ "/MV/standardausgabe", CONST_TARMED_DRUCKER); //$NON-NLS-1$
					CoreHub.globalCfg.set(
						Preferences.LEISTUNGSCODES_CFG_KEY + "/MV/bedingungen", "Kostenträger:K"); //$NON-NLS-1$ //$NON-NLS-2$
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/MV/gesetz", //$NON-NLS-1$
						"MVG"); //$NON-NLS-1$
					
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/privat/name", //$NON-NLS-1$
						PRIVATE_NAME);
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY
						+ "/privat/leistungscodes", CONST_TARMED_LEISTUNG); //$NON-NLS-1$
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY
						+ "/privat/standardausgabe", CONST_TARMED_DRUCKER); //$NON-NLS-1$
					CoreHub.globalCfg.set(
						Preferences.LEISTUNGSCODES_CFG_KEY + "/privat/gesetz", "VVG"); //$NON-NLS-1$ //$NON-NLS-2$
					// CoreHub.globalCfg.set(PreferenceConstants.LEISTUNGSCODES_CFG_KEY+"/privat/bedingungen",
					// "Rechnungsempfänger:K");
					
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/VVG/name", //$NON-NLS-1$
						VVG_NAME);
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY
						+ "/VVG/leistungscodes", CONST_TARMED_LEISTUNG); //$NON-NLS-1$
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY
						+ "/VVG/standardausgabe", CONST_TARMED_DRUCKER); //$NON-NLS-1$
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/VVG/bedingungen", //$NON-NLS-1$
						KVG_REQUIREMENTS);
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/VVG/gesetz", //$NON-NLS-1$
						"VVG"); //$NON-NLS-1$
					
					PersistentObject
						.getConnection()
						.exec(
							"UPDATE VK_PREISE set typ='UVG' WHERE typ='ch.elexis.data.TarmedLeistungUVG'"); //$NON-NLS-1$
					PersistentObject
						.getConnection()
						.exec(
							"UPDATE VK_PREISE set typ='KVG' WHERE typ='ch.elexis.data.TarmedLeistungKVG'"); //$NON-NLS-1$
					PersistentObject
						.getConnection()
						.exec(
							"UPDATE VK_PREISE set typ='IV' WHERE typ='ch.elexis.data.TarmedLeistungIV'"); //$NON-NLS-1$
					PersistentObject
						.getConnection()
						.exec(
							"UPDATE VK_PREISE set typ='MV' WHERE typ='ch.elexis.data.TarmedLeistungMV'"); //$NON-NLS-1$
					update();
					break;
				}
			}
			ret = CoreHub.globalCfg.nodes(Preferences.LEISTUNGSCODES_CFG_KEY);
			if (ret == null) {
				return new String[] {
					Messages.Fall_Undefined
				};
			}
		}
		return ret;
	}
	
	public static void createAbrechnungssystem(final String systemname, final String codesystem,
		final String ausgabe, final String... requirements){
		String key = Preferences.LEISTUNGSCODES_CFG_KEY + "/" + systemname; //$NON-NLS-1$
		CoreHub.globalCfg.set(key + "/name", systemname); //$NON-NLS-1$
		CoreHub.globalCfg.set(key + "/leistungscodes", codesystem); //$NON-NLS-1$
		CoreHub.globalCfg.set(key + "/standardausgabe", ausgabe); //$NON-NLS-1$
		CoreHub.globalCfg.set(key + "/bedingungen", StringTool.join(requirements, //$NON-NLS-1$
			";")); //$NON-NLS-1$
	}
	
	public static void removeAbrechnungssystem(final String systemName){
		CoreHub.globalCfg.remove(Preferences.LEISTUNGSCODES_CFG_KEY + "/" + systemName); //$NON-NLS-1$
		CoreHub.globalCfg.flush();
	}
	
	public static String getCodeSystem(final String billingSystem){
		String ret = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ billingSystem + "/leistungscodes", null); //$NON-NLS-1$
		if (ret == null) { // compatibility
			getAbrechnungsSysteme();
			ret = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
				+ billingSystem + "/leistungscodes", "?"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return ret;
	}
	
	public static String getDefaultPrintSystem(final String billingSystem){
		String ret = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ billingSystem + "/standardausgabe", null); //$NON-NLS-1$
		if (ret == null) { // compatibility
			getAbrechnungsSysteme();
			ret = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
				+ billingSystem + "/standardausgabe", "?"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return ret;
	}
	
	public static String[] getBillingSystemConstants(final String billingSystem){
		String bc = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ billingSystem + "/constants", null); //$NON-NLS-1$
		if (bc == null) {
			return new String[0];
		} else {
			return bc.split("#"); //$NON-NLS-1$
		}
	}
	
	public static String getBillingSystemConstant(final String billingSystem, final String constant){
		String[] c = getBillingSystemConstants(billingSystem);
		for (String bc : c) {
			String[] val = bc.split("="); //$NON-NLS-1$
			if (val[0].equalsIgnoreCase(constant)) {
				return val[1];
			}
		}
		return ""; //$NON-NLS-1$
	}
	
	/**
	 * add a billing system constant
	 * 
	 * @param billingSystem
	 *            the Billing system
	 * @param constant
	 *            a String of the form name=value
	 * 
	 */
	public static void addBillingSystemConstant(final String billingSystem, final String constant){
		if (constant.indexOf('=') != -1) {
			String bc = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
				+ billingSystem + "/constants", null); //$NON-NLS-1$
			if (bc != null) {
				bc += "#" + constant; //$NON-NLS-1$
			} else {
				bc = constant;
			}
			CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/" + billingSystem //$NON-NLS-1$
				+ "/constants", bc); //$NON-NLS-1$
		}
	}
	
	public static void removeBillingSystemConstant(final String billingSystem, final String constant){
		String bc = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ billingSystem + "/constants", null); //$NON-NLS-1$
		bc = bc.replaceAll(constant, ""); //$NON-NLS-1$
		bc = bc.replaceAll("##", "#"); //$NON-NLS-1$ //$NON-NLS-2$
		bc = bc.replaceFirst("#$", ""); //$NON-NLS-1$ //$NON-NLS-2$
		bc = bc.replaceFirst("^#", ""); //$NON-NLS-1$ //$NON-NLS-2$
		CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/" + billingSystem //$NON-NLS-1$
			+ "/constants", bc); //$NON-NLS-1$
	}
	
	/**
	 * 
	 * @param billingSystem
	 * @param attr
	 * @return
	 * @deprecated use getBillingSystemConstant
	 */
	@Deprecated
	public static String getBillingSystemAttribute(final String billingSystem, final String attr){
		String ret = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ billingSystem + "/" + attr, ""); //$NON-NLS-1$ //$NON-NLS-2$
		return ret;
	}
	
	/**
	 * Retrieve requirements of a given billingSystem
	 * 
	 * @param billingSystem
	 * @return a ; separated String of fields name:type where type is one of K,T,D for Kontakt,
	 *         Text, Date TM Text Multiline TS Text Styled CS Combo saved as string CN Combo saved
	 *         as numeric (selected index) LS List items, saved as strings, tab-delimited LN List
	 *         items, saved as numerics, tab-delimited (selected indexes) X CheckBox always saved as
	 *         numeric RS Radios, saved as string RN Radios, saved as numeric, selected index
	 */
	public static String getRequirements(final String billingSystem){
		String ret = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ billingSystem + "/bedingungen", null); //$NON-NLS-1$
		return ret;
	}
	
	/**
	 * Retrieve optionals of a given billingSystem
	 * 
	 * @param billingSystem
	 * @return a ; separated String of fields name:type where type is one of K,T,D for Kontakt,
	 *         Text, Date TM Text Multiline TS Text Styled CS Combo saved as string CN Combo saved
	 *         as numeric (selected index) LS List items, saved as strings, tab-delimited LN List
	 *         items, saved as numerics, tab-delimited (selected indexes) X CheckBox always saved as
	 *         numeric RS Radios, saved as string RN Radios, saved as numeric, selected index
	 */
	public static String getOptionals(final String billingSystem){
		String ret = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ billingSystem + "/fakultativ", null); //$NON-NLS-1$
		return ret;
	}
	
	/**
	 * Retrieve unused/saved definitions of previously used required and optional field for a given
	 * billingSystem
	 * 
	 * @param billingSystem
	 * @return a ; separated String of fields name:type where type is one of K,T,D for Kontakt,
	 *         Text, Date TM Text Multiline TS Text Styled CS Combo saved as string CN Combo saved
	 *         as numeric (selected index) LS List items, saved as strings, tab-delimited LN List
	 *         items, saved as numerics, tab-delimited (selected indexes) X CheckBox always saved as
	 *         numeric RS Radios, saved as string RN Radios, saved as numeric, selected index
	 */
	public static String getUnused(final String billingSystem){
		String ret = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ billingSystem + "/unused", null); //$NON-NLS-1$
		return ret;
	}
	
	/**
	 * Return the referenced field as a PersistentObject. For fields not representing
	 * PersistentObjects, this method returns null.
	 * 
	 * This method is mainly used to replace indirect fields in text templates (e. g.
	 * [Fall.Kostenträger.Bezeichnung1])
	 * 
	 * Actually, this method should be defined by the class PersistentObject and implemented by all
	 * subclasses. A subclass should de-reference all its field it defines. If the sublcass extends
	 * another sublcass, it should also call the superclass' method. All of this is not yet
	 * implemented.
	 * 
	 * TODO: implement further fields of Fall, e. g. PatientID and GarantID
	 * 
	 * @param field
	 *            the field to resolve. This must represent a Persistent Object, else null is
	 *            returned.
	 * @return the referenced object, or null if it could not be found
	 */
	public PersistentObject getReferencedObject(String field){
		// first consider the billing system requirements
		Kontakt kontakt = getRequiredContact(field);
		if (kontakt != null) {
			if (kontakt.exists()) {
				if (kontakt.istPerson()) {
					kontakt = Person.load(kontakt.getId());
				} else if (kontakt.istOrganisation()) {
					kontakt = Organisation.load(kontakt.getId());
				}
				return kontakt;
			} else {
				return null;
			}
		}
		
		// then try our own fields
		// TODO
		
		return null;
	}
}
