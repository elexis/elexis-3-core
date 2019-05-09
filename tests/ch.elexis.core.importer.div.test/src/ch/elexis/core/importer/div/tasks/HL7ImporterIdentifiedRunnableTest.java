package ch.elexis.core.importer.div.tasks;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.elexis.core.importer.div.importers.ILabImportUtil;
import ch.elexis.core.importer.div.tasks.internal.HL7ImporterIIdentifiedRunnable;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.tasks.IIdentifiedRunnable.RunContextParameter;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.elexis.core.utils.PlatformHelper;

public class HL7ImporterIdentifiedRunnableTest {
	
	private static IModelService coreModelService;
	private static ILabImportUtil labimportUtil;
	private static IVirtualFilesystemService vfsService;
	private static IPatient patient;
	
	private HL7ImporterIIdentifiedRunnable hl7ImporterRunnable;
	
	@BeforeClass
	public static void beforeClass() throws IOException{
		coreModelService = OsgiServiceUtil.getService(IModelService.class,
			"(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();
		labimportUtil = OsgiServiceUtil.getService(ILabImportUtil.class).get();
		
		patient = new IContactBuilder.PatientBuilder(coreModelService, "Hans", "Muster",
			LocalDate.of(2011, 1, 12), Gender.MALE).build();
		patient.setPatientNr("5083");
		coreModelService.save(patient);
	}
	
	@Before
	public void before(){
		hl7ImporterRunnable = new HL7ImporterIIdentifiedRunnable(coreModelService, labimportUtil, vfsService);
	}
	
	@Test
	public void basicLocalFileImport() throws TaskException, MalformedURLException{
		File src = new File(PlatformHelper.getBasePath("ch.elexis.core.importer.div.test"),
			"rsc/5083_LabCube_ABXMicrosEmi_20160217143956_198647.hl7");
		String url = src.toURI().toURL().toString();
		
		Map<String, Serializable> context = hl7ImporterRunnable.getDefaultRunContext();
		context.put(RunContextParameter.STRING_URL, url);
		context.put(HL7ImporterIIdentifiedRunnable.RCP_BOOLEAN_CREATE_LABORATORY_IF_NOT_EXISTS,
			Boolean.TRUE);
		hl7ImporterRunnable.run(context, new NullProgressMonitor(),
			LoggerFactory.getLogger(getClass()));
	}
	
	@Test
	public void basicSmbFileImport() throws TaskException, MalformedURLException{
		// smb://unittest:unittest@gitlab.medelexis.ch/tests/
		String url =
			"smb://unittest:unittest@gitlab.medelexis.ch/tests/5083_LabCube_DriChem7000_20180314131140_288107.hl7";
		
		Map<String, Serializable> context = hl7ImporterRunnable.getDefaultRunContext();
		context.put(RunContextParameter.STRING_URL, url);
		context.put(HL7ImporterIIdentifiedRunnable.RCP_BOOLEAN_CREATE_LABORATORY_IF_NOT_EXISTS,
			Boolean.TRUE);
		hl7ImporterRunnable.run(context, new NullProgressMonitor(),
			LoggerFactory.getLogger(getClass()));
	}
	
}
