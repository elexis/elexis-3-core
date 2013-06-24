/*******************************************************************************
 * Copyright (c) 2005-2011, G. Weirich and Elexis
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;

/**
 * A class that creates and detects lockfile to provide cooperative detection od running instances
 * of an applicatiion (or for other purposes)
 * 
 * @author gerry
 * 
 */
public class LockFile {
	File baseDir;
	String baseName;
	int maxNum;
	int timeOutSeconds;
	
	/**
	 * Create an instance of a LockFile class. This will not yet create an actual file. A call to
	 * LockFile#lock() will create a file in the given dir and with the given name and a suffix
	 * ranging from 1 to maxNum.
	 * 
	 * @param dir
	 *            the dir where the lockfile(s) should be created
	 * @param basename
	 *            the basename for the files
	 * @param maxNum
	 *            maximum number of instances of this lock that may be aquired simultaneously
	 * @param timeoutSeconds
	 *            after what time will a lock treaded as invalid
	 */
	public LockFile(File dir, String basename, int maxNum, int timeoutSeconds){
		baseDir = dir;
		baseName = basename;
		this.maxNum = maxNum;
		this.timeOutSeconds = timeoutSeconds;
	}
	
	/**
	 * create a lockfile with the parameters as given to the constructor. There will be created at
	 * most maxNUm lockfiles. Each of them will expire after timeoutSeconds, or after the
	 * application exits. If the application does not exist normally, the lockfile might not be
	 * deleted. It will the be considered valid until the expire time is reached.
	 * 
	 * @return lock number on success, 0 if there are already maxNum lockfiles and the lock could
	 *         not be aquired
	 * @throws IOException
	 *             id something went wrong
	 */
	public int lock() throws IOException{
		int n = 1;
		
		while (n <= maxNum) {
			File file = new File(baseDir, constructFilename(n));
			if (!isLockValid(file)) {
				if (createLockfile(file)) {
					return n;
				}
			}
			n++;
		}
		return 0;
	}
	
	private boolean isLockValid(File file) throws IOException{
		if (!file.exists()) {
			return false;
		}
		if(!file.canWrite()){
			throw new IOException(("Can't write "+file.getAbsolutePath()));
		}

		TimeTool now = new TimeTool();
		DataInputStream dais = new DataInputStream(new FileInputStream(file));
		String ts = dais.readUTF();
		TimeTool tt = new TimeTool();
		dais.close();
		if (tt.set(ts)) {
			if (tt.secondsTo(now) > timeOutSeconds) {
				if (file.delete()) {
					return false;
				}
			} else {
				return true;
			}
		}
		if (file.delete()) {
			return false;
		}
		throw (new IOException("Can not delete " + file.getAbsolutePath()));
	}
	
	private boolean createLockfile(File file) throws IOException{
		if (!file.createNewFile()) {
			return false;
		}
		file.deleteOnExit();
		DataOutputStream daos = new DataOutputStream(new FileOutputStream(file));
		daos.writeUTF(new TimeTool().toString(TimeTool.FULL_ISO));
		daos.close();
		return true;
	}
	
	private String constructFilename(int n){
		return new StringBuilder().append(baseName).append(".").append(Integer.toString(n))
			.toString();
	}
	
	/**
	 * Refresh the lock i.e. extend its validity time by the original timeout once again. An
	 * application should prefer this update mechanism over a too long timeout, because in case of
	 * abnormal termination, the lock does not stay too long active.
	 * 
	 * @param n
	 *            number of the lockfile (as received by the lock() call)
	 * @return true on success
	 */
	public boolean updateLock(int n){
		File file = new File(baseDir, constructFilename(n));
		if (!file.exists()) {
			return false;
		}
		try {
			DataOutputStream daos = new DataOutputStream(new FileOutputStream(file));
			daos.writeUTF(new TimeTool().toString(TimeTool.FULL_ISO));
			daos.close();
			return true;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return false;
		}
	}
	
	/**
	 * Check if at least one lockfile with the given pattern exists
	 * 
	 * @return true if one ore more lockfiles exist
	 * @throws IOException
	 */
	public boolean existsLock() throws IOException{
		int n = 1;
		while (n <= maxNum) {
			File file = new File(baseDir, constructFilename(n));
			if (isLockValid(file)) {
				return true;
			}
		}
		return false;
	}
}
