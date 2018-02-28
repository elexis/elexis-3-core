package ch.rgw.tools;

import org.junit.Assert;
import org.junit.Test;


public class Test_StringTool {
	
	@Test
	public void testStringCompare(){
		
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
