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
package ch.elexis.core.ui.scripting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import ch.elexis.core.data.interfaces.text.IOpaqueDocument;
import ch.elexis.core.data.services.GlobalServiceDescriptors;
import ch.elexis.core.data.services.IDocumentManager;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.views.IPatFilter;
import ch.elexis.core.ui.views.PatFilterImpl;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.data.Sticker;
import ch.rgw.io.FileTool;

public class DocumentExport {
	Sticker sticker = null;
	PatFilterImpl pf = new PatFilterImpl();
	
	public String doExport(String destination, String stickerName){
		if (stickerName != null) {
			List<Sticker> ls =
				new Query<Sticker>(Sticker.class, Sticker.NAME, stickerName).execute();
			if (ls != null && ls.size() > 0) {
				sticker = ls.get(0);
			} else {
				return "Sticker " + stickerName + " nicht gefunden.";
			}
		}
		IDocumentManager mgr =
			(IDocumentManager) Extensions.findBestService(
				GlobalServiceDescriptors.DOCUMENT_MANAGEMENT, null);
		if (mgr == null) {
			return "Keine Dokumente gefunden";
		}
		try {
			if (destination == null) {
				FileDialog fd = new FileDialog(UiDesk.getTopShell(), SWT.SAVE);
				fd.setFilterExtensions(new String[] {
					"*.csv"
				});
				fd.setFilterNames(new String[] {
					"Comma Separated Values (CVS)"
				});
				fd.setOverwrite(true);
				destination = fd.open();
			}
			if (destination != null) {
				File csv = new File(destination);
				File parent = csv.getParentFile();
				File dir = new File(parent, FileTool.getNakedFilename(destination));
				dir.mkdirs();
				
				CSVWriter writer = new CSVWriter(new FileWriter(csv));
				String[] header = new String[] {
					"Patient", "Name", "Kategorie", "Datum", "Stichwörter", "Pfad"
				};
				List<IOpaqueDocument> dox = mgr.listDocuments(null, null, null, null, null, null);
				writer.writeNext(header);
				for (IOpaqueDocument doc : dox) {
					Patient pat = doc.getPatient();
					if (pat != null) {
						if (sticker != null) {
							if (pf.accept(pat, sticker) != IPatFilter.ACCEPT) {
								continue;
							}
						}
						
						String subdirname = pat.get(Patient.FLD_PATID);
						if (subdirname != null) {
							File subdir = new File(dir, subdirname);
							subdir.mkdirs();
							String[] line = new String[header.length];
							line[0] = pat.getId();
							line[1] = doc.getTitle();
							line[2] = doc.getCategory();
							line[3] = doc.getCreationDate();
							line[4] = doc.getKeywords();
							String docfilename = doc.getGUID() + "." + doc.getMimeType();
							line[5] =
								dir.getName() + File.separator + subdir.getName() + File.separator
									+ docfilename;
							byte[] bin = doc.getContentsAsBytes();
							if (bin != null) {
								File f = new File(subdir, docfilename);
								FileOutputStream fos = new FileOutputStream(f);
								fos.write(bin);
								fos.close();
								writer.writeNext(line);
							}
						}
						
					}
				}
				return "Export ok";
			} else {
				return "Abgebrochen.";
				
			}
		} catch (Exception e) {
			ElexisStatus status =
				new ElexisStatus(ElexisStatus.ERROR, Hub.PLUGIN_ID, ElexisStatus.CODE_NONE,
					"Fehler beim Export: " + e.getMessage(), e);
			throw new ScriptingException(status);
		}
		
	}
}
