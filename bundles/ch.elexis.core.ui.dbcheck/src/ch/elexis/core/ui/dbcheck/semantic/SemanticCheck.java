package ch.elexis.core.ui.dbcheck.semantic;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.rgw.tools.JdbcLink;

public abstract class SemanticCheck {
	StringBuilder oklog;
	StringBuilder errlog;
	
	public String getErrorLog(){
		return errlog.toString();
	}
	
	public String getOutputLog(){
		return oklog.toString();
	}
	
	public abstract String checkSemanticStateCoreTables(JdbcLink j, IProgressMonitor monitor);
}
