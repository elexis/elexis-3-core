package ch.elexis.core.ui.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.ui.actions.HistoryLoader;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.rgw.tools.TimeTool;

public class HistoryLoaderTests {
	private static HistoryLoader loader;
	private static StringBuilder sb;
	private static ArrayList<Konsultation> lCons;
	private static Patient patGriss, patSter, patKum, patPaal;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception{
		lCons = new ArrayList<Konsultation>();
		
		//test patients
		patGriss = new Patient("Grissemann", "Christoph", "17.05.66", Patient.MALE);
		patSter = new Patient("Stermann", "Dirk", "07.12.65", Patient.MALE);
		patKum = new Patient("Kummer", "Christa", "08.09.64", Patient.FEMALE);
		patPaal = new Patient("Paal", "Günther", "23.03.62", Patient.MALE);
	}
	
	@Test
	public void testExecuteWith3Consultations() throws InterruptedException{
		// init some specific sample data
		lCons = new ArrayList<Konsultation>();
		
		//case and consultation 1
		Fall c1 = patPaal.neuerFall("TstCase1-Paal", "TestAccident", "UVG");
		c1.setBeginnDatum("19.11.2015");
		Konsultation cons1 = new Konsultation(c1);
		cons1.setDatum("20.11.2015", true);
		lCons.add(cons1);
		//case and consultation 2
		Fall c2 = patPaal.neuerFall("TstCase2-Paal", "TestIllness", "KVG");
		c2.setBeginnDatum("01.01.2014");
		Konsultation cons2 = new Konsultation(c2);
		cons2.setDatum("24.01.2014", true);
		lCons.add(cons2);
		//case and consultation 3
		Fall c3 = patPaal.neuerFall("TstCase3-Paal", "TestPrevention", "MV");
		c3.setBeginnDatum("13.11.2015");
		Konsultation cons3 = new Konsultation(c3);
		cons3.setDatum("13.11.2015", true);
		lCons.add(cons3);
		
		// pass values to history loader and schedule
		HistoryLoader loader = new HistoryLoader(new StringBuilder(), lCons);
		loader.schedule(0);
		loader.join(); // wait for finish
		assertEquals(3, loader.getSize());
		
		String dataString = (String) loader.getData();
		String[] split = dataString.split("<p>");
		// 3 cases/cons + starting tag 
		assertEquals(4, split.length);
		
		// consultation date
		assertTrue(split[1].contains("20.11.2015"));
		// check case details displayed
		assertTrue(split[1].contains("UVG: TestAccident - TstCase1-Paal(19.11.2015- offen)"));
		
		// TestCase3 happened before TestCase2 and therefore will be at 2 position
		// check consultation date and case details
		assertTrue(split[2].contains("13.11.2015"));
		assertTrue(split[2].contains("MV: TestPrevention - TstCase3-Paal(13.11.2015- offen)"));
		
		// TestCase2 is the oldest and therefore at last place
		// check consultation date and case details
		assertTrue(split[3].contains("01.01.2014"));
		assertTrue(split[3].contains("KVG: TestIllness - TstCase2-Paal(01.01.2014- offen)"));
		
		//cancel job
		loader.cancel();
	}
	
	@Test
	public void testExecuteWithRandomConsultations() throws InterruptedException{
		int consListSize = 13;
		lCons = generateTestConsultationAndCases(patPaal, consListSize);
		HistoryLoader loader = new HistoryLoader(new StringBuilder(), lCons);
		loader.schedule(0);
		loader.join(); // wait for job to finish
		
		// check size
		assertEquals(consListSize, loader.getSize());
		
		String dataString = (String) loader.getData();
		String[] split = dataString.split("<p>");
		// cases/cons + starting tag 
		assertEquals(consListSize + 1, split.length);
		
		// check consultation date an case details
		for (int i = 0; i < lCons.size(); i++) {
			Konsultation cons = lCons.get(i);
			String consDate = cons.getDatum();
			String caseBeginDate = cons.getFall().getBeginnDatum();
			
			String caseConsInfo = split[i + 1];
			assertTrue(caseConsInfo.contains(cons.getId()));
			assertTrue(caseConsInfo.contains(consDate));
			assertTrue(caseConsInfo.contains("Bond James"));
			assertTrue(caseConsInfo.contains("KVG: Tests - TstCase"));
			assertTrue(caseConsInfo.contains("(" + caseBeginDate + "- offen)"));
		}
		
		//cancel job
		loader.cancel();
	}
	
	@Test
	public void testExecuteFromDifferentThreads() throws InterruptedException, ExecutionException{
		// init some test data
		int nrConsSter = 13;
		int nrConsGriss = 4;
		int nrOfConsKum = 24;
		List<Konsultation> consSter = generateTestConsultationAndCases(patSter, nrConsSter);
		List<Konsultation> consKum = generateTestConsultationAndCases(patKum, nrOfConsKum);
		List<Konsultation> consGriss = generateTestConsultationAndCases(patGriss, nrConsGriss);
		sb = new StringBuilder();
		
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		List<Callable<String[]>> callables = new ArrayList<Callable<String[]>>();
		callables.add(new Callable<String[]>() {
			public String[] call() throws Exception{
				scheduleLoaderFor(patSter, nrConsSter);
				Thread.sleep(2000);
				String dataString = (String) loader.getData();
				return new String[] {
					patSter.getName(), dataString
				};
			}
		});
		callables.add(new Callable<String[]>() {
			public String[] call() throws Exception{
				scheduleLoaderFor(patGriss, nrConsGriss);
				Thread.sleep(2000);
				String dataString = (String) loader.getData();
				return new String[] {
					patGriss.getName(), dataString
				};
			}
		});
		callables.add(new Callable<String[]>() {
			public String[] call() throws Exception{
				scheduleLoaderFor(patKum, nrOfConsKum);
				Thread.sleep(2000);
				String dataString = (String) loader.getData();
				return new String[] {
					patKum.getName(), dataString
				};
			}
		});
		
		// invoke all tasks and check data
		List<Future<String[]>> futures = executorService.invokeAll(callables);
		for (Future<String[]> future : futures) {
			String[] result = future.get();
			
			//find which patients cons we're handling
			List<Konsultation> consList = new ArrayList<Konsultation>();
			if (result[0].equals(patSter.getName())) {
				consList = consSter;
			} else if (result[0].equals(patGriss.getName())) {
				consList = consGriss;
			} else {
				consList = consKum;
			}
			
			String[] split = result[1].split("<p>");
			assertEquals(consList.size() + 1, split.length);
			// check consultation date an case details
			for (int i = 0; i < consList.size(); i++) {
				Konsultation cons = consList.get(i);
				String consDate = cons.getDatum();
				String caseBeginDate = cons.getFall().getBeginnDatum();
				
				String caseConsInfo = split[i + 1];
				assertTrue(caseConsInfo.contains(cons.getId()));
				assertTrue(caseConsInfo.contains(consDate));
				assertTrue(caseConsInfo.contains("Bond James"));
				assertTrue(caseConsInfo.contains("KVG: Tests - TstCase"));
				assertTrue(caseConsInfo.contains("(" + caseBeginDate + "- offen)"));
			}
		}
		
		executorService.shutdown();
	}
	
	/**
	 * reconstruct ui event when patien is selected
	 * 
	 * @param pat
	 * @param expectedSize
	 * @throws InterruptedException
	 */
	private static void scheduleLoaderFor(Patient pat, int expectedSize)
		throws InterruptedException{
		// clear list and populate with patients consultations
		lCons.clear();
		Fall[] cases = pat.getFaelle();
		for (Fall c : cases) {
			Konsultation[] consList = c.getBehandlungen(true);
			for (Konsultation cons : consList) {
				lCons.add(cons);
			}
		}
		assertEquals(expectedSize, lCons.size());
		
		if (loader != null) {
			loader.cancel();
		}
		
		sb.setLength(0);
		loader = new HistoryLoader(sb, lCons, false);
		loader.schedule();
	}
	
	/**
	 * generate test cases and consultation with random dates between 1999 and 2015
	 * 
	 * @param pat
	 *            Patient to which cases and cons shall be added
	 * @param nrOfCons
	 *            how many cases/cons you'd like to add
	 * @return list a sorted list (newest-> oldest) of consultations with belonging cases from this
	 *         patient
	 */
	private static ArrayList<Konsultation> generateTestConsultationAndCases(Patient pat,
		int nrOfCons){
		ArrayList<Konsultation> consList = new ArrayList<Konsultation>();
		for (int seqNr = 1; seqNr <= nrOfCons; seqNr++) {
			// generate a random test date
			Calendar cal = Calendar.getInstance();
			int year = randBetween(1999, 2015);
			cal.set(Calendar.YEAR, year);
			int dayOfYear = randBetween(1, cal.getActualMaximum(Calendar.DAY_OF_YEAR));
			cal.set(Calendar.DAY_OF_YEAR, dayOfYear);
			
			//  create test case and consultation
			Fall testCase = pat.neuerFall("TstCase" + seqNr, "Tests", "KVG");
			testCase.setBeginnDatum(formatCalendarToString(cal));
			// set cons date to next for every second testcase
			if (seqNr % 2 == 0) {
				cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);
			}
			Konsultation testCons = new Konsultation(testCase);
			testCons.setDatum(formatCalendarToString(cal), true);
			consList.add(testCons);
		}
		
		Collections.sort(consList, new Comparator<Konsultation>() {
			TimeTool t1 = new TimeTool();
			TimeTool t2 = new TimeTool();
			
			public int compare(final Konsultation o1, final Konsultation o2){
				if ((o1 == null) || (o2 == null)) {
					return 0;
				}
				t1.set(o1.getDatum());
				t2.set(o2.getDatum());
				if (t1.isBefore(t2)) {
					return 1;
				}
				if (t1.isAfter(t2)) {
					return -1;
				}
				return 0;
			}
		});
		return consList;
	}
	
	/**
	 * formats the date
	 * 
	 * @param cal
	 * @return date of this format dd.MM.yyyy
	 */
	private static String formatCalendarToString(Calendar cal){
		return cal.get(Calendar.DAY_OF_MONTH) + "." + cal.get(Calendar.MONTH) + "."
			+ cal.get(Calendar.YEAR);
	}
	
	private static int randBetween(int start, int end){
		return start + (int) Math.round(Math.random() * (end - start));
	}
	
}
