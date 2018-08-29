package ch.elexis.core.data.interfaces;

import ch.elexis.data.Mandant;

public interface IStock {
	
	public String getId();

	public String getCode();

	public String getDriverUuid();

	public String getDriverConfig();

	public default boolean isCommissioningSystem() {
		String driverUuid = getDriverUuid();
		return (driverUuid != null && driverUuid.length() > 0);
	}

	public Integer getPriority();
	
	public Mandant getOwner();
	
}
