package ch.elexis.core.pdfbox.ui.parts.handlers;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import ch.elexis.core.pdfbox.ui.parts.PdfPreviewPartLoadHandler;

/**
 * Provides functionality to highlight search terms in a PDF document. This
 * class assumes that an instance of {@link PDDocument} is available and
 * initialized. It uses a custom {@link PDFTextStripper} to locate and annotate
 * matches found in the document.
 *
 * @author Dalibor Aksic
 * @version 0.1
 */
public class PDFTextHighlighter {

	private PDDocument pdDocument;
	private boolean pageSet = false;

	/**
	 * Constructs a new PDFTextHighlighter with the specified PDF document.
	 *
	 * @param pdDocument The {@link PDDocument} to be used for text highlighting.
	 */
	public PDFTextHighlighter(PDDocument pdDocument) {
		this.pdDocument = pdDocument;
	}

	/**
	 * Highlights occurrences of the specified search text in the PDF document. This
	 * method supports both exact phrase searches and individual word searches
	 * within the text of the PDF. For exact phrases, the search text must include a
	 * "+" symbol between words. Otherwise, all individual words in the search text
	 * are highlighted separately throughout the document. The method iterates
	 * through the PDF document's text, applying highlight annotations for matches.
	 * It throws an IOException if the PDF document ({@code pdDocument}) is not
	 * loaded.
	 *
	 * @param searchText The text to search for within the PDF document. Use "+" for
	 *                   exact phrase searches. For example, "open+source" searches
	 *                   for the exact phrase "open source", whereas "open source"
	 *                   would highlight occurrences of "open" and "source"
	 *                   independently.
	 * @throws IOException If {@code pdDocument} is null or if there is an error
	 *                     accessing the document's pages.
	 */
	public void highlightSearchTextInPDF(String searchText) throws IOException {
		if (pdDocument == null) {
			throw new IOException("pdDocument is null");
		}

		boolean isExactPhrase = searchText.contains("+");
		String searchPattern = isExactPhrase ? searchText.replaceAll("\\+", " ") : searchText;
		PDFTextStripper stripper = new PDFTextStripper() {
			@Override
			protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
				String stringLower = string.toLowerCase();
				if (isExactPhrase) {
					int lastPlusIndex = searchText.lastIndexOf('+');
					if (lastPlusIndex != -1) {
						String afterLastPlus = searchText.substring(lastPlusIndex + 1);
						String combinedSearchPattern = searchText.substring(0, lastPlusIndex).replaceAll(".*\\s", "")
								+ " " + afterLastPlus;
						combinedSearchPattern = combinedSearchPattern.replace("+", " ").toLowerCase();
						highlightForExactPhrase(stringLower, combinedSearchPattern, textPositions);
					} else {
						highlightForExactPhrase(stringLower, searchPattern, textPositions);
					}
				} else {
					highlightForIndividualWords(stringLower, searchPattern, textPositions);
				}
			}

			private void highlightForExactPhrase(String stringLower, String searchPattern,
					List<TextPosition> textPositions) throws IOException {
				String searchRegex = escapeRegexSpecialCharacters(searchPattern);
				Pattern pattern = Pattern.compile(searchRegex);
				Matcher matcher = pattern.matcher(stringLower);
				while (matcher.find()) {
					highlightMatch(textPositions, matcher, pdDocument, getCurrentPageNo());
				}
			}

			private void highlightForIndividualWords(String stringLower, String searchPattern,
					List<TextPosition> textPositions) throws IOException {
				for (String word : searchPattern.split("\\s+")) {
					String regex = escapeRegexSpecialCharacters(word);
					Pattern pattern = Pattern.compile(regex);
					Matcher matcher = pattern.matcher(stringLower);
					while (matcher.find()) {
						highlightMatch(textPositions, matcher, pdDocument, getCurrentPageNo());
					}
				}
			}
		};
		stripper.setSortByPosition(true);
		stripper.getText(pdDocument);
	}

	/**
	 * Highlights a match in the PDF document by creating a text markup annotation.
	 * This method is called internally to highlight text that matches the search
	 * criteria. It calculates the annotation's position based on the text positions
	 * within the document. The method also ensures the highlighted page is visible
	 * in the preview, setting it as the current page if not already done.
	 *
	 * @param textPositions The positions of text items found by the
	 *                      PDFTextStripper.
	 * @param matcher       The Matcher object containing the start and end
	 *                      positions of the matched text.
	 * @param document      The PDF document where the text is being highlighted.
	 * @param pageNo        The page number where the match is found.
	 * @throws IOException If there is an error accessing the page or annotations of
	 *                     the document.
	 */
	private void highlightMatch(List<TextPosition> textPositions, Matcher matcher, PDDocument document, int pageNo)
			throws IOException {
		int textIndex = matcher.start();
		int endIndex = matcher.end();
		PDPage page = document.getPage(pageNo - 1); // Page index is zero-based
		PDRectangle cropBox = page.getCropBox();
		float pageHeight = cropBox.getHeight();
		TextPosition start = textPositions.get(textIndex);
		TextPosition end = textPositions.get(endIndex - 1);
		float tolerance = 5.0f;
		float width = end.getEndX() - start.getXDirAdj() + tolerance;
		float height = start.getHeightDir() * 2.5f;
		float newY = pageHeight - (start.getYDirAdj());
		PDAnnotationTextMarkup markup = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
		markup.setRectangle(new PDRectangle(start.getXDirAdj(), newY, width, height));
		markup.setQuadPoints(new float[] { start.getXDirAdj(), newY, start.getXDirAdj() + width, newY,
				start.getXDirAdj(), newY + height, start.getXDirAdj() + width, newY + height });
		markup.setColor(new PDColor(new float[] { 1, 1, 0 }, PDDeviceRGB.INSTANCE));
		page.getAnnotations().add(markup);
		if (!pageSet) {
			PdfPreviewPartLoadHandler.setCurrentPageNo(pageNo);
			pageSet = true;
		}
	}

	/**
	 * Transforms a search term for correct regex matching, including the handling
	 * of German umlauts and special characters, and prepares it for regex matching.
	 * 
	 * @param word The search term to be processed.
	 * @return A regex-friendly version of the search term, with special handling
	 *         for umlauts and special characters.
	 */
	private static String escapeRegexSpecialCharacters(String word) {
		String escapedWord = word.replaceAll("([\\\\\\[\\](){}.*+?^$|])", "\\\\$1").replace("ue", "(ü|ue)")
				.replace("oe", "(ö|oe)").replace("ae", "(ä|ae)").replace("ss", "(ß|ss)");
		return escapedWord;
	}
}