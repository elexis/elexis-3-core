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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.views.IPatFilter;
import ch.elexis.core.ui.views.PatFilterImpl;
import ch.elexis.data.Brief;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.elexis.data.Sticker;
import ch.rgw.io.FileTool;

public class BriefExport {
	Sticker sticker = null;
	PatFilterImpl pf = new PatFilterImpl();
	
	public String doExport(String filename, String stickerName){
		if (stickerName != null) {
			List<Sticker> ls =
				new Query<Sticker>(Sticker.class, Sticker.FLD_NAME, stickerName).execute();
			if (ls != null && ls.size() > 0) {
				sticker = ls.get(0);
			} else {
				return "Sticker " + stickerName + " nicht gefunden.";
			}
		}
		if (filename == null) {
			FileDialog fd = new FileDialog(UiDesk.getTopShell(), SWT.SAVE);
			fd.setFilterExtensions(new String[] {
				"*.csv"
			});
			fd.setFilterNames(new String[] {
				"Comma Separated Values (CVS)"
			});
			fd.setOverwrite(true);
			filename = fd.open();
		}
		if (filename != null) {
			List<Brief> briefe = new Query<Brief>(Brief.class).execute();
			File csv = new File(filename);
			File parent = csv.getParentFile();
			File dir = new File(parent, FileTool.getNakedFilename(filename));
			dir.mkdirs();
			try {
				CSVWriter writer = new CSVWriter(new FileWriter(csv));
				String[] header = new String[] {
					"Betreff", "Datum", "Adressat", "Mimetype", "Typ", "Patient", "Pfad"
				};
				String[] fields =
					new String[] {
						Brief.FLD_SUBJECT, Brief.FLD_DATE, Brief.FLD_DESTINATION_ID,
						Brief.FLD_MIME_TYPE, Brief.FLD_TYPE, Brief.FLD_PATIENT_ID,
						Brief.FLD_PATIENT_ID
					};
				writer.writeNext(header);
				for (Brief brief : briefe) {
					Person pers = brief.getPatient();
					if (pers != null) {
						if (!pers.istPatient()) {
							continue;
						}
						if (sticker != null) {
							if (pf.accept(Patient.load(pers.getId()), sticker) != IPatFilter.ACCEPT) {
								continue;
							}
						}
						String subdirname = pers.get(Patient.FLD_PATID);
						if (subdirname != null) {
							File subdir = new File(dir, subdirname);
							subdir.mkdirs();
							String[] line = new String[fields.length];
							brief.get(fields, line);
							byte[] bin = brief.loadBinary();
							if (bin != null) {
								File f = new File(subdir, brief.getId() + ".odt");
								FileOutputStream fos = new FileOutputStream(f);
								fos.write(bin);
								fos.close();
								line[line.length - 1] =
									dir.getName() + File.separator + subdir.getName()
										+ File.separator + f.getName();
								writer.writeNext(line);
							}
						}
					}
				}
				writer.close();
				return "Export ok";
			} catch (Exception ex) {
				ElexisStatus status =
					new ElexisStatus(ElexisStatus.ERROR, Hub.PLUGIN_ID, ElexisStatus.CODE_NONE,
						"Fehler beim Export: " + ex.getMessage(), ex);
				throw new ScriptingException(status);
			}
		}
		return "Abgebrochen";
	}
}
