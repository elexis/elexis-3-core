package ch.elexis.core.findings.util.internal;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import ch.elexis.core.findings.util.internal.JsonStructuralFeature.Type;

public class FindingsFormat20 extends FindingsFormat {
	
	public FindingsFormat20(){
		HashMap<String, JsonStructuralFeature> conditionFields = new HashMap<>();
		conditionFields.put("resourceType",
			new JsonStructuralFeature("resourceType", Type.PRIMITIVE));
		conditionFields.put("id", new JsonStructuralFeature("id", Type.PRIMITIVE));
		conditionFields.put("text", new JsonStructuralFeature("text", Type.OBJECT));
		conditionFields.put("category", new JsonStructuralFeature("category", Type.OBJECT));
		conditionFields.put("code", new JsonStructuralFeature("code", Type.OBJECT));
		conditionFields.put("subject", new JsonStructuralFeature("subject", Type.OBJECT));
		conditionFields.put("dateRecorded",
			new JsonStructuralFeature("dateRecorded", Type.PRIMITIVE));
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
		JsonObject jsonObject = getJsonObject(rawContent);
		JsonElement resourceType = jsonObject.get("resourceType");
		
		return convertToCurrentFormat(resourceType, jsonObject);
	}
	
	private Optional<String> convertToCurrentFormat(JsonElement resourceType,
		JsonObject jsonObject){
		
		HashMap<String, JsonStructuralFeatureTransformation> transformations = new HashMap<>();
		transformations.put("dateRecorded", new JsonStructuralFeatureTransformation() {
			@Override
			public JsonElement transformValue(JsonElement element){
				return element;
			}
			
			@Override
			public String transformKey(String key){
				return "assertedDate";
			}
		});
		transformations.put("category", new JsonStructuralFeatureTransformation() {
			
			@Override
			public String transformKey(String key){
				return key;
			}
			
			@Override
			public JsonElement transformValue(JsonElement element){
				JsonObject category = new JsonObject();
				JsonArray coding = new JsonArray();
				for (Entry<String, JsonElement> entry : ((JsonObject) element).entrySet()) {
					if (entry.getKey().equals("coding") && entry.getValue().isJsonArray()) {
						JsonArray existingCoding = (JsonArray) entry.getValue();
						for (JsonElement jsonElement : existingCoding) {
							if (jsonElement.isJsonObject()
								&& ((JsonObject) jsonElement).get("system").getAsString()
									.equals("http://hl7.org/fhir/condition-category")
								&& ((JsonObject) jsonElement).get("code").getAsString()
									.equals("diagnosis")) {
								JsonObject newCoding = new JsonObject();
								newCoding.add("system",
									new JsonPrimitive("http://hl7.org/fhir/condition-category"));
								newCoding.add("code", new JsonPrimitive("problem-list-item"));
								newCoding.add("display", new JsonPrimitive("Problem List Item"));
								coding.add(newCoding);
							} else {
								coding.add(jsonElement);
							}
						}
					}
				}
				category.add("coding", coding);
				
				JsonArray ret = new JsonArray();
				ret.add(category);
				return ret;
			}
		});
		
		switch (resourceType.getAsString()) {
		case "Condition":
			return convert(transformations, jsonObject);
		}
		
		return Optional.empty();
	}
	
}
