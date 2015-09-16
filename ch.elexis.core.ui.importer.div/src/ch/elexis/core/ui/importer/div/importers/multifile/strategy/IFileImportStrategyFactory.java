package ch.elexis.core.ui.importer.div.importers.multifile.strategy;

import java.io.File;
import java.util.Map;

/**
 * a factory containing information about all the available {@link IFileImportStrategy
 * IFileImportStrategies} and for which file type they are appropriate.
 * 
 * @author lucia
 *		
 */
public interface IFileImportStrategyFactory {
	
	/**
	 * Use this to create a {@link Map} containing the HL7File and it's {@link IFileImportStrategy}.
	 * <br>
	 * Add any connected files you'd like and their {@link IFileImportStrategy} using this method.
	 * <br>
	 * 
	 * @param hl7File
	 *            {@link File}
	 * @return {@link Map} of all {@link File Files} and their appropriate
	 *         {@link IFileImportStrategy}
	 */
	public Map<File, IFileImportStrategy> createImportStrategyMap(File hl7File);
	
}
