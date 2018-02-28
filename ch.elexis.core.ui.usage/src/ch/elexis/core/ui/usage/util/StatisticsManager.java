package ch.elexis.core.ui.usage.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.time.DateUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.usage.model.IStatistic;
import ch.elexis.core.ui.usage.model.ModelFactory;
import ch.elexis.core.ui.usage.model.Statistics;
import ch.rgw.tools.TimeTool;

public class StatisticsManager {
	
	private Statistics statistics = ModelFactory.eINSTANCE.createStatistics();
	
	private boolean disableAutoExport = false;
	
	// Initialization-on-demand holder idiom
	private static class SingletonHolder {
		public static final StatisticsManager instance = new StatisticsManager();
	}
	
	public static StatisticsManager getInstance(){
		return SingletonHolder.instance;
	}
	
	/**
	 * Creates a new statistics entry for a call of a view or perspective
	 * 
	 * @param action
	 * @param type
	 */
	public void addCallingStatistic(String action, boolean isPerspective){
		String type = isPerspective ? "call: perspective" : "call: view";
		IStatistic lastItem = findLastElementOfType(action, type);
		
		if (lastItem != null) {
			updateStastic(lastItem.getAction() + " -> " + action,
				isPerspective ? "switch: perspective" : "switch: view");
		}
		
		updateStastic(action, type);
		
	}
	
	/**
	 * Creates a new statistics entry for a closing of a view or perspective
	 * 
	 * @param action
	 * @param type
	 */
	public void addClosingStatistic(String action, boolean isPerpsective){
		updateStastic(action, isPerpsective ? "close: perspective" : "close: view");
	}
	
	private void updateStastic(String action, String type){
		Optional<IStatistic> opStastics = statistics.getStatistics().stream()
			.filter(p -> p.getAction().equals(action) && p.getActionType().equals(type))
			.findFirst();
		if (opStastics.isPresent()) {
			opStastics.get().setValue(opStastics.get().getValue() + 1);
			opStastics.get().setTime(new Date(System.currentTimeMillis()));
		} else {
			// create new entry
			IStatistic iStatistic = ModelFactory.eINSTANCE.createSimpleStatistic();
			iStatistic.setTime(new Date());
			iStatistic.setAction(action);
			iStatistic.setActionType(type);
			iStatistic.setValue(1);
			statistics.getStatistics().add(iStatistic);
		}
		statistics.setTo(new Date());
	}
	
	private IStatistic findLastElementOfType(String action, String type){
		List<IStatistic> list = statistics.getStatistics().stream()
			.filter(p -> p.getActionType().equals(type) && !p.getAction().equals(action))
			.collect(Collectors.toList());
		list.sort((p1, p2) -> p2.getTime().compareTo(p1.getTime()));
		if (list.size() > 0) {
			return list.get(0);
		}
		return null;
	}
	
	public Statistics getStatistics(){
		return statistics;
	}
	
	/**
	 * Automatically exports the usage statistics to a file. All files older then 30 days from the
	 * statistics directory will be deleted. This method can only be executed once.
	 * 
	 * @throws IOException
	 */
	public void autoExportStatistics()
		throws IOException{
		TimeTool t = new TimeTool(System.currentTimeMillis());
		String dir = CoreHub.getWritableUserDir().getAbsolutePath() + File.separator + "statistics";
		String fileName = "usage" + t.toString(TimeTool.TIMESTAMP) + ".xml";
		if (!disableAutoExport) {
			try {
				File directory = new File(dir);
				if (directory.isDirectory()) {
					Collection<File> filesToDelete = FileUtils.listFiles(directory,
						new AgeFileFilter(DateUtils.addDays(new Date(), -30)), TrueFileFilter.TRUE);
					for (File file : filesToDelete) {
						boolean success = FileUtils.deleteQuietly(file);
						if (!success) {
							LoggerFactory.getLogger(getClass())
								.warn("Cannot delete old file at: " + file.getAbsolutePath());
						}
					}
				}
			} catch (Exception e) {
				LoggerFactory.getLogger(getClass()).warn("Cannot delete old files.", e);
			}
			exportStatisticsToFile(dir + File.separator + fileName);
			disableAutoExport = true;
		}
	}
	
	public void exportStatisticsToFile(String path) throws IOException{
		if (statistics != null) {
			File toExport = new File(path);
			String content = createXMI();
			if (content != null) {
				FileUtils.writeStringToFile(toExport, content);
			}
		}
	}
	
	private String createXMI(){
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("xmi", new XMIResourceFactoryImpl());
		ResourceSet resSet = new ResourceSetImpl();
		Resource resource = resSet.createResource(URI.createURI("statistics.xml"));
		resource.getContents().add(statistics);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		try {
			resource.save(os, Collections.EMPTY_MAP);
			os.flush();
			String aString = new String(os.toByteArray(), "UTF-8");
			os.close();
			return aString;
		} catch (IOException e) {
			LoggerFactory.getLogger(StatisticsManager.class).error("", e);
		}
		return null;
	}
	
}
