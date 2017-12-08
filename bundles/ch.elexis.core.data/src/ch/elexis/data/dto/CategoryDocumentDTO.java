package ch.elexis.data.dto;

import ch.elexis.core.model.ICategory;

public class CategoryDocumentDTO implements ICategory {
	
	private String name;
	
	public CategoryDocumentDTO(String name){
		super();
		this.name = name;
	}
	
	@Override
	public String getName(){
		return name;
	}
	
	@Override
	public void setName(String value){
		this.name = value;
	}
	
	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CategoryDocumentDTO other = (CategoryDocumentDTO) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
