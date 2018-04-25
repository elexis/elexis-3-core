package ch.rgw.tools;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;

import ch.rgw.tools.PasswordValidator.PasswordValidationResult;

public class PasswordValidatorTest {
	
	@Test
	public void testCases(){
		
		assertEquals(Collections.singleton(PasswordValidationResult.SUCCESS),
			PasswordValidator.validateNewPass("aA1bCd2e", "aA1bCd2e"));
		
		assertEquals(Collections.singleton(PasswordValidationResult.DO_NOT_MATCH),
			PasswordValidator.validateNewPass("aA1bCddf", "aA1bCdde"));
		
		assertEquals(Collections.singleton(PasswordValidationResult.MISSING_LOWERCASE),
			PasswordValidator.validateNewPass("aA1bCdde".toUpperCase(), "aA1bCdde".toUpperCase()));
		
		assertEquals(Collections.singleton(PasswordValidationResult.MISSING_UPPERCASE),
			PasswordValidator.validateNewPass("aA1bCdde".toLowerCase(), "aA1bCdde".toLowerCase()));
		
		assertEquals(Collections.singleton(PasswordValidationResult.MISSING_NUMBER),
			PasswordValidator.validateNewPass("aAEbCdde", "aAEbCdde"));
		
		assertEquals(
			new HashSet<PasswordValidationResult>(
				Arrays.asList(PasswordValidationResult.MISSING_NUMBER,
					PasswordValidationResult.MISSING_UPPERCASE)),
			PasswordValidator.validateNewPass("aAEbCdde".toLowerCase(), "aAEbCdde".toLowerCase()));
		
		assertEquals(
			new HashSet<PasswordValidationResult>(
				Arrays.asList(PasswordValidationResult.MISSING_NUMBER,
					PasswordValidationResult.MISSING_LOWERCASE)),
			PasswordValidator.validateNewPass("aAEbCdde".toUpperCase(), "aAEbCdde".toUpperCase()));
		
		assertEquals(Collections.singleton(PasswordValidationResult.TOO_SHORT),
			PasswordValidator.validateNewPass("aA1bCdd", "aA1bCdd"));
		
		assertEquals(Collections.singleton(PasswordValidationResult.EXCLUDED_PASSWORD),
			PasswordValidator.validateNewPass("aA1bCdd", "aA1bCdd",
				new HashSet<String>(Arrays.asList("aA1bCdd"))));
	}
	
}
