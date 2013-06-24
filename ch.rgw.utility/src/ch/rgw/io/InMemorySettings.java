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

import java.util.Hashtable;

import ch.rgw.tools.Log;
import ch.rgw.tools.StringTool;

/**
 * Settings-Variante, die nur im Speicher gehalten wird (kann allerdings mit toByteArray in eine
 * Persistenntform gebracht werden.
 */
public class InMemorySettings extends Settings {
	/**
	 * 
	 */
	private static final long serialVersionUID = 0xeee1231L;
	
	public static final String Version(){
		return Messages.getString("InMemorySettings.0");} //$NON-NLS-1$
	
	public InMemorySettings(){
		super();
	}
	
	/**
	 * InMemorySettings aus einem Array von Param=Wert - Paaren erstellen
	 * 
	 * @param preset
	 *            Arrays aus Strings der form Name=wert
	 */
	public InMemorySettings(String[] preset){
		for (int i = 0; i < preset.length; i++) {
			String[] pair = preset[i].split(Messages.getString("InMemorySettings.1")); //$NON-NLS-1$
			if (pair.length != 2) {
				log.error(Messages.getString("InMemorySettings.badDefinition") + preset[i]); //$NON-NLS-1$
			}
			set((pair[0]).trim(), pair[1].trim());
		}
	}
	
	/**
	 * InMemorySettings aus einem komprimierten ByteArray erstellen
	 * 
	 * @param compressed
	 *            eine mit {@link StringTool#fold(byte[], int, Object)} erstellte komprimierte
	 *            Hashtable
	 */
	public InMemorySettings(byte[] compressed){
		if (compressed != null) {
			node = StringTool.fold(compressed, StringTool.GUESS, null);
		}
	}
	
	/**
	 * InMemorySettings aus einem Vorgabestring erstellen
	 * 
	 * @param preset
	 *            ein mit {@link StringTool#enPrintable(byte[])} erstellter String
	 */
	public InMemorySettings(String preset){
		super(StringTool.dePrintable(preset));
	}
	
	/**
	 * InMemorySetting aus einer Hashtable erstellen
	 * 
	 */
	public InMemorySettings(Hashtable hash){
		super(hash);
	}
	
	/**
	 * Persistenzform als komprimiertes ByteArray ausgeben
	 * 
	 * @return ein komprimiertes ByteArray, das als Parameter für den Konstrukltor ByteArray dienen
	 *         kann.
	 */
	public byte[] toByteArray(){
		return StringTool.flatten(node, StringTool.GUESS, null);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.rgw.tools.Settings#flush()
	 */
	protected void flush_absolute(){ /* empty */
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.rgw.tools.Settings#undo()
	 */
	public void undo(){ /* empty */
	}
	
	public Hashtable getNode(){
		return node;
	}
	
}
