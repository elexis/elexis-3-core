package ch.elexis.core.importer.div.importers.multifile;

import java.io.File;

import ch.elexis.core.importer.div.importers.HL7Parser;
import ch.elexis.core.importer.div.importers.IPersistenceHandler;
import ch.elexis.core.importer.div.importers.multifile.strategy.IFileImportStrategyFactory;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.rgw.tools.Result;

public interface IMultiFileParser {

	public static final String CTX_LABNAME = "labname";
	public static final String CTX_PATIENT = "patient";
	public static final String CTX_LABID = "labid";
	public static final String CTX_GROUP = "group";
	public static final String CTX_PRIO = "prio";
	public static final String CTX_TIME = "time";

	/**
	 * import a hl7 file following the instructions from the
	 * {@link IFileImportStrategyFactory}
	 *
	 * @param hl7File               a hl7File
	 * @param importStrategyFactory a {@link IFileImportStrategyFactory}
	 *                              implementation. <br>
	 *                              There is a default implementation
	 *                              {@link DefaultImportStrategyFactory} that
	 *                              imports HL7Files (using the {@link HL7Parser}
	 *                              and it's connected PDF files
	 * @return a {@link Result} indicating whether import succeeded or not
	 */
	public Result<Object> importFromFile(File hl7File, IFileImportStrategyFactory importStrategyFactory,
			HL7Parser hl7parser, IPersistenceHandler persistenceHandler);

	/**
	 * Imports all HL7Files from the given directory. <br>
	 * Calls
	 * {@linkplain IMultiFileParser#importFromFile(File, IFileImportStrategyFactory)}
	 * for each file (check this Method for further information).
	 *
	 * @param directory             some directory
	 * @param importStrategyFactory a {@link IFileImportStrategyFactory}
	 *                              implementation. <br>
	 *                              There is a default implementation
	 *                              {@link DefaultImportStrategyFactory} that
	 *                              imports HL7Files (using the {@link HL7Parser}
	 *                              and it's connected PDF files
	 * @return a {@link Result} indicating whether import succeeded or not
	 */
	public Result<Object> importFromDirectory(File directory, IFileImportStrategyFactory importStrategyFactory,
			HL7Parser hl7parser, IPersistenceHandler persistenceHandler);

	/**
	 *
	 * @param urlString
	 * @param vfsService
	 * @param importStrategyFactory
	 * @param hl7parser
	 * @param persistenceHandler
	 * @return
	 */
	public Result<Object> importFromHandle(IVirtualFilesystemHandle fileHandle,
			IFileImportStrategyFactory importStrategyFactory, HL7Parser hl7parser,
			IPersistenceHandler persistenceHandler);
}
