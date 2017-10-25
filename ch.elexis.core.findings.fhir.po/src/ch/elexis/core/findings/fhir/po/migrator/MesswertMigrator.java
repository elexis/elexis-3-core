package ch.elexis.core.findings.fhir.po.migrator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.befunde.Messwert;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationType;
import ch.elexis.core.findings.UriType;
import ch.elexis.core.findings.fhir.po.migrator.messwert.MesswertFieldMapping;
import ch.elexis.core.findings.fhir.po.migrator.strategy.IMigrationStrategy;
import ch.elexis.core.findings.fhir.po.migrator.strategy.MesswertMigrationStrategyFactory;
import ch.elexis.core.findings.fhir.po.model.Observation;
import ch.elexis.core.findings.templates.service.FindingsTemplateService;
import ch.elexis.core.findings.util.FindingsTextUtil;
import ch.elexis.data.DBConnection;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.TimeTool;

public class MesswertMigrator {
	
	private static Logger logger = LoggerFactory.getLogger(MigratorService.class);
	
	public static boolean isMesswertAvailable(){
		try {
			Class<?> clazz =
				MesswertMigrator.class.getClassLoader().loadClass("ch.elexis.befunde.Messwert");
			return clazz != null;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
	
	private Optional<FindingsTemplateService> findingsTemplateService;
	
	private Optional<IFindingsService> findingsService;
	
	public MesswertMigrator(){
		initialize();
	}
	
	private void initialize(){
		MesswertMigrationStrategyFactory.clearCodeToTemplateCache();
		
		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
		ServiceReference<FindingsTemplateService> templateServiceReference =
			context.getServiceReference(FindingsTemplateService.class);
		if (templateServiceReference != null) {
			FindingsTemplateService service = context.getService(templateServiceReference);
			if (service != null) {
				findingsTemplateService = Optional.of(service);
				MesswertMigrationStrategyFactory.setFindingsTemplateService(service);
			}
		} else {
			findingsTemplateService = Optional.empty();
		}
		
		ServiceReference<IFindingsService> findingsServiceReference =
			context.getServiceReference(IFindingsService.class);
		if (findingsServiceReference != null) {
			IFindingsService service = context.getService(findingsServiceReference);
			if (service != null) {
				findingsService = Optional.of(service);
			}
		} else {
			findingsService = Optional.empty();
		}
	}
	
	/**
	 * Test if the migrator initilization was successful.
	 * 
	 * @return
	 */
	public boolean initialized(){
		return findingsTemplateService.isPresent() && findingsService.isPresent();
	}
	
	/**
	 * Try to migrate all Messwert values of the patient to {@link IObservation} instances. If all
	 * values are migrated the Messwert is marked. If the migration of one value fails, none of the
	 * values is migrated to {@link IObservation}.
	 * 
	 * @param patientId
	 */
	public void migratePatientMesswerte(String patientId){
		Map<String, MesswertFieldMapping> mappingsMap =
			buildMappingsMap(MesswertFieldMapping.getMappings());
		for (Messwert messwert : getMesswerte(patientId)) {
			migrateMesswert(messwert, mappingsMap);
		}
	}
	
	private Map<String, MesswertFieldMapping> buildMappingsMap(List<MesswertFieldMapping> list){
		HashMap<String, MesswertFieldMapping> ret = new HashMap<>();
		for (MesswertFieldMapping messwertFieldMapping : list) {
			ret.put(
				messwertFieldMapping.getLocalBefund() + messwertFieldMapping.getLocalBefundField(),
				messwertFieldMapping);
		}
		return ret;
	}
	
	private void migrateMesswert(Messwert messwert, Map<String, MesswertFieldMapping> mappingsMap){
		String name = messwert.get(Messwert.FLD_NAME);
		TimeTool timeTool = new TimeTool();
		List<IObservation> observations = new ArrayList<>();
		boolean migrationError = false;
		if (isNotMigrated(messwert)) {
			@SuppressWarnings("unchecked")
			Map<Object, Object> values = messwert.getMap(Messwert.FLD_BEFUNDE);
			for (Object key : values.keySet()) {
				MesswertFieldMapping mapping = mappingsMap.get(name + ((String) key));
				if (mapping != null) {
					Optional<IObservation> observation =
						migrateMesswert(messwert, mapping, observations);
					if (!observation.isPresent()) {
						migrationError = true;
						break;
					} else {
						timeTool.set(messwert.getDate());
						observation.get().setEffectiveTime(timeTool.toLocalDateTime());
						observation.get()
							.setOriginUri(UriType.DB.toString(messwert.storeToString()));
						observations.add(observation.get());
						ObservationType observationType = observation.get().getObservationType();
						if (observationType == ObservationType.REF) {
							observation.get()
								.setText(FindingsTextUtil.getGroupText(observation.get()));
						} else {
							observation.get()
								.setText(FindingsTextUtil.getObservationText(observation.get()));
						}
					}
				} else {
					logger.warn("No mapping for [" + name + ((String) key) + "], not migrated");
				}
			}
			if (migrationError) {
				// delete all created Observations og this Messwert
				deleteObservations(observations);
			}
		}
	}
	
	private void deleteObservations(List<IObservation> observations){
		if (!observations.isEmpty()) {
			DBConnection connection = PersistentObject.getDefaultConnection();
			Stm stm = connection.getStatement();
			try {
				for (IObservation observation : observations) {
					stm.exec("DELETE FROM CH_ELEXIS_CORE_FINDINGS_OBSERVATION WHERE ID='"
						+ observation.getId() + "';");
				}
			} finally {
				connection.releaseStatement(stm);
			}
		}
	}
	
	private Optional<IObservation> migrateMesswert(Messwert messwert, MesswertFieldMapping mapping,
		List<IObservation> createdObservations){
		String result = messwert.getResult(mapping.getLocalBefundField());
		if (result != null && !result.isEmpty()) {
			IMigrationStrategy strategy =
				MesswertMigrationStrategyFactory.get(mapping, messwert, createdObservations);
			return strategy.migrate();
		}
		return Optional.empty();
	}
	
	private List<Messwert> getMesswerte(String patientId){
		Query<Messwert> query = new Query<Messwert>(Messwert.class);
		query.add(Messwert.FLD_PATIENT_ID, Query.EQUALS, patientId);
		return query.execute();
	}
	
	private boolean isNotMigrated(Messwert messwert){
		return lookupMigratedObservations(UriType.DB.toString(messwert.storeToString())).isEmpty();
	}
	
	private List<IObservation> lookupMigratedObservations(String originuri){
		ArrayList<IObservation> ret = new ArrayList<>();
		Stm stm = PersistentObject.getDefaultConnection().getStatement();
		if (stm != null) {
			try {
				ResultSet result = stm
					.query("SELECT ID FROM CH_ELEXIS_CORE_FINDINGS_OBSERVATION WHERE originuri = '"
						+ originuri + "';");
				while ((result != null) && result.next()) {
					String id = result.getString(1);
					ret.add(Observation.load(id));
				}
			} catch (SQLException e) {
				LoggerFactory.getLogger(getClass()).error("Error on migrated lookup", e);
			} finally {
				PersistentObject.getDefaultConnection().releaseStatement(stm);
			}
		}
		return ret;
	}
}
