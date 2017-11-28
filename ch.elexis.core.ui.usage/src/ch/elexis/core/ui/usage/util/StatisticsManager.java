package ch.elexis.core.ui.usage.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ui.usage.model.IStatistic;
import ch.elexis.core.ui.usage.model.ModelFactory;
import ch.elexis.core.ui.usage.model.Statistics;

public class StatisticsManager {
	
	private Statistics statistics = ModelFactory.eINSTANCE.createStatistics();
	
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
	public void addCallingStatistic(String action){
		String type = "View Auswahl";
		IStatistic lastItem = findLastElementOfType(action, type);
		
		if (lastItem != null) {
			updateStastic(lastItem.getAction() + " -> " + action, "View Wechsel");
		}
		
		updateStastic(action, type);
		
	}
	
	/**
	 * Creates a new statistics entry for a closing of a view or perspective
	 * 
	 * @param action
	 * @param type
	 */
	public void addClosingStatistic(String action){
		updateStastic(action, "View Schließen");
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
			iStatistic.setTime(new Date(System.currentTimeMillis()));
			iStatistic.setAction(action);
			iStatistic.setActionType(type);
			iStatistic.setValue(1);
			statistics.getStatistics().add(iStatistic);
		}
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
		Resource resource = resSet.createResource(URI.createURI("findingsTemplate.xml"));
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
