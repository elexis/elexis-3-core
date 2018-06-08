package ch.elexis.core.model;

import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.types.Country;

public class Contact extends AbstractIdDeleteModelAdapter<Kontakt> implements IContact {
	
	public Contact(Kontakt model){
		super(model);
	}
	
	@Override
	public boolean isMandator(){
		return getEntity().isMandator();
	}
	
	@Override
	public void setMandator(boolean value){
		getEntity().setMandator(value);
	}
	
	@Override
	public boolean isUser(){
		return getEntity().isUser();
	}
	
	@Override
	public void setUser(boolean value){
		getEntity().setUser(value);
	}
	
	@Override
	public boolean isPerson(){
		return getEntity().isPerson();
	}
	
	@Override
	public void setPerson(boolean value){
		getEntity().setPerson(value);
	}
	
	@Override
	public boolean isPatient(){
		return getEntity().isPatient();
	}
	
	@Override
	public void setPatient(boolean value){
		getEntity().setPatient(value);
	}
	
	@Override
	public boolean isLaboratory(){
		return getEntity().isLaboratory();
	}
	
	@Override
	public void setLaboratory(boolean value){
		getEntity().setLaboratory(value);
	}
	
	@Override
	public boolean isOrganization(){
		return getEntity().isOrganisation();
	}
	
	@Override
	public void setOrganization(boolean value){
		getEntity().setOrganisation(value);
	}
	
	@Override
	public String getDescription1(){
		return getEntity().getDescription1();
	}
	
	@Override
	public void setDescription1(String value){
		getEntity().setDescription1(value);
	}
	
	@Override
	public String getDescription2(){
		return getEntity().getDescription2();
	}
	
	@Override
	public void setDescription2(String value){
		getEntity().setDescription2(value);
	}
	
	@Override
	public String getDescription3(){
		return getEntity().getDescription3();
	}
	
	@Override
	public void setDescription3(String value){
		getEntity().setDescription3(value);
	}
	
	@Override
	public String getCode(){
		return getEntity().getCode();
	}
	
	@Override
	public void setCode(String value){
		getEntity().setCode(value);
	}
	
	@Override
	public Country getCountry(){
		return getEntity().getCountry();
	}
	
	@Override
	public void setCountry(Country value){
		getEntity().setCountry(value);
	}
	
	@Override
	public String getZip(){
		return getEntity().getZip();
	}
	
	@Override
	public void setZip(String value){
		getEntity().setZip(value);
	}
	
	@Override
	public String getCity(){
		return getEntity().getCity();
	}
	
	@Override
	public void setCity(String value){
		getEntity().setCity(value);
	}
	
	@Override
	public String getStreet(){
		return getEntity().getStreet();
	}
	
	@Override
	public void setStreet(String value){
		getEntity().setStreet(value);
	}
	
	@Override
	public String getPhone1(){
		return getEntity().getPhone1();
	}
	
	@Override
	public void setPhone1(String value){
		getEntity().setPhone1(value);
	}
	
	@Override
	public String getPhone2(){
		return getEntity().getPhone2();
	}
	
	@Override
	public void setPhone2(String value){
		getEntity().setPhone2(value);
	}
	
	@Override
	public String getFax(){
		return getEntity().getFax();
	}
	
	@Override
	public void setFax(String value){
		getEntity().setFax(value);
	}
	
	@Override
	public String getEmail(){
		return getEntity().getEmail();
	}
	
	@Override
	public void setEmail(String value){
		getEntity().setEmail(value);
	}
	
	@Override
	public String getWebsite(){
		return getEntity().getWebsite();
	}
	
	@Override
	public void setWebsite(String value){
		getEntity().setWebsite(value);
	}
	
	@Override
	public String getMobile(){
		return getEntity().getMobile();
	}
	
	@Override
	public void setMobile(String value){
		getEntity().setMobile(value);
	}
	
	@Override
	public String getComment(){
		return getEntity().getComment();
	}
	
	@Override
	public void setComment(String value){
		getEntity().setComment(value);
	}
}
