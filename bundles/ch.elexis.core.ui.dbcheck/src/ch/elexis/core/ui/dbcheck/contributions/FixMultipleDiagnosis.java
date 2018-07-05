package ch.elexis.core.ui.dbcheck.contributions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.elexis.core.model.IDiagnose;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Konsultation;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;

public class FixMultipleDiagnosis extends ExternalMaintenance {
	
	private Logger log = LoggerFactory.getLogger(FixMultipleDiagnosis.class);
	
	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion){
		StringBuilder sb = new StringBuilder();
		boolean resultOk = true;
		
		Map<String, ArrayList<IDiagnose>> diagMap = new HashMap<>();
		Set<String> uniqueDiagnosen = new HashSet<>();
		
		File file = new File(CoreHub.getWritableUserDir(), "5579_diagnosisFix.csv");
		try (FileWriter fw = new FileWriter(file)) {
			Query<Konsultation> qre = new Query<Konsultation>(Konsultation.class);
			qre.clear(true);
			
			List<Konsultation> execute = qre.execute();
			pm.beginTask("Fixing multiple Diagnoses", execute.size() * 3);
			pm.subTask("Collection diagnosis information");
			for (Konsultation konsultation : execute) {
				ArrayList<IDiagnose> diagnosen = konsultation.getDiagnosen();
				Set<String> uniqueDiagnosenThisCons = new HashSet<>();
				if (diagnosen.size() > 0) {
					fw.write(konsultation.getId() + ";");
					for (IDiagnose iDiagnose : diagnosen) {
						String diagnoseSts = iDiagnose.getClass().getName()
							+ StringConstants.DOUBLECOLON + iDiagnose.getCode();
						uniqueDiagnosen.add(diagnoseSts);
						uniqueDiagnosenThisCons.add(diagnoseSts);
					}
					for (String diagnoseSts : uniqueDiagnosenThisCons) {
						fw.write(diagnoseSts + ";");
					}
					diagMap.put(konsultation.getId(), diagnosen);
					fw.write("\n");
				} else {
					sb.append("[" + konsultation.getId() + "] 0 diagnosis entries\n");
				}
				
				pm.worked(1);
			}
			
		} catch (Exception e) {
			log.error("Error backing up diagnosis", e);
			pm.done();
			return e.getMessage();
		}
		
		StringBuilder sql = new StringBuilder(200);
		sql.append("DELETE FROM BEHDL_DG_JOINT WHERE 1 = 1");
		PersistentObject.getConnection().exec(sql.toString());
		
		sql = new StringBuilder(200);
		sql.append("DELETE FROM DIAGNOSEN WHERE 1 = 1");
		PersistentObject.getConnection().exec(sql.toString());
		
		pm.subTask("Setting diagnosis information");
		Set<String> keySet = diagMap.keySet();
		for (String key : keySet) {
			Konsultation kons = Konsultation.load(key);
			ArrayList<IDiagnose> arrayList = diagMap.get(key);
			for (IDiagnose iDiagnose : arrayList) {
				addDiagnose(kons, iDiagnose);
			}
			pm.worked(1);
		}
		
		pm.subTask("Validating diagnosis against backup file");
		try {
			List<String> entries = Files.readAllLines(file.toPath());
			for (String line : entries) {
				String[] split = line.split(";");
				List<String> diagnosisEntries =
					Arrays.asList(Arrays.copyOfRange(split, 1, split.length));
				Konsultation kons = Konsultation.load(split[0]);
				ArrayList<IDiagnose> diagnosen = kons.getDiagnosen();
				int should = diagnosisEntries.size();
				if (diagnosen.size() != should) {
					sb.append("[" + split[0] + "] # of diagnosis entries mismatch " + should + " "
						+ diagnosen.size() + "\n");
					resultOk = false;
				} else {
					for (IDiagnose iDiagnose : diagnosen) {
						if (!diagnosisEntries.contains(iDiagnose.getClass().getName()
							+ StringConstants.DOUBLECOLON + iDiagnose.getCode())) {
							sb.append("[" + split[0] + "] missing " + iDiagnose.getClass().getName()
								+ StringConstants.DOUBLECOLON + iDiagnose.getCode()
								+ "\n");
							resultOk = false;
						}
					}
				}
				pm.worked(1);
			}
		} catch (IOException e) {
			log.error("Could not validate", e);
			sb.append("Could not validate diagnosis entries " + e.getMessage());
		}
		pm.done();
		
		String count =
			PersistentObject.getConnection().queryString("SELECT COUNT(*) FROM DIAGNOSEN");
		int parseLong = Integer.parseInt(count);
		
		sb.append("--> No of unique diagnoses matches: "
			+ Boolean.toString(parseLong == uniqueDiagnosen.size()));
		sb.append("Result is equivalent " + resultOk + "\n");
		return sb.toString();
	}
	
	Set<String> uniqueSet = new HashSet<>();
	
	private void addDiagnose(Konsultation kons, IDiagnose dg){
		String dgid = prepareDiagnoseSelectWithCodeAndClass(kons.getId(), dg.getCode(),
			dg.getClass().getName());
		if (dgid != null) {
			return;
		}
		
		String diagnosisEntryExists = PersistentObject.getConnection().queryString(
			"SELECT ID FROM DIAGNOSEN WHERE KLASSE=" + JdbcLink.wrap(dg.getClass().getName())
				+ " AND DG_CODE=" + JdbcLink.wrap(dg.getCode()));
		StringBuilder sql = new StringBuilder(200);
		if (StringTool.isNothing(diagnosisEntryExists)) {
			diagnosisEntryExists = StringTool.unique("bhdl");
			sql.append("INSERT INTO DIAGNOSEN (ID, LASTUPDATE, DG_CODE, DG_TXT, KLASSE) VALUES (")
				.append(JdbcLink.wrap(diagnosisEntryExists)).append(",")
				.append(Long.toString(System.currentTimeMillis())).append(",")
				.append(JdbcLink.wrap(dg.getCode())).append(",").append(JdbcLink.wrap(dg.getText()))
				.append(",").append(JdbcLink.wrap(dg.getClass().getName())).append(")");
			PersistentObject.getConnection().exec(sql.toString());
			sql.setLength(0);
		}
		/**
		 * @deprecated remove ID,lastupdate,deleted in 3.3
		 * @see https://redmine.medelexis.ch/issues/5629
		 */
		sql.append("INSERT INTO BEHDL_DG_JOINT (ID,BEHANDLUNGSID,DIAGNOSEID) VALUES (")
			.append(JdbcLink.wrap(StringTool.unique("bhdx"))).append(",")
			.append(kons.getWrappedId()).append(",").append(JdbcLink.wrap(diagnosisEntryExists))
			.append(")");
		PersistentObject.getConnection().exec(sql.toString());
	}
	
	private final String STM_S_BDJ = "SELECT BDJ.DiagnoseId FROM BEHDL_DG_JOINT BDJ, DIAGNOSEN D"
		+ " WHERE BDJ.BehandlungsID=? AND D.ID = BDJ.DiagnoseID AND D.DG_CODE=? AND D.KLASSE=?;";
	
	private String prepareDiagnoseSelectWithCodeAndClass(String konsId, String code,
		String classname){
		PreparedStatement pst = PersistentObject.getConnection().getPreparedStatement(STM_S_BDJ);
		try {
			pst.setString(1, konsId);
			pst.setString(2, code);
			pst.setString(3, classname);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				return rs.getString(1);
			}
		} catch (SQLException e) {
			MessageEvent.fireError("Fehler beim Löschen", e.getMessage(), e);
			log.error("Error deleting diagnosis", e);
		} finally {
			PersistentObject.getConnection().releasePreparedStatement(pst);
		}
		return null;
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "[5579] Fix multiple diagnosis entry creation";
	}
	
}
