package ch.elexis.core.findings;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BackboneComponent {
	
	private List<ICoding> coding = new ArrayList<>();
	private Optional<BigDecimal> numericValue = Optional.empty();
	private Optional<String> numericValueUnit = Optional.empty();
	private final String id;
	
	public BackboneComponent(String id){
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
}
