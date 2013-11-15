package ch.elexis.core.exceptions;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * <p>
 * Runtime Exception containing a status object describing the cause of the exception.
 * </p>
 * <p>
 * This class can be used without OSGi running.
 * </p>
 * 
 * @see IStatus
 */
@SuppressWarnings("serial")
public class PersistenceException extends RuntimeException {
	
	/** Status object. */
	private final IStatus status;
	private final int errCode;
	
	public static final int NOT_DEFINED = 0;
	public static final int DB_CONNECTION_NOT_CONFIGURED = 1;
	
	public PersistenceException(String message){
		super(message);
		errCode = NOT_DEFINED;
		status = new Status(Status.ERROR, "Persistence", message);
	}
	
	/**
	 * Creates a new exception with the given status object. The message of the given status is used
	 * as the exception message.
	 * 
	 * @param status
	 *            the status object to be associated with this exception
	 */
	public PersistenceException(IStatus status){
		super(status.getMessage());
		errCode = NOT_DEFINED;
		this.status = status;
	}
	
	/**
	 * 
	 * @param errCode
	 * @since 3.0.0
	 */
	public PersistenceException(int errCode){
		super();
		this.status = null;
		this.errCode = errCode;
	}
	
	/**
	 * Returns the cause of this exception, or <code>null</code> if none.
	 * 
	 * @return the cause for this exception
	 */
	public Throwable getCause(){
		return status.getException();
	}
	
	/**
	 * Returns the status object for this exception.
	 * 
	 * @return a status object
	 */
	public final IStatus getStatus(){
		return status;
	}
	
	/**
	 * Prints a stack trace out for the exception, and any nested exception that it may have
	 * embedded in its Status object.
	 */
	public void printStackTrace(){
		printStackTrace(System.err);
	}
	
	/**
	 * Prints a stack trace out for the exception, and any nested exception that it may have
	 * embedded in its Status object.
	 * 
	 * @param output
	 *            the stream to write to
	 */
	public void printStackTrace(PrintStream output){
		synchronized (output) {
			super.printStackTrace(output);
			printChildren(status, output);
		}
	}
	
	/**
	 * Prints a stack trace out for the exception, and any nested exception that it may have
	 * embedded in its Status object.
	 * 
	 * @param output
	 *            the stream to write to
	 */
	public void printStackTrace(PrintWriter output){
		synchronized (output) {
			super.printStackTrace(output);
			printChildren(status, output);
		}
	}
	
	static public void printChildren(IStatus status, PrintStream output){
		IStatus[] children = status.getChildren();
		if (children == null || children.length == 0)
			return;
		for (int i = 0; i < children.length; i++) {
			output.println("Contains: " + children[i].getMessage()); //$NON-NLS-1$
			Throwable exception = children[i].getException();
			if (exception != null)
				exception.printStackTrace(output);
			printChildren(children[i], output);
		}
	}
	
	static public void printChildren(IStatus status, PrintWriter output){
		IStatus[] children = status.getChildren();
		if (children == null || children.length == 0)
			return;
		for (int i = 0; i < children.length; i++) {
			output.println("Contains: " + children[i].getMessage()); //$NON-NLS-1$
			output.flush(); // call to synchronize output
			Throwable exception = children[i].getException();
			if (exception != null)
				exception.printStackTrace(output);
			printChildren(children[i], output);
		}
	}
}
