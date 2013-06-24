/*******************************************************************************
 * Copyright (c) 2008-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/

package ch.rgw.crypt;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import ch.rgw.crypt.Cryptologist.VERIFY_RESULT;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;
import ch.rgw.tools.SoapConverter;
import ch.rgw.tools.StringTool;

/**
 * Secure Authenticated Transmission The Transmitter stores a hashtable in an encrypted and signed
 * wrapper.
 * 
 * @author Gerry
 * 
 */
public class SAT {
	/** A key to store the return value of a call */
	public static final String KEY_RESULT = "return";
	/** A key to store error conditions */
	public static final String KEY_ERROR = "error";
	
	public static final String USER_UNKNOWN = "User unknown";
	public static final String RESULT_BAD_SIGNATURE = "Bad signature";
	public static final String ADM_TIMESTAMP = "ADM_timestamp";
	public static final String ADM_SIGNED_BY = "ADM_user";
	public static final String ADM_PAYLOAD = "ADM_payload";
	public static final String ADM_SIGNATURE = "ADM_signature";
	public static final String ERR_SERVER = "Server error: ";
	public static final String ERR_DECRYPT = "Decrypt error: ";
	private static final String VERSION = "0.3.0";
	private String ident = "xidClient";
	private String prov = "elexis.ch";
	
	Cryptologist crypt;
	
	/**
	 * Create a new SAT actor
	 * 
	 * @param c
	 *            a fully configured Cryptologist
	 */
	public SAT(Cryptologist c){
		crypt = c;
	}
	
	public SAT(String creator, String provider, Cryptologist c){
		this(c);
		ident = creator;
		prov = provider;
	}
	
	/**
	 * Decrypt, and verify a packet (using our preconfigured Cryptologist)
	 * 
	 * @param encrypted
	 *            the encrypted packet
	 * @return a hashmap with the Parameters and an additional parameter "ADM_SIGNED_BY" containing
	 *         the sender's ID
	 */
	public Map<String, Serializable> unwrap(byte[] encrypted, boolean bCheckSignature)
		throws CryptologistException{
		if (encrypted == null) {
			throw new CryptologistException("Null packet from server",
				CryptologistException.ERR_BAD_PARAMETER);
		}
		if (encrypted.length < 35) { // is probably an error message
			throw new CryptologistException(new String(encrypted),
				CryptologistException.ERR_SHORT_BLOCK);
		}
		Result<byte[]> dec = crypt.decrypt(encrypted);
		if ((dec == null) || (!dec.isOK())) {
			throw new CryptologistException("Decryption failed: " + dec == null ? "dec is null"
					: dec.toString(), CryptologistException.ERR_DECRYPTION_FAILURE);
		}
		byte[] decrypted = dec.get();
		SoapConverter sc = new SoapConverter();
		if (sc.load(decrypted)) {
			Map<String, Serializable> fields = sc.getParameters();
			String user = (String) fields.get(ADM_SIGNED_BY);
			Long ts = (Long) fields.get(ADM_TIMESTAMP);
			byte[] signature = (byte[]) fields.get(ADM_SIGNATURE);
			if ((StringTool.isNothing(user)) || (signature == null)) {
				throw new CryptologistException("Bad protocol",
					CryptologistException.ERR_BAD_PROTOCOL);
			}
			if (ts == null || ((System.currentTimeMillis() - ts) > 300000)) {
				throw new CryptologistException("timeout", CryptologistException.ERR_TIMEOUT);
			}
			Map<String, Serializable> ret = (Map<String, Serializable>) fields.get(ADM_PAYLOAD);
			if (!bCheckSignature) {
				return ret;
			}
			byte[] digest = calcDigest(sc);
			if (crypt.verify(digest, signature, user) == VERIFY_RESULT.OK) {
				ret.put(ADM_SIGNED_BY, user);
				return ret;
			} else {
				throw new CryptologistException(USER_UNKNOWN + ": " + user,
					CryptologistException.ERR_USER_UNKNOWN);
			}
		} else {
			HashMap<String, Serializable> result = new HashMap<String, Serializable>();
			result.put(KEY_ERROR, "Invalid Message");
			return result;
		}
		
	}
	
	/**
	 * Sign and encrypt a HashMap. We sign the unencrypted data and encrypt later. This imposes more
	 * load on verifying (since it must be decrypted prior to verify the signature) but improves
	 * stability against replay attacks.
	 * 
	 * @param hash
	 *            a hashtable containing arbitrary String/Object pairs. All objects must be
	 *            Serializables. Keynames starting with ADM_ are reserved and must not be used.
	 * @param dest
	 *            the receiver. The Object will be encoded with the receiver's public key
	 * @return a byte array containing the signed and encrypted Hashmap. This will remain valid for
	 *         5 Minutes.
	 * @throws Exception
	 */
	public byte[] wrap(Map<String, Serializable> var, String dest) throws CryptologistException{
		
		SoapConverter sc = new SoapConverter();
		sc.create(ident, VERSION, prov);
		try {
			sc.addMap(null, ADM_PAYLOAD, var);
		} catch (Exception ex) {
			throw new CryptologistException("Internal Cryptologist error: " + ex.getMessage(),
				CryptologistException.ERR_INTERNAL);
		}
		sc.addIntegral(ADM_TIMESTAMP, System.currentTimeMillis());
		sc.addString(ADM_SIGNED_BY, crypt.getUser());
		byte[] digest = calcDigest(sc);
		byte[] signature = crypt.sign(digest);
		sc.addArray(ADM_SIGNATURE, signature);
		String xml = sc.toString();
		byte[] wrapped = crypt.encrypt(StringTool.getBytes(xml), dest);
		if (wrapped == null) {
			throw new CryptologistException("Encry<ption failed",
				CryptologistException.ERR_ENCRYPTION_FAILURE);
		}
		return wrapped;
	}
	
	@Deprecated
	public String sendRequest(String hostaddress, String request){
		
		try {
			// Connect to server
			URLConnection conn = (new URL(hostaddress)).openConnection();
			
			// Get output stream
			conn.setDoOutput(true);
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
			
			String rx = new String(Base64Coder.encodeString(request));
			out.write("request=" + rx);
			out.close();
			StringBuilder sb = new StringBuilder();
			InputStream is = conn.getInputStream();
			int in;
			while (((in = is.read()) != -1)) {
				sb.append((byte) in);
			}
			return Base64Coder.decodeString(sb.toString()); // .decodeBuffer(conn.getInputStream());
			
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return "";
		}
		
	}
	
	public byte[] sendRequest(String hostaddress, byte[] request){
		try {
			// Connect to server
			URLConnection conn = (new URL(hostaddress)).openConnection();
			
			// Get output stream
			conn.setDoOutput(true);
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
			
			// String rx = new String(Base64Coder.encode(request));
			String rx = StringTool.enPrintableStrict(request);
			out.write("request=" + rx);
			out.close();
			// StringBuilder sb = new StringBuilder();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			InputStream is = conn.getInputStream();
			int in;
			while (((in = is.read()) != -1)) {
				// sb.append((byte) in);
				baos.write(in);
			}
			// return Base64Coder.decode(sb.toString()); //
			// .decodeBuffer(conn.getInputStream());
			return baos.toByteArray();
			
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
	}
	
	private byte[] calcDigest(SoapConverter sc){
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			Document doc = sc.getXML();
			Element eRoot = doc.getRootElement();
			Element body = eRoot.getChild("Body", SoapConverter.ns);
			addParameters(body, digest);
			return digest.digest();
		} catch (NoSuchAlgorithmException e) {
			ExHandler.handle(e);
		}
		return null;
	}
	
	private void addParameters(Element e, MessageDigest digest){
		List<Element> params = e.getChildren("parameter", SoapConverter.ns);
		for (Element el : params) {
			String type = el.getAttributeValue("type");
			String name = el.getAttributeValue("name");
			if (type.equalsIgnoreCase(SoapConverter.TYPE_MAP)) {
				addParameters(el, digest);
			} else if (name.equalsIgnoreCase(ADM_SIGNATURE)) {
				continue;
			} else {
				digest.update(StringTool.getBytes(type));
				digest.update(StringTool.getBytes(name));
				digest.update(StringTool.getBytes(el.getTextTrim()));
			}
		}
	}
}
