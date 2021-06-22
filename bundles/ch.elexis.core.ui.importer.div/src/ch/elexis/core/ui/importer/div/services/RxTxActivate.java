package ch.elexis.core.ui.importer.div.services;

import java.util.Arrays;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ui.importer.div.rs232.Connection;
import gnu.io.DriverManager;

@Component(service = {})
public class RxTxActivate {
	
	@Activate
	public void activate(){
		Logger logger = LoggerFactory.getLogger(getClass());
		
		DriverManager.getInstance().loadDrivers();
		logger.info("RxTx activated");
		

		String[] activePorts = Connection.getComPorts();
		logger.info("Found serial ports: " + Arrays.toString(activePorts));
	}
}
