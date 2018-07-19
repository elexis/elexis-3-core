package ch.elexis.core.importer.div.importers.multifile.strategy;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import ch.elexis.core.importer.div.importers.HL7Parser;
import ch.elexis.core.importer.div.importers.ILabContactResolver;
import ch.elexis.core.importer.div.importers.IPersistenceHandler;
import ch.elexis.core.importer.div.importers.multifile.IMultiFileParser;
import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.ILabResult;
import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;

/**
 * Imports HL7Files using the {@link HL7Parser} and populates the context with the following
 * information: <br>
 * {@link IMultiFileParser#CTX_PATIENT Patient}, {@link IMultiFileParser#CTX_LABID LabId},
 * {@link IMultiFileParser#CTX_GROUP Group}, {@link IMultiFileParser#CTX_PRIO Prio},
 * {@link IMultiFileParser#CTX_TIME Time}
 * 		
 */
public class DefaultHL7ImportStrategy implements IFileImportStrategy {
	
	private boolean testMode;
	
	private boolean moveAfterImport;
	
	private ILabContactResolver labContactResolver;
	
	public static final String CFG_IMPORT_ENCDATA = "hl7Parser/importencdata";
	
	@SuppressWarnings("unchecked")
	@Override
	public Result<Object> execute(File file, Map<String, Object> context, HL7Parser hl7Parser, IPersistenceHandler persistenceHandler) throws IOException{
		String myLab = (String) context.get(IMultiFileParser.CTX_LABNAME);

		Result<Object> result = null;
		
		if (testMode) {
			// we need to enable patient creation when testing otherwise test will fail
			if (labContactResolver != null) {
				result = (Result<Object>) hl7Parser.importFile(file, null, null, labContactResolver,
					true);
			} else {
				result = (Result<Object>) hl7Parser.importFile(file.getAbsolutePath(), true);
			}
			
			if (moveAfterImport) {
				FileImportStrategyUtil.moveAfterImport(result.isOK(), file);
			}
		} else {
			if (labContactResolver != null) {
				result = (Result<Object>) hl7Parser.importFile(file, null, null, labContactResolver,
					false);
			} else {
				result = (Result<Object>) hl7Parser.importFile(file.getAbsolutePath(), false);
			}
			
			if (moveAfterImport) {
				FileImportStrategyUtil.moveAfterImport(result.isOK(), file);
			}
		}
		
		Object resultObj = result.get();
		if (resultObj instanceof String) {
			List<ILabOrder> orders = persistenceHandler.getLabOrdersByOrderId((String) resultObj);
			if (orders != null && !orders.isEmpty()) {
				ILabOrder order = orders.get(0);
				context.put(IMultiFileParser.CTX_PATIENT, order.getPatientContact());
				context.put(IMultiFileParser.CTX_LABID, order.getLabResult().getOriginContact().getId());
				context.put(IMultiFileParser.CTX_GROUP, order.getLabItem().getGroup());
				context.put(IMultiFileParser.CTX_PRIO, order.getLabItem().getPriority());
				context.put(IMultiFileParser.CTX_TIME, getDate(order.getLabResult()));
			}
		}
		return result;
	}
	
	private TimeTool getDate(ILabResult result){
		TimeTool observationTime = result.getObservationTime();
		if (observationTime == null) {
			return new TimeTool(result.getDate());
		}
		return observationTime;
	}
	
	@Override
	public void setTestMode(boolean testing){
		this.testMode = testing;
	}
	
	@Override
	public IFileImportStrategy setMoveAfterImport(boolean value){
		this.moveAfterImport = value;
		return this;
	}
	
	@Override
	public IFileImportStrategy setLabContactResolver(ILabContactResolver resolver){
		this.labContactResolver = resolver;
		return this;
	}
}
