package ch.elexis.core.services.es;

import org.apache.commons.lang3.StringUtils;
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
import ch.elexis.core.common.ElexisEventTopics;
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
	private static boolean standalone;

	private String restUrl;
	private Timer timer;

	private IEventService eventService;
	private IInstanceService instanceService;
	private ILockService lockService;

	private ConnectionStatus connectionStatus;

	@Reference
	private IConfigService configService;
	@Reference
	private IContextService contextService;

	@Activate
	public void activate() {
		standalone = false;
		lockService = new DenyAllLockService();
		eventService = new NoRemoteEventService();
		instanceService = new NoRemoteInstanceService();

		initializeProperties();

		if (standalone) {
			// standalone mode, mock services
			eventService = new NoRemoteEventService();
			instanceService = new NoRemoteInstanceService();
			lockService = new AcceptAllLockService();
		} else {
			timer = new Timer();
			timer.schedule(new RefreshTask(this), 5000, 5000);

			InstanceStatus instanceStatus = createInstanceStatus();
			updateInstanceStatus(instanceStatus);
		}
	}

	@Deactivate
	public void deactivate() {
		if (timer != null) {
			timer.cancel();
		}
	}

	@Override
	public UUID getSystemUuid() {
		return systemUuid;
	}

	private void initializeProperties() {
		restUrl = System.getProperty(ElexisSystemPropertyConstants.ELEXIS_SERVER_REST_INTERFACE_URL);
		if (restUrl != null && restUrl.length() > 0) {
			try {
				new URL(restUrl);
			} catch (MalformedURLException e) {
				log.error("Invalid elexis-server url [{}], not initializing remote es services.", restUrl);
				restUrl = null;
			}
			connectionStatus = ConnectionStatus.LOCAL;
			log.info("Operating against elexis-server instance on " + restUrl);
		} else {
			standalone = true;
			connectionStatus = ConnectionStatus.STANDALONE;
			log.debug("No elexis-server url provided, operating in stand-alone mode.");
		}
	}

	@Override
	public boolean isStandalone() {
		return standalone;
	}

	@Override
	public InstanceStatus createInstanceStatus() {
		InstanceStatus instanceStatus = new InstanceStatus();
		instanceStatus.setState(InstanceStatus.STATE.ACTIVE);
		instanceStatus.setUuid(getSystemUuid().toString());
		instanceStatus.setVersion(Elexis.VERSION);
		instanceStatus.setOperatingSystem(System.getProperty("os.name") + "/" + System.getProperty("os.version") + "/"
				+ System.getProperty("os.arch") + "/J" + System.getProperty("java.version"));
		String identId = configService.getLocal(Preferences.STATION_IDENT_ID, StringUtils.EMPTY);
		String identTxt = configService.getLocal(Preferences.STATION_IDENT_TEXT, StringUtils.EMPTY);
		instanceStatus.setIdentifier(identTxt + " [" + identId + "]");
		IUser u = contextService.getActiveUser().orElse(null);
		instanceStatus.setActiveUser((u != null) ? u.getId() : "NO USER ACTIVE");
		return instanceStatus;
	}

	@Override
	public IStatus postEvent(ElexisEvent elexisEvent) {
		if (eventService != null) {
			try {
				elexisEvent.putProperty("systemuuid", systemUuid.toString());
				eventService.postEvent(elexisEvent);
				return Status.OK_STATUS;
			} catch (Exception e) {
				return new Status(Status.ERROR, Bundle.ID, e.getMessage(), e);
			}
		}
		return new Status(Status.ERROR, Bundle.ID, "No EventService available");
	}

	@Override
	public boolean deliversRemoteEvents() {
		return !(eventService instanceof NoRemoteEventService);
	}

	@Override
	public Response updateInstanceStatus(InstanceStatus request) {
		return instanceService.updateStatus(request);
	}

	@Override
	public Response getInstanceStatus() {
		return instanceService.getStatus();
	}

	@Override
	public LockResponse acquireOrReleaseLocks(LockRequest request) {
		return lockService.acquireOrReleaseLocks(request);
	}

	@Override
	public boolean isLocked(LockRequest request) {
		return lockService.isLocked(request);
	}

	@Override
	public LockInfo getLockInfo(String storeToString) {
		return lockService.getLockInfo(storeToString);
	}

	@Override
	public synchronized boolean validateElexisServerConnection() {
		if (ConnectionStatus.STANDALONE == connectionStatus) {
			return true;
		}

		boolean connectionOk = false;
		try {
			String testRestUrl = restUrl + "/elexis/lockservice/lockInfo";
			URL url = new URL(testRestUrl);

			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			urlConn.setConnectTimeout(1000);
			urlConn.setReadTimeout(1500);
			urlConn.connect();

			connectionOk = (urlConn.getResponseCode() >= 200 && urlConn.getResponseCode() < 300);
		} catch (IOException e) {
			log.warn("Error connecting to elexis-server", e);
		}

		if (connectionOk && connectionStatus != ConnectionStatus.REMOTE) {
			// connected to elexis-server, connection is up
			connectionStatus = ConnectionStatus.REMOTE;
			eventService = ConsumerFactory.createConsumer(restUrl, new ElexisServerClientConfig(), IEventService.class);
			contextService.postEvent(ElexisEventTopics.EVENT_RELOAD, IEventService.class);
			instanceService = ConsumerFactory.createConsumer(restUrl, new ElexisServerClientConfig(),
					IInstanceService.class);
			contextService.postEvent(ElexisEventTopics.EVENT_RELOAD, IInstanceService.class);
			lockService = ConsumerFactory.createConsumer(restUrl, new ElexisServerClientConfig(), ILockService.class);
			contextService.postEvent(ElexisEventTopics.EVENT_RELOAD, ILockService.class);
		}

		if (!connectionOk && connectionStatus != ConnectionStatus.LOCAL) {
			// connected to elexis-server, connection is down
			connectionStatus = ConnectionStatus.LOCAL;
			eventService = new NoRemoteEventService();
			contextService.postEvent(ElexisEventTopics.EVENT_RELOAD, IEventService.class);
			// TODO should we react otherwise here?
			instanceService = new NoRemoteInstanceService();
			contextService.postEvent(ElexisEventTopics.EVENT_RELOAD, IInstanceService.class);
			lockService = new DenyAllLockService();
			contextService.postEvent(ElexisEventTopics.EVENT_RELOAD, ILockService.class);
		}

		return connectionOk;
	}

	@Override
	public ConnectionStatus getConnectionStatus() {
		return connectionStatus;
	}

}
