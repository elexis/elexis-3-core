package ch.elexis.data;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.rgw.tools.JdbcLink;

public class Test_Prescription extends AbstractPersistentObjectTest {
	
	private JdbcLink link;
	
	@Before
	public void setUp(){
		link = initDB();
	}
	
	@After
	public void tearDown(){
		if (link != null) {
			link.exec("DROP ALL OBJECTS");
			link.disconnect();
		}
	}
	
	private class TestItem {
		private TestItem(String string, List<Float> asList){
			this.setDose(string);
			this.setAsFloats(asList);
		}
		
		public List<Float> getAsFloats(){
			return asFloats;
		}
		
		public void setAsFloats(List<Float> asFloats){
			this.asFloats = asFloats;
		}
		
		public String getDose(){
			return dose;
		}
		
		public void setDose(String dose){
			this.dose = dose;
		}
		
		private String dose = "";
		private List<Float> asFloats = Arrays.asList();
	}
	
	private List<TestItem> initializeTestItems(){
		List<TestItem> testItems = new ArrayList<TestItem>();
		ArrayList<Float> empty = new ArrayList<Float>();

		/*
		iR, max 10mtl
		iR, selten
		iR: 1-0-1
		iR bis 2-0-2
		iR 10Trpf
		iR 10Trpf)
		iR (~2 tgl)
		bis 06.07.14: 0.5-0-0-1
		bis 1-0-1
		bis 10x
		bis zu 2x1/Tag
		bprog -max 4
		ca 3/8
		STOPP 11.12.13
		Stopp 11/13)
		ab 07.07.14: 1-0-0-1
		?
		??
		???
		Dauerrezept 13.01.14
		8
		8 Trpf
		8 Trpf/d
		8-0-0
		8-0-0-0
		80
		80mg tgl
		85+75
		8Trpf
		8mg
		8trpf
		8trpf tg
		8trpf tgl o 1/2Fl mtl
		9 mg
		9/4 /wo
		5mg tgl
		5x tgl. für 5 d
		5x1Tbl /Wo
		6*1
		6-mtl (1.+7.)
		60
		65
		7/8
		72 mg
		75mg!
		7mg tgl
		40-40-40
		400
		44
		45mg
		4T tgl
		4x1/d
		4x20 gtts
		4x20/d
		5
		5 tgl
		5*1
		5*10ml /Wo
		5-0-0
		5-0-0-0
		5-4-4
		5/8-0-0
		5/wo ~
		37.5
		3T wötl
		3mtl
		3x1
		3x5tr
		4
		4* 1/4
		4*1
		4+3
		30mg decrendo
		25mg/Wo
		25mg/Wo, Start m 10, wö+5mg
		26 E
		2iR (max6 tgl)
		2wöchentl
		2x
		2x pro Woche
		2x tgl
		2x1
		2-3 Tbl
		2-wöchentl
		2-wöchentlich
		2.5
		2.5-0-2.5
		20 E
		20-0-0
		1x1 alle 14 d
		1x1 alle 4h
		1x1, 7 Tage PAUSE
		10mg
		10tg mtl
		10trpf
		12
		125 mcgr tgk
		12mg
		16mg
		180ug /Wo
		1A 3-mntl
		1Fl /2Mt
		1Fl mtl
		1Fl/2Mt
		1inj Wo
		1mg /Woche
		1ml, 2-tgl
		1/2Fl mtl
		1/2bis1 - 0 - 0
		1/-/-
		1/0/0
		1/1/1
		1/2
		1/2 - 0 - 0
		1/2 - 0 - 1/2
		1/2 - 0 -1/2
		*/
		testItems.add(new TestItem("1/2", Arrays.asList(0.5f)));
		testItems.add(new TestItem("7/8", Arrays.asList(0.875f)));

		// I think a human will return a different result, but at least they do
		// do not throw an exception
		testItems.add(new TestItem("~1", empty));
		testItems.add(new TestItem("~1/2", empty));
		testItems.add(new TestItem("~1/2 tgl", empty));
		testItems.add(new TestItem("1 (ev 2)", empty));
		testItems.add(new TestItem("1 /3Tg", empty));
		testItems.add(new TestItem("1 1/2", empty));
		testItems.add(new TestItem("1 1/2-0-0", Arrays.asList(0.0f, 0.0f, 0.0f)));
		testItems.add(new TestItem("1 3-mtl", Arrays.asList(0.0f)));
		testItems.add(new TestItem("1 3-tgl", Arrays.asList(0.0f)));
		testItems.add(new TestItem("1 3-tägl", Arrays.asList(0.0f)));
		testItems.add(new TestItem("0.5/-/-", Arrays.asList(0.0f)));
		testItems.add(new TestItem("(1)-0-1", Arrays.asList(0.0f, 0.0f, 1.0f)));
		testItems.add(new TestItem("-8 tgl", Arrays.asList(0.0f)));
		testItems.add(new TestItem(".5", empty));
		testItems.add(new TestItem("1 Amp 3 monatlich", empty));
		testItems.add(new TestItem("1 MAT/72 h", empty));
		testItems.add(new TestItem("1 So", empty));
		testItems
			.add(new TestItem("bis 06.07.14: 0.5-0-0-1", Arrays.asList(0.0f, 0.0f, 0.0f, 1.0f)));
		
		// From here I think the human and the algorithm arrive at the same conclusion
		// for a daily dosage
		// first the cases where we cannot get a numerical result
		testItems.add(new TestItem("", empty));
		testItems.add(new TestItem("iR od 1", empty));
		testItems.add(new TestItem("(Abg.Apoth", empty));
		testItems.add(new TestItem("(ca 5*10E)", empty));
		testItems.add(new TestItem("(gel)", empty));
		testItems.add(new TestItem("-", empty));
		testItems.add(new TestItem("nur nach Verordnung", empty));
		
		// here at last we have valid numerical values 
		testItems.add(new TestItem("~6-4-2", Arrays.asList(0.0f, 4.0f, 2.0f)));
		testItems
			.add(new TestItem("1-1-1-0 (KoGu erhalten)", Arrays.asList(1.0f, 1.0f, 1.0f, 0.0f)));
		testItems
			.add(new TestItem("1-0-0-0 (STOPP 8.1.14)", Arrays.asList(1.0f, 0.0f, 0.0f, 0.0f)));
		testItems.add(new TestItem("1-0-0-0 (bis 08/15)", Arrays.asList(1.0f, 0.0f, 0.0f, 0.0f)));
		testItems.add(new TestItem("1-0-0-0 jeden 2. Tag", Arrays.asList(1.0f, 0.0f, 0.0f, 0.0f)));
		testItems
.add(new TestItem("1-0-0-0, Sa+So 1.5-0-0", Arrays.asList(1.0f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.0f)));
		testItems.add(new TestItem("0-0-1-0 bis INR2x therapeutisch", Arrays.asList(0.0f, 0.0f,
			1.0f, 0.0f)));
		testItems.add(new TestItem(".5-.5-1", Arrays.asList(0.5f, 0.5f, 1.0f)));
		testItems.add(new TestItem("0-0- 1/2", Arrays.asList(0.0f, 0.0f, 0.5f)));
		testItems.add(new TestItem("0-0- 1/4-1/2", Arrays.asList(0.0f, 0.0f, 0.25f, 0.5f)));
		testItems.add(new TestItem("0-0- 1/8", Arrays.asList(0.0f, 0.0f, 0.125f)));
		testItems.add(new TestItem("0-0-*-0", Arrays.asList(0.0f, 0.0f, 0.0f, 0.0f)));
		testItems.add(new TestItem("0-0-0- 40E", Arrays.asList(0.0f, 0.0f, 0.0f, 0.0f)));
		testItems.add(new TestItem("0 (bis 08", empty));
		testItems.add(new TestItem("½", Arrays.asList(0.5f)));
		testItems.add(new TestItem("¼", Arrays.asList(0.25f)));
		testItems.add(new TestItem("1½", Arrays.asList(1.5f)));
		testItems.add(new TestItem("1", Arrays.asList(1.0f)));
		testItems.add(new TestItem("0", Arrays.asList(0.0f)));
		testItems.add(new TestItem("1-1-1-1", Arrays.asList(1.0f, 1.0f, 1.0f, 1.0f)));
		testItems.add(new TestItem("0-0-0-1", Arrays.asList(0.0f, 0.0f, 0.0f, 1.0f)));
		testItems.add(new TestItem("½-0-0-0", Arrays.asList(0.5f, 0.0f, 0.0f, 0.0f)));
		testItems.add(new TestItem("¼-0-0-0", Arrays.asList(0.25f, 0.0f, 0.0f, 0.0f)));
		testItems.add(new TestItem("1½-0-0-0", Arrays.asList(1.5f, 0.0f, 0.0f, 0.0f)));
		testItems.add(new TestItem("0.5-1-1-", Arrays.asList(0.5f, 1.0f, 1.0f)));
		return testItems;
	}
	
	@Test
	public void testExampleDoseAsFloats(){
		List<Float> res = Prescription.getDoseAsFloats("1-1-1-1");
		assert (res != null);
		assertEquals(4, res.size());

		List<Float> oneLiner = Arrays.asList(1.0f, 1.0f, 1.0f, 1.0f);
		assertEquals(oneLiner.size(), res.size());
		for (int j = 0; j < oneLiner.size(); j++) {
			System.out.println("Testing " + oneLiner.get(j));
			assertEquals(oneLiner.get(j), res.get(j));
		}
		res = Prescription.getDoseAsFloats("0 (bis 08/15)");
		assert (res != null);
		assertEquals(0, res.size());

	}
	
	@Test
	public void testDoseAsFloats(){
		for (TestItem item2test : initializeTestItems()) {
			String test_line = item2test.getDose();
			System.out.println("Testing size dose " + test_line);
			List<Float> res = Prescription.getDoseAsFloats(item2test.getDose());
			List<Float> expected = item2test.getAsFloats();
			assert (res != null);
			if (expected.size() != res.size()) {
				System.out.println("Testing size: " + res.size() + " != " + expected.size()
					+ " dose " + item2test.getDose() + " " + res);
				System.out.println("Failed!!! " + test_line);
			}
			assertEquals(expected.size(), res.size());
			for (int j = 0; j < expected.size(); j++) {
				float expect = expected.get(j);
				float actual = res.get(j);
				if (Math.abs(expect - actual) > 0.01f) {
					System.out.println("Testing element : " + j + " of dose: " + test_line
						+ " failed " + expect + " != " + actual);
					System.out.println("Failed!!! " + test_line);
				}
				assertEquals(expected.get(j), res.get(j));
			}
		}
	}
}
