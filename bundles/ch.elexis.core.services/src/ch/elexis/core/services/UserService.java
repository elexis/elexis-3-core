package ch.elexis.core.services;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.apache.commons.codec.DecoderException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IUser;
import ch.rgw.tools.PasswordEncryptionService;

@Component
public class UserService implements IUserService {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;
	
	@Override
	public boolean verifyPassword(IUser user, char[] attemptedPassword){
		boolean ret = false;
		
		if (user != null) {
			PasswordEncryptionService pes = new PasswordEncryptionService();
			try {
				ret = pes.authenticate(attemptedPassword, user.getHashedPassword(), user.getSalt());
			} catch (NoSuchAlgorithmException | InvalidKeySpecException | DecoderException e) {
				LoggerFactory.getLogger(getClass()).warn("Error verifying password for user [{}].",
					user.getLabel(), e);
			}
		}
		
		return ret;
	}
	
	@Override
	public void setPasswordForUser(IUser user, String password){
		if (user != null) {
			PasswordEncryptionService pes = new PasswordEncryptionService();
			try {
				String salt = pes.generateSaltAsHexString();
				String hashed_pw = pes.getEncryptedPasswordAsHexString(password, salt);
				user.setSalt(salt);
				user.setHashedPassword(hashed_pw);
				modelService.save(user);
			} catch (NoSuchAlgorithmException | InvalidKeySpecException | DecoderException e) {
				LoggerFactory.getLogger(getClass()).warn("Error setting password for user [{}].",
					user.getLabel(), e);
			}
		}
		
	}
	
}
