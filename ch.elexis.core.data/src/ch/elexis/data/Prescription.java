/*******************************************************************************
 * Copyright (c) 2005-2015, G. Weirich, Elexis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 	  M. Descher - contributions
 *******************************************************************************/
package ch.elexis.data;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.model.PrescriptionMethods;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * A prescription. An article with the required signature and Patient.
 * 
 * @since 3.1.0 support for prescription type, manual sort order, ...
 */
public class Prescription extends PersistentObject {
	
	public static final String FLD_DATE_UNTIL = "DateUntil"; // date medication was stopped
	public static final String FLD_DATE_FROM = "DateFrom";
	public static final String FLD_COUNT = "Anzahl";
	public static final String FLD_REMARK = "Bemerkung";
	public static final String FLD_DOSAGE = "Dosis";
	/**
	 * <ul>
	 * <li><code>null</code> entry for fixed medication
	 * <li><code>Direktabgabe</code> was dispensed in the course of a consultation
	 * <li><code>Rezept-ID</code> dispensed in the form of a recipe, where the resp. ID is shown
	 * </ul>
	 */
	public static final String FLD_REZEPT_ID = "RezeptID";
	public static final String FLD_ARTICLE_ID = "ArtikelID";
	public static final String FLD_ARTICLE = "Artikel";
	public static final String FLD_PATIENT_ID = "PatientID";
	public static final String FLD_EXT_TERMS = "terms";
	
	// since 3.1.0
	public static final String FLD_DATE_PRESC = "prescDate"; // prescription date
	public static final String FLD_SORT_ORDER = "sortOrder"; // manual sort order in the UI table
	public static final String FLD_PRESC_TYPE = "prescType"; // prescription type, see flags
	public static final String FLD_PRESCRIPTOR = "prescriptor"; // contact that prescribed this
	public static final String FLD_EXT_STOP_REASON = "stopReason"; // reason for stopping medication
	public static final String FLD_EXT_STOPPED_BY = "stopper"; // who stopped the prescription
	public static final String FLD_EXT_INTOLERANCE = "intolerance";
	public static final String FLD_EXT_DISPOSAL_COMMENT = "disposalComment";
	public static final String FLD_EXT_DATE_LAST_DISPOSAL = "lastDisposal";
	public static final String FLD_EXT_DISPOSED_BY = "disposedBy";
	
	/**
	 * Prescription is a direct distribution within a consultation
	 */
	public static final String FLD_REZEPTID_VAL_DIREKTABGABE = "Direktabgabe";
	/**
	 * ID of the {@link Verrechnet} if {@link #FLD_REZEPTID_VAL_DIREKTABGABE}
	 */
	public static final String FLD_EXT_VERRECHNET_ID = "verrechnetId";
	
	public static final String TABLENAME = "PATIENT_ARTIKEL_JOINT";
	
	static {
		addMapping(TABLENAME, FLD_PATIENT_ID, FLD_ARTICLE, FLD_ARTICLE_ID, FLD_REZEPT_ID,
			FLD_DATE_FROM, FLD_DATE_UNTIL, FLD_DATE_PRESC + "=S:D:" + FLD_DATE_PRESC, FLD_DOSAGE,
			FLD_REMARK, FLD_COUNT, FLD_PRESC_TYPE, FLD_SORT_ORDER, FLD_EXTINFO, FLD_PRESCRIPTOR);
	}
	
	public Prescription(Artikel a, Patient p, String dosage, String remark){
		create(null);
		String article = a.storeToString();
		String dateNow = new TimeTool().toString(TimeTool.DATE_GER);
		IPersistentObject prescriptor = ElexisEventDispatcher.getSelected(Anwender.class);
		
		set(new String[] {
			FLD_ARTICLE, FLD_PATIENT_ID, FLD_DOSAGE, FLD_REMARK, FLD_DATE_PRESC,
			FLD_SORT_ORDER, FLD_PRESCRIPTOR
		}, article, p.getId(), dosage, remark, dateNow, "999", prescriptor.getId());
		setBeginDate(null);
	}
	
	/**
	 * Creates a copy of the other Prescription
	 * 
	 * @param other
	 */
	public Prescription(Prescription other){
		String[] fields = new String[] {
			FLD_ARTICLE, FLD_PATIENT_ID, FLD_DOSAGE, FLD_REMARK, FLD_ARTICLE_ID
		};
		
		String[] vals = new String[fields.length];
		if (other.get(fields, vals)) {
			create(null);
			set(fields, vals);
			setBeginDate(null);
			IPersistentObject prescriptor = ElexisEventDispatcher.getSelected(Anwender.class);
			set(FLD_PRESCRIPTOR, prescriptor.getId());
			String comment = other.getDisposalComment();
			if (comment != null && !comment.isEmpty()) {
				setDisposalComment(comment);
			}
		}
	}
	
	public static Prescription load(String id){
		return new Prescription(id);
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	@Override
	public boolean isDragOK(){
		return true;
	}
	
	protected Prescription(){}
	
	protected Prescription(String id){
		super(id);
	}
	
	/**
	 * Set the begin date of this prescription
	 * 
	 * @param date
	 *            may be null to set it as today
	 */
	public void setBeginDate(String date){
		TimeTool newDate = new TimeTool();
		if (date != null && !date.isEmpty()) {
			newDate.set(date);
		}
		set(FLD_DATE_FROM, newDate.toString(TimeTool.TIMESTAMP));
	}
	
	public String getBeginDate(){
		String timestamp = checkNull(get(FLD_DATE_FROM));
		if (!timestamp.isEmpty()) {
			TimeTool timetool = new TimeTool(timestamp);
			return timetool.toString(TimeTool.DATE_GER);
		}
		return "";
	}
	
	public String getBeginTime(){
		String timestamp = checkNull(get(FLD_DATE_FROM));
		if (!timestamp.isEmpty()) {
			TimeTool timetool = new TimeTool(timestamp);
			return timetool.toString(TimeTool.FULL_GER);
		}
		return "";
	}
	
	public void setEndDate(String date){
		TimeTool newDate = new TimeTool();
		if (date != null && !date.isEmpty()) {
			newDate.set(date);
		}
		set(FLD_DATE_UNTIL, newDate.toString(TimeTool.TIMESTAMP));
	}
	
	public String getEndDate(){
		String timestamp = checkNull(get(FLD_DATE_UNTIL));
		if (!timestamp.isEmpty()) {
			TimeTool timetool = new TimeTool(timestamp);
			return timetool.toString(TimeTool.DATE_GER);
		}
		return "";
	}
	
	public String getEndTime(){
		String timestamp = checkNull(get(FLD_DATE_UNTIL));
		if (!timestamp.isEmpty()) {
			TimeTool timetool = new TimeTool(timestamp);
			return timetool.toString(TimeTool.FULL_GER);
		}
		return "";
	}
	
	@Override
	public String getLabel(){
		return getSimpleLabel() + " " + getDosis();
	}
	
	public String getSimpleLabel(){
		Artikel art = getArtikel();
		if (art != null) {
			return getArtikel().getLabel();
		} else {
			return "Fehler";
		}
	}
	
	/**
	 * return the article contained in this prescription. In earlier versions of elexis, this was
	 * the Article ID, now it is a String representation of the Article itself (which allows for
	 * reconstruction of the subclass used). For compatibility reasons we use the old technique for
	 * old prescriptions.
	 * 
	 * @return
	 */
	public Artikel getArtikel(){
		// compatibility layer
		String art = get(FLD_ARTICLE);
		if (StringTool.isNothing(art)) {
			return Artikel.load(get(FLD_ARTICLE_ID));
		}
		return (Artikel) CoreHub.poFactory.createFromString(art);
	}
	
	/**
	 * return the dose of a drugs. Up to Version 3.1 (hopefully) Elexis did not specify exactly the
	 * dose of a drug, but used a String where many doctors used shortcuts like 1-0-1-1 to specify
	 * that the number of entities (e.g. a tablet) for breakfast, lunch, supper, before night Other
	 * examples are 0-0- 1/4-1/2 0.5/-/- 0-0-0- 40E 0.5 Stk alle 3 Tage
	 * 
	 * @return String
	 */
	public String getDosis(){
		return checkNull(get(FLD_DOSAGE));
	}
	
	/**
	 * Return the dose of a drugs as a list of up to 4 floats.<br>
	 * Up to Version 3.0.10 (hopefully) Elexis did not specify exactly the dose of a drug, but used
	 * a String where many doctors used shortcuts like 1-0-1-1 to specify that the number of
	 * entities (e.g. a tablet) for breakfast, lunch, supper, before night Here are some examples
	 * how this procedure deals with the some input. 0-0- 1/4-1/2 => <0.0,0.0,0.25> 0.5/-/- =>
	 * <0.5> 0-0-0- 40E => <0.0,0.0,0.0,40.0> 0.5 Stk alle 3 Tage => <0.5> 1inj Wo => <>
	 * 
	 * More examples can be found in the unit test.
	 * 
	 * @return a list of (up to 4) floats
	 */
	public static ArrayList<Float> getDoseAsFloats(String dosis){
		ArrayList<Float> list = new ArrayList<Float>();
		float num = 0;
		if (dosis != null) {
			// Match stuff like '1/2', '7/8'
			if (dosis.matches("^[0-9]/[0-9]$")) {
				list.add(getNum(dosis));
				
			} else if (dosis.matches("[0-9½¼]+([xX][0-9]+(/[0-9]+)?|)")) { //$NON-NLS-1$
				String[] dose = dosis.split("[xX]"); //$NON-NLS-1$
				float count = getNum(dose[0]);
				if (dose.length > 1)
					num = getNum(dose[1]) * count;
				else
					num = getNum(dose[0]);
				list.add(num);
			} else if (dosis.indexOf('-') != -1) {
				String[] dos = dosis.split("[- ]"); //$NON-NLS-1$
				if (dos.length > 2) {
					for (String d : dos) {
						boolean hasDigit = d.matches("^[~/.]*[½¼0-9].*");
						if (d.indexOf(' ') != -1)
							list.add(getNum(d.substring(0, d.indexOf(' '))));
						else if (d.length() > 0 && hasDigit)
							list.add(getNum(d));
						if (list.size() >= 4)
							return list;
					}
				} else if (dos.length > 1) {
					list.add(getNum(dos[1]));
				} else {
					// nothing to add
				}
			}
		}
		return list;
	}
	
	/**
	 * 
	 * @return the signature split into a string array with 4 elements; will always return an array
	 *         of 4 elements, where empty entries are of type String ""
	 * @since 3.1.0
	 * @since 3.2.0 relocated to {@link PrescriptionMethods#getSignatureAsStringArray(String)}
	 */
	public static String[] getSignatureAsStringArray(String signature){
		return PrescriptionMethods.getSignatureAsStringArray(signature);
	}
	
	public void setDosis(String newDose){
		String oldDose = getDosis();
		if (!oldDose.equals(newDose)) {
			set(FLD_DOSAGE, newDose);
		}
	}
	
	/**
	 * the prescription order for the intake (Einnahmevorschrift)
	 * 
	 * @return
	 */
	public String getBemerkung(){
		return checkNull(get(FLD_REMARK));
		
	}
	
	public void setBemerkung(String value){
		set(FLD_REMARK, checkNull(value));
	}
	
	/**
	 * @return a disposal comment (Abgabekommentar)
	 * @since 3.1.0
	 */
	public String getDisposalComment(){
		return checkNull((String) getExtInfoStoredObjectByKey(FLD_EXT_DISPOSAL_COMMENT));
	}
	
	/**
	 * set a disposal comment (Abgabekommentar)
	 * 
	 * @param disposalComment
	 * @since 3.1.0
	 */
	public void setDisposalComment(String disposalComment){
		setExtInfoStoredObjectByKey(FLD_EXT_DISPOSAL_COMMENT, disposalComment);
	}
	
	/**
	 * Ein Medikament stoppen
	 */
	@Override
	public boolean delete(){
		if (CoreHub.acl.request(AccessControlDefaults.MEDICATION_MODIFY)) {
			stop(null);
			return true;
		}
		return false;
	}
	
	/**
	 * delete mediation
	 * 
	 * @return
	 */
	public boolean remove(){
		if (CoreHub.acl.request(AccessControlDefaults.DELETE_MEDICATION)) {
			return super.delete();
		}
		return false;
	}
	
	/**
	 * Stops this prescription
	 * 
	 * @param until
	 *            if <code>null</code> effective now, else with the given time
	 * @since 3.1.0
	 */
	public void stop(TimeTool until){
		if (until == null)
			until = new TimeTool();
		set(FLD_DATE_UNTIL, until.toString(TimeTool.TIMESTAMP));
		IPersistentObject user = ElexisEventDispatcher.getSelected(Anwender.class);
		setExtInfoStoredObjectByKey(Prescription.FLD_EXT_STOPPED_BY, user.getId());
	}
	
	/**
	 * A listing of all administration periods of this prescription. This is to retrieve later when
	 * and how the article was prescribed
	 * 
	 * @return a Map of TimeTools and Doses (Sorted by date)
	 */
	public SortedMap<TimeTool, String> getTerms(){
		TreeMap<TimeTool, String> ret = new TreeMap<TimeTool, String>();
		String raw = (String) getExtInfoStoredObjectByKey(FLD_EXT_TERMS);
		if (raw != null) {
			String[] terms = raw.split(StringTool.flattenSeparator);
			for (String term : terms) {
				if (term.length() > 0) {
					String[] flds = term.split(StringConstants.DOUBLECOLON);
					if (flds != null && flds.length > 0) {
						TimeTool date = new TimeTool(flds[0]);
						String dose = "n/a";
						if (flds.length > 1) {
							dose = flds[1];
						}
						ret.put(date, dose);
					}
				}
			}
		}
		ret.put(new TimeTool(get(FLD_DATE_FROM)), get(FLD_DOSAGE));
		return ret;
	}
	
	/**
	 * A listing of all administration periods of this prescription. This is to retrieve later when
	 * and how the article was prescribed
	 * 
	 * @return a Map of TimeTools and Doses (Sorted by date)
	 */
	public static float calculateTagesDosis(String dosis) throws NumberFormatException{
		float total = 0f;
		List<Float> res = getDoseAsFloats(dosis);
		for (int j = 0; j < res.size(); j++) {
			total += res.get(j);
		}
		return total;
	}
	
	private static float getNum(String num){
		try {
			String n = num.trim();
			if (n.matches("^[0-9]/[0-9]$")) {
				float value = getNum(n.substring(0, 1)) / getNum(n.substring(2));
				return value;
			} else if (n.equalsIgnoreCase("½"))
				return 0.5F;
			else if (n.equalsIgnoreCase("¼"))
				return 0.25F;
			else if (n.equalsIgnoreCase("1½"))
				return 1.5F;
				
			else if (n.indexOf('/') != -1) {
				if (n.length() == 1) {
					return 0.0f;
				}
				String[] bruch = n.split(StringConstants.SLASH);
				if (bruch.length < 2) {
					return 0.0f;
				}
				float zaehler = Float.parseFloat(bruch[0]);
				float nenner = Float.parseFloat(bruch[1]);
				if (nenner == 0.0f) {
					return 0.0f;
				}
				return zaehler / nenner;
			}
			// matching for values like 2x1, 2,5x1 or 20x1
			else if (n.toLowerCase().matches("^[0-9][,.]*[0-9]*x[0-9][,.]*[0-9]*$")) {
				n = n.replace("\\s", "");
				String[] nums = n.toLowerCase().split("x");
				float num1 = Float.parseFloat(nums[0].replace(",", "."));
				float num2 = Float.parseFloat(nums[1].replace(",", "."));
				return num1 * num2;
			}
			// matching numbers with comma i.e. 1,5 and parses it to 1.5 for float value
			else if (n.matches("^[0-9,]")) {
				n = n.replace(",", ".");
				return Float.parseFloat(n);
			}
			// any other digit-letter combination. replaces comma with dot and removes all non-digit chars. i.e. 1,5 p. Day becomes 1.5
			else {
				n = n.replace(",", ".");
				n = n.replaceAll("[^\\d.]", "");
				if (n.endsWith(".")) {
					n = n.substring(0, n.length() - 1);
				}
				return Float.parseFloat(n);
			}
		} catch (NumberFormatException e) {
			ElexisStatus status = new ElexisStatus(ElexisStatus.INFO, CoreHub.PLUGIN_ID,
				ElexisStatus.CODE_NONE, e.getLocalizedMessage(), e);
			ElexisEventDispatcher.fireElexisStatusEvent(status);
			return 0.0F;
		}
	}
	
	/**
	 * 
	 * @return
	 * @since 3.1.0
	 */
	public boolean isFixedMediation(){
		String[] vals = new String[2];
		get(new String[] {
			FLD_REZEPT_ID, FLD_DATE_UNTIL
		}, vals);
		
		return isFixedMedication(vals[0], vals[1]);
	}
	
	/**
	 * 
	 * @param rezeptId
	 * @param dateUntil
	 * @return
	 * @since 3.1
	 */
	public boolean isFixedMedication(String rezeptId, String dateUntil){
		boolean rezeptIdIsNull = rezeptId.length() == 0;
		boolean dateIsNull = dateUntil.length() == 0;
		if (!dateIsNull) {
			TimeTool tt = new TimeTool(dateUntil);
			dateIsNull = tt.isAfter(new TimeTool());
		}
		return (rezeptIdIsNull & dateIsNull);
	}
	
	/**
	 * set prescription type flag
	 * 
	 * @param flagVal
	 *            {@link EntryType#flag}
	 * @param b
	 *            the boolean value to set to
	 * @since 3.1.0
	 */
	public void setPrescType(int flagVal, boolean b){
		int val = getInt(FLD_PRESC_TYPE);
		boolean flag = ((val & flagVal) == flagVal);
		if (b != flag) {
			val ^= flagVal;
			setInt(FLD_PRESC_TYPE, val);
		}
	}
	
	/**
	 * get the boolean value of a prescription type flag
	 * 
	 * @param flag
	 *            {@link EntryType#flag}
	 * @return
	 */
	public boolean isPrescType(int flag){
		return ((getInt(FLD_PRESC_TYPE) & flag) == flag);
	}
	
	/**
	 * 
	 * @param reserve
	 *            this is a medication to keep as reserve or "Reservemedikation"
	 * @since 3.1.0
	 */
	public void setReserveMedication(boolean reserve){
		setPrescType(EntryType.RESERVE_MEDICATION.getFlag(), reserve);
	}
	
	/**
	 * required by JFace data-binding
	 * 
	 * @since 3.1.0
	 */
	public boolean isReserveMedication(){
		return isPrescType(EntryType.RESERVE_MEDICATION.getFlag());
	}
	
	/**
	 * @return the reason for stopping the medication
	 * @since 3.1.0
	 */
	public String getStopReason(){
		return (String) getExtInfoStoredObjectByKey(FLD_EXT_STOP_REASON);
	}
	
	/**
	 * param stopReason the reason for stopping the medication
	 * 
	 * @since 3.1.0
	 */
	public void setStopReason(String stopReason){
		setExtInfoStoredObjectByKey(FLD_EXT_STOP_REASON, stopReason);
	}
	
	/**
	 * @return whether this medication was directly applied during a consultation
	 * @since 3.1.0
	 */
	public boolean isAppliedMedication(){
		return isPrescType(EntryType.APPLICATION.getFlag());
	}
	
	/**
	 * @return the {@link Verrechnet} for an applied medication (check
	 *         {@link #isAppliedMedication()}
	 * @since 3.1.0
	 */
	public @Nullable Verrechnet getVerrechnetForAppliedMedication(){
		String verrechnetId =
			(String) getExtInfoStoredObjectByKey(Prescription.FLD_EXT_VERRECHNET_ID);
		if (verrechnetId != null && verrechnetId.length() > 0) {
			return Verrechnet.load(verrechnetId);
		}
		return null;
	}
	
	/**
	 * @return the prescription type according to {@link EntryType}
	 * @since 3.1.0
	 */
	public EntryType getEntryType(){
		if (isFixedMediation()) {
			if (isReserveMedication()) {
				return EntryType.RESERVE_MEDICATION;
			}
			return EntryType.FIXED_MEDICATION;
		}
		String rezeptId = get(FLD_REZEPT_ID);
		if (rezeptId.equals(FLD_REZEPTID_VAL_DIREKTABGABE)) {
			return (isAppliedMedication()) ? EntryType.APPLICATION : EntryType.SELF_DISPENSED;
			// SD OR APP
		}
		
		if (getDosis().equals(StringConstants.ZERO) && !isAppliedMedication()) {
			return EntryType.FIXED_MEDICATION;
		}
		return EntryType.RECIPE;
	}
	
	/**
	 * The allowed prescription and disposal types  @since 3.1.0
	 */
	public enum EntryType {
		//@formatter:off
		/** Medicine to take over a longer period. <br> i.e. against too high blood pressure, heart medicine **/
		FIXED_MEDICATION (0), 
		/** Medicine given in case a need occurs."Reservemedikation" <br>i.e. patient plans a journey and gets medicine against pain, sickness, insect bites to take in case something happens  **/
		RESERVE_MEDICATION (1), 
		/** Written a recipe for this medicine **/
		RECIPE (2),
		/** For self dispensation **/
		SELF_DISPENSED (3), 
		/** Directly applied during consultation **/
		APPLICATION (4);
		//@formatter:on
		
		private final int flag;
		
		private EntryType(int flag){
			this.flag = flag;
		}
		
		public int getFlag(){
			return flag;
		}
	}
	
	/**
	 * 
	 * @return the {@link Verrechnet} or {@link Rezept} where this prescription was last disposed,
	 *         <code>null</code> if none found
	 * @since 3.1.0
	 */
	public @Nullable IPersistentObject getLastDisposed(){
		String rezeptId = get(Prescription.FLD_REZEPT_ID);
		return getLastDisposed(rezeptId);
	}
	
	/**
	 * @param rezeptId
	 * @return
	 * @since 3.1.0
	 * @see #getLastDisposed()
	 */
	public @Nullable IPersistentObject getLastDisposed(String rezeptId){
		if (StringTool.leer.equals(rezeptId)) {
			// fixed medication - need to find the last disposition by querying db
			Query<Prescription> qre = new Query<Prescription>(Prescription.class);
			qre.add(Prescription.FLD_PATIENT_ID, Query.LIKE, get(Prescription.FLD_PATIENT_ID));
			qre.add(Prescription.FLD_ARTICLE, Query.LIKE, get(Prescription.FLD_ARTICLE));
			qre.add(Prescription.FLD_REZEPT_ID, Query.NOT_EQUAL, StringTool.leer);
			qre.orderBy(true, PersistentObject.FLD_LASTUPDATE);
			List<Prescription> execute = qre.execute();
			if (execute.size() > 0) {
				return execute.get(0).getLastDisposed();
			} else {
				return null;
			}
		} else if (Prescription.FLD_REZEPTID_VAL_DIREKTABGABE.equals(rezeptId)) {
			return getVerrechnetForAppliedMedication();
		} else {
			Query<Prescription> qre = new Query<Prescription>(Prescription.class);
			qre.add(Prescription.FLD_PATIENT_ID, Query.LIKE, get(Prescription.FLD_PATIENT_ID));
			qre.add(Prescription.FLD_ARTICLE, Query.LIKE, get(Prescription.FLD_ARTICLE));
			qre.add(Prescription.FLD_REZEPT_ID, Query.NOT_EQUAL, FLD_REZEPTID_VAL_DIREKTABGABE);
			qre.add(Prescription.FLD_REZEPT_ID, Query.NOT_EQUAL, StringTool.leer);
			qre.orderBy(true, PersistentObject.FLD_LASTUPDATE);
			
			List<Prescription> execute = qre.execute();
			if (execute.size() > 0) {
				return Rezept.load(execute.get(0).get(Prescription.FLD_REZEPT_ID));
			}
		}
		return null;
	}
	
	/**
	 * @return the date to which, according to {@link #getLastDisposed()}, the patient should be
	 *         supplied with medication units, <code>null</code> if unknown
	 * @since 3.1.0
	 */
	public @Nullable TimeTool getSuppliedUntilDate(){
		IPersistentObject po = getLastDisposed();
		if (po == null)
			return null;
			
		float tagesDosis = calculateTagesDosis(getDosis());
		
		String disposeDateString = null;
		if (po instanceof Verrechnet) {
			disposeDateString = ((Verrechnet) po).getKons().get(Konsultation.DATE);
		} else if (po instanceof Rezept) {
			disposeDateString = ((Rezept) po).getDate();
		}
		if (disposeDateString == null || disposeDateString.length() < 1)
			return null;
		TimeTool disposeDate = new TimeTool(disposeDateString);
		
		int packageSize = (getArtikel()!=null) ? getArtikel().getPackungsGroesse() : 0;
		if (packageSize == 0)
			return null;
			
		int noSuppliedDays = Math.round(packageSize / tagesDosis);
		disposeDate.addDays(noSuppliedDays);
		
		return disposeDate;
	}
}
