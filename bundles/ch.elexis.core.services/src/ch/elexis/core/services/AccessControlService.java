package ch.elexis.core.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ac.ACE;
import ch.elexis.core.ac.AbstractAccessControl;
import ch.elexis.core.ac.AccessControlDefaults;
import ch.elexis.core.ac.IACLContributor;
import ch.elexis.core.constants.ExtensionPointConstantsData;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.RoleConstants;
import ch.elexis.core.services.internal.RoleBasedAccessControl;
import ch.elexis.core.utils.Extensions;

@Component
public class AccessControlService implements IAccessControlService {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;
	
	@Reference(cardinality=ReferenceCardinality.OPTIONAL)
	private IContextService contextService;
	
	private static Map<String, ACE> allDefinedACEs;
	
	private static AbstractAccessControl acl;
	
	@Activate
	private void activate(){
		acl = new RoleBasedAccessControl(modelService, contextService);
	}
	
	/**
	 * initialize all defined ACEs, only performed once
	 * 
	 * @return
	 */
	private void initAllDefinedACEs(){
		if (allDefinedACEs != null)
			return;
		
		List<ACE> temp = getACLContributionExtensions().stream()
			.flatMap(acl -> Arrays.asList(acl.getACL()).stream()).collect(Collectors.toList());
		allDefinedACEs =
			temp.stream().collect(Collectors.toMap(a -> ((ACE) a).getCanonicalName(), a -> a));
	}
	
	@SuppressWarnings("unchecked")
	private List<IACLContributor> getACLContributionExtensions(){
		return Extensions.getClasses(ExtensionPointConstantsData.ACL_CONTRIBUTION,
			ExtensionPointConstantsData.ACL_CONTRIBUTION_PT_CONTRIBUTOR);
	}
	
	@Override
	public void initializeDefaults(){
		IRole role = modelService.load(RoleConstants.SYSTEMROLE_LITERAL_USER, IRole.class).get();
		ACE[] anwender = AccessControlDefaults.getAnwender();
		Arrays.asList(anwender).stream().forEachOrdered(ace -> grant(role, ace));
		ACE[] alle = AccessControlDefaults.getAlle();
		Arrays.asList(alle).stream().forEachOrdered(ace -> grant(role, ace));
		
		grant(RoleConstants.SYSTEMROLE_LITERAL_ASSISTANT,
			AccessControlDefaults.LSTG_CHARGE_FOR_ALL);
		grant(RoleConstants.SYSTEMROLE_LITERAL_ASSISTANT, AccessControlDefaults.LSTG_VERRECHNEN);
		
		grant(RoleConstants.SYSTEMROLE_LITERAL_DOCTOR, AccessControlDefaults.USER);
		grant(RoleConstants.SYSTEMROLE_LITERAL_DOCTOR, AccessControlDefaults.MANDANT);
		grant(RoleConstants.SYSTEMROLE_LITERAL_DOCTOR,
			AccessControlDefaults.ADMIN_KONS_EDIT_IF_BILLED);
		grant(RoleConstants.SYSTEMROLE_LITERAL_EXECUTIVE_DOCTOR, AccessControlDefaults.ACE_ACCESS);
	}
	
	@Override
	public boolean request(ACE ace){
		return acl.request(ace);
	}
	
	@Override
	public boolean request(String canonicalName){
		if (canonicalName == null || canonicalName.length() < 1) {
			return false;
		}
		
		ACE aceByCanonicalName = getACEByCanonicalName(canonicalName);
		return request(aceByCanonicalName);
	}
	
	private ACE getACEByCanonicalName(String canonicalName){
		initAllDefinedACEs();
		return allDefinedACEs.get(canonicalName);
	}
	
	@Override
	public boolean request(IRole r, ACE ace){
		return acl.request(r, ace);
	}
	
	@Override
	public boolean request(IUser u, ACE ace){
		return acl.request(u, ace);
	}
	
	@Override
	public void grant(IRole role, ACE ace){
		acl.grant(role, ace);
	}
	
	@Override
	public void grant(String roleId, ACE ace){
		Optional<IRole> role = modelService.load(roleId, IRole.class);
		if (role.isPresent()) {
			acl.grant(role.get(), ace);
		} else {
			LoggerFactory.getLogger(getClass())
				.warn("Could not grant role [{}] ace [{}]: role not found", roleId, ace);
		}
	}
	
	@Override
	public void revoke(IRole role, ACE ace){
		acl.revoke(role, ace);
	}
	
	/**
	 * @return all defined ACE elements
	 * @since 3.1
	 */
	public List<ACE> getAllDefinedACElements(){
		initAllDefinedACEs();
		return new ArrayList<ACE>(allDefinedACEs.values());
	}
	
}
