package ch.elexis.importer.div;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ch.elexis.importer.div.Helpers.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.data.beans.ContactBean;
import ch.elexis.core.importer.div.importers.HL7Parser;
import ch.elexis.core.importer.div.importers.OverwriteAllImportHandler;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.ui.importer.div.importers.DefaultLabContactResolver;
import ch.elexis.core.ui.importer.div.importers.ImporterPatientResolver;
import ch.elexis.core.ui.importer.div.importers.LabImportUtil;
import ch.elexis.data.LabItem;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.rgw.tools.Result;

public class HL7InitLabItemTest {
	private static final String MY_TESTLAB = "myTestLab";
	private static Path workDir = null;
	
	private HL7Parser hl7Parser;
	
	@Before
	public void setup() throws Exception{
		removeExistingItems();
		
		workDir = Helpers.copyRscToTempDirectory();
		
		hl7Parser = new HL7Parser(MY_TESTLAB, new MaleFemalePatientResolver(), new LabImportUtil(),
			new OverwriteAllImportHandler(), new DefaultLabContactResolver(), false);
	}
	
	@After
	public void tearDown() throws Exception{
		removeAllPatientsAndDependants();
		if (workDir != null) {
			Helpers.removeTempDirectory(workDir);
		}
	}
	
	@Test
	public void testImportRefValues() throws IOException{
		File hl7File = new File(workDir.toString(), "Viollier/Viollier_1.HL7");
		Result<?> result = hl7Parser.importFile(hl7File, null, true);
		if (result.isOK()) {
			assertTrue(true); // show import was successful
		} else {
			fail();
		}
		result = hl7Parser.importFile(hl7File, null, true);
		if (result.isOK()) {
			assertTrue(true); // show import was successful
		} else {
			fail();
		}
		Query<LabItem> itemQuery = new Query<>(LabItem.class);
		itemQuery.add(LabItem.TITLE, Query.EQUALS, "Calcium");
		List<LabItem> items = itemQuery.execute();
		assertEquals(1, items.size());
		LabItem item = items.get(0);
		assertEquals("mmol/L", item.getEinheit());
		assertEquals("2.20 - 2.65", item.getReferenceFemale());
		assertEquals("2.20 - 2.65", item.getReferenceMale());
	}
	
	static private void removeExistingItems(){
		Query<LabItem> qr = new Query<>(LabItem.class);
		List<LabItem> qrr = qr.execute();
		for (int j = 0; j < qrr.size(); j++) {
			qrr.get(j).delete();
		}
	}
	
	private class MaleFemalePatientResolver extends ImporterPatientResolver {
		
		private Patient female;
		private Patient male;
		
		private Patient last;
		
		public MaleFemalePatientResolver(){
			female = new Patient("Test", "Female", "01.01.1999", Patient.FEMALE);
			male = new Patient("Test", "Male", "01.01.1999", Patient.MALE);
		}
		
		@Override
		public IPatient resolvePatient(String firstname, String lastname, String birthDate){
			if (last == null || last == male) {
				last = female;
				return new ContactBean(female);
			} else {
				last = male;
				return new ContactBean(male);
			}
		}
		
		@Override
		public boolean matchPatient(IPatient patient, String firstname, String lastname,
			String birthDate){
			return true;
		}
		
		@Override
		public List<? extends IPatient> getPatientById(String patid){
			return Collections.singletonList(resolvePatient("", "", ""));
		}
	}
}
