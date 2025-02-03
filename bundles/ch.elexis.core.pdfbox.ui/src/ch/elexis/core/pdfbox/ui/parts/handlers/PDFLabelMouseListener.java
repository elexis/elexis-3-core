package ch.elexis.core.pdfbox.ui.parts.handlers;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class PDFLabelMouseListener {

	private Map<Integer, List<Rectangle>> markedAreasPerPage;
	private String selectionText = "";
	private Image[] images;
	private int startX;
	private int startY;
	private int endX;
	private int endY;
	private Image[] labelBackgrounds;
	private GC[] gcBackgrounds;
	private PDFTextExtractor pdfTextExtractor;
	private Menu menu;

	private String finalText;

	private Rectangle currentSelection;

	public PDFLabelMouseListener(Map<Integer, List<Rectangle>> markedAreasPerPage, Image[] images,
			Image[] labelBackgrounds, GC[] gcBackgrounds, PDFTextExtractor pdfTextExtractor) {
		this.markedAreasPerPage = markedAreasPerPage;
		this.images = images;
		this.labelBackgrounds = labelBackgrounds;
		this.gcBackgrounds = gcBackgrounds;
		this.pdfTextExtractor = pdfTextExtractor;
		this.currentSelection = null;
	}

	public void addMouseListenersToLabel(Label label, int pageIndex, int j) {

		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 1) {
					if ((e.stateMask & SWT.CTRL) == 0) {
						clearSelection(pageIndex);
						label.redraw();
					}
					startX = e.x;
					startY = e.y;
				}
			}

			@Override
			public void mouseUp(MouseEvent e) {
				if (e.button == 1) {
					endX = e.x;
					endY = e.y;
					if (startX != endX && startY != endY) {
						if (currentSelection != null) {
							markedAreasPerPage.computeIfAbsent(pageIndex, k -> new ArrayList<>()).add(currentSelection);
							currentSelection = null;
						}
						String test1 = pdfTextExtractor.extractTextFromMarkedAreas();
						markedAreasPerPage.clear();
						adjustSelectionToEndOfLine(pageIndex, startX, startY, endX, endY);
						String test2 = pdfTextExtractor.extractTextFromMarkedAreas();
						finalText = trimToMatch(test1, test2);
						showContextMenu(label, endX, endY);
					}
					label.redraw();
				}
				if (e.button == 3) {
					clearSelection(pageIndex);
					label.redraw();
				}
			}
		});

		label.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				if ((e.stateMask & SWT.BUTTON1) != 0) {
					int oldEndX = endX;
					int oldEndY = endY;
					endX = e.x;
					endY = e.y;

					currentSelection = new Rectangle(Math.min(startX, endX), Math.min(startY, endY),
							Math.abs(endX - startX), Math.abs(endY - startY));
					label.redraw(Math.min(startX, oldEndX), Math.min(startY, oldEndY), Math.abs(oldEndX - startX),
							Math.abs(oldEndY - startY), false);
					label.redraw(Math.min(startX, endX), Math.min(startY, endY), Math.abs(endX - startX),
							Math.abs(endY - startY), false);
				}
			}
		});

		label.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				if (labelBackgrounds[j] == null) {
					labelBackgrounds[j] = new Image(Display.getDefault(), images[j].getBounds());
					gcBackgrounds[j] = new GC(labelBackgrounds[j]);
					gcBackgrounds[j].drawImage(images[j], 0, 0);
					gcBackgrounds[j].dispose();
					gcBackgrounds[j] = null;
				}

				e.gc.drawImage(labelBackgrounds[j], 0, 0, labelBackgrounds[j].getBounds().width,
						labelBackgrounds[j].getBounds().height, 0, 0, label.getSize().x, label.getSize().y);

				List<Rectangle> markedAreas = markedAreasPerPage.get(pageIndex);
				if (markedAreas != null) {
					Color highlightColor = new Color(Display.getDefault(), 0, 0, 255);
					try {
						e.gc.setBackground(highlightColor);
						e.gc.setAlpha(30);
						for (Rectangle rect : markedAreas) {
							e.gc.fillRectangle(rect.x, rect.y, rect.width, rect.height);
						}
					} finally {
						highlightColor.dispose();
					}
				}

				if (currentSelection != null) {
					Color highlightColor = new Color(Display.getDefault(), 0, 0, 255);
					try {
						e.gc.setBackground(highlightColor);
						e.gc.setAlpha(30);
						e.gc.fillRectangle(currentSelection.x, currentSelection.y, currentSelection.width,
								currentSelection.height);
					} finally {
						highlightColor.dispose();
					}
				}
			}
		});
		label.addDisposeListener(e -> {
			if (labelBackgrounds[j] != null && !labelBackgrounds[j].isDisposed()) {
				labelBackgrounds[j].dispose();
				labelBackgrounds[j] = null;
			}
		});
	}

	public void disposeResources() {
		for (int i = 0; i < labelBackgrounds.length; i++) {
			if (labelBackgrounds[i] != null && !labelBackgrounds[i].isDisposed()) {
				labelBackgrounds[i].dispose();
			}
			if (gcBackgrounds[i] != null && !gcBackgrounds[i].isDisposed()) {
				gcBackgrounds[i].dispose();
			}
		}
	}

	private void adjustSelectionToEndOfLine(int pageIndex, int startX, int startY, int endX, int endY) {
		int adjustedEndX = images[pageIndex].getBounds().width - 1;
		for (int y = startY; y < endY; y += 20) {
			int bottomY = y + 28;
			if (bottomY >= endY) {
				currentSelection = new Rectangle(startX, y, endX - startX, endY - y);
				markedAreasPerPage.computeIfAbsent(pageIndex, k -> new ArrayList<>()).add(currentSelection);
				currentSelection = null;
			} else {
				currentSelection = new Rectangle(startX, y, adjustedEndX - startX, 20);
				markedAreasPerPage.computeIfAbsent(pageIndex, k -> new ArrayList<>()).add(currentSelection);
				startX = 0;
			}
		}
	}

	private String trimToMatch(String shortText, String longText) {
		String lastLine = getLastLine(shortText);
		int endIndex = longText.lastIndexOf(lastLine);
		if (endIndex != -1) {
			String trimmedText = longText.substring(0, endIndex + lastLine.length()).trim();
			finalText = trimmedText;
			return trimmedText;
		}
		return longText;
	}

	private String getLastLine(String text) {
		String[] lines = text.split("\\r?\\n");
		if (lines.length > 0) {
			return lines[lines.length - 1].trim();
		}
		return text;
	}

	public void clearSelection(int pageIndex) {
		startX = 0;
		startY = 0;
		endX = 0;
		endY = 0;
		currentSelection = null;
		if (pageIndex >= 0) {
			markedAreasPerPage.remove(pageIndex);
		} else {
			markedAreasPerPage.clear();
		}
	}

	private void showContextMenu(Label label, int x, int y) {
		menu = new Menu(label.getShell(), SWT.POP_UP);
		MenuItem copyItem = new MenuItem(menu, SWT.PUSH);
		copyItem.setText("Kopieren");
		copyItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String textToCopy;
				boolean validSelection = markedAreasPerPage.values().stream().flatMap(List::stream)
						.anyMatch(rect -> rect.width > 0 && rect.height > 0);
				if (!validSelection) {
					textToCopy = pdfTextExtractor.extractTextFromDocument();
				} else {
					selectionText = finalText;
					textToCopy = selectionText;
				}
				if (!textToCopy.isEmpty()) {
					Clipboard clipboard = new Clipboard(Display.getDefault());
					clipboard.setContents(new Object[] { textToCopy }, new Transfer[] { TextTransfer.getInstance() });
					clipboard.dispose();
				}
				clearSelection(-1);
				menu.dispose();
			}
		});
		menu.setLocation(label.toDisplay(x, y));
		menu.setVisible(true);
    }
}
