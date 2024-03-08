package ch.elexis.core.spotlight.ui.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import ch.elexis.core.documents.DocumentStore;
import ch.elexis.core.model.IDocument;

/**
 * Provides utility methods for converting DOCX files to PDF and for creating
 * and updating IDocument instances for the purpose of previewing in Spotlight.
 * This class leverages Apache POI for DOCX processing and iText for PDF
 * manipulation.
 * 
 * @author Dalibor Aksic
 * @version 0.1
 */

public class DocumentConverter {
	private static Font font;
	/**
	 * Converts a DOCX file to a PDF and saves it to a temporary file. The margin on
	 * the left side of the PDF is set to 2.5 cm.
	 *
	 * @param docxPath The file path to the source DOCX document.
	 */
	public static void convertDocxToPdfAndSaveInTemp(String docxPath) {
		try (XWPFDocument docx = new XWPFDocument(new FileInputStream(docxPath))) {
			int words = 0;
			for (XWPFParagraph p : docx.getParagraphs()) {
				String[] wordArray = p.getText().split("\\s+"); // Trennt den Text in Wörter
				words += wordArray.length;
			}
			String outputPdfPath = System.getProperty("java.io.tmpdir") + "/convertedDocumentImproved.pdf";
			float marginLeft = 2.5f * 28.35f; // Konvertierung von Zentimetern zu Punkten (1 cm = 28.35 pt)
			Document pdfDocument = new Document(PageSize.A4, marginLeft, 50, 50, 50);
			PdfWriter.getInstance(pdfDocument, new FileOutputStream(outputPdfPath));
			pdfDocument.open();
			boolean contentAdded = false;
			boolean newPage = true;
			for (IBodyElement element : docx.getBodyElements()) {
				if (element instanceof XWPFParagraph) {
					XWPFParagraph paragraph = (XWPFParagraph) element;
					StringBuilder paragraphText = new StringBuilder();
					for (XWPFRun run : paragraph.getRuns()) {
						String fontFamily = run.getFontFamily();
						int fontSize = (run.getFontSize() == -1) ? 11 : run.getFontSize();
						font = FontFactory.getFont(fontFamily, fontSize, BaseColor.BLACK);
						paragraphText.append(run.toString());
					}
					Paragraph pdfParagraph = new Paragraph(paragraphText.toString(), font);
					if (paragraph.getText().matches("Seite \\d+")) {
						if (!newPage) {
							pdfDocument.newPage();
						}
						newPage = false;
					}
					pdfDocument.add(pdfParagraph);
					contentAdded |= processImagesFromParagraph(paragraph, pdfDocument);
					contentAdded = true;
				}
			}
			if (!contentAdded) {
				pdfDocument.add(new Paragraph(" "));
			}
			pdfDocument.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Processes images from a given XWPFParagraph and adds them to a PDF document.
	 *
	 * @param paragraph   The XWPFParagraph containing images to be processed.
	 * @param pdfDocument The Document to which images will be added.
	 * @return true if an image was added, false otherwise.
	 * @throws Exception if an error occurs during image processing.
	 */
	private static boolean processImagesFromParagraph(XWPFParagraph paragraph, Document pdfDocument) throws Exception {
	    boolean imageAdded = false;
	    for (XWPFRun run : paragraph.getRuns()) {
	        for (XWPFPicture picture : run.getEmbeddedPictures()) {
	            XWPFPictureData pictureData = picture.getPictureData();
	            if (pictureData != null) {
	                byte[] byteData = pictureData.getData();
	                Image image = Image.getInstance(byteData);
					float width = picture.getCTPicture().getSpPr().getXfrm().getExt().getCx() * 96 / 914400;
					float height = picture.getCTPicture().getSpPr().getXfrm().getExt().getCy() * 96 / 914400;
					image.scaleAbsolute(width * 0.75f, height * 0.75f);
	                pdfDocument.add(image);
	                imageAdded = true;
	            }
	        }
	    }
	    return imageAdded;
	}

	/**
	 * Creates an IDocument object for a PDF, updates its preview in Spotlight, and
	 * returns the IDocument object.
	 *
	 * @param docxDocument   The source IDocument representing the DOCX file.
	 * @param spotlightShell The SpotlightShell instance used for updating the PDF
	 *                       preview.
	 * @param documentStore  The DocumentStore used for creating the PDF IDocument.
	 * @return The newly created IDocument object for the PDF.
	 */
	public static IDocument createPdfIDocumentAndUpdatePreview(IDocument docxDocument, SpotlightShell spotlightShell,
			DocumentStore documentStore) {
		File tempFile = null;
		File pdfFile = null;
		IDocument pdfIDocument = null;
		try {
			tempFile = File.createTempFile("document", ".docx");
			tempFile.deleteOnExit();
			try (InputStream contentStream = docxDocument.getContent()) {
				Files.copy(contentStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			convertDocxToPdfAndSaveInTemp(tempFile.getAbsolutePath());
			pdfFile = new File(System.getProperty("java.io.tmpdir"), "convertedDocumentImproved.pdf");
			pdfIDocument = documentStore.createDocument(documentStore.getDefaultDocumentStore().getId(),
					docxDocument.getPatient().getId(), null, null);
			pdfIDocument.setMimeType("application/pdf");
			pdfIDocument.setTitle(docxDocument.getTitle());
			pdfIDocument.setPatient(docxDocument.getPatient());
			pdfIDocument.setAuthor(docxDocument.getAuthor());
			pdfIDocument.setStoreId(docxDocument.getStoreId());
			pdfIDocument.setExtension("pdf");
			try (InputStream pdfInputStream = new FileInputStream(pdfFile)) {
				pdfIDocument.setContent(pdfInputStream);
			}
			spotlightShell.adjustShellSize(true);
			spotlightShell.updatePdfPreview(pdfIDocument);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pdfIDocument;
	}
}
