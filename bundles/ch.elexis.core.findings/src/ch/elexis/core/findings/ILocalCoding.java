package ch.elexis.core.findings;

import java.util.List;

import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.Identifiable;

public interface ILocalCoding extends ICoding, Identifiable, Deleteable {
	
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
	
	/**
	 * Set the code attribute.
	 * 
	 * @param code
	 */
	public void setCode(String code);
	
	/**
	 * Set the display attribute.
	 * 
	 * @param display
	 */
	public void setDisplay(String display);
}
