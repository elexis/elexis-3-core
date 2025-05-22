package ch.elexis.core.services.internal;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.RoleConstants;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.IUserBuilder;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.CoreUtil;
import ch.rgw.tools.TimeTool;

@Component
public class InitializeDatabaseService {

	private static final String ADMINISTRATOR = "Administrator";

	@Reference
	private IAccessControlService accessControlService;

	@Reference
	private IConfigService configService;

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Activate
	public void activate() {
		accessControlService.doPrivileged(() -> {
			if (needsInitialization()) {
				LoggerFactory.getLogger(getClass()).info("Initializing Database");
				initAdministrator();
				writeCreated();
			}
			autoCreateFirstMandator(isRunningFromScratch() || CoreUtil.isTestMode());
		});
	}

	private IMandator autoCreateFirstMandator(boolean fromScratch) {
		if (fromScratch) {
			String clientEmail = System.getProperty(ElexisSystemPropertyConstants.CLIENT_EMAIL);
			if (clientEmail == null) {
				clientEmail = "james@bond.invalid"; //$NON-NLS-1$
			}
			IMandator ret = createMandatorWithUser("007", "topsecret", "James", "Bond", clientEmail); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			IPerson person = ret.asIPerson();
			person.setTitel("Dr. med."); //$NON-NLS-1$
			person.setGender(Gender.MALE);
			person.setPhone1("0061 555 55 55"); //$NON-NLS-1$
			person.setFax("0061 555 55 56"); //$NON-NLS-1$
			person.setStreet("10, Baker Street"); //$NON-NLS-1$
			person.setZip("9999"); //$NON-NLS-1$
			person.setCity("Elexikon"); //$NON-NLS-1$
			coreModelService.save(person);
			return ret;
		} else {
			String firstMandantName = System.getProperty(ElexisSystemPropertyConstants.FIRST_MANDANT_NAME,
					StringUtils.EMPTY);
			String firstMandantEmail = System.getProperty(ElexisSystemPropertyConstants.FIRST_MANDANT_EMAIL,
					StringUtils.EMPTY);
			String firstMandantPassword = System.getProperty(ElexisSystemPropertyConstants.FIRST_MANDANT_PASSWORD,
					StringUtils.EMPTY);
			// The FirstMandantDialog requires
			// * a name
			// * a password
			// * a email address containing a '@'
			// Therefore we apply the same tests here
			if (firstMandantEmail.contains("@") && !firstMandantName.isEmpty() //$NON-NLS-1$
					&& !firstMandantPassword.isEmpty()) {
				IMandator ret = createMandatorWithUser(firstMandantName, firstMandantPassword, firstMandantName,
						firstMandantName, firstMandantEmail);
				return ret;
			}
		}
		return null;
	}

	private IMandator createMandatorWithUser(String username, String password, String firstName, String lastName,
			String email) {
		IMandator mandator = new IContactBuilder.MandatorBuilder(coreModelService, firstName, lastName).build();
		mandator.setUser(true);
		mandator.setActive(true);
		mandator.setPerson(true);
		mandator.setEmail(email);
		coreModelService.save(mandator);
		IUser user = new IUserBuilder(coreModelService, username, mandator).build();
		coreModelService.load(RoleConstants.ACCESSCONTROLE_ROLE_MEDICAL_USER, IRole.class).ifPresent(user::addRole);
		coreModelService.load(RoleConstants.ACCESSCONTROLE_ROLE_MEDICAL_PRACTITIONER, IRole.class)
				.ifPresent(user::addRole);
		coreModelService.save(user);
		return mandator;
	}

	private void initAdministrator() {
		IPerson person = coreModelService.getQuery(IPerson.class)
				.and(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.EQUALS, ADMINISTRATOR)
				.executeSingleResult().orElse(null);
		if (person == null) {
			person = new IContactBuilder.PersonBuilder(coreModelService, ADMINISTRATOR, ADMINISTRATOR, Gender.UNKNOWN)
					.build();
			person.setUser(true);
			coreModelService.save(person);
		}
		Optional<IUser> user = coreModelService.load(ADMINISTRATOR, IUser.class);
		if (user.isPresent() && user.get().getAssignedContact() == null) {
			user.get().setAssignedContact(person);
			user.get().removeRole(
					coreModelService.load(RoleConstants.ACCESSCONTROLE_ROLE_MEDICAL_USER, IRole.class).get());
			coreModelService.save(user.get());
		} else {
			throw new IllegalStateException("Incorrect DB state - No admin user found!");
		}
	}

	private void writeCreated() {
		configService.set("created", new TimeTool().toString(TimeTool.FULL_GER)); //$NON-NLS-1$

	}

	private boolean needsInitialization() {
		return configService.get("created", (String) null) == null; //$NON-NLS-1$
	}

	private boolean isRunningFromScratch() {
		return ElexisSystemPropertyConstants.RUN_MODE_FROM_SCRATCH
				.equals(System.getProperty(ElexisSystemPropertyConstants.RUN_MODE));
	}
}
