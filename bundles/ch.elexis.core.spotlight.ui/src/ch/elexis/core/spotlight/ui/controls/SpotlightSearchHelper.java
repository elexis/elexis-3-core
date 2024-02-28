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
		int firstIndex = -1;
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
				if (firstIndex == -1)
					firstIndex = index;
				index += word.length();
			}
		}
		if (firstIndex != -1) {
			centerTextAtPosition(styledText, firstIndex);
		}
	}

	/**
	 * Centers the StyledText widget's view on the line containing the specified
	 * position. This method calculates the optimal scroll position to make the line
	 * visible in the center of the visible area if possible.
	 * 
	 * @param styledText The StyledText widget to be scrolled.
	 * @param position   The character position in the text to be centered in the
	 *                   view.
	 */
	public static void centerTextAtPosition(StyledText styledText, int position) {
		Display.getDefault().asyncExec(() -> {
			if (!styledText.isDisposed()) {
				int line = styledText.getLineAtOffset(position);
				int linesVisible = styledText.getClientArea().height / styledText.getLineHeight();
				int topLine = line - (linesVisible / 6);
				styledText.setTopIndex(Math.max(topLine, 0));
				styledText.redraw();
			}
		});
	}
}
