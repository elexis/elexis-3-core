package ch.elexis.core.model;

public class Account implements IAccount {
	
	private int numeric;
	private String name;
	
	public Account(int numeric, String name){
		this.numeric = numeric;
		this.name = name;
	}
	
	@Override
	public int getNumeric(){
		return numeric;
	}
	
	@Override
	public String getName(){
		return name;
	}
	
	@Override
	public void setName(String name){
		this.name = name;
	}
	
	@Override
	public void setNumeric(int value){
		this.numeric = value;
	}
}
