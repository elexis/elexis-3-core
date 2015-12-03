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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

import ch.elexis.core.data.util.PlatformHelper;

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
	static void removeTempDirectory(Path path) {
		try {
			FileUtils.deleteDirectory(path.toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
