package ch.elexis.core.findings.util.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class FindingsFormat {
	
	protected HashMap<String, Map<String, JsonStructuralFeature>> resourceFieldsMap =
		new HashMap<>();
	
	protected GsonBuilder gsonBuilder = new GsonBuilder();
	
	protected Gson getGson(){
		return gsonBuilder.create();
	}
	
	protected JsonObject getJsonObject(String content){
		return getGson().fromJson(content, JsonObject.class);
	}
	
	public HashMap<String, Map<String, JsonStructuralFeature>> getResourceFieldsMap(){
		return resourceFieldsMap;
	}
	
	public abstract boolean isFindingsFormat(String rawContent);
	
	public abstract Optional<String> convertToCurrentFormat(String rawContent);
	
	protected boolean checkFields(Map<String, JsonStructuralFeature> structuralFeatureMap,
		JsonObject jsonObject){
		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			JsonStructuralFeature structuralFeature = structuralFeatureMap.get(entry.getKey());
			if (structuralFeature != null && !structuralFeature.isSameType(entry.getValue())) {
				return false;
			}
		}
		return true;
	}
	
	protected Optional<String> convert(
		Map<String, JsonStructuralFeatureTransformation> transformMap, JsonObject jsonObject){
		JsonObject newObject = new JsonObject();

		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			JsonStructuralFeatureTransformation transformation = transformMap.get(entry.getKey());
			if (transformation != null) {
				newObject.add(transformation.transformKey(entry.getKey()),
					transformation.transformValue(entry.getValue()));
			} else {
				newObject.add(entry.getKey(), entry.getValue());
			}
		}
		return Optional.of(getGson().toJson(newObject));
	}
}
