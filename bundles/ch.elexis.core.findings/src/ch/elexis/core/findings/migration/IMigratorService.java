package ch.elexis.core.findings.migration;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IFinding;

public interface IMigratorService {

	/**
	 * Preference if diagnose is used structured, saved in global config
	 */
	public static final String DIAGNOSE_SETTINGS_USE_STRUCTURED = "diagnose/settings/useStructured";

	/**
	 * Preference if personal anamnesis is used structured, saved in global config
	 */
	public static final String PERSANAM_SETTINGS_USE_STRUCTURED = "persanamnese/settings/useStructured";

	/**
	 * Preference if risk factors is used structured, saved in global config
	 */
	public static final String RISKFACTOR_SETTINGS_USE_STRUCTURED = "riskfactor/settings/useStructured";

	/**
	 * Preference if family anamnesis is used structured, saved in global config
	 */
	public static final String FAMANAM_SETTINGS_USE_STRUCTURED = "familyanamnese/settings/useStructured";

	/**
	 * Preference if allergies and intolerances is used structured, saved in global
	 * config
	 */
	public static final String ALLERGYINTOLERANCE_SETTINGS_USE_STRUCTURED = "allergyintolerance/settings/useStructured";

	/**
	 * Migrate older information of the patient to the its new {@link IFinding}
	 * representation.
	 *
	 * @param patientId
	 * @param filter
	 * @param coding
	 */
	public void migratePatientsFindings(String patientId, Class<? extends IFinding> filter, ICoding coding);

	/**
	 * Migrate older information of the consultation to the its new {@link IFinding}
	 * representation.
	 *
	 * @param consultationId
	 * @param filter
	 */
	public void migrateConsultationsFindings(String consultationId, Class<? extends IFinding> filter);
}
