package ch.elexis.core.spotlight.ui.controls;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	 * Highlights the phrase in the specified StyledText widget where the search
	 * terms are connected with a plus sign ('+'). It treats the terms separated by
	 * plus signs as a single phrase to be highlighted with a yellow background. If
	 * the searchText does not contain a plus sign, it highlights each term
	 * separately.
	 * 
	 * For instance, given the searchText "test Ihre+Werbung", the method will
	 * highlight the phrase "test Ihre Werbung" if found in the text. If the
	 * searchText is "test Ihre Werbung" without plus signs, each word "test",
	 * "Ihre", and "Werbung" will be highlighted individually.
	 * 
	 * @param styledText The StyledText widget where search terms will be
	 *                   highlighted.
	 * @param searchText A string containing the terms to search for and highlight
	 *                   within the text. Terms connected with '+' indicate a single
	 *                   phrase.
	 */
	public static void highlightSearchText(StyledText styledText, String searchText) {
		String fullText = styledText.getText().toLowerCase();
		styledText.setStyleRange(null);
		int plusIndex = searchText.indexOf('+');
		if (plusIndex != -1) {
			String beforePlus = searchText.substring(0, plusIndex).toLowerCase();
			String afterPlus = searchText.substring(plusIndex + 1).toLowerCase();
			if (!beforePlus.isEmpty()) {
				String[] wordsBeforePlus = beforePlus.split("\\s+");
				String lastWordBeforePlus = wordsBeforePlus[wordsBeforePlus.length - 1];
				String exakt = lastWordBeforePlus + "+" + afterPlus;
				highlightExactPhrase(styledText, exakt.replace("+", " "), fullText);
			}
		} else {
			String[] searchWords = searchText.toLowerCase().split("\\s+");
			for (String word : searchWords) {
				if (word.isEmpty())
					continue;
				highlightWord(styledText, word, fullText);
			}
		}
	}

	/**
	 * Highlights the first occurrence of a specific phrase within the given
	 * {@link StyledText} widget. This method searches for the exact phrase in the
	 * full text and, if found, applies a yellow background highlight to the phrase.
	 * It uses a regular expression to find the phrase, escaping any special
	 * characters in the phrase to avoid unintended behavior. After highlighting the
	 * phrase, it centers the text view at the position of the found phrase. This
	 * method is useful for drawing attention to specific parts of the text.
	 *
	 * @param styledText The {@link StyledText} widget where the text is displayed.
	 * @param phrase     The exact phrase to search for and highlight.
	 * @param fullText   The full text contained within the {@link StyledText}
	 *                   widget.
	 */
	private static void highlightExactPhrase(StyledText styledText, String phrase, String fullText) {
		String searchRegex = escapeRegexSpecialCharacters(phrase);
		Pattern pattern = Pattern.compile(searchRegex);
		Matcher matcher = pattern.matcher(fullText);
		if (matcher.find()) {
			int index = matcher.start();
			StyleRange styleRange = new StyleRange();
			styleRange.start = index;
			styleRange.length = matcher.end() - index;
			styleRange.background = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
			styledText.setStyleRange(styleRange);
			centerTextAtPosition(styledText, index);
		}
	}

	/**
	 * Highlights all occurrences of a specific word within the given
	 * {@link StyledText} widget. Similar to
	 * {@link #highlightExactPhrase(StyledText, String, String)}, this method
	 * searches for and highlights all occurrences of the given word. Each found
	 * word is highlighted with a yellow background. The method uses a regular
	 * expression for the search, escaping any special characters in the word to
	 * ensure accurate matching. This is particularly useful for emphasizing every
	 * appearance of a word in a text.
	 *
	 * @param styledText The {@link StyledText} widget where the text is displayed.
	 * @param word       The word to search for and highlight across the full text.
	 * @param fullText   The full text contained within the {@link StyledText}
	 *                   widget.
	 */
	private static void highlightWord(StyledText styledText, String word, String fullText) {
		String searchRegex = escapeRegexSpecialCharacters(word);
		Pattern pattern = Pattern.compile(searchRegex);
		Matcher matcher = pattern.matcher(fullText);
		boolean isFirstMatch = true;
		while (matcher.find()) {
			int index = matcher.start();
			StyleRange styleRange = new StyleRange();
			styleRange.start = index;
			styleRange.length = matcher.end() - index;
			styleRange.background = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
			styledText.setStyleRange(styleRange);
			if (isFirstMatch) {
				centerTextAtPosition(styledText, index);
				isFirstMatch = false;
			}
		}
	}

	/**
	 * Transforms a search term to ensure correct handling of German umlauts and
	 * sharp S (ß), and escapes special characters for regex matching. It replaces
	 * 'ue', 'oe', 'ae', and 'ss' with patterns that match both the literal sequence
	 * and the corresponding umlaut or ß. Then, it escapes any special characters
	 * that could interfere with regex operations, except for '|', '(', and ')',
	 * which are necessary for the logic of umlaut recognition.
	 * 
	 * @param word The search term to be processed, which may include umlauts, sharp
	 *             S, or other special characters.
	 * @return A string modified for regex use, with umlauts replaced and special
	 *         characters escaped.
	 */
	private static String escapeRegexSpecialCharacters(String word) {
		String escapedWord = word.replaceAll("([\\\\\\[\\](){}.*+?^$|])", "\\\\$1")
				.replace("ue", "(ü|ue)").replace("oe", "(ö|oe)").replace("ae", "(ä|ae)").replace("ss", "(ß|ss)");
		return escapedWord;
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