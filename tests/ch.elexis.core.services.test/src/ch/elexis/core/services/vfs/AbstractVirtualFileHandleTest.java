package ch.elexis.core.services.vfs;

import java.io.File;
import java.nio.file.Path;

import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;

public abstract class AbstractVirtualFileHandleTest {

	static IVirtualFilesystemHandle testHandle;
	static IVirtualFilesystemService service;

	static Path tempDirectory;
	static File testDirectory;

}
