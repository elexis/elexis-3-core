package ch.elexis.core.coding.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import ch.elexis.core.coding.internal.model.ConceptList;
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
			LoggerFactory.getLogger(JsonValueSet.class)
				.error("Error oasing valueset [" + name + "]");
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
		if (valueSet.conceptList != null) {
			for (ConceptList cList : valueSet.conceptList) {
				if (cList.concept != null) {
					return cList.concept.stream().map(e -> (ICoding) e)
						.collect(Collectors.toList());
				}
			}
		}
		return Collections.emptyList();
	}
	
	@Override
	public String toString(){
		return "JsonValueSet [valueSet=" + valueSet + "]";
	}
	
	public static String getSystemLanguage(){
		String language = Locale.getDefault().getLanguage();
		if (language != null) {
			switch (language) {
			case "de":
				return "de-CH";
			case "fr":
				return "fr-CH";
			case "it":
				return "it-CH";
			case "en":
				return "en-US";
			}
		}
		return "de-CH";
	}
}
