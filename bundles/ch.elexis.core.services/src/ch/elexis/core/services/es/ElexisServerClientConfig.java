package ch.elexis.core.services.es;

import org.glassfish.jersey.client.ClientConfig;

public class ElexisServerClientConfig extends ClientConfig {
	
	public ElexisServerClientConfig(){
		register(GsonProvider.class);
	}
}
