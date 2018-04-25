package ch.rgw.tools;

import static org.junit.Assert.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.apache.commons.codec.DecoderException;
import org.junit.Test;

public class PasswordEncryptionServiceTest {
	
	public static final String PASSWORD = "TestPassword";
	public static final String SALT = "077d65632f897a82";
	
	private PasswordEncryptionService pes = new PasswordEncryptionService();
	
	@Test
	public void testGeneratAndAuthenticatePassword() throws NoSuchAlgorithmException,
		InvalidKeySpecException{
		byte[] encryptedPassword = pes.getEncryptedPassword(PASSWORD, SALT.getBytes());
		assertTrue(pes.authenticate(PASSWORD, encryptedPassword, SALT.getBytes()));
	}
	
	@Test
	public void testGenerateAndAuthenticateHexStoredPassword() throws NoSuchAlgorithmException,
		InvalidKeySpecException, DecoderException{
		String hexEncryptedPassword = pes.getEncryptedPasswordAsHexString(PASSWORD, SALT);
		assertTrue(pes.authenticate(PASSWORD, hexEncryptedPassword, SALT));
	}
	
}
