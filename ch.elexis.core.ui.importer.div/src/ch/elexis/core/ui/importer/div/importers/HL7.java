/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/

package ch.elexis.core.ui.importer.div.importers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.exchange.KontaktMatcher;
import ch.elexis.core.ui.importer.div.importers.hl7.internal.AbstractSegment;
import ch.elexis.data.Anschrift;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * This class parses a HL7 file containing lab results. It tries to comply with several possible
 * Substandards of the HL7 and to return always reasonable values for each field.
 * 
 * @author Gerry
 * 
 */
public class HL7 {
	
	static Logger logger = LoggerFactory.getLogger(HL7.class);
	
	private static final String NTE = "NTE"; //$NON-NLS-1$
	private static final String ORC = "ORC"; //$NON-NLS-1$
	private static final String OBX = "OBX"; //$NON-NLS-1$
	private static final String OBR = "OBR"; //$NON-NLS-1$
	private static final String MSH = "MSH"; //$NON-NLS-1$
	
	public enum RECORDTYPE {
		TEXT, CODED, NUMERIC, STRUCTURED, OTHER
	};
	
	public enum RESULTSTATUS {
		CORR, DEL, FINAL, DETAIL, PRELIMINARY, NOTVERIFIED, UNKNOWN
	};
	
	String separator;
	String labName;
	String labID;
	String filename;
	
	String[] lines;
	Kontakt labor;
	Patient pat;
	
	/**
	 * We can force this hl7 to be attributed to a specific lab (if we know, who the sender should
	 * be) by providing a name and a short name. If we pass null, the lab will be taken out of the
	 * file (if a sender is provided here)
	 * 
	 * @param labor
	 *            String
	 * @param kuerzel
	 *            String
	 */
	public HL7(final String labor, final String kuerzel){
		labName = labor;
		labID = kuerzel;
	}
	
	public String getSeparator(){
		return this.separator;
	}
	
	/**
	 * Load file into memory and break it up to separate lines. All other methods should only be
	 * called after load was successful. To comply with some of the many standards around, we accept
	 * \n and \r and any combination thereof as field separators
	 * 
	 * @param filename
	 *            String
	 * @return
	 */
	public Result<Object> load(final String filename){
		File file = new File(filename);
		this.filename = filename;
		if (!file.canRead()) {
			return new Result<Object>(SEVERITY.WARNING, 1, Messages.HL7_CannotReadFile, filename,
				true);
		}
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis, "iso-8859-1"); //$NON-NLS-1$
			// FileReader fr = new FileReader(file);
			
			char[] in = new char[(int) file.length()];
			if (isr.read(in) != in.length) {
				return new Result<Object>(SEVERITY.WARNING, 3, "EOF", filename, true); //$NON-NLS-1$
			}
			String hl7raw = new String(in);
			lines = hl7raw.split("[\\r\\n]+"); //$NON-NLS-1$
			separator = "\\" + lines[0].substring(3, 4); //$NON-NLS-1$
			isr.close();
			return new Result<Object>("OK"); //$NON-NLS-1$
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return new Result<Object>(SEVERITY.ERROR, 2, Messages.HL7_ExceptionWhileReading,
				ex.getMessage(), true);
		}
		
	}
	
	/**
	 * Load the message and break it up to separate lines. All other methods should only be called
	 * after load was successful. To comply with some of the many standards around, we accept \n and
	 * \r and any combination thereof as field separators
	 * 
	 * @param message
	 *            String
	 * @return
	 */
	public Result<Object> loadMessage(final String message){
		lines = message.split("[\\r\\n]+"); //$NON-NLS-1$
		separator = "\\" + lines[0].substring(3, 4); //$NON-NLS-1$"
		return new Result<Object>("OK"); //$NON-NLS-1$
	}
	
	/**
	 * find a single HL7-Record
	 * 
	 * @param header
	 *            header identifying the desired record
	 * @param start
	 *            what line to start scanning
	 * @return the first occurence of an element of type 'header' after 'start' lines or an empty
	 *         Element if no such record was found
	 */
	private String[] getElement(final String header, final int start){
		for (int i = start; i < lines.length; i++) {
			if (lines[i].startsWith(header)) {
				return lines[i].split(separator);
			}
		}
		return new String[0];
	}
	
	public String getFilename(){
		return filename;
	}
	
	/**
	 * This method tries to find the patient denoted by this HL7-record. We try the PID-field
	 * PatientID, that is documented as "PlacerID". But unfortunately not all labs use this field.
	 * Thus we try secondly the ORC-field "Placer Order Number". If the fields are different, we use
	 * the ORC field. The Order number then is interpreted as a checksummed order number
	 * (Patient-Number+modulo10+ -HHmm). If we cannot find the Patient using this method, we try to
	 * find him/her with the name and birthdate. If we still cannot make an unambiguous
	 * identification, we ask the user to tell us, who this lab result belongs to. If the user can't
	 * decide we refuse the import.
	 * 
	 * This mess happens, because the labs interpret the hl7 'standard' differently and
	 * inconsistently.
	 * 
	 * @param createIfNotFound
	 *            create the patient record in the database if neccessary
	 * @return the Patient or null if it was not found and createIfNotFound was false, or an error
	 *         indicating the problem if it could not be created
	 */
	public Result<Object> getPatient(final boolean createIfNotFound){
		Query<Patient> qbe = new Query<Patient>(Patient.class);
		List<Patient> list = null;
		String nachname = ""; //$NON-NLS-1$
		String vorname = ""; //$NON-NLS-1$
		String gebdat = ""; //$NON-NLS-1$
		String sex = Person.FEMALE;
		
		if (pat == null) {
			String[] elPid = getElement("PID", 0); //$NON-NLS-1$
			String patid = elPid[2];
			if (StringTool.isNothing(patid)) {
				patid = elPid[3];
				if (StringTool.isNothing(patid)) {
					patid = elPid[4];
					if (patid == null) {
						patid = ""; //$NON-NLS-1$
					}
				}
			}
			String[] pidflds = patid.split("[\\^ ]+"); //$NON-NLS-1$
			String pid = "";
			if (pidflds.length > 0)
				pid = pidflds[pidflds.length - 1];
			
			String[] orc = getElement(ORC, 0);
			if (orc.length > 2) {
				String orderNumber = orc[2]; // Placer order number
				if (orderNumber.length() > 0) {
					if (orderNumber.indexOf('-') != -1) {
						pid = StringTool.checkModulo10(orderNumber.split("-")[0]); //$NON-NLS-1$
					} else {
						pid = orderNumber;
					}
				}
				
			}
			if (pid.indexOf('-') != -1) {
				pid = StringTool.checkModulo10(pid.split("-")[0]); //$NON-NLS-1$
			}
			if (pid != null) {
				// Find a patient with the given ID
				qbe.add(Patient.FLD_PATID, Query.EQUALS, pid);
				list = qbe.execute();
			}
			
			if (elPid.length > 5) {
				String[] name = elPid[5].split("\\^"); //$NON-NLS-1$
				if (name.length > 0) {
					nachname = name[0];
				}
				if (name.length > 1) {
					vorname = name[1];
				}
				if (elPid.length > 7) {
					gebdat = elPid[7];
					if (elPid.length > 8) {
						sex = elPid[8].equalsIgnoreCase("M") ? Person.MALE : Person.FEMALE; //$NON-NLS-1$
					} else {
						sex = StringTool.isFemale(vorname) ? Person.FEMALE : Person.MALE;
					}
				}
			}
			if ((pid == null) || (list.size() != 1)) {
				// We did not find the patient using the PatID, so we try the
				// name and birthdate
				qbe.clear();
				qbe.add(Person.NAME, Query.EQUALS, StringTool.normalizeCase(nachname));
				qbe.add(Person.FIRSTNAME, Query.EQUALS, StringTool.normalizeCase(vorname));
				qbe.add(Person.BIRTHDATE, Query.EQUALS,
					new TimeTool(gebdat).toString(TimeTool.DATE_COMPACT));
				list = qbe.execute();
				if ((list != null) && (list.size() == 1)) {
					pat = list.get(0);
				} else {
					if (createIfNotFound) {
						String address = StringConstants.EMPTY;
						String phone = StringConstants.EMPTY;
						if (elPid.length > 11) {
							address = elPid[11];
							if (elPid.length > 13) {
								phone = elPid[13];
							}
						}
						pat = new Patient(nachname, vorname, gebdat, sex);
						pat.set(Patient.FLD_PATID, pid);
						String[] adr = address.split("\\^+"); //$NON-NLS-1$
						Anschrift an = pat.getAnschrift();
						if (adr.length > 0) {
							an.setStrasse(adr[0]);
							if (adr.length > 1) {
								an.setOrt(adr[1]);
								if (adr.length > 2) {
									an.setPlz(adr[2].length() > 5 ? adr[2].substring(0, 4) : adr[2]);
									if (adr.length > 3) {
										an.setLand(adr[3]);
									}
								}
							}
						}
						
						pat.setAnschrift(an);
						pat.set(Patient.FLD_PHONE1, phone);
					} else {
						pat =
							(Patient) KontaktSelektor.showInSync(Patient.class,
								Messages.HL7_SelectPatient, Messages.HL7_WhoIs + nachname + " " //$NON-NLS-1$
									+ vorname + " ," + gebdat + "?"); //$NON-NLS-1$ //$NON-NLS-2$
						if (pat == null) {
							return new Result<Object>(SEVERITY.WARNING, 1,
								Messages.HL7_PatientNotInDatabase, null, true);
						}
					}
				}
			} else {
				// if the patient with the given ID was found, we verify, if it
				// is the correct name
				pat = list.get(0);
				if (nachname.length() != 0 && vorname.length() != 0) {
					if (!KontaktMatcher.isSame(pat, nachname, vorname, gebdat)) {
						StringBuilder sb = new StringBuilder();
						sb.append(Messages.HL7_NameConflictWithID)
							.append(pid)
							.append(":\n") //$NON-NLS-1$
							.append(Messages.HL7_Lab).append(nachname).append(StringTool.space)
							.append(vorname)
							.append("(").append(sex).append("),").append(gebdat).append("\n") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							.append(Messages.HL7_Database).append(pat.getLabel());
						pat = null;
						return new Result<Object>(SEVERITY.WARNING, 4, sb.toString(), null, true);
					}
				}
			}
		}
		return new Result<Object>(pat);
	}
	
	public Result<String> getUID(){
		String[] msh = getElement("MSH", 0); //$NON-NLS-1$
		if (msh.length > 9) {
			return new Result<String>(msh[9]);
		}
		return new Result<String>(SEVERITY.ERROR, 1, "Invalid MSH", "Error", true); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * 
	 * @return Date of the HL7 message
	 */
	public TimeTool getDate(){
		String[] msh = getElement("MSH", 0);
		if (msh.length > 9) {
			return new TimeTool(msh[6]);
		}
		return new TimeTool();
	}
	
	/**
	 * Find the lab issuing this file. If we provided a lab name in the constructor, ths will return
	 * that lab.
	 * 
	 * @return the lab or null if it could not be found
	 */
	public Result<Kontakt> getLabor(){
		return new Result<Kontakt>(LabImportUtil.getOrCreateLabor(labName));
	}
	
	/**
	 * Find the first OBR record in the file
	 * 
	 * @return an OBR (which might be empty)
	 */
	public OBR firstOBR(){
		OBR ret = new OBR(0).nextOBR(0);
		if (ret == null) {
			ret = new OBR(0);
			ret.field = new String[20];
		}
		return ret;
	}
	
	public MSH getMSH(){
		MSH ret = new MSH(0);
		if (ret.isValid()) {
			return ret;
		}
		return null;
	}
	
	/**
	 * Find the index of the next Element of a given type
	 * 
	 * @param type
	 *            String
	 * @param prev
	 *            position to start searching
	 * @return
	 */
	int findNext(final String type, final int prev){
		for (int i = prev; i < lines.length; i++) {
			if (lines[i].startsWith(type)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * MessageHeader element, 21 entries
	 * 
	 * @author Lucia
	 * 
	 */
	public class MSH extends AbstractSegment {
		/** Receiving facility */
		public static final int RECV_FAC_INDEX = 5;
		/** Date/Time of Message */
		public static final int DATETIME_INDEX = 6;
		/** Message type */
		public static final int MESSAGE_TYPE_INDEX = 8;
		
		public MSH(final int of){
			this.of = of;
			field = lines[of].split(separator);
		}
		
		public boolean isValid(){
			return getFieldAt(0).equals(MSH);
		}
		
		public TimeTool getDate(){
			String date = getFieldAt(DATETIME_INDEX);
			
			TimeTool tt = makeTime(date);
			if (tt == null) {
				return new TimeTool();
			}
			return tt;
		}
		
		public String[] getFields(){
			return field;
		}
	}
	
	public class OBR extends AbstractSegment {
		/** Observation Time of Message */
		public static final int REQUESTED_TIME_INDEX = 6;
		/** Observation Time of Message */
		public static final int OBSERVATION_TIME_INDEX = 7;
		/** Status Changed Time of Message */
		public static final int STATUS_TIME_INDEX = 22;
		
		public OBR(final int off){
			of = off;
			field = lines[of].split(separator);
		}
		
		public OBR nextOBR(final OBR obr){
			return nextOBR(obr.of);
		}
		
		OBR nextOBR(final int of){
			int n = findNext(OBR, of + 1);
			if (n == -1) {
				return null;
			}
			return new OBR(n);
		}
		
		/**
		 * Find the next OBX after a given OBX
		 * 
		 * @param old
		 *            the OBX from which to start searching
		 * @return the next OBX or null if none was found
		 */
		public OBX nextOBX(final OBX old){
			return nextOBX(old.getOf());
		}
		
		/**
		 * Find the first OBX of this OBR
		 * 
		 * @return an OBX or null if none found
		 */
		public OBX firstOBX(){
			int oldof = of;
			while (++of < lines.length) {
				if (lines[of].startsWith(OBX)) {
					return new OBX(this, of);
				}
				if (lines[of].startsWith(OBR)) {
					of -= 1;
					return null;
				}
			}
			of = oldof;
			return null;
		}
		
		/**
		 * Find the next OBX after a given position
		 * 
		 * @param old
		 *            the position to start looking from
		 * @return the first OBX after 'old' or null if none was found
		 */
		OBX nextOBX(final int old){
			int nf = old + 1;
			while (true) {
				if (nf >= lines.length) {
					return null;
				}
				if (lines[nf].startsWith(OBX)) {
					return new OBX(this, nf);
				}
				if (lines[nf].startsWith(OBR)) {
					return null;
				}
				nf += 1;
			}
		}
		
		/**
		 * Unfortunately, not all labs use all date fields. So we try several possible positions.
		 * 
		 * @return the OBR's date (obr[7]). If none was found, it will be the date of today.
		 */
		public TimeTool getDate(){
			String date = getFieldAt(OBSERVATION_TIME_INDEX);
			
			if (date.equals(INVALID)) {
				date = getFieldAt(STATUS_TIME_INDEX);
				if (date.equals(INVALID)) {
					date = getFieldAt(REQUESTED_TIME_INDEX);
					if (date.equals(INVALID)) {
						return new TimeTool();
					}
				}
			}
			TimeTool tt = makeTime(date);
			if (tt == null) {
				return new TimeTool();
			}
			return tt;
		}
		
		/**
		 * Get the observation time of the OBR record. If it is empty null is returned.
		 * 
		 * @return
		 */
		public TimeTool getObservationTime(){
			String date = getFieldAt(OBSERVATION_TIME_INDEX);
			
			if (date.equals(INVALID)) {
				date = getFieldAt(STATUS_TIME_INDEX);
				if (date.equals(INVALID)) {
					date = getFieldAt(REQUESTED_TIME_INDEX);
					if (date.equals(INVALID)) {
						return new TimeTool();
					}
				}
			}
			if (!date.isEmpty()) {
				return makeTimeStamp(date);
			}
			return null;
		}
		
		@Override
		public boolean isValid(){
			return getFieldAt(0).equals(OBR);
		}
	}
	
	public class OBX extends AbstractSegment {
		/** Analysis Time of Message */
		public static final int ANALYSIS_TIME_INDEX = 19;
		/** Observation Time of Message */
		public static final int OBSERVATION_TIME_INDEX = 14;
		
		private static final String FT = "FT"; //$NON-NLS-1$
		private static final String TX = "TX"; //$NON-NLS-1$
		private static final String SN = "SN"; //$NON-NLS-1$
		private static final String MO = "MO"; //$NON-NLS-1$
		private static final String CE = "CE"; //$NON-NLS-1$
		private static final String NM = "NM"; //$NON-NLS-1$
		
		OBR myOBR;
		
		OBX(final OBR obr, final int off){
			setPosition(off);
			myOBR = obr;
		}
		
		public int getOf(){
			return of;
		}
		
		private void setPosition(final int off){
			of = off;
			field = lines[of].split(separator);
			
			// remove spaces from numeric results
			if (field.length >= 5) {
				String value = field[5];
				value = value.replace(" ", "");
				if (isNumeric(value)) {
					field[5] = value;
				}
			}
			
			// remove spaces from numeric reference ranges
			if (field.length >= 7) {
				String value = field[7];
				value = value.replace(" ", "");
				if (isNumeric(value)) {
					field[7] = value;
				}
			}
		}
		
		public int getLineOffset(){
			return of;
		}
		
		public String getObxNr(){
			return field[1];
		}
		
		public String getItemCode(){
			String[] fl = getField(3).split("\\^"); //$NON-NLS-1$
			if (fl[0].startsWith("HIS-")) { //$NON-NLS-1$
				return "Hist"; //$NON-NLS-1$
			} else {
				return fl[0];
			}
		}
		
		public String getItemName(){
			String raw = getField(3);
			String[] split = raw.split("\\^"); //$NON-NLS-1$
			if (split.length > 1) {
				if (split[0].startsWith("HIS-")) { //$NON-NLS-1$
					return Messages.HL7_Hostologie;
				} else {
					return split[1];
				}
			}
			return split[0];
		}
		
		public String getResultValue(){
			StringBuilder ret = new StringBuilder();
			int lastPos = of;
			if (getType().equals(RECORDTYPE.TEXT)) {
				String[] flds = getField(3).split("\\^"); //$NON-NLS-1$
				if (flds.length > 1) {
					while (flds[0].startsWith("HIS-")) { //$NON-NLS-1$
						lastPos = of;
						ret.append("*.").append(flds[1]).append(".*:\n").append(getField(5)) //$NON-NLS-1$ //$NON-NLS-2$
							.append("\n\n"); //$NON-NLS-1$
						OBX nextOBX = myOBR.nextOBX(this);
						if (nextOBX == null) {
							break;
						}
						setPosition(nextOBX.of);
						flds = getField(3).split("\\^"); //$NON-NLS-1$
					}
					setPosition(lastPos);
				}
			}
			if (ret.length() == 0) {
				return getField(5);
			} else {
				return ret.toString();
			}
		}
		
		public String getUnits(){
			String raw = getField(6);
			String[] split = raw.split("\\^"); //$NON-NLS-1$
			if (split.length > 0) {
				return split[0];
			}
			return "-"; //$NON-NLS-1$
		}
		
		public String getRefRange(){
			return getField(7);
		}
		
		/**
		 * Unfortunately, the date field is not provided by all applications. If we don't find an
		 * OBX date, we use the OBR date.
		 * 
		 * @return
		 * @deprecated better use obr date for date of sample
		 */
		@Deprecated
		public TimeTool getDate(){
			String tim = getField(14);
			if (tim.length() == 0) {
				return myOBR.getDate();
			}
			return makeTime(tim);
		}
		
		/**
		 * Get the observation time of the OBX record. If it is empty the observation time of the
		 * OBR record is returned.
		 * 
		 * @return
		 */
		public TimeTool getObservationTime(){
			String tim = getField(OBSERVATION_TIME_INDEX);
			
			if (tim.equals(INVALID) || tim.isEmpty()) {
				tim = getField(ANALYSIS_TIME_INDEX);
				if (tim.equals(INVALID) || tim.isEmpty()) {
					return myOBR.getObservationTime();
				}
			}
			return makeTimeStamp(tim);
		}
		
		private String[] abnormalFlagStartCharacters = {
			"-", "+", "<", ">", "L", "H"
		};
		
		/**
		 * This is greatly simplified from the possible values <<, <, >,>>, +, ++, -, -- and so on
		 * we just say "it's pathologic".
		 * 
		 * @return true if it's any of the pathologic values.
		 */
		public boolean isPathologic(){
			String abnormalFlag = getField(8);
			if (!StringTool.isNothing(abnormalFlag)) {
				for (String startChar : abnormalFlagStartCharacters) {
					if (abnormalFlag.startsWith(startChar)) {
						return true;
					}
				}
			}
			return false;
		}
		
		public RECORDTYPE getType(){
			String type = getField(2);
			if (type.equals(TX) || type.equals("ST") || type.equals(FT)) { //$NON-NLS-1$
				if (isNumeric(getField(5))) {
					return RECORDTYPE.NUMERIC;
				}
				return RECORDTYPE.TEXT;
			} else if (type.equals(NM) || type.equals(MO)) {
				return RECORDTYPE.NUMERIC;
			} else if (type.equals(CE)) {
				return RECORDTYPE.CODED;
			} else if (type.equals(SN)) {
				return RECORDTYPE.STRUCTURED;
			} else {
				return RECORDTYPE.OTHER;
			}
		}
		
		/**
		 * HL7 defines TX for plaintext and NM for numeric. Unfortunately, some labs put numbers in
		 * TX fields, thus we have to check
		 * 
		 * @return true if the field is TX and contains not only numbers.
		 */
		public boolean isPlainText(){
			if (getField(2).equals(TX)) {
				String res = getField(5);
				return !isNumeric(res);
			}
			return false;
		}
		
		private boolean isNumeric(String str){
			return str.matches("((<|>|-|\\+)?[0-9]+(\\.[0-9]+)?)+"); //$NON-NLS-1$
		}
		
		public boolean isNumeric(){
			String type = getField(2);
			if (type.equals(TX)) {
				String res = getField(5);
				res = res.replace(" ", "");
				return isNumeric(res);
			} else if (type.equals(NM)) {
				return true;
			}
			return false;
		}
		
		public boolean isFormattedText(){
			return (field[2].equals(FT));
		}
		
		public RESULTSTATUS getStatus(){
			String stat = getField(11);
			if (stat.equals("C")) { //$NON-NLS-1$
				return RESULTSTATUS.CORR;
			} else if (stat.equals("D")) { //$NON-NLS-1$
				return RESULTSTATUS.DEL;
			} else if (stat.equals("F")) { //$NON-NLS-1$
				return RESULTSTATUS.FINAL;
			} else if (stat.equals("O")) { //$NON-NLS-1$
				return RESULTSTATUS.DETAIL;
			} else if (stat.equals("P")) { //$NON-NLS-1$
				return RESULTSTATUS.PRELIMINARY;
			} else if (stat.equals("R")) { //$NON-NLS-1$
				return RESULTSTATUS.NOTVERIFIED;
			} else {
				return RESULTSTATUS.UNKNOWN;
			}
		}
		
		/**
		 * Find the comment field of this OBX. Funny enough this is stored outside of the OBX
		 * usually. To make things simpler, we put all comments one after another in the same string
		 * (why not?).
		 * 
		 * @return The comment (that can be an empty String or might contain several NTE records)
		 */
		public String getComment(){
			return getOBXComments(lines, this);
		}
		
		private String getField(final int f){
			if (field.length > f) {
				return field[f];
			}
			return StringConstants.EMPTY;
		}
		
		@Override
		public boolean isValid(){
			return getFieldAt(0).equals(OBX);
		}
	}
	
	/**
	 * Findet Kommentare zu einem OBX. Standartmässig werden die NTE Kommentare anhand der obxNr
	 * gesucht.
	 * 
	 * @param hl7Rows
	 * @return String
	 */
	protected String getOBXComments(String[] hl7Rows, OBX obx){
		String comments = getFollowOBXComments(hl7Rows, obx);
		if (comments.isEmpty()) {
			comments = getMatchingOBXComment(hl7Rows, obx);
		}
		return comments;
	}
	
	private String getFollowOBXComments(String[] hl7Rows, OBX obx){
		int lineOffset = obx.getLineOffset() + 1;
		StringBuilder ret = new StringBuilder();
		for (int i = lineOffset; i < hl7Rows.length; i++) {
			if (hl7Rows[i].startsWith(NTE)) {
				String[] nte = hl7Rows[i].split(separator);
				if (nte.length > 1 && i == lineOffset) {
					if (!nte[1].equals("1")) {
						break;
					}
				}
				if (nte.length > 3) {
					ret.append(nte[3]).append(StringTool.lf);
				}
			} else {
				break;
			}
		}
		return ret.toString();
	}
	
	private String getMatchingOBXComment(String[] hl7Rows, OBX obx){
		int lineOffset = obx.getLineOffset() + 1;
		String obxNr = obx.getObxNr();
		StringBuilder ret = new StringBuilder();
		for (int i = lineOffset; i < hl7Rows.length; i++) {
			if (hl7Rows[i].startsWith(NTE)) {
				String[] nte = hl7Rows[i].split(separator);
				if (nte.length > 1) {
					if (nte[1].equals(obxNr)) {
						if (nte.length > 3) {
							ret.append(nte[3]).append(StringTool.lf);
						}
					}
				}
			} else {
				break;
			}
		}
		return ret.toString();
	}
	
	/**
	 * Findet alle Kommentare. Standartmässig werden die NTE Kommentare anhand der obxNr angehängt.
	 * 
	 * @param hl7Rows
	 * @return String
	 */
	protected String getComments(String[] hl7Rows){
		StringBuffer comments = new StringBuffer();
		
		for (int i = 0; i < hl7Rows.length; i++) {
			if (hl7Rows[i].startsWith(NTE)) {
				String[] nte = hl7Rows[i].split(separator);
				if (nte.length > 3) {
					String rawComment;
					String source = nte[1];
					if (source.matches("^0*$")) { //$NON-NLS-1$
						// independent comment
						rawComment = nte[3];
					} else {
						// OBX comment
						String obxName = getItemNameForNTE(source);
						rawComment = obxName + ": " + nte[3]; //$NON-NLS-1$
					}
					comments.append(rawComment);
					comments.append("\n"); //$NON-NLS-1$
				}
			}
		}
		
		return comments.toString();
	}
	
	/**
	 * Extract all comments (NTE), global and OBX comments
	 * 
	 * @return a string containing all comments, separated by newlines
	 */
	public String getComments(){
		return getComments(lines);
	}
	
	/**
	 * Get Item Name in OBX corresponding to NTE Helper method for getComments()
	 * 
	 * @param source
	 * @return the item's name, or "" if not found
	 */
	private String getItemNameForNTE(final String source){
		String[] obx;
		int i = findNext(OBX, 0);
		while (i != -1) {
			obx = lines[i].split(separator);
			if (obx[1].equals(source)) {
				String raw = obx[3];
				String[] split = raw.split("\\^"); //$NON-NLS-1$
				String obxName;
				if (split.length > 1) {
					obxName = split[1];
				} else {
					obxName = split[0];
				}
				
				return obxName;
			}
			
			i = findNext(OBX, i + 1);
		}
		
		// not found
		return ""; //$NON-NLS-1$
	}
	
	public static TimeTool makeTime(final String datestring){
		if (datestring.length() >= 8) {
			String date = datestring.substring(0, 8);
			TimeTool ret = new TimeTool();
			if (ret.set(date)) {
				return ret;
			}
		}
		return null;
	}
	
	public static TimeTool makeTimeStamp(final String datestring){
		String timestamp = datestring;
		if (timestamp.length() >= 8) {
			if (timestamp.length() < 14) {
				// fill missing values with 0
				for (int i = timestamp.length(); i < 14; i++) {
					timestamp = timestamp + "0";
				}
			}
			return new TimeTool(timestamp);
		}
		return null;
	}
}
