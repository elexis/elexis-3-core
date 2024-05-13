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

import static ch.elexis.core.ui.laboratory.LaboratoryTextTemplateRequirement.TT_LABPAPER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;
import org.jdom2.Document;
import org.jdom2.Element;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.text.ITextPlugin;
import ch.elexis.core.ui.text.ITextPlugin.ICallback;
import ch.elexis.core.ui.text.TextContainer;
import ch.elexis.data.Brief;
import ch.elexis.data.Patient;

public class LaborblattView extends ViewPart implements ICallback {
	public static final String ID = "ch.elexis.Laborblatt"; //$NON-NLS-1$
	TextContainer text;

	public LaborblattView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		text = new TextContainer(getViewSite());
		text.getPlugin().createContainer(parent, this);

	}

	@Override
	public void setFocus() {
		// TODO Automatisch erstellter Methoden-Stub

	}

	public boolean createLaborblatt(final Patient pat, final String[] header, final TreeItem[] rows) {
		return createLaborblatt(pat, header, rows, null);
	}

	public boolean createLaborblatt(final Patient pat, final String[] header, final TreeItem[] rows,
			int[] skipColumnsIndex) {
		Brief br = text.createFromTemplateName(text.getAktuelleKons(), TT_LABPAPER, Brief.LABOR, pat, null);
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

		LinkedList<String[]> usedRows = new LinkedList<>();
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

		// Inspect and modify the lines to mark the pathologic values
		fld = inspectValues(fld);

		boolean ret = text.getPlugin().insertTable("[Laborwerte]", //$NON-NLS-1$
				ITextPlugin.FIRST_ROW_IS_HEADER, fld, colsizes);
		text.saveBrief(br, Brief.LABOR);
		return ret;
	}

	private String[][] inspectValues(String[][] values) {
		// Iterate over all values
		for (int rowCounter = 0; rowCounter < values.length; rowCounter++) {
			// Skip first line as it contains header information
			if (rowCounter > 0) {
				String reference = new String();
				// Loop through all elements of current row
				for (int columnCounter = 0; columnCounter < values[rowCounter].length; columnCounter++) {
					String value = new String();
					// The first column contains the reference data
					if (columnCounter == 1) {
						if (!values[rowCounter][columnCounter].isEmpty()) {
							reference = values[rowCounter][columnCounter];
						}
					}
					// The subsequent columns contain measuring data
					if (columnCounter > 1 && !values[rowCounter][1].isEmpty()) {
						if (!values[rowCounter][columnCounter].isEmpty()) {
							value = values[rowCounter][columnCounter];
							// Specific measuring data being tested
							values[rowCounter][columnCounter] = modifyPathologicValues(reference, value);
						}
					}
				}
			}
		}
		return values;
	}

	private String modifyPathologicValues(String reference, String value) {
		// Regex to identify floats
		Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
		boolean pathologic = false;
		String modifiedValue = new String();
		// Processing only floats
		if (!reference.isEmpty() && pattern.matcher(value).matches()) {
			Float valueFloat = Float.parseFloat(value);
			// Inspecting values against upper and lower limits
			if (reference.contains("-")) {
				List<String> myList = new ArrayList<String>(Arrays.asList(reference.split("-")));
				String lowerStr = myList.get(0);
				String upperStr = myList.get(1);
				Float lower = Float.parseFloat(lowerStr);
				Float upper = Float.parseFloat(upperStr);
				pathologic = (valueFloat > upper || valueFloat < lower) ? true : false;
				// Inspecting vallues against upper limit
			} else if (reference.contains("<")) {
				String upperLimit = reference.substring(1);
				Float upper = Float.parseFloat(upperLimit);
				pathologic = valueFloat > upper ? true : false;
				// Inspecting value against lower limit
			} else if (reference.contains(">")) {
				String lowerLimit = reference.substring(1);
				Float lower = Float.parseFloat(lowerLimit);
				pathologic = valueFloat < lower ? true : false;
			}
		}
		// Add ** if value is pathologic
		modifiedValue = pathologic ? "**" + value : value;
		return modifiedValue;
	}

	private boolean skipColumn(int index, int[] skip) {
		for (int i : skip) {
			if (index == i) {
				return true;
			}
		}
		return false;
	}

	public boolean createLaborblatt(final Patient pat, final String[] header, final TableItem[] rows) {
		Brief br = text.createFromTemplateName(text.getAktuelleKons(), TT_LABPAPER, Brief.LABOR, pat, null);
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

		LinkedList<String[]> usedRows = new LinkedList<>();
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
	public boolean createLaborblatt(Patient pat, Document doc) {
		/* Brief br= */text.createFromTemplateName(text.getAktuelleKons(), TT_LABPAPER, Brief.LABOR, pat, null);

		ArrayList<String[]> rows = new ArrayList<>();
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
			rows.add(new String[] { el.getAttribute("Name").getValue() }); //$NON-NLS-1$
			List<Element> params = el.getChildren("Parameter"); //$NON-NLS-1$
			for (Element param : params) {
				Element ref = param.getChild("Referenz"); //$NON-NLS-1$
				String[] row = new String[cols];
				StringBuilder sb = new StringBuilder();
				sb.append(param.getAttributeValue("Name")).append(" (").append( //$NON-NLS-1$ //$NON-NLS-2$
						ref.getAttributeValue("min")).append("-").append( //$NON-NLS-1$ //$NON-NLS-2$
								ref.getAttributeValue("max")) //$NON-NLS-1$
						.append(") ").append( //$NON-NLS-1$
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
	public void save() {
		// TODO Automatisch erstellter Methoden-Stub

	}

	@Override
	public boolean saveAs() {
		// TODO Automatisch erstellter Methoden-Stub
		return false;
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}
}
