package ch.elexis.core.findings;

import java.util.List;
import java.util.Optional;

public interface IFindingsService {

	public List<IFinding> getPatientsFindings(String patientId, Class<? extends IFinding> filter);

	public List<IFinding> getConsultationsFindings(String consultationId, Class<? extends IFinding> filter);

	public void saveFinding(IFinding finding);

	public void deleteFinding(IFinding finding);

	public IFindingsFactory getFindingsFactory();

	public Optional<IFinding> findById(String idPart);

	/**
	 * Set if the service should try to create or update Findings from existing
	 * information. Existing information are Finding specific e.g.
	 * {@link IEncounter} will search for {@link Behandung}.<br />
	 * 
	 * If set to true, find operations will run the IFinding and Service
	 * specific create or update code.<br />
	 * 
	 * Default createOrUpdate is false.
	 * 
	 * @param value
	 */
	public void setCreateOrUpdate(boolean value);

	public boolean getCreateOrUpdate();
}
