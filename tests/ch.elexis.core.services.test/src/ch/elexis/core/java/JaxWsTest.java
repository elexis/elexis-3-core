package ch.elexis.core.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;

import org.junit.Test;

import com.wstutorial.ws.HelloWorld;
import com.wstutorial.ws.HelloWorldImpl;

/**
 * Publish and consume a jax ws webservice. Requires the fragment
 * <code>ch.elexis.core.services.test.ws</code> to add the package
 * <code>com.wstutorial.ws</code> to <code>info.elexis.target.jaxws.core</code>
 * in order to be able to publish the service.
 */
public class JaxWsTest {

	// https://github.com/eclipse-ee4j/metro-jax-ws/issues/128
	// https://github.com/vogellacompany/eclipsercp-javaws-java11

	@Test
	public void jaxwsFunctionality() throws MalformedURLException {

//		
//		// Publishes the SOAP Web Service
//		// If NPE here -> missing javax.xml.ws.spi.Provider#DEFAULT_JAXWSPROVIDER
//		// -> by setting System property, adding fragment jakarta.xml.ws-api-ws-fragment and updating classloader
//		// we bind this together
//		

		// Override default setting in <code>info.elexis.target.jaxws.core</code> which
		// is wrong
		System.setProperty("javax.xml.ws.spi.Provider", "com.sun.xml.ws.spi.ProviderImpl");
		Endpoint endpoint = Endpoint.publish("http://localhost:14429/ws/helloworld", new HelloWorldImpl());
		assertTrue(endpoint.isPublished());
		assertEquals("http://schemas.xmlsoap.org/wsdl/soap/http", endpoint.getBinding().getBindingID());

		// Data to access the web service
		String namespaceURI = "http://ws.wstutorial.com/";
		String servicePart = "HelloWorldImplService";
		String portName = "HelloWorldImplPort";
		QName serviceQN = new QName(namespaceURI, servicePart);
		QName portQN = new QName(namespaceURI, portName);

		URL wsdlDocumentLocation = getClass().getResource("helloworld.wsdl.xml");
		Service service = Service.create(wsdlDocumentLocation, serviceQN);
		HelloWorld testInterface = service.getPort(portQN, HelloWorld.class);

		assertEquals("Hello Junge !", testInterface.sayHelloWorld("Junge"));

		// Unpublishes the SOAP Web Service
		endpoint.stop();
		assertFalse(endpoint.isPublished());
	}

}
