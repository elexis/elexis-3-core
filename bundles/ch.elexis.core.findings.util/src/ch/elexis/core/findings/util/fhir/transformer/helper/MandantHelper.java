package ch.elexis.core.findings.util.fhir.transformer.helper;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.codesystems.PractitionerRole;

import ch.elexis.core.findings.codes.CodingSystem;

public class MandantHelper extends AbstractHelper {

	public CodeableConcept getPractitionerRoleCode(String roleId) {
		CodeableConcept code = new CodeableConcept();
		if ("mpa".equals(roleId) || "mpk".equals(roleId)) {
			code.addCoding(new Coding(PractitionerRole.NURSE.getSystem(), PractitionerRole.NURSE.toCode(),
					PractitionerRole.NURSE.toCode()));
		} else if ("medical-practitioner".equals(roleId) || "mandator".equals(roleId)) {
			code.addCoding(new Coding(PractitionerRole.DOCTOR.getSystem(), PractitionerRole.DOCTOR.toCode(),
					PractitionerRole.DOCTOR.toCode()));
		} else if ("executive_doctor".equals(roleId)) {
			code.addCoding(new Coding(CodingSystem.ELEXIS_PRACTITIONER_ROLE.getSystem(), "mandant", "mandant"));
		} else {
			code.addCoding(new Coding(CodingSystem.ELEXIS_PRACTITIONER_ROLE.getSystem(), roleId, roleId));
		}
		return code;
	}
}
