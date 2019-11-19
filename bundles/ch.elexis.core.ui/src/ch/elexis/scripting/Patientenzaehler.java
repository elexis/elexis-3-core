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
package ch.elexis.scripting;

import java.util.HashMap;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.rgw.tools.TimeTool;

import com.tiff.common.ui.datepicker.DatePicker;

import ch.rgw.tools.StringTool;
public class Patientenzaehler extends TitleAreaDialog {
	DatePicker dpVon, dpBis;
	public int kons, cases, men, women;
	
	public String getResult(){
		StringBuilder sb = new StringBuilder();
		sb.append("Mandant ").append(CoreHub.actMandant.getLabel()).append(":\n").append("Total ")
			.append(men + women).append(" Patienten; ").append(women).append(" Frauen und ")
			.append(men).append(" Männer.\n").append("in ").append(kons)
			.append(" Konsultationen zu ").append(cases).append(" Fällen.");
		return sb.toString();
	}
	
	public Patientenzaehler(){
		super(UiDesk.getTopShell());
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = (Composite) super.createDialogArea(parent);
		Composite inner = new Composite(ret, SWT.NONE);
		inner.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		inner.setLayout(new GridLayout(2, true));
		new Label(inner, SWT.NONE).setText("Startdatum");
		new Label(inner, SWT.NONE).setText("Enddatum");
		dpVon = new DatePicker(inner, SWT.NONE);
		dpBis = new DatePicker(inner, SWT.NONE);
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		setTitle("Patientenzähler");
		setMessage("Bitte start- und enddatum (inklusive) angeben");
	}
	
	@Override
	protected void okPressed(){
		TimeTool ttVon = new TimeTool(dpVon.getDate().getTime());
		TimeTool ttBis = new TimeTool(dpBis.getDate().getTime());
		Query<Konsultation> qbe = new Query<Konsultation>(Konsultation.class);
		qbe.add("Datum", ">=", ttVon.toString(TimeTool.DATE_COMPACT));
		qbe.add("Datum", "<=", ttBis.toString(TimeTool.DATE_COMPACT));
		qbe.add("MandantID", StringTool.equals, CoreHub.actMandant.getId());
		HashMap<String, Patient> maenner = new HashMap<String, Patient>();
		HashMap<String, Patient> frauen = new HashMap<String, Patient>();
		HashMap<String, Fall> faelle = new HashMap<String, Fall>();
		
		for (Konsultation k : qbe.execute()) {
			Fall fall = k.getFall();
			faelle.put(fall.getId(), fall);
			Patient p = fall.getPatient();
			if (p.getGeschlecht().equals(Person.MALE)) {
				maenner.put(p.getId(), p);
			} else {
				frauen.put(p.getId(), p);
			}
			kons++;
		}
		men = maenner.size();
		women = frauen.size();
		cases = faelle.size();
		super.okPressed();
	}
	
}
