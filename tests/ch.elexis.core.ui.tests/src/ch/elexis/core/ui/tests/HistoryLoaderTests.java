package ch.elexis.core.ui.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.test.initializer.ConfigInitializer;
import ch.elexis.core.ui.actions.BackgroundJob;
import ch.elexis.core.ui.actions.BackgroundJob.BackgroundJobListener;
import ch.elexis.core.ui.actions.HistoryLoader;
import ch.elexis.core.ui.actions.KonsFilter;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.rgw.tools.TimeTool;

public class HistoryLoaderTests implements BackgroundJobListener {
	private static Patient patGriss, patSter, patKum, patPaal;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception{
		//test patients
		patGriss = new Patient("Grissemann", "Christoph", "17.05.66", Patient.MALE);
		patSter = new Patient("Stermann", "Dirk", "07.12.65", Patient.MALE);
		patKum = new Patient("Kummer", "Christa", "08.09.64", Patient.FEMALE);
		patPaal = new Patient("Paal", "Günther", "23.03.62", Patient.MALE);
		
		new ConfigInitializer().initializeBillingSystems(ConfigServiceHolder.get());
	}
	
	private int finishedLoaders;
	
	private ArrayList<Konsultation> historyDisplaylKons;
	private HistoryLoader historyDisplayLoader;
	
	@Test
	public void testExecuteWith3Consultations() throws InterruptedException{
		// init some specific sample data
		ArrayList<Konsultation> lCons = new ArrayList<Konsultation>();
		
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
		HistoryLoader loader = new HistoryLoader(new StringBuilder(), convertToEncounters(lCons));
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
	
	private List<IEncounter> convertToEncounters(List<Konsultation> list){
		List<IEncounter> encounters = new ArrayList<>();
		for (Konsultation konsultation : list) {
			encounters.add(CoreModelServiceHolder.get().load(konsultation.getId(), IEncounter.class)
				.orElse(null));
		}
		return encounters;
	}
	
	@Test
	public void testExecuteWithRandomConsultations() throws InterruptedException{
		int consListSize = 13;
		ArrayList<Konsultation> consList = generateTestConsultationAndCases(patPaal, consListSize);
		HistoryLoader loader = new HistoryLoader(new StringBuilder(), convertToEncounters(consList));
		loader.schedule(0);
		loader.join(); // wait for job to finish
		
		// check size
		assertEquals(consListSize, loader.getSize());
		
		String dataString = (String) loader.getData();
		String[] split = dataString.split("<p>");
		// cases/cons + starting tag 
		assertEquals(consListSize + 1, split.length);
		
		// check consultation date an case details
		for (int i = 0; i < consList.size(); i++) {
			String caseConsInfo = split[i + 1];
			String consId = getInfoConsId(caseConsInfo);
			assertNotNull(consId);
			Konsultation cons = getConsFromList(consId, consList);
			assertNotNull(cons);
			String consDate = cons.getDatum();
			String caseBeginDate = cons.getFall().getBeginnDatum();
			
			assertTrue(caseConsInfo.contains(consDate));
			assertTrue(caseConsInfo.contains("(" + caseBeginDate + "- offen)"));
		}
		
		//cancel job
		loader.cancel();
	}
	
	@Test
	public void testExecuteFromDifferentThreads() throws InterruptedException, ExecutionException{
		// init some test data
		int nrConsSter = 50;
		int nrConsGriss = 40;
		int nrOfConsKum = 30;
		removeTestConsultationAndCases(patSter);
		removeTestConsultationAndCases(patKum);
		removeTestConsultationAndCases(patGriss);
		List<Konsultation> consSter = generateTestConsultationAndCases(patSter, nrConsSter);
		List<Konsultation> consKum = generateTestConsultationAndCases(patKum, nrOfConsKum);
		List<Konsultation> consGriss = generateTestConsultationAndCases(patGriss, nrConsGriss);
		
		ExecutorService executorService = Executors.newCachedThreadPool();
		List<LoaderCallable> callables = new ArrayList<LoaderCallable>();
		
		for (int i = 0; i < 10; i++) {
			callables.add(new LoaderCallable(patGriss, new StringBuilder()));
			callables.add(new LoaderCallable(patSter, new StringBuilder()));
			callables.add(new LoaderCallable(patKum, new StringBuilder()));
		}
		
		int loadersSize = callables.size();
		finishedLoaders = 0;
		// invoke all tasks and check data
		List<Future<LoaderData>> futures = executorService.invokeAll(callables);
		// wait for all loaders to finish
		// if test is blocking here, make sure test is NOT running in UI thread
		while (finishedLoaders < loadersSize) {
			Thread.sleep(100);
		}
		
		for (Future<LoaderData> future : futures) {
			LoaderData loaderData = future.get();
			
			List<Konsultation> consList = null;
			if (loaderData.patient == patGriss) {
				consList = consGriss;
			} else if (loaderData.patient == patSter) {
				consList = consSter;
			} else if (loaderData.patient == patKum) {
				consList = consKum;
			}
			
			String result = (String) loaderData.loader.getData();
			
			testLoaderResult(result, consList);
		}
		executorService.shutdown();
	}
	
	private class ThreadSaveTimeToolRunnable implements Runnable {
		
		private boolean failed = false;
		
		private String failedToString;
		
		@Override
		public void run(){
			TimeTool tool = new TimeTool("01.01.2020");
			while (!failed) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// ignore
				}
				String toString = tool.toString(TimeTool.DATE_GER);
				if (!toString.equals("01.01.2020")) {
					failed = true;
					failedToString = toString;
				}
			}
		}
		
		public boolean isFailed(){
			return failed;
		}
		
		public String getFailedToString(){
			return failedToString;
		}
	}
	
	// TODO
	@Test
	@Ignore
	public void testExecuteThreadSafeTimeTool() throws InterruptedException{
		historyDisplaylKons = new ArrayList<Konsultation>(20);
		
		// init some test data
		int nrConsGriss = 40;
		removeTestConsultationAndCases(patGriss);
		List<Konsultation> consGriss = generateTestConsultationAndCases(patGriss, nrConsGriss);
		
		// start Thread using TimeTool
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		
		ThreadSaveTimeToolRunnable toolRunnable = new ThreadSaveTimeToolRunnable();
		
		executorService.execute(toolRunnable);
		
		for (int i = 0; i < 100; i++) {
			historyDisplayStop();
			historyDisplayLoad(patGriss);
			historyDisplayStart();
			
			// wait for all loaders to finish
			// if test is blocking here, make sure test is NOT running in UI thread
			while (historyDisplayLoader.getData() == null) {
				Thread.sleep(10);
			}
			
			String result = (String) historyDisplayLoader.getData();
			assertNotNull(result);
			testLoaderResult(result, consGriss);
			
			if (toolRunnable.isFailed()) {
				fail("TimeTool not 01.01.2020 but [" + toolRunnable.getFailedToString() + "]");
			}
		}
		
		executorService.shutdown();
	}
	
	@Test
	@Ignore
	public void testExecuteLikeHistoryDisplay() throws InterruptedException{
		historyDisplaylKons = new ArrayList<Konsultation>(20);
		
		// init some test data
		int nrConsSter = 50;
		int nrConsGriss = 40;
		int nrOfConsKum = 30;
		removeTestConsultationAndCases(patSter);
		removeTestConsultationAndCases(patKum);
		removeTestConsultationAndCases(patGriss);
		List<Konsultation> consSter = generateTestConsultationAndCases(patSter, nrConsSter);
		List<Konsultation> consKum = generateTestConsultationAndCases(patKum, nrOfConsKum);
		List<Konsultation> consGriss = generateTestConsultationAndCases(patGriss, nrConsGriss);
		
		for (int i = 0; i < 50; i++) {
			finishedLoaders = 0;
			historyDisplayStop();
			historyDisplayLoad(patSter);
			historyDisplayStart();
			
			Thread.sleep(10);
			
			historyDisplayStop();
			historyDisplayLoad(patKum);
			historyDisplayStart();
			
			Thread.sleep(10);
			
			historyDisplayStop();
			historyDisplayLoad(patGriss);
			historyDisplayStart();
			
			// wait for all loaders to finish
			// if test is blocking here, make sure test is NOT running in UI thread
			while (historyDisplayLoader.getData() == null) {
				Thread.sleep(500);
			}
			
			String result = (String) historyDisplayLoader.getData();
			assertNotNull(result);
			testLoaderResult(result, consGriss);
		}
	}
	
	public void historyDisplayStart(){
		historyDisplayStart(null);
	}
	
	public void historyDisplayStart(KonsFilter f){
		historyDisplayStop();
		historyDisplayLoader = new HistoryLoader(new StringBuilder(), convertToEncounters(historyDisplaylKons), false);
		historyDisplayLoader.setFilter(f);
		historyDisplayLoader.addListener(this);
		historyDisplayLoader.schedule();
	}
	
	public void historyDisplayStop(){
		if (historyDisplayLoader != null) {
			historyDisplayLoader.removeListener(this);
			historyDisplayLoader.cancel();
		}
	}
	
	public void historyDisplayLoad(Fall fall, boolean clear){
		if (clear) {
			historyDisplaylKons.clear();
		}
		if (fall != null) {
			Konsultation[] kons = fall.getBehandlungen(true);
			for (Konsultation k : kons) {
				historyDisplaylKons.add(k);
			}
		}
	}
	
	public void historyDisplayLoad(Patient pat){
		if (pat != null) {
			historyDisplaylKons.clear();
			Fall[] faelle = pat.getFaelle();
			for (Fall f : faelle) {
				historyDisplayLoad(f, false);
			}
		}
	}
	
	private Konsultation getConsFromList(String consId, List<Konsultation> consList){
		for (Konsultation konsultation : consList) {
			if (konsultation.getId().equals(consId)) {
				return konsultation;
			}
		}
		return null;
	}
	
	private String getInfoConsId(String caseConsInfo){
		int endIdx = caseConsInfo.indexOf("\">");
		int startIdx = caseConsInfo.indexOf("=\"");
		
		if (endIdx != -1 && startIdx != -1) {
			return caseConsInfo.substring(startIdx + 2, endIdx);
		}
		return null;
	}
	
	private void testLoaderResult(String result, List<Konsultation> consList){
		String[] split = result.split("<p>");
		
		// check consultation date an case details
		for (int i = 0; i < split.length - 1; i++) {
			String caseConsInfo = split[i + 1];
			String consId = getInfoConsId(caseConsInfo);
			assertNotNull(consId);
			Konsultation cons = getConsFromList(consId, consList);
			assertNotNull(cons);
			String consDate = cons.getDatum();
			System.out.println("Cons date [" + consDate + "]");
			String caseBeginDate = cons.getFall().getBeginnDatum();
			System.out.println("Case date [" + consDate + "]");
			
			if (!caseConsInfo.contains(consDate)) {
				System.out.println("No cons date [" + consDate + "] in [" + caseConsInfo + "]");
			}
			
			assertTrue(caseConsInfo.contains(consDate));
			assertTrue(caseConsInfo.contains("(" + caseBeginDate + "- offen)"));
		}
	}
	
	/**
	 * reconstruct ui event when patien is selected
	 * 
	 * @param pat
	 * @param expectedSize
	 * @throws InterruptedException
	 */
	private HistoryLoader scheduleLoaderFor(Patient pat, StringBuilder sb)
		throws InterruptedException{
		// clear list and populate with patients consultations
		ArrayList<Konsultation> lCons = new ArrayList<Konsultation>();
		Fall[] cases = pat.getFaelle();
		for (Fall c : cases) {
			Konsultation[] consList = c.getBehandlungen(true);
			for (Konsultation cons : consList) {
				lCons.add(cons);
			}
		}
		HistoryLoader loader = new HistoryLoader(sb, convertToEncounters(lCons), false);
		loader.addListener(this);
		loader.schedule();
		return loader;
	}
	
	private class LoaderData {
		private Patient patient;
		private HistoryLoader loader;
	}
	
	private class LoaderCallable implements Callable<LoaderData> {
		
		private Patient patient;
		private StringBuilder sb;
		
		public LoaderCallable(Patient patient, StringBuilder sb){
			this.patient = patient;
			this.sb = sb;
		}
		
		@Override
		public LoaderData call() throws Exception{
			LoaderData ret = new LoaderData();
			ret.loader = scheduleLoaderFor(patient, sb);
			ret.patient = patient;
			return ret;
		}
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
			Fall testCase = pat.neuerFall("TstCase" + seqNr, pat.getLabel(true) + "_tests", "KVG");
			testCase.setBeginnDatum(formatCalendarToString(cal));
			// set cons date to next for every second testcase
			if (seqNr % 2 == 0) {
				cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);
			}
			Konsultation testCons = new Konsultation(testCase);
			testCons.setDatum(formatCalendarToString(cal), true);
			consList.add(testCons);
		}
		return consList;
	}
	
	private static void removeTestConsultationAndCases(Patient pat){
		Fall[] faelle = pat.getFaelle();
		for (Fall fall : faelle) {
			Konsultation[] konsultationen = fall.getBehandlungen(true);
			for (Konsultation konsultation : konsultationen) {
				konsultation.delete();
			}
			fall.delete();
		}
	}
	
	/**
	 * formats the date
	 * 
	 * @param cal
	 * @return date of this format dd.MM.yyyy
	 */
	private static String formatCalendarToString(Calendar cal){
		return  String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)) + "." + String.format("%02d", cal.get(Calendar.MONTH)+1) + "."
			+ cal.get(Calendar.YEAR);
	}
	
	private static int randBetween(int start, int end){
		return start + (int) Math.round(Math.random() * (end - start));
	}
	
	@Override
	public void jobFinished(BackgroundJob j){
		if (j instanceof HistoryLoader) {
			HistoryLoader loader = (HistoryLoader) j;
			finishedLoaders++;
		}
	}
}
