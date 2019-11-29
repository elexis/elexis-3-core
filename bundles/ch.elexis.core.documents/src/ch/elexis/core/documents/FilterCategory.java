package ch.elexis.core.documents;

import java.util.Objects;

import ch.elexis.core.model.ICategory;

public class FilterCategory implements ICategory {
	
	private String name;
	private String label;
	
	public FilterCategory(ICategory category){
		this.name = category.getName();
	}
	
	public FilterCategory(String name, String lbl){
		this.name = name;
		this.label = lbl;
	}
	
	@Override
	public String getName(){
		return name;
	}
	
	@Override
	public void setName(String name){
		this.name = name;
	}
	
	public String getLabel(){
		return label;
	}
	
	public void setLabel(String label){
		this.label = label;
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(name);
	}
	
	public boolean isAll(){
		return name == null;
	}
	
	@Override
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FilterCategory other = (FilterCategory) obj;
		return Objects.equals(name, other.name);
	}
	
}
