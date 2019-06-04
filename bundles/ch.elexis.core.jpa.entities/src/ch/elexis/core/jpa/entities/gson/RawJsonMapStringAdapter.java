package ch.elexis.core.jpa.entities.gson;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Raw serialization of a json map serialized as string
 */
public class RawJsonMapStringAdapter extends TypeAdapter<String> {
	
	@Override
	public void write(JsonWriter out, String value) throws IOException{
		if (value != null) {
			out.jsonValue(value);
		}
	}
	
	@Override
	public String read(JsonReader in) throws IOException{
		JsonObject jsonObject = new JsonObject();
		in.beginObject();
		while (in.hasNext()) {
			final String name = in.nextName();
			final String value = in.nextString();
			jsonObject.addProperty(name, value);
		}
		in.endObject();
		return jsonObject.toString();
	}
	
}
