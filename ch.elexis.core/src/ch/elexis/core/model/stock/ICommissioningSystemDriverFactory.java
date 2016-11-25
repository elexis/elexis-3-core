package ch.elexis.core.model.stock;

import java.util.UUID;

public interface ICommissioningSystemDriverFactory {
	
	/**
	 * Uniquely identifies this "system driver" in order to create instances of it. The system may
	 * e.g. hold 2 instances of a class implementing this interface, in order to control 2 devices.
	 * 
	 * @return
	 */
	public UUID getIdentification();
	
	public String getName();
	
	public String getDescription();
	
	public ICommissioningSystemDriver createDriverInstance();
}
