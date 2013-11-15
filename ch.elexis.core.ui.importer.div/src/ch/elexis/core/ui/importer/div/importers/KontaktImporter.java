/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.importer.div.importers;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.data.Kontakt;
import ch.elexis.core.data.Query;
import ch.elexis.core.ui.util.ImporterPage;
import ch.elexis.core.ui.util.SWTHelper;

public class KontaktImporter extends ImporterPage {
	KontaktImporterBlatt importer;
	
	public KontaktImporter(){
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Composite createPage(final Composite parent){
		importer = new KontaktImporterBlatt(parent);
		importer.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		return importer;
	}
	
	@Override
	public IStatus doImport(final IProgressMonitor monitor) throws Exception{
		if (importer.doImport(monitor)) {
			return Status.OK_STATUS;
		}
		return new Status(Status.ERROR,
			"ch.elexis.import.div", 1, Messages.KontaktImporter_ErrorImport, null); //$NON-NLS-1$
	}
	
	@Override
	public String getDescription(){
		return Messages.KontaktImporter_ExplanationImport;
	}
	
	@Override
	public String getTitle(){
		return Messages.KontaktImporter_Title;
	}
	
	static Kontakt queryKontakt(final String name, final String vorname, final String strasse,
		final String plz, final String ort, final boolean createIfMissing){
		Query<Kontakt> qbe = new Query<Kontakt>(Kontakt.class);
		List<Kontakt> res = qbe.queryFields(new String[] {
			"Bezeichnung1", "Bezeichnung2", "Strasse", "Plz", "Ort"}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			new String[] {
				name, vorname, strasse, plz, ort
			}, false);
		if ((res != null) && (res.size() > 0)) {
			Kontakt found = res.get(0);
			StringBuilder s1 = new StringBuilder();
			StringBuilder s2 = new StringBuilder();
			s1.append(found.get("Bezeichnung1")).append(", ").append(found.get("Bezeichnung2")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				.append(" - ").append(found.get("Strasse")).append(" ") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				.append(found.get("Plz")).append(" ").append(found.get("Ort")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			
			s2.append(name).append(", ").append(vorname).append(" - ") //$NON-NLS-1$ //$NON-NLS-2$
				.append(strasse).append(" ").append(plz).append(" ").append(ort); //$NON-NLS-1$ //$NON-NLS-2$
			
			if (SWTHelper.askYesNo(Messages.KontaktImporter_AskSameTitle,
				Messages.KontaktImporter_AskSameText1 + s1.toString()
					+ Messages.KontaktImporter_AskSameAnd + s1.toString()
					+ Messages.KontaktImporter_AskSameText2)) {
				return found;
			}
		}
		return null;
	}
}
