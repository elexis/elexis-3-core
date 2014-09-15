package ch.rgw.tools;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class Test_VersionInfo {

	@Test
	public void TestCompare(){
		check("1.0.0.20110910","1.0.0.20110911");
		check("1.0.0.20110910","1.0.1.20110909");
		check("1.0.2.20110910","1.1.1.20110909");
		check("1.0.0","1.0.1.20110909");
		check("1","1.0.0.1");
	}

	@Test
	public void TestMatch(){
//cm("1.2.3","1.2.*","1.3.*");
	//m("1.*","1.3.4.3","2.*.2.1");
	}
	private void check(String s1, String s2){
		VersionInfo v1=new VersionInfo(s1);
		VersionInfo v2=new VersionInfo(s2);
		assertTrue(v1.isOlder(v2));
		assertFalse(v1.isNewer(v2));
		assertFalse(v1.isEqual(v2));
		assertTrue(v2.isNewer(v1));
		assertFalse(v2.isOlder(v1));
	}
	private void cm(String s1, String s2,String s3){
		VersionInfo v1=new VersionInfo(s1);
		VersionInfo v2=new VersionInfo(s2);
		VersionInfo v3=new VersionInfo(s3);
		assertTrue(v1.matches(v2));
		assertTrue(v2.matches(v1));
		assertFalse(v1.matches(v3));
		assertFalse(v2.matches(v3));
	}
}
