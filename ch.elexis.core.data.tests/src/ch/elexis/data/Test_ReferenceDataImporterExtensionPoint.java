package ch.elexis.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.junit.Test;

import ch.elexis.core.data.extension.ReferenceDataImporterExtensionPoint;
import ch.elexis.core.data.interfaces.AbstractReferenceDataImporter;

public class Test_ReferenceDataImporterExtensionPoint {
	private static final String TARMED_ID = "tarmed";
	
	@Test
	public void testGetImporters(){
		List<AbstractReferenceDataImporter> importers =
			ReferenceDataImporterExtensionPoint.getImporters();
		assertNotSame(0, importers.size());
		
		boolean hasTarmedImporter = false;
		for (AbstractReferenceDataImporter importer : importers) {
			if (TARMED_ID.equals(importer.getReferenceDataIdResponsibleFor())) {
				hasTarmedImporter = true;
			}
		}
		assertTrue(hasTarmedImporter);
	}
	
	@Test
	public void testUpdate() throws FileNotFoundException{
		File tarmedFile =
			new File(System.getProperty("user.dir") + File.separator + "rsc" + File.separator
				+ "tarmed.mdb");
		InputStream tarmedInStream = new FileInputStream(tarmedFile);
		
		IStatus retStatus =
			ReferenceDataImporterExtensionPoint.update(TARMED_ID, null, tarmedInStream);
		
		assertEquals(IStatus.OK, retStatus.getCode());
	}
	
}
