package ch.elexis.core.ui.dbcheck.syntactic;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.rgw.tools.JdbcLink;

public abstract class SyntacticCheck {
	StringBuilder oklog;
	StringBuilder errlog;
	StringBuilder fixScript;

	public abstract String checkCoreTables(JdbcLink j, IProgressMonitor monitor);

	public String getErrorLog() {
		return errlog.toString();
	}

	public String getOutputLog() {
		return oklog.toString();
	}

	public String getFixScript() {
		return fixScript.toString();
	}

	/**
	 * Handmade list of compatible dataTypes -- This is not optimal, but suffices
	 * for our situation, another solution would definitely need a lot of additional
	 * work :(
	 *
	 * @param dataTypeFound
	 * @param dataTypeRequested
	 * @author Marco Descher - yes, blame me for it ...
	 * @return
	 */
	protected static boolean isCompatible(String dataTypeFound, String dataTypeRequested) {
		if (dataTypeFound.equalsIgnoreCase(dataTypeRequested))
			return true;

		DataType found = new DataType(dataTypeFound);
		DataType requested = new DataType(dataTypeRequested);
		if (found.isCompatibleWith(requested))
			return true;

		if ("int8(19)".equalsIgnoreCase(dataTypeFound) && "bigint(20)".equalsIgnoreCase(dataTypeRequested))
			return true;
		if ("blob".equalsIgnoreCase(dataTypeFound) && "longblob".equalsIgnoreCase(dataTypeRequested))
			return true;
		if ("text".equalsIgnoreCase(dataTypeFound) && "longtext".equalsIgnoreCase(dataTypeRequested))
			return true;
		if ("text(2147483647)".equalsIgnoreCase(dataTypeFound) && "longtext".equalsIgnoreCase(dataTypeRequested))
			return true;
		if ("bytea(2147483647)".equalsIgnoreCase(dataTypeFound) && "longblob".equalsIgnoreCase(dataTypeRequested))
			return true;
		return false;
	}

}
