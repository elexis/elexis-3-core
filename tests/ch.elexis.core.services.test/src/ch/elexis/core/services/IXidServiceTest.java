package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.test.initializer.TestDatabaseInitializer;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;

public class IXidServiceTest extends AbstractServiceTest {

	private IXidService service = OsgiServiceUtil.getService(IXidService.class).get();

	@Test
	public void addAndFindXids() {
		TestDatabaseInitializer.getXidService().localRegisterXIDDomainIfNotExists("domain", "domain",
				XidConstants.ASSIGNMENT_LOCAL);

		IPatient patient = new IContactBuilder.PatientBuilder(coreModelService, "first", "last", LocalDate.now(),
				Gender.FEMALE).buildAndSave();
		service.addXid(patient, "domain", "domainId", true);

		List<IXid> xids = service.getXids(patient);
		assertEquals("domainId", xids.get(0).getDomainId());
		assertEquals("first", service.findObject("domain", "domainId", IPatient.class).get().getFirstName());

		coreModelService.remove(patient);
	}
}
