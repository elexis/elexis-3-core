package ch.elexis.core.model;

import java.io.Serializable;

public class Statistics implements Comparable<Statistics>, Serializable {
	
	private static final long serialVersionUID = -359961813285271991L;
	
	private String storeToString;
	private int count;
	
	public Statistics(){}
	
	public Statistics(String storeToString){
		this.storeToString = storeToString;
		this.count = 1;
	}
	
	public int compareTo(Statistics other){
		return other.getCount() - count;
	}
	
	public void setCount(int count){
		this.count = count;
	}
	
	public int getCount(){
		return count;
	}
	
	public String getStoreToString(){
		return storeToString;
	}
	
	public void increase(){
		count++;
	}
}