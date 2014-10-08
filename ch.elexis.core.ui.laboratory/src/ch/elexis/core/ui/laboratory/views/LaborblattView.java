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

package ch.elexis.core.ui.laboratory.views;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;
import org.jdom.Document;
import org.jdom.Element;

import ch.elexis.core.ui.text.ITextPlugin;
import ch.elexis.core.ui.text.ITextPlugin.ICallback;
import ch.elexis.core.ui.text.TextContainer;
import ch.elexis.data.Brief;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;

public class LaborblattView extends ViewPart implements ICallback {
	public static final String ID = "ch.elexis.Laborblatt"; //$NON-NLS-1$
	TextContainer text;
	
	public LaborblattView(){}
	
	@Override
	public void createPartControl(Composite parent){
		text = new TextContainer(getViewSite());
		text.getPlugin().createContainer(parent, this);
		
	}
	
	@Override
	public void setFocus(){
		// TODO Automatisch erstellter Methoden-Stub
		
	}
	
	public boolean createLaborblatt(final Patient pat, final String[] header, final TreeItem[] rows){
		return createLaborblatt(pat, header, rows, null);
	}
	
	public boolean createLaborblatt(final Patient pat, final String[] header,
		final TreeItem[] rows, int[] skipColumnsIndex){
		Brief br =
			text.createFromTemplateName(Konsultation.getAktuelleKons(),
				Messages.LaborblattView_LabTemplateName, Brief.LABOR, pat, null);
		if (br == null) {
			return false;
		}
		Tree tree = rows[0].getParent();
		int cols = tree.getColumnCount() - skipColumnsIndex.length;
		int[] colsizes = new int[cols];
		float first = 25;
		float second = 10;
		if (cols > 2) {
			int rest = Math.round((100f - first - second) / (cols - 2f));
			for (int i = 2; i < cols; i++) {
				colsizes[i] = rest;
			}
		}
		colsizes[0] = Math.round(first);
		colsizes[1] = Math.round(second);
		
		LinkedList<String[]> usedRows = new LinkedList<String[]>();
		usedRows.add(header);
		for (int i = 0; i < rows.length; i++) {
			boolean used = false;
			String[] row = new String[cols];
			for (int j = 0, skipped = 0; j < cols + skipped; j++) {
				if (skipColumn(j, skipColumnsIndex)) {
					skipped++;
					continue;
				}
				int destIndex = j - skipped;
				row[destIndex] = rows[i].getText(j);
				if ((destIndex > 1) && (row[destIndex].length() > 0)) {
					used = true;
					// break;
				}
			}
			if (used == true) {
				usedRows.add(row);
			}
		}
		String[][] fld = usedRows.toArray(new String[0][]);
		boolean ret = text.getPlugin().insertTable("[Laborwerte]", //$NON-NLS-1$
			ITextPlugin.FIRST_ROW_IS_HEADER, fld, colsizes);
		text.saveBrief(br, Brief.LABOR);
		return ret;
	}
	
	private boolean skipColumn(int index, int[] skip){
		for (int i : skip) {
			if (index == i) {
				return true;
			}
		}
		return false;
	}

	public boolean createLaborblatt(final Patient pat, final String[] header, final TableItem[] rows){
		Brief br =
			text.createFromTemplateName(Konsultation.getAktuelleKons(),
				Messages.LaborblattView_LabTemplateName, Brief.LABOR, pat, null);
		if (br == null) {
			return false;
		}
		Table table = rows[0].getParent();
		int cols = table.getColumnCount();
		int[] colsizes = new int[cols];
		float first = 25;
		float second = 10;
		if (cols > 2) {
			int rest = Math.round((100f - first - second) / (cols - 2f));
			for (int i = 2; i < cols; i++) {
				colsizes[i] = rest;
			}
		}
		colsizes[0] = Math.round(first);
		colsizes[1] = Math.round(second);
		
		LinkedList<String[]> usedRows = new LinkedList<String[]>();
		usedRows.add(header);
		for (int i = 0; i < rows.length; i++) {
			boolean used = false;
			String[] row = new String[cols];
			for (int j = 0; j < cols; j++) {
				row[j] = rows[i].getText(j);
				if ((j > 1) && (row[j].length() > 0)) {
					used = true;
					// break;
				}
			}
			if (used == true) {
				usedRows.add(row);
			}
		}
		String[][] fld = usedRows.toArray(new String[0][]);
		boolean ret = text.getPlugin().insertTable("[Laborwerte]", //$NON-NLS-1$
			ITextPlugin.FIRST_ROW_IS_HEADER, fld, colsizes);
		text.saveBrief(br, Brief.LABOR);
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public boolean createLaborblatt(Patient pat, Document doc){
		/* Brief br= */text.createFromTemplateName(Konsultation.getAktuelleKons(),
			Messages.LaborblattView_LabTemplateName, Brief.LABOR, pat, null);
		
		ArrayList<String[]> rows = new ArrayList<String[]>();
		Element root = doc.getRootElement();
		String druckdat = root.getAttributeValue(Messages.LaborblattView_created);
		Element daten = root.getChild("Daten"); //$NON-NLS-1$
		List datlist = daten.getChildren();
		int cols = datlist.size() + 1;
		String[] firstline = new String[cols];
		firstline[0] = druckdat;
		for (int i = 1; i < cols; i++) {
			Element dat = (Element) datlist.get(i - 1);
			firstline[i] = dat.getAttributeValue("Tag"); //$NON-NLS-1$
		}
		rows.add(firstline);
		List groups = root.getChildren("Gruppe"); //$NON-NLS-1$
		for (Element el : (List<Element>) groups) {
			rows.add(new String[] {
				el.getAttribute("Name").getValue()}); //$NON-NLS-1$
			List<Element> params = el.getChildren("Parameter"); //$NON-NLS-1$
			for (Element param : params) {
				Element ref = param.getChild("Referenz"); //$NON-NLS-1$
				String[] row = new String[cols];
				StringBuilder sb = new StringBuilder();
				sb.append(param.getAttributeValue("Name")).append(" (").append( //$NON-NLS-1$ //$NON-NLS-2$
					ref.getAttributeValue("min")).append("-").append( //$NON-NLS-1$ //$NON-NLS-2$
					ref.getAttributeValue("max")).append(") ").append( //$NON-NLS-1$ //$NON-NLS-2$
					param.getAttributeValue("Einheit")); //$NON-NLS-1$
				row[0] = sb.toString();
				List<Element> results = param.getChildren("Resultat"); //$NON-NLS-1$
				int i = 1;
				for (Element result : results) {
					row[i++] = result.getValue();
				}
				rows.add(row);
			}
		}
		if (text.getPlugin().insertTable("[Laborwerte]", //$NON-NLS-1$
			ITextPlugin.FIRST_ROW_IS_HEADER, rows.toArray(new String[0][]), null)) {
			if (text.getPlugin().isDirectOutput()) {
				text.getPlugin().print(null, null, true);
				getSite().getPage().hideView(this);
				return true;
			}
		}
		return false;
		
	}
	
	@Override
	public void save(){
		// TODO Automatisch erstellter Methoden-Stub
		
	}
	
	@Override
	public boolean saveAs(){
		// TODO Automatisch erstellter Methoden-Stub
		return false;
	}
}
