package ch.elexis.core.ui.importer.div.importers.multifile.strategy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class DefaultImportStrategyFactory implements IFileImportStrategyFactory {
	private static final Logger log = LoggerFactory.getLogger(DefaultImportStrategyFactory.class);
	
	@Override
	public Map<File, IFileImportStrategy> createImportStrategyMap(File hl7File){
		Map<File, IFileImportStrategy> ret = new HashMap<>();
		if (!validateHL7File(hl7File)) {
			throw new IllegalStateException("File [" + hl7File + "] is not a processable HL7 File");
		}
		ret.put(hl7File, new DefaultHL7ImportStrategy());
		
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
	
	private List<File> getMatchingFiles(File hl7File){
		List<File> matchingFiles = new ArrayList<File>();
		
		String origin = hl7File.getName();
		String seekName = FileTool.getNakedFilename(origin);
		
		File directory = hl7File.getParentFile();
		for (File f : directory.listFiles()) {
			String name = f.getName();
			if (name.startsWith(seekName) && !name.equals(origin)) {
				matchingFiles.add(f);
			}
		}
		log.debug("Found " + matchingFiles.size() + " matching files for HL7File ["
			+ hl7File.getName() + "]");
		return matchingFiles;
	}
	
	private boolean validateHL7File(File hl7File){
		if (hl7File == null || !hl7File.exists()) {
			return false;
		}
		
		if (!(hl7File.getName().toLowerCase().endsWith("hl7"))) {
			return false;
		}
		return true;
	}
}
