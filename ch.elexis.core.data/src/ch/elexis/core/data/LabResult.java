/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.data;

import java.util.LinkedList;
import java.util.List;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class LabResult extends PersistentObject {
	public static final String LABRESULT_UNSEEN = "Labresult:unseen";
	public static final String DATE = "Datum";
	public static final String TIME = "Zeit";
	public static final String FLAGS = "Flags";
	public static final String COMMENT = "Kommentar";
	public static final String RESULT = "Resultat";
	public static final String ITEM_ID = "ItemID";
	public static final String PATIENT_ID = "PatientID";
	public static final int PATHOLOGIC = 1 << 0;
	public static final int OBSERVE = 1 << 1; // Anwender erklärt den Parameter für
	// beobachtungswürdig
	public static final int NORMAL = 1 << 2; // Anwender erklärt den Wert explizit für normal (auch
	// wenn er formal ausserhalb des Normbereichs ist)
	
	private static final String TABLENAME = "LABORWERTE";
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	static {
		addMapping(TABLENAME, PATIENT_ID, DATE_COMPOUND, ITEM_ID, RESULT, COMMENT, FLAGS,
			"Quelle=Origin", TIME);
	}
	
	/**
	 * create a new LabResult. If the type is numeric, we'll check whether it's pathologic
	 */
	public LabResult(final Patient p, final TimeTool date, final LabItem item, final String result,
		final String comment){
		create(null);
		String[] fields = {
			PATIENT_ID, DATE, ITEM_ID, RESULT, COMMENT, FLAGS
		};
		int flags = isPathologic(p, item, result) ? PATHOLOGIC : 0;
		String[] vals =
			new String[] {
				p.getId(),
				date == null ? new TimeTool().toString(TimeTool.DATE_GER) : date
					.toString(TimeTool.DATE_GER), item.getId(), result, comment,
				Integer.toString(flags)
			};
		set(fields, vals);
		addToUnseen();
	}
	
	private boolean isPathologic(final Patient p, final LabItem item, final String result){
		if (item.getTyp().equals(LabItem.typ.ABSOLUTE)) {
			if (result.toLowerCase().startsWith("pos")) {
				return true;
			}
			if (result.trim().startsWith("+")) {
				return true;
			}
		} else /* if(item.getTyp().equals(LabItem.typ.NUMERIC)) */{
			String nr;
			if (p.getGeschlecht().equalsIgnoreCase(Person.MALE)) {
				nr = item.getRefM();
			} else {
				nr = item.getRefW();
			}
			if (nr.trim().startsWith("<")) {
				try {
					double ref = Double.parseDouble(nr.substring(1).trim());
					double val = Double.parseDouble(result);
					if (val >= ref) {
						return true;
					}
				} catch (NumberFormatException nfe) {
					// don't mind
				}
			} else if (nr.trim().startsWith(">")) {
				try {
					double ref = Double.parseDouble(nr.substring(1).trim());
					double val = Double.parseDouble(result);
					if (val <= ref) {
						return true;
					}
				} catch (NumberFormatException nfe) {
					// again, don't mind
				}
			} else {
				String[] range = nr.split("\\s*-\\s*");
				if (range.length == 2) {
					try {
						double lower = Double.parseDouble(range[0]);
						double upper = Double.parseDouble(range[1]);
						double val = Double.parseDouble(result);
						if ((val < lower) || (val > upper)) {
							return true;
						}
					} catch (NumberFormatException nre) {
						// still, we don't mind
					}
				}
			}
		}
		return false;
		
	}
	
	public static LabResult load(final String id){
		return new LabResult(id);
	}
	
	public Patient getPatient(){
		return Patient.load(get(PATIENT_ID));
	}
	
	public String getDate(){
		return get(DATE);
	}
	
	public TimeTool getDateTime(){
		String temp = get(TIME);
		if ((temp == null) || ("".equals(temp)))
			temp = "000000";
		while (temp.length() < 6) {
			temp += "0";
		}
		return new TimeTool(get(DATE) + " " + temp.substring(0, 2) + ":" + temp.substring(2, 4)
			+ ":" + temp.substring(4, 6));
	}
	
	public LabItem getItem(){
		return LabItem.load(get(ITEM_ID));
	}
	
	public String getResult(){
		return checkNull(get(RESULT));
	}
	
	public void setResult(final String res){
		int flags = isPathologic(getPatient(), getItem(), res) ? PATHOLOGIC : 0;
		set(new String[] {
			RESULT, FLAGS
		}, new String[] {
			checkNull(res), Integer.toString(flags)
		});
	}
	
	public String getComment(){
		return checkNull(get(COMMENT));
		
	}
	
	public boolean isFlag(final int flag){
		return (getFlags() & flag) != 0;
	}
	
	public void setFlag(final int flag, final boolean set){
		int flags = getFlags();
		if (set) {
			flags |= flag;
		} else {
			flags &= ~(flag);
		}
		setInt(FLAGS, flags);
	}
	
	public int getFlags(){
		return checkZero(get(FLAGS));
	}
	
	protected LabResult(){}
	
	protected LabResult(final String id){
		super(id);
	}
	
	@Override
	public String getLabel(){
		StringBuilder sb = new StringBuilder();
		sb.append(getItem().getLabel()).append(", ").append(getDate()).append(": ")
			.append(getResult());
		return sb.toString();
		// return getResult();
	}
	
	public static LabResult getForDate(final Patient pat, final TimeTool date, final LabItem item){
		Query<LabResult> qbe = new Query<LabResult>(LabResult.class);
		qbe.add(ITEM_ID, Query.EQUALS, item.getId());
		qbe.add(PATIENT_ID, Query.EQUALS, pat.getId());
		qbe.add(DATE, Query.EQUALS, date.toString(TimeTool.DATE_COMPACT));
		List<LabResult> res = qbe.execute();
		if ((res != null) && (res.size() > 0)) {
			return res.get(0);
		}
		return null;
	}
	
	/**
	 * add a LabResult to the list of unseen LabResults. We do not keep LabResults older than
	 * KEEP_UNSEEN_LAB_RESULTS days in this list.
	 */
	public void addToUnseen(){
		List<LabResult> o = getUnseen();
		LinkedList<String> n = new LinkedList<String>();
		n.add(getId());
		TimeTool limit = new TimeTool();
		try { // We need to catch wrong formatted numbers in KEEP_UNSEEN
			limit.addHours(-24
				* Integer.parseInt(CoreHub.globalCfg.get(Preferences.LABSETTINGS_CFG_KEEP_UNSEEN_LAB_RESULTS,
					Preferences.DAYS_TO_KEEP_UNSEEN_LAB_RESULTS)));
		} catch (NumberFormatException nex) {
			ExHandler.handle(nex);
			limit.addHours(-24 * 7);
		}
		// log.log(limit.dump(),Log.INFOS);
		TimeTool tr = new TimeTool();
		for (LabResult lr : o) {
			log.info(lr.getDate());
			if (tr.set(lr.getDate())) {
				if (tr.isAfter(limit)) {
					n.add(lr.getId());
				}
			}
		}
		NamedBlob unseen = NamedBlob.load(LABRESULT_UNSEEN);
		String results = StringTool.join(n, ",");
		unseen.putString(results);
		// unseen.set("lastupdate", new TimeTool().toString(TimeTool.TIMESTAMP));
	}
	
	/**
	 * Remove a lab result from the list of unseen results.
	 */
	public void removeFromUnseen(){
		NamedBlob unseen = NamedBlob.load(LABRESULT_UNSEEN);
		String results = unseen.getString();
		results = results.replaceAll(getId(), StringTool.leer);
		unseen.putString(results.replaceAll(",,", ","));
		// unseen.set("lastupdate", new TimeTool().toString(TimeTool.TIMESTAMP));
	}
	
	/**
	 * Return a List of unseen LabResults
	 * 
	 * @return
	 */
	public static List<LabResult> getUnseen(){
		LinkedList<LabResult> ret = new LinkedList<LabResult>();
		NamedBlob unseen = NamedBlob.load(LABRESULT_UNSEEN);
		String results = unseen.getString();
		if (results.length() > 0) {
			for (String id : results.split(",")) {
				LabResult lr = load(id);
				if (lr.exists()) {
					ret.add(lr);
				}
			}
		}
		return ret;
	}
	
	/**
	 * return the time when the last change to the list of unseen results was made
	 * 
	 * @return a timestamp (as in System.CurrentTimeMillis())
	 */
	public static long getLastUpdateUnseen(){
		NamedBlob unseen = NamedBlob.load(LABRESULT_UNSEEN);
		long lastup = unseen.getLastUpdate();
		return lastup;
	}
}
