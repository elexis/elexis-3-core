package ch.elexis.core.importer.div.importers.multifile.strategy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rgw.io.FileTool;

public abstract class BasicFileImportStrategyFactory implements IFileImportStrategyFactory {
	
	private static final Logger log = LoggerFactory.getLogger(BasicFileImportStrategyFactory.class);
	
	@Override
	public Map<File, IFileImportStrategy> createImportStrategyMap(File hl7File){
		Map<File, IFileImportStrategy> ret = new HashMap<>();
		if (!validateHL7File(hl7File)) {
			throw new IllegalStateException("File [" + hl7File + "] is not a processable HL7 File");
		}
		ret.put(hl7File, new DefaultHL7ImportStrategy());
		
		return ret;
	}
	
	protected List<File> getMatchingFiles(File hl7File){
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
	
	protected boolean validateHL7File(File hl7File){
		if (hl7File == null || !hl7File.exists()) {
			return false;
		}
		
		if (!(hl7File.getName().toLowerCase().endsWith("hl7"))) {
			return false;
		}
		return true;
	}
	
}
