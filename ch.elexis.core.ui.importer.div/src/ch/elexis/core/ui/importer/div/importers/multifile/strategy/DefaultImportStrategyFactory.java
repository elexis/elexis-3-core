package ch.elexis.core.ui.importer.div.importers.multifile.strategy;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.importer.div.importers.multifile.strategy.BasicFileImportStrategyFactory;
import ch.elexis.core.importer.div.importers.multifile.strategy.DefaultHL7ImportStrategy;
import ch.elexis.core.importer.div.importers.multifile.strategy.IFileImportStrategy;
import ch.elexis.core.importer.div.importers.multifile.strategy.IFileImportStrategyFactory;
import ch.rgw.io.FileTool;

/**
 * Contains strategies for HL7Files ({@link DefaultHL7ImportStrategy}) and their matching PDFFiles (
 * {@link DefaultPDFImportStrategy})<br>
 * Matching of HL7File to PDF takes place via the filename (need to be identical apart from file
 * suffix).
 * 
 * @author lucia
 * 		
 */
public class DefaultImportStrategyFactory extends BasicFileImportStrategyFactory {
	private static final Logger log = LoggerFactory.getLogger(DefaultImportStrategyFactory.class);
	
	private boolean moveAfterImport;
	
	@Override
	public Map<File, IFileImportStrategy> createImportStrategyMap(File hl7File){
		Map<File, IFileImportStrategy> ret = super.createImportStrategyMap(hl7File);
		
		List<File> matchingFiles = getMatchingFiles(hl7File);
		// matching files for this hl7 file, probably pdf
		if (!matchingFiles.isEmpty()) {
			DefaultPDFImportStrategy pdfImportStrategy = new DefaultPDFImportStrategy();
			for (File mFile : matchingFiles) {
				String type = FileTool.getExtension(mFile.getName()).toLowerCase();
				if ("pdf".equals(type)) {
					log.debug("... adding [" + mFile.getName() + "] with DefaultPDFImportStrategy");
					ret.put(mFile, pdfImportStrategy);
				}
			}
		}
			
		ret.values().forEach(strategy -> strategy.setMoveAfterImport(moveAfterImport));
		return ret;
	}
	
	/**
	 * Specify if imported files should be moved to archiv and error directory inside the import
	 * directory. Default is false.
	 * 
	 * @param value
	 * @return
	 */
	public IFileImportStrategyFactory setMoveAfterImport(boolean value){
		this.moveAfterImport = value;
		return this;
	}
}
