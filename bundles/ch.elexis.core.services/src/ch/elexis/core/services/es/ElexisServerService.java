package ch.elexis.core.services.es;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.jaxrs.consumer.ConsumerFactory;

import ch.elexis.core.common.ElexisEvent;
import ch.elexis.core.common.InstanceStatus;
import ch.elexis.core.constants.Elexis;
import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockRequest;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IUser;
import ch.elexis.core.server.IEventService;
import ch.elexis.core.server.IInstanceService;
import ch.elexis.core.server.ILockService;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IElexisServerService;
import ch.elexis.core.services.internal.Bundle;

@Component
public class ElexisServerService implements IElexisServerService {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	private static final UUID systemUuid = UUID.randomUUID();
	private boolean standalone = false;
	
	private String restUrl;
	final String testRestUrl = restUrl + "/elexis/lockservice/lockInfo";
	private Timer timer;
	
	private IEventService eventService = new NoRemoteEventService();
	private IInstanceService instanceService;
	private ILockService lockService = new DenyAllLockService();
	
	@Reference
	private IConfigService configService;
	@Reference
	private IContextService contextService;
	
	@Activate
	public void activate(){
		reconfigure();
		
		if (!standalone) {
			timer = new Timer();
			timer.schedule(new RefreshTask(this), 5000, 5000);
			
			InstanceStatus instanceStatus = createInstanceStatus();
			updateInstanceStatus(instanceStatus);
		}
	}
	
	@Deactivate
	public void deactivate() {
		if(timer != null) {
			timer.cancel();
		}
	}
	
	@Override
	public UUID getSystemUuid(){
		return systemUuid;
	}
	
	@Override
	public String reconfigure(){
		restUrl =
			System.getProperty(ElexisSystemPropertyConstants.ELEXIS_SERVER_REST_INTERFACE_URL);
		if (restUrl != null && restUrl.length() > 0) {
			try {
				new URL(restUrl);
			} catch (MalformedURLException e) {
				log.error("Invalid elexis-server url [{}], not initializing remote es services.",
					restUrl);
				restUrl = null;
				return null;
			}
			log.info("Operating against elexis-server instance on " + restUrl);
		} else {
			standalone = true;
			log.debug("No elexis-server url provided, operating in stand-alone mode.");
		}
		
		verifyAndUpdateConnection();
		
		return restUrl;
	}
	
	protected void verifyAndUpdateConnection() {
		if(standalone) {
			// standalone mode, mock services
			eventService = new NoRemoteEventService();
			instanceService = new NoRemoteInstanceService();
			lockService = new AcceptAllLockService();
			return;
		}  
		
		if (validateConnection()) {
			// connected to elexis-server, connection is up
			eventService = ConsumerFactory.createConsumer(restUrl, new ElexisServerClientConfig(),
				IEventService.class);
			instanceService = ConsumerFactory.createConsumer(restUrl,
				new ElexisServerClientConfig(), IInstanceService.class);
			lockService = ConsumerFactory.createConsumer(restUrl, new ElexisServerClientConfig(),
				ILockService.class);
		} else {
			// connected to elexis-server, connection is down
			eventService = new NoRemoteEventService();
			// TODO should we react otherwise here?
			instanceService = new NoRemoteInstanceService();
			lockService = new DenyAllLockService();
		}
	}
	
	@Override
	public boolean isStandalone(){
		return standalone;
	}
	
	@Override
	public InstanceStatus createInstanceStatus(){
		InstanceStatus instanceStatus = new InstanceStatus();
		instanceStatus.setState(InstanceStatus.STATE.ACTIVE);
		instanceStatus.setUuid(getSystemUuid().toString());
		instanceStatus.setVersion(Elexis.VERSION);
		instanceStatus.setOperatingSystem(
			System.getProperty("os.name") + "/" + System.getProperty("os.version") + "/"
				+ System.getProperty("os.arch") + "/J" + System.getProperty("java.version"));
		String identId = configService.getLocal(Preferences.STATION_IDENT_ID, "");
		String identTxt = configService.getLocal(Preferences.STATION_IDENT_TEXT, "");
		instanceStatus.setIdentifier(identTxt + " [" + identId + "]");
		IUser u = contextService.getActiveUser().orElse(null);
		instanceStatus.setActiveUser((u != null) ? u.getId() : "NO USER ACTIVE");
		return instanceStatus;
	}
	
	@Override
	public IStatus postEvent(ElexisEvent elexisEvent){
		if (eventService != null) {
			try {
				eventService.postEvent(elexisEvent);
				return Status.OK_STATUS;
			} catch (Exception e) {
				return new Status(Status.ERROR, Bundle.ID, e.getMessage(), e);
			}
		}
		return new Status(Status.ERROR, Bundle.ID, "No EventService available");
	}
	
	public boolean deliversRemoteEvents(){
		return !(eventService instanceof NoRemoteEventService);
	}
	
	@Override
	public Response updateInstanceStatus(InstanceStatus request){
		return instanceService.updateStatus(request);
	}
	
	@Override
	public Response getInstanceStatus(){
		return instanceService.getStatus();
	}
	
	@Override
	public LockResponse acquireOrReleaseLocks(LockRequest request){
		return lockService.acquireOrReleaseLocks(request);
	}
	
	@Override
	public boolean isLocked(LockRequest request){
		return lockService.isLocked(request);
	}
	
	@Override
	public LockInfo getLockInfo(String storeToString){
		return lockService.getLockInfo(storeToString);
	}
	
	/**
	 * Test the connection to the server, by a specific server url
	 * 
	 * @return <code>false</code> if connection could not be made
	 */
	private boolean validateConnection(){
		try {
			URL url = new URL(testRestUrl);
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			urlConn.connect();
			
			return (urlConn.getResponseCode() >= 200 && urlConn.getResponseCode() < 300);
		} catch (IOException e) {
			log.warn("Error connecting to elexis-server", e);
		}
		return false;
	}
	
}
