package ch.elexis.core.eenv;

import java.io.IOException;

import ch.elexis.core.jdt.Nullable;

public interface IOcrMyPdfService {

	/**
	 * Send the provided application/pdf byte array to the ocrMyPdf service. If the
	 * pdf is already containing text, input is directly returned.
	 * 
	 * @param input
	 * @param parameters
	 *            to pass to the OcrMyPdf service, defaults to <code>-l deu</code>
	 * @return
	 * @throws IOException
	 * @see https://github.com/jbarlow83/OCRmyPDF/blob/master/misc/webservice.py
	 */
	public byte[] performOcr(byte[] input, @Nullable String parameters) throws IOException, OcrMyPdfException;

	/**
	 * An exception that is thrown by the OCRMyPdf Service. That is, the file was
	 * received, but there was some error in performing the OCR process on it.
	 */
	public static class OcrMyPdfException extends Exception {

		private static final long serialVersionUID = -6129161457684126686L;

		public enum TYPE {
			/** File could not be accessed, as its encrypted **/
			ENCRYPTED_FILE,
			/** File is a pdf form **/
			UNREADABLE_XFA_FORM_FILE,
			/** Other exception in the OcrMyPdf process **/
			OTHER;
		};

		private final TYPE type;
		private final String message;

		public OcrMyPdfException(TYPE type) {
			this(type, null);
		}

		public OcrMyPdfException(TYPE type, String message) {
			this.type = type;
			this.message = message;
		}

		public TYPE getType() {
			return type;
		}

		public String getMessage() {
			switch (getType()) {
				case ENCRYPTED_FILE :
					return "Unreadable - Input PDF is encrypted";
				case UNREADABLE_XFA_FORM_FILE :
					return "Unreadable -  PDF contains dynamic XFA forms";
				case OTHER :
					return message;
				default :
					return "UNKOWN";
			}
		}

	}
}
