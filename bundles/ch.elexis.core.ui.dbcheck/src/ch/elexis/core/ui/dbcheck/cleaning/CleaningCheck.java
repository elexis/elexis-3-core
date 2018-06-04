package ch.elexis.core.ui.dbcheck.cleaning;

import ch.rgw.tools.JdbcLink;

public abstract class CleaningCheck {
	StringBuilder oklog;
	StringBuilder errlog;
	
	public String getErrorLog(){
		return errlog.toString();
	}
	
	public String getOutputLog(){
		return oklog.toString();
	}
	
	public abstract String cleanCoreTables(JdbcLink j);
}
