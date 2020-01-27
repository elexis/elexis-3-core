package ch.elexis.core.findings.fhir.model.service;

import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.IClinicalImpression;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IDocumentReference;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFamilyMemberHistory;
import ch.elexis.core.findings.ILocalCoding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservationLink;
import ch.elexis.core.findings.IProcedureRequest;
import ch.elexis.core.jpa.model.adapter.AbstractModelAdapterFactory;
import ch.elexis.core.jpa.model.adapter.MappingEntry;

public class FindingsModelAdapterFactory extends AbstractModelAdapterFactory {
	
	private static FindingsModelAdapterFactory INSTANCE;
	
	public static synchronized FindingsModelAdapterFactory getInstance(){
		if (INSTANCE == null) {
			INSTANCE = new FindingsModelAdapterFactory();
		}
		return INSTANCE;
	}
	
	private FindingsModelAdapterFactory(){
		super();
	}
	
	@Override
	protected void initializeMappings(){
		addMapping(new MappingEntry(IAllergyIntolerance.class,
			ch.elexis.core.findings.fhir.model.AllergyIntolerance.class,
			ch.elexis.core.jpa.entities.AllergyIntolerance.class));
		addMapping(new MappingEntry(IClinicalImpression.class,
			ch.elexis.core.findings.fhir.model.ClinicalImpression.class,
			ch.elexis.core.jpa.entities.ClinicalImpression.class));
		addMapping(
			new MappingEntry(ICondition.class, ch.elexis.core.findings.fhir.model.Condition.class,
				ch.elexis.core.jpa.entities.Condition.class));
		addMapping(
			new MappingEntry(IEncounter.class, ch.elexis.core.findings.fhir.model.Encounter.class,
				ch.elexis.core.jpa.entities.Encounter.class));
		addMapping(new MappingEntry(IFamilyMemberHistory.class,
			ch.elexis.core.findings.fhir.model.FamilyMemberHistory.class,
			ch.elexis.core.jpa.entities.FamilyMemberHistory.class));
		addMapping(new MappingEntry(ILocalCoding.class,
			ch.elexis.core.findings.fhir.model.LocalCoding.class,
			ch.elexis.core.jpa.entities.LocalCoding.class));
		addMapping(new MappingEntry(IObservation.class,
			ch.elexis.core.findings.fhir.model.Observation.class,
			ch.elexis.core.jpa.entities.Observation.class));
		addMapping(new MappingEntry(IObservationLink.class,
			ch.elexis.core.findings.fhir.model.ObservationLink.class,
			ch.elexis.core.jpa.entities.ObservationLink.class));
		addMapping(new MappingEntry(IProcedureRequest.class,
			ch.elexis.core.findings.fhir.model.ProcedureRequest.class,
			ch.elexis.core.jpa.entities.ProcedureRequest.class));
		addMapping(new MappingEntry(IDocumentReference.class,
			ch.elexis.core.findings.fhir.model.DocumentReference.class,
			ch.elexis.core.jpa.entities.DocumentReference.class));
	}
}
