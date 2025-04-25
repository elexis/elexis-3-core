package ch.elexis.core.jaxrs.filter;

import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.slf4j.Logger;

import ch.elexis.core.fhir.rdus.IResourceSynchronizer;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.builder.IUserBuilder;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.ITraceService;
import ch.elexis.core.status.ObjectStatus;
import ch.elexis.core.status.StatusUtil;
import ch.elexis.core.utils.OsgiServiceUtil;

public class ContextSettingFilterUtil {

	/**
	 * Dynamically creates user if applicable
	 * 
	 * @param email
	 */
	IUser performDynamicUserCreationIfApplicable(IModelService coreModelService, Logger logger,
			String stationIdentifier, String preferredUsername, String elexisContactId, String email) {

		if (preferredUsername == null || preferredUsername.length() > 24) {
			logger.warn("[{}] REFUSE dynamic user creation - Invalid username", preferredUsername);
			return null;
		}

		// if an elexisContactId is set, and it is valid - dynamically create user
		Optional<IContact> assignedContact = coreModelService.load(elexisContactId, IContact.class);
		if (!assignedContact.isPresent()) {
			assignedContact = tryViaRdusFhir(logger, elexisContactId);
		}
		if (!assignedContact.isPresent()) {
			logger.warn("[{}] FAIL dynamic user creation - Invalid or missing attribute elexisContactId [{}]",
					preferredUsername, elexisContactId);
			return null;
		}
		logger.info("[{}] OK dynamic user creation - assigned contact [{}]", preferredUsername, elexisContactId);
		IUser _user = new IUserBuilder(coreModelService, preferredUsername, assignedContact.get()).buildAndSave();

		ITraceService traceService = OsgiServiceUtil.getService(ITraceService.class).orElse(null);
		if (traceService != null) {
			traceService.addTraceEntry(preferredUsername, stationIdentifier,
					" Dynamic user creation [" + email + "] via ContextSettingFilter");
			OsgiServiceUtil.ungetService(traceService);
		} else {
			logger.warn("TraceService not available. Could not trace dynamic user creation [" + email + "]");
		}

		return _user;
	}

	/**
	 * Tries to synchronize the required contact from the Medelexis central FHIR
	 * repository - if the resp. service is available
	 * 
	 * @param elexisContactId
	 * @return
	 */
	private Optional<IContact> tryViaRdusFhir(Logger logger, String elexisContactId) {
		Optional<IResourceSynchronizer> fhirRdus = OsgiServiceUtil.getService(IResourceSynchronizer.class);
		if (fhirRdus.isPresent()) {
			IStatus syncStatus = fhirRdus.get().pull("Person/" + elexisContactId);
			OsgiServiceUtil.ungetService(fhirRdus.get());
			StatusUtil.logStatus(elexisContactId, logger, syncStatus, true, true);
			if (syncStatus.isOK()) {
				@SuppressWarnings("rawtypes")
				IContact contact = (IContact) ((ObjectStatus) syncStatus).get();
				return Optional.of(contact);
			}
		}

		return Optional.empty();
	}

}
