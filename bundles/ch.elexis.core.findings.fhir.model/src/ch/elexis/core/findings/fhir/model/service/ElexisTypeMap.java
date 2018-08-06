package ch.elexis.core.findings.fhir.model.service;

import java.util.HashMap;

import ch.elexis.core.jpa.entities.Condition;
import ch.elexis.core.jpa.entities.Encounter;
import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.jpa.entities.Observation;
import ch.elexis.core.services.IModelService;

/**
 * Map type names from new {@link AbstractDBObjectIdDeleted} subclasses to PersistentObject legacy
 * type names. Use by {@link IModelService#loadFromString(String)} and
 * {@link IModelService#storeToString(ch.elexis.core.model.Identifiable)}.
 * 
 * @author thomas
 *
 */
public class ElexisTypeMap {
	
	private static final HashMap<String, Class<? extends EntityWithId>> stsToClassMap;
	private static final HashMap<Class<? extends EntityWithId>, String> classToStsMap;
	
	static {
		stsToClassMap = new HashMap<String, Class<? extends EntityWithId>>();
		classToStsMap = new HashMap<Class<? extends EntityWithId>, String>();

		// bi-directional mappable
		stsToClassMap.put("ch.elexis.core.findings.fhir.po.model.Encounter", Encounter.class);
		classToStsMap.put(Encounter.class, "ch.elexis.core.findings.fhir.po.model.Encounter");
		stsToClassMap.put("ch.elexis.core.findings.fhir.po.model.Condition", Condition.class);
		classToStsMap.put(Condition.class, "ch.elexis.core.findings.fhir.po.model.Condition");
		stsToClassMap.put("ch.elexis.core.findings.fhir.po.model.Observation", Observation.class);
		classToStsMap.put(Observation.class, "ch.elexis.core.findings.fhir.po.model.Observation");
	}

	/**
	 * 
	 * @param obj
	 * @return <code>null</code> if not resolvable, else the resp. Entity Type
	 */
	public static String getKeyForObject(EntityWithId obj){
		if(obj != null) {
			return classToStsMap.get(obj.getClass());
		}

		return null;
	}

	public static Class<? extends EntityWithId> get(String value){
		return stsToClassMap.get(value);
	}
}