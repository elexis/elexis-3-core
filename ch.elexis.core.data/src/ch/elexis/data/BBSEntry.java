/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
// $Id: BBSEntry.java 4828 2008-12-17 16:43:33Z rgw_ch $
/*******************************************************************************
 * Copyright (c) 2005, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *******************************************************************************/

package ch.elexis.data;

import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Ein Eintrag fürs Schwarze Brett. Einträge sind hierarchisch organisiert
 * 
 * @author gerry
 * 
 */
public class BBSEntry extends PersistentObject {
	
	static {
		addMapping("BBS", "reference", "Thema=topic", "datum=S:D:date", "time", "authorID",
			"text=message");
		
	}
	
	public BBSEntry(String topic, Anwender author, BBSEntry ref, String text){
		create(null);
		String refid = ref == null ? "NIL" : ref.getId();
		TimeTool tt = new TimeTool();
		set(new String[] {
			"reference", "Thema", "authorID", "datum", "time", "text"
		}, refid, topic, author.getId(), tt.toString(TimeTool.DATE_GER),
			tt.toString(TimeTool.TIME_COMPACT), text);
	}
	
	public Anwender getAuthor(){
		return Anwender.load(get("authorID"));
	}
	
	public BBSEntry getReference(){
		return BBSEntry.load(get("reference"));
	}
	
	public String getTopic(){
		return get("Thema");
	}
	
	public String getText(){
		return get("text");
	}
	
	@Override
	public String getLabel(){
		StringBuilder ret = new StringBuilder();
		ret.append(getDate()).append(",").append(getTime()).append(": ").append(get("Thema"))
			.append(" (").append(getAuthor().getLabel()).append(")");
		
		return ret.toString();
	}
	
	@Override
	protected String getTableName(){
		return "BBS";
	}
	
	protected BBSEntry(){}
	
	protected BBSEntry(String id){
		super(id);
	}
	
	public static BBSEntry load(String id){
		return new BBSEntry(id);
	}
	
	public String getDate(){
		return get("datum");
	}
	
	public String getTime(){
		String t = get("time");
		if (StringTool.isNothing((t))) {
			return "00:00";
		} else {
			return t.substring(0, 2) + ":" + t.substring(2);
		}
	}
}
