package ch.elexis.core.data.server;

import org.glassfish.jersey.client.ClientConfig;

public class ElexisServerClientConfig extends ClientConfig {
	
	public ElexisServerClientConfig(){
		register(GsonProvider.class);
	}
}
