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

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.interfaces.IOutputter;
import ch.elexis.core.model.BriefConstants;
import ch.elexis.core.text.XRefExtensionConstants;
import ch.rgw.compress.CompEx;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Ein Brief ist ein mit einem externen Programm erstelles Dokument. (Im Moment immer
 * OpenOffice.org). Die Klasse Briefe mit der Tabelle Briefe enthält dabei die Meta-Informationen,
 * während die private Klasse contents mit der Tabelle HEAP die eigentlichen Dokumente als black
 * box, nämlich im Binärformat des erstellenden Programms, enthält. Ein Brief bezieht sich immer auf
 * eine bestimmte Konsultation, zu der er erstellt wurde.
 * 
 * @author Gerry
 * 
 */
public class Brief extends PersistentObject {
	public static final String FLD_MIME_TYPE = "MimeType";
	public static final String FLD_DATE_MODIFIED = "modifiziert";
	public static final String FLD_DATE = "Datum";
	public static final String FLD_TYPE = "Typ";
	public static final String FLD_KONSULTATION_ID = "BehandlungsID";
	public static final String FLD_DESTINATION_ID = "DestID";
	public static final String FLD_SENDER_ID = "AbsenderID";
	public static final String FLD_PATIENT_ID = "PatientID";
	public static final String FLD_SUBJECT = "Betreff";
	public static final String FLD_NOTE = "note";
	public static final String TABLENAME = "BRIEFE";
	public static final String TEMPLATE = BriefConstants.TEMPLATE;
	public static final String AUZ = BriefConstants.AUZ;
	public static final String RP = BriefConstants.RP;
	public static final String UNKNOWN = BriefConstants.UNKNOWN;
	public static final String LABOR = BriefConstants.LABOR;
	public static final String BESTELLUNG = BriefConstants.BESTELLUNG;
	public static final String RECHNUNG = BriefConstants.RECHNUNG;
	
	public static final String MIMETYPE_OO2 = "application/vnd.oasis.opendocument.text";
	public static final String SYS_TEMPLATE = "SYS";
	public static final String DONT_ASK_FOR_ADDRESS_STICKER = "brief_dontaskforaddressee-*-&";
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	static {
		addMapping(TABLENAME, FLD_SUBJECT, FLD_PATIENT_ID, DATE_COMPOUND, FLD_SENDER_ID,
			FLD_DESTINATION_ID, FLD_KONSULTATION_ID, FLD_TYPE, "modifiziert=S:D:modifiziert",
			"geloescht", FLD_MIME_TYPE, "gedruckt=S:D:gedruckt", "Path", FLD_NOTE);
	}
	
	protected Brief(){/* leer */
	}
	
	protected Brief(String id){
		super(id);
	}
	
	/** Einen Brief anhand der ID aus der Datenbank laden */
	public static Brief load(String id){
		return new Brief(id);
	}
	
	/** Einen neuen Briefeintrag erstellen */
	public Brief(String Betreff, TimeTool Datum, Kontakt Absender, Kontakt dest, Konsultation bh,
		String typ){
		try {
			super.create(null);
			if (Datum == null) {
				Datum = new TimeTool();
			}
			String pat = StringTool.leer, bhdl = StringTool.leer;
			if (bh != null) {
				bhdl = bh.getId();
				pat = bh.getFall().getPatient().getId();
			}
			String dst = "";
			if (dest != null) {
				dst = dest.getId();
			}
			String dat = Datum.toString(TimeTool.TIMESTAMP);
			set(new String[] {
				FLD_SUBJECT, FLD_PATIENT_ID, FLD_DATE, FLD_SENDER_ID, FLD_DATE_MODIFIED,
				FLD_DESTINATION_ID, FLD_KONSULTATION_ID, FLD_TYPE, "geloescht"
			}, new String[] {
				Betreff, pat, dat, Absender == null ? StringTool.leer : Absender.getId(), dat, dst,
				bhdl, typ, StringConstants.ZERO
			});
			new contents(this);
		} catch (Throwable ex) {
			ExHandler.handle(ex);
		}
	}
	
	public void setPatient(Person k){
		set(FLD_PATIENT_ID, k.getId());
	}
	
	public void setTyp(String typ){
		set(FLD_TYPE, typ);
	}
	
	public String getTyp(){
		String t = get(FLD_TYPE);
		if (t == null) {
			return "Brief";
		}
		return t;
	}
	
	/** Speichern als Text */
	public boolean save(String cnt){
		contents c = contents.load(getId());
		c.save(cnt);
		set(FLD_DATE_MODIFIED, new TimeTool().toString(TimeTool.TIMESTAMP));
		return true;
	}
	
	/** Speichern in Binärformat */
	public boolean save(byte[] in, String mimetype){
		if (in != null) {
			// if(mimetype.equalsIgnoreCase(MIMETYPE_OO2)){
			contents c = contents.load(getId());
			c.save(in);
			set(FLD_DATE_MODIFIED, new TimeTool().toString(TimeTool.TIMESTAMP));
			set(FLD_MIME_TYPE, mimetype);
			return true;
			// }
			// return false;
		}
		return false;
	}
	
	/** Binärformat laden */
	public byte[] loadBinary(){
		contents c = contents.load(getId());
		c.setDBConnection(getDBConnection());
		return c.getBinary();
	}
	
	/** Textformat laden */
	public String read(){
		contents c = contents.load(getId());
		return c.read();
	}
	
	/** Mime-Typ des Inhalts holen */
	public String getMimeType(){
		String gm = get(FLD_MIME_TYPE);
		if (StringTool.isNothing(gm)) {
			return MIMETYPE_OO2;
		}
		return gm;
	}
	
	public static boolean canHandle(String mimetype){
		/*
		 * if(mimetype.equalsIgnoreCase(MIMETYPE_OO2)){ return true; }
		 */
		return true;
	}
	
	public boolean delete(){
		getConnection().exec("UPDATE HEAP SET deleted='1' WHERE ID=" + getWrappedId());
		String konsID = get(FLD_KONSULTATION_ID);
		if (!StringTool.isNothing(konsID) && (!konsID.equals(SYS_TEMPLATE))) {
			Konsultation kons = Konsultation.load(konsID);
			if ((kons != null) && kons.exists() && (kons.isEditable(false))) {
				kons.removeXRef(XRefExtensionConstants.providerID, getId());
			}
		}
		return super.delete();
	}
	
	public OutputLog logOutput(IOutputter outputter){
		return new OutputLog(this, outputter);
	}
	
	/** Einen Brief unwiederruflich löschen */
	public boolean remove(){
		try {
			getConnection().exec("DELETE FROM HEAP WHERE ID=" + getWrappedId());
			getConnection().exec("DELETE FROM BRIEFE WHERE ID=" + getWrappedId());
		} catch (Throwable ex) {
			ExHandler.handle(ex);
			return false;
		}
		return true;
	}
	
	public String getBetreff(){
		return checkNull(get(FLD_SUBJECT));
	}
	
	public void setBetreff(String nBetreff){
		set(FLD_SUBJECT, nBetreff);
	}
	
	public String getDatum(){
		return new TimeTool(get(FLD_DATE)).toString(TimeTool.DATE_GER);
	}
	
	public Kontakt getAdressat(){
		String dest = get(FLD_DESTINATION_ID);
		return dest == null ? null : Kontakt.load(dest);
	}
	
	public void setAdressat(String adressatId){
		set(FLD_DESTINATION_ID, adressatId);
	}
	
	public Person getPatient(){
		Person pat = Person.load(get(FLD_PATIENT_ID));
		if ((pat != null) && (pat.state() > INVALID_ID)) {
			return pat;
		}
		return null;
	}
	
	public String getLabel(){
		return checkNull(get(FLD_DATE)) + StringTool.space + checkNull(get(FLD_SUBJECT));
	}
	
	private static class contents extends PersistentObject {
		private static final String CONTENTS = "inhalt";
		static final String CONTENT_TABLENAME = "HEAP";
		
		static {
			addMapping(CONTENT_TABLENAME, CONTENTS);
		}
		
		private contents(Brief br){
			create(br.getId());
		}
		
		private contents(String id){
			super(id);
		}
		
		byte[] getBinary(){
			return getBinary(CONTENTS);
		}
		
		private String read(){
			byte[] raw = getBinary();
			if (raw != null) {
				byte[] ret = CompEx.expand(raw);
				return StringTool.createString(ret);
			}
			return "";
		}
		
		private void save(String contents){
			byte[] comp = CompEx.Compress(contents, CompEx.BZIP2);
			setBinary(CONTENTS, comp);
		}
		
		private void save(byte[] contents){
			setBinary(CONTENTS, contents);
		}
		
		@Override
		public String getLabel(){
			return getId();
		}
		
		static contents load(String id){
			return new contents(id);
		}
		
		@Override
		protected String getTableName(){
			return CONTENT_TABLENAME;
		}
		
	}
}
