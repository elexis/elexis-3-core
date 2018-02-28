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
package ch.elexis.core.data.util;

import java.util.ArrayList;
import java.util.List;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IDataAccess;
import ch.elexis.core.text.model.Samdas;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;

@SuppressWarnings("deprecation")
public class AllDataAccessor implements IDataAccess {
	
	private Element[] elements = {
		new Element(IDataAccess.TYPE.STRING, "Konsultationen", //$NON-NLS-1$
			"[Alle:-:-:Konsultationen]", null, 0), //$NON-NLS-1$
		new Element(IDataAccess.TYPE.STRING, "Konsultationen mit Fall", //$NON-NLS-1$
			"[Alle:-:-:KonsultationenFall]", null, 0) //$NON-NLS-1$
		};
	
	ArrayList<Element> elementsList;
	
	public AllDataAccessor(){
		// initialize the list of defined elements
		elementsList = new ArrayList<Element>();
		for (int i = 0; i < elements.length; i++)
			elementsList.add(elements[i]);
	}
	
	@Override
	public String getName(){
		return "Spezial Alle";
	}
	
	@Override
	public String getDescription(){
		return "Alle Elemente eines Typs ausgeben.";
	}
	
	@Override
	public List<Element> getList(){
		return elementsList;
	}
	
	@Override
	public Result<Object> getObject(String descriptor, PersistentObject dependentObject,
		String dates, String[] params){
		Result<Object> ret = null;
		
		if (descriptor.equals("Konsultationen")) { //$NON-NLS-1$
			Patient patient = ElexisEventDispatcher.getSelectedPatient();
			if (patient != null)
				ret = new Result<Object>(getAllKonsultations(patient, false));
			else
				ret =
					new Result<Object>(Result.SEVERITY.ERROR, IDataAccess.OBJECT_NOT_FOUND,
						"Kein Patient selektiert.", //$NON-NLS-1$
						null, false);
		}
		else if (descriptor.equals("KonsultationenFall")) { //$NON-NLS-1$
			Patient patient = ElexisEventDispatcher.getSelectedPatient();
			if (patient != null)
				ret = new Result<Object>(getAllKonsultations(patient, true));
			else
				ret = new Result<Object>(Result.SEVERITY.ERROR, IDataAccess.OBJECT_NOT_FOUND,
					"Kein Patient selektiert.", //$NON-NLS-1$
					null, false);
		}
		return ret;
		
	}
	
	private Object getAllKonsultations(Patient patient, boolean withFall){
		StringBuilder sb = new StringBuilder();
		
		TimeTool date = new TimeTool();
		
		for (Fall fall : patient.getFaelle()) {
			for (Konsultation kons : fall.getBehandlungen(true)) {
				date.set(kons.getDatum());
				sb.append(date.toString(TimeTool.DATE_GER));
				
				if (withFall) {
					sb.append(" ");
					sb.append(kons.getMandant().getLabel(false));
					sb.append(" - ");
					sb.append(fall.getBezeichnung());
					sb.append(" ");
				}
				
				sb.append("\n"); //$NON-NLS-1$
				
				Samdas samdas = new Samdas(kons.getEintrag().getHead());
				sb.append(samdas.getRecordText());
				sb.append("\n"); //$NON-NLS-1$
			}
		}
		
		return sb.toString();
	}
}
