package ch.rgw.tools;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Adapted from https://codereview.stackexchange.com/questions/63283/password-validation-in-java
 * 
 * Password Validator. Requires the password to adhere to the following rules:<br>
 * <ul>
 * <li>At least 8 characters
 * <li>At least one lowercase letter
 * <li>At least one uppercase letter
 * <li>At least one number
 * </ul>
 */
public class PasswordValidator {
	
	public static enum PasswordValidationResult {
			SUCCESS, IS_EMPTY, DO_NOT_MATCH, TOO_SHORT, MISSING_UPPERCASE, MISSING_LOWERCASE,
			MISSING_NUMBER, EXCLUDED_PASSWORD
	}
	
	public static Set<PasswordValidationResult> validateNewPass(String pass1, String pass2,
		Set<String> exclusionSet){
		
		if (pass1 == null || pass2 == null) {
			return Collections.singleton(PasswordValidationResult.IS_EMPTY);
		}
		
		if (pass1.isEmpty() || pass2.isEmpty()) {
			return Collections.singleton(PasswordValidationResult.IS_EMPTY);
		}
		
		if (!pass1.equals(pass2)) {
			return Collections.singleton(PasswordValidationResult.DO_NOT_MATCH);
		}
		
		for (String value : exclusionSet) {
			if (pass1.equalsIgnoreCase(value)) {
				return Collections.singleton(PasswordValidationResult.EXCLUDED_PASSWORD);
			}
		}
		
		Set<PasswordValidationResult> result =
			new HashSet<PasswordValidator.PasswordValidationResult>();
		
		boolean pass = true;
		for (PasswordRule rule : RULES) {
			if (!rule.passRule(pass1)) {
				result.add(rule.failResult());
				pass = false;
			}
		}
		
		return pass ? Collections.singleton(PasswordValidationResult.SUCCESS) : result;
	}
	
	public static Set<PasswordValidationResult> validateNewPass(String pass1, String pass2){
		return validateNewPass(pass1, pass2, Collections.emptySet());
	}
	
	private interface PasswordRule {
		boolean passRule(String password);
		
		PasswordValidationResult failResult();
	}
	
	private abstract static class BaseRule implements PasswordRule {
		private final PasswordValidationResult failResult;
		
		BaseRule(PasswordValidationResult failResult){
			this.failResult = failResult;
		}
		
		public PasswordValidationResult failResult(){
			return failResult;
		}
	}
	
	private static final PasswordRule[] RULES = {
		new BaseRule(PasswordValidationResult.TOO_SHORT) {
			
			@Override
			public boolean passRule(String password){
				return password.length() >= 8;
			}
		},
		
		new BaseRule(PasswordValidationResult.MISSING_UPPERCASE) {
			
			private final Pattern ucletter = Pattern.compile(".*[\\p{Lu}].*");
			
			@Override
			public boolean passRule(String password){
				return ucletter.matcher(password).matches();
			}
		},
		
		new BaseRule(PasswordValidationResult.MISSING_LOWERCASE) {
			
			private final Pattern ucletter = Pattern.compile(".*[\\p{Ll}].*");
			
			@Override
			public boolean passRule(String password){
				return ucletter.matcher(password).matches();
			}
		},
		
		new BaseRule(PasswordValidationResult.MISSING_NUMBER) {
			
			private final Pattern ucletter = Pattern.compile(".*[0-9].*");
			
			@Override
			public boolean passRule(String password){
				return ucletter.matcher(password).matches();
			}
		}
	
	};
}
