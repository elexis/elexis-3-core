package ch.elexis.core.ui.importer.div.importers.multifile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.elexis.core.ui.importer.div.importers.Messages;
import ch.elexis.core.ui.importer.div.importers.multifile.strategy.IFileImportStrategy;
import ch.elexis.core.ui.importer.div.importers.multifile.strategy.IFileImportStrategyFactory;
import ch.rgw.io.FileTool;
import ch.rgw.tools.Result;
import ch.rgw.tools.Result.SEVERITY;

public class MultiFileParser implements IMultiFileParser {
	private boolean testMode = false;
	private String myLab;
	
	public MultiFileParser(String myLab){
		this.myLab = myLab;
	}
	
	@Override
	public Result<Object> importFromFile(File hl7File,
		IFileImportStrategyFactory importStrategyFactory){
		Map<String, Object> context = new HashMap<>();
		context.put(CTX_LABNAME, myLab);
		
		// try to resolving import strategies
		Map<File, IFileImportStrategy> strategyMap = null;
		try {
			strategyMap = importStrategyFactory.createImportStrategyMap(hl7File);
		} catch (IllegalStateException ise) {
			// file was invalid
			return new Result<Object>(SEVERITY.ERROR, 1, Messages.MultiFileParser_InvalidFile,
				hl7File, true);
		}
		
		List<File> keys = sortStrategyList(hl7File, strategyMap);
		Result<Object> results = new Result<>();
		for (File file : keys) {
			IFileImportStrategy importStrategy = strategyMap.get(file);
			importStrategy.setTestMode(testMode);
			results.add(importStrategy.execute(file, context));
		}
		return results;
	}
	
	@Override
	public Result<Object> importFromDirectory(File directory,
		IFileImportStrategyFactory importStrategyFactory){
		Result<Object> results = new Result<>();
		for (File file : directory.listFiles()) {
			String extension = FileTool.getExtension(file.getName()).toLowerCase();
			if (extension.equals("hl7")) {
				results.add(importFromFile(file, importStrategyFactory));
			}
		}
		return results;
	}
	
	private List<File> sortStrategyList(File hl7File, Map<File, IFileImportStrategy> strategyMap){
		// ensure hl7 file is imported first
		ArrayList<File> keys = new ArrayList<File>(strategyMap.keySet());
		keys.remove(hl7File);
		keys.add(0, hl7File);
		
		return keys;
	}
	
	public void setTestMode(boolean testing){
		testMode = true;
	}
	
	public boolean inTestMode(){
		return testMode;
	}
}
