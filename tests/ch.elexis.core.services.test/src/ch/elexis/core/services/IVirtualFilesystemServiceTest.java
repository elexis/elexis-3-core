package ch.elexis.core.services;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.services.vfs.VirtualFileHandle_FileDirectory_Test;
import ch.elexis.core.services.vfs.VirtualFileHandle_FileFile_Test;
import ch.elexis.core.services.vfs.VirtualFilesystemServiceTest;

@RunWith(Suite.class)
@SuiteClasses({ VirtualFilesystemServiceTest.class, VirtualFileHandle_FileFile_Test.class, VirtualFileHandle_FileDirectory_Test.class })
public class IVirtualFilesystemServiceTest {
}
