/*******************************************************************************
 * Copyright (c) 2015 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.jpa.entities.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts the Elexis extInfo byte array into the contained Map<Object, Object>. 
 * The method conforms to the getInfoStore.
 *
 */
@Converter
public class ElexisExtInfoMapConverter implements AttributeConverter<Map<Object, Object>, byte[]> {
	
	private static Logger log = LoggerFactory.getLogger(ElexisExtInfoMapConverter.class);
	
	/**
	 * Convert a Hashtable into a compressed byte array.
	 * 
	 * @param hash
	 *            the hashtable to store
	 * @return
	 */
	private static byte[] flatten(final Hashtable<Object, Object> hash){
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(hash.size() * 30);
			ZipOutputStream zos = new ZipOutputStream(baos);
			zos.putNextEntry(new ZipEntry("hash"));
			ObjectOutputStream oos = new ObjectOutputStream(zos);
			oos.writeObject(hash);
			zos.close();
			baos.close();
			return baos.toByteArray();
		} catch (Exception ex) {
			log.warn("Exception flattening HashTable, returning null: "+ex.getMessage());
			return null;
		}
	}
	
	/**
	 * Recreate a Hashtable from a byte array as created by flatten()
	 * 
	 * @param flat
	 *            the byte array
	 * @return the original Hashtable or null if no Hashtable could be created from the array
	 */
	@SuppressWarnings("unchecked")
	private static Hashtable<Object, Object> fold(final byte[] flat) {
		if (flat.length == 0) {
			return null;
		}
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(flat))) {
			ZipEntry entry = zis.getNextEntry();
			if (entry != null) {
				try (ObjectInputStream ois = new ObjectInputStream(zis)) {
					return (Hashtable<Object, Object>)  ois.readObject();
				}
			} else {
				return null;
			}
		} catch (IOException | ClassNotFoundException ex) {
			log.error("Exception folding byte array", ex);
			return null;
		}		
	}
	
	@Override
	public byte[] convertToDatabaseColumn(Map<Object, Object> objectValue){
		if (objectValue == null) {
			return null;
		}
		if (objectValue instanceof Hashtable) {
			Hashtable<Object, Object> ov = (Hashtable<Object, Object>) objectValue;
			return flatten(ov);
		}
		log.error("Could not convert into byte array " + objectValue);
		return null;
	}
	
	@Override
	public Map<Object, Object> convertToEntityAttribute(byte[] dataValue){
		if (dataValue == null) {
			return new Hashtable<Object, Object>();
		}
		Hashtable<Object, Object> ret = fold((byte[]) dataValue);
		if (ret == null) {
			return new Hashtable<Object, Object>();
		}
		return ret;
	}
	
}
