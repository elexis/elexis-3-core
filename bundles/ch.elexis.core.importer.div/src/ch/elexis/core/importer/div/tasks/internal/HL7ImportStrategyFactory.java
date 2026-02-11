package ch.elexis.core.importer.div.tasks.internal;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import ch.elexis.core.importer.div.importers.ILabContactResolver;
import ch.elexis.core.importer.div.importers.ImportHandler;
import ch.elexis.core.importer.div.importers.multifile.strategy.BasicFileImportStrategyFactory;
import ch.elexis.core.importer.div.importers.multifile.strategy.DefaultPDFImportStrategy;
import ch.elexis.core.importer.div.importers.multifile.strategy.IFileImportStrategy;
import ch.elexis.core.importer.div.importers.multifile.strategy.IFileImportStrategyFactory;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;

public class HL7ImportStrategyFactory extends BasicFileImportStrategyFactory {

	private boolean moveAfterImport;

	private ILabContactResolver labContactResolver;

	private Logger logger;

	private ImportHandler importHandler;

	public HL7ImportStrategyFactory(Logger logger, ImportHandler importHandler) {
		this.logger = logger;
		this.importHandler = importHandler;
	}

	@Override
	public Map<IVirtualFilesystemHandle, IFileImportStrategy> createImportStrategyMap(
			IVirtualFilesystemHandle hl7File) {
		Map<IVirtualFilesystemHandle, IFileImportStrategy> ret = super.createImportStrategyMap(hl7File);

		List<IVirtualFilesystemHandle> matchingFiles;
		try {
			matchingFiles = getMatchingFiles(hl7File);
		} catch (IOException e) {
			// masquerade, as this exception is already catched upstream
			throw new IllegalStateException(e);
		}
		// matching files for this hl7 file, probably pdf
		if (!matchingFiles.isEmpty()) {
			DefaultPDFImportStrategy pdfImportStrategy = new DefaultPDFImportStrategy(importHandler);
			for (IVirtualFilesystemHandle mFile : matchingFiles) {
				String type = FilenameUtils.getExtension(mFile.getName()).toLowerCase();
				if ("pdf".equals(type)) {
					logger.debug("... adding [" + mFile.getName() + "] with DefaultPDFImportStrategy");
					ret.put(mFile, pdfImportStrategy);
				}
			}
		}

		ret.values().forEach(
				strategy -> strategy.setMoveAfterImport(moveAfterImport).setLabContactResolver(labContactResolver));
		return ret;
	}

	@Override
	public IFileImportStrategyFactory setLabContactResolver(ILabContactResolver resolver) {
		this.labContactResolver = resolver;
		return this;
	}

	@Override
	public IFileImportStrategyFactory setMoveAfterImport(boolean value) {
		this.moveAfterImport = value;
		return this;
	}

}
