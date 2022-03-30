package ch.elexis.core.services;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.rgw.tools.PasswordEncryptionService;

@Component
public class UserService implements IUserService {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	@Override
	public boolean verifyPassword(IUser user, char[] attemptedPassword) {
		boolean ret = false;

		if (user != null) {
			PasswordEncryptionService pes = new PasswordEncryptionService();
			try {
				ret = pes.authenticate(attemptedPassword, user.getHashedPassword(), user.getSalt());
			} catch (NoSuchAlgorithmException | InvalidKeySpecException | DecoderException e) {
				LoggerFactory.getLogger(getClass()).warn("Error verifying password for user [{}].", user.getLabel(), e);
			}
		}

		return ret;
	}

	@Override
	public void setPasswordForUser(IUser user, String password) {
		if (user != null) {
			PasswordEncryptionService pes = new PasswordEncryptionService();
			try {
				String salt = pes.generateSaltAsHexString();
				String hashed_pw = pes.getEncryptedPasswordAsHexString(password, salt);
				user.setSalt(salt);
				user.setHashedPassword(hashed_pw);
				modelService.save(user);
			} catch (NoSuchAlgorithmException | InvalidKeySpecException | DecoderException e) {
				LoggerFactory.getLogger(getClass()).warn("Error setting password for user [{}].", user.getLabel(), e);
			}
		}

	}

	@Override
	public Set<IMandator> getExecutiveDoctorsWorkingFor(IContact user) {
		String mandators = (String) user.getExtInfo("Mandant");
		if (mandators == null) {
			return Collections.emptySet();
		}

		List<IMandator> allActivateMandators = modelService.getQuery(IMandator.class).execute().parallelStream()
				.filter(IMandator::isActive).collect(Collectors.toList());

		List<String> mandatorsIdList = Arrays.asList(mandators.split(","));
		return allActivateMandators.stream().filter(p -> mandatorsIdList.contains(p.getLabel()))
				.collect(Collectors.toSet());
	}

	@Override
	public Optional<IMandator> getDefaultExecutiveDoctorWorkingFor(IContact userContact) {
		String defaultMandatorId = (String) userContact.getExtInfo("StdMandant");
		if (StringUtils.isNotEmpty(defaultMandatorId)) {
			return modelService.load(defaultMandatorId, IMandator.class);
		}
		return Optional.empty();
	}

	@Override
	public List<IUser> getUsersByAssociatedContact(IContact contact) {
		if (contact == null) {
			return Collections.emptyList();
		}
		IQuery<IUser> qre = modelService.getQuery(IUser.class);
		qre.and(ModelPackage.Literals.IUSER__ASSIGNED_CONTACT, COMPARATOR.EQUALS, contact);
		qre.and(ModelPackage.Literals.IUSER__ACTIVE, COMPARATOR.EQUALS, true);
		return qre.execute();
	}

}
