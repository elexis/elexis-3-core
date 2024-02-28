package ch.elexis.core.spotlight.ui.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;

/**
 * Provides functionality to highlight search terms within a StyledText widget.
 * This class is designed to enhance text search visibility by marking search
 * terms with a specific background color, improving user experience in document
 * and consultation viewing contexts.
 * 
 * @author Dalibor Aksic
 * @version 0.1
 */
public class SpotlightSearchHelper {

	/**
	 * Highlights all occurrences of the given search terms in the specified
	 * StyledText widget. Each search term is highlighted with a yellow background
	 * for easy identification.
	 * 
	 * @param styledText The StyledText widget where search terms will be
	 *                   highlighted.
	 * @param searchText A string containing the terms to search and highlight
	 *                   within the text.
	 */
	public static void highlightSearchText(StyledText styledText, String searchText) {
		String fullText = styledText.getText().toLowerCase();
		String[] searchWords = searchText.toLowerCase().split("\\s+");
		styledText.setStyleRange(null);
		for (String word : searchWords) {
			if (word.isEmpty())
				continue;
			int index = 0;
			while ((index = fullText.indexOf(word, index)) != -1) {
				StyleRange styleRange = new StyleRange();
				styleRange.start = index;
				styleRange.length = word.length();
				styleRange.background = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
				styledText.setStyleRange(styleRange);
				index += word.length();
			}
		}
	}
}
