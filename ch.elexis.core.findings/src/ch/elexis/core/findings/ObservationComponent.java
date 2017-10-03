package ch.elexis.core.findings;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.elexis.core.findings.IObservation.ObservationType;

public class ObservationComponent {
	
	private List<ICoding> coding = new ArrayList<>();
	private Optional<BigDecimal> numericValue = Optional.empty();
	private Optional<String> numericValueUnit = Optional.empty();
	private Optional<String> stringValue = Optional.empty();
	private final String id;
	private Map<String, String> extensions = new HashMap<>();
	
	public static final String EXTENSION_OBSERVATION_TYPE_URL = "www.elexis.info/observation/type";
	
	public ObservationComponent(String id){
		this.id = id;
	}
	
	public String getId(){
		return id;
	}
	
	public List<ICoding> getCoding(){
		return coding;
	}
	public void setCoding(List<ICoding> coding){
		this.coding = coding;
	}
	
	public Optional<BigDecimal> getNumericValue(){
		return numericValue;
	}
	
	public void setNumericValue(Optional<BigDecimal> numericValue){
		this.numericValue = numericValue;
	}
	
	public Optional<String> getNumericValueUnit(){
		return numericValueUnit;
	}
	
	public void setNumericValueUnit(Optional<String> numericValueUnit){
		this.numericValueUnit = numericValueUnit;
	}
	
	public void setStringValue(Optional<String> stringValue){
		this.stringValue = stringValue;
	}
	
	public Optional<String> getStringValue(){
		return stringValue;
	}
	
	public void setExtensions(Map<String, String> extensions){
		this.extensions = extensions;
	}
	
	public Map<String, String> getExtensions(){
		return extensions;
	}
	
	public <T> T getTypeFromExtension(Class<T> clazz){
		String type = null;
		if (clazz.equals(ObservationType.class)) {
			type = getExtensions().get(EXTENSION_OBSERVATION_TYPE_URL);
			if (type != null) {
				return clazz.cast(ObservationType.valueOf(type));
			}
		}
		
		return null;
	}
}
