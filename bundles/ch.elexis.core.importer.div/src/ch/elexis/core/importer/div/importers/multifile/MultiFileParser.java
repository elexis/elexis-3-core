package ch.elexis.core.importer.div.importers.multifile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import ch.elexis.core.importer.div.importers.HL7Parser;
import ch.elexis.core.importer.div.importers.IPersistenceHandler;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.importer.div.importers.multifile.strategy.IFileImportStrategy;
import ch.elexis.core.importer.div.importers.multifile.strategy.IFileImportStrategyFactory;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.services.holder.VirtualFilesystemServiceHolder;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;

public class MultiFileParser implements IMultiFileParser {
	private boolean testMode = false;
	private String myLab;

	public MultiFileParser(String myLab) {
		this.myLab = myLab;
	}

	@Override
	public Result<Object> importFromFile(File hl7File, IFileImportStrategyFactory importStrategyFactory,
			HL7Parser hl7parser, IPersistenceHandler persistenceHandler) {
		try {
			IVirtualFilesystemHandle fileHandle = VirtualFilesystemServiceHolder.get().of(hl7File);
			return importFromHandle(fileHandle, importStrategyFactory, hl7parser, persistenceHandler);
		} catch (IOException e) {
			return new Result<>(SEVERITY.ERROR, null);
		}
	}

	@Override
	public Result<Object> importFromDirectory(File directory, IFileImportStrategyFactory importStrategyFactory,
			HL7Parser hl7parser, IPersistenceHandler persistenceHandler) {
		try {
			IVirtualFilesystemHandle fileHandle = VirtualFilesystemServiceHolder.get().of(directory);
			return importFromHandle(fileHandle, importStrategyFactory, hl7parser, persistenceHandler);
		} catch (IOException e) {
			return new Result<>(e);
		}
	}

	/**
	 * Can be overridden to change order of file import from directory.
	 *
	 * @param iVirtualFilesystemHandles
	 * @return
	 */
	protected IVirtualFilesystemHandle[] sortListHandles(IVirtualFilesystemHandle[] iVirtualFilesystemHandles) {
		Arrays.parallelSort(iVirtualFilesystemHandles, new Comparator<IVirtualFilesystemHandle>() {

			@Override
			public int compare(IVirtualFilesystemHandle left, IVirtualFilesystemHandle right) {
				return left.getName().compareTo(right.getName());
			}
		});
		return iVirtualFilesystemHandles;
	}

	@Override
	public Result<Object> importFromHandle(IVirtualFilesystemHandle fileHandle,
			IFileImportStrategyFactory importStrategyFactory, HL7Parser hl7parser,
			IPersistenceHandler persistenceHandler) {

		boolean isDirectory = false;
		try {
			isDirectory = fileHandle.isDirectory();
		} catch (IOException e) {
			return new Result<>(e);
		}

		if (isDirectory) {
			// directory import
			Result<Object> results = new Result<>();
			try {
				IVirtualFilesystemHandle[] listHandles = sortListHandles(fileHandle.listHandles());
				for (IVirtualFilesystemHandle childHandle : listHandles) {
					if ("hl7".equalsIgnoreCase(childHandle.getExtension())) {
						Result<Object> importFromHandle = importFromHandle(childHandle, importStrategyFactory,
								hl7parser, persistenceHandler);
						results.add(importFromHandle);
					}
				}
				return results;
			} catch (IOException e) {
				return new Result<>(e);
			}

		} else {
			// file import
			Map<String, Object> context = new HashMap<>();
			context.put(CTX_LABNAME, myLab);

			// try to resolving import strategies
			Map<IVirtualFilesystemHandle, IFileImportStrategy> strategyMap = null;
			try {
				strategyMap = importStrategyFactory.createImportStrategyMap(fileHandle);
			} catch (IllegalStateException ise) {
				// file was invalid
				return new Result<>(SEVERITY.ERROR, 1, Messages.MultiFileParser_InvalidFile + ": " + ise.getMessage(),
						fileHandle, true);
			}

			List<IVirtualFilesystemHandle> keys = sortStrategyList(fileHandle, strategyMap);
			Result<Object> results = new Result<>();
			for (IVirtualFilesystemHandle file : keys) {
				IFileImportStrategy importStrategy = strategyMap.get(file);
				importStrategy.setTestMode(testMode);
				try {
					results.add(importStrategy.execute(file, context, hl7parser, persistenceHandler));
				} catch (IOException e) {
					LoggerFactory.getLogger(getClass()).error("Error executing import", e);
					return new Result<>(e);
				}
			}
			return results;
		}
	}

	private List<IVirtualFilesystemHandle> sortStrategyList(IVirtualFilesystemHandle hl7File,
			Map<IVirtualFilesystemHandle, IFileImportStrategy> strategyMap) {
		// ensure hl7 file is imported first
		ArrayList<IVirtualFilesystemHandle> keys = new ArrayList<>(strategyMap.keySet());
		keys.remove(hl7File);
		keys.add(0, hl7File);

		return keys;
	}

	public void setTestMode(boolean testing) {
		testMode = true;
	}

	public boolean inTestMode() {
		return testMode;
	}
}
