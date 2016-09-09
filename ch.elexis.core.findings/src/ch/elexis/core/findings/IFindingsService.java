package ch.elexis.core.findings;

import java.util.List;

public interface IFindingsService {
	
	public List<IFinding> getPatientsFindings(String patientId, Class<? extends IFinding> filter);
	
	public List<IFinding> getConsultationsFindings(String consultationId,
		Class<? extends IFinding> filter);
	
	public void saveFinding(IFinding finding);
	
	public void deleteFinding(IFinding finding);
	
	public IFindingsFactory getFindingsFactory();
}
