package ch.elexis.data;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import ch.elexis.core.data.extension.ReferenceDataImporterExtensionPoint;
import ch.elexis.core.data.interfaces.AbstractReferenceDataImporter;

public class Test_ReferenceDataImporterExtensionPoint {
	
	@Test
	public void testGetImporters(){
		List<AbstractReferenceDataImporter> importers =
			ReferenceDataImporterExtensionPoint.getImporters();
		assertEquals(0, importers.size());
	}
}
