/*******************************************************************************
 * Copyright (c) 2015 MEDEVIT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 *******************************************************************************/
package ch.elexis.core.services;

/**
 * Factory for producing new {@link IFormattedOutput} implementations.
 * 
 * @author thomas
 *
 */
public interface IFormattedOutputFactory {
	/**
	 * Supported input Object types for the transformation.
	 * 
	 */
	public enum ObjectType {
			JAXB, DOM, XMLSTREAM
	}
	
	/**
	 * Supported output formats for the transformation.
	 * 
	 */
	public enum OutputType {
			PDF, PS, PNG, PCL
	}
	
	/**
	 * Returns a {@link IFormattedOutput} implementation depending on the {@link ObjectType} and
	 * {@link OutputType} parameters
	 * 
	 * @param objectType
	 * @param outputType
	 * @return implementation
	 */
	public IFormattedOutput getFormattedOutputImplementation(ObjectType objectType,
		OutputType outputType);
}
