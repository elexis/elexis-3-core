package ch.elexis.core.findings.util.internal;

import com.google.gson.JsonElement;

public class JsonStructuralFeature {
	
	public enum Type
	{
			PRIMITIVE, OBJECT, ARRAY
	}
	
	private String name;
	
	private Type type;
	
	public JsonStructuralFeature(String name, Type type){
		this.name = name;
		this.type = type;
	}
	
	public String getName(){
		return name;
	}
	
	public boolean isSameType(JsonElement element) {
		switch (type) {
		case PRIMITIVE:
			return element.isJsonPrimitive();
		case OBJECT:
			return element.isJsonObject();
		case ARRAY:
			return element.isJsonArray();
		}
		return false;
	}
}
