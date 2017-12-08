/*******************************************************************************
 * Copyright (c) 2005-2008, G. Weirich, D.Lutz and Elexis
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

import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.net.NetTool;

public class DBLog extends PersistentObject {
	private static final String TABLENAME = "LOGS";
	
	public static enum TYP {
		DELETE, UNDELETE, UNKNOWN
	};
	
	static {
		addMapping(TABLENAME, "OID", "Datum=S:D:datum", "typ", "userID", "station", "ExtInfo");
	}
	
	public DBLog(PersistentObject obj, TYP typ){
		create(null);
		if (typ == null) {
			typ = TYP.UNKNOWN;
		}
		String user = "?";
		if (CoreHub.actUser != null) {
			user = CoreHub.actUser.getId();
		}
		String hostname = "?";
		if (NetTool.hostname != null) {
			hostname = NetTool.hostname;
		}
		String oid = obj.storeToString();
		if (oid == null) {
			oid = obj.getId();
		}
		
		set(new String[] {
			"OID", "Datum", "typ", "userID", "station"
		}, new String[] {
			oid, new TimeTool().toString(TimeTool.DATE_GER), typ.name(), user, hostname
		});
	}
	
	public static DBLog load(String id){
		return new DBLog(id);
	}
	
	protected DBLog(String id){
		super(id);
	}
	
	protected DBLog(){}
	
	public Anwender getAnwender(){
		String aid = checkNull(get("userID"));
		Anwender an = Anwender.load(aid);
		return an;
	}
	
	public String getTimeStamp(){
		long up = getLastUpdate();
		TimeTool ts = new TimeTool(up);
		return ts.toString(TimeTool.FULL_GER);
	}
	
	public String getWorkstation(){
		return checkNull(get("station"));
	}
	
	public String getType(){
		return checkNull(get("typ"));
	}
	
	public PersistentObject getObject(){
		String oid = getObjectID();
		PersistentObject ret = CoreHub.poFactory.createFromString(oid);
		return ret;
	}
	
	public String getObjectID(){
		return checkNull(get("OID"));
	}
	
	@Override
	public String getLabel(){
		return "DB-Log";
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
}
