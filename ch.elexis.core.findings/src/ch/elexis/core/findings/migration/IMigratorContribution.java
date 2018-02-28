package ch.elexis.core.findings.migration;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IFinding;

public interface IMigratorContribution {
	
	public boolean canHandlePatientsFindings(Class<? extends IFinding> filter, ICoding coding);
	
	public void migratePatientsFindings(String patientId, Class<? extends IFinding> filter,
		ICoding coding);
}
