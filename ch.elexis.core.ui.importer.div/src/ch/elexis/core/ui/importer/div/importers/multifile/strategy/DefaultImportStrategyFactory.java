package ch.elexis.core.ui.importer.div.importers.multifile.strategy;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.importer.div.importers.multifile.strategy.BasicFileImportStrategyFactory;
import ch.elexis.core.importer.div.importers.multifile.strategy.DefaultHL7ImportStrategy;
import ch.elexis.core.importer.div.importers.multifile.strategy.IFileImportStrategy;
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
	
	@Override
	public Map<File, IFileImportStrategy> createImportStrategyMap(File hl7File){
		Map<File, IFileImportStrategy> ret = super.createImportStrategyMap(hl7File);
		
		List<File> matchingFiles = getMatchingFiles(hl7File);
		// no matching files for this hl7 file
		if (matchingFiles.isEmpty())
			return ret;
			
		DefaultPDFImportStrategy pdfImportStrategy = new DefaultPDFImportStrategy();
		for (File mFile : matchingFiles) {
			String type = FileTool.getExtension(mFile.getName()).toLowerCase();
			if ("pdf".equals(type)) {
				log.debug("... adding [" + mFile.getName() + "] with DefaultPDFImportStrategy");
				ret.put(mFile, pdfImportStrategy);
			}
		}
		return ret;
	}
	

}
