/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
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

import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class AUF extends PersistentObject {
	
	public static final String FLD_PERCENT = "Prozent";
	public static final String FLD_REASON = "Grund";
	public static final String FLD_CASE_ID = "FallID";
	public static final String FLD_PATIENT_ID = "PatientID";
	public static final String TABLENAME = "AUF";
	public static final String FLD_ZUSATZ = "Zusatz";
	public static final String FLD_DATE_FROM = "von";
	public static final String FLD_DATE_UNTIL = "bis";
	static {
		addMapping(TABLENAME, FLD_PATIENT_ID, FLD_CASE_ID, "von=S:D:DatumVon", "bis=S:D:DatumBis",
			FLD_REASON, FLD_PERCENT, "Zusatz=AUFZusatz", "Erstellt=S:D:DatumAUZ");
	}
	
	public AUF(Fall f, String von, String bis, String proz, String grund){
		if (f != null) {
			Patient p = f.getPatient();
			if (p != null) {
				create(null);
				set(new String[] {
					FLD_PATIENT_ID, FLD_CASE_ID, "von", "bis", FLD_PERCENT, FLD_REASON, "Erstellt"
				}, p.getId(), f.getId(), von, bis, proz, grund,
					new TimeTool().toString(TimeTool.DATE_GER));
			}
		}
		
	}
	
	@Override
	public String getLabel(){
		String[] f = {
			FLD_DATE_FROM, FLD_DATE_UNTIL, FLD_PERCENT, FLD_REASON, "Erstellt"
		};
		String[] v = new String[f.length];
		get(f, v);
		StringBuilder sb = new StringBuilder();
		if (!StringTool.isNothing(v[4])) {
			sb.append("[").append(v[4]).append("]: ");
		}
		sb.append(v[0]).append("-").append(v[1]).append(": ").append(v[2]).append("% (")
			.append(v[3]).append(")");
		return sb.toString();
	}
	
	public Patient getPatient(){
		return getFall().getPatient();
	}
	
	public Fall getFall(){
		return Fall.load(get(FLD_CASE_ID));
	}
	
	public TimeTool getBeginn(){
		return new TimeTool(checkNull(get(FLD_DATE_FROM)));
	}
	
	public TimeTool getEnd(){
		return new TimeTool(checkNull(get(FLD_DATE_UNTIL)));
	}
	
	public void setBeginn(String date){
		set(FLD_DATE_FROM, date);
	}
	
	public void setEnd(String date){
		set(FLD_DATE_UNTIL, date);
	}
	
	public String getGrund(){
		return checkNull(get(FLD_REASON));
	}
	
	public String getZusatz(){
		return checkNull(get(FLD_ZUSATZ));
	}
	
	public String getProzent(){
		return checkNull(get(FLD_PERCENT));
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	public static AUF load(String id){
		return new AUF(id);
	}
	
	protected AUF(){}
	
	protected AUF(String id){
		super(id);
	}
}
