package ch.elexis.core.eigendiagnosen.model.service;

import ch.elexis.core.jpa.entities.Eigendiagnose;
import ch.elexis.core.jpa.model.adapter.AbstractModelAdapterFactory;
import ch.elexis.core.jpa.model.adapter.MappingEntry;
import ch.elexis.core.model.IDiagnosis;

public class DiagnosisModelAdapterFactory extends AbstractModelAdapterFactory {
	
	private static DiagnosisModelAdapterFactory INSTANCE;
	
	public static synchronized DiagnosisModelAdapterFactory getInstance(){
		if (INSTANCE == null) {
			INSTANCE = new DiagnosisModelAdapterFactory();
		}
		return INSTANCE;
	}
	
	private DiagnosisModelAdapterFactory(){
		super();
	}
	
	@Override
	protected void initializeMappings(){
		addMapping(new MappingEntry(IDiagnosis.class,
			ch.elexis.core.eigendiagnosen.model.CustomDiagnosis.class,
			Eigendiagnose.class));
	}
}
