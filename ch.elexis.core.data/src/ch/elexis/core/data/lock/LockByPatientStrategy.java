package ch.elexis.core.data.lock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import info.elexis.server.elexis.common.types.LockInfo;


/**
 * For first version we lock on patient, resp. whole domain so we should add all
 * dependendent locking elements
 *
 */
public class LockByPatientStrategy {

	public static List<LockInfo> createLockInfoList(Patient patient, String userId) {
		ArrayList<LockInfo> lockList = new ArrayList<>();

		lockList.add(new LockInfo(patient.storeToString(), userId));

		List<LockInfo> bezugskontakte = patient.getBezugsKontakte().stream()
				.map(b -> new LockInfo(b.storeToString(), userId)).collect(Collectors.toList());
		lockList.addAll(bezugskontakte);

		List<LockInfo> faelle = Arrays.asList(patient.getFaelle()).stream()
				.map(l -> new LockInfo(l.storeToString(), userId)).collect(Collectors.toList());
		lockList.addAll(faelle);

		if (faelle.size() > 0) {
			Query<Konsultation> qbeKonsen = new Query<>(Konsultation.class);
			qbeKonsen.startGroup();
			for (LockInfo fall : faelle) {
				qbeKonsen.add(Konsultation.FLD_CASE_ID, Query.LIKE, fall.getElementId());
				qbeKonsen.or();
			}
			qbeKonsen.endGroup();
			List<LockInfo> konsen = qbeKonsen.execute().stream().map(k -> new LockInfo(k.storeToString(), userId))
					.collect(Collectors.toList());
			lockList.addAll(konsen);
		}

		return lockList;
	}

	public static List<LockInfo> createLockInfoList(String storeToString, String userId) {
		PersistentObject po = CoreHub.poFactory.createFromString(storeToString);
		if(po instanceof Patient) {
			return createLockInfoList((Patient) po, userId);
		} else {
			throw new IllegalArgumentException();
		}
	}

}
