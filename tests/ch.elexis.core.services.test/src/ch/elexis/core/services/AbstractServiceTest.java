package ch.elexis.core.services;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.ICoverageBuilder;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.types.Gender;
import ch.rgw.tools.TimeTool;

public abstract class AbstractServiceTest {

	static IModelService coreModelService = AllServiceTests.getModelService();

	public List<IMandator> testMandators = new ArrayList<IMandator>();
	public List<IPatient> testPatients = new ArrayList<IPatient>();
	public List<ICoverage> testCoverages = new ArrayList<ICoverage>();
	public List<IEncounter> testEncounters = new ArrayList<IEncounter>();

	public void createTestMandantPatientFallBehandlung() {
		TimeTool timeTool = new TimeTool();
		IPerson _mandator = new IContactBuilder.PersonBuilder(coreModelService, "mandator1 " + timeTool.toString(),
				"Anton" + timeTool.toString(), timeTool.toLocalDate(), Gender.MALE).mandator().buildAndSave();
		IMandator mandator = coreModelService.load(_mandator.getId(), IMandator.class).get();
		testMandators.add(mandator);

		IPatient patient = new IContactBuilder.PatientBuilder(coreModelService, "Armer", "Anton" + timeTool.toString(),
				timeTool.toLocalDate(), Gender.MALE).buildAndSave();
		testPatients.add(patient);

		ICoverage testCoverage = new ICoverageBuilder(coreModelService, patient, "Fallbezeichnung", "Fallgrund", "KVG")
				.buildAndSave();
		testCoverage.setExtInfo("Versicherungsnummer", "12340815"); // KVG requirement for billing
		coreModelService.save(testCoverage);
		testCoverages.add(testCoverage);

		IEncounter behandlung = new IEncounterBuilder(coreModelService, testCoverage, mandator).buildAndSave();
		testEncounters.add(behandlung);
	}

	/**
	 * remove the test setting generated via
	 * {@link #createTestMandantPatientFallBehandlung()}, and clean the
	 * {@link IContext} from its elements if they were set
	 */
	public void cleanup() {
		for (IEncounter cons : testEncounters) {
			// List<Verrechnet> verrechnet =
			// VerrechnetService.getAllVerrechnetForBehandlung(cons);
			// for (Verrechnet verrechnet2 : verrechnet) {
			// System.out.print("Deleting verrechnet " + verrechnet2.getLabel() + " on
			// behandlung "
			// + cons.getLabel());
			// VerrechnetService.remove(verrechnet2);
			// System.out.println(" [OK]");
			// }

			coreModelService.remove(cons);
		}
		testEncounters.clear();
		for (ICoverage fall : testCoverages) {
			coreModelService.remove(fall);
		}
		testCoverages.clear();
		for (IContact contact : testPatients) {
			coreModelService.remove(contact);
		}
		testPatients.clear();
		for (IMandator mandator : testMandators) {
			IMandator activeMandator = ContextServiceHolder.get().getActiveMandator().orElse(null);
			if (mandator.equals(activeMandator)) {
				ContextServiceHolder.get().setActiveMandator(null);
			}
			coreModelService.remove(mandator);
		}
		testMandators.clear();
	}

	/**
	 * Accept all HTTPS certificates (used for servers running a self-signed
	 * certificate)
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public static void acceptAllCerts() throws NoSuchAlgorithmException, KeyManagementException {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		}};

		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

	}
}
