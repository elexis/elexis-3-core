package ch.elexis.core.findings.util.internal;

import java.util.HashMap;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import ch.elexis.core.findings.util.internal.JsonStructuralFeature.Type;

public class FindingsFormat24 extends FindingsFormat {

	public FindingsFormat24() {
		HashMap<String, JsonStructuralFeature> conditionFields = new HashMap<>();
		conditionFields.put("resourceType", new JsonStructuralFeature("resourceType", Type.PRIMITIVE));
		conditionFields.put("id", new JsonStructuralFeature("id", Type.PRIMITIVE));
		conditionFields.put("text", new JsonStructuralFeature("text", Type.OBJECT));
		conditionFields.put("category", new JsonStructuralFeature("category", Type.ARRAY));
		conditionFields.put("code", new JsonStructuralFeature("code", Type.OBJECT));
		conditionFields.put("subject", new JsonStructuralFeature("subject", Type.OBJECT));
		conditionFields.put("assertedDate", new JsonStructuralFeature("assertedDate", Type.PRIMITIVE));
		resourceFieldsMap.put("Condition", conditionFields);

		HashMap<String, JsonStructuralFeature> encounterFields = new HashMap<>();
		encounterFields.put("resourceType", new JsonStructuralFeature("resourceType", Type.PRIMITIVE));
		encounterFields.put("id", new JsonStructuralFeature("id", Type.PRIMITIVE));
		encounterFields.put("text", new JsonStructuralFeature("text", Type.OBJECT));
		encounterFields.put("diagnosis", new JsonStructuralFeature("diagnosis", Type.ARRAY));
		encounterFields.put("subject", new JsonStructuralFeature("subject", Type.OBJECT));
		resourceFieldsMap.put("Encounter", encounterFields);

		HashMap<String, JsonStructuralFeature> procedureRequestFields = new HashMap<>();
		procedureRequestFields.put("resourceType", new JsonStructuralFeature("resourceType", Type.PRIMITIVE));
		procedureRequestFields.put("id", new JsonStructuralFeature("id", Type.PRIMITIVE));
		procedureRequestFields.put("text", new JsonStructuralFeature("text", Type.OBJECT));
		procedureRequestFields.put("context", new JsonStructuralFeature("context", Type.OBJECT));
		resourceFieldsMap.put("ProcedureRequest", procedureRequestFields);
	}

	@Override
	public int isFindingsFormat(String rawContent) {
		JsonObject jsonObject = getJsonObject(rawContent);
		JsonElement resourceType = jsonObject.get("resourceType");

		return checkFindingsFormatProperties(resourceType, jsonObject);
	}

	private int checkFindingsFormatProperties(JsonElement resourceType, JsonObject jsonObject) {
		switch (resourceType.getAsString()) {
		case "Condition":
			return checkFields(resourceFieldsMap.get("Condition"), jsonObject);
		case "Encounter":
			return checkFields(resourceFieldsMap.get("Encounter"), jsonObject);
		case "ProcedureRequest":
			return checkFields(resourceFieldsMap.get("ProcedureRequest"), jsonObject);
		}
		return 0;
	}

	@Override
	public Optional<String> convertToCurrentFormat(String rawContent) {
		JsonObject jsonObject = getJsonObject(rawContent);
		JsonElement resourceType = jsonObject.get("resourceType");

		return convertToCurrentFormat(resourceType, jsonObject);
	}

	private Optional<String> convertToCurrentFormat(JsonElement resourceType, JsonObject jsonObject) {
		HashMap<String, JsonStructuralFeatureTransformation> conditionTransformations = new HashMap<>();
		conditionTransformations.put("assertedDate", new JsonStructuralFeatureTransformation() {
			@Override
			public JsonElement transformValue(JsonElement element) {
				return element;
			}

			@Override
			public String transformKey(String key) {
				return "recordedDate";
			}
		});
		conditionTransformations.put("clinicalStatus", new JsonStructuralFeatureTransformation() {

			@Override
			public String transformKey(String key) {
				return key;
			}

			@Override
			public JsonElement transformValue(JsonElement element) {
				JsonArray coding = new JsonArray();

				JsonPrimitive value = (JsonPrimitive) element;
				if (StringUtils.isNotBlank(value.getAsString())) {
					JsonObject newCoding = new JsonObject();
					newCoding.add("system",
							new JsonPrimitive("http://terminology.hl7.org/CodeSystem/condition-clinical"));
					newCoding.add("code", new JsonPrimitive(value.getAsString()));
					coding.add(newCoding);
				}

				JsonObject ret = new JsonObject();
				ret.add("coding", coding);
				return ret;
			}
		});

		HashMap<String, JsonStructuralFeatureTransformation> encounterTransformations = new HashMap<>();
		encounterTransformations.put("!addAfter!identifier", new JsonStructuralFeatureTransformation() {
			@Override
			public JsonElement transformValue(JsonElement element) {
				((JsonObject) element).add("status", new JsonPrimitive("finished"));
				return element;
			}

			@Override
			public String transformKey(String key) {
				throw new UnsupportedOperationException();
			}
		});

		HashMap<String, JsonStructuralFeatureTransformation> procedureRequestTransformations = new HashMap<>();
		procedureRequestTransformations.put("resourceType", new JsonStructuralFeatureTransformation() {
			@Override
			public JsonElement transformValue(JsonElement element) {
				return new JsonPrimitive("ServiceRequest");
			}

			@Override
			public String transformKey(String key) {
				return key;
			}
		});
		procedureRequestTransformations.put("context", new JsonStructuralFeatureTransformation() {
			@Override
			public JsonElement transformValue(JsonElement element) {
				return element;
			}

			@Override
			public String transformKey(String key) {
				return "encounter";
			}
		});

		switch (resourceType.getAsString()) {
		case "Condition":
			return convert(conditionTransformations, jsonObject);
		case "Encounter":
			return convert(encounterTransformations, jsonObject);
		case "ProcedureRequest":
			return convert(procedureRequestTransformations, jsonObject);
		}

		return Optional.empty();
	}
}
