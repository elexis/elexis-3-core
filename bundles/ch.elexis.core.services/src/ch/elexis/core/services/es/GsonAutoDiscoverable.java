package ch.elexis.core.services.es;

import javax.inject.Singleton;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.spi.ForcedAutoDiscoverable;

public final class GsonAutoDiscoverable implements ForcedAutoDiscoverable {

	@Override
	public void configure(final FeatureContext context) {
		context.register(new GsonMessagingBinder());
	}

	private class GsonMessagingBinder extends AbstractBinder {

		@Override
		protected void configure() {
			bind(GsonProvider.class).to(MessageBodyReader.class).in(Singleton.class);
			bind(GsonProvider.class).to(MessageBodyWriter.class).in(Singleton.class);
		}
	}
}