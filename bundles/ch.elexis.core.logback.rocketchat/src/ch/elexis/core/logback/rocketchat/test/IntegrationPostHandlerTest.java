package ch.elexis.core.logback.rocketchat.test;

import java.io.IOException;
import java.net.MalformedURLException;

import ch.elexis.core.logback.rocketchat.internal.IntegrationPostHandler;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;

public class IntegrationPostHandlerTest {
	
	private static Logger testLogger = new LoggerContext().getLogger(IntegrationPostHandler.class);
	
	public static final String WEBHOOK_URL = "https://rocketchat.medelexis.ch/hooks/4qyKfbsaGP4Ruwqqg/vXmDS5fW6aGkFwrLuAzZ7yPThjHGnhiG6e9y6hyBxaQ6GMt4";
	
	
	public static void main(String[] args) throws MalformedURLException, IOException{
	
		ILoggingEvent ile = new IntegrationPostHandlerTest().createNestedLoggingEvent(Level.DEBUG);
		System.out.println(new IntegrationPostHandler(ile, null, true).post(WEBHOOK_URL));
		
		ile = new IntegrationPostHandlerTest().createNestedLoggingEvent(Level.INFO);
		System.out.println(new IntegrationPostHandler(ile, null, true).post(WEBHOOK_URL));
		
		ile = new IntegrationPostHandlerTest().createNestedLoggingEvent(Level.WARN);
		System.out.println(new IntegrationPostHandler(ile, null, true).post(WEBHOOK_URL));
		
		ile = new IntegrationPostHandlerTest().createNestedLoggingEvent(Level.ERROR);
		System.out.println(new IntegrationPostHandler(ile, null, true).post(WEBHOOK_URL));
	}
	
	public ILoggingEvent createLoggingEvent(Level level){
		return createNestedLoggingEvent(level);
	}
	
	private ILoggingEvent createNestedLoggingEvent(Level level){
		return new LoggingEvent("fqcn", testLogger, level, "TEST " + level.levelStr + " MESSAGE",
			new Throwable("Diagnosis"), (Object[]) null);
	}
	
}
