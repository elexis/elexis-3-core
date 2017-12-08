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

package ch.rgw.crypt;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;

/**
 * A Cryptologist knows how to create keys and certificates, and encrypt, decrypt, sign and verify
 * byte arrays.
 * 
 * @author gerry
 * 
 */
public interface Cryptologist {
	
	/**
	 * encrypt a byte array
	 * 
	 * @param source
	 *            the plain bytes
	 * @param receiverKeyName
	 *            name of the receiver's public key
	 * 
	 * @return the encrypted bytes or null if encryption failed
	 */
	public byte[] encrypt(byte[] source, String receiverKeyName);
	
	public void encrypt(InputStream source, OutputStream dest, String receiverKeyName)
		throws CryptologistException;
	
	/**
	 * Sign a byte array (create and sign a MAC)
	 * 
	 * @param source
	 *            the bytes to sign
	 * @return the signature
	 */
	public byte[] sign(byte[] source);
	
	/**
	 * decrypt a byte array
	 * 
	 * @param encrypted
	 *            the encrypted bytes
	 * @return the plain array or null of decryption failed
	 */
	public Result<byte[]> decrypt(byte[] encrypted);
	
	public void decrypt(InputStream source, OutputStream dest) throws CryptologistException;
	
	public enum VERIFY_RESULT {
		OK, SIGNER_UNKNOWN, BAD_SIGNATURE, INTERNAL_ERROR
	}
	
	/**
	 * Verify a MAC
	 * 
	 * @param data
	 *            the signed data
	 * @param signature
	 *            the signed digest
	 * @param signerKeyName
	 *            name of the signer's public key
	 * @return
	 */
	public VERIFY_RESULT verify(byte[] data, byte[] signature, String signerKeyName);
	
	public boolean hasCertificateOf(String alias);
	
	public boolean hasKeyOf(String alias);
	
	public boolean addCertificate(X509Certificate cert);
	
	public boolean addCertificate(byte[] certEncoded);
	
	public boolean removeCertificate(String alias);
	
	public KeyPair generateKeys(String alias, char[] pwd, TimeTool validFrom, TimeTool validUntil);
	
	public X509Certificate getCertificate(String alias);
	
	public X509Certificate generateCertificate(PublicKey pk, String alias, TimeTool validFrom,
		TimeTool validUntil);
	
	public String getUser();
	
	public boolean isFunctional();
	
	public byte[] getCertificateEncoded(String alias) throws CryptologistException;
}
