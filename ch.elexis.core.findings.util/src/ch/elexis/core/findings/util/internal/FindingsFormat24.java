package ch.elexis.core.findings.util.internal;

import java.util.HashMap;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ch.elexis.core.findings.util.internal.JsonStructuralFeature.Type;

public class FindingsFormat24 extends FindingsFormat {
	
	
	public FindingsFormat24(){
		HashMap<String, JsonStructuralFeature> conditionFields = new HashMap<>();
		conditionFields.put("resourceType", new JsonStructuralFeature("resourceType", Type.PRIMITIVE));
		conditionFields.put("id", new JsonStructuralFeature("id", Type.PRIMITIVE));
		conditionFields.put("text", new JsonStructuralFeature("text", Type.OBJECT));
		conditionFields.put("category", new JsonStructuralFeature("category", Type.ARRAY));
		conditionFields.put("code", new JsonStructuralFeature("code", Type.OBJECT));
		conditionFields.put("subject", new JsonStructuralFeature("subject", Type.OBJECT));
		conditionFields.put("assertedDate",
			new JsonStructuralFeature("assertedDate", Type.PRIMITIVE));
		resourceFieldsMap.put("Condition", conditionFields);
	}
	
	public boolean isFindingsFormat(String rawContent){
		JsonObject jsonObject = getJsonObject(rawContent);
		JsonElement resourceType = jsonObject.get("resourceType");
		
		return checkFindingsFormatProperties(resourceType, jsonObject);
	}
	
	private boolean checkFindingsFormatProperties(JsonElement resourceType, JsonObject jsonObject){
		switch (resourceType.getAsString()) {
		case "Condition":
			return checkFields(resourceFieldsMap.get("Condition"), jsonObject);
		}
		return false;
	}
	
	@Override
	public Optional<String> convertToCurrentFormat(String rawContent){
		return Optional.empty();
	}
}
