package ch.elexis.core.text.docx.util;

import java.math.BigInteger;
import java.util.List;

import org.docx4j.UnitsOfMeasurement;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.TblGrid;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.docx4j.wml.TrPr;

import ch.elexis.core.text.ITextPlugin;
import jakarta.xml.bind.JAXBException;

public class TableUtil {

	static org.docx4j.wml.ObjectFactory wmlObjectFactory = Context.getWmlObjectFactory();

	public static P pTable() {
		return null;
	}

	/**
	 * Insert a table before the R with the information provided.
	 *
	 * @param r
	 * @param properties
	 * @param contents
	 * @param columnSizes
	 * @param totalWidthTwips
	 * @param columnSizesPercent
	 * @return
	 */
	public static Tbl insertTable(R r, int properties, String[][] contents, int[] columnSizes, int totalWidthTwips,
			boolean columnSizesPercent) {

		Tbl table = createTable(contents.length, contents[0].length, columnSizes, totalWidthTwips, columnSizesPercent);

		if (properties == ITextPlugin.FIRST_ROW_IS_HEADER) {
			Tr headerRow = (Tr) table.getContent().get(0);

			repeatOnEveryPage(headerRow);

			for (int columnIndex = 0; columnIndex < contents[0].length; columnIndex++) {
				Tc column = (Tc) headerRow.getContent().get(columnIndex);
				setCellText(column, r, contents[0][columnIndex]);
			}
		}
		int rowIndex = properties == ITextPlugin.FIRST_ROW_IS_HEADER ? 1 : 0;
		for (; rowIndex < contents.length; rowIndex++) {
			Tr row = (Tr) table.getContent().get(rowIndex);

			for (int columnIndex = 0; columnIndex < contents[0].length; columnIndex++) {
				Tc column = (Tc) row.getContent().get(columnIndex);
				setCellText(column, r, contents[rowIndex][columnIndex]);
			}
		}
		P p = DocxUtil.getParentP(r);
		if (p != null) {
			ContentAccessor parent = DocxUtil.getParentContentAccessor(p);
			List<Object> parentContent = parent.getContent();
			int index = parentContent.indexOf(p);
			parentContent.add(index, table);
		}
		return table;
	}

	/**
	 * Write the content into a table cell. Line breaks (\n, \r\n, \r) are converted
	 * to additional paragraphs within the cell and tabs (\t) to tab elements, since
	 * a literal newline or tab character inside a w:t is not rendered as such by
	 * Word or LibreOffice (a literal CR even makes LibreOffice drop the whole
	 * table).
	 *
	 * @param column      the table cell
	 * @param templateRun run providing the run properties (rPr) to use
	 * @param content     the cell content, may contain line breaks and tabs
	 */
	private static void setCellText(Tc column, R templateRun, String content) {
		P columnPara = (P) column.getContent().get(0);
		R run = wmlObjectFactory.createR();
		run.setRPr(templateRun.getRPr());
		columnPara.getContent().add(run);

		String[] lines = (content == null ? "" : content).replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
		for (int i = 0; i < lines.length; i++) {
			if (i > 0) {
				// additional line within the cell as a new paragraph
				columnPara = DocxUtil.appendParagraph(run);
				run = DocxUtil.getOrCreateRun(columnPara);
				run.setRPr(templateRun.getRPr());
			}
			String[] tabs = lines[i].split("\t", -1);
			Text text = DocxUtil.getOrCreateText(run);
			text.setValue(tabs[0]);
			for (int j = 1; j < tabs.length; j++) {
				text = DocxUtil.appendTabbedText(run);
				text.setValue(tabs[j]);
			}
		}
	}

	private static Tbl createTable(int rows, int cols, int[] columnSizes, int totalWidthTwips,
			boolean columnSizesPercent) {
		Tbl tbl = Context.getWmlObjectFactory().createTbl();

		if (columnSizesPercent && columnSizes != null) {
			columnSizes = get100Percentages(columnSizes);
		}

		// w:tblPr
		String strTblPr = "<w:tblPr " + Namespaces.W_NAMESPACE_DECLARATION + ">" + "<w:tblStyle w:val=\"TableGrid\"/>"
				+ "<w:tblW w:w=\"0\" w:type=\"auto\"/>" + "<w:tblLook w:val=\"04A0\"/>" + "</w:tblPr>";
		TblPr tblPr = null;
		try {
			tblPr = (TblPr) XmlUtils.unmarshalString(strTblPr);
		} catch (JAXBException e) {
			// Shouldn't happen
			e.printStackTrace();
		}

		tbl.setTblPr(tblPr);

		TblGrid tblGrid = Context.getWmlObjectFactory().createTblGrid();
		tbl.setTblGrid(tblGrid);
		for (int i = 1; i <= cols; i++) {
			TblGridCol gridCol = null;
			if (columnSizes != null) {
				if (columnSizesPercent && columnSizes[i - 1] > 0) {
					gridCol = Context.getWmlObjectFactory().createTblGridCol();
					gridCol.setW(getPercentageTwips(totalWidthTwips, columnSizes[i - 1]));
				} else if (columnSizes[i - 1] > 0) {
					gridCol = Context.getWmlObjectFactory().createTblGridCol();
					gridCol.setW(BigInteger.valueOf(UnitsOfMeasurement.mmToTwip(columnSizes[i - 1])));
				}
			} else {
				gridCol = Context.getWmlObjectFactory().createTblGridCol();
				gridCol.setW(getPercentageTwips(totalWidthTwips, 100 / cols));
			}
			if (gridCol != null) {
				tblGrid.getGridCol().add(gridCol);
			}
		}

		// Now the rows
		for (int j = 1; j <= rows; j++) {
			Tr tr = Context.getWmlObjectFactory().createTr();
			tbl.getContent().add(tr);

			// The cells
			for (int i = 1; i <= cols; i++) {

				Tc tc = Context.getWmlObjectFactory().createTc();
				tr.getContent().add(tc);

				TcPr tcPr = Context.getWmlObjectFactory().createTcPr();
				tc.setTcPr(tcPr);
				TblWidth cellWidth = Context.getWmlObjectFactory().createTblWidth();
				tcPr.setTcW(cellWidth);
				cellWidth.setType("dxa");
				if (columnSizes != null) {
					if (columnSizesPercent) {
						cellWidth.setW(getPercentageTwips(totalWidthTwips, columnSizes[i - 1]));
					} else {
						cellWidth.setW(BigInteger.valueOf(UnitsOfMeasurement.mmToTwip(columnSizes[i - 1])));
					}
				} else {
					cellWidth.setW(getPercentageTwips(totalWidthTwips, 100 / cols));
				}
				// Cell content - an empty <w:p/>
				tc.getContent().add(Context.getWmlObjectFactory().createP());
			}

		}
		return tbl;
	}

	private static int[] get100Percentages(int[] columnSizes) {
		int[] ret = new int[columnSizes.length];
		System.arraycopy(columnSizes, 0, ret, 0, columnSizes.length);
		int sum = 0;
		for (int size : columnSizes) {
			sum += size;
		}
		int diff = 100 - sum;
		ret[columnSizes.length - 1] += diff;
		return ret;
	}

	private static BigInteger getPercentageTwips(int totalWidth, int percentage) {
		return BigInteger.valueOf((totalWidth / 100) * percentage);
	}

	/**
	 * Add a border with the provided width to the table.
	 *
	 * @param table
	 * @param width
	 */
	public static void addBorders(Tbl table, int width) {
		table.setTblPr(new TblPr());
		CTBorder border = new CTBorder();
		border.setColor("auto");
		border.setSz(BigInteger.valueOf(width));
		border.setSpace(new BigInteger("0"));
		border.setVal(STBorder.SINGLE);

		TblBorders borders = new TblBorders();
		borders.setBottom(border);
		borders.setLeft(border);
		borders.setRight(border);
		borders.setTop(border);
		borders.setInsideH(border);
		borders.setInsideV(border);
		table.getTblPr().setTblBorders(borders);
	}

	private static void repeatOnEveryPage(Tr tr) {
		BooleanDefaultTrue bdt = Context.getWmlObjectFactory().createBooleanDefaultTrue();
		TrPr trPr = new TrPr();
		trPr.getCnfStyleOrDivIdOrGridBefore().add(Context.getWmlObjectFactory().createCTTrPrBaseTblHeader(bdt));
		tr.setTrPr(trPr);
	}
}
