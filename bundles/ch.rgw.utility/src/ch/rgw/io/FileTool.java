/*******************************************************************************
 * Copyright (c) 2005-2013, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.rgw.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;

import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Log;
import ch.rgw.tools.StringTool;

/**
 * @author Gerry
 * 		
 *         TODO To change the template for this generated type comment go to Window - Preferences -
 *         Java - Code Style - Code Templates
 */
public class FileTool {
	public static String Version(){
		return "1.4.0";
	}
	
	private static final Log log = Log.get("FileTool");
	
	public static String DIRECTORY_SEPARATOR = File.separator;
	
	public static final String ZIP_EXTENSION = ".gz";
	
	public static final int REPLACE_IF_EXISTS = 0;
	
	public static final int BACKUP_IF_EXISTS = 1;
	
	public static final int FAIL_IF_EXISTS = 2;
	
	private static String getCorrectSeparators(final String pathOrFilename){
		return pathOrFilename.replace("\\", DIRECTORY_SEPARATOR).replace("//", DIRECTORY_SEPARATOR)
			.replace("/", DIRECTORY_SEPARATOR);
	}
	
	private static String removeMultipleSeparators(String pathOrFilename){
		String doubleSeparator = DIRECTORY_SEPARATOR + DIRECTORY_SEPARATOR;
		while (pathOrFilename.indexOf(doubleSeparator) >= 0) {
			pathOrFilename = pathOrFilename.replaceAll(doubleSeparator, DIRECTORY_SEPARATOR);
		}
		return pathOrFilename;
	}
	
	/**
	 * Retourniert Pfad ohne Dateinamen als String
	 */
	public static String getFilepath(final String filenamePath){
		String correctFilenamePath = getCorrectSeparators(filenamePath);
		
		if (correctFilenamePath.indexOf(DIRECTORY_SEPARATOR) < 0) {
			return "";
		}
		return correctFilenamePath.substring(0,
			correctFilenamePath.lastIndexOf(DIRECTORY_SEPARATOR));
	}
	
	/**
	 * Retourniert Dateinamen ohne Pfad als String
	 */
	public static String getFilename(final String filenamePath){
		String correctFilenamePath = getCorrectSeparators(filenamePath);
		
		if (correctFilenamePath.indexOf(DIRECTORY_SEPARATOR) < 0) {
			return filenamePath;
		}
		return correctFilenamePath.substring(
			correctFilenamePath.lastIndexOf(DIRECTORY_SEPARATOR) + 1, correctFilenamePath.length());
	}
	
	/**
	 * Retourniert Dateinamen ohne Pfad und Endung. Falls keine Endung vorhanden ist, wird der
	 * Dateinamen retourniert.
	 */
	public static String getNakedFilename(final String filenamePath){
		String filename = getFilename(filenamePath);
		
		if (filename.lastIndexOf(".") > 0) {
			return filename.substring(0, filename.lastIndexOf("."));
		}
		
		return filename;
	}
	
	/**
	 * Retourniert Dateiendung (mit Punkt). Falls keine Endung gefunden wird, wird ein leerer String
	 * retourniert.
	 */
	public static String getExtension(String name){
		int idx = name.lastIndexOf('.');
		if (idx == -1) {
			return "";
		}
		return name.substring(idx + 1);
	}
	
	/**
	 * Ueberprueft, ob Verzeichnis existiert. Falls nicht, wird probiert, das Verzeichnis zu
	 * erstellen.
	 * 
	 * @param path
	 *            , darf nicht null sein.
	 */
	public static void checkCreatePath(final String path) throws IllegalArgumentException{
		File dir = new File(path);
		if (dir.exists()) {
			if (!dir.isDirectory()) {
				throw new IllegalArgumentException("Eingabe ist kein gueltiges Verzeichnis", null);
			}
		} else {
			if (!dir.mkdirs()) {
				throw new IllegalArgumentException(
					"Verzeichnis <" + path + "> kann nicht erstellt werden!", null);
			}
		}
	}
	
	/**
	 * Ueberprueft ob Verzeichnis korrekt ist. Falls nicht, wird das Verzeichnis korrigiert und
	 * retourniert.
	 * 
	 * @param path
	 *            oder null
	 */
	public static String getCorrectPath(String path) throws IllegalArgumentException{
		if (path == null) {
			throw new IllegalArgumentException("Bitte geben Sie ein Verzeichnis ein!", null);
		}
		path = getCorrectSeparators(path);
		path = removeMultipleSeparators(path);
		if (!path.endsWith(DIRECTORY_SEPARATOR)) {
			path += DIRECTORY_SEPARATOR;
		}
		return path;
	}
	
	/**
	 * Ueberprueft, ob eine Datei existiert
	 */
	public static boolean doesFileExist(final String filePathName){
		if (filePathName == null) {
			return false;
		}
		File file = new File(filePathName);
		return file.isFile() && file.exists();
	}
	
	/**
	 * Ueberprueft, ob es sich um ein absolutes Verzeichnis handelt
	 */
	public static boolean isRootDir(String dir){
		return (dir.startsWith(DIRECTORY_SEPARATOR) || dir.indexOf(":") > 0);// Linux
		// &
		// Windows
		// Root
	}
	
	/**
	 * Loescht Datei
	 * 
	 * @param filePathName
	 *            Kompletter Filename mit Pfad
	 * @return true wenn geloescht, sonst false
	 */
	public static boolean deleteFile(final String filePathName) throws IllegalArgumentException{
		if (doesFileExist(filePathName)) {
			File file = new File(filePathName);
			return file.delete();
		}
		return true;
	}
	
	/**
	 * Liest gezippte Datei
	 */
	public static byte[] readZippedFile(final String filenamePath) throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] daten = new byte[1024];
		try (GZIPInputStream in = new GZIPInputStream(new FileInputStream(filenamePath))) {
			// Original-Datei mit Stream verbinden
			// Alle Daten aus der Original-Datei einlesen und
			// in die Ausgabe schreiben
			int read = 0;
			while ((read = in.read(daten, 0, 1024)) != -1)
				out.write(daten, 0, read);
		} finally {
			out.close();
		}
		return out.toByteArray();
	}
	
	/**
	 * Gibt das Basisverzeichnis von clazz resp. des Jars, in dem diese Klasse sich befindet zurück.
	 * Holt hierfür die URL der Klass und unterscheidet folgende Fälle:
	 * jar:file://netzlaufwerk/pfad/MyApp.jar file://netzlaufwerk/pfad/MyApp.class
	 * jar:file:/X:/pfad/MyApp.jar file://X:/pfad/MyApp.class
	 */
	public static String getBasePath(Class clazz){
		String raw = getClassPath(clazz);
		if (raw == null) {
			return ".";
		}
		String found = null;
		Pattern p = Pattern.compile(".*?file:(\\/{1,2})(.+?)[^\\\\\\/]+\\.(jar|class).*");
		Matcher m = p.matcher(raw);
		if (m.matches()) {
			found = m.group(2);
			if (found.matches("[a-zA-Z]:.+")) {
				return found;
			}
			return m.group(1) + found;
		}
		
		return found;
	}
	
	/**
	 * Retourniert Verzeichnis einer Klasse
	 */
	public static String getClassPath(Class clazz){
		ClassLoader loader = clazz.getClassLoader();
		if (loader == null) {
			return null;
		}
		URL url = loader.getResource(clazz.getName().replace('.', '/') + ".class");
		return (url != null) ? url.toString() : null;
	}
	
	/**
	 * Kopiert Datei
	 * <p>
	 * src
	 * </p>
	 * nach
	 * <p>
	 * dest
	 * </p>
	 * .
	 * 
	 * @param src
	 *            Quelldatei
	 * @param dest
	 *            Zieldatei
	 * @param if_exists
	 *            <br>
	 *            <li>REPLACE_IF_EXISTS</li>
	 *            <li>BACKUP_IF_EXISTS</li>
	 *            <li>FAIL_IF_EXISTS</li>
	 * @return
	 */
	public static boolean copyFile(File src, File dest, int if_exists){
		if (src.canRead() == false) {
			log.log(MessageFormat.format(Messages.FileTool_cantReadSource, //$NON-NLS-1$
				src.getAbsolutePath()), Log.ERRORS);
			return false;
		}
		if (dest.exists()) {
			String pname = dest.getAbsolutePath();
			if (pname.equalsIgnoreCase(src.getAbsolutePath())) {
				return true;
			}
			switch (if_exists) {
			case REPLACE_IF_EXISTS:
				if (dest.delete() == false) {
					log.log(MessageFormat.format(Messages.FileTool_cantDeleteTarget, //$NON-NLS-1$
						dest.getAbsolutePath()), Log.ERRORS);
					return false;
				}
				break;
			case BACKUP_IF_EXISTS:
				File bak = new File(pname + ".bak");
				if (bak.exists() == true) {
					if (bak.delete() == false) {
						log.log(MessageFormat.format(Messages.FileTool_backupExists, //$NON-NLS-1$
							bak.getAbsolutePath()), Log.ERRORS);
						return false;
					}
				}
				if (dest.renameTo(bak) == false) {
					log.log(MessageFormat.format(Messages.FileTool_cantRenameTarget, //$NON-NLS-1$
						bak.getAbsolutePath()), Log.ERRORS);
					return false;
				}
				dest = new File(pname);
				break;
			case FAIL_IF_EXISTS:
				log.log(MessageFormat.format(Messages.FileTool_targetExists, //$NON-NLS-1$
					dest.getAbsolutePath()), Log.ERRORS);
				return false;
			default:
				log.log(MessageFormat.format(Messages.FileTool_badCopyMode, //$NON-NLS-1$
					src.getAbsolutePath(), if_exists), Log.ERRORS);
				return false;
			}
		}
		
		// Copy data
		BufferedOutputStream bos = null;
		BufferedInputStream bis = null;
		try {
			if (dest.createNewFile() == false) {
				log.log(MessageFormat.format(Messages.FileTool_couldnotcreate, //$NON-NLS-1$
					dest.getAbsolutePath()), Log.ERRORS);
				return false;
			}
			if (dest.canWrite() == false) {
				log.log(MessageFormat.format(Messages.FileTool_cantWriteTarget, //$NON-NLS-1$
					dest.getAbsolutePath()), Log.ERRORS);
				return false;
			}
			
			bos = new BufferedOutputStream(new FileOutputStream(dest));
			bis = new BufferedInputStream(new FileInputStream(src));
			byte[] buffer = new byte[131072];
			while (true) {
				int r = bis.read(buffer);
				if (r == -1) {
					break;
				}
				bos.write(buffer, 0, r);
			}
		} catch (IOException ex) {
			ExHandler.handle(ex);
			log.log(MessageFormat.format(Messages.FileTool_cantCopy, //$NON-NLS-1$
				dest.getAbsolutePath(), ex.getMessage()), Log.ERRORS);
			return false;
		} finally {
			try {
				if (bis != null) {
					bis.close();
				}
				if (bos != null) {
					bos.close();
				}
			} catch (IOException e) {
				log.log(e.getMessage(), Log.WARNINGS);
			}
		}
		return true;
	}
	
	/**
	 * Kopiert Stream von
	 * 
	 * @param is
	 * @param os
	 * @throws IOException
	 */
	public static void copyStreams(InputStream is, OutputStream os) throws IOException{
		copyStreamsWithChecksum(is, os, null);
	}
	
	/**
	 * Kopiert Streams und erstellt MD5-Checksumme. Streams werden nicht geschlossen, aber output
	 * wird geflusht.
	 */
	public static byte[] copyStreamsWithChecksum(InputStream is, OutputStream os, String algo)
		throws IOException{
		MessageDigest md = null;
		if (algo != null) {
			try {
				md = MessageDigest.getInstance(algo);
			} catch (NoSuchAlgorithmException e) {
				log.log(e.getMessage(), Log.WARNINGS);
			}
		}
		BufferedOutputStream bos = null;
		BufferedInputStream bis = null;
		bos = new BufferedOutputStream(os);
		bis = new BufferedInputStream(is);
		byte[] buffer = new byte[65535];
		while (true) {
			int r = bis.read(buffer);
			if (r == -1) {
				break;
			}
			if (md != null) {
				md.update(buffer, 0, r);
			}
			bos.write(buffer, 0, r);
		}
		// bis.close(); Closing woild kill an outer zipinput or
		// objectinputstream
		bos.flush();
		// bos.close closing would kill an outer zipoutput or objectoutputstream
		if (md == null) {
			return null;
		}
		return md.digest();
	}
	
	/**
	 * Liest binaere Datei. Vorsicht bei grossen Dateien. Diese koennen zu einem OutOfMemory Error
	 * fuehren. Grosse Dateien sollten wenn moeglich in einzelnen Bloecken (InputStream) gelesen
	 * werden.
	 */
	public static byte[] readFile(final File file) throws IOException{
		FileInputStream input = null;
		byte[] daten = null;
		try {
			input = new FileInputStream(file);
			daten = new byte[input.available()];
			input.read(daten);
		} finally {
			if (input != null) {
				input.close();
			}
		}
		return daten;
	}
	
	/**
	 * Liest Text Datei
	 */
	public static String readTextFile(final File file) throws IOException{
		return readTextFile(file, Charset.defaultCharset().name());
	}
	
	/**
	 * Liest Text Datei
	 */
	public static String readTextFile(final File file, final String charsetName) throws IOException{
		byte[] text = readFile(file);
		return new String(text, charsetName);
	}
	
	/**
	 * Schreibt binaere Datei
	 */
	public static void writeFile(final File file, final byte[] daten) throws IOException{
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(file);
			output.write(daten);
		} finally {
			if (output != null) {
				output.close();
			}
		}
	}
	
	/**
	 * Schreibt Text Datei
	 */
	public static void writeTextFile(final File file, final String text) throws IOException{
		if (text != null) {
			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new FileWriter(file));
				
				bw.write(text);
			} finally {
				if (bw != null) {
					bw.close();
				}
			}
		}
	}
	
	/**
	 * Delete a directory with all of its contents and subcontents
	 * 
	 * @param Directory
	 *            to Delete
	 * @return true if successful, otherwise false
	 */
	public static boolean deltree(String d){
		File f = new File(d);
		boolean res = true;
		if (f.exists()) {
			if (f.isDirectory()) {
				String[] subs = f.list();
				for (String sub : subs) {
					if (deltree(f.getAbsolutePath() + File.separator + sub) == false) {
						res = false;
					}
				}
			}
			if (f.delete() == false) {
				res = false;
			}
		}
		return res;
	}
	
	/**
	 * TODO: Kommentar
	 */
	public static File resolveFile(String filepath){
		Pattern p = Pattern.compile("%(.+?)%");
		Matcher m = p.matcher(filepath);
		Settings env = CfgSettings.open(".environment", "System Environment für java");
		while (m.find() == true) {
			String f = m.group(1);
			String rep = env.get(f, "");
			if (StringTool.isNothing(rep)) {
				rep = System.getenv(f);
			}
			filepath = m.replaceFirst(rep);
		}
		// pathname=pathname.replaceAll("%(.+?)%",env.get("$1",""));
		log.log("Abgeleiteter Pfadname: " + filepath, Log.DEBUGMSG);
		return new File(filepath);
	}
	
	/**
	 * Generate the md5 checksum
	 * 
	 * @param file
	 * @return
	 * @since 3.0.0 this has been replaced by a java internal method to move away from bouncycastle
	 *        dependency
	 */
	public static byte[] checksum(File file){
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			FileInputStream in = new FileInputStream(file);
			byte[] arr = new byte[65535];
			int num;
			do {
				num = in.read(arr);
				if (num == -1) {
					break;
				}
				md5.update(arr, 0, num);
			} while (num == arr.length);
			in.close();
			byte[] ret = new byte[16];
			md5.digest(ret, 0, 16);
			return ret;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
	}
	
	/**
	 * doesn't work because it depends on same DIRECTORY_SEPARATORs in zipper and unzipper
	 * 
	 * Unzips a file in the file directory
	 */
	public static final void unzip(final String filenamePath) throws IOException{
		final int BUFFER = 2048;
		int count;
		byte data[] = new byte[BUFFER];
		
		if (filenamePath == null || filenamePath.length() == 0) {
			throw new IllegalArgumentException("No file to unzip!");
		}
		
		String baseZipDirName = getFilepath(filenamePath);
		String unzippedDirName = getNakedFilename(filenamePath);
		String baseUnzippedDirName =
			getFilepath(filenamePath) + DIRECTORY_SEPARATOR + unzippedDirName;
		File baseUnzippedDir = new File(baseUnzippedDirName);
		if (!baseUnzippedDir.exists()) {
			baseUnzippedDir.mkdirs();
		}
		
		FileInputStream fileInputstream = null;
		ZipInputStream zipIn = null;
		try {
			fileInputstream = new FileInputStream(filenamePath);
			zipIn = new ZipInputStream(new BufferedInputStream(fileInputstream));
			ZipEntry entry;
			while ((entry = zipIn.getNextEntry()) != null) {
				
				String entryFilenamePath = entry.getName();
				if (!entryFilenamePath.startsWith(unzippedDirName)) {
					entryFilenamePath = unzippedDirName + DIRECTORY_SEPARATOR + entryFilenamePath;
				}
				
				// Check entry sub directory
				String entryPathname = getFilepath(entryFilenamePath);
				if (entryPathname != null && entryPathname.length() > 0) {
					File entryPath = new File(baseZipDirName + DIRECTORY_SEPARATOR + entryPathname);
					if (!entryPath.exists()) {
						entryPath.mkdirs();
					}
				}
				
				// Check entry file
				String entryFilename = getFilename(entryFilenamePath);
				if (entryFilename != null && entryFilename.length() > 0) {
					File outputFile =
						new File(baseZipDirName + DIRECTORY_SEPARATOR + entryFilenamePath);
					if (!outputFile.exists()) {
						outputFile.createNewFile();
					}
					
					// write the files to the disk();
					BufferedOutputStream dest = null;
					FileOutputStream fileOutputstream = null;
					try {
						fileOutputstream = new FileOutputStream(outputFile);
						dest = new BufferedOutputStream(fileOutputstream, BUFFER);
						while ((count = zipIn.read(data, 0, BUFFER)) != -1) {
							dest.write(data, 0, count);
						}
						dest.flush();
					} finally {
						if (fileOutputstream != null) {
							fileOutputstream.close();
						}
						if (dest != null) {
							dest.close();
						}
					}
				}
			}
		} finally {
			if (fileInputstream != null) {
				fileInputstream.close();
			}
			if (zipIn != null) {
				zipIn.close();
			}
		}
	}
	
	/**
	 * unzip a file into a directory with the same name than the file and in the file's directory
	 * 
	 * public static final void unzip(final String file) throws IOException{ if (file == null ||
	 * file.length() == 0) { throw new IllegalArgumentException("No file to unzip!"); }
	 * 
	 * String baseZipDirName = getFilepath(file); String unzippedDirName = getNakedFilename(file);
	 * File baseUnzippedDir = new File(baseZipDirName, unzippedDirName); if
	 * (!baseUnzippedDir.exists()) { baseUnzippedDir.mkdirs(); }
	 * 
	 * FileInputStream fileInputstream = null; ZipInputStream zipIn = null; try { fileInputstream =
	 * new FileInputStream(file); zipIn = new ZipInputStream(new
	 * BufferedInputStream(fileInputstream)); unzip(zipIn,baseUnzippedDir); } finally { if
	 * (fileInputstream != null) { fileInputstream.close(); } if (zipIn != null) { zipIn.close(); }
	 * } }
	 * 
	 * private static void unzip(ZipInputStream zis, File directory) throws IOException { ZipEntry
	 * entry; while ((entry = zis.getNextEntry()) != null) { if (entry.isDirectory()) { File subdir
	 * = new File(directory, entry.getName()); if (!subdir.exists()) { subdir.mkdirs(); } unzip(zis,
	 * subdir); } else { FileOutputStream fos = new FileOutputStream(new File(directory,
	 * entry.getName())); FileTool.copyStreams(zis, fos); fos.close(); } }
	 * 
	 * }
	 */
	
	/**
	 * Unzips a file in the file directory
	 */
	public static final void unjar(final String filenamePath) throws IOException{
		final int BUFFER = 2048;
		int count;
		byte data[] = new byte[BUFFER];
		
		if (filenamePath == null || filenamePath.length() == 0) {
			throw new IllegalArgumentException("No file to unjar!");
		}
		
		String baseJarDirName = getFilepath(filenamePath);
		String unjaredDirName = getNakedFilename(filenamePath);
		String baseUnjaredDirName =
			getFilepath(filenamePath) + DIRECTORY_SEPARATOR + unjaredDirName;
		File baseUnjaredDir = new File(baseUnjaredDirName);
		if (!baseUnjaredDir.exists()) {
			baseUnjaredDir.mkdirs();
		}
		
		FileInputStream fileInputstream = null;
		JarInputStream jarIn = null;
		try {
			fileInputstream = new FileInputStream(filenamePath);
			jarIn = new JarInputStream(new BufferedInputStream(fileInputstream));
			JarEntry entry;
			while ((entry = jarIn.getNextJarEntry()) != null) {
				String entryFilenamePath = entry.getName();
				if (!entryFilenamePath.startsWith(unjaredDirName)) {
					entryFilenamePath = unjaredDirName + DIRECTORY_SEPARATOR + entryFilenamePath;
				}
				
				// Check entry sub directory
				String entryPathname = getFilepath(entryFilenamePath);
				if (entryPathname != null && entryPathname.length() > 0) {
					File entryPath = new File(baseJarDirName + DIRECTORY_SEPARATOR + entryPathname);
					if (!entryPath.exists()) {
						entryPath.mkdirs();
					}
				}
				
				// Check entry file
				String entryFilename = getFilename(entryFilenamePath);
				if (entryFilename != null && entryFilename.length() > 0) {
					File outputFile =
						new File(baseJarDirName + DIRECTORY_SEPARATOR + entryFilenamePath);
					if (!outputFile.exists()) {
						outputFile.createNewFile();
					}
					
					// write the files to the disk();
					BufferedOutputStream dest = null;
					FileOutputStream fileOutputstream = null;
					try {
						fileOutputstream = new FileOutputStream(outputFile);
						dest = new BufferedOutputStream(fileOutputstream, BUFFER);
						while ((count = jarIn.read(data, 0, BUFFER)) != -1) {
							dest.write(data, 0, count);
						}
						dest.flush();
					} finally {
						if (fileOutputstream != null) {
							fileOutputstream.close();
						}
						if (dest != null) {
							dest.close();
						}
					}
				}
			}
		} finally {
			if (fileInputstream != null) {
				fileInputstream.close();
			}
			if (jarIn != null) {
				jarIn.close();
			}
		}
	}
	
	/**
	 * Adds a file to a jar target.
	 */
	private static void addFileToJar(String path, File source, JarOutputStream target)
		throws IOException{
		
		if (source.isDirectory()) {
			String directory = getFilename(source.getPath());
			if (directory.length() > 0) {
				directory += "/";
				JarEntry entry = new JarEntry(path + directory);
				entry.setTime(source.lastModified());
				target.putNextEntry(entry);
				target.closeEntry();
			}
			for (File nestedFile : source.listFiles()) {
				addFileToJar(path + directory, nestedFile, target);
			}
		} else {
			String filename = getFilename(source.getPath());
			JarEntry entry = new JarEntry(path + filename);
			entry.setTime(source.lastModified());
			target.putNextEntry(entry);
			
			new BufferedInputStream(new FileInputStream(source));
			
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(source));
			try {
				byte[] buffer = new byte[1024];
				while (true) {
					int count = in.read(buffer);
					if (count == -1)
						break;
					target.write(buffer, 0, count);
				}
				target.closeEntry();
			} finally {
				if (in != null)
					in.close();
			}
		}
	}
	
	/**
	 * Adds a file to a jar target.
	 */
	private static void addFileToJar(File source, JarOutputStream target) throws IOException{
		addFileToJar("", source, target);
	}
	
	/**
	 * Returns a directory (and all subdirectories) as jar
	 */
	public static byte[] asJar(String directoryPath) throws IOException{
		String jarFilenamePath = directoryPath + ".jar";
		
		JarOutputStream jos = null;
		try {
			jos = new JarOutputStream(new FileOutputStream(jarFilenamePath));
			
			File directory = new File(directoryPath);
			for (File file : directory.listFiles()) {
				addFileToJar(file, jos);
			}
		} finally {
			if (jos != null) {
				jos.close();
			}
		}
		
		byte[] jarContent = readFile(new File(jarFilenamePath));
		deleteFile(jarFilenamePath);
		
		return jarContent;
	}
	
	/**
	 * Copies all files under srcDir to dstDir. If dstDir does not exist, it will be created.
	 */
	public static void copyDirectory(File srcDir, File dstDir) throws IOException{
		if (srcDir.isDirectory()) {
			if (!dstDir.exists()) {
				dstDir.mkdir();
			}
			
			String[] children = srcDir.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(srcDir, children[i]), new File(dstDir, children[i]));
			}
		} else {
			// Copying a File
			log.log("Copy file: " + srcDir.getPath() + " to " + dstDir.getPath(), Log.DEBUGMSG);
			copyFile(new File(srcDir.getPath()), new File(dstDir.getPath()),
				FileTool.REPLACE_IF_EXISTS);
		}
	}
	
	public static void unzip(File zipFile, File unzipDir) throws IOException{
		try (ZipFile file = new ZipFile(zipFile)) {
			FileSystem fileSystem = FileSystems.getDefault();
			// Get file entries
			Enumeration<? extends ZipEntry> entries = file.entries();
			// We will unzip files in this folder
			String uncompressedDirectory = unzipDir.getAbsolutePath();
			// Iterate over entries
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				// If directory then create a new directory in uncompressed folder
				if (entry.isDirectory()) {
					Files.createDirectories(
						fileSystem.getPath(uncompressedDirectory + entry.getName()));
				}
				// Else create the file
				else {
					try (InputStream is = file.getInputStream(entry);
							BufferedInputStream bis = new BufferedInputStream(is)) {
						String uncompressedFileName =
							uncompressedDirectory + File.separator + entry.getName();
						Path uncompressedFilePath = fileSystem.getPath(uncompressedFileName);
						// make sure directories exist
						Files.createDirectories(uncompressedFilePath.getParent());
						Files.createFile(uncompressedFilePath);
						try (FileOutputStream fileOutput =
							new FileOutputStream(uncompressedFileName)) {
							IOUtils.copy(bis, fileOutput);
						}
					}
				}
			}
		}
	}
}
