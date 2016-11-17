package ch.elexis.core.findings;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

public class FindingsServiceTest {
	
	@Test
	public void getFactory(){
		IFindingsFactory factory = FindingsServiceComponent.getService().getFindingsFactory();
		assertNotNull(factory);
	}
	
	@Test
	public void getFindingsForPatient(){
		List<IFinding> finding =
			FindingsServiceComponent.getService().getPatientsFindings("abc", IFinding.class);
		assertNotNull(finding);
	}
	
	@Test
	public void getFindingsForConsultation(){
		List<IFinding> finding =
				FindingsServiceComponent.getService().getConsultationsFindings("abc", IFinding.class);
		assertNotNull(finding);
	}
}
