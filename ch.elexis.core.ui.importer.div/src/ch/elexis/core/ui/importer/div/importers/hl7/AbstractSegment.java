package ch.elexis.core.ui.importer.div.importers.hl7;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ui.importer.div.importers.HL7.MSH;

public abstract class AbstractSegment {
	static Logger logger = LoggerFactory.getLogger(AbstractSegment.class);
	
	public static final String INVALID = "INVALID";
	
	protected int of;
	public String[] field;
	
	public abstract boolean isValid();
	
	/**
	 * 
	 * @param index
	 * @return the String value if index is valid, else {@link MSH#INVALID}
	 */
	public String getFieldAt(int index){
		try {
			return field[index];
		} catch (IndexOutOfBoundsException e) {
			logger.debug("IndexOutOfBounds at MSH index: " + index);
			return INVALID;
		}
	}
	
}
