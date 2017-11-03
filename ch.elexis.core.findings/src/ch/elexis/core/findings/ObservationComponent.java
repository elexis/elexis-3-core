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
	private BigDecimal numericValue;
	private String numericValueUnit;
	private String stringValue;
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
		return Optional.ofNullable(numericValue);
	}
	
	public void setNumericValue(BigDecimal numericValue) {
		this.numericValue = numericValue;
	}
	
	public Optional<String> getNumericValueUnit(){
		return Optional.ofNullable(numericValueUnit);
	}
	
	public void setNumericValueUnit(String numericValueUnit) {
		this.numericValueUnit = numericValueUnit;
	}
	
	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}
	
	public Optional<String> getStringValue(){
		return Optional.ofNullable(stringValue);
	}
	
	public void setExtensions(Map<String, String> extensions){
		this.extensions = extensions;
	}
	
	public Map<String, String> getExtensions(){
		return extensions;
	}
	
	/**
	 * Returns the type of the observation component element
	 * 
	 * @param clazz
	 * @return
	 */
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
