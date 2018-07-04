package ch.elexis.core.model;

public class TransientCategory implements ICategory {
	
	private String name;
	
	public TransientCategory(String name){
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
		TransientCategory other = (TransientCategory) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
