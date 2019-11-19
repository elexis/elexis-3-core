/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.data.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rgw.tools.ExHandler;

import ch.rgw.tools.StringTool;
public class ProcessController {
	private String proc_result;
	private String proc_err;
	private int proc_exitCode;
	private static Logger log = LoggerFactory.getLogger(ProcessController.class.getName());
	
	public String getResult(){
		return proc_result;
	}
	
	public String getErrorString(){
		return proc_err;
	}
	
	public int getExitCode(){
		return proc_exitCode;
	}
	
	public boolean run(String program, String command, String inputStr){
		Process p;
		
		log.info("executing " + program + StringTool.space + command + ", " + inputStr);
		
		try {
			p = Runtime.getRuntime().exec(new String[] {
				program, command
			});
		} catch (IOException io) {
			ExHandler.handle(io);
			return false;
		}
		if (inputStr != null) {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			try {
				out.write(inputStr);
				out.close();
			} catch (IOException io) {
				System.out.println("Exception at write! " + io.getMessage());
				return false;
			}
		}
		
		ProcessStreamReader psr_stdout = new ProcessStreamReader(p.getInputStream(), "ERROR");
		ProcessStreamReader psr_stderr = new ProcessStreamReader(p.getErrorStream(), "OUTPUT");
		psr_stdout.start();
		psr_stderr.start();
		try {
			
			psr_stdout.join();
			psr_stderr.join();
		} catch (InterruptedException i) {
			System.out.println("Exception at join! " + i.getMessage());
			return false;
		}
		
		try {
			p.waitFor();
			
		} catch (InterruptedException i) {
			System.out.println("Exception at waitfor! " + i.getMessage());
			return false;
		}
		
		try {
			proc_exitCode = p.exitValue();
		} catch (IllegalThreadStateException itse) {
			return false;
		}
		proc_result = psr_stdout.getString();
		proc_err = psr_stderr.getString();
		
		return true;
	}
	
	class ProcessStreamReader extends Thread {
		InputStream is;
		
		String type;
		
		OutputStream os;
		
		String fullLine = StringTool.leer;
		
		/**
		 * Constructor for the ProcessStreamReader object
		 * 
		 * @param is
		 *            Description of the Parameter
		 * @param type
		 *            Description of the Parameter
		 */
		ProcessStreamReader(InputStream is, String type){
			this(is, type, null);
		}
		
		/**
		 * Constructor for the ProcessStreamReader object
		 * 
		 * @param is
		 *            Description of the Parameter
		 * @param type
		 *            Description of the Parameter
		 * @param redirect
		 *            Description of the Parameter
		 */
		ProcessStreamReader(InputStream is, String type, OutputStream redirect){
			this.is = is;
			this.type = type;
			this.os = redirect;
		}
		
		/**
		 * Main processing method for the ProcessStreamReader object
		 */
		public void run(){
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					fullLine = fullLine + line + StringTool.lf;
				}
				
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		
		/**
		 * Gets the string attribute of the ProcessStreamReader object
		 * 
		 * @return The string value
		 */
		String getString(){
			return fullLine;
		}
		
	}
	
}
