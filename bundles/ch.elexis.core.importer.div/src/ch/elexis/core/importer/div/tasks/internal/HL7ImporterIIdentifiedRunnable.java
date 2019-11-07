package ch.elexis.core.importer.div.tasks.internal;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;

import ch.elexis.core.importer.div.importers.DefaultPersistenceHandler;
import ch.elexis.core.importer.div.importers.HL7Parser;
import ch.elexis.core.importer.div.importers.ILabImportUtil;
import ch.elexis.core.importer.div.importers.ImportHandler;
import ch.elexis.core.importer.div.importers.TransientLabResult;
import ch.elexis.core.importer.div.importers.multifile.MultiFileParser;
import ch.elexis.core.importer.div.importers.multifile.strategy.IFileImportStrategyFactory;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.rgw.tools.Result;

/**
 * 
 * @see ch.elexis.laborimport.hl7.automatic.AutomaticImportService
 */
public class HL7ImporterIIdentifiedRunnable implements IIdentifiedRunnable {

	public static final String RUNNABLE_ID = "hl7importer";
	public static final String DESCRIPTION = "Import a single hl7 file from a given url";

	/**
	 * run parameter: the laboratory name to use, defaults to: "myLab"
	 */
	public static final String RCP_STRING_IMPORTER_LABNAME = "labName";
	/**
	 * run parameter: create patient if not exists, default: <code>false</code>
	 */
	public static final String RCP_BOOLEAN_CREATE_PATIENT_IF_NOT_EXISTS = "createPatientIfNotExists";
	/**
	 * run parameter: create laboratory if not exists, default: <code>false</code>
	 */
	public static final String RCP_BOOLEAN_CREATE_LABORATORY_IF_NOT_EXISTS = "createLaboratoryIfNotExists";

	/**
	 * run parameter: move hl7 file after successful import, default:
	 * <code>true</code>
	 */
	public static final String RCP_BOOLEAN_MOVE_FILE_AFTER_IMPORT = "moveFile";

	private ILabImportUtil labimportUtil;
	private IModelService coreModelService;
	private IVirtualFilesystemService vfsService;

	public HL7ImporterIIdentifiedRunnable(IModelService coreModelService, ILabImportUtil labimportUtil,
			IVirtualFilesystemService vfsService) {
		this.coreModelService = coreModelService;
		this.labimportUtil = labimportUtil;
		this.vfsService = vfsService;
	}

	@Override
	public String getId() {
		return RUNNABLE_ID;
	}

	@Override
	public String getLocalizedDescription() {
		return DESCRIPTION;
	}

	@Override
	public Map<String, String> getDefaultRunContext() {
		Map<String, String> defaultRunContext = new HashMap<>();
		defaultRunContext.put(RunContextParameter.STRING_URL, RunContextParameter.VALUE_MISSING_REQUIRED);
		defaultRunContext.put(RCP_BOOLEAN_CREATE_PATIENT_IF_NOT_EXISTS, Boolean.toString(false));
		defaultRunContext.put(RCP_BOOLEAN_CREATE_LABORATORY_IF_NOT_EXISTS, Boolean.toString(true));
		defaultRunContext.put(RCP_BOOLEAN_MOVE_FILE_AFTER_IMPORT, Boolean.toString(true));
		defaultRunContext.put(RCP_STRING_IMPORTER_LABNAME, "myLab");
		return defaultRunContext;
	}

	@Override
	public Map<String, Serializable> run(Map<String, Serializable> context, IProgressMonitor progressMonitor,
			Logger logger) throws TaskException {

		boolean bCreateLaboratoryIfNotExists = Boolean.valueOf((String) context.get(RCP_BOOLEAN_CREATE_LABORATORY_IF_NOT_EXISTS));
		boolean bMoveFile = Boolean.valueOf((String) context.get(RCP_BOOLEAN_MOVE_FILE_AFTER_IMPORT));
		String urlString = (String) context.get(RunContextParameter.STRING_URL);
		String labName = (String) context.get(RCP_STRING_IMPORTER_LABNAME);

		// TODO make configurable
		final boolean CFG_IMPORT_ENCDATA = false;

		MyImportHandler myImportHandler = new MyImportHandler();
		HL7ImporterLabContactResolver labContactResolver = new HL7ImporterLabContactResolver(coreModelService, logger,
				bCreateLaboratoryIfNotExists);
		IFileImportStrategyFactory importStrategyFactory = new HL7ImportStrategyFactory(logger, myImportHandler)
				.setMoveAfterImport(bMoveFile).setLabContactResolver(labContactResolver);
		MultiFileParser multiFileParser = new MultiFileParser(labName);
		HL7Parser hl7Parser = new HL7Parser(labName, new HL7ImporterPatientResolver(coreModelService, logger),
				labimportUtil, myImportHandler, labContactResolver, CFG_IMPORT_ENCDATA);

		IVirtualFilesystemHandle fileHandle;
		try {
			fileHandle = vfsService.of(urlString);
			Result<?> result = multiFileParser.importFromHandle(fileHandle, importStrategyFactory, hl7Parser,
					new DefaultPersistenceHandler());
			if(!result.isOK()) {
				throw new TaskException(TaskException.EXECUTION_ERROR, result.toString());
			}
			return Collections.singletonMap(IIdentifiedRunnable.ReturnParameter.RESULT_DATA, result.toString());
		} catch (IOException e) {
			throw new TaskException(TaskException.EXECUTION_ERROR, e);
		}
	}

	private class MyImportHandler extends ImportHandler {

		@Override
		public OverwriteState askOverwrite(IPatient patient, ILabResult oldResult, TransientLabResult newResult) {
			// TODO make configurable
			return null;
		}
	}

}
