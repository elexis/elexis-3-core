package ch.elexis.core.coding.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import ch.elexis.core.coding.internal.model.ValueSet;
import ch.elexis.core.coding.internal.model.ValueSets;
import ch.elexis.core.findings.ICoding;

public class JsonValueSet {
	
	public static Optional<JsonValueSet> load(String name){
		try {
			InputStream jsonInput =
				JsonValueSet.class.getResourceAsStream("/rsc/valuesets/" + name + ".json");
			if (jsonInput != null) {
				Gson gson = new GsonBuilder().create();
				ValueSets valueSets =
					gson.fromJson(IOUtils.toString(jsonInput, "UTF-8"), ValueSets.class);
				if (valueSets.hasValueSet()) {
					return Optional
						.of(new JsonValueSet(valueSets.valueSets.get(0).valueSet.get(0)));
				}
			}
		} catch (JsonSyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Optional.empty();
	}
	
	private ValueSet valueSet;
	
	public JsonValueSet(ValueSet valueSet){
		this.valueSet = valueSet;
	}
	
	public String getId(){
		return valueSet.id;
	}
	
	public List<ICoding> getCoding(){
		// TODO Auto-generated method stub
		return null;
	}
	
}
