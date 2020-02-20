package ch.elexis.core.importer.div.tasks.internal;

import java.io.IOException;
import java.io.Serializable;
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
import ch.rgw.tools.Result.CODE;

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
	 * run parameter: overwrite existing lab results, default: <code>true</code>
	 */
	public static final String RCP_BOOLEAN_OVERWRITE_EXISTING_RESULTS = "overwriteExistingResults";
	/**
	 * run parameter: import encapsulated data, default: <code>true</code>
	 */
	public static final String RCP_BOOLEAN_IMPORT_ENCAPSULATED_DATA = "importEncapsulatedData";
	
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
	public Map<String, Serializable> getDefaultRunContext() {
		Map<String, Serializable> defaultRunContext = new HashMap<>();
		defaultRunContext.put(RunContextParameter.STRING_URL, RunContextParameter.VALUE_MISSING_REQUIRED);
		defaultRunContext.put(RCP_BOOLEAN_CREATE_PATIENT_IF_NOT_EXISTS, Boolean.FALSE);
		defaultRunContext.put(RCP_BOOLEAN_CREATE_LABORATORY_IF_NOT_EXISTS, Boolean.TRUE);
		defaultRunContext.put(RCP_BOOLEAN_MOVE_FILE_AFTER_IMPORT, Boolean.TRUE);
		defaultRunContext.put(RCP_BOOLEAN_OVERWRITE_EXISTING_RESULTS, Boolean.TRUE);
		defaultRunContext.put(RCP_BOOLEAN_IMPORT_ENCAPSULATED_DATA, Boolean.TRUE);
		defaultRunContext.put(RCP_STRING_IMPORTER_LABNAME, "myLab");
		return defaultRunContext;
	}

	@Override
	public Map<String, Serializable> run(Map<String, Serializable> context, IProgressMonitor progressMonitor,
			Logger logger) throws TaskException {

		boolean bCreateLaboratoryIfNotExists = (boolean) context.get(RCP_BOOLEAN_CREATE_LABORATORY_IF_NOT_EXISTS);
		boolean bMoveFile = (boolean) context.get(RCP_BOOLEAN_MOVE_FILE_AFTER_IMPORT);
		boolean importEncData = (boolean) context.get(RCP_BOOLEAN_IMPORT_ENCAPSULATED_DATA);
		boolean overwriteExistingResults = (boolean) context.get(RCP_BOOLEAN_OVERWRITE_EXISTING_RESULTS);
		String urlString = (String) context.get(RunContextParameter.STRING_URL);
		String labName = (String) context.get(RCP_STRING_IMPORTER_LABNAME);

		MyImportHandler myImportHandler = new MyImportHandler(logger, overwriteExistingResults);
		HL7ImporterLabContactResolver labContactResolver = new HL7ImporterLabContactResolver(coreModelService, labimportUtil, logger,
				bCreateLaboratoryIfNotExists);
		IFileImportStrategyFactory importStrategyFactory = new HL7ImportStrategyFactory(logger, myImportHandler)
				.setMoveAfterImport(bMoveFile).setLabContactResolver(labContactResolver);
		MultiFileParser multiFileParser = new MultiFileParser(labName);
		HL7Parser hl7Parser = new HL7Parser(labName, new HL7ImporterPatientResolver(coreModelService, logger),
				labimportUtil, myImportHandler, labContactResolver, importEncData);

		IVirtualFilesystemHandle fileHandle;
		try {
			fileHandle = vfsService.of(urlString);
			Result<?> result = multiFileParser.importFromHandle(fileHandle, importStrategyFactory,
				hl7Parser, new DefaultPersistenceHandler());

			Map<String, Serializable> resultMap = new HashMap<String, Serializable>();
			@SuppressWarnings("rawtypes")
			Result.msg fileUrl = result.removeMsgEntry("url", CODE.URL);
			if(fileUrl != null) {
				resultMap.put(ReturnParameter.STRING_URL, (String) fileUrl.getObject());
			}
			if (!result.isOK()) {
				resultMap.put(ReturnParameter.MARKER_WARN, null);
			}
			resultMap.put(ReturnParameter.RESULT_DATA, result.toString());
			return resultMap;
		} catch (IOException e) {
			throw new TaskException(TaskException.EXECUTION_ERROR, e);
		}
	}

	private class MyImportHandler extends ImportHandler {
		
		private final boolean overwriteExistingResults;
		private final Logger logger;
		
		public MyImportHandler(Logger logger, boolean overwriteExistingResults){
			this.logger = logger;
			this.overwriteExistingResults = overwriteExistingResults;
		}
		
		@Override
		public OverwriteState askOverwrite(IPatient patient, ILabResult oldResult,
			TransientLabResult newResult){
			if (overwriteExistingResults) {
				logger.warn("Overwriting labResult [{}] old value [{}] new value [{}]", patient,
					oldResult, newResult);
				return OverwriteState.OVERWRITE;
			}
			return OverwriteState.IGNORE;
		}
	}

}
