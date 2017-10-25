package ch.elexis.core.findings;

import java.util.List;

public interface ILocalCoding extends ICoding {
	
	/**
	 * Get mapped codes. Codings are mapped if their meaning is the same.
	 * 
	 * @return
	 */
	public List<ICoding> getMappedCodes();
	
	/**
	 * Set mapped codes. Codings are mapped if their meaning is the same.
	 * 
	 * @return
	 */
	public void setMappedCodes(List<ICoding> mappedCodes);
}
