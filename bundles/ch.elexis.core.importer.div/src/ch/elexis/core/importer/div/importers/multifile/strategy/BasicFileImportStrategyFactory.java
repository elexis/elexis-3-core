package ch.elexis.core.importer.div.importers.multifile.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;

public abstract class BasicFileImportStrategyFactory implements IFileImportStrategyFactory {

	private static final Logger log = LoggerFactory.getLogger(BasicFileImportStrategyFactory.class);

	@Override
	public Map<IVirtualFilesystemHandle, IFileImportStrategy> createImportStrategyMap(
			IVirtualFilesystemHandle fileHandle) {
		Map<IVirtualFilesystemHandle, IFileImportStrategy> ret = new HashMap<>();
		if (!validateHL7File(fileHandle)) {
			throw new IllegalStateException("File [" + fileHandle + "] is not a processable HL7 File");
		}
		ret.put(fileHandle, new DefaultHL7ImportStrategy());

		return ret;
	}

	protected List<IVirtualFilesystemHandle> getMatchingFiles(IVirtualFilesystemHandle hl7File) throws IOException {
		List<IVirtualFilesystemHandle> matchingFiles = new ArrayList<>();

		String origin = hl7File.getName();
		String seekName = FilenameUtils.getBaseName(origin);

		IVirtualFilesystemHandle directory = hl7File.getParent();
		for (IVirtualFilesystemHandle f : directory.listHandles()) {
			String name = f.getName();
			if (name.startsWith(seekName) && !name.equals(origin)) {
				matchingFiles.add(f);
			}
		}
		log.debug("Found " + matchingFiles.size() + " matching files for HL7File [" + hl7File.getName() + "]");
		return matchingFiles;
	}

	protected boolean validateHL7File(IVirtualFilesystemHandle hl7File) {
		if (hl7File == null) {
			return false;
		}

		try {
			return (hl7File.exists() && hl7File.getExtension().equalsIgnoreCase("hl7"));
		} catch (IOException e) {
			return false;
		}
	}

}
