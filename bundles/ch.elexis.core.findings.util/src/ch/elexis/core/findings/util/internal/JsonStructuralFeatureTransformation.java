package ch.elexis.core.findings.util.internal;

import com.google.gson.JsonElement;

public abstract class JsonStructuralFeatureTransformation {
	
	public abstract JsonElement transformValue(JsonElement element);
	
	public abstract String transformKey(String key);
}