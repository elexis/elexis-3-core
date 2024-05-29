package ch.elexis.core.pdfbox.ui.parts.handlers;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.pdfbox.text.TextPosition;
import org.eclipse.swt.graphics.Image;

public class PDFTextExtractor {

	private PDDocument pdDocument;
	private Image[] images;
	private Map<Integer, List<Rectangle>> markedAreasPerPage;

	public PDFTextExtractor(PDDocument pdDocument, Image[] images, Map<Integer, List<Rectangle>> markedAreasPerPage) {
		this.pdDocument = pdDocument;
		this.images = images;
		this.markedAreasPerPage = markedAreasPerPage;
	}

	public String extractTextFromMarkedAreas() {
		StringBuilder extractedText = new StringBuilder();
		try {
			for (int pageIndex : markedAreasPerPage.keySet()) {
				PDPage page = pdDocument.getPage(pageIndex);
				PDRectangle pageSize = page.getMediaBox();
				float pageWidth = pageSize.getWidth();
				float pageHeight = pageSize.getHeight();
				float scaleX = pageWidth / images[pageIndex].getBounds().width;
				float scaleY = pageHeight / images[pageIndex].getBounds().height;
				PDFTextStripperByArea stripperByArea = new PDFTextStripperByArea();
				stripperByArea.setSortByPosition(true);
				stripperByArea.setStartPage(pageIndex + 1);
				stripperByArea.setEndPage(pageIndex + 1);
				int regionCount = 1;
				for (Rectangle rect : markedAreasPerPage.get(pageIndex)) {
					int pdfStartX = (int) (rect.x * scaleX);
					int pdfStartY = (int) (rect.y * scaleY);
					int pdfEndX = (int) ((rect.x + rect.width) * scaleX);
					int pdfEndY = (int) ((rect.y + rect.height) * scaleY);
					int x = Math.max(0, pdfStartX);
					int y = Math.max(0, pdfStartY);
					int width = Math.min((int) pageWidth, pdfEndX - pdfStartX + 2);
					int height = Math.min((int) pageHeight, pdfEndY - pdfStartY + 2);
					stripperByArea.addRegion("region" + regionCount++, new java.awt.Rectangle(x, y, width, height));
				}
				stripperByArea.extractRegions(page);
				for (int i = 1; i < regionCount; i++) {
					extractedText.append(stripperByArea.getTextForRegion("region" + i)).append("\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return extractedText.toString();
	}

	public String extractTextFromDocument() {
		StringBuilder extractedText = new StringBuilder();
		try {
			int numberOfPages = pdDocument.getNumberOfPages();
			PDFTextStripperByArea stripperByArea = new PDFTextStripperByArea();
			stripperByArea.setSortByPosition(true);
			for (int i = 0; i < numberOfPages; i++) {
				PDPage page = pdDocument.getPage(i);
				stripperByArea.addRegion("page" + (i + 1), new java.awt.Rectangle(0, 0,
						(int) page.getMediaBox().getWidth(), (int) page.getMediaBox().getHeight()));
				stripperByArea.extractRegions(page);
				extractedText.append(stripperByArea.getTextForRegion("page" + (i + 1))).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return extractedText.toString();
	}

	// Method to extract rectangles for a specific text on a specific page
	public List<Rectangle> extractRectanglesForText(String searchText, int pageIndex) {
		List<Rectangle> textRectangles = new ArrayList<>();
		try {
			PDPage page = pdDocument.getPage(pageIndex);
			PDRectangle pageSize = page.getMediaBox();
			float pageWidth = pageSize.getWidth();
			float pageHeight = pageSize.getHeight();
			float scaleX = pageWidth / images[pageIndex].getBounds().width;
			float scaleY = pageHeight / images[pageIndex].getBounds().height;
			PDFTextStripperByArea stripperByArea = new PDFTextStripperByArea() {
				@Override
				protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
					if (string.contains(searchText)) {
						for (TextPosition text : textPositions) {
							if (text.getUnicode().contains(searchText)) {
								float x = text.getXDirAdj();
								float y = text.getYDirAdj();
								float width = text.getWidthDirAdj();
								float height = text.getHeightDir();
								int rectX = (int) (x / scaleX);
								int rectY = (int) ((pageHeight - y) / scaleY);
								int rectWidth = (int) (width / scaleX);
								int rectHeight = (int) (height / scaleY);
								textRectangles.add(new Rectangle(rectX, rectY, rectWidth, rectHeight));
							}
						}
					}
				}
			};
			stripperByArea.setSortByPosition(true);
			stripperByArea.setStartPage(pageIndex + 1);
			stripperByArea.setEndPage(pageIndex + 1);
			stripperByArea.extractRegions(page);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return textRectangles;
	}
}
