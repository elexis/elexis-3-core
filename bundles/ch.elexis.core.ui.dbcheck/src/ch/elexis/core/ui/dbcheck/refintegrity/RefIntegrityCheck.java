package ch.elexis.core.ui.dbcheck.refintegrity;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.rgw.tools.JdbcLink;

public abstract class RefIntegrityCheck {
	StringBuilder oklog;
	StringBuilder errlog;
	
	public String getErrorLog(){
		return errlog.toString();
	}
	
	public String getOutputLog(){
		return oklog.toString();
	}
	
	public abstract String checkReferentialIntegrityStateCoreTables(JdbcLink j,
		IProgressMonitor monitor);
}
