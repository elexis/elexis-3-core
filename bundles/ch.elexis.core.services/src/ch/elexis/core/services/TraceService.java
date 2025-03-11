package ch.elexis.core.services;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.IUser;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.rgw.tools.net.NetTool;

@Component
public class TraceService implements ITraceService {

	private ExecutorService traceExecutor;

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	@Activate
	public void activate() {
		traceExecutor = Executors.newSingleThreadExecutor();
	}

	@Deactivate
	public void deactivate() {
		traceExecutor.shutdown();
	}

	@Override
	public void addTraceEntry(String username, String workstation, String action) {
		traceExecutor.execute(() -> {
			String _workstation = StringUtils.abbreviate(workstation, 40);
			String _username = StringUtils.abbreviate(username, 30);
			String _action = (StringUtils.isEmpty(action)) ? StringUtils.EMPTY : action;

			String insertStatement = "INSERT INTO TRACES (logtime, workstation, username, action) VALUES("
					+ System.currentTimeMillis() + ", '" + _workstation + "', '" + _username + "', '" + _action + "')";

			modelService.executeNativeUpdate(insertStatement, false);
		});

	}

	@Override
	public void addTraceEntry(String action) {
		String username = null;
		String workstation = NetTool.hostname;
		Optional<IContextService> contextService = OsgiServiceUtil.getService(IContextService.class);
		if (contextService.isPresent()) {
			Optional<IUser> user = contextService.get().getActiveUser();
			if (user.isPresent()) {
				username = user.get().getId();
			}
			OsgiServiceUtil.ungetService(contextService.get());
		}
		this.addTraceEntry(username, workstation, action);
	}

}
