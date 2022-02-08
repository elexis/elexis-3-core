package ch.elexis.core.findings.util.fhir.transformer.helper;

import org.hl7.fhir.r4.model.Money;
import org.hl7.fhir.r4.model.Reference;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.Identifiable;

public class FhirUtil {
	
	public static Reference getReference(Identifiable identifiable){
		if (identifiable == null) {
			return null;
		}
		
		String resourceType = null;
		
		if (identifiable instanceof IPatient) {
			resourceType = "Patient";
		} else if (identifiable instanceof IEncounter) {
			resourceType = "Encounter";
		} else if (identifiable instanceof IBilled) {
			resourceType = "ChargeItem";
		}
		
		if (resourceType != null) {
			return new Reference(new IdDt(resourceType, identifiable.getId()));
		}
		
		throw new IllegalArgumentException(identifiable.getClass().getCanonicalName());
	}
	
	public static Money toFhir(ch.rgw.tools.Money total){
		Money money = new Money();
		money.setValue(total.doubleValue());
		money.setCurrency("CHF");
		return money;
	}
	
}
