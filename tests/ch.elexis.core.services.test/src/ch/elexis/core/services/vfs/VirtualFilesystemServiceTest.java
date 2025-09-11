package ch.elexis.core.services.vfs;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import org.junit.Test;

import ch.elexis.core.eenv.AccessToken;
import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.utils.OsgiServiceUtil;

public class VirtualFilesystemServiceTest {

	IVirtualFilesystemService service = OsgiServiceUtil.getService(IVirtualFilesystemService.class).get();

	@Test
	public void of_unixoid() throws IOException {
		File file = new File("/tmp/test.txt");
		assertEquals(file, service.of("/tmp/test.txt").toFile().get());
		assertEquals(file, service.of("//tmp/test.txt").toFile().get());
		assertEquals(file, service.of("file:///tmp/test.txt").toFile().get());
	}

	@Test
	public void of_windows_share_UNC_Notation() throws IOException {
		IVirtualFilesystemHandle unc_handle = service.of("\\\\medeserv\\elexisdata\\folder_name");
		assertEquals(new URL("smb://medeserv/elexisdata/folder_name"), unc_handle.toURL());
		IVirtualFilesystemHandle subDir = unc_handle.subDir("subdir");
		assertEquals("smb://medeserv/elexisdata/folder_name/subdir/", subDir.toURL().toString());
	}

	@Test
	public void of_windows_C_Notation() throws IOException {
		IVirtualFilesystemHandle handle = service.of("C:/Windows/Test/");
		assertEquals("file://C:/Windows/Test/", handle.toURL().toString());
		IVirtualFilesystemHandle subDir = handle.subDir("subdir");
		assertEquals("file://C:/Windows/Test/subdir/", subDir.toURL().toString());
	}

	@Test
	public void of_dav_Notation() {
		// must not allow authorization
	}

	@Test
	public void of_davs_Notation() throws IOException {
		AccessToken accessToken = new AccessToken("token", new Date(), new Date(), "username", null, null);
		ContextServiceHolder.get().setTyped(accessToken);

		IVirtualFilesystemHandle handle = service.of(
				"davs://{ctx.access-token}@my.nextcloud.ch/cloud/remote.php/dav/groupfolders/{ctx.preferred-username}/");
		String string = handle.toURL().toString();
		assertEquals("davs://token@my.nextcloud.ch/cloud/remote.php/dav/groupfolders/username/", string);

		handle = service.of(
				"davs://%7Bctx.access-token%7D@my.nextcloud.ch/cloud/remote.php/dav/groupfolders/%7Bctx.preferred-username%7D/");
		string = handle.toURL().toString();
		assertEquals("davs://token@my.nextcloud.ch/cloud/remote.php/dav/groupfolders/username/", string);
	}

	@Test
	public void hidePasswordInUrlString() {
		String hidePasswordInUrlString = IVirtualFilesystemService
				.hidePasswordInUrlString("smb://username:password@testsrv/share/folder");
		assertEquals("smb://username:***@testsrv/share/folder", hidePasswordInUrlString);

		hidePasswordInUrlString = IVirtualFilesystemService.hidePasswordInUrlString("\\\\medeserv\\share\\folder");
		assertEquals("\\\\medeserv\\share\\folder", hidePasswordInUrlString);

		hidePasswordInUrlString = IVirtualFilesystemService.hidePasswordInUrlString("C:/Windows/Test");
		assertEquals("C:/Windows/Test", hidePasswordInUrlString);
	}

	@Test
	public void parseHandlingWithSpaces() throws IOException {
		IVirtualFilesystemHandle dirWithSpace = service
				.of("C:\\Users\\mad\\Documents\\Arbeit\\Testing\\Omnivore\\Abstand mit\\");
		URL url = dirWithSpace.toURL();
		assertEquals(new URL("file://C:/Users/mad/Documents/Arbeit/Testing/Omnivore/Abstand%20mit/"), url);
		IVirtualFilesystemHandle dirWithSpaceSubdir = dirWithSpace.subDir("1");
		url = dirWithSpaceSubdir.toURL();
		assertEquals(new URL("file://C:/Users/mad/Documents/Arbeit/Testing/Omnivore/Abstand%20mit/1/"), url);

		// multiple spaces
		dirWithSpace = service.of("C:\\Users\\mad\\Documents\\Arbeit\\Testing\\Omni  vore\\Abstand mit\\");
		url = dirWithSpace.toURL();
		assertEquals(new URL("file://C:/Users/mad/Documents/Arbeit/Testing/Omni%20%20vore/Abstand%20mit/"), url);
		dirWithSpaceSubdir = dirWithSpace.subDir("1");
		url = dirWithSpaceSubdir.toURL();
		assertEquals(new URL("file://C:/Users/mad/Documents/Arbeit/Testing/Omni%20%20vore/Abstand%20mit/1/"), url);
	}

}
