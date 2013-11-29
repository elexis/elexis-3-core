/*******************************************************************************
 * Copyright (c) 2010, G. Weirich and Elexis
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

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IOutputter;
import ch.elexis.core.data.util.Extensions;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;

/**
 * An OutputLog instance carries the information when and where to a PersistentObject has been sent.
 * 
 * @author gerry
 * 
 */
public class OutputLog extends PersistentObject {
	public static final String FLD_OBJECT_TYPE = "ObjectType";
	public static final String FLD_OBJECT_ID = "ObjectID";
	public static final String FLD_OUTPUTTER = "Outputter";
	static final String TABLENAME = "OUTPUT_LOG";
	static final HashMap<String, IOutputter> outputter_cache = new HashMap<String, IOutputter>();
	
	static {
		addMapping(TABLENAME, FLD_OBJECT_ID, FLD_OBJECT_TYPE, FLD_OUTPUTTER, DATE_COMPOUND,
			FLD_EXTINFO);
	}
	
	public OutputLog(PersistentObject po, IOutputter io){
		create(null);
		set(new String[] {
			FLD_OBJECT_ID, FLD_OBJECT_TYPE, FLD_DATE, FLD_OUTPUTTER
		}, po.getId(), po.getClass().getName(), new TimeTool().toString(TimeTool.DATE_GER),
			io.getOutputterID());
		ElexisEventDispatcher.getInstance().fire(
			new ElexisEvent(po, po.getClass(), ElexisEvent.EVENT_UPDATE));
	}
	
	@Override
	public String getLabel(){
		return get(FLD_DATE) + ":" + get(FLD_OUTPUTTER);
	}
	
	public String getOutputterID(){
		return checkNull(get(FLD_OUTPUTTER));
	}
	
	public static List<OutputLog> getOutputs(PersistentObject po){
		Query<OutputLog> qbe = new Query<OutputLog>(OutputLog.class);
		qbe.add(FLD_OBJECT_ID, Query.EQUALS, po.getId());
		qbe.orderBy(true, FLD_LASTUPDATE);
		return qbe.execute();
	}
	
	public static IOutputter getOutputter(String outputterID){
		IOutputter ret = outputter_cache.get(outputterID);
		if (ret == null) {
			List<IConfigurationElement> eps = Extensions.getExtensions("ch.elexis.Transporter");
			for (IConfigurationElement ep : eps) {
				String id = ep.getAttribute("id");
				if (id != null && id.equals(outputterID)) {
					try {
						ret = (IOutputter) ep.createExecutableExtension("Outputter");
						outputter_cache.put(outputterID, ret);
						break;
					} catch (CoreException ex) {
						ExHandler.handle(ex);
					}
				}
			}
		}
		return ret;
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	public static OutputLog load(String id){
		return new OutputLog(id);
	}
	
	protected OutputLog(String id){
		super(id);
	}
	
	OutputLog(){}
}
