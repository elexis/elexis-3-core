/*******************************************************************************
 * Reusable rich text editor for findings composites.
 *
 * Shows text WYSIWYG (real bold/italic/underline/strikethrough, font and
 * background colour, bullet and numbered lists) on top of an SWT
 * {@link StyledText}, with a small toolbar. The model representation is a plain
 * string with inline markup: {@code <strong>}, {@code <em>}, {@code <u>},
 * {@code <s>}, {@code <span style="color:#RRGGBB">},
 * {@code <span style="background-color:#RRGGBB">} and {@code <ul>/<ol>/<li>} -
 * the same markup the docx text plugin and the diagnose view understand.
 *
 * Intended to be reused by DiagnoseListComposite, PersonalAnamnesisComposite,
 * FamilyAnamnesisComposite, RiskComposite, SocialAnamnesisComposite, ...
 ******************************************************************************/
package ch.elexis.core.findings.ui.composites;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import ch.elexis.core.l10n.Messages;

/**
 * Composite with a formatting toolbar and a {@link StyledText} that renders
 * inline markup. Use {@link #setText(String)} / {@link #getText()} to load and
 * store the markup.
 */
public class RichTextEditorComposite extends Composite {

	private static final int MK_BOLD = 0;
	private static final int MK_ITALIC = 1;
	private static final int MK_UNDERLINE = 2;
	private static final int MK_STRIKE = 3;
	private static final int MK_COLOR = 4;
	private static final int MK_BG = 5;

	private static final int LINE_NONE = 0;
	private static final int LINE_BULLET = 1;
	private static final int LINE_NUMBER = 2;

	private static final int BULLET_WIDTH = 30;

	private static final Pattern TAG = Pattern.compile(
			"</?(b|strong|i|em|u|s|strike|del)\\s*/?>" + 
			"|<span\\s+style\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s>]+))\\s*>" + 
			"|</span\\s*>",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern LIST_BLOCK = Pattern.compile("<(ul|ol)\\s*>(.*?)</\\1\\s*>",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private static final Pattern LIST_ITEM = Pattern.compile("<li\\s*>(.*?)</li\\s*>",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private final StyledText styledText;
	private final Map<RGB, Color> colorCache = new HashMap<>();

	public RichTextEditorComposite(Composite parent, int style) {
		super(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 2;
		setLayout(layout);

		createToolbar();

		styledText = new StyledText(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | style);
		styledText.setLayoutData(new GridData(GridData.FILL_BOTH));

		addDisposeListener(e -> colorCache.values().forEach(Color::dispose));
	}

	private void createToolbar() {
		ToolBar toolBar = new ToolBar(this, SWT.FLAT);
		toolBar.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));

		ToolItem bold = new ToolItem(toolBar, SWT.PUSH);
		bold.setText("𝐅");
		bold.setToolTipText(Messages.RichTextEditorComposite_Bold);
		bold.addListener(SWT.Selection, e -> toggleFlag(MK_BOLD));

		ToolItem italic = new ToolItem(toolBar, SWT.PUSH);
		italic.setText("𝐾");
		italic.setToolTipText(Messages.RichTextEditorComposite_Italic);
		italic.addListener(SWT.Selection, e -> toggleFlag(MK_ITALIC));

		ToolItem underline = new ToolItem(toolBar, SWT.PUSH);
		underline.setText("U\u0332"); // U combined with ‘Combining Low Line’
		underline.setToolTipText(Messages.RichTextEditorComposite_Underline);
		underline.addListener(SWT.Selection, e -> toggleFlag(MK_UNDERLINE));

		ToolItem strike = new ToolItem(toolBar, SWT.PUSH);
		strike.setText("a\u0336b\u0336"); // a and b combined with ‘Combining Long Stroke Overlay’
		strike.setToolTipText(Messages.RichTextEditorComposite_Strikethrough);
		strike.addListener(SWT.Selection, e -> toggleFlag(MK_STRIKE));

		new ToolItem(toolBar, SWT.SEPARATOR);

		ToolItem fgColor = new ToolItem(toolBar, SWT.PUSH);
		fgColor.setText("A\u0332"); // 'A' combined with an underscore (A̲)
		fgColor.setToolTipText(Messages.RichTextEditorComposite_FontColor);
		fgColor.addListener(SWT.Selection, e -> chooseColor(true));

		ToolItem bgColor = new ToolItem(toolBar, SWT.PUSH);
		bgColor.setText("\uD83D\uDD8D"); // Highlighter emoji (🖍)
		bgColor.setToolTipText(Messages.RichTextEditorComposite_BackgroundColor);
		bgColor.addListener(SWT.Selection, e -> chooseColor(false));

		new ToolItem(toolBar, SWT.SEPARATOR);

		ToolItem bullet = new ToolItem(toolBar, SWT.PUSH);
		bullet.setText("\u2022 \u2261"); // Dot + 3 lines (• ≡)
		bullet.setToolTipText(Messages.RichTextEditorComposite_BulletList);
		bullet.addListener(SWT.Selection, e -> toggleList(LINE_BULLET));

		ToolItem number = new ToolItem(toolBar, SWT.PUSH);
		number.setText("1. \u2261"); // 1st + 3 lines (1st ≡)
		number.setToolTipText(Messages.RichTextEditorComposite_NumberedList);
		number.addListener(SWT.Selection, e -> toggleList(LINE_NUMBER));
	}
	
	private void toggleFlag(int kind) {
		Point sel = styledText.getSelectionRange();
		if (sel == null || sel.y <= 0) {
			return;
		}
		int start = sel.x;
		int length = sel.y;
		boolean allSet = true;
		for (int i = 0; i < length; i++) {
			if (!hasFlag(styledText.getStyleRangeAtOffset(start + i), kind)) {
				allSet = false;
				break;
			}
		}
		boolean enable = !allSet;
		applyToSelection(start, length, sr -> setFlag(sr, kind, enable));
	}

	private void chooseColor(boolean foreground) {
		Point sel = styledText.getSelectionRange();
		if (sel == null || sel.y <= 0) {
			return;
		}
		ColorDialog dialog = new ColorDialog(getShell());
		RGB rgb = dialog.open();
		if (rgb == null) {
			return;
		}
		Color color = color(rgb);
		applyToSelection(sel.x, sel.y, sr -> {
			if (foreground) {
				sr.foreground = color;
			} else {
				sr.background = color;
			}
		});
	}

	private interface StyleMutator {
		void apply(StyleRange sr);
	}

	private void applyToSelection(int start, int length, StyleMutator mutator) {
		List<StyleRange> ranges = new ArrayList<>();
		StyleRange running = null;
		for (int i = 0; i < length; i++) {
			int offset = start + i;
			StyleRange source = styledText.getStyleRangeAtOffset(offset);
			StyleRange sr = source != null ? (StyleRange) source.clone() : new StyleRange();
			sr.start = offset;
			sr.length = 1;
			mutator.apply(sr);
			if (running != null && running.start + running.length == sr.start && similar(running, sr)) {
				running.length++;
			} else {
				running = sr;
				ranges.add(running);
			}
		}
		styledText.replaceStyleRanges(start, length, ranges.toArray(new StyleRange[0]));
	}

	private static boolean hasFlag(StyleRange sr, int kind) {
		if (sr == null) {
			return false;
		}
		switch (kind) {
		case MK_BOLD:
			return (sr.fontStyle & SWT.BOLD) != 0;
		case MK_ITALIC:
			return (sr.fontStyle & SWT.ITALIC) != 0;
		case MK_UNDERLINE:
			return sr.underline;
		case MK_STRIKE:
			return sr.strikeout;
		default:
			return false;
		}
	}

	private static void setFlag(StyleRange sr, int kind, boolean on) {
		switch (kind) {
		case MK_BOLD:
			sr.fontStyle = on ? (sr.fontStyle | SWT.BOLD) : (sr.fontStyle & ~SWT.BOLD);
			break;
		case MK_ITALIC:
			sr.fontStyle = on ? (sr.fontStyle | SWT.ITALIC) : (sr.fontStyle & ~SWT.ITALIC);
			break;
		case MK_UNDERLINE:
			sr.underline = on;
			break;
		case MK_STRIKE:
			sr.strikeout = on;
			break;
		default:
			break;
		}
	}

	private static boolean similar(StyleRange a, StyleRange b) {
		return a.fontStyle == b.fontStyle && a.underline == b.underline && a.strikeout == b.strikeout
				&& Objects.equals(a.foreground, b.foreground) && Objects.equals(a.background, b.background);
	}

	public void setText(String markup) {
		String value = normalizeListBlocks(markup != null ? markup : StringUtils.EMPTY);
		String[] rawLines = value.split("\n", -1);

		StringBuilder plain = new StringBuilder();
		List<CharStyle> styles = new ArrayList<>();
		List<Integer> lineTypes = new ArrayList<>();

		for (String rawLine : rawLines) {
			String trimmed = rawLine.trim();
			boolean ul = trimmed.regionMatches(true, 0, "<ul", 0, 3);
			boolean ol = trimmed.regionMatches(true, 0, "<ol", 0, 3);
			if (ul || ol) {
				int type = ol ? LINE_NUMBER : LINE_BULLET;
				Matcher im = LIST_ITEM.matcher(rawLine);
				while (im.find()) {
					addLine(plain, styles, lineTypes, im.group(1).trim(), type);
				}
			} else {
				addLine(plain, styles, lineTypes, rawLine, LINE_NONE);
			}
		}

		styledText.setText(plain.toString());
		styledText.setStyleRanges(buildRanges(styles));
		for (int i = 0; i < lineTypes.size() && i < styledText.getLineCount(); i++) {
			if (lineTypes.get(i) != LINE_NONE) {
				setLineBulletType(i, lineTypes.get(i));
			}
		}
		renumber();
	}

	private void addLine(StringBuilder plain, List<CharStyle> styles, List<Integer> lineTypes, String content,
			int type) {
		if (!lineTypes.isEmpty()) {
			plain.append('\n');
			styles.add(CharStyle.PLAIN);
		}
		parseContent(content, plain, styles);
		lineTypes.add(type);
	}

	private void parseContent(String content, StringBuilder plain, List<CharStyle> styles) {
		Matcher m = TAG.matcher(content);
		int last = 0;
		int bold = 0;
		int italic = 0;
		int underline = 0;
		int strike = 0;
		Deque<String> fg = new ArrayDeque<>();
		Deque<String> bg = new ArrayDeque<>();
		Deque<boolean[]> spans = new ArrayDeque<>();
		while (m.find()) {
			appendRun(plain, styles, content.substring(last, m.start()), bold, italic, underline, strike, fg.peek(),
					bg.peek());
			String token = m.group();
			if (m.group(1) != null) {
				boolean closing = token.charAt(1) == '/';
				int delta = closing ? -1 : 1;
				switch (m.group(1).toLowerCase()) {
				case "b":
				case "strong":
					bold = Math.max(0, bold + delta);
					break;
				case "i":
				case "em":
					italic = Math.max(0, italic + delta);
					break;
				case "u":
					underline = Math.max(0, underline + delta);
					break;
				case "s":
				case "strike":
				case "del":
					strike = Math.max(0, strike + delta);
					break;
				default:
					break;
				}
			} else if (m.group(2) != null || m.group(3) != null || m.group(4) != null) {
				String styleAttribute = m.group(2) != null ? m.group(2) : (m.group(3) != null ? m.group(3) : m.group(4));
				
				String foregroundColor = extractColor(styleAttribute, "color");
				String backgroundColor = extractColor(styleAttribute, "background-color");
				
				boolean setForeground = foregroundColor != null;
				boolean setBackground = backgroundColor != null;
				
				if (setForeground) {
					fg.push(foregroundColor);
				}
				if (setBackground) {
					bg.push(backgroundColor);
				}
				spans.push(new boolean[] { setForeground, setBackground });
			} else if (!spans.isEmpty()) {
				boolean[] rec = spans.pop();
				if (rec[0] && !fg.isEmpty()) {
					fg.pop();
				}
				if (rec[1] && !bg.isEmpty()) {
					bg.pop();
				}
			}
			last = m.end();
		}
		appendRun(plain, styles, content.substring(last), bold, italic, underline, strike, fg.peek(), bg.peek());
	}

	private static void appendRun(StringBuilder plain, List<CharStyle> styles, String text, int bold, int italic,
			int underline, int strike, String fg, String bg) {
		if (text.isEmpty()) {
			return;
		}
		CharStyle style = new CharStyle(bold > 0, italic > 0, underline > 0, strike > 0, fg, bg);
		for (int i = 0; i < text.length(); i++) {
			plain.append(text.charAt(i));
			styles.add(style);
		}
	}

	private StyleRange[] buildRanges(List<CharStyle> styles) {
		List<StyleRange> ranges = new ArrayList<>();
		int i = 0;
		while (i < styles.size()) {
			CharStyle style = styles.get(i);
			if (style.isPlain()) {
				i++;
				continue;
			}
			int start = i;
			while (i < styles.size() && styles.get(i).equals(style)) {
				i++;
			}
			StyleRange sr = new StyleRange();
			sr.start = start;
			sr.length = i - start;
			int fontStyle = SWT.NORMAL;
			if (style.bold) {
				fontStyle |= SWT.BOLD;
			}
			if (style.italic) {
				fontStyle |= SWT.ITALIC;
			}
			sr.fontStyle = fontStyle;
			sr.underline = style.underline;
			sr.strikeout = style.strike;
			sr.foreground = color(style.fg);
			sr.background = color(style.bg);
			ranges.add(sr);
		}
		return ranges.toArray(new StyleRange[0]);
	}

	private static String normalizeListBlocks(String value) {
		Matcher m = LIST_BLOCK.matcher(value);
		StringBuffer out = new StringBuffer();
		while (m.find()) {
			String collapsed = m.group().replaceAll("\\r?\\n[ \\t]*", StringUtils.EMPTY);
			m.appendReplacement(out, Matcher.quoteReplacement(collapsed));
		}
		m.appendTail(out);
		return out.toString();
	}

	public String getText() {
		int lineCount = styledText.getLineCount();
		List<String> units = new ArrayList<>();
		int i = 0;
		while (i < lineCount) {
			int type = effectiveLineType(i);
			if (type == LINE_NONE) {
				units.add(serializeLine(i, 0));
				i++;
			} else {
				String tag = type == LINE_NUMBER ? "ol" : "ul";
				StringBuilder block = new StringBuilder("<").append(tag).append(">");
				while (i < lineCount && effectiveLineType(i) == type) {
					int skip = lineType(i) == LINE_NONE ? dashMarkerLength(styledText.getLine(i)) : 0;
					block.append("<li>").append(serializeLine(i, skip)).append("</li>");
					i++;
				}
				block.append("</").append(tag).append(">");
				units.add(block.toString());
			}
		}
		return String.join("\n", units);
	}

	private int effectiveLineType(int lineIndex) {
		int type = lineType(lineIndex);
		if (type != LINE_NONE) {
			return type;
		}
		return dashMarkerLength(styledText.getLine(lineIndex)) > 0 ? LINE_BULLET : LINE_NONE;
	}


	private static int dashMarkerLength(String line) {
		if (line.length() < 2 || line.charAt(0) != '-') {
			return 0;
		}
		if (line.charAt(1) != ' ' && line.charAt(1) != '\t') {
			return 0;
		}
		int i = 1;
		while (i < line.length() && (line.charAt(i) == ' ' || line.charAt(i) == '\t')) {
			i++;
		}
		return i;
	}

	private String serializeLine(int lineIndex, int startCol) {
		String line = styledText.getLine(lineIndex);
		int base = styledText.getOffsetAtLine(lineIndex);
		StringBuilder sb = new StringBuilder();
		List<Mark> open = new ArrayList<>();
		for (int k = startCol; k < line.length(); k++) {
			List<Mark> want = marksFor(styleAt(base + k));
			int common = 0;
			while (common < open.size() && common < want.size() && open.get(common).equals(want.get(common))) {
				common++;
			}
			for (int x = open.size() - 1; x >= common; x--) {
				sb.append(closeTag(open.get(x)));
			}
			while (open.size() > common) {
				open.remove(open.size() - 1);
			}
			for (int x = common; x < want.size(); x++) {
				sb.append(openTag(want.get(x)));
				open.add(want.get(x));
			}
			sb.append(line.charAt(k));
		}
		for (int x = open.size() - 1; x >= 0; x--) {
			sb.append(closeTag(open.get(x)));
		}
		return sb.toString();
	}

	private CharStyle styleAt(int offset) {
		StyleRange sr = styledText.getStyleRangeAtOffset(offset);
		if (sr == null) {
			return CharStyle.PLAIN;
		}
		return new CharStyle((sr.fontStyle & SWT.BOLD) != 0, (sr.fontStyle & SWT.ITALIC) != 0, sr.underline,
				sr.strikeout, hex(sr.foreground), hex(sr.background));
	}

	private static List<Mark> marksFor(CharStyle style) {
		List<Mark> marks = new ArrayList<>();
		if (style.bold) {
			marks.add(new Mark(MK_BOLD, null));
		}
		if (style.italic) {
			marks.add(new Mark(MK_ITALIC, null));
		}
		if (style.underline) {
			marks.add(new Mark(MK_UNDERLINE, null));
		}
		if (style.strike) {
			marks.add(new Mark(MK_STRIKE, null));
		}
		if (style.fg != null) {
			marks.add(new Mark(MK_COLOR, style.fg));
		}
		if (style.bg != null) {
			marks.add(new Mark(MK_BG, style.bg));
		}
		return marks;
	}

	private static String openTag(Mark mark) {
		switch (mark.kind) {
		case MK_BOLD:
			return "<strong>";
		case MK_ITALIC:
			return "<em>";
		case MK_UNDERLINE:
			return "<u>";
		case MK_STRIKE:
			return "<s>";
		case MK_COLOR:
			return "<span style=\"color:" + mark.value + "\">";
		case MK_BG:
			return "<span style=\"background-color:" + mark.value + "\">";
		default:
			return StringUtils.EMPTY;
		}
	}

	private static String closeTag(Mark mark) {
		switch (mark.kind) {
		case MK_BOLD:
			return "</strong>";
		case MK_ITALIC:
			return "</em>";
		case MK_UNDERLINE:
			return "</u>";
		case MK_STRIKE:
			return "</s>";
		case MK_COLOR:
		case MK_BG:
			return "</span>";
		default:
			return StringUtils.EMPTY;
		}
	}

	private void toggleList(int type) {
		int caret = styledText.getCaretOffset();
		Point sel = styledText.getSelectionRange();
		int startOffset = sel != null ? sel.x : caret;
		int endOffset = (sel != null && sel.y > 0) ? sel.x + sel.y - 1 : caret;
		int firstLine = styledText.getLineAtOffset(Math.min(startOffset, endOffset));
		int lastLine = styledText.getLineAtOffset(Math.max(startOffset, endOffset));

		boolean allSame = true;
		for (int l = firstLine; l <= lastLine; l++) {
			if (lineType(l) != type) {
				allSame = false;
				break;
			}
		}
		for (int l = firstLine; l <= lastLine; l++) {
			if (allSame) {
				setLineBulletType(l, LINE_NONE);
			} else {
				stripLeadingMarker(l);
				setLineBulletType(l, type);
			}
		}
		renumber();
	}

	private void stripLeadingMarker(int lineIndex) {
		String line = styledText.getLine(lineIndex);
		int strip = leadingMarkerLength(line);
		if (strip > 0) {
			styledText.replaceTextRange(styledText.getOffsetAtLine(lineIndex), strip, StringUtils.EMPTY);
		}
	}

	private static int leadingMarkerLength(String line) {
		if (line.isEmpty()) {
			return 0;
		}
		char first = line.charAt(0);
		if (first == '-' || first == '•') {
			int i = 1;
			if (i < line.length() && (line.charAt(i) == ' ' || line.charAt(i) == '\t')) {
				i++;
			}
			return i;
		}
		int digits = 0;
		while (digits < line.length() && Character.isDigit(line.charAt(digits))) {
			digits++;
		}
		if (digits > 0 && digits < line.length() && (line.charAt(digits) == '.' || line.charAt(digits) == ')')) {
			int i = digits + 1;
			if (i < line.length() && (line.charAt(i) == ' ' || line.charAt(i) == '\t')) {
				i++;
			}
			return i;
		}
		return 0;
	}

	private int lineType(int lineIndex) {
		Bullet bullet = styledText.getLineBullet(lineIndex);
		if (bullet == null) {
			return LINE_NONE;
		}
		if ((bullet.type & ST.BULLET_DOT) != 0) {
			return LINE_BULLET;
		}
		return LINE_NUMBER;
	}

	private void setLineBulletType(int lineIndex, int type) {
		styledText.setLineBullet(lineIndex, 1, null);
		switch (type) {
		case LINE_BULLET:
			styledText.setLineBullet(lineIndex, 1, createDotBullet());
			break;
		case LINE_NUMBER:
			styledText.setLineBullet(lineIndex, 1, createNumberBullet());
			break;
		default:
			break;
		}
	}

	private void renumber() {
		int lineCount = styledText.getLineCount();
		if (lineCount == 0) {
			return;
		}
		int[] types = new int[lineCount];
		for (int i = 0; i < lineCount; i++) {
			types[i] = lineType(i);
		}
		styledText.setLineBullet(0, lineCount, null);

		int i = 0;
		while (i < lineCount) {
			if (types[i] == LINE_BULLET) {
				styledText.setLineBullet(i, 1, createDotBullet());
				i++;
			} else if (types[i] == LINE_NUMBER) {
				int start = i;
				while (i < lineCount && types[i] == LINE_NUMBER) {
					i++;
				}
				styledText.setLineBullet(start, i - start, createNumberBullet());
			} else {
				i++;
			}
		}
	}

	private Bullet createDotBullet() {
		return new Bullet(ST.BULLET_DOT, bulletStyle());
	}

	private Bullet createNumberBullet() {
		Bullet bullet = new Bullet(ST.BULLET_NUMBER | ST.BULLET_TEXT, bulletStyle());
		bullet.text = ".";
		return bullet;
	}

	private static StyleRange bulletStyle() {
		StyleRange style = new StyleRange();
		style.metrics = new GlyphMetrics(0, 0, BULLET_WIDTH);
		return style;
	}

	private Color color(String hex) {
		RGB rgb = toRgb(hex);
		if (rgb == null) {
			return null;
		}
		return colorCache.computeIfAbsent(rgb, r -> new Color(getDisplay(), r));
	}

	private Color color(RGB rgb) {
		return colorCache.computeIfAbsent(rgb, r -> new Color(getDisplay(), r));
	}

	private static RGB toRgb(String hex) {
		if (hex == null) {
			return null;
		}
		String h = hex.startsWith("#") ? hex.substring(1) : hex;
		if (h.length() != 6) {
			return null;
		}
		try {
			return new RGB(Integer.parseInt(h.substring(0, 2), 16), Integer.parseInt(h.substring(2, 4), 16),
					Integer.parseInt(h.substring(4, 6), 16));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static String hex(Color color) {
		if (color == null) {
			return null;
		}
		RGB rgb = color.getRGB();
		return String.format("#%02X%02X%02X", rgb.red, rgb.green, rgb.blue);
	}

	private static String extractColor(String style, String key) {
		if (style == null) {
			return null;
		}
		style = style.replace("&quot;", "").replace("&apos;", "").replace("\"", "").replace("'", "");
		for (String declaration : style.split(";")) {
			int colon = declaration.indexOf(':');
			if (colon < 0) {
				continue;
			}
			if (declaration.substring(0, colon).trim().equalsIgnoreCase(key)) {
				RGB rgb = toRgb(declaration.substring(colon + 1).trim());
				if (rgb != null) {
					return String.format("#%02X%02X%02X", rgb.red, rgb.green, rgb.blue);
				}
			}
		}
		return null;
	}

	public StyledText getStyledText() {
		return styledText;
	}

	public void addModifyListener(ModifyListener listener) {
		styledText.addModifyListener(listener);
	}

	public void setEditable(boolean editable) {
		styledText.setEditable(editable);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		styledText.setEnabled(enabled);
	}

	private static final class CharStyle {
		static final CharStyle PLAIN = new CharStyle(false, false, false, false, null, null);

		final boolean bold;
		final boolean italic;
		final boolean underline;
		final boolean strike;
		final String fg;
		final String bg;

		CharStyle(boolean bold, boolean italic, boolean underline, boolean strike, String fg, String bg) {
			this.bold = bold;
			this.italic = italic;
			this.underline = underline;
			this.strike = strike;
			this.fg = fg;
			this.bg = bg;
		}

		boolean isPlain() {
			return !bold && !italic && !underline && !strike && fg == null && bg == null;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof CharStyle)) {
				return false;
			}
			CharStyle c = (CharStyle) o;
			return bold == c.bold && italic == c.italic && underline == c.underline && strike == c.strike
					&& Objects.equals(fg, c.fg) && Objects.equals(bg, c.bg);
		}

		@Override
		public int hashCode() {
			return Objects.hash(bold, italic, underline, strike, fg, bg);
		}
	}

	private static final class Mark {
		final int kind;
		final String value;

		Mark(int kind, String value) {
			this.kind = kind;
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Mark)) {
				return false;
			}
			Mark m = (Mark) o;
			return kind == m.kind && Objects.equals(value, m.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(kind, value);
		}
	}
}
