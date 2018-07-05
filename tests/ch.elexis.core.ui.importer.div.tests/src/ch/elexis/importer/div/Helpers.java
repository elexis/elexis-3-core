/*******************************************************************************
 * Copyright (c) 2015, Elexis und Niklaus Giger <niklaus.giger@member.fsf.org
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    N. Giger - initial implementation
 *
 *******************************************************************************/

package ch.elexis.importer.div;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;

import ch.elexis.core.data.util.PlatformHelper;
import ch.elexis.core.importer.div.importers.HL7Parser;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabOrder;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.rgw.tools.Result;

public class Helpers {
	
	/*
	 * For running the tests we have to copy the
	 * files under RSC to a work directory, as the the HL7 importer moves the
	 * incoming files to files containing a timestamp
	 *
	 * @author: Niklaus Giger
	 * @return: The path of the temp directory
	 */
	static Path copyRscToTempDirectory(){
		Path path = null;
		try {
			path = Files.createTempDirectory("HL7_Test");
			File src =
				new File(PlatformHelper.getBasePath("ch.elexis.core.ui.importer.div.tests"), "rsc");
			System.out.println("src: " + src.toString());
			FileUtils.copyDirectory(src, path.toFile());
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return path;
	}
	
	static void removeTempDirectory(Path path){
		try {
			FileUtils.deleteDirectory(path.toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void removeAllPatientsAndDependants(){
		Query<Patient> qr = new Query<Patient>(Patient.class);
		List<Patient> qrr = qr.execute();
		for (int j = 0; j < qrr.size(); j++) {
			qrr.get(j).delete(true);
		}
		
		qr = new Query<Patient>(Patient.class);
		qrr = qr.execute();
	}
	
	public static void removeAllLaboWerte(){
		Query<LabResult> qr = new Query<LabResult>(LabResult.class);
		List<LabResult> qrr = qr.execute();
		for (int j = 0; j < qrr.size(); j++) {
			qrr.get(j).removeFromDatabase();
		}
		Query<LabOrder> qro = new Query<LabOrder>(LabOrder.class);
		List<LabOrder> qrro = qro.execute();
		for (int j = 0; j < qrro.size(); j++) {
			qrro.get(j).removeFromDatabase();
		}
		Query<LabItem> qrli = new Query<LabItem>(LabItem.class);
		List<LabItem> qLi = qrli.execute();
		for (int j = 0; j < qLi.size(); j++) {
			qLi.get(j).removeFromDatabase();
		}
		PersistentObject.clearCache();
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void parseOneHL7file(HL7Parser hlp, File f, boolean deleteAll,
		boolean alsoFailing) throws IOException{
		String name = f.getAbsolutePath();
		if (f.canRead() && (name.toLowerCase().endsWith(".hl7"))) {
			if (f.getName().equalsIgnoreCase("01TEST5005.hl7")
				|| f.getName().equalsIgnoreCase("1_Kunde_20090612083757162_10009977_.HL7")) {
				if (!alsoFailing) {
					// System.out.println("Skipping " + name);
					return;
				}
			}
			// System.out.println("parseOneHL7file " + name + "  " + f.length() + " bytes ");
			Result<?> rs = hlp.importFile(f, f.getParentFile(), true);
			if (!rs.isOK()) {
				String info = "Datei " + name + " fehlgeschlagen";
				System.out.println(info);
				fail(info);
			} else {
				assertTrue(true); // To show that we were successfull
				// System.out.println("Datei " + name +
				// " erfolgreich verarbeitet");
				Query<LabResult> qr = new Query<LabResult>(LabResult.class);
				List<LabResult> qrr = qr.execute();
				if (qrr.size() <= 0)
					fail("NoLabResult");
				if (deleteAll) {
					removeAllPatientsAndDependants();
				}
			}
		} else {
			System.out.println("Skipping Datei " + name);
		}
	}
}
