package ch.elexis.core.data.service.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.time.DateUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.services.IConflictHandler;
import ch.elexis.core.services.IConflictHandler.Result;
import ch.elexis.core.services.ILocalDocumentService;
import ch.elexis.core.utils.FileUtil;
import ch.rgw.tools.MimeTool;

@Component
public class LocalDocumentService implements ILocalDocumentService {
	
	private HashMap<Object, File> managedFiles = new HashMap<>();
	private HashMap<Object, LockResponse> managedLocks = new HashMap<>();
	
	private HashMap<Class<?>, ISaveHandler> registeredSaveHandler = new HashMap<>();
	private HashMap<Class<?>, ILoadHandler> registeredLoadHandler = new HashMap<>();
	
	@Override
	public Optional<File> getTempFile(Object documentSource){
		if (documentSource != null) {
			ILoadHandler loadHandler = getRegisteredLoadHandler(documentSource.getClass());
			if (loadHandler == null) {
				throw new IllegalStateException("No load handler for [" + documentSource + "]");
			}
			
			String fileName = System.currentTimeMillis() + "_" + getFileName(documentSource);
			InputStream in = loadHandler.load(documentSource);
			if (in != null) {
				return writeLocalTempFile(fileName, in);
			}
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<File> add(Object documentSource, IConflictHandler conflictHandler)
		throws IllegalStateException{
		boolean readOnly = false;
		if (documentSource != null) {
			LockResponse result = LocalLockServiceHolder.get().acquireLock(documentSource);
			if (result.isOk()) {
				managedLocks.put(documentSource, result);
			} else {
				readOnly = true;
			}
			
			ILoadHandler loadHandler = getRegisteredLoadHandler(documentSource.getClass());
			if (loadHandler == null) {
				throw new IllegalStateException("No load handler for [" + documentSource + "]");
			}
			
			String fileName = getFileName(documentSource);
			InputStream in = loadHandler.load(documentSource);
			if (in != null) {
				Optional<File> ret = writeLocalFile(fileName, in, conflictHandler, readOnly);
				ret.ifPresent(file -> {
					managedFiles.put(documentSource, file);
				});
				return ret;
			}
		}
		return Optional.empty();
	}
	
	private ILoadHandler getRegisteredLoadHandler(Class<? extends Object> clazz){
		ILoadHandler matchingHandler = registeredLoadHandler.get(clazz);
		if (matchingHandler == null) {
			Class<?>[] interfaces = clazz.getInterfaces();
			for (Class<?> interfaze : interfaces) {
				matchingHandler = registeredLoadHandler.get(interfaze);
				if(matchingHandler != null) {
					break;
				}
			}
		}
		return matchingHandler;
	}
	
	@Override
	public void remove(Object documentSource, IConflictHandler conflictHandler){
		// try to delete the file
		File file = managedFiles.get(documentSource);
		if (file != null && file.exists()) {
			Path path = Paths.get(file.getAbsolutePath());
			boolean deleted = false;
			while (!(deleted = tryDelete(path))) {
				Result result = conflictHandler.getResult();
				if (result == Result.OVERWRITE) {
					// try again
				} else {
					break;
				}
			}
			if (deleted) {
				removeManaged(documentSource);
			}
		} else {
			removeManaged(documentSource);
		}
	}
	
	@Override
	public void remove(Object documentSource, boolean delete){
		File file = managedFiles.get(documentSource);
		if (delete && file != null && file.exists()) {
			tryDelete(Paths.get(file.getAbsolutePath()));
		}
		removeManaged(documentSource);
	}
	
	private void removeManaged(Object documentSource){
		LockResponse lock = managedLocks.get(documentSource);
		if (lock != null) {
			LocalLockServiceHolder.get().releaseLock(documentSource);
			managedLocks.remove(documentSource);
		}
		managedFiles.remove(documentSource);
	}
	
	private boolean tryDelete(Path path){
		try {
			if (tryBackup(path)) {
				Files.delete(path);
				return true;
			}
		} catch (IOException e) {
			
		}
		return false;
	}
	
	private boolean tryBackup(Path path){
		try {
			File src = path.toFile();
			File backupDir = new File(getDocumentCachePath() + File.separator + "backup");
			deleteBackupFilesOlderThen(backupDir, 90);
			FileUtils.copyFile(src,
				new File(backupDir, "bak_" + System.currentTimeMillis() + "_" + src.getName()));
			return true;
		} catch (IOException e) {
			LoggerFactory.getLogger(getClass()).warn("Cannot create backup for file.", e);
			return false;
		}
	}
	
	private void deleteBackupFilesOlderThen(File backupDir, int days){
		try {
			if (backupDir.isDirectory()) {
				Collection<File> filesToDelete = FileUtils.listFiles(backupDir,
					new AgeFileFilter(DateUtils.addDays(new Date(), days * -1)),
					TrueFileFilter.TRUE);
				for (File file : filesToDelete) {
					boolean success = FileUtils.deleteQuietly(file);
					if (!success) {
						LoggerFactory.getLogger(getClass())
							.warn("Cannot delete old backup file at: " + file.getAbsolutePath());
					}
				}
			}
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass()).warn("Cannot delete old backup files.", e);
		}
	}
	
	@Override
	public boolean contains(Object documentSource){
		return managedFiles.containsKey(documentSource);
	}
	
	@Override
	public Optional<InputStream> getContent(Object documentSource){
		File file = managedFiles.get(documentSource);
		if (file != null) {
			try {
				return Optional.of(new ByteArrayInputStream(
					Files.readAllBytes(Paths.get(file.getAbsolutePath()))));
			} catch (IOException e) {
				LoggerFactory.getLogger(getClass()).error("Error reading file", e);
			}
		}
		return Optional.empty();
	}
	
	/**
	 * Write a local file using the filename and the content.
	 * 
	 * @param content
	 * @param fileName
	 * @param conflictHandler
	 * 
	 * @return
	 */
	private Optional<File> writeLocalFile(String fileName, InputStream content,
		IConflictHandler conflictHandler, boolean readOnly){
		Path dirPath = Paths.get(getDocumentCachePath());
		if (!Files.exists(dirPath, new LinkOption[0])) {
			try {
				Files.createDirectories(dirPath);
			} catch (IOException e) {
				LoggerFactory.getLogger(getClass()).error("Could not create directory", e);
				return Optional.empty();
			}
		}
		Path filePath = Paths.get(getDocumentCachePath() + File.separator, fileName);
		if (Files.exists(filePath)) {
			Result result = conflictHandler.getResult();
			if (result == Result.ABORT) {
				return Optional.empty();
			} else if (result == Result.KEEP) {
				return Optional.of(filePath.toFile());
			} else if (result == Result.OVERWRITE) {
				return Optional.ofNullable(writeFile(filePath, content, readOnly, false));
			}
		} else {
			return Optional.ofNullable(writeFile(filePath, content, readOnly, false));
		}
		return Optional.empty();
	}
	
	private Optional<File> writeLocalTempFile(String fileName, InputStream content){
		Path dirPath = Paths.get(getDocumentTempPath());
		if (!Files.exists(dirPath, new LinkOption[0])) {
			try {
				Files.createDirectories(dirPath);
			} catch (IOException e) {
				LoggerFactory.getLogger(getClass()).error("Could not create directory", e);
				return Optional.empty();
			}
		}
		Path filePath = Paths.get(getDocumentCachePath() + File.separator, fileName);
		return Optional.ofNullable(writeFile(filePath, content, true, true));
	}
	
	@Override
	public String getDocumentCachePath(){
		return CoreHub.getWritableUserDir().getAbsolutePath() + File.separator + ".localdoc";
		
	}
	
	private String getDocumentTempPath(){
		return CoreHub.getWritableUserDir().getAbsolutePath() + File.separator + ".localdoctemp";
		
	}
	
	private File writeFile(Path path, InputStream content, boolean readOnly, boolean deleteOnExit){
		try {
			Files.deleteIfExists(path);
			
			Path newFile = Files.createFile(path);
			Files.copy(content, newFile, StandardCopyOption.REPLACE_EXISTING);
			File ret = newFile.toFile();
			ret.setWritable(!readOnly);
			if (deleteOnExit) {
				ret.deleteOnExit();
			}
			return ret;
		} catch (IOException e) {
			LoggerFactory.getLogger(getClass()).error("Error writing file", e);
		}
		return null;
	}
	
	/**
	 * User reflection to determine a meaningful name.
	 * 
	 * @param documentSource
	 * @return
	 */
	private String getFileName(Object documentSource){
		StringBuilder sb = new StringBuilder("_");
		try {
			Method nameMethod = null;
			Method[] methods = documentSource.getClass().getMethods();
			for (Method method : methods) {
				if (isGetNameMethod(method)) {
					nameMethod = method;
					break;
				}
			}
			if (nameMethod != null) {
				sb.append(nameMethod.invoke(documentSource, new Object[0]));
			} else {
				sb.append(getDefaultFileName());
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// default
			sb.append(getDefaultFileName());
		}
		try {
			Method idMethod = null;
			Method[] methods = documentSource.getClass().getMethods();
			for (Method method : methods) {
				if (isGetIdMethod(method)) {
					idMethod = method;
					break;
				}
			}
			if (idMethod != null) {
				sb.append("[" + idMethod.invoke(documentSource, new Object[0]) + "]");
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// ignore
		}
		sb.append("_");
		try {
			Method mimeMethod = null;
			Method[] methods = documentSource.getClass().getMethods();
			for (Method method : methods) {
				if (method.getName().toLowerCase().contains("mime")
					&& method.getReturnType() == String.class) {
					mimeMethod = method;
					break;
				}
			}
			if (mimeMethod != null) {
				sb.append(
					"." + getFileEnding((String) mimeMethod.invoke(documentSource, new Object[0])));
			} else {
				sb.append(getDefaultFileEnding());
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// default
			sb.append(getDefaultFileEnding());
		}
		return FileUtil.removeInvalidChars(sb.toString());
	}
	
	private boolean isGetIdMethod(Method method){
		if (method.getParameterTypes().length > 0) {
			return false;
		}
		String lowerName = method.getName().toLowerCase();
		if (lowerName.equals("getid")) {
			return true;
		}
		return false;
	}
	
	private boolean isGetNameMethod(Method method){
		if (method.getParameterTypes().length > 0) {
			return false;
		}
		String lowerName = method.getName().toLowerCase();
		if (lowerName.contains("betreff") || lowerName.contains("titel")
			|| lowerName.contains("title")) {
			return true;
		}
		return false;
	}
	
	private String getFileEnding(String mime){
		if (mime != null) {
			if (mime.length() < 5) {
				return mime;
			} else {
				String ret = MimeTool.getExtension(mime);
				if (ret.length() > 5) {
					return getDefaultFileEnding();
				} else if (ret.isEmpty()) {
					return FilenameUtils.getExtension(mime);
				} else {
					return ret;
				}
			}
		}
		return getDefaultFileEnding();
	}
	
	private String getDefaultFileEnding(){
		return ".tmp";
	}
	
	private Object getDefaultFileName(){
		return "localFile" + System.currentTimeMillis();
	}
	
	@Override
	public List<Object> getAll(){
		return new ArrayList<>(managedFiles.keySet());
	}
	
	@Override
	public void registerSaveHandler(Class<?> clazz, ISaveHandler saveHandler){
		registeredSaveHandler.put(clazz, saveHandler);
	}
	
	@Override
	public void registerLoadHandler(Class<?> clazz, ILoadHandler saveHandler){
		registeredLoadHandler.put(clazz, saveHandler);
	}
	
	private ISaveHandler getRegisteredSaveHandler(Class<? extends Object> clazz){
		ISaveHandler matchingHandler = registeredSaveHandler.get(clazz);
		if (matchingHandler == null) {
			Class<?>[] interfaces = clazz.getInterfaces();
			for (Class<?> interfaze : interfaces) {
				matchingHandler = registeredSaveHandler.get(interfaze);
				if (matchingHandler != null) {
					break;
				}
			}
		}
		return matchingHandler;
	}
	@Override
	public boolean save(Object documentSource) throws IllegalStateException{
		ISaveHandler saveHandler = getRegisteredSaveHandler(documentSource.getClass());
		if(saveHandler != null) {
			return saveHandler.save(documentSource, this);
		}
		throw new IllegalStateException("No save handler for [" + documentSource + "]");
	}
}
