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
	
}