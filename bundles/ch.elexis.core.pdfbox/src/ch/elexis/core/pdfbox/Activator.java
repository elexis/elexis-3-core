package ch.elexis.core.pdfbox;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;

import org.apache.pdfbox.jbig2.JBIG2ImageReaderSpi;
import org.apache.pdfbox.jbig2.util.log.LoggerFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	
	@Override
	public void start(BundleContext context) throws Exception{
		// PDFBox uses Serviceloader to access the JGIB2 implementation
		// we don't have a separate service loader plugin, so we directly
		// register within IIORegistry
		IIORegistry defaultInstance = IIORegistry.getDefaultInstance();
		boolean registerServiceProvider = defaultInstance
			.registerServiceProvider(new JBIG2ImageReaderSpi(), ImageReaderSpi.class);
		LoggerFactory.getLogger(getClass()).debug("IIORegistry registered "
			+ JBIG2ImageReaderSpi.class.getName() + " " + registerServiceProvider);
	}
	
	@Override
	public void stop(BundleContext context) throws Exception{}
	
}
