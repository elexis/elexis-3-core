package ch.elexis.core.findings.templates.ui.dataaccess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.interfaces.IDataAccess;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.templates.model.FindingsTemplate;
import ch.elexis.core.findings.templates.model.FindingsTemplates;
import ch.elexis.core.findings.templates.model.InputDataGroup;
import ch.elexis.core.findings.templates.model.InputDataGroupComponent;
import ch.elexis.core.findings.templates.ui.util.FindingsServiceHolder;
import ch.elexis.data.DBConnection;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;

public class FindingDataAccessor implements IDataAccess {
	
	private static final String FIRST = "first";
	private static final String LAST = "last";
	private static final String ALL = "all";
	
	private static final String PREFIX_FIRST = "[Befunde-Neu:Patient:first:";
	private static final String PREFIX_LAST = "[Befunde-Neu:Patient:last:";
	private static final String PREFIX_ALL = "[Befunde-Neu:Patient:all:";
	private static final String SUFFIX = "]";
	
	private IDataAccess.Element createElement(String readableName, String placeholder){
		return new IDataAccess.Element(IDataAccess.TYPE.STRING, readableName, placeholder,
			Patient.class, 1);
	}
	
	@Override
	public String getName(){
		return "Befunde-Neu";
	}
	
	@Override
	public String getDescription(){
		return "Befunde-Neu";
	}
	
	@Override
	public List<Element> getList(){
		FindingsTemplates findingsTemplates =
			FindingsServiceHolder.findingsTemplateService.getFindingsTemplates("Standard Vorlagen");
		
		List<String> parameters = new ArrayList<>();
		EList<FindingsTemplate> findingsTemplates2 = findingsTemplates.getFindingsTemplates();
		// sort
		ECollections.sort(findingsTemplates2, new Comparator<FindingsTemplate>() {
			
			@Override
			public int compare(FindingsTemplate o1, FindingsTemplate o2){
				if (o1 == null || o2 == null) {
					return o1 != null ? 1 : -1;
				}
				return StringUtils.lowerCase(o1.getTitle())
					.compareTo(StringUtils.lowerCase(o2.getTitle()));
			}
		});
		
		for (FindingsTemplate findingTemplate : findingsTemplates2) {
			if (findingTemplate.getInputData() instanceof InputDataGroupComponent
				|| findingTemplate.getInputData() instanceof InputDataGroup) {
				parameters.add(findingTemplate.getTitle());
			}
		}
		List<Element> ret = new ArrayList<Element>(parameters.size());
		for (String n : parameters) {
			// placeholder for first finding
			String placeholder = PREFIX_FIRST + n + SUFFIX;
			String readableName = n + " - " + "Erster";
			ret.add(createElement(readableName, placeholder));
			
			// placeholder for last finding
			placeholder = PREFIX_LAST + n + SUFFIX;
			readableName = n + " - " + "Letzter";
			ret.add(createElement(readableName, placeholder));
			
			// placeholder for all findings
			placeholder = PREFIX_ALL + n + SUFFIX;
			readableName = n + " - " + "Alle";
			ret.add(createElement(readableName, placeholder));
		}
		return ret;
	}
	
	@Override
	public Result<Object> getObject(String descriptor, PersistentObject dependentObject, String key,
		String[] params){
		Result<Object> ret = null;
		if (!(dependentObject instanceof Patient)) {
			ret = new Result<Object>(Result.SEVERITY.ERROR, IDataAccess.INVALID_PARAMETERS,
				"Ungültiger Parameter", //$NON-NLS-1$
				dependentObject, true);
		} else {
			DBConnection dbConnection = null;
			PreparedStatement preparedStatement = null;
			
			try {
				
				Patient pat = (Patient) dependentObject;
				
				if (!(ALL.equals(key) || FIRST.equals(key) || LAST.equals(key))) {
					return new Result<Object>(Result.SEVERITY.ERROR, IDataAccess.OBJECT_NOT_FOUND,
						"Fehler beim Parsen der Befund-Daten. Prefix " + key + " nicht bekannt.",
						params, true);
				}
				dbConnection = PersistentObject.getDefaultConnection();
				preparedStatement = dbConnection.getPreparedStatement(
					"select id from CH_ELEXIS_CORE_FINDINGS_OBSERVATION where deleted = '0' and referenced = '0' and patientId = ? and content like ? and content like ?");
				preparedStatement.setString(1, pat.getId());
				preparedStatement.setString(2, "%\"code\":\"" + descriptor + "\"%");
				preparedStatement.setString(3, "%\"system\":\"www.elexis.info/coding/local\"%");
				
				// TODO last updat eeffektive time
				
				ResultSet res = preparedStatement.executeQuery();
				
				List<IObservation> observations = new ArrayList<>();
				while ((res != null) && (res.next() == true)) {
					String observationId = res.getString(1);
					IObservation iObservation = FindingsServiceHolder.findingsService
						.findById(observationId, IObservation.class).orElse(null);
					if (iObservation != null) {
						observations.add(iObservation);
					}
				}
				
				Collections.sort(observations, new Comparator<IObservation>() {
					public int compare(IObservation o1, IObservation o2){
						Optional<LocalDateTime> d1 = o1.getEffectiveTime();
						Optional<LocalDateTime> d2 = o2.getEffectiveTime();
						
						if (d1.isPresent() && d2.isPresent()) {
							return d1.get().isAfter(d2.get()) ? 1
									: (d1.get().equals(d2.get()) ? 0 : -1);
						} else if (d1.isPresent()) {
							return 1;
						} else if (d2.isPresent()) {
							return -1;
						} else {
							return 0;
						}
					};
				});
				
				StringBuilder textBuilder = new StringBuilder();
				if (!observations.isEmpty()) {
					if (FIRST.equals(key)) {
						IObservation iObservation = observations.get(0);
						observations.clear();
						observations.add(iObservation);
					} else if (LAST.equals(key)) {
						IObservation iObservation = observations.get(observations.size() - 1);
						observations.clear();
						observations.add(iObservation);
					}
					
					for (IObservation iObservation : observations) {
						if (iObservation != null) {
							if (textBuilder.length() > 0) {
								textBuilder.append(System.lineSeparator());
							}
							iObservation.getText().ifPresent(txt -> {
								iObservation.getEffectiveTime().ifPresent(date -> {
									textBuilder
										.append(new TimeTool(date).toString(TimeTool.LARGE_GER));
									textBuilder.append(" ");
								});
								textBuilder.append(txt);
							});
						}
					}
				}
				
				ret = new Result<Object>(textBuilder.toString());
			} catch (Exception e) {
				LoggerFactory.getLogger(FindingDataAccessor.class).error("parse error", e);
				return new Result<Object>(Result.SEVERITY.ERROR, IDataAccess.OBJECT_NOT_FOUND,
					"Fehler beim Parsen der Befunde-Daten", params, true);
			} finally {
				if (dbConnection != null && preparedStatement != null) {
					dbConnection.releasePreparedStatement(preparedStatement);
				}
			}
		}
		return ret;
	}
	
}
