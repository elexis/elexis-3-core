package ch.elexis.core.status;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;

// TODO refactor ...
public class StatusUtil {
	public static void printStatus(Logger log, IStatus status){
		print(log, "", status);
	}
	
	private static void print(Logger log, String indent, IStatus status){
		if (status.isMultiStatus()) {
			log.warn(indent + status.getMessage().replace('\n', ' '));
			String childIndent = indent + "  ";
			for (IStatus c : status.getChildren()) {
				print(log, childIndent, c);
			}
		} else {
			log.warn(indent + status.getMessage().replace('\n', ' '));
		}
	}
	
	public static void printStatus(PrintStream out, IStatus status){
		print(out, "", status);
	}
	
	private static void print(PrintStream log, String indent, IStatus status){
		if (status.isMultiStatus()) {
			log.print(indent + status.getMessage());
			String childIndent = indent + "  ";
			for (IStatus c : status.getChildren()) {
				print(log, childIndent, c);
			}
		} else {
			log.println(indent + status.getMessage());
		}
	}
	
	public static IStatus errorSeeLog(String pluginId){
		return new Status(Status.ERROR, pluginId, "Execution error, see log.");
	}
	
	public static String printStatus(IStatus status){
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			PrintStream ps = new PrintStream(baos);
			printStatus(ps, status);
			return baos.toString(Charset.defaultCharset().toString());
		} catch (IOException e) {
			return e.getMessage();
		}
	}
	
}
