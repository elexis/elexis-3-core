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

package ch.elexis.core.data.util;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.rgw.tools.ExHandler;

/**
 * Die Datenbank aufräumen, ungültige Datensätze finden, ggf. Indices aufbauen
 * 
 * @author gerry
 * 
 */
import ch.rgw.tools.StringTool;
public class DatabaseCleaner {
	
	OutputStream osw;
	ArrayList<PersistentObject> purgeList = new ArrayList<PersistentObject>(200);
	boolean purge;
	
	/**
	 * Neuen DatabaseCleaner erstellen
	 * 
	 * @param os
	 *            Outputstream, in den die Reports geschrieben werden
	 * @param withPurge
	 *            true, wenn fehlerhafte Einträge gelöscht werden sollen
	 */
	public DatabaseCleaner(OutputStream os, boolean withPurge){
		osw = os;
		purge = withPurge;
	}
	
	public void checkAll(){
		checkKonsultationen();
		checkRechnungen();
	}
	
	public void checkKonsultationen(){
		Query<Konsultation> qbe = new Query<Konsultation>(Konsultation.class);
		List<Konsultation> list = qbe.execute();
		
		for (Konsultation k : list) {
			Fall fall = k.getFall();
			if (fall == null) {
				blame(k, Messages.DatabaseCleaner_NoCaseForKons); //$NON-NLS-1$
				continue;
			}
			Mandant m = k.getMandant();
			if (m == null) {
				blame(k, Messages.DatabaseCleaner_NoMandatorForKons); //$NON-NLS-1$
				continue;
			}
		}
		
	}
	
	public void checkRechnungen(){
		Query<Rechnung> qbe = new Query<Rechnung>(Rechnung.class);
		List<Rechnung> list =
			(List<Rechnung>) qbe.queryExpression(
				"SELECT ID FROM RECHNUNGEN WHERE FallID is null", new LinkedList<Rechnung>()); //$NON-NLS-1$
		for (Rechnung rn : list) {
			if (true) {
				blame(rn, Messages.DatabaseCleaner_NoCaseForBill); //$NON-NLS-1$
				Query<Konsultation> qk = new Query<Konsultation>(Konsultation.class);
				qk.add("RechnungsID", StringTool.equals, rn.getId());
				List<Konsultation> lk = qk.execute();
				for (Konsultation k : lk) {
					Fall f = k.getFall();
					Patient pat = f.getPatient();
					note(Messages.DatabaseCleaner_concerning + pat.getLabel()
						+ ", " + f.getLabel() + ", " + k.getLabel()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				if (purge) {
					PersistentObject
						.getConnection()
						.exec(
							"UPDATE BEHANDLUNGEN SET RECHNUNGSID=NULL WHERE RECHNUNGSID=" + rn.getWrappedId()); //$NON-NLS-1$
				}
			}
			
		}
	}
	
	void blame(PersistentObject o, String msg){
		try {
			osw.write((StringTool.crlf + msg + ": " + o.getId() + ", " + o.getLabel() + StringTool.crlf).getBytes("iso-8859-1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			purgeList.add(o);
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
	}
	
	void note(String msg){
		try {
			osw.write(("  -- " + msg + StringTool.crlf).getBytes("iso-8859-1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
	}
	
	void doPurge(){
		if (purge) {
			for (PersistentObject o : purgeList) {
				o.delete();
			}
		}
		
	}
}
