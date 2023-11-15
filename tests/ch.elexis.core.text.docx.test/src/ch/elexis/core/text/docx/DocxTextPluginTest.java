package ch.elexis.core.text.docx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

import ch.elexis.core.text.ITextPlugin;
import ch.elexis.core.text.ReplaceCallback;
import ch.elexis.core.text.docx.util.DocxUtil;
import ch.elexis.core.text.docx.util.FindTextVisitor;

public class DocxTextPluginTest {

	// from ch.elexis.core.ui.text.TextContainer
	private static final String DONT_SHOW_REPLACEMENT_ERRORS = "*";
	public static final String MATCH_SQUARE_BRACKET = "[\\[\\]]"; //$NON-NLS-1$

	public static final String MATCH_TEMPLATE = "\\[[" + DONT_SHOW_REPLACEMENT_ERRORS //$NON-NLS-1$
			+ "]?[-a-zA-ZäöüÄÖÜéàè_ ]+\\.[-a-zA-Z0-9äöüÄÖÜéàè_ ]+\\]";

	@Test
	public void ueberweisungSie() throws Docx4JException, IOException {
		DocxTextPlugin plugin = new DocxTextPlugin();

		// replacements in header 10
		// replacements in body 25
		// replacements in footer 12
		assertTrue(plugin.loadFromStream(getClass().getResourceAsStream("/rsc/ueberweisungSie.docx"), true));

		plugin.findOrReplace(MATCH_TEMPLATE, new ReplaceCallback() {
			public Object replace(final String in) {
				return replaceFields(in.replaceAll(MATCH_SQUARE_BRACKET, ""));
			}

			private Object replaceFields(String replaceAll) {
				return "test replace text";
			}
		});

		WordprocessingMLPackage document = (WordprocessingMLPackage) plugin.getCurrentDocument();
		assertNotNull(document);
		int foundCount = plugin.findTextCount("test replace text");
		assertTrue(foundCount > 0);

		saveToTempFileAndDelete(document, "ueberweisungSie");
	}

	@Test
	public void laborBlatt() throws Docx4JException, IOException {
		DocxTextPlugin plugin = new DocxTextPlugin();

		// replacements in body 12, 1 x tabelle
		// replacements in footer 12
		assertTrue(plugin.loadFromStream(getClass().getResourceAsStream("/rsc/laborblatt.docx"), true));

		// from ch.elexis.core.ui.laboratory.views.LaborblattView
		int cols = 3;
		int[] colsizes = new int[cols];
		float first = 25;
		float second = 10;
		if (cols > 2) {
			int rest = Math.round((100f - first - second) / (cols - 2f));
			for (int i = 2; i < cols; i++) {
				colsizes[i] = rest;
			}
		}
		colsizes[0] = Math.round(first);
		colsizes[1] = Math.round(second);

		LinkedList<String[]> usedRows = new LinkedList<String[]>();
		for (int i = 0; i < 5; i++) {
			String[] row = new String[cols];
			for (int j = 0; j < cols; j++) {
				row[j] = "row " + i + " column " + j;
			}
			usedRows.add(row);
		}
		String[][] fld = usedRows.toArray(new String[0][]);
		assertTrue(plugin.insertTable("[Laborwerte]", //$NON-NLS-1$
				ITextPlugin.FIRST_ROW_IS_HEADER, fld, colsizes));

		WordprocessingMLPackage document = (WordprocessingMLPackage) plugin.getCurrentDocument();
		assertNotNull(document);
		FindTextVisitor visitor = new FindTextVisitor("row 1 column 1");
		TraversalUtil.visit(document.getMainDocumentPart(), visitor);
		assertEquals(1, visitor.getFound().size());
		assertNotNull(DocxUtil.getParentTbl(visitor.getFound().get(0)));
		saveToTempFileAndDelete(document, "laborblatt");
	}

	@Test
	public void tarmed44s1Prepare() throws Docx4JException, IOException {
		DocxTextPlugin plugin = new DocxTextPlugin();

		assertTrue(plugin.loadFromStream(getClass().getResourceAsStream("/rsc/tarmed44s1.docx"), true));
		WordprocessingMLPackage document = (WordprocessingMLPackage) plugin.getCurrentDocument();
		assertNotNull(document);

		plugin.prepare();

		saveToTempFileAndDelete(document, "tarmed44s1_prepared");
	}

	@Test
	public void tarmed44s1() throws Docx4JException, IOException {
		DocxTextPlugin plugin = new DocxTextPlugin();

		assertTrue(plugin.loadFromStream(getClass().getResourceAsStream("/rsc/tarmed44s1.docx"), true));

		// text.replace("\\[F98\\]", XMLPrinterUtil.getEANList(eanArray));
		// find in table required
		plugin.findOrReplace("\\[F98\\]", new ReplaceCallback() {
			public String replace(final String in) {
				return "EAN List";
			}
		});
		WordprocessingMLPackage document = (WordprocessingMLPackage) plugin.getCurrentDocument();
		assertNotNull(document);

		int foundCount = plugin.findTextCount("EAN List");
		assertTrue(foundCount > 0);

		// find in textbox required, tab problem
		plugin.findOrReplace("\\[F44.Datum\\]", new ReplaceCallback() {
			public String replace(final String in) {
				return "Falldatum";
			}
		});
		foundCount = plugin.findTextCount("Falldatum");
		assertTrue(foundCount > 0);

		plugin.findOrReplace("\\[Fall.BeginnDatum\\]", new ReplaceCallback() {
			public String replace(final String in) {
				return "01.01.2011";
			}
		});
		foundCount = plugin.findTextCount("01.01.2011");
		assertTrue(foundCount > 0);

		// replace in textbox with line break
		plugin.findOrReplace("\\[Adressat.Anschrift]", new ReplaceCallback() {
			public String replace(final String in) {
				return "test adressat\ntest adresse";
			}
		});
		foundCount = plugin.findTextCount("test adressat");
		assertTrue(foundCount > 0);

		// insert absolute position with cursor
		Object cursor = plugin.insertTextAt(0, 255, 190, 45, " ", ITextPlugin.ALIGN_LEFT); //$NON-NLS-1$
		String balanceHeaders = "Code\tSatz\tBetrag\tMWSt\tMWSt.-Nr.:\t"; //$NON-NLS-1$
		plugin.setFont("Helvetica", ITextPlugin.STYLE_BOLD, 7); //$NON-NLS-1$
		cursor = plugin.insertText(cursor, balanceHeaders, ITextPlugin.ALIGN_LEFT);

		plugin.setFont("Helvetica", ITextPlugin.STYLE_NORMAL, 7); //$NON-NLS-1$
		cursor = plugin.insertText(cursor, "keine\t", ITextPlugin.ALIGN_LEFT);

		plugin.setFont("Helvetica", ITextPlugin.STYLE_BOLD, 7); //$NON-NLS-1$
		cursor = plugin.insertText(cursor, "Anzahlung:\t", ITextPlugin.ALIGN_LEFT);

		plugin.setFont("Helvetica", ITextPlugin.STYLE_NORMAL, 7); //$NON-NLS-1$
		cursor = plugin.insertText(cursor, "0.00\t\t\t", ITextPlugin.ALIGN_LEFT);

		plugin.setFont("Helvetica", ITextPlugin.STYLE_BOLD, 7); //$NON-NLS-1$
		cursor = plugin.insertText(cursor, "Gesamtbetrag:\t", ITextPlugin.ALIGN_RIGHT);

		plugin.setFont("Helvetica", ITextPlugin.STYLE_NORMAL, 7); //$NON-NLS-1$
		cursor = plugin.insertText(cursor, "1234.12\n", ITextPlugin.ALIGN_RIGHT);

		// second line
		String secondLine = "0\t" + "0" + "\t" + "0" + "\t" + "0" + "\t";
		plugin.setFont("Helvetica", ITextPlugin.STYLE_NORMAL, 7); //$NON-NLS-1$
		cursor = plugin.insertText(cursor, secondLine, ITextPlugin.ALIGN_LEFT);

		saveToTempFileAndDelete(document, "tarmed44s1");
	}

	private void saveToTempFileAndDelete(WordprocessingMLPackage document, String prefix)
			throws Docx4JException, IOException {
			File tempFile = File.createTempFile(prefix, ".docx");
			Docx4J.save(document, tempFile, Docx4J.FLAG_SAVE_ZIP_FILE);
			tempFile.delete();
	}
}
