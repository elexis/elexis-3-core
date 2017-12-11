package ch.elexis.hl7;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

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
	
	public List<HL7Reader> getReader(File file) throws IOException{
		checkClassLoader();
		
		messageList = new ArrayList<Message>();
		return load(file);
	}
	
	public @Nullable HL7Reader getReader(String message) throws IOException {
		checkClassLoader();
		
		messageList = new ArrayList<Message>();
		try {
			return loadMessage(message);
		} catch (HL7Exception e) {
			throw new IOException(HL7Exception.class.getName()+": "+e.getMessage());
		}
	}
	
	private List<HL7Reader> load(File file) throws IOException{
		if (!file.canRead()) {
			throw new IOException(MessageFormat
				.format(Messages.getString("HL7Reader_CannotReadFile"), file.getAbsolutePath()));
		}
		
		List<HL7Reader> ret = new ArrayList<HL7Reader>();
		try (InputStream inputStream = getFileInputStream(file)) {
			// HAPI utility class will iterate over the messages which appear over an InputStream
			Hl7InputStreamMessageStringIterator stringIterator =
				new Hl7InputStreamMessageStringIterator(inputStream);
				
			Parser p = new PipeParser();
			p.setValidationContext(new NoValidation());
			
			while (stringIterator.hasNext()) {
				String next = stringIterator.next();
				next = assureSaveMSH9Access(next);
				Message hl7Message = p.parse(next);
				messageList.add(hl7Message);
				ret.add(getReaderForMessage(hl7Message));
			}
			return ret;
		} catch (Exception ex) {
			throw new IOException(ex);
		}
	}
	
	private void checkClassLoader(){
		ClassLoader modelLoader = V23.class.getClassLoader();
		ClassLoader parserLoader = Parser.class.getClassLoader();
		if (modelLoader != parserLoader) {
			throw new IllegalStateException("Model and Parser loaded by different ClassLoader");
		}
	}
	
	private InputStream getFileInputStream(File file) throws IOException{
		byte[] bytes = Files.readAllBytes(file.toPath());
		CharsetDetector detector = new CharsetDetector();
		detector.setText(bytes);
		CharsetMatch match = detector.detect();
		
		if (match != null) {
			if (match.getName().contains("IBM424")) {
				logger.warn(
					"Reading HL7 file " + file.getAbsolutePath() + " with unsupported encoding "
						+ match.getName() + " - trying to use ISO-8859-1 instead");
						
				return new ByteArrayInputStream(new String(bytes, "ISO-8859-1").getBytes());
			}
			logger.info("Reading HL7 file " + file.getAbsolutePath() + " encoded " + match.getName()
				+ " language " + match.getLanguage());
			return new ByteArrayInputStream(match.getString().getBytes());
		}
		
		return new ByteArrayInputStream(bytes);
	}
	
	private String assureSaveMSH9Access(String hl7Message){
		String separator = "\r";
		String[] splitted = hl7Message.split(separator);
		if (splitted.length < 2) {
			separator = "\n";
			splitted = hl7Message.split(separator);
		}
		
		if (splitted.length < 2) {
			throw new IllegalArgumentException("Could not split message");
		}
		
		String[] mshPart = splitted[0].split("\\|", -1);
		
		if (!mshPart[8].equals("ORU^R01") && !mshPart[11].startsWith("2.5")) {
			mshPart[8] = "ORU^R01";
			splitted[0] = joinStrings(mshPart, "|");
		}
		
		// 2.3.2 is no proper Hl7 version and therefore needs to be handled as version 2.3
		if (mshPart[11].equals("2.3.2")) {
			mshPart[11] = "2.3";
			splitted[0] = joinStrings(mshPart, "|");
		}
		
		// #2747 BugFix as LabCube_SpotChemD sends occasionally SN which is not allowed for version
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
		
		return joinStrings(splitted, separator);
	}
	
	private String joinStrings(String[] array, String separator){
		StringBuilder builder = new StringBuilder();
		for (String s : array) {
			if (builder.length() != 0) {
				builder.append(separator);
			}
			builder.append(s);
		}
		return builder.toString();
	}
	
	private HL7Reader loadMessage(String message) throws HL7Exception{
		Parser p = new PipeParser();
		p.setValidationContext(new NoValidation());
		Message hl7Msg = p.parse(message);
		
		messageList.add(hl7Msg);
		return getReaderForMessage(hl7Msg);
	}
	
	private HL7Reader getReaderForMessage(Message message){
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
