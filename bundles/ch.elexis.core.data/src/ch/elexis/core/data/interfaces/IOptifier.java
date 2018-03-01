/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.data.interfaces;

import ch.elexis.core.jdt.Nullable;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Verrechnet;
import ch.rgw.tools.Result;

/**
 * Ein Optifier ist ein Optimizer und Verifier für Code-Systeme
 * 
 * @author gerry
 * 
 */
public interface IOptifier {
	/**
	 * Eine Konsultation optifizieren
	 */
	public Result<Object> optify(Konsultation kons);
	
	/**
	 * Eine Leistung einer Konsultation hinzufügen; die anderen Leistungen der Kons ggf. anpassen
	 * 
	 * @param code
	 *            der hinzuzufügende code
	 * @param kons
	 *            die Konsultation
	 * @return Result mit der möglicherweise veränderten Liste
	 */
	public ch.rgw.tools.Result<IVerrechenbar> add(IVerrechenbar code, Konsultation kons);
	
	/**
	 * Eine Leistung aus einer Konsultation entfernen; die Liste ggf. anpassen
	 * 
	 * @param code
	 *            der zu enfternende code
	 * @param kons
	 *            die KOnsultation
	 * @return Result mit der möglicherweise veränderten Liste
	 */
	public Result<Verrechnet> remove(Verrechnet code, Konsultation kons);
	
	/**
	 * @return the {@link Verrechnet} that may have been created during
	 *         {@link IOptifier#add(IVerrechenbar, Konsultation)}
	 * @since 3.1.0
	 */
	public @Nullable Verrechnet getCreatedVerrechnet();
	
	/**
	 * Add an object to the context of the {@link IOptifier} implementation. If a object for the
	 * provided key already exists, the value is replaced.
	 * 
	 * @param key
	 * @param value
	 */
	default void putContext(String key, Object value){
		// default do nothing implement in subclass
	}
	
	/**
	 * Add an implementation specific context object. If a object for the provided key already
	 * exists, the value is replaced.
	 */
	default void clearContext(){
		// default do nothing implement in subclass
	}
}