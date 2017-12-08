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

package ch.rgw.tools.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Scanner;

import ch.rgw.tools.StringTool;

/**
 * @author Gerry
 */

public class NetTool {
	static final String Version = "1.0.1";
	public static final java.util.ArrayList<String> IPs = new java.util.ArrayList<String>();
	public static String hostname;
	
	static {
		Enumeration<NetworkInterface> nis = null;
		;
		try {
			nis = NetworkInterface.getNetworkInterfaces();
			
			while (nis.hasMoreElements()) {
				NetworkInterface ni = nis.nextElement();
				Enumeration<InetAddress> ias = ni.getInetAddresses();
				while (ias.hasMoreElements()) {
					InetAddress ia = ias.nextElement();
					String ip = ia.getHostAddress();
					if (StringTool.isNothing(hostname)) {
						hostname = ia.getHostName();
					} else if (StringTool.isIPAddress(hostname)) {
						if (!StringTool.isIPAddress(ia.getHostName())) {
							hostname = ia.getHostName();
						}
					}
					IPs.add(ip);
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	// Windows only
	public static String getMacAddress() throws IOException{
		Process proc = Runtime.getRuntime().exec("cmd /c ipconfig /all");
		Scanner s = new Scanner(proc.getInputStream());
		return s.findInLine("\\p{XDigit}\\p{XDigit}(-\\p{XDigit}\\p{XDigit}){5}");
	}
	
	public static void main(String[] args) throws IOException{
		System.out.println(getMacAddress());
	}
}
