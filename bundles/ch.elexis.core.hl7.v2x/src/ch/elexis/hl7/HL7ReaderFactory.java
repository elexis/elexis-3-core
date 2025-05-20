package ch.elexis.hl7;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.GenericMessage.V23;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Hl7InputStreamMessageStringIterator;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.services.holder.VirtualFilesystemServiceHolder;
import ch.elexis.hl7.v26.Messages;
import ch.elexis.hl7.v2x.HL7ReaderV21;
import ch.elexis.hl7.v2x.HL7ReaderV22;
import ch.elexis.hl7.v2x.HL7ReaderV23;
import ch.elexis.hl7.v2x.HL7ReaderV231;
import ch.elexis.hl7.v2x.HL7ReaderV24;
import ch.elexis.hl7.v2x.HL7ReaderV25;
import ch.elexis.hl7.v2x.HL7ReaderV251;
import ch.elexis.hl7.v2x.HL7ReaderV26;

public enum HL7ReaderFactory {

	INSTANCE;

	protected List<Message> messageList;

	private static Logger logger = LoggerFactory.getLogger(HL7ReaderFactory.class);

	public List<HL7Reader> getReader(File file) throws IOException {
		IVirtualFilesystemHandle fileHandle = VirtualFilesystemServiceHolder.get().of(file);
		return getReader(fileHandle);
	}

	public List<HL7Reader> getReader(IVirtualFilesystemHandle file) throws IOException {
		checkClassLoader();

		messageList = new ArrayList<>();
		return load(file);
	}

	public @Nullable HL7Reader getReader(String message) throws IOException {
		checkClassLoader();

		messageList = new ArrayList<>();
		try {
			return loadMessage(message);
		} catch (HL7Exception e) {
			throw new IOException(HL7Exception.class.getName() + ": " + e.getMessage());
		}
	}

	private List<HL7Reader> load(IVirtualFilesystemHandle file) throws IOException {
		if (!file.canRead()) {
			throw new IOException(MessageFormat.format(Messages.HL7Reader_CannotReadFile, file.getAbsolutePath()));
		}

		List<HL7Reader> ret = new ArrayList<>();
		try (InputStream inputStream = getFileInputStream(file)) {
			// HAPI utility class will iterate over the messages which appear over an
			// InputStream
			Hl7InputStreamMessageStringIterator stringIterator = new Hl7InputStreamMessageStringIterator(inputStream);

			Parser p = new PipeParser();
			p.setValidationContext(new NoValidation());

			while (stringIterator.hasNext()) {
				String next = stringIterator.next();
				next = assureSaveMessage(next);
				Message hl7Message = p.parse(next);
				messageList.add(hl7Message);
				ret.add(getReaderForMessage(hl7Message));
			}
			return ret;
		} catch (Exception ex) {
			throw new IOException(ex);
		}
	}

	private void checkClassLoader() {
		ClassLoader modelLoader = V23.class.getClassLoader();
		ClassLoader parserLoader = Parser.class.getClassLoader();
		if (modelLoader != parserLoader) {
			throw new IllegalStateException("Model and Parser loaded by different ClassLoader");
		}
	}

	private InputStream getFileInputStream(IVirtualFilesystemHandle fileHandle) throws IOException {
		byte[] bytes = fileHandle.readAllBytes();
		CharsetDetector detector = new CharsetDetector();
		detector.setText(bytes);
		CharsetMatch match = detector.detect();

		if (match != null) {
			if (match.getName().contains("IBM424")) {
				logger.warn("Reading HL7 file " + fileHandle.getAbsolutePath() + " with unsupported encoding "
						+ match.getName() + " - trying to use ISO-8859-1 instead");

				return new ByteArrayInputStream(new String(bytes, "ISO-8859-1").getBytes());
			} else if (match.getName().contains("Big5")) {
				CharsetMatch[] allMatches = detector.detectAll();
				for (CharsetMatch charsetMatch : allMatches) {
					if (charsetMatch.getName().equals("ISO-8859-1") && charsetMatch.getConfidence() > 25) {
						logger.warn("Reading HL7 file " + fileHandle.getAbsolutePath() + " with unlikely encoding "
								+ match.getName() + " - trying to use ISO-8859-1 instead");

						return new ByteArrayInputStream(new String(bytes, "ISO-8859-1").getBytes());
					}
				}
			}
			logger.info("Reading HL7 file " + fileHandle.getAbsolutePath() + " encoded " + match.getName()
					+ " language " + match.getLanguage());
			return new ByteArrayInputStream(match.getString().getBytes());
		}

		return new ByteArrayInputStream(bytes);
	}

	private String assureSaveMessage(String hl7Message) {
		String ret = assureSaveMSH9Access(hl7Message);
		ret = assureSaveORC(ret);
		return ret;
	}

	private String[] getLines(String hl7Message) {
		String separator = StringUtils.CR;
		String[] splitted = hl7Message.split(separator);
		if (splitted.length < 2) {
			separator = StringUtils.LF;
			splitted = hl7Message.split(separator);
		}
		// make sure no new lines at beginning of string
		for (int i = 0; i < splitted.length; i++) {
			splitted[i] = splitted[i].replaceAll(StringUtils.LF, StringUtils.EMPTY);
		}
		return splitted;
	}

	private int getIndexOfSegment(String[] splittedMessage, String segmentStart) {
		int index = 0;
		boolean found = false;
		for (; index < splittedMessage.length; index++) {
			if (splittedMessage[index].startsWith(segmentStart)) {
				found = true;
				break;
			}
		}
		return found ? index : -1;
	}

	private String assureSaveORC(String hl7Message) {
		String[] splitted = getLines(hl7Message);
		if (splitted.length < 2) {
			throw new IllegalArgumentException("Could not split message");
		}
		List<String> splittedList = new ArrayList<>(Arrays.asList(splitted));
		String[] mshPart = splitted[0].split("\\|", -1);

		if (mshPart[8].contains("OUL^R22")) {
			int orcIndex = getIndexOfSegment(splitted, "ORC|");
			int obrIndex = getIndexOfSegment(splitted, "OBR|");
			if (orcIndex > 0 && obrIndex > 0 && orcIndex < obrIndex) {
				Collections.swap(splittedList, orcIndex, obrIndex);
			}
		}
		return joinStrings(splittedList.toArray(new String[splittedList.size()]), "\r\n");
	}

	private String assureSaveMSH9Access(String hl7Message) {
		String[] splitted = getLines(hl7Message);
		if (splitted.length < 2) {
			throw new IllegalArgumentException("Could not split message");
		}

		String[] mshPart = splitted[0].split("\\|", -1);

		if (mshPart[1].length() < 4) {
			logger.warn("Replacing msg header encoding characters [" + mshPart[1] + "] with default");
			mshPart[1] = "^~\\&";
			splitted[0] = joinStrings(mshPart, "|");
		}

		if (!mshPart[8].equals("ORU^R01") && !mshPart[11].startsWith("2.5") && !mshPart[11].startsWith("2.6")) {
			mshPart[8] = "ORU^R01";
			splitted[0] = joinStrings(mshPart, "|");
		}

		// 2.3.2 is no proper Hl7 version and therefore needs to be handled as version
		// 2.3
		if (mshPart[11].equals("2.3.2")) {
			mshPart[11] = "2.3";
			splitted[0] = joinStrings(mshPart, "|");
		}

		// #2747 BugFix as LabCube_SpotChemD sends occasionally SN which is not allowed
		// for version
		// 2.2
		if (mshPart[11].equals("2.2")) {
			for (int i = 0; i < splitted.length; i++) {
				if (splitted[i].startsWith("OBX")) {
					String[] obxPart = splitted[i].split("\\|", -1);
					if (obxPart[2].equals("SN")) {
						obxPart[2] = "NM";
						splitted[i] = joinStrings(obxPart, "|");
					}
				}
			}
		}

		if (mshPart[11].equals("2.7.1")) {
			mshPart[11] = "2.6";
			splitted[0] = joinStrings(mshPart, "|");
		}

		return joinStrings(splitted, "\r\n");
	}

	private String joinStrings(String[] array, String separator) {
		StringBuilder builder = new StringBuilder();
		for (String s : array) {
			if (builder.length() != 0) {
				builder.append(separator);
			}
			builder.append(s);
		}
		return builder.toString();
	}

	private HL7Reader loadMessage(String message) throws HL7Exception {
		Parser p = new PipeParser();
		p.setValidationContext(new NoValidation());
		message = assureSaveMessage(message);
		Message hl7Msg = p.parse(message);

		messageList.add(hl7Msg);
		return getReaderForMessage(hl7Msg);
	}

	private HL7Reader getReaderForMessage(Message message) {
		String version = message.getVersion();

		if (version.equals("2.1")) {
			return new HL7ReaderV21(message);
		} else if (version.equals("2.2")) {
			return new HL7ReaderV22(message);
		}
		if (version.equals("2.3")) {
			return new HL7ReaderV23(message);
		}
		if (version.equals("2.3.1")) {
			return new HL7ReaderV231(message);
		}
		if (version.equals("2.4")) {
			return new HL7ReaderV24(message);
		}
		if (version.equals("2.5")) {
			return new HL7ReaderV25(message);
		}
		if (version.equals("2.5.1")) {
			return new HL7ReaderV251(message);
		}
		if (version.equals("2.6")) {
			return new HL7ReaderV26(message);
		}
		return null;
	}
}
