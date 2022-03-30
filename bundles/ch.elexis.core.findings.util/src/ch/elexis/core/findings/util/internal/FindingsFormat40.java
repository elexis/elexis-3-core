package ch.elexis.core.findings.util.internal;

import java.util.HashMap;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ch.elexis.core.findings.util.internal.JsonStructuralFeature.Type;

public class FindingsFormat40 extends FindingsFormat {

	public FindingsFormat40() {
		HashMap<String, JsonStructuralFeature> conditionFields = new HashMap<>();
		conditionFields.put("resourceType", new JsonStructuralFeature("resourceType", Type.PRIMITIVE));
		conditionFields.put("id", new JsonStructuralFeature("id", Type.PRIMITIVE));
		conditionFields.put("text", new JsonStructuralFeature("text", Type.OBJECT));
		conditionFields.put("category", new JsonStructuralFeature("category", Type.ARRAY));
		conditionFields.put("code", new JsonStructuralFeature("code", Type.OBJECT));
		conditionFields.put("subject", new JsonStructuralFeature("subject", Type.OBJECT));
		conditionFields.put("recordedDate", new JsonStructuralFeature("recordedDate", Type.PRIMITIVE));
		conditionFields.put("clinicalStatus", new JsonStructuralFeature("clinicalStatus", Type.OBJECT));
		resourceFieldsMap.put("Condition", conditionFields);

		HashMap<String, JsonStructuralFeature> encounterFields = new HashMap<>();
		encounterFields.put("resourceType", new JsonStructuralFeature("resourceType", Type.PRIMITIVE));
		encounterFields.put("id", new JsonStructuralFeature("id", Type.PRIMITIVE));
		encounterFields.put("text", new JsonStructuralFeature("text", Type.OBJECT));
		encounterFields.put("diagnosis", new JsonStructuralFeature("diagnosis", Type.ARRAY));
		encounterFields.put("subject", new JsonStructuralFeature("subject", Type.OBJECT));
		encounterFields.put("status", new JsonStructuralFeature("status", Type.PRIMITIVE));
		resourceFieldsMap.put("Encounter", encounterFields);

		HashMap<String, JsonStructuralFeature> procedureRequestFields = new HashMap<>();
		procedureRequestFields.put("resourceType", new JsonStructuralFeature("resourceType", Type.PRIMITIVE));
		procedureRequestFields.put("id", new JsonStructuralFeature("id", Type.PRIMITIVE));
		procedureRequestFields.put("text", new JsonStructuralFeature("text", Type.OBJECT));
		procedureRequestFields.put("encounter", new JsonStructuralFeature("context", Type.OBJECT));
		resourceFieldsMap.put("ServiceRequest", procedureRequestFields);
	}

	public int isFindingsFormat(String rawContent) {
		JsonObject jsonObject = getJsonObject(rawContent);
		JsonElement resourceType = jsonObject.get("resourceType");

		return checkFindingsFormatProperties(resourceType, jsonObject);
	}

	private int checkFindingsFormatProperties(JsonElement resourceType, JsonObject jsonObject) {
		switch (resourceType.getAsString()) {
			case "Condition" :
				return checkFields(resourceFieldsMap.get("Condition"), jsonObject);
			case "Encounter" :
				int ret = checkFields(resourceFieldsMap.get("Encounter"), jsonObject);
				if (!checkRequiredField(resourceFieldsMap.get("Encounter").get("status"), jsonObject)) {
					ret--;
				}
				return ret;
			case "ProcedureRequest" :
				return 0;
			case "ServiceRequest" :
				return checkFields(resourceFieldsMap.get("ServiceRequest"), jsonObject);
		}
		return 0;
	}

	@Override
	public Optional<String> convertToCurrentFormat(String rawContent) {
		return Optional.empty();
	}
}
