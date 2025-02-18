package ch.elexis.core.services.es;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.spi.ForcedAutoDiscoverable;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;

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