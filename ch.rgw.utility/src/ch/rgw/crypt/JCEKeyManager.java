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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.x509.X509V1CertificateGenerator;

import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Vereinfachtes API für die Java Kryptographie-Klassen KeyManager stellt die Verbindung zu einem
 * keystore her und lässt auf die darin befindlichen Schlüssel zugreifen.
 */

public class JCEKeyManager {
	// private static final String CERTIFICATE_SIGNATURE_ALGO =
	// "SHA256WithRSAEncryption";
	private static final String CERTIFICATE_SIGNATURE_ALGO = "SHA256withRSA";
	
	public static String Version(){
		return "0.1.6";
	}
	
	protected KeyStore ks;
	private static SecureRandom _srnd;
	protected static Logger log;
	
	@SuppressWarnings("unused")
	private JCEKeyManager(){}
	
	protected char[] storePwd = null;
	protected String ksType;
	private String ksFile;
	
	static {
		log = Logger.getLogger("KeyManager");
		// Security.addProvider(new
		// org.bouncycastle.jce.provider.BouncyCastleProvider());
		// _srnd = SecureRandom.getInstance("SHA1PRNG"); // Create random
		// number generator.
		
		_srnd = new SecureRandom();
	}
	
	/**
	 * The Constructor does not actually create or access a keystore but only defines the access
	 * rules The keystore ist valid after a successful call to create() or load()
	 * 
	 * @param keystoreFile
	 *            path and name of the keystore to use if null: {user.home}/.keystore is used.
	 * @param type
	 *            type of the keystore. If NULL: jks
	 * @param keystorePwd
	 *            password for the keystore must not be null.
	 */
	public JCEKeyManager(String keystoreFile, String type, char[] keystorePwd){
		this(type, keystorePwd);
		if (StringTool.isNothing(keystoreFile)) {
			ksFile = System.getProperty("user.home") + "/.keystore";
		} else {
			ksFile = FileTool.resolveFile(keystoreFile).getAbsolutePath();
		}
		log.log(Level.FINE, "ksPathName: " + ksFile);
		
		File fks = new File(ksFile);
		if (!fks.exists()) {
			File fksPath = fks.getParentFile();
			if (!fksPath.exists()) {
				fksPath.mkdirs();
			}
		}
		
	}
	
	public JCEKeyManager(String type, char[] storepwd){
		try {
			_srnd = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			ExHandler.handle(e);
			_srnd = new SecureRandom();
		} // Create random
		if (StringTool.isNothing(type)) {
			ksType = "jks";
		} else {
			ksType = type;
		}
		storePwd = storepwd;
	}
	
	/**
	 * Keystore laden
	 */
	public boolean load(boolean bCreateIfNotExists){
		try {
			File ksf = new File(ksFile);
			if (!ksf.exists()) {
				return create(false);
			}
			ks = KeyStore.getInstance(ksType);
			ks.load(new FileInputStream(ksFile), storePwd);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			log.log(Level.SEVERE,
				"No Keystore found or could not open Keystore: " + ex.getMessage());
			return false;
		}
		return true;
	}
	
	public boolean create(boolean bDeleteIfExists){
		File ksF = new File(ksFile);
		if (ksF.exists()) {
			if (bDeleteIfExists) {
				if (!ksF.delete()) {
					return false;
				}
			} else {
				return false;
			}
		}
		if (ks == null) {
			try {
				ks = KeyStore.getInstance(ksType);
				ks.load(null, null);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return save();
	}
	
	public boolean save(){
		try {
			ks.store(new FileOutputStream(ksFile), storePwd);
			return true;
		} catch (Exception e) {
			ExHandler.handle(e);
		}
		return false;
	}
	
	public boolean isKeystoreLoaded(){
		return (ks == null) ? false : true;
	}
	
	/**
	 * Public key mit dem Alias alias holen. Es wird auf Gültigkeit des Zertifiktats getestet
	 * 
	 * @param alias
	 *            Name des gesuchten Schlüssels
	 * @return den gesuchten Schlüssel oder null - nicht gefunden
	 */
	public PublicKey getPublicKey(String alias){
		if (alias == null) {
			return null;
		}
		if (ks == null) {
			log.log(Level.WARNING, "Keystore nicht geladen");
			if (!load(true)) {
				return null;
			}
		}
		try {
			
			java.security.cert.Certificate cert = ks.getCertificate(alias);
			if (cert == null) {
				log.log(Level.WARNING, "No certificate \"" + alias + "\"found");
				return null;
			} else {
				return cert.getPublicKey();
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
		
	}
	
	public X509Certificate getCertificate(String alias){
		if (ks == null) {
			log.log(Level.WARNING, "Keystore nicht geladen");
			if (!load(true)) {
				return null;
			}
		}
		try {
			
			java.security.cert.Certificate cert = ks.getCertificate(alias);
			if (cert == null) {
				log.log(Level.WARNING, "No certificate \"" + alias + "\"found");
				return null;
			} else {
				return (X509Certificate) cert;
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
		
	}
	
	/** Public key aus einem Input Stream lesen */
	public PublicKey getPublicKey(InputStream is){
		try {
			java.security.cert.CertificateFactory cf =
				java.security.cert.CertificateFactory.getInstance("X.509");
			java.security.cert.Certificate cert = cf.generateCertificate(is);
			return cert.getPublicKey();
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
	}
	
	/**
	 * Private key mit dem Alias alias holen
	 * 
	 * @param alias
	 *            Zu holender Schlüssel
	 * @param pwd
	 *            Schlüssel-Passwort
	 * @return den Schlüssel oder null
	 */
	public PrivateKey getPrivateKey(String alias, char[] pwd){
		
		try {
			if (StringTool.isNothing(alias) || (!ks.isKeyEntry(alias))) {
				log.log(Level.WARNING, "Alias falsch oder fehlend");
				return null;
			}
			return (PrivateKey) ks.getKey(alias, pwd);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			log.log(Level.SEVERE, "Kann Key nicht laden");
			return null;
		}
	}
	
	/**
	 * Zertifikat dem keystore zufügen
	 * 
	 * @param cert
	 *            Ein X.509 Zertifikat
	 * @return true bei Erfolg
	 */
	public boolean addCertificate(X509Certificate cert){
		
		try {
			String[] n = cert.getSubjectX500Principal().getName().split(",");
			for (String sub : n) {
				if (sub.startsWith("CN")) {
					String[] fx = sub.split("\\s*=\\s*");
					if (fx.length > 1) {
						ks.setCertificateEntry(fx[1].trim(), cert);
						return true;
					}
				}
			}
			return false;
		} catch (KeyStoreException e) {
			ExHandler.handle(e);
			return false;
		}
	}
	
	/*
	 * public Certificate createCertificate(PublicKey pk, PrivateKey signingKey){ CertificateFactory
	 * cf=CertificateFactory.getInstance("X.509"); } throws InvalidKeyException,
	 * NoSuchProviderException, SignatureException {
	 */
	
	/**
	 * Generate a certificate from a public key and a signing private key.
	 * 
	 * @param pk
	 *            the key to make a certficate from
	 * @param signingKey
	 *            the signer's private key
	 * @param name
	 *            of the issuer
	 * @param name
	 *            of the certificate holder
	 * @return the signed certificate.
	 * @throws KeyStoreException
	 * 
	 */
	public X509Certificate generateCertificate(PublicKey pk, PrivateKey signingKey, String issuer,
		String subject, TimeTool ttFrom, TimeTool ttUntil) throws InvalidKeyException,
		NoSuchProviderException, SignatureException, CertificateEncodingException,
		IllegalStateException, NoSuchAlgorithmException, KeyStoreException{
		
		// generate the certificate
		X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();
		
		certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		certGen.setIssuerDN(new X500Principal("CN=" + issuer));
		if (ttFrom == null) {
			ttFrom = new TimeTool();
		}
		if (ttUntil == null) {
			ttUntil = new TimeTool(ttFrom);
			ttUntil.add(TimeTool.YEAR, 2);
		}
		certGen.setNotBefore(ttFrom.getTime());
		certGen.setNotAfter(ttUntil.getTime());
		certGen.setSubjectDN(new X500Principal("CN=" + subject));
		certGen.setPublicKey(pk);
		certGen.setSignatureAlgorithm(CERTIFICATE_SIGNATURE_ALGO);
		// X509Certificate cert = certGen.generate(signingKey, "BC");
		X509Certificate cert = certGen.generate(signingKey);
		ks.setCertificateEntry(subject, cert);
		return cert;
	}
	
	public boolean addKeyPair(PrivateKey kpriv, X509Certificate cert, char[] keyPwd)
		throws Exception{
		String alias = getName(cert);
		ks.setKeyEntry(alias, kpriv, keyPwd, new Certificate[] {
			cert
		});
		return true;
	}
	
	String getName(X509Certificate cert){
		String cn = cert.getSubjectDN().getName();
		int s = cn.indexOf('=');
		if (s != -1) {
			return cn.substring(s + 1);
		}
		return cn;
	}
	
	public boolean existsPrivate(String alias){
		try {
			return ks.isKeyEntry(alias);
		} catch (KeyStoreException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean existsCertificate(String alias){
		try {
			return ks.isCertificateEntry(alias);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return false;
		}
	}
	
	public KeyPair generateKeys(){
		try {
			KeyPairGenerator kp = KeyPairGenerator.getInstance("RSA");
			kp.initialize(1024, _srnd);
			return kp.generateKeyPair();
		} catch (Exception e) {
			ExHandler.handle(e);
		}
		return null;
		
	}
	
	/*
	 * public DHParameterSpec createParams() throws Exception{ AlgorithmParameterGenerator paramGen
	 * = AlgorithmParameterGenerator.getInstance("DH"); paramGen.init(512); AlgorithmParameters
	 * params = paramGen.generateParameters(); DHParameterSpec dhps = (DHParameterSpec)
	 * params.getParameterSpec(DHParameterSpec.class); return dhps; }
	 * 
	 * public KeyPair createKeyPair(DHParameterSpec params){ try { KeyPairGenerator kpg =
	 * KeyPairGenerator.getInstance("DiffieHellman"); if (params != null) { kpg.initialize(params);
	 * } KeyPair kp = kpg.generateKeyPair();
	 * 
	 * return kp; } catch (Exception ex) { ExHandler.handle(ex); return null; } }
	 */
	
	public SecureRandom getRandom(){
		return _srnd;
	}
	
	public boolean removeKey(String alias){
		try {
			ks.deleteEntry(alias);
			return save();
		} catch (KeyStoreException e) {
			ExHandler.handle(e);
			return false;
		}
	}
}
