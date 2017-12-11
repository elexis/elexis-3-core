package ch.elexis.core.model;

public interface IStock {
	
	public String getId();

	public String getCode();

	public String getDriverUuid();

	public String getDriverConfig();

	public default boolean isCommissioningSystem() {
		String driverUuid = getDriverUuid();
		return (driverUuid != null && driverUuid.length() > 0);
	}

}
