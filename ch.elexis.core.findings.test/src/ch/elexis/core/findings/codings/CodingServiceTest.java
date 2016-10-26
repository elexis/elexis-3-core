package ch.elexis.core.findings.codings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Test;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.codes.ICodingService;
import ch.elexis.core.findings.codes.ILocalCodingContribution;

public class CodingServiceTest {
	
	@After
	public void afterTest(){
		ICodingService codingService = CodingServiceComponent.getService();
		assertNotNull(codingService);
		List<ICoding> readCodes =
			codingService.getAvailableCodes(ILocalCodingContribution.LOCAL_CODE_SYSTEM);
		for (ICoding iCoding : readCodes) {
			codingService.removeLocalCoding(iCoding);
		}
	}
	
	@Test
	public void getAvailableCodeSystems(){
		ICodingService codingService = CodingServiceComponent.getService();
		assertNotNull(codingService);
		List<String> systems = codingService.getAvailableCodeSystems();
		assertNotNull(systems);
		assertFalse(systems.isEmpty());
	}
	
	@Test
	public void addLocalCoding(){
		ICodingService codingService = CodingServiceComponent.getService();
		assertNotNull(codingService);
		List<ICoding> readCodes =
			codingService.getAvailableCodes(ILocalCodingContribution.LOCAL_CODE_SYSTEM);
		assertNotNull(readCodes);
		assertTrue(readCodes.isEmpty());
		
		codingService.addLocalCoding(new ICoding() {
			@Override
			public String getSystem(){
				return ILocalCodingContribution.LOCAL_CODE_SYSTEM;
			}
			
			@Override
			public String getDisplay(){
				return "display text";
			}
			
			@Override
			public String getCode(){
				return "code1";
			}
		});
		readCodes =
			codingService.getAvailableCodes(ILocalCodingContribution.LOCAL_CODE_SYSTEM);
		assertNotNull(readCodes);
		assertFalse(readCodes.isEmpty());
		assertEquals("code1", readCodes.get(0).getCode());
		assertEquals("display text", readCodes.get(0).getDisplay());
	}
}
