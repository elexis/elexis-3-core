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
// $Id: DayMessage.java 2767 2007-07-09 10:51:59Z rgw_ch $
/*
 * Created on 05.08.2005
 */
package ch.elexis.data;

import ch.rgw.tools.TimeTool;

public class DayMessage extends PersistentObject {
	public boolean isNew = false;
	static {
		addMapping("AGNDAYS", "message", "infos");
	}
	
	public DayMessage(TimeTool date, String message, String infos){
		create(date.toString(TimeTool.DATE_COMPACT));
		setMessages(message, infos);
	}
	
	public void setMessages(String message, String info){
		set(new String[] {
			"message", "infos"
		}, new String[] {
			message, info
		});
	}
	
	public String getMessage(){
		return get("message");
	}
	
	public String getInfos(){
		return get("infos");
	}
	
	public String getLabel(){
		return get("Date") + " " + getMessage();
	}
	
	public static DayMessage load(String day){
		DayMessage ret = new DayMessage(day);
		if (ret.state() == DELETED) {
			ret.undelete();
		} else if (ret.state() < DELETED) {
			ret.create(day);
			ret.isNew = true;
		}
		return ret;
	}
	
	@Override
	protected String getTableName(){
		return "AGNDAYS";
	}
	
	DayMessage(){/* leer */}
	
	DayMessage(String id){
		super(id);
	}
	
}
