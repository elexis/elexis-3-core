package ch.elexis.core.model;

import java.time.LocalDateTime;

import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.types.Gender;

public class Person extends Contact implements IPerson {
	
	public Person(Kontakt model){
		super(model);
	}
	
	@Override
	public Gender getGender(){
		return getEntity().getGender();
	}
	
	@Override
	public void setGender(Gender value){
		getEntity().setGender(value);
	}
	
	@Override
	public String getTitel(){
		return getEntity().getTitel();
	}
	
	@Override
	public void setTitel(String value){
		getEntity().setTitel(value);
	}
	
	@Override
	public String getTitelSuffix(){
		return getEntity().getTitelSuffix();
	}
	
	@Override
	public void setTitelSuffix(String value){
		getEntity().setTitelSuffix(value);
	}
	
	@Override
	public String getFirstName(){
		return getEntity().getDescription2();
	}
	
	@Override
	public LocalDateTime getDateOfBirth(){
		return getEntity().getDob().atStartOfDay();
	}
	
	@Override
	public void setDateOfBirth(LocalDateTime value){
		getEntity().setDob(value.toLocalDate());
	}
	
	@Override
	public void setFirstName(String value){
		getEntity().setDescription2(value);
	}
	
	@Override
	public String getLastName(){
		return getEntity().getDescription1();
	}
	
	@Override
	public void setLastName(String value){
		getEntity().setDescription1(value);
	}
}
