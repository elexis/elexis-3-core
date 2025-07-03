package ch.rgw.tools;

import org.junit.Assert;
import org.junit.Test;

public class Test_StringTool {

	@Test
	public void testIsMailAddress() {
		// Test valid email addresses that should pass
		Assert.assertTrue("Basic email should be valid", StringTool.isMailAddress("test@example.com"));
		Assert.assertTrue("Email with dot should be valid", StringTool.isMailAddress("test.person@example.com"));
		Assert.assertTrue("Email with dash should be valid", StringTool.isMailAddress("test-person@example.com"));
		
		// Test valid email addresses with plus character (Gmail-style aliases)
		Assert.assertTrue("Email with plus should be valid", StringTool.isMailAddress("test.person+tag@gmail.com"));
		Assert.assertTrue("Email with plus and number should be valid", StringTool.isMailAddress("user+123@domain.org"));
		Assert.assertTrue("Email with plus at end should be valid", StringTool.isMailAddress("test+@example.com"));
		
		// Test invalid email addresses
		Assert.assertFalse("Null should be invalid", StringTool.isMailAddress(null));
		Assert.assertFalse("Empty string should be invalid", StringTool.isMailAddress(""));
		Assert.assertFalse("String without @ should be invalid", StringTool.isMailAddress("testexample.com"));
		Assert.assertFalse("String without domain should be invalid", StringTool.isMailAddress("test@"));
		Assert.assertFalse("String without local part should be invalid", StringTool.isMailAddress("@example.com"));
		Assert.assertFalse("String with single char TLD should be invalid", StringTool.isMailAddress("test@example.c"));
		Assert.assertFalse("String with too long TLD should be invalid", StringTool.isMailAddress("test@example.toolongext"));
	}

	@Test
	public void testStringCompare() {

		Assert.assertEquals(0, StringTool.compareNumericStrings(null, null));
		Assert.assertEquals(1, StringTool.compareNumericStrings("1", null));
		Assert.assertEquals(-1, StringTool.compareNumericStrings(null, "1"));

		Assert.assertEquals(0, StringTool.compareNumericStrings("0", "0"));
		Assert.assertEquals(0, StringTool.compareNumericStrings("0", "000"));
		Assert.assertEquals(0, StringTool.compareNumericStrings("000", "0"));
		Assert.assertEquals(1, StringTool.compareNumericStrings("1", "0"));
		Assert.assertEquals(-1, StringTool.compareNumericStrings("0", "1"));
		Assert.assertEquals(0, StringTool.compareNumericStrings("1", "1"));
		Assert.assertEquals(1, StringTool.compareNumericStrings("10", "1"));

		Assert.assertEquals(0, StringTool.compareNumericStrings("", ""));
		Assert.assertEquals(1, StringTool.compareNumericStrings("1", ""));
		Assert.assertEquals(-1, StringTool.compareNumericStrings("", "1"));

		Assert.assertEquals(-1, StringTool.compareNumericStrings("-2", "-1"));
		Assert.assertEquals(0, StringTool.compareNumericStrings("-2", "-2"));
		Assert.assertEquals(-1, StringTool.compareNumericStrings("-2", "1"));
		Assert.assertEquals(1, StringTool.compareNumericStrings("2", "-1"));

		Assert.assertEquals(-1, StringTool.compareNumericStrings("-", "1"));
		Assert.assertEquals(-1, StringTool.compareNumericStrings("+", "1"));

		Assert.assertEquals(1, StringTool.compareNumericStrings("1", "-"));
		Assert.assertEquals(1, StringTool.compareNumericStrings("1", "+"));
	}
}
