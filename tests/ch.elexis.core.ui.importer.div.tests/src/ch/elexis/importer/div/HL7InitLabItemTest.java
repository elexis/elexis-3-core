package ch.elexis.importer.div;

import static ch.elexis.importer.div.Helpers.removeAllPatientsAndDependants;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.importer.div.importers.HL7Parser;
import ch.elexis.core.importer.div.importers.OverwriteAllImportHandler;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.types.Gender;
import ch.elexis.core.ui.importer.div.importers.DefaultLabContactResolver;
import ch.elexis.core.ui.importer.div.importers.ImporterPatientResolver;
import ch.elexis.core.ui.importer.div.services.LabImportUtilHolder;
import ch.rgw.tools.Result;

public class HL7InitLabItemTest {
	private static final String MY_TESTLAB = "myTestLab";
	private static Path workDir = null;
	
	private HL7Parser hl7Parser;
	
	@Before
	public void setup() throws Exception{
		removeExistingItems();
		
		workDir = Helpers.copyRscToTempDirectory();
		
		hl7Parser =
			new HL7Parser(MY_TESTLAB, new MaleFemalePatientResolver(), LabImportUtilHolder.get(),
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
		IQuery<ILabItem> labItemQuery = CoreModelServiceHolder.get().getQuery(ILabItem.class);
		labItemQuery.and(ModelPackage.Literals.ILAB_ITEM__NAME, COMPARATOR.EQUALS, "Calcium");
		List<ILabItem> items = labItemQuery.execute();
		assertEquals(1, items.size());
		ILabItem item = items.get(0);
		assertEquals("mmol/L", item.getUnit());
		assertEquals("2.20 - 2.65", item.getReferenceFemale());
		// TODO REGRESSION CHECK test error patient is female ..
		//		assertEquals("2.20 - 2.65", item.getReferenceMale());
	}
	
	static private void removeExistingItems(){
		CoreModelServiceHolder.get().getQuery(ILabItem.class).execute()
			.forEach(li -> CoreModelServiceHolder.get().delete(li));
	}
	
	private class MaleFemalePatientResolver extends ImporterPatientResolver {
		
		private IPatient female;
		private IPatient male;
		
		private IPatient last;
		
		public MaleFemalePatientResolver(){
			female = new IContactBuilder.PatientBuilder(CoreModelServiceHolder.get(), "Female",
				"Test", LocalDate.of(1999, 1, 1), Gender.FEMALE).buildAndSave();
			male = new IContactBuilder.PatientBuilder(CoreModelServiceHolder.get(), "Male", "Test",
				LocalDate.of(1999, 1, 1), Gender.MALE).buildAndSave();
		}
		
		@Override
		public IPatient resolvePatient(String firstname, String lastname, String birthDate){
			if (last == null || last == male) {
				last = female;
				return female;
			} else {
				last = male;
				return male;
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
