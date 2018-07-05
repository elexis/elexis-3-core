package ch.elexis.core.ui.dbcheck.syntactic;

public class DataType {
	String dataType;
	int dataTypeSize = -1;
	
	public DataType(String dt){
		int i = dt.indexOf("(");
		int j = dt.indexOf(")");
		if ((i > 0) && (j > 0)) {
			dataType = dt.substring(0, i);
			String dataTypeSizeStr = dt.substring(i + 1, j);
			dataTypeSize = Integer.parseInt(dataTypeSizeStr);
		}
	}
	
	@Override
	public String toString(){
		return dataType + " of size " + dataTypeSize;
	}
	
	public String getDataType(){
		return dataType;
	}
	
	public int getDataTypeSize(){
		return dataTypeSize;
	}
	
	public boolean isCompatibleWith(DataType requested){
		boolean dtc = dataTypeCompatible(requested);
		boolean sizeCompatible = false;
		if (dataTypeSize == -1) {
			sizeCompatible = true;
		} else {
			if (dataTypeSize >= requested.getDataTypeSize())
				sizeCompatible = true;
		}
		return (dtc && sizeCompatible);
	}
	
	private boolean dataTypeCompatible(DataType requested){
		if (dataType.equalsIgnoreCase(requested.getDataType()))
			return true;
		if (comp("bpchar", "char", requested))
			return true;
		if (comp("varchar", "char", requested))
			return true;
		
		return false;
	}
	
	private boolean comp(String string, String string2, DataType requested){
		String dtR = requested.getDataType();
		String dtF = dataType;
		
		if (dtF.equalsIgnoreCase(string) && dtR.equalsIgnoreCase(string2))
			return true;
		return false;
	}
	
}
