package ch.elexis.core.services;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.services.vfs.VirtualFileHandle_Combined_Test;
import ch.elexis.core.services.vfs.VirtualFileHandle_FileDirectory_Test;
import ch.elexis.core.services.vfs.VirtualFileHandle_FileFile_Test;
import ch.elexis.core.services.vfs.VirtualFileHandle_SmbDirectory_Test;
import ch.elexis.core.services.vfs.VirtualFileHandle_WebdavDirectory_Test;
import ch.elexis.core.services.vfs.VirtualFileHandle_WebdavFile_Test;
import ch.elexis.core.services.vfs.VirtualFilesystemServiceTest;

@RunWith(Suite.class)
@SuiteClasses({ VirtualFilesystemServiceTest.class, VirtualFileHandle_FileFile_Test.class,
		VirtualFileHandle_FileDirectory_Test.class, VirtualFileHandle_SmbDirectory_Test.class,
		VirtualFileHandle_WebdavFile_Test.class, VirtualFileHandle_WebdavDirectory_Test.class,
		VirtualFileHandle_Combined_Test.class })
public class IVirtualFilesystemServiceTest {
}
